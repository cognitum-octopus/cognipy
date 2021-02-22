using CogniPy.CNL.DL;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;

namespace CogniPy.CNL.EN
{
    public class InvTransform : CogniPy.CNL.EN.IVisitor
    {
        public enum EntityKind { Concept, AnyRole, DataRole, DataType, Instance, Annotation }

        private endict.WordKind markerForm = endict.WordKind.NormalForm;
        private EntityKind markerKind = EntityKind.Concept;
        private string marker = null;
        string ToDL(string name, CogniPy.ARS.EntityKind kind, endict.WordKind form)
        {
            EntityKind newKind;
            switch (kind)
            {
                case CogniPy.ARS.EntityKind.Concept:
                    newKind = EntityKind.Concept;
                    break;
                case CogniPy.ARS.EntityKind.DataRole:
                    newKind = EntityKind.DataRole;
                    break;
                case CogniPy.ARS.EntityKind.Role:
                    newKind = EntityKind.AnyRole;
                    break;
                case CogniPy.ARS.EntityKind.DataType:
                    newKind = EntityKind.DataType;
                    break;
                case CogniPy.ARS.EntityKind.SWRLVariable:
                    throw new Exception("Cannot translate from SWRLVariable to EN");
                case CogniPy.ARS.EntityKind.Instance:
                    newKind = EntityKind.Instance;
                    break;
                case CogniPy.ARS.EntityKind.Annotation:
                    newKind = EntityKind.Annotation;
                    break;
                default:
                    throw new Exception("Don't know what to do with " + kind.ToString());
            }

            return ToDL(name, newKind, form);
        }

        string ToDL(string name, EntityKind kind, endict.WordKind form)
        {
            if (marker != null && name == marker)
            {
                markerKind = (kind == EntityKind.AnyRole) ? (isDataRoleStatement.get() ? EntityKind.DataRole : EntityKind.AnyRole) : kind;
                markerForm = form;
            }

            var allParts = (new EnName() { id = name }).Split();
            if (!System.String.IsNullOrWhiteSpace(allParts.term) && !allParts.term.Contains("<") && _useFullUri)
            {
                var tterm = pfx2Ns(allParts.term);
                if (!System.String.IsNullOrWhiteSpace(tterm))
                    allParts.term = "<" + tterm + ">";
                else
                    throw new Exception("No namespace found for prefix " + allParts.term + ". You need to define it before saving into Ontorion.");
            }

            return ENNameingConvention.ToDL(allParts.Combine(), form).id;
        }
        public EntityKind GetMarkerKind() { return markerKind; }
        public endict.WordKind GetMarkerForm() { return markerForm; }

        VisitingParam<bool> isModal = new VisitingParam<bool>(false);
        VisitingParam<bool> isPlural = new VisitingParam<bool>(false);
        VisitingParam<bool> isDataRoleStatement = new VisitingParam<bool>(false);
        VisitingParam<CNL.DL.Node> roleNode = new VisitingParam<CNL.DL.Node>(null);

        // This is used from EN TO DL --> pfx can be a namespace inside <>, the namespace returned should be in <>
        string defPfx2Ns(string pfx)
        {
            if (System.String.IsNullOrWhiteSpace(pfx))
                throw new Exception("No default namespace is defined.");

            if (pfx.StartsWith("<") && pfx.EndsWith(">"))
            {
                var argg = pfx.Substring(1, pfx.Length - 2);
                if (!argg.EndsWith("/") && !argg.EndsWith("#") && !argg.Contains("#"))
                    argg += "#";
                return "<" + argg + ">";
            }
            else
                throw new Exception("No namespace defined for prefix: " + pfx);
        }

        bool _useFullUri = false;
        Func<string, string> _pfx2Ns = null;
        Func<string, string> pfx2Ns
        {
            get
            {
                if (_pfx2Ns == null)
                    return defPfx2Ns;
                else
                    return _pfx2Ns;
            }
        }

        public InvTransform() { }
        public InvTransform(string marker) { this.marker = marker; }

        public CogniPy.CNL.DL.Paragraph Convert(CogniPy.CNL.EN.paragraph p, bool useFullUri = false, Func<string, string> pfx2Ns = null)
        {
            _useFullUri = useFullUri;
            _pfx2Ns = pfx2Ns;
            return p.accept(this) as CogniPy.CNL.DL.Paragraph;
        }
        public CogniPy.CNL.DL.Statement Convert(CogniPy.CNL.EN.sentence s)
        {
            return s.accept(this) as CogniPy.CNL.DL.Statement;
        }

        public object Visit(CogniPy.CNL.EN.paragraph p)
        {
            CogniPy.CNL.DL.Paragraph ret = new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() };
            foreach (var x in p.sentences)
            {
                ret.Statements.Add(x.accept(this) as CNL.DL.Statement);
            }
            return ret;
        }

        public CogniPy.CNL.DL.Statement.Modality Modality(string tok)
        {
            switch (tok)
            {
                case "□": return Statement.Modality.MUST;
                case "◊": return Statement.Modality.SHOULD;
                case "◊◊": return Statement.Modality.CAN;
                case "~◊◊": return Statement.Modality.MUSTNOT;
                case "~◊": return Statement.Modality.SHOULDNOT;
                case "~□": return Statement.Modality.CANNOT;
                default: return Statement.Modality.IS;
            }
        }

        public object Visit(CogniPy.CNL.EN.subsumption p)
        {
            CogniPy.CNL.DL.Subsumption ret = new CNL.DL.Subsumption(null);

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
            ret.C = p.c.accept(this) as CNL.DL.Node;
            using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                ret.D = p.d.accept(this) as CNL.DL.Node;
            ret.modality = modal;
            return ret;
        }

        public object Visit(CogniPy.CNL.EN.nosubsumption p)
        {
            CogniPy.CNL.DL.Subsumption ret = new CNL.DL.Subsumption(null);

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
            ret.C = p.c.accept(this) as CNL.DL.Node;
            using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                ret.D = new CogniPy.CNL.DL.ConceptNot(null) { C = p.d.accept(this) as CNL.DL.Node };
            ret.modality = modal;

            return ret;
        }

        public object Visit(CogniPy.CNL.EN.subsumption_if p)
        {
            CogniPy.CNL.DL.Subsumption ret = new CNL.DL.Subsumption(null);

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
            ret.C = p.c.accept(this) as CNL.DL.Node;
            using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                ret.D = p.d.accept(this) as CNL.DL.Node;
            ret.modality = modal;
            return ret;
        }

        public object Visit(datatypedef p)
        {
            CogniPy.CNL.DL.DataTypeDefinition ret = new DataTypeDefinition(null, new CNL.DL.ID(null) { yytext = ToDL(p.name, EntityKind.DataType, endict.WordKind.NormalForm) }, p.db.accept(this) as AbstractBound);
            return ret;
        }

        public object Visit(CogniPy.CNL.EN.equivalence2 p)
        {
            CogniPy.CNL.DL.Equivalence ret = new CNL.DL.Equivalence(null) { Equivalents = new List<Node>() };

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
            ret.Equivalents.Add(p.c.accept(this) as CNL.DL.Node);
            using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                ret.Equivalents.Add(p.d.accept(this) as CNL.DL.Node);
            ret.modality = modal;
            return ret;
        }
        //public object Visit(Ontorion.CNL.EN.equivalence_def p)
        //{
        //    Ontorion.CNL.DL.Equivalence ret = new CNL.DL.Equivalence(null) { Equivalents = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    ret.Equivalents.Add(p.c.accept(this) as CNL.DL.Node);
        //    ret.Equivalents.Add(p.d.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}
        //public object Visit(Ontorion.CNL.EN.disjoint p)
        //{
        //    Ontorion.CNL.DL.Disjoint ret = new CNL.DL.Disjoint(null) { Disjoints = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    foreach (var e in p.different)
        //        ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}

        public object Visit(CogniPy.CNL.EN.exclusives p)
        {
            CogniPy.CNL.DL.Disjoint ret = new CNL.DL.Disjoint(null) { Disjoints = new List<Node>() };

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
            using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
            {

                foreach (var e in p.objectRoleExprs)
                    ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);
            }

            ret.modality = modal;
            return ret;
        }


        //public object Visit(Ontorion.CNL.EN.disjointunion p)
        //{
        //    Ontorion.CNL.DL.DisjointUnion ret = new CNL.DL.DisjointUnion(null) { Union = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    ret.name = ToDL(p.name, EntityKind.Concept, endict.WordKind.NormalForm);

        //    foreach (var e in p.union)
        //        ret.Union.Add(e.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}

        public object Visit(CogniPy.CNL.EN.exclusiveunion p)
        {
            CogniPy.CNL.DL.DisjointUnion ret = new CNL.DL.DisjointUnion(null) { Union = new List<Node>() };

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);

            ret.name = ToDL(p.name, EntityKind.Concept, endict.WordKind.NormalForm);

            foreach (var e in p.objectRoleExprs)
                ret.Union.Add(e.accept(this) as CNL.DL.Node);

            ret.modality = modal;
            return ret;
        }

        public object Visit(rolesubsumption p)
        {
            if (p.subChain.Count == 1)
            {
                CogniPy.CNL.DL.RoleInclusion ret = new RoleInclusion(null);
                CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
                ret.C = p.subChain[0].accept(this) as CNL.DL.Node;
                using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                {
                    ret.D = p.superRole.accept(this) as CNL.DL.Node;
                }

                ret.modality = modal;
                return ret;
            }
            else if (p.subChain.Count > 1)
            {
                CogniPy.CNL.DL.ComplexRoleInclusion ret = new CNL.DL.ComplexRoleInclusion(null) { RoleChain = new List<Node>() };
                CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);

                foreach (var x in p.subChain)
                {
                    ret.RoleChain.Add(x.accept(this) as CNL.DL.Node);
                }

                using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                {
                    ret.R = p.superRole.accept(this) as CNL.DL.Node;
                }

                ret.modality = modal;
                return ret;
            }
            else
            {
                Assert(false);
                return null;
            }
        }
        //public object Visit(Ontorion.CNL.EN.roleequivalence p)
        //{
        //    Ontorion.CNL.DL.RoleEquivalence ret = new CNL.DL.RoleEquivalence(null) { Equivalents = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    foreach (var e in p.equals)
        //        ret.Equivalents.Add(e.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}

        public object Visit(CogniPy.CNL.EN.roleequivalence2 p)
        {
            CogniPy.CNL.DL.RoleEquivalence ret = new CNL.DL.RoleEquivalence(null) { Equivalents = new List<Node>() };

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);

            ret.Equivalents.Add(p.r.accept(this) as CNL.DL.Node);
            ret.Equivalents.Add(p.s.accept(this) as CNL.DL.Node);

            ret.modality = modal;
            return ret;
        }

        //public object Visit(Ontorion.CNL.EN.roledisjoint p)
        //{
        //    Ontorion.CNL.DL.RoleDisjoint ret = new CNL.DL.RoleDisjoint(null) { Disjoints = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    foreach (var e in p.different)
        //        ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}

        public object Visit(CogniPy.CNL.EN.roledisjoint2 p)
        {
            CogniPy.CNL.DL.RoleDisjoint ret = new CNL.DL.RoleDisjoint(null) { Disjoints = new List<Node>() };

            CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);

            ret.Disjoints.Add(p.r.accept(this) as CNL.DL.Node);
            ret.Disjoints.Add(p.s.accept(this) as CNL.DL.Node);

            ret.modality = modal;
            return ret;
        }

        public object Visit(datarolesubsumption p)
        {
            using (isDataRoleStatement.set(true))
            {
                CogniPy.CNL.DL.DataRoleInclusion ret = new DataRoleInclusion(null);
                CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);
                ret.C = p.subRole.accept(this) as CNL.DL.Node;
                using (isModal.set(modal != CogniPy.CNL.DL.Statement.Modality.IS))
                {
                    ret.D = p.superRole.accept(this) as CNL.DL.Node;
                }

                ret.modality = modal;
                return ret;
            }
        }

        //public object Visit(Ontorion.CNL.EN.dataroleequivalence p)
        //{
        //    Ontorion.CNL.DL.DataRoleEquivalence ret = new CNL.DL.DataRoleEquivalence(null) { Equivalents = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    foreach (var e in p.equals)
        //        ret.Equivalents.Add(e.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}

        public object Visit(CogniPy.CNL.EN.dataroleequivalence2 p)
        {
            using (isDataRoleStatement.set(true))
            {
                CogniPy.CNL.DL.DataRoleEquivalence ret = new CNL.DL.DataRoleEquivalence(null) { Equivalents = new List<Node>() };

                ret.Equivalents.Add(p.r.accept(this) as CNL.DL.Node);
                ret.Equivalents.Add(p.s.accept(this) as CNL.DL.Node);

                return ret;
            }
        }
        //public object Visit(Ontorion.CNL.EN.dataroledisjoint p)
        //{
        //    Ontorion.CNL.DL.DataRoleDisjoint ret = new CNL.DL.DataRoleDisjoint(null) { Disjoints = new List<Node>() };

        //    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

        //    foreach (var e in p.different)
        //        ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);

        //    ret.modality = modal;
        //    return ret;
        //}
        public object Visit(CogniPy.CNL.EN.dataroledisjoint2 p)
        {
            using (isDataRoleStatement.set(true))
            {
                CogniPy.CNL.DL.DataRoleDisjoint ret = new CNL.DL.DataRoleDisjoint(null) { Disjoints = new List<Node>() };

                CogniPy.CNL.DL.Statement.Modality modal = Modality(p.modality);

                ret.Disjoints.Add(p.r.accept(this) as CNL.DL.Node);
                using (isModal.set(true))
                    ret.Disjoints.Add(p.s.accept(this) as CNL.DL.Node);

                ret.modality = modal;
                return ret;
            }
        }

        public object Visit(CogniPy.CNL.EN.haskey p)
        {
            CogniPy.CNL.DL.HasKey ret = new CNL.DL.HasKey(null) { Roles = new List<Node>(), DataRoles = new List<Node>() };

            foreach (var e in p.roles)
                ret.Roles.Add(e.accept(this) as CNL.DL.Node);
            using (isDataRoleStatement.set(true))
            {
                foreach (var e in p.dataroles)
                    ret.DataRoles.Add(e.accept(this) as CNL.DL.Node);
            }
            ret.C = p.s.accept(this) as CNL.DL.Node;

            return ret;
        }

        public object Visit(CogniPy.CNL.EN.subjectEvery p)
        {
            return p.s.accept(this);
        }
        public object Visit(CogniPy.CNL.EN.subjectEverything p)
        {
            return p.t != null ? p.t.accept(this) : new CogniPy.CNL.DL.Top(null);
        }

        //go into nosubsumptions
        public object Visit(CogniPy.CNL.EN.subjectNo p)
        {
            return p.s.accept(this);
        }
        public object Visit(subjectNothing p)
        {
            return new CogniPy.CNL.DL.Top(null);
        }


        CogniPy.CNL.DL.Instance BigName(string name)
        {
            return new CNL.DL.NamedInstance(null) { name = ToDL(name, EntityKind.Instance, endict.WordKind.NormalForm) };
        }

        CogniPy.CNL.DL.Instance Unnamed(bool only, single s)
        {
            return new CNL.DL.UnnamedInstance(null) { Only = only, C = s.accept(this) as CogniPy.CNL.DL.Node };
        }

        public object Visit(subjectBigName p)
        {
            return new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, BigName(p.name)));
        }
        public object Visit(subjectThe p)
        {
            return new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, Unnamed(p.only, p.s)));
        }

        //public object Visit(defObjectRoleExpr1 p)
        //{
        //    using (roleNode.set(null))
        //    {
        //        if (p.Negated)
        //            return new Ontorion.CNL.DL.ConceptNot(null) { C = p.s.accept(this) as Ontorion.CNL.DL.Node };
        //        else
        //            return p.s.accept(this);
        //    }
        //}
        public object Visit(objectRoleExpr1 p)
        {
            using (roleNode.set(null))
            {
                if (p.Negated)
                    return new CogniPy.CNL.DL.ConceptNot(null) { C = p.s.accept(this) as CogniPy.CNL.DL.Node };
                else
                    return p.s.accept(this);
            }
        }

        public object Visit(roleWithXY p)
        {
            if (p.name == ENNameingConvention.TOPROLENAME)
            {
                CNL.DL.Node n = new CNL.DL.Top(null);
                if (p.inverse)
                    n = new CNL.DL.RoleInversion(null) { R = n };
                return n;
            }
            else if (p.name == ENNameingConvention.BOTTOMROLENAME)
            {
                CNL.DL.Node n = new CNL.DL.Bottom(null);
                if (p.inverse)
                    n = new CNL.DL.RoleInversion(null) { R = n };
                return n;
            }
            else
            {
                if (!p.inverse)
                    return new CNL.DL.Atomic(null) { id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple) };
                else
                    return new CNL.DL.RoleInversion(null) { R = new CNL.DL.Atomic(null) { id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple) } };
            }
        }

        public object Visit(notRoleWithXY p)
        {
            if (p.name == ENNameingConvention.TOPROLENAME)
            {
                CNL.DL.Node n = new CNL.DL.Top(null);
                if (p.inverse)
                    n = new CNL.DL.RoleInversion(null) { R = n };
                return n;
            }
            else if (p.name == ENNameingConvention.BOTTOMROLENAME)
            {
                CNL.DL.Node n = new CNL.DL.Bottom(null);
                if (p.inverse)
                    n = new CNL.DL.RoleInversion(null) { R = n };
                return n;
            }
            else
            {
                if (!p.inverse)
                    return new CNL.DL.Atomic(null) { id = ToDL(p.name, EntityKind.AnyRole, endict.WordKind.PastParticiple) };
                else
                    return new CNL.DL.RoleInversion(null) { R = new CNL.DL.Atomic(null) { id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple) } };
            }
        }

        public object Visit(role p)
        {
            if (p.name == ENNameingConvention.TOPROLENAME)
            {
                CNL.DL.Node n = new CNL.DL.Top(null);
                if (p.inverse)
                    n = new CNL.DL.RoleInversion(null) { R = n };
                return n;
            }
            else if (p.name == ENNameingConvention.BOTTOMROLENAME)
            {
                CNL.DL.Node n = new CNL.DL.Bottom(null);
                if (p.inverse)
                    n = new CNL.DL.RoleInversion(null) { R = n };
                return n;
            }
            else
            {
                if (!p.inverse)
                    return new CNL.DL.Atomic(null) { id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : (isPlural.get() ? endict.WordKind.PluralFormVerb : endict.WordKind.PastParticiple)) };
                else
                    return new CNL.DL.RoleInversion(null) { R = new CNL.DL.Atomic(null) { id = ToDL(p.name, EntityKind.AnyRole, endict.WordKind.SimplePast) } };
            }
        }
        public object Visit(objectRoleExpr2 p)
        {
            using (isModal.set(isModal.get() || p.Negated))
            {
                using (roleNode.set(p.r.accept(this) as CNL.DL.Node))
                {
                    if (p.Negated)
                        return new CogniPy.CNL.DL.ConceptNot(null) { C = p.s.accept(this) as CogniPy.CNL.DL.Node };
                    else
                    {
                        if (p.s != null)
                            return p.s.accept(this);
                        else
                            return new CNL.EN.oobjectSomething(null).accept(this);
                    }
                }
            }
        }
        public object Visit(objectRoleExpr3 p)
        {
            using (isModal.set(isModal.get()))
            {
                return new CogniPy.CNL.DL.SomeRestriction(null) { C = p.t.accept(this) as CNL.DL.Node, R = p.r.accept(this) as CNL.DL.Node };
            }
        }

        public object Visit(oobjectA p)
        {
            using (isModal.set(false))
            {
                if (roleNode.get() != null)
                    return new CNL.DL.SomeRestriction(null) { C = p.s.accept(this) as CNL.DL.Node, R = roleNode.get() };
                else
                    return p.s.accept(this);
            }
        }
        public object Visit(oobjectOnly p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                using (isPlural.set(true))
                    return new CNL.DL.OnlyRestriction(null) { C = p.s.accept(this) as CNL.DL.Node, R = roleNode.get() };
            }
        }

        public void Assert(bool b)
        {
            if (!b)
            {
#if DEBUG
                System.Diagnostics.Debugger.Break();
#endif
                throw new Exception("Conversion Assertion Failed.");
            }
        }
        public object Visit(oobjectCmp p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                CNL.DL.Node aC = null;
                using (isPlural.set(long.Parse(p.Cnt) != 1))
                    aC = p.s.accept(this) as CNL.DL.Node;
                return new CNL.DL.NumberRestriction(null) { N = p.Cnt, Kind = p.Cmp, C = aC, R = roleNode.get() };
            }
        }

        public object Visit(oobjectCmpInstance p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                CNL.DL.Node aC = null;
                using (isPlural.set(long.Parse(p.Cnt) != 1))
                    aC = new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, p.i.accept(this) as CNL.DL.Instance));
                return new CNL.DL.NumberRestriction(null) { N = p.Cnt, Kind = p.Cmp, C = aC, R = roleNode.get() };
            }
        }
        public object Visit(oobjectBnd p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.SomeValueRestriction(null) { R = roleNode.get(), B = p.b.accept(this) as CNL.DL.AbstractBound };
            }
        }
        public object Visit(oobjectOnlyBnd p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.OnlyValueRestriction(null) { R = roleNode.get(), B = p.b.accept(this) as CNL.DL.AbstractBound };
            }
        }
        public object Visit(oobjectCmpBnd p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.NumberValueRestriction(null) { N = p.Cnt, Kind = p.Cmp, R = roleNode.get(), B = p.b.accept(this) as CNL.DL.AbstractBound };
            }
        }
        public object Visit(oobjectInstance p)
        {
            using (isModal.set(false))
            {
                if (roleNode.get() != null)
                    return new CNL.DL.SomeRestriction(null) { C = new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, p.i.accept(this) as CNL.DL.Instance)), R = roleNode.get() };
                else
                    return new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, p.i.accept(this) as CNL.DL.Instance));
            }
        }
        public object Visit(instanceBigName p)
        {
            return BigName(p.name);
        }
        public object Visit(instanceThe p)
        {
            return Unnamed(p.only, p.s);
        }
        public object Visit(oobjectOnlyInstance p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.OnlyRestriction(null) { C = new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, p.i.accept(this) as CNL.DL.Instance)), R = roleNode.get() };
            }
        }

        public object Visit(oobjectSelf p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.SelfReference(null) { R = roleNode.get() };
            }
        }
        public object Visit(oobjectSomething p)
        {
            using (isModal.set(false))
            {
                if (roleNode.get() != null)
                    return new CNL.DL.SomeRestriction(null) { C = new CNL.DL.Top(null), R = roleNode.get() };
                else
                    return new CNL.DL.Top(null);
            }
        }
        public object Visit(oobjectNothing p)
        {
            using (isModal.set(false))
            {
                if (roleNode.get() != null)
                    return new CNL.DL.SomeRestriction(null) { C = new CNL.DL.Bottom(null), R = roleNode.get() };
                else
                    return new CNL.DL.Bottom(null);
            }
        }
        public object Visit(oobjectOnlyNothing p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.OnlyRestriction(null) { C = new CNL.DL.Bottom(null), R = roleNode.get() };
            }
        }
        public object Visit(oobjectSomethingThat p)
        {
            using (isModal.set(false))
            {
                if (roleNode.get() != null)
                    return new CNL.DL.SomeRestriction(null) { C = p.t.accept(this) as CNL.DL.Node, R = roleNode.get() };
                else
                    return p.t.accept(this);
            }
        }
        public object Visit(oobjectOnlySomethingThat p)
        {
            using (isModal.set(false))
            {
                Assert(p is oobjectRelated && roleNode.get() != null);
                return new CNL.DL.OnlyRestriction(null) { C = p.t.accept(this) as CNL.DL.Node, R = roleNode.get() };
            }
        }

        string name(string str)
        {
            return ToDL(str, EntityKind.Concept, isPlural.get() ? endict.WordKind.PluralFormNoun : endict.WordKind.NormalForm);
        }

        public object Visit(singleName p)
        {
            return new CNL.DL.Atomic(null) { id = name(p.name) };
        }
        public object Visit(singleThing p)
        {
            return new CNL.DL.Top(null);
        }
        public object Visit(singleNameThat p)
        {
            return new CNL.DL.ConceptAnd(null, new CNL.DL.Atomic(null) { id = name(p.name) }, p.t.accept(this) as CNL.DL.Node);
        }
        public object Visit(singleThingThat p)
        {
            return p.t.accept(this);
        }
        public object Visit(thatOrLoop p)
        {
            return p.o.accept(this);
        }
        public object Visit(singleOneOf p)
        {
            using (isModal.set(false))
            {
                CNL.DL.InstanceSet iset = null;
                using (isPlural.set(false))
                {
                    iset = new CNL.DL.InstanceSet(null) { Instances = new List<CNL.DL.Instance>() };
                    foreach (var i in p.insts)
                        iset.Instances.Add(i.accept(this) as CNL.DL.Instance);
                }
                return iset;
            }
        }
        public object Visit(andloop p)
        {
            CNL.DL.Node first = null;
            foreach (var e in p.exprs)
            {
                if (first == null)
                    first = e.accept(this) as CNL.DL.Node;
                else
                    first = new CNL.DL.ConceptAnd(null, first, e.accept(this) as CNL.DL.Node);
            }
            return first;
        }
        public object Visit(orloop p)
        {
            CNL.DL.Node first = null;
            foreach (var e in p.exprs)
            {
                if (first == null)
                    first = e.accept(this) as CNL.DL.Node;
                else
                    first = new CNL.DL.ConceptOr(null, first, e.accept(this) as CNL.DL.Node);
            }
            return first;
        }
        public object Visit(instanceList p)
        {
            var list = new List<CNL.DL.Instance>();
            foreach (var e in p.insts)
            {
                list.Add(e.accept(this) as CNL.DL.Instance);
            }
            return new CogniPy.CNL.DL.InstanceSet(null) { Instances = list };
        }

        public object Visit(facet p)
        {
            return new CNL.DL.Facet(null, p.Cmp, p.V.accept(this) as CNL.DL.Value);
        }

        public object Visit(facetList p)
        {
            var fl = new CNL.DL.FacetList(null) { List = new List<Facet>() };
            foreach (var f in p.Facets)
                fl.List.Add(f.accept(this) as CNL.DL.Facet);
            return fl;
        }

        public object Visit(boundFacets p)
        {
            return new CNL.DL.BoundFacets(null, p.l.accept(this) as CNL.DL.FacetList);
        }

        public object Visit(boundNot p)
        {
            return new CNL.DL.BoundNot(null, p.bnd.accept(this) as AbstractBound);
        }

        public object Visit(boundAnd p)
        {
            if (p.List.Count == 1)
                return p.List[0].accept(this);
            else
            {
                var ret = new CNL.DL.BoundAnd(null) { List = new List<AbstractBound>() };
                foreach (var l in p.List)
                    ret.List.Add(l.accept(this) as AbstractBound);
                return ret;
            }
        }

        public object Visit(boundOr p)
        {
            if (p.List.Count == 1)
                return p.List[0].accept(this);
            else
            {
                var ret = new CNL.DL.BoundOr(null) { List = new List<AbstractBound>() };
                foreach (var l in p.List)
                    ret.List.Add(l.accept(this) as AbstractBound);
                return ret;
            }
        }

        public object Visit(boundVal p)
        {
            return new CNL.DL.BoundVal(null, p.Cmp, p.V.accept(this) as CNL.DL.Value);
        }

        public object Visit(boundTotal p)
        {
            Value v = null;
            switch (p.Kind)
            {
                case "DEC":
                    v = new CNL.DL.DecimalNumber(null, "$3.14");
                    break;
                case "NUM":
                    v = new CNL.DL.Number(null, "1");
                    break;
                case "STR":
                    v = new CNL.DL.String(null, "\'...\'");
                    break;
                case "DBL":
                    v = new CNL.DL.Float(null, "3.14");
                    break;
                case "BOL":
                    v = new CNL.DL.Bool(null, "[1]");
                    break;
                case "DTM":
                    v = new CNL.DL.DateTimeVal(null, "2012-02-16");
                    break;
                case "DUR":
                    v = new CNL.DL.Duration(null, "P1DT12H35M30.234S");
                    break;
                default:
                    Assert(false);
                    break;
            }
            return new CNL.DL.TotalBound(null, v);
        }
        public object Visit(boundDataType p)
        {
            return new CNL.DL.DTBound(null, new CNL.DL.ID(null) { yytext = ToDL(p.name, EntityKind.DataType, endict.WordKind.NormalForm) });
        }
        public object Visit(boundTop p)
        {
            return new CNL.DL.TopBound(null);
        }
        public object Visit(boundOneOf p)
        {
            var ret = new CNL.DL.ValueSet(null) { Values = new List<Value>() };
            foreach (var val in p.vals)
            {
                ret.Values.Add(val.accept(this) as Value);
            }
            return ret;
        }
        public object Visit(Number p)
        {
            return new CNL.DL.Number(null, p.val);
        }
        public object Visit(DecimalNumber p)
        {
            return new CNL.DL.DecimalNumber(null, p.val);
        }
        public object Visit(Bool p)
        {
            return new CNL.DL.Bool(null, p.val == "true" ? "[1]" : "[0]");
        }
        public object Visit(StrData p)
        {
            return new CNL.DL.String(null, p.val);
        }
        public object Visit(DateTimeData p)
        {
            return new CNL.DL.DateTimeVal(null, p.val);
        }
        public object Visit(Duration p)
        {
            return new CNL.DL.Duration(null, p.val);
        }

        public object Visit(Float p)
        {
            return new CNL.DL.Float(null, p.val);
        }

        ////////// SWRL /////////////////////////////////////////////////////////////////////

        public object Visit(swrlrule p)
        {
            listVars.Clear();
            newDataValVar = 1;
            newInstanceValVar.Clear();

            CogniPy.CNL.DL.SwrlStatement swrl_statement = new CNL.DL.SwrlStatement(null);
            swrl_statement.slp = p.Predicate.accept(this) as CNL.DL.SwrlItemList;
            swrl_statement.slc = p.Result.accept(this) as CNL.DL.SwrlItemList;
            swrl_statement.modality = Modality(p.modality);
            return swrl_statement;
        }

        public object Visit(clause p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            foreach (var e in p.Conditions)
                listT.AddRange(e.accept(this) as List<CNL.DL.SwrlItem>);
            return new CogniPy.CNL.DL.SwrlItemList(null) { list = listT };
        }

        public object Visit(clause_result p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            foreach (var e in p.Conditions)
                listT.AddRange(e.accept(this) as List<CNL.DL.SwrlItem>);
            return new CogniPy.CNL.DL.SwrlItemList(null) { list = listT };
        }

        public object Visit(condition_is p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as SwrlIObject;
            CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
            if (inst != null) listT.Add(inst);

            var id_o2 = p.objectB.accept(this) as SwrlIObject;
            CNL.DL.SwrlInstance inst2 = createSwrlInstanceFromObjectR(p.objectB, id_o2);
            if (inst2 != null)
            {
                id_o2 = inst2.I;
                listT.Add(inst2);
            }

            if (p.condition_kind == condition_kind.None)
                listT.Add(new CNL.DL.SwrlSameAs(null) { I = id_o, J = id_o2 });
            else
                listT.Add(new CNL.DL.SwrlDifferentFrom(null) { I = id_o, J = id_o2 });

            return listT;
        }

        public object Visit(condition_exists p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as SwrlIObject;
            var itm = createSwrlInstanceFromObjectR(p.objectA, id_o);
            if (itm == null)
                itm = createNewSwrlInstance(null, id_o);
            listT.Add(itm);
            return listT;
        }

        public object Visit(condition_definition p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as CNL.DL.SwrlIObject;
            string clsName = null;
            if (p.objectA is objectr_nio)
            {
                clsName = (p.objectA as objectr_nio).notidentobject.name;
            }
            else if ((p.objectA is objectr_io) && (p.objectA as objectr_io).identobject is identobject_name)
            {
                clsName = ((p.objectA as objectr_io).identobject as identobject_name).name;
            }
            if (clsName != null)
            {
                var myAtom = new CNL.DL.Atomic(null) { id = ToDL(clsName, EntityKind.Concept, endict.WordKind.NormalForm) };
                listT.Add(new CNL.DL.SwrlInstance(null, myAtom, id_o));
            }
            var id_o2 = p.objectClass.accept(this) as CNL.DL.Node;
            listT.Add(new CNL.DL.SwrlInstance(null, id_o2, id_o));

            return listT;
        }

        public object Visit(condition_role p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as CNL.DL.SwrlIObject;
            CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
            if (inst != null) listT.Add(inst);

            var id_o2 = p.objectB.accept(this) as CNL.DL.SwrlIObject;
            CNL.DL.SwrlInstance inst2 = createSwrlInstanceFromObjectR(p.objectB, id_o2);
            if (inst2 != null)
            {
                id_o2 = inst2.I;
                listT.Add(inst2);
            }
            if (p.condition_kind == condition_kind.None)
                listT.Add(new CNL.DL.SwrlRole(null)
                {
                    I = id_o,
                    J = id_o2,
                    R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.PastParticiple)
                });
            else if (p.condition_kind == condition_kind.Inv)
                listT.Add(new CNL.DL.SwrlRole(null)
                {
                    I = id_o2,
                    J = id_o,
                    R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.SimplePast)
                });

            return listT;
        }

        public object Visit(condition_data_property p)
        {
            var listT = new List<CNL.DL.SwrlItem>();

            var id_o = p.objectA.accept(this) as SwrlIObject;
            CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
            if (inst != null) listT.Add(inst);

            var db2 = p.d_object.accept(this) as SwrlDObject;

            listT.Add(new CNL.DL.SwrlDataProperty(null)
            {
                IO = id_o,
                DO = db2,
                R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple)
            });
            return listT;
        }

        public object Visit(condition_result_is p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as CNL.DL.SwrlIObject;
            var id_o2 = p.objectB.accept(this) as CNL.DL.SwrlIObject;

            if (p.condition_kind == condition_kind.None)
                listT.Add(new CNL.DL.SwrlSameAs(null) { I = id_o, J = id_o2 });
            else
                listT.Add(new CNL.DL.SwrlDifferentFrom(null) { I = id_o, J = id_o2 });

            return listT;
        }

        public object Visit(condition_result_definition p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as CNL.DL.SwrlIObject;
            var id_o2 = p.objectClass.accept(this) as CNL.DL.Node;
            listT.Add(new CNL.DL.SwrlInstance(null, id_o2, id_o));
            return listT;
        }

        public object Visit(condition_result_role p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            bool id_ex = true;
            if (p.objectA is identobject_name)
                id_ex = isVarAlreadyIntroduced((identobject_name)p.objectA);

            var id_o = p.objectA.accept(this) as CNL.DL.SwrlIObject;
            if (!id_ex)
            {
                var inst = createSwrlInstanceFromObject_name((identobject_name)p.objectA, id_o);
                if (inst != null) listT.Add(inst);
            }

            bool id_ex2 = true;
            if (p.objectB is identobject_name)
                id_ex2 = isVarAlreadyIntroduced((identobject_name)p.objectB);

            var id_o2 = p.objectB.accept(this) as CNL.DL.SwrlIObject;
            if (!id_ex2)
            {
                var inst = createSwrlInstanceFromObject_name((identobject_name)p.objectB, id_o2);
                if (inst != null) listT.Add(inst);
            }

            if (p.condition_kind == condition_kind.None)
                listT.Add(new CNL.DL.SwrlRole(null)
                {
                    I = id_o,
                    J = id_o2,
                    R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.PastParticiple)
                });
            else if (p.condition_kind == condition_kind.Inv)
                listT.Add(new CNL.DL.SwrlRole(null)
                {
                    I = id_o2,
                    J = id_o,
                    R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.SimplePast)
                });
            return listT;
        }

        public object Visit(condition_result_data_property p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as SwrlIObject;

            var db2 = p.d_object.accept(this) as SwrlDObject;

            listT.Add(new CNL.DL.SwrlDataProperty(null)
            {
                IO = id_o,
                DO = db2,
                R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple)
            });

            return listT;
        }

        int newDataValVar = 1;
        public object Visit(condition_data_property_bound p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.objectA.accept(this) as SwrlIObject;
            CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
            if (inst != null) listT.Add(inst);

            if (p.bnd.isStrict())
            {
                var d_val = new datavalval(null, p.bnd.getStrictVal());
                var db2 = d_val.accept(this) as SwrlDObject;

                listT.Add(new CNL.DL.SwrlDataProperty(null)
                {
                    IO = id_o,
                    DO = db2,
                    R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple)
                });
            }
            else
            {
                var d_val = new datavalvar(null, "tmp-" + (newDataValVar++).ToString());
                var db2 = d_val.accept(this) as SwrlDObject;

                listT.Add(new CNL.DL.SwrlDataProperty(null)
                {
                    IO = id_o,
                    DO = db2,
                    R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple)
                });

                var b = p.bnd.accept(this) as AbstractBound;
                listT.Add(new CNL.DL.SwrlDataRange(null)
                {
                    DO = db2,
                    B = b
                });
            }
            return listT;
        }

        public object Visit(condition_data_bound p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var b = p.bound.accept(this) as AbstractBound;
            var db2 = p.d_object.accept(this) as SwrlDObject;
            listT.Add(new CNL.DL.SwrlDataRange(null)
            {
                DO = db2,
                B = b
            });
            return listT;
        }

        public object Visit(objectr_nio p)
        {
            return p.notidentobject.accept(this);
        }

        public object Visit(objectr_io p)
        {
            return p.identobject.accept(this);
        }

        public object Visit(notidentobject p)
        {
            int idx = p.num == null ? -2 : int.Parse(p.num);
            return getNewVarIdentifier(p.name ?? "thing", idx);
        }

        public object Visit(identobject_name p)
        {
            int idx = p.num == null ? -1 : int.Parse(p.num);
            return getNewVarIdentifier(p.name ?? "thing", idx);
        }

        public object Visit(identobject_inst p)
        {
            return p.i.accept(this);
        }

        public object Visit(instancer p)
        {
            return new CNL.DL.SwrlIVal(null) { I = ToDL(p.name, EntityKind.Instance, endict.WordKind.NormalForm) };
        }

        public object Visit(datavalvar p)
        {
            return new CNL.DL.SwrlDVar(null) { VAR = "value-" + p.num };
        }

        public object Visit(datavalval p)
        {
            var val = p.dv.accept(this) as CNL.DL.Value;
            return new CNL.DL.SwrlDVal(null, val);
        }

        public object Visit(condition_builtin p)
        {
            var bii = p.bi.accept(this);
            if (bii is CNL.DL.SwrlBuiltIn)
                return new List<CNL.DL.SwrlItem>() { bii as CNL.DL.SwrlItem };
            else
                return bii;
        }

        public object Visit(condition_result_builtin p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var bi = p.bi.accept(this) as CNL.DL.SwrlBuiltIn;
            listT.Add(bi);
            return listT;
        }

        static string BuiltinTpyToCmp(string tpy)
        {
            string cmp = "";
            switch (tpy)
            {
                case "<=": cmp = "≤"; break;
                case ">=": cmp = "≥"; break;
                case "<>": cmp = "≠"; break;
                default: cmp = tpy; break;
            }

            return cmp;
        }

        public object Visit(builtin_cmp p)
        {
            return new CNL.DL.SwrlBuiltIn(null, BuiltinTpyToCmp(p.cmp),
                new List<ISwrlObject>(){
                    p.b.accept(this) as SwrlDObject,
                    p.a.accept(this) as SwrlDObject});
        }

        public object Visit(builtin_list p)
        {
            var lst = (from x in p.vals select (x.accept(this) as ISwrlObject)).ToList();
            lst.Add(p.result.accept(this) as SwrlDObject);
            return new CNL.DL.SwrlBuiltIn(null, p.tpy, lst);
        }

        public object Visit(builtin_bin p)
        {
            return new CNL.DL.SwrlBuiltIn(null, p.tpy,
                new List<ISwrlObject>(){
                    p.b.accept(this) as SwrlDObject,
                    p.d.accept(this) as SwrlDObject,
                    p.result.accept(this) as SwrlDObject});
        }

        public object Visit(builtin_alpha p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.a.accept(this) as CNL.DL.SwrlIObject;
            CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.a, id_o);
            if (inst != null) listT.Add(inst);

            listT.Add(new CNL.DL.SwrlBuiltIn(null, "alpha-representation-of",
                new List<ISwrlObject>(){
                    id_o,
                    p.b.accept(this) as SwrlDObject}));

            return listT;
        }

        public object Visit(builtin_annot p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var id_o = p.a.accept(this) as CNL.DL.SwrlIObject;
            var prop_o = p.prop.accept(this) as CNL.DL.SwrlDObject;
            CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.a, id_o);
            if (inst != null) listT.Add(inst);

            listT.Add(new CNL.DL.SwrlBuiltIn(null, "annotation",
                new List<ISwrlObject>(){
                    id_o,
                    prop_o,
                    p.lang.accept(this) as SwrlDObject,
                    p.b.accept(this) as SwrlDObject}));

            return listT;
        }

        public object Visit(builtin_exe p)
        {
            var listT = new List<CNL.DL.SwrlItem>();
            var el = p.ea.accept(this) as CNL.DL.SwrlVarList;

            var l = new List<ISwrlObject>(){
                    new SwrlDVal(null){ Val = new CNL.DL.String(null){ val = p.name}}};

            foreach (var e in el.list)
                l.Add(e as ISwrlObject);

            l.Add(p.a.accept(this) as ISwrlObject);

            listT.Add(new CNL.DL.SwrlBuiltIn(null, "execute", l));

            return listT;
        }

        public object Visit(builtin_unary_cmp p)
        {
            return new CNL.DL.SwrlBuiltIn(null, p.tpy,
                new List<ISwrlObject>(){
                    p.b.accept(this) as SwrlDObject,
                    p.result.accept(this) as SwrlDObject});
        }

        public object Visit(builtin_unary_free p)
        {
            return new CNL.DL.SwrlBuiltIn(null, p.tpy,
                new List<ISwrlObject>(){
                    p.b.accept(this) as SwrlDObject,
                    p.a.accept(this) as SwrlDObject});
        }

        public object Visit(builtin_substr p)
        {
            var l = new List<ISwrlObject>(){
                    p.b.accept(this) as SwrlDObject,
                    p.c.accept(this) as SwrlDObject};

            if (p.d != null)
                l.Add(p.d.accept(this) as SwrlDObject);

            l.Add(p.result.accept(this) as SwrlDObject);

            return new CNL.DL.SwrlBuiltIn(null, p.tpy, l);
        }

        public object Visit(builtin_trans p)
        {
            return new CNL.DL.SwrlBuiltIn(null, p.tpy,
                new List<ISwrlObject>(){
                    p.b.accept(this) as SwrlDObject,
                    p.c.accept(this) as SwrlDObject,
                    p.d.accept(this) as SwrlDObject,
                    p.result.accept(this) as SwrlDObject});
        }

        public object Visit(builtin_duration p)
        {
            var DT = p.d.accept(this) as List<ISwrlObject>;
            DT.Add(p.a.accept(this) as SwrlDObject);
            return new CNL.DL.SwrlBuiltIn(null, "duration", DT);
        }

        public object Visit(builtin_datetime p)
        {
            var DT = p.d.accept(this) as List<ISwrlObject>;
            DT.Add(p.a.accept(this) as SwrlDObject);
            return new CNL.DL.SwrlBuiltIn(null, "datetime", DT);
        }

        public object Visit(duration_w p)
        {
            return new List<ISwrlObject>(){
                new SwrlDVal(null, new CogniPy.CNL.DL.String(null, "'W'")),
                p.y.accept(this) as SwrlDObject,
                p.W.accept(this) as SwrlDObject,
                p.d.accept(this) as SwrlDObject,
                p.h.accept(this) as SwrlDObject,
                p.m.accept(this) as SwrlDObject,
                p.s.accept(this) as SwrlDObject};
        }

        public object Visit(duration_m p)
        {
            return new List<ISwrlObject>(){
                new SwrlDVal(null, new CogniPy.CNL.DL.String(null, "'M'")),
                p.y.accept(this) as SwrlDObject,
                p.M.accept(this) as SwrlDObject,
                p.d.accept(this) as SwrlDObject,
                p.h.accept(this) as SwrlDObject,
                p.m.accept(this) as SwrlDObject,
                p.s.accept(this) as SwrlDObject};

        }

        public object Visit(datetime p)
        {
            return new List<ISwrlObject>(){
                new SwrlDVal(null, new CogniPy.CNL.DL.String(null, "'M'")),
                p.y.accept(this) as SwrlDObject,
                p.M.accept(this) as SwrlDObject,
                p.d.accept(this) as SwrlDObject,
                p.h.accept(this) as SwrlDObject,
                p.m.accept(this) as SwrlDObject,
                p.s.accept(this) as SwrlDObject};
        }

        private List<string> listVars = new List<string>();//list of chosen (or given) identifiers of instances used in swrl rule

        private string getExistingIdentifier(string name, int idx)  // bierze istniejący identyfikator, w pp zwraca pusty string
        {
            var parst = new DlName() { id = ToDL(name, EntityKind.Concept, endict.WordKind.NormalForm) }.Split();

            string nn;

            Assert(idx != -2);
            if (!newInstanceValVar.ContainsKey(name))
                newInstanceValVar.Add(name, 1);

            if (idx == -1) nn = parst.name + (newInstanceValVar[name] != 1 ? "-0" + newInstanceValVar[name].ToString() : "-x");
            else nn = parst.name + "-" + idx;

            var id = new DlName.Parts() { name = nn }.Combine().id;

            if (!listVars.Contains(id))
            {
                listVars.Add(id);
                return id;
            }
            else return "";
        }

        Dictionary<string, int> newInstanceValVar = new Dictionary<string, int>();

        private bool isVarAlreadyIntroduced(identobject_name io)
        {
            var namet = io.name ?? "thing";
            var parst = new DlName() { id = ToDL(namet, EntityKind.Concept, endict.WordKind.NormalForm) }.Split();

            int idx = io.num == null
              ? -1
              : int.Parse(io.num);

            if (!newInstanceValVar.ContainsKey(namet))
                return false;

            string nn = "";
            if (idx < 0) nn = parst.name + (newInstanceValVar[namet] != 1 ? "-0" + newInstanceValVar[namet].ToString() : "-x");
            else nn = parst.name + "-" + idx;

            var id = new DlName.Parts() { name = nn }.Combine().id;

            if (!listVars.Contains(id))
                return false;

            return true;

        }


        private CNL.DL.SwrlIVar getNewVarIdentifier(string namet, int idx)
        //tworzy nowy identyfikator instancji danej klasy wg konwencji "nazwa-klasy"_numer 
        //jesli już istnieje, to zwraca ten istniejący
        {
            var parst = new DlName() { id = ToDL(namet, EntityKind.Concept, endict.WordKind.NormalForm) }.Split();

            string nn;

            if (idx == -2 || !newInstanceValVar.ContainsKey(namet))
            {
                if (!newInstanceValVar.ContainsKey(namet))
                    newInstanceValVar.Add(namet, 0);
                newInstanceValVar[namet] = newInstanceValVar[namet] + 1;
            }

            if (idx < 0) nn = parst.name + (newInstanceValVar[namet] != 1 ? "-0" + newInstanceValVar[namet].ToString() : "-x");
            else nn = parst.name + "-" + idx;

            var id = new DlName.Parts() { name = nn }.Combine().id;

            if (!listVars.Contains(id))
                listVars.Add(id);

            return new SwrlIVar(null) { VAR = id };
        }

        private CNL.DL.SwrlInstance createNewSwrlInstance(string class_name, SwrlIObject id)
        {
            return new CNL.DL.SwrlInstance(null)
            {
                C = (class_name == null) ? new CogniPy.CNL.DL.Top(null) as CNL.DL.Node : new CogniPy.CNL.DL.Atomic(null) { id = class_name },
                I = id
            };
        }

        private CNL.DL.SwrlInstance createSwrlInstanceFromObject_name(CNL.EN.identobject_name o, SwrlIObject id)
        {
            string class_name = o.name;
            int idx = o.num == null
                          ? -1
                          : int.Parse(o.num);

            if (class_name == null)
                return null;
            else
            {

                string vname = getExistingIdentifier(class_name ?? "thing", idx);
                if (!listVars.Contains(vname))
                {
                    var vid = getNewVarIdentifier(class_name ?? "thing", idx);
                    return createNewSwrlInstance(class_name, vid);
                }
                else
                    return null;
            }
        }

        private CNL.DL.SwrlInstance createSwrlInstanceFromObjectR(CNL.EN.objectr o)
        {
            if (o is CNL.EN.objectr_nio)
            {
                string class_name = (o as CNL.EN.objectr_nio).notidentobject.name;

                int idx = (o as CNL.EN.objectr_nio).notidentobject.num == null ? -2 : int.Parse((o as CNL.EN.objectr_nio).notidentobject.num);
                return createNewSwrlInstance(name(class_name), getNewVarIdentifier(class_name ?? "thing", idx));
            }
            else if (o is CNL.EN.objectr_io)
            {
                Debugger.Break();
                throw new NotImplementedException();
            }
            return null;
        }

        private CNL.DL.SwrlInstance createSwrlInstance(CNL.EN.identobject io, SwrlIObject id)
        {
            if (io is identobject_name)
            {
                string class_name = (io as identobject_name).name;
                if (class_name == null)
                    return null;
                else
                    return createNewSwrlInstance(name(class_name), id);
            }
            else return null;
        }

        private CNL.DL.SwrlInstance createSwrlInstance(CNL.EN.notidentobject nio, SwrlIObject id)
        {
            if (nio is notidentobject)
            {
                string class_name = nio.name;
                if (class_name == null)
                    return null;
                else
                    return createNewSwrlInstance(name(class_name), id);
            }
            else return null;
        }

        private CNL.DL.SwrlInstance createSwrlInstanceFromObjectR(CNL.EN.objectr o, SwrlIObject id)
        {
            if (o is CNL.EN.objectr_nio)
            {
                string class_name = (o as CNL.EN.objectr_nio).notidentobject.name;
                if (class_name == null)
                    return null;
                else
                    return createNewSwrlInstance(name(class_name), id);
            }
            else if (o is CNL.EN.objectr_io)
            {
                if ((o as CNL.EN.objectr_io).identobject is CNL.EN.identobject_name)
                {
                    string class_name = ((o as CNL.EN.objectr_io).identobject as CNL.EN.identobject_name).name;
                    int idx = ((o as CNL.EN.objectr_io).identobject as CNL.EN.identobject_name).num == null
                                  ? -1
                                  : int.Parse(((o as CNL.EN.objectr_io).identobject as CNL.EN.identobject_name).num);
                    if (class_name == null)
                        return null;
                    else
                    {
                        string vname = getExistingIdentifier(class_name ?? "thing", idx);
                        if (!listVars.Contains(vname))
                        {
                            var vid = getNewVarIdentifier(class_name ?? "thing", idx);
                            return createNewSwrlInstance(name(class_name), vid);
                        }
                        else
                            return createNewSwrlInstance(name(class_name), id);
                    }
                }
                else
                    return null;
            }
            else
                throw new InvalidOperationException("Unknown Swrl Term");
        }

        public object Visit(swrlrulefor p)
        {
            listVars.Clear();
            newDataValVar = 1;
            newInstanceValVar.Clear();

            CogniPy.CNL.DL.SwrlIterate swrl_statement = new CNL.DL.SwrlIterate(null);
            swrl_statement.slp = p.Predicate.accept(this) as CNL.DL.SwrlItemList;
            swrl_statement.slc = p.Result.accept(this) as CNL.DL.SwrlItemList;
            swrl_statement.vars = p.Collection.accept(this) as CNL.DL.SwrlVarList;

            return swrl_statement;
        }


        //////////// SWRL //////////////////////////////////////////////////////////////

        //////////// EXE //////////////////////////////////////////////////////////////

        public object Visit(exerule p)
        {
            listVars.Clear();
            newDataValVar = 1;
            newInstanceValVar.Clear();
            var exe_statement = new CNL.DL.ExeStatement(null);
            exe_statement.slp = p.slp.accept(this) as CNL.DL.SwrlItemList;
            exe_statement.args = p.args.accept(this) as CNL.DL.SwrlVarList;
            exe_statement.exe = p.exe;
            return exe_statement;
        }

        public object Visit(exeargs p)
        {
            var idlist = new CNL.DL.SwrlVarList(null) { list = new List<IExeVar>() };
            foreach (var el in p.exevars)
            {
                idlist.list.Add(el.accept(this) as IExeVar);
            }
            return idlist;
        }

        public object Visit(code p)
        {
            var code_statement = new CodeStatement(null);
            code_statement.exe = p.exe;
            return code_statement;
        }

        //////////// EXE //////////////////////////////////////////////////////////////

        public object Visit(annotation p)
        {
            return new CNL.DL.Annotation(null, "%" + p.txt);
        }

        public object Visit(dlannotationassertion p)
        {
            CogniPy.ARS.EntityKind result = CogniPy.CNL.AnnotationManager.ParseSubjectKind(p.subjKind);
#if !SILVERLIGHT
            return new CNL.DL.DLAnnotationAxiom(null) { subject = p.subject, subjKind = p.subjKind, annotName = p.annotName, value = System.Net.WebUtility.HtmlEncode(p.value), language = p.language };
#else
            return new CNL.DL.DLAnnotationAxiom(null) { subject = p.subject, subjKind = p.subjKind, annotName = p.annotName, value = p.value, language = p.language };
#endif
        }

    }
}
