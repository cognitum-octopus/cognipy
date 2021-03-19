using CogniPy.CNL.DL;
using CogniPy.CNL.EN;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using org.semanticweb.owlapi.vocab;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.Text;
using System.Text.RegularExpressions;

namespace CogniPy.ARS
{
    public class IncorectOWLStatementException : Exception
    {

    }
    public class NoInversionsForDataRolesException : IncorectOWLStatementException
    {
        public string RoleName;
        public NoInversionsForDataRolesException(string role)
        {
            RoleName = role;
        }

        public override string Message
        {
            get
            {
                return "Inversion on attributes are prohibited.\r\nYou tryied to do it with '" + RoleName + "' attribute.";
            }
        }
    }

    public class NoTautology : IncorectOWLStatementException
    {
        public NoTautology()
        {
        }
        public override string Message
        {
            get
            {
                return "Entity cannot be defined by itself.";
            }
        }
    }


    public class DifferenceToItsef : IncorectOWLStatementException
    {
        public override string Message
        {
            get
            {
                return "Entity cannot be different from itself.";
            }
        }
    }

    public class Transform : CogniPy.CNL.DL.IVisitor
    {
        DLToOWLNameConv owlNC = new DLToOWLNameConv();

        public Dictionary<string, string> InvUriMappings { get { return owlNC.InvUriMappings; } set { owlNC.InvUriMappings = value; } }
        bool forReasoning = false;

        public void setOWLDataFactory(bool forReasoning, string defaultNS, OWLDataFactory factory, PrefixOWLOntologyFormat namespaceManager, CogniPy.CNL.EN.endict lex)
        {
            this.owlNC.setOWLFormat(defaultNS, namespaceManager, lex);
            this.factory = factory;
            this.forReasoning = forReasoning;
        }

        public static java.util.Set GetJavaAxiomSet(IEnumerable<AxiomOrComment> axioms)
        {
            var ret = new java.util.LinkedHashSet();
            foreach (var axiom in axioms)
                if (axiom.axiom != null)
                    ret.add(axiom.axiom);
            return ret;
        }

        public static java.util.Set GetJavaAxiomSet(IEnumerable<OWLAxiom> axioms)
        {
            var ret = new java.util.LinkedHashSet();
            foreach (var axiom in axioms)
                ret.add(axiom);
            return ret;
        }

        private OWLDataFactory factory;
        private OWLReasoner resolvingReasoner = null;



        private List<OWLAxiom> additionalAxioms = null;
        private List<OWLAxiom> additionalHotfixDeclarations = null;

        public void setReasoner(OWLReasoner reasoner)
        {
            this.resolvingReasoner = reasoner;
        }


        public class AxiomOrComment
        {
            public OWLAxiom axiom;
            public string comment;
        }

        public class Axioms
        {
            public List<AxiomOrComment> axioms = new List<AxiomOrComment>();
            public HashSet<OWLAxiom> additions = new HashSet<OWLAxiom>();
            public HashSet<OWLAxiom> hotfixes = new HashSet<OWLAxiom>();
        }

        private bool hasAnnotationsForStatement = false;
        private Dictionary<string, List<DLAnnotationAxiom>> annotationsBySubject = null;

        public Axioms Convert(CogniPy.CNL.DL.Paragraph p, CogniPy.CNL.DL.Paragraph paraFromAnnotStatements = null)
        {
            if (paraFromAnnotStatements != null && paraFromAnnotStatements.Statements.Count > 0)
            {
                annotationsBySubject = new Dictionary<string, List<DLAnnotationAxiom>>();
                int nannot = 0;
                foreach (var stmtAnn in paraFromAnnotStatements.Statements)
                {
                    if (stmtAnn is DLAnnotationAxiom)
                    {
                        nannot++;
                        var dlannotAx = stmtAnn as DLAnnotationAxiom;
                        if (!annotationsBySubject.ContainsKey(dlannotAx.subject))
                            annotationsBySubject.Add(dlannotAx.subject, new List<DLAnnotationAxiom>());
                        annotationsBySubject[dlannotAx.subject].Add(dlannotAx);
                    }
                }
                if (nannot > 0)
                    hasAnnotationsForStatement = true;
            }

            additionalAxioms = new List<OWLAxiom>();
            additionalHotfixDeclarations = new List<OWLAxiom>();
            var axioms = p.accept(this) as List<AxiomOrComment>;

            return new Axioms()
            {
                axioms = axioms,
                additions = new HashSet<OWLAxiom>(additionalAxioms),
                hotfixes = new HashSet<OWLAxiom>(additionalHotfixDeclarations)
            };
        }

        public KeyValuePair<OWLClassExpression, HashSet<OWLAxiom>> Convert(CogniPy.CNL.DL.Node e)
        {
            additionalAxioms = new List<OWLAxiom>();
            additionalHotfixDeclarations = new List<OWLAxiom>();
            var cls = e.accept(this) as OWLClassExpression;
            return new KeyValuePair<OWLClassExpression, HashSet<OWLAxiom>>(cls, new HashSet<OWLAxiom>(additionalAxioms));
        }

        public OWLNamedIndividual GetNamedIndividual(string I)
        {
            return factory.getOWLNamedIndividual(owlNC.getIRIFromId(I, EntityKind.Instance));
        }

        public IRI getIRIFromDL(string I, EntityKind kind)
        {
            return owlNC.getIRIFromId(I, kind);
        }

        public OWLObjectPropertyExpression GetObjectProperty(CogniPy.CNL.DL.Node r)
        {
            using (context.set(VisitingContext.ObjectRole))
                return r.accept(this) as OWLObjectPropertyExpression;
        }
        public OWLDataProperty GetDataProperty(CogniPy.CNL.DL.Node r)
        {
            using (context.set(VisitingContext.DataRole))
                return r.accept(this) as OWLDataProperty;
        }
        public object Visit(CogniPy.CNL.DL.Paragraph e)
        {
            List<AxiomOrComment> axioms = new List<AxiomOrComment>();
            foreach (var stmt in e.Statements)
            {
                //                if (!isDisabledStatement(stmt))
                var aoc = new AxiomOrComment();
                var n = stmt.accept(this);
                if (n == null)
                    continue;
                if (n is OWLAxiom)
                    aoc.axiom = n as OWLAxiom;
                else
                    aoc.comment = n.ToString();
                axioms.Add(aoc);
            }
            return axioms;
        }

        private java.util.Set getOWLAnnotationForStatement(CogniPy.CNL.DL.Statement e)
        {
            java.util.Set annotationsForStatement = new java.util.LinkedHashSet();
            if (hasAnnotationsForStatement)
            {
                var ser = new CogniPy.CNL.DL.Serializer(false);
                var stmtSer = ser.Serialize(new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() { e } }).Replace("\r\n", "");
                if (annotationsBySubject.ContainsKey(stmtSer))
                {
                    foreach (var ann in annotationsBySubject[stmtSer])
                        annotationsForStatement.add(ann.accept(this) as OWLAnnotation);

                }
            }
            return annotationsForStatement;
        }

        //        Dictionary<string, List<Ontorion.CNL.DL.Statement>> namedStatements4Instances = new Dictionary<string, List<Ontorion.CNL.DL.Statement>>();
        Dictionary<CogniPy.CNL.DL.UnnamedInstance, List<CogniPy.CNL.DL.Statement>> unnamedStatements4Instances = new Dictionary<CogniPy.CNL.DL.UnnamedInstance, List<CogniPy.CNL.DL.Statement>>();
        //        Ontorion.CNL.DL.Statement currentStatement = null;
        void setCurrentStatement(CogniPy.CNL.DL.Statement stmt)
        {
            //            if (InconsistencyDebugMode) return;

            //            currentStatement = stmt;
        }

        //public List<Ontorion.CNL.DL.Instance> GetInstances()
        //{
        //    List<Ontorion.CNL.DL.Instance> ret = new List<Ontorion.CNL.DL.Instance>();
        //    foreach (string ni in namedStatements4Instances.Keys)
        //        ret.Add(new Ontorion.CNL.DL.NamedInstance(null) { name = ni });
        //    foreach(var k in unnamedStatements4Instances.Keys)
        //        ret.Add(k);
        //    return ret;
        //}


        //        public bool InconsistencyDebugMode = false;


        //public List<Ontorion.CNL.DL.Statement> GetDisabledStatements()
        //{
        //    List<Ontorion.CNL.DL.Statement> ret = new List<Ontorion.CNL.DL.Statement>();
        //    foreach (var stmt in disabledStatements)
        //    {
        //        if (stmt.Value)
        //            ret.Add(stmt.Key);
        //    }
        //    return ret;
        //}

        //Dictionary<Ontorion.CNL.DL.Statement, bool> disabledStatements = new Dictionary<Ontorion.CNL.DL.Statement, bool>();
        //public java.util.Set DisableEnableInstance(Ontorion.CNL.DL.Instance inst, bool val)
        //{
        //    if (!InconsistencyDebugMode) return null;

        //    java.util.Set ret = new java.util.HashSet();

        //    if (inst is Ontorion.CNL.DL.NamedInstance)
        //    {
        //        if (namedStatements4Instances.ContainsKey((inst as Ontorion.CNL.DL.NamedInstance).name))
        //        {
        //            foreach (var stmt in namedStatements4Instances[(inst as Ontorion.CNL.DL.NamedInstance).name])
        //            {
        //                disabledStatements[stmt] = val;
        //                ret.addAll(Convert(stmt));
        //            }
        //        }
        //    }
        //    else
        //    {
        //        if (unnamedStatements4Instances.ContainsKey(inst as Ontorion.CNL.DL.UnnamedInstance))
        //        {
        //            foreach (var stmt in unnamedStatements4Instances[inst as Ontorion.CNL.DL.UnnamedInstance])
        //            {
        //                disabledStatements[stmt] = val;
        //                ret.addAll(Convert(stmt));
        //            }
        //        }
        //    }

        //    return ret;
        //}

        //public void DisableAllInstances()
        //{
        //    var insts = GetInstances();
        //    foreach (var inst in insts)
        //    {
        //        if (inst is Ontorion.CNL.DL.NamedInstance)
        //        {
        //            if (namedStatements4Instances.ContainsKey((inst as Ontorion.CNL.DL.NamedInstance).name))
        //            {
        //                foreach (var stmt in namedStatements4Instances[(inst as Ontorion.CNL.DL.NamedInstance).name])
        //                {
        //                    disabledStatements[stmt] = true;
        //                }
        //            }
        //        }
        //        else
        //        {
        //            if (unnamedStatements4Instances.ContainsKey(inst as Ontorion.CNL.DL.UnnamedInstance))
        //            {
        //                foreach (var stmt in unnamedStatements4Instances[inst as Ontorion.CNL.DL.UnnamedInstance])
        //                {
        //                    disabledStatements[stmt] = true;
        //                }
        //            }
        //        }
        //    }
        //}
        //public java.util.Set DisableEnableStatement(Ontorion.CNL.DL.Statement stmt, bool val)
        //{
        //    if (!InconsistencyDebugMode) return null;
        //    disabledStatements[stmt] = val;
        //    return Convert(stmt);
        //}

        //bool isDisabledStatement(Ontorion.CNL.DL.Statement stmt)
        //{
        //    if (!InconsistencyDebugMode) return false;

        //    if (disabledStatements.ContainsKey(stmt))
        //    {
        //        return disabledStatements[stmt];
        //    }
        //    else
        //        return false;
        //}

        //        void setNamedStatement4Instance(Ontorion.CNL.DL.NamedInstance inst)
        //        {
        ////            if (!InconsistencyDebugMode)
        ////            {
        //                if (!namedStatements4Instances.ContainsKey(inst.name))
        //                    namedStatements4Instances[inst.name] = new List<Ontorion.CNL.DL.Statement>();
        //                namedStatements4Instances[inst.name].Add(currentStatement);
        ////            }
        //        }
        //        void setUnnamedStatement4Instance(Ontorion.CNL.DL.UnnamedInstance inst)
        //        {
        ////           if (!InconsistencyDebugMode)
        ////            {
        //                if (!unnamedStatements4Instances.ContainsKey(inst))
        //                    unnamedStatements4Instances[inst] = new List<Ontorion.CNL.DL.Statement>();
        //                unnamedStatements4Instances[inst].Add(currentStatement);
        ////            }
        //        }

        private CogniPy.CNL.DL.NamedInstance getSingleNamgedInstance(CogniPy.CNL.DL.Node C)
        {
            if (C is CogniPy.CNL.DL.InstanceSet)
            {
                if ((C as CogniPy.CNL.DL.InstanceSet).Instances.Count == 1)
                {
                    if ((C as CogniPy.CNL.DL.InstanceSet).Instances[0] is CogniPy.CNL.DL.NamedInstance)
                    {
                        return (C as CogniPy.CNL.DL.InstanceSet).Instances[0] as CogniPy.CNL.DL.NamedInstance;
                    }
                }
            }
            return null;
        }

        private CogniPy.CNL.DL.Value getSingleEqualValue(CogniPy.CNL.DL.AbstractBound C)
        {
            if (C is CogniPy.CNL.DL.ValueSet)
            {
                if ((C as CogniPy.CNL.DL.ValueSet).Values.Count == 1)
                    return (C as CogniPy.CNL.DL.ValueSet).Values[0] as CogniPy.CNL.DL.Value;
            }
            else if (C is CogniPy.CNL.DL.BoundVal)
            {
                if ((C as CogniPy.CNL.DL.BoundVal).Kind == "=")
                {
                    return (C as CogniPy.CNL.DL.BoundVal).V;
                }
            }
            else if (C is CogniPy.CNL.DL.BoundFacets)
            {
                if ((C as CogniPy.CNL.DL.BoundFacets).FL.List.Count == 1 && (C as CogniPy.CNL.DL.BoundFacets).FL.List[0].Kind == "=")
                    return (C as CogniPy.CNL.DL.BoundFacets).FL.List[0].V;
            }
            return null;
        }

        public object Visit(CNL.DL.Annotation a)
        {
            return a.txt;
        }

        public object Visit(CNL.DL.DLAnnotationAxiom a)
        {
            var annotProp = factory.getOWLAnnotationProperty(owlNC.getIRIFromId(a.annotName, CogniPy.ARS.EntityKind.Role));
            OWLLiteral annotLit;
            if (!System.String.IsNullOrEmpty(a.language))
                annotLit = factory.getOWLLiteral(a.value, a.language);
            else
                annotLit = factory.getOWLLiteral(a.value);

            var annotEl = factory.getOWLAnnotation(annotProp, annotLit, getOWLAnnotationForStatement(a));

            CogniPy.ARS.EntityKind result = CogniPy.CNL.AnnotationManager.ParseSubjectKind(a.subjKind);

            if (result != EntityKind.Statement)
            {
                var owlAnnotSubj = owlNC.getIRIFromId(a.subject, result) as OWLAnnotationSubject;
                return factory.getOWLAnnotationAssertionAxiom(owlAnnotSubj, annotEl);
            }
            else
            {
                return annotEl;
            }
        }

        public object Visit(CogniPy.CNL.DL.Subsumption e)
        {
            using (context.set(VisitingContext.Concept))
            {
                setCurrentStatement(e);
                var iC = getSingleNamgedInstance(e.C);
                var iD = getSingleNamgedInstance(e.D);
                if (iC != null)
                {
                    if (iD != null)
                    {
                        var indivs = new java.util.HashSet();
                        indivs.add(iC.accept(this) as OWLNamedIndividual);
                        indivs.add(iD.accept(this) as OWLNamedIndividual);
                        if (indivs.size() < 2)
                            throw new NoTautology();
                        else
                        {
                            return factory.getOWLSameIndividualAxiom(indivs, getOWLAnnotationForStatement(e));
                        }
                    }
                    else
                    {
                        if (e.D is SomeRestriction)
                        {
                            var iS = getSingleNamgedInstance((e.D as SomeRestriction).C);
                            if (iS != null)
                            {
                                OWLObjectPropertyExpression r;
                                using (context.set(VisitingContext.ObjectRole))
                                    r = (e.D as SomeRestriction).R.accept(this) as OWLObjectPropertyExpression;
                                return factory.getOWLObjectPropertyAssertionAxiom(r, iC.accept(this) as OWLNamedIndividual, iS.accept(this) as OWLNamedIndividual, getOWLAnnotationForStatement(e));
                            }
                        }
                        else if (e.D is SomeValueRestriction)
                        {
                            var iV = getSingleEqualValue((e.D as SomeValueRestriction).B);
                            if (iV != null)
                            {
                                OWLDataPropertyExpression r;
                                using (context.set(VisitingContext.DataRole))
                                    r = (e.D as SomeValueRestriction).R.accept(this) as OWLDataPropertyExpression;
                                return factory.getOWLDataPropertyAssertionAxiom(r, iC.accept(this) as OWLNamedIndividual, iV.accept(this) as OWLLiteral, getOWLAnnotationForStatement(e));
                            }
                        }
                        return factory.getOWLClassAssertionAxiom(e.D.accept(this) as OWLClassExpression, iC.accept(this) as OWLNamedIndividual, getOWLAnnotationForStatement(e));
                    }
                }
                if (e.C is CNL.DL.Top)
                {
                    if (e.D is CNL.DL.NumberRestriction) // object functional 
                    {
                        if (
                            ((e.D as CNL.DL.NumberRestriction).Kind == "≤" && int.Parse((e.D as CNL.DL.NumberRestriction).N) == 1)
                            || ((e.D as CNL.DL.NumberRestriction).Kind == "<" && int.Parse((e.D as CNL.DL.NumberRestriction).N) == 2)
                        )
                        {
                            if ((e.D as CNL.DL.NumberRestriction).C is CNL.DL.Top)
                            {
                                if ((e.D as CNL.DL.NumberRestriction).R is Atomic)
                                {
                                    OWLObjectPropertyExpression r;
                                    using (context.set(VisitingContext.ObjectRole))
                                        r = (e.D as CNL.DL.NumberRestriction).R.accept(this) as OWLObjectPropertyExpression;
                                    return factory.getOWLFunctionalObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
                                }
                                else if ((e.D as CNL.DL.NumberRestriction).R is RoleInversion)
                                {
                                    if (((e.D as CNL.DL.NumberRestriction).R as RoleInversion).R is Atomic)
                                    {
                                        OWLObjectPropertyExpression r;
                                        using (context.set(VisitingContext.ObjectRole))
                                            r = ((e.D as CNL.DL.NumberRestriction).R as RoleInversion).R.accept(this) as OWLObjectPropertyExpression;
                                        return factory.getOWLInverseFunctionalObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
                                    }
                                }
                            }
                        }
                    }
                    else if (e.D is CNL.DL.NumberValueRestriction) //functional data
                    {
                        if (
                            ((e.D as CNL.DL.NumberValueRestriction).Kind == "≤" && int.Parse((e.D as CNL.DL.NumberValueRestriction).N) == 1)
                            || ((e.D as CNL.DL.NumberValueRestriction).Kind == "<" && int.Parse((e.D as CNL.DL.NumberValueRestriction).N) == 2)
                            )
                            if ((e.D as CNL.DL.NumberValueRestriction).B is CNL.DL.TopBound)
                            {
                                OWLDataPropertyExpression r;
                                using (context.set(VisitingContext.DataRole))
                                    r = (e.D as CNL.DL.NumberValueRestriction).R.accept(this) as OWLDataPropertyExpression;
                                return factory.getOWLFunctionalDataPropertyAxiom(r, getOWLAnnotationForStatement(e));
                            }
                    }
                    else if (e.D is CNL.DL.OnlyRestriction) // object range
                    {
                        if ((e.D as CNL.DL.OnlyRestriction).R is Atomic)
                        {
                            OWLObjectPropertyExpression r;
                            using (context.set(VisitingContext.ObjectRole))
                                r = (e.D as CNL.DL.OnlyRestriction).R.accept(this) as OWLObjectPropertyExpression;

                            return factory.getOWLObjectPropertyRangeAxiom(
                                r,
                                (e.D as CNL.DL.OnlyRestriction).C.accept(this) as OWLClassExpression, getOWLAnnotationForStatement(e));
                        }
                    }
                    else if (e.D is CNL.DL.OnlyValueRestriction) // data range
                    {

                        if ((e.D as CNL.DL.OnlyValueRestriction).R is Atomic)
                        {
                            OWLDataPropertyExpression r;
                            using (context.set(VisitingContext.DataRole))
                                r = (e.D as CNL.DL.OnlyValueRestriction).R.accept(this) as OWLDataPropertyExpression;

                            return factory.getOWLDataPropertyRangeAxiom(
                                r,
                                (e.D as CNL.DL.OnlyValueRestriction).B.accept(this) as OWLDataRange, getOWLAnnotationForStatement(e));
                        }
                    }
                    else if (e.D is CNL.DL.SelfReference) // reflexive
                    {
                        OWLObjectPropertyExpression r;
                        using (context.set(VisitingContext.ObjectRole))
                            r = (e.D as CNL.DL.SelfReference).R.accept(this) as OWLObjectPropertyExpression;

                        return factory.getOWLReflexiveObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
                    }
                }
                else if (e.C is SomeRestriction)
                {
                    if ((e.C as SomeRestriction).C is CNL.DL.Top) // object domain
                    {
                        if ((e.C as CNL.DL.SomeRestriction).R is Atomic)
                        {
                            OWLObjectPropertyExpression r;
                            using (context.set(VisitingContext.ObjectRole))
                                r = (e.C as CNL.DL.SomeRestriction).R.accept(this) as OWLObjectPropertyExpression;

                            return factory.getOWLObjectPropertyDomainAxiom(
                                r,
                                e.D.accept(this) as OWLClassExpression, getOWLAnnotationForStatement(e));
                        }
                    }
                }
                else if (e.C is SomeValueRestriction)
                {
                    if ((e.C as SomeValueRestriction).B is CNL.DL.TopBound) // data domain
                    {
                        if ((e.C as CNL.DL.SomeValueRestriction).R is Atomic)
                        {
                            OWLDataPropertyExpression r;
                            using (context.set(VisitingContext.DataRole))
                                r = (e.C as CNL.DL.SomeValueRestriction).R.accept(this) as OWLDataPropertyExpression;

                            return factory.getOWLDataPropertyDomainAxiom(
                                r,
                                e.D.accept(this) as OWLClassExpression, getOWLAnnotationForStatement(e));
                        }
                    }
                }

                if (e.D is Bottom)
                {
                    if (e.C is CNL.DL.SelfReference) // irreflexive
                    {
                        OWLObjectPropertyExpression r;
                        using (context.set(VisitingContext.ObjectRole))
                            r = (e.C as CNL.DL.SelfReference).R.accept(this) as OWLObjectPropertyExpression;

                        return factory.getOWLIrreflexiveObjectPropertyAxiom(r, getOWLAnnotationForStatement(e));
                    }
                }
                return factory.getOWLSubClassOfAxiom(e.C.accept(this) as OWLClassExpression, e.D.accept(this) as OWLClassExpression, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.Equivalence e)
        {
            using (context.set(VisitingContext.Concept))
            {
                setCurrentStatement(e);
                bool allIndivs = true;
                foreach (var x in e.Equivalents)
                {
                    var ic = getSingleNamgedInstance(x);
                    if (ic == null)
                    {
                        allIndivs = false;
                        break;
                    }
                }
                java.util.Set s = new java.util.HashSet();
                if (allIndivs)
                {
                    foreach (var x in e.Equivalents)
                    {
                        var ic = getSingleNamgedInstance(x);
                        s.add(ic.accept(this));
                    }
                    if (s.size() < 2)
                        throw new NoTautology();
                    else
                        return factory.getOWLSameIndividualAxiom(s, getOWLAnnotationForStatement(e));
                }
                else
                {
                    foreach (var x in e.Equivalents)
                        s.add(x.accept(this));
                    if (s.size() < 2)
                        throw new NoTautology();
                    else
                        return factory.getOWLEquivalentClassesAxiom(s, getOWLAnnotationForStatement(e));
                }
            }
        }

        public object Visit(CogniPy.CNL.DL.Disjoint e)
        {
            using (context.set(VisitingContext.Concept))
            {
                setCurrentStatement(e);
                java.util.Set s = new java.util.HashSet();
                foreach (var x in e.Disjoints)
                {
                    s.add(x.accept(this));
                }
                if (s.size() < 2)
                    throw new DifferenceToItsef();
                else
                    return factory.getOWLDisjointClassesAxiom(s, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.DisjointUnion e)
        {
            using (context.set(VisitingContext.Concept))
            {
                setCurrentStatement(e);
                java.util.Set s = new java.util.HashSet();
                foreach (var x in e.Union)
                {
                    s.add(x.accept(this));
                }
                if (s.size() < 2)
                    throw new DifferenceToItsef();
                else
                    return factory.getOWLDisjointUnionAxiom(factory.getOWLClass(owlNC.getIRIFromId(e.name, EntityKind.Concept)), s, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(DataTypeDefinition e)
        {
            using (context.set(VisitingContext.Concept))
            {
                setCurrentStatement(e);
                return factory.getOWLDatatypeDefinitionAxiom(factory.getOWLDatatype(owlNC.getIRIFromId(e.name, EntityKind.Concept)), e.B.accept(this) as OWLDataRange, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(DTBound e)
        {
            return factory.getOWLDatatype(owlNC.getIRIFromId(e.name, EntityKind.Concept));
        }

        public object Visit(CogniPy.CNL.DL.RoleInclusion e)
        {
            using (context.set(VisitingContext.ObjectRole))
            {
                setCurrentStatement(e);
                if ((e.C is RoleInversion) && (e.C as RoleInversion).R is Atomic && e.D is Atomic)
                {
                    if (((e.C as RoleInversion).R as Atomic).id == (e.D as Atomic).id)
                    { //symmetric
                        return factory.getOWLSymmetricObjectPropertyAxiom(e.D.accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
                    }
                    else
                    { // role inversion
                        return factory.getOWLInverseObjectPropertiesAxiom((e.C as RoleInversion).R.accept(this) as OWLObjectPropertyExpression, e.D.accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
                    }
                }
                return factory.getOWLSubObjectPropertyOfAxiom(e.C.accept(this) as OWLObjectPropertyExpression, e.D.accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.RoleEquivalence e)
        {
            using (context.set(VisitingContext.ObjectRole))
            {
                setCurrentStatement(e);

                java.util.Set s = new java.util.HashSet();
                foreach (var x in e.Equivalents)
                {
                    s.add(x.accept(this));
                }
                //if (s.size() < 2)
                //    throw new NoTautology();
                //else
                return factory.getOWLEquivalentObjectPropertiesAxiom(s, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.RoleDisjoint e)
        {
            using (context.set(VisitingContext.ObjectRole))
            {
                setCurrentStatement(e);

                if (e.Disjoints.Count == 2)
                {
                    if ((e.Disjoints[0] is RoleInversion) && (e.Disjoints[0] as RoleInversion).R is Atomic && e.Disjoints[1] is Atomic)
                    {
                        if (((e.Disjoints[0] as RoleInversion).R as Atomic).id == (e.Disjoints[1] as Atomic).id)
                        { //asymmetric
                            return factory.getOWLAsymmetricObjectPropertyAxiom(e.Disjoints[1].accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
                        }
                    }
                    else if ((e.Disjoints[1] is RoleInversion) && (e.Disjoints[1] as RoleInversion).R is Atomic && e.Disjoints[0] is Atomic)
                    {
                        if (((e.Disjoints[1] as RoleInversion).R as Atomic).id == (e.Disjoints[0] as Atomic).id)
                        { //asymmetric
                            return factory.getOWLAsymmetricObjectPropertyAxiom(e.Disjoints[0].accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
                        }
                    }
                }

                java.util.Set s = new java.util.HashSet();
                foreach (var x in e.Disjoints)
                {
                    s.add(x.accept(this));
                }
                if (s.size() < 2)
                    throw new DifferenceToItsef();
                else
                    return factory.getOWLDisjointObjectPropertiesAxiom(s, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.ComplexRoleInclusion e)
        {
            using (context.set(VisitingContext.ObjectRole))
            {
                setCurrentStatement(e);

                if (e.R is Atomic && e.RoleChain.Count == 2 && e.RoleChain[0] is Atomic && e.RoleChain[1] is Atomic)
                {
                    if ((e.R as Atomic).id == (e.RoleChain[0] as Atomic).id && (e.R as Atomic).id == (e.RoleChain[1] as Atomic).id)
                    {
                        //transitive 
                        return factory.getOWLTransitiveObjectPropertyAxiom(e.R.accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
                    }
                }

                java.util.List chain = new java.util.ArrayList();
                foreach (var r in e.RoleChain)
                    chain.add(r.accept(this) as OWLObjectPropertyExpression);

                return factory.getOWLSubPropertyChainOfAxiom(chain, e.R.accept(this) as OWLObjectPropertyExpression, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.DataRoleInclusion e)
        {
            using (context.set(VisitingContext.DataRole))
            {
                setCurrentStatement(e);
                return factory.getOWLSubDataPropertyOfAxiom(e.C.accept(this) as OWLDataPropertyExpression, e.D.accept(this) as OWLDataPropertyExpression, getOWLAnnotationForStatement(e));
            }
        }

        public object Visit(CogniPy.CNL.DL.DataRoleEquivalence e)
        {
            using (context.set(VisitingContext.DataRole))
            {
                setCurrentStatement(e);
                java.util.Set s = new java.util.HashSet();
                foreach (var x in e.Equivalents)
                {
                    s.add(x.accept(this));
                }
                if (s.size() < 2)
                    throw new NoTautology();
                else
                {
                    return factory.getOWLEquivalentDataPropertiesAxiom(s, getOWLAnnotationForStatement(e));
                }
            }
        }

        public object Visit(CogniPy.CNL.DL.DataRoleDisjoint e)
        {
            using (context.set(VisitingContext.DataRole))
            {
                setCurrentStatement(e);
                java.util.Set s = new java.util.HashSet();
                foreach (var x in e.Disjoints)
                {
                    s.add(x.accept(this));
                }
                if (s.size() < 2)
                    throw new DifferenceToItsef();
                else
                {
                    return factory.getOWLDisjointDataPropertiesAxiom(s, getOWLAnnotationForStatement(e));
                }
            }
        }

        public object Visit(CogniPy.CNL.DL.InstanceOf e)
        {
            setCurrentStatement(e);
            return factory.getOWLClassAssertionAxiom(e.C.accept(this) as OWLClassExpression, e.I.accept(this) as OWLIndividual, getOWLAnnotationForStatement(e));
        }

        public object Visit(CogniPy.CNL.DL.RelatedInstances e)
        {
            setCurrentStatement(e);
            OWLObjectPropertyExpression prop;
            using (context.set(VisitingContext.ObjectRole))
                prop = e.R.accept(this) as OWLObjectPropertyExpression;
            return factory.getOWLObjectPropertyAssertionAxiom(prop, e.I.accept(this) as OWLIndividual, e.J.accept(this) as OWLIndividual, getOWLAnnotationForStatement(e));
        }

        public object Visit(CogniPy.CNL.DL.InstanceValue e)
        {
            setCurrentStatement(e);
            OWLDataPropertyExpression prop;
            using (context.set(VisitingContext.DataRole))
                prop = e.R.accept(this) as OWLDataPropertyExpression;
            return factory.getOWLDataPropertyAssertionAxiom(prop, e.I.accept(this) as OWLIndividual,
                e.V.accept(this) as OWLLiteral, getOWLAnnotationForStatement(e));
        }

        public object Visit(CogniPy.CNL.DL.SameInstances e)
        {
            setCurrentStatement(e);
            var indivs = new java.util.HashSet();
            foreach (var x in e.Instances)
            {
                Assert(x is CNL.DL.NamedInstance);
                indivs.add(factory.getOWLNamedIndividual(owlNC.getIRIFromId((x as CNL.DL.NamedInstance).name, EntityKind.Instance)));
            }
            if (indivs.size() < 2)
                throw new NoTautology();
            else
                return factory.getOWLSameIndividualAxiom(indivs, getOWLAnnotationForStatement(e));
        }

        public object Visit(CogniPy.CNL.DL.DifferentInstances e)
        {
            setCurrentStatement(e);
            var indivs = new java.util.HashSet();
            foreach (var x in e.Instances)
            {
                Assert(x is CNL.DL.NamedInstance);
                indivs.add(factory.getOWLNamedIndividual(owlNC.getIRIFromId((x as CNL.DL.NamedInstance).name, EntityKind.Instance)));
            }
            if (indivs.size() < 2)
                throw new DifferenceToItsef();
            else
                return factory.getOWLDifferentIndividualsAxiom(indivs, getOWLAnnotationForStatement(e));
        }

        public object Visit(CogniPy.CNL.DL.HasKey e)
        {
            setCurrentStatement(e);
            java.util.Set roleSet = new java.util.HashSet();
            using (context.set(VisitingContext.ObjectRole))
            {
                foreach (var x in e.Roles)
                {
                    roleSet.add(x.accept(this));
                }
            }
            using (context.set(VisitingContext.DataRole))
            {
                foreach (var x in e.DataRoles)
                {
                    roleSet.add(x.accept(this));
                }
            }
            return factory.getOWLHasKeyAxiom(e.C.accept(this) as OWLClassExpression, roleSet, getOWLAnnotationForStatement(e));
        }

        public object Visit(CogniPy.CNL.DL.NamedInstance e)
        {
            //            setNamedStatement4Instance(e);
            return factory.getOWLNamedIndividual(owlNC.getIRIFromId(e.name, EntityKind.Instance));
        }


        public object Visit(CogniPy.CNL.DL.UnnamedInstance e)
        {
            //            setUnnamedStatement4Instance(e);
            OWLIndividual ni;
            if (forReasoning)
            {
                CogniPy.CNL.DL.Serializer dlserializer = new CogniPy.CNL.DL.Serializer(false);
                string name = "\"" + dlserializer.Serialize(e.C).Replace("\"", "\"\"") + "." + Guid.NewGuid().ToString("N") + "_uUu_" + "\"";
                ni = factory.getOWLNamedIndividual(owlNC.getIRIFromId(name, EntityKind.Instance));
            }
            else
                ni = factory.getOWLAnonymousIndividual();

            if (e.Only)
            {
                var c = e.C.accept(this) as OWLClassExpression;
                if (!c.isTopEntity())
                {
                    java.util.Set inds = new java.util.HashSet();
                    inds.add(ni);
                    additionalAxioms.Add(factory.getOWLEquivalentClassesAxiom(c, factory.getOWLObjectOneOf(inds)));
                }
            }
            else
            {
                var c = e.C.accept(this) as OWLClassExpression;
                if (!c.isTopEntity())
                    additionalAxioms.Add(factory.getOWLClassAssertionAxiom(c, ni));
            }
            return ni;
        }

        public object Visit(CogniPy.CNL.DL.Number e)
        {
            //return factory.getOWLLiteral(int.Parse(e.val));
            return factory.getOWLLiteral(e.ToInt());
        }
        public object Visit(CogniPy.CNL.DL.DecimalNumber e)
        {
            //return factory.getOWLLiteral(int.Parse(e.val));
            return getLiteralVal(e);
        }
        public object Visit(CogniPy.CNL.DL.Bool e)
        {
            //object tt = factory.getOWLLiteral(bool.Parse(e.ToBool().ToString()));
            //return factory.getOWLLiteral(bool.Parse(e.val));
            return factory.getOWLLiteral(e.ToBool());
        }


        OWLLiteral getLiteralVal(Value v)
        {
            if (v is CNL.DL.Bool)
                return factory.getOWLLiteral(v.ToBool());
            else if (v is CNL.DL.String)
                return factory.getOWLLiteral(v.ToString(), OWL2Datatype.XSD_STRING);
            else if (v is CNL.DL.Float)
                return factory.getOWLLiteral(v.ToString(), OWL2Datatype.XSD_DOUBLE);
            else if (v is CNL.DL.DecimalNumber)
                return factory.getOWLLiteral(v.ToString().Substring(1), OWL2Datatype.XSD_DECIMAL);
            else if (v is CNL.DL.Number)
                return factory.getOWLLiteral(v.ToInt());
            else if (v is CNL.DL.DateTimeVal)
                return factory.getOWLLiteral(completeDTMVal(v.ToStringExact()), OWL2Datatype.XSD_DATE_TIME);
            else if (v is CNL.DL.Duration)
                return factory.getOWLLiteral(v.ToStringExact() + "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration", factory.getRDFPlainLiteral());
            else
                return factory.getOWLLiteral(v.ToString()); //TODO xsd:date i inne typy
        }

        static Regex DtmRg = new Regex(@"(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?)(Z|((\+|\-)[0-2][0-9]:[0-5][0-9]))?)?", RegexOptions.Compiled);
        static string completeDTMVal(string val)
        {
            var m = DtmRg.Match(val);
            var dta = m.Groups["date"].Value;
            var tm = m.Groups["time"].Value;
            StringBuilder sb = new StringBuilder();
            sb.Append(dta);
            if (string.IsNullOrEmpty(tm))
                sb.Append("T00:00:00");
            else
                sb.Append(tm);
            if (tm.Length == "T00:00".Length)
                sb.Append(":00");
            return sb.ToString();
        }


        public object Visit(CogniPy.CNL.DL.DateTimeVal e)
        {
            return getLiteralVal(e);
        }

        public object Visit(CogniPy.CNL.DL.Duration e)
        {
            return getLiteralVal(e);
        }

        public object Visit(CogniPy.CNL.DL.String e)
        {
            return getLiteralVal(e);
        }
        public object Visit(CogniPy.CNL.DL.Float e)
        {
            return getLiteralVal(e);
        }

        static CultureInfo en_cult = new CultureInfo("en-US");
        OWLDatatype forcedDatatype = null;
        public object Visit(Facet e)
        {
            var val = getLiteralVal(e.V);
            if (e.Kind == "≤")
                return factory.getOWLFacetRestriction(OWLFacet.MAX_INCLUSIVE, val);
            else if (e.Kind == "<")
                return factory.getOWLFacetRestriction(OWLFacet.MAX_EXCLUSIVE, val);
            else if (e.Kind == "≥")
                return factory.getOWLFacetRestriction(OWLFacet.MIN_INCLUSIVE, val);
            else if (e.Kind == ">")
                return factory.getOWLFacetRestriction(OWLFacet.MIN_EXCLUSIVE, val);
            else if (e.Kind == "#")
            {
                forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
                return factory.getOWLFacetRestriction(OWLFacet.PATTERN, val);
            }
            else if (e.Kind == "<->")
            {
                forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
                return factory.getOWLFacetRestriction(OWLFacet.LENGTH, val);
            }
            else if (e.Kind == "<-> ≥")
            {
                forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
                return factory.getOWLFacetRestriction(OWLFacet.MIN_LENGTH, val);
            }
            else if (e.Kind == "<-> ≤")
            {
                forcedDatatype = factory.getOWLDatatype(XSDVocabulary.STRING.getIRI());
                return factory.getOWLFacetRestriction(OWLFacet.MAX_LENGTH, val);
            }

            Assert(false);
            return null;
        }

        public object Visit(FacetList e)
        {
            java.util.HashSet ret = new java.util.HashSet();
            foreach (var f in e.List)
                ret.add(f.accept(this));
            return ret;
        }

        public object Visit(BoundFacets e)
        {
            var dtp = getLiteralVal(e.FL.List[0].V).getDatatype();
            forcedDatatype = null;
            var set = e.FL.accept(this) as java.util.Set;

            return factory.getOWLDatatypeRestriction(forcedDatatype == null ? dtp : forcedDatatype, set);
        }

        public object Visit(BoundOr e)
        {
            java.util.Set s = new java.util.HashSet();
            foreach (var i in e.List)
                s.add(i.accept(this));
            return factory.getOWLDataUnionOf(s);
        }

        public object Visit(BoundAnd e)
        {
            java.util.Set s = new java.util.HashSet();
            foreach (var i in e.List)
                s.add(i.accept(this));
            return factory.getOWLDataIntersectionOf(s);
        }

        public object Visit(BoundNot e)
        {
            return factory.getOWLDataComplementOf(e.B.accept(this) as OWLDataRange);
        }

        public object Visit(BoundVal e)
        {
            var val = getLiteralVal(e.V);
            if (e.Kind == "=")
                return factory.getOWLDataOneOf(val);
            else // if (e.Kind == "≠")
                return factory.getOWLDataComplementOf(factory.getOWLDataOneOf(val));
        }

        public object Visit(CogniPy.CNL.DL.TotalBound e)
        {
            var dt = getLiteralVal(e.V).getDatatype();
            if (forReasoning && (dt.ToString() == "http://www.w3.org/2001/XMLSchema#double"))
                return factory.getOWLDataUnionOf(factory.getOWLDatatypeMinInclusiveRestriction(0.0), factory.getOWLDatatypeMaxInclusiveRestriction(0.0));
            else
                return dt;
        }

        public object Visit(CogniPy.CNL.DL.TopBound e)
        {
            return factory.getTopDatatype();
        }

        public object Visit(CogniPy.CNL.DL.ValueSet e)
        {
            java.util.Set vals = new java.util.HashSet();
            foreach (var val in e.Values)
                vals.add(val.accept(this));
            return factory.getOWLDataOneOf(vals);
        }

        enum VisitingContext { Concept, ObjectRole, DataRole };
        CogniPy.CNL.DL.VisitingParam<VisitingContext> context = new CogniPy.CNL.DL.VisitingParam<VisitingContext>(VisitingContext.Concept);

        public object Visit(CogniPy.CNL.DL.Atomic e)
        {
            switch (context.get())
            {
                case VisitingContext.Concept:
                    {
                        var cls = factory.getOWLClass(owlNC.getIRIFromId(e.id, EntityKind.Concept));
                        if (resolvingReasoner != null)
                        {
                            var set = resolvingReasoner.getInstances(cls, false).getFlattened();
                            var oneOf = factory.getOWLObjectOneOf(set);
                            var inters = factory.getOWLObjectIntersectionOf(cls, oneOf);
                            return inters;
                        }
                        else
                            return cls;
                    }
                case VisitingContext.ObjectRole:
                    return factory.getOWLObjectProperty(owlNC.getIRIFromId(e.id, EntityKind.Role));
                case VisitingContext.DataRole:
                    return factory.getOWLDataProperty(owlNC.getIRIFromId(e.id, EntityKind.Role));
            }
            Assert(false);
            return null;
        }
        public object Visit(CogniPy.CNL.DL.Top e)
        {
            switch (context.get())
            {
                case VisitingContext.Concept:
                    return factory.getOWLThing();
                case VisitingContext.ObjectRole:
                    return factory.getOWLTopObjectProperty();
                case VisitingContext.DataRole:
                    return factory.getOWLTopDataProperty();
            }
            Assert(false);
            return null;
        }
        public object Visit(CogniPy.CNL.DL.Bottom e)
        {
            switch (context.get())
            {
                case VisitingContext.Concept:
                    return factory.getOWLNothing();
                case VisitingContext.ObjectRole:
                    return factory.getOWLBottomObjectProperty();
                case VisitingContext.DataRole:
                    return factory.getOWLBottomDataProperty();
            }
            Assert(false);
            return null;
        }
        public object Visit(CogniPy.CNL.DL.RoleInversion e)
        {
            if (context.get() == VisitingContext.DataRole)
                throw new NoInversionsForDataRolesException((e.R as CogniPy.CNL.DL.Atomic).id);
            Assert(context.get() == VisitingContext.ObjectRole);
            object o = e.R.accept(this);
            Assert(o is OWLObjectPropertyExpression);
            return factory.getOWLObjectInverseOf(o as OWLObjectPropertyExpression);
        }
        public object Visit(CogniPy.CNL.DL.InstanceSet e)
        {
            java.util.Set inds = new java.util.HashSet();
            foreach (var c in e.Instances)
            {
                inds.add(c.accept(this));
            }
            return factory.getOWLObjectOneOf(inds);
        }
        public object Visit(CogniPy.CNL.DL.ConceptOr e)
        {
            java.util.Set clss = new java.util.HashSet();
            foreach (var c in e.Exprs)
            {
                clss.add(c.accept(this));
            }
            return factory.getOWLObjectUnionOf(clss);
        }
        public object Visit(CogniPy.CNL.DL.ConceptAnd e)
        {
            java.util.Set clss = new java.util.HashSet();
            foreach (var c in e.Exprs)
            {
                clss.add(c.accept(this));
            }
            return factory.getOWLObjectIntersectionOf(clss);
        }
        public object Visit(CogniPy.CNL.DL.ConceptNot e)
        {
            return factory.getOWLObjectComplementOf(e.C.accept(this) as OWLClassExpression);
        }
        public object Visit(CogniPy.CNL.DL.OnlyRestriction e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.ObjectRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLObjectPropertyExpression);
            }
            object owlce = null;
            using (context.set(VisitingContext.Concept))
            {
                owlce = e.C.accept(this);
                Assert(owlce != null && owlce is OWLClassExpression);
            }
            return factory.getOWLObjectAllValuesFrom(owlpe as OWLObjectPropertyExpression, owlce as OWLClassExpression);
        }
        public object Visit(CogniPy.CNL.DL.SomeRestriction e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.ObjectRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLObjectPropertyExpression);
            }
            object owlce = null;
            using (context.set(VisitingContext.Concept))
            {
                owlce = e.C.accept(this);
                Assert(owlce != null && owlce is OWLClassExpression);
            }
            return factory.getOWLObjectSomeValuesFrom(owlpe as OWLObjectPropertyExpression, owlce as OWLClassExpression);
        }
        public object Visit(CogniPy.CNL.DL.OnlyValueRestriction e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.DataRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLDataPropertyExpression);
                object owlb = null;
                owlb = e.B.accept(this);
                Assert(owlb != null);
                return factory.getOWLDataAllValuesFrom(owlpe as OWLDataPropertyExpression, owlb as OWLDataRange);
            }
        }
        public object Visit(CogniPy.CNL.DL.SomeValueRestriction e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.DataRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLDataPropertyExpression);
                object owlb = null;
                owlb = e.B.accept(this);
                Assert(owlb != null);
                return factory.getOWLDataSomeValuesFrom(owlpe as OWLDataPropertyExpression, owlb as OWLDataRange);
            }
        }
        public object Visit(CogniPy.CNL.DL.SelfReference e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.ObjectRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLObjectPropertyExpression);
            }
            return factory.getOWLObjectHasSelf(owlpe as OWLObjectPropertyExpression);
        }
        public object Visit(CogniPy.CNL.DL.NumberRestriction e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.ObjectRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLObjectPropertyExpression);
            }
            object owlce = null;
            using (context.set(VisitingContext.Concept))
            {
                owlce = e.C.accept(this);
                Assert(owlce != null && owlce is OWLClassExpression);
            }
            if (e.Kind == "=")
                return factory.getOWLObjectExactCardinality(int.Parse(e.N), owlpe as OWLObjectPropertyExpression, owlce as OWLClassExpression);
            else if (e.Kind == "≤" || e.Kind == "<")
                return factory.getOWLObjectMaxCardinality(e.Kind == "<" ? int.Parse(e.N) - 1 : int.Parse(e.N), owlpe as OWLObjectPropertyExpression, owlce as OWLClassExpression);
            else if (e.Kind == "≥" || e.Kind == ">")
                return factory.getOWLObjectMinCardinality(e.Kind == ">" ? int.Parse(e.N) + 1 : int.Parse(e.N), owlpe as OWLObjectPropertyExpression, owlce as OWLClassExpression);
            else // different
            {
                return
                    factory.getOWLObjectUnionOf(
                        factory.getOWLObjectMaxCardinality(int.Parse(e.N) - 1, owlpe as OWLObjectPropertyExpression,
                                                           owlce as OWLClassExpression),
                        factory.getOWLObjectMinCardinality(int.Parse(e.N) + 1, owlpe as OWLObjectPropertyExpression,
                                                           owlce as OWLClassExpression));
            }
        }
        public object Visit(CogniPy.CNL.DL.NumberValueRestriction e)
        {
            object owlpe = null;
            using (context.set(VisitingContext.DataRole))
            {
                owlpe = e.R.accept(this);
                Assert(owlpe != null && owlpe is OWLDataPropertyExpression);
                object owlb = null;
                owlb = e.B.accept(this);
                Assert(owlb != null);
                if (e.Kind == "=")
                    return factory.getOWLDataExactCardinality(int.Parse(e.N), owlpe as OWLDataPropertyExpression, owlb as OWLDataRange);
                else if (e.Kind == "≤" || e.Kind == "<")
                    return factory.getOWLDataMaxCardinality(e.Kind == "<" ? int.Parse(e.N) - 1 : int.Parse(e.N), owlpe as OWLDataPropertyExpression, owlb as OWLDataRange);
                else if (e.Kind == "≥" || e.Kind == ">")
                    return factory.getOWLDataMinCardinality(e.Kind == ">" ? int.Parse(e.N) + 1 : int.Parse(e.N), owlpe as OWLDataPropertyExpression, owlb as OWLDataRange);
                else // different
                {
                    return
                        factory.getOWLObjectUnionOf(
                            factory.getOWLDataMaxCardinality(int.Parse(e.N) - 1, owlpe as OWLDataPropertyExpression,
                                                               owlpe as OWLDataRange),
                            factory.getOWLDataMinCardinality(int.Parse(e.N) + 1, owlpe as OWLDataPropertyExpression,
                                                               owlpe as OWLDataRange));
                }
            }
        }
        public static void Assert(bool b)
        {
            if (!b)
            {
#if DEBUG
                System.Diagnostics.Debugger.Break();
#endif
                throw new Exception("Conversion Assertion Failed dl->owlapi.");
            }
        }

        ///////////////// SWRL ///////////////////////////////////////////
        public object Visit(CNL.DL.SwrlStatement e)
        {
            return factory.getSWRLRule(e.slp.accept(this) as java.util.Set, e.slc.accept(this) as java.util.Set, getOWLAnnotationForStatement(e));
        }

        public object Visit(CNL.DL.SwrlItemList e)
        {
            java.util.Set atoms = new java.util.LinkedHashSet();
            for (int i = 0; i < e.list.Count; i++)
            {
                var x = e.list[i].accept(this);
                if (x is SWRLAtom)
                    atoms.add(x);
                else if (x is List<SWRLAtom>)
                {
                    foreach (var y in x as List<SWRLAtom>)
                        atoms.add(y);
                }
            }

            return atoms;
        }

        public object Visit(CNL.DL.SwrlInstance e)
        {
            object obj = null;
            using (context.set(VisitingContext.Concept))
            {
                if (e.C is CNL.DL.Node)
                {
                    obj = (e.C as CNL.DL.Node).accept(this);
                    Assert(obj is OWLClassExpression);
                    if (obj is OWLClass)
                        additionalHotfixDeclarations.Add(factory.getOWLSubClassOfAxiom(obj as OWLClass, factory.getOWLThing()));
                }
            }
            var arg = e.I.accept(this);
            Assert(arg is SWRLIArgument);
            return factory.getSWRLClassAtom(obj as OWLClassExpression, arg as SWRLIArgument);
        }

        public object Visit(CNL.DL.SwrlRole e)
        {
            var R = factory.getOWLObjectProperty(owlNC.getIRIFromId(e.R, EntityKind.Role));

            SWRLIArgument arg1 = e.I.accept(this) as SWRLIArgument;
            SWRLIArgument arg2 = e.J.accept(this) as SWRLIArgument;

            return factory.getSWRLObjectPropertyAtom(R, arg1, arg2);
        }

        public object Visit(CNL.DL.SwrlSameAs e)
        {
            SWRLIArgument arg1 = e.I.accept(this) as SWRLIArgument;
            SWRLIArgument arg2 = e.J.accept(this) as SWRLIArgument;

            return factory.getSWRLSameIndividualAtom(arg1, arg2);
        }


        public object Visit(CNL.DL.SwrlDifferentFrom e)
        {
            SWRLIArgument arg1 = e.I.accept(this) as SWRLIArgument;
            SWRLIArgument arg2 = e.J.accept(this) as SWRLIArgument;

            return factory.getSWRLDifferentIndividualsAtom(arg1, arg2);
        }

        public object Visit(CNL.DL.SwrlDataProperty e)
        {
            var R = factory.getOWLDataProperty(owlNC.getIRIFromId(e.R, EntityKind.Role));

            SWRLIArgument arg1 = e.IO.accept(this) as SWRLIArgument;
            SWRLDArgument arg2 = e.DO.accept(this) as SWRLDArgument;

            return factory.getSWRLDataPropertyAtom(R, arg1, arg2);
        }

        void AppendComparator(List<SWRLAtom> ret, string comparator, ISwrlObject A, ISwrlObject B)
        {
            SWRLBuiltInsVocabulary buitIn = null;
            switch (comparator)
            {
                case "": buitIn = SWRLBuiltInsVocabulary.EQUAL; break;
                case "=": buitIn = SWRLBuiltInsVocabulary.EQUAL; break;
                case "≤": buitIn = SWRLBuiltInsVocabulary.LESS_THAN_OR_EQUAL; break;
                case "≥": buitIn = SWRLBuiltInsVocabulary.GREATER_THAN_OR_EQUAL; break;
                case "<": buitIn = SWRLBuiltInsVocabulary.LESS_THAN; break;
                case ">": buitIn = SWRLBuiltInsVocabulary.GREATER_THAN; break;
                case "≠": buitIn = SWRLBuiltInsVocabulary.NOT_EQUAL; break;
                default: throw new InvalidOperationException();
            }
            java.util.ArrayList args = new java.util.ArrayList();
            args.add(B.accept(this) as SWRLDArgument);
            args.add(A.accept(this) as SWRLDArgument);
            ret.Add(factory.getSWRLBuiltInAtom(buitIn.getIRI(), args));
        }

        string mapCode(string code)
        {
            switch (code)
            {
                case "≤": return "<=";
                case "≥": return ">=";
                case "≠": return "<>";
                default: return code;
            }
        }

        public object Visit(SwrlBuiltIn e)
        {
            var builtInName = e.builtInName;
            var btag = KeyWords.Me.GetTag(mapCode(builtInName));
            List<SWRLAtom> ret = new List<SWRLAtom>();
            if (btag == "CMP" || btag == "EQ")
            {

                var A = e.Values[0];
                var B = e.Values[1];
                AppendComparator(ret, e.builtInName, A, B);
            }
            else
            {
                IRI buitIn = null;

                java.util.ArrayList lst = new java.util.ArrayList();
                lst.add(e.Values[e.Values.Count - 1].accept(this));

                for (int i = 0; i < e.Values.Count - 1; i++)
                    lst.add(e.Values[i].accept(this));

                if (builtInName == "plus" || builtInName == "times" || builtInName == "followed-by")
                {
                    if (builtInName == "followed-by")
                        buitIn = SWRLBuiltInsVocabulary.STRING_CONCAT.getIRI();
                    else if (builtInName == "plus")
                        buitIn = SWRLBuiltInsVocabulary.ADD.getIRI();
                    else if (builtInName == "times")
                        buitIn = SWRLBuiltInsVocabulary.MULTIPLY.getIRI();
                }
                else if (builtInName == "datetime" || builtInName == "duration")
                {
                    if (builtInName == "datetime")
                        buitIn = SWRLBuiltInsVocabulary.DATE_TIME.getIRI();
                    else if (builtInName == "duration")
                        buitIn = SWRLBuiltInsVocabulary.DAY_TIME_DURATION.getIRI();
                }
                else if (e.builtInName == "alpha-representation-of")
                {
                    buitIn = IRI.create("http://ontorion.com/swrlb#" + "alphaRepresentationOf");
                }
                else if (e.builtInName == "annotation")
                {
                    buitIn = IRI.create("http://ontorion.com/swrlb#" + "annotation");
                }
                else if (e.builtInName == "execute")
                {
                    buitIn = IRI.create("http://ontorion.com/swrlb#" + "executeExternalFunction");
                }
                else if (builtInName == "translated" || builtInName == "replaced")
                {
                    if (builtInName == "translated")
                        buitIn = SWRLBuiltInsVocabulary.TRANSLATE.getIRI();
                    else if (builtInName == "replaced")
                        buitIn = SWRLBuiltInsVocabulary.REPLACE.getIRI();
                }
                else if (builtInName == "from" || builtInName == "before" || builtInName == "after")
                {
                    if (e.builtInName == "from")
                        buitIn = SWRLBuiltInsVocabulary.SUBSTRING.getIRI();
                    else if (e.builtInName == "before")
                        buitIn = SWRLBuiltInsVocabulary.SUBSTRING_BEFORE.getIRI();
                    else if (e.builtInName == "after")
                        buitIn = SWRLBuiltInsVocabulary.SUBSTRING_AFTER.getIRI();
                }
                else if (e.Values.Count == 3)
                {
                    if (e.builtInName == "minus")
                        buitIn = SWRLBuiltInsVocabulary.SUBTRACT.getIRI();
                    else if (e.builtInName == "divided-by")
                        buitIn = SWRLBuiltInsVocabulary.DIVIDE.getIRI();
                    else if (e.builtInName == "integer-divided-by")
                        buitIn = SWRLBuiltInsVocabulary.INTEGER_DIVIDE.getIRI();
                    else if (e.builtInName == "modulo")
                        buitIn = SWRLBuiltInsVocabulary.MOD.getIRI();
                    else if (e.builtInName == "raised-to-the-power-of")
                        buitIn = SWRLBuiltInsVocabulary.POW.getIRI();
                    else if (e.builtInName == "rounded-with-the-precision-of")
                        buitIn = SWRLBuiltInsVocabulary.ROUND_HALF_TO_EVEN.getIRI();
                }
                else if (e.Values.Count == 2)
                {
                    if (e.builtInName == "not")
                        buitIn = SWRLBuiltInsVocabulary.BOOLEAN_NOT.getIRI();
                    else if (e.builtInName == "minus")
                        buitIn = SWRLBuiltInsVocabulary.UNARY_MINUS.getIRI();
                    else if (e.builtInName == "absolute-value-of")
                        buitIn = SWRLBuiltInsVocabulary.ABS.getIRI();
                    else if (e.builtInName == "ceiling-of")
                        buitIn = SWRLBuiltInsVocabulary.CEILING.getIRI();
                    else if (e.builtInName == "floor-of")
                        buitIn = SWRLBuiltInsVocabulary.FLOOR.getIRI();
                    else if (e.builtInName == "round-of")
                        buitIn = SWRLBuiltInsVocabulary.ROUND.getIRI();
                    else if (e.builtInName == "sine-of")
                        buitIn = SWRLBuiltInsVocabulary.SIN.getIRI();
                    else if (e.builtInName == "cosine-of")
                        buitIn = SWRLBuiltInsVocabulary.COS.getIRI();
                    else if (e.builtInName == "tangent-of")
                        buitIn = SWRLBuiltInsVocabulary.TAN.getIRI();
                    else if (e.builtInName == "case-ignored")
                        buitIn = SWRLBuiltInsVocabulary.STRING_EQUALS_IGNORE_CASE.getIRI();
                    else if (e.builtInName == "length-of")
                        buitIn = SWRLBuiltInsVocabulary.STRING_LENGTH.getIRI();
                    else if (e.builtInName == "space-normalized")
                        buitIn = SWRLBuiltInsVocabulary.NORMALIZE_SPACE.getIRI();
                    else if (e.builtInName == "upper-cased")
                        buitIn = SWRLBuiltInsVocabulary.UPPER_CASE.getIRI();
                    else if (e.builtInName == "lower-cased")
                        buitIn = SWRLBuiltInsVocabulary.LOWER_CASE.getIRI();
                    else if (e.builtInName == "contains-string")
                        buitIn = SWRLBuiltInsVocabulary.CONTAINS.getIRI();
                    else if (e.builtInName == "starts-with-string")
                        buitIn = SWRLBuiltInsVocabulary.STARTS_WITH.getIRI();
                    else if (e.builtInName == "ends-with-string")
                        buitIn = SWRLBuiltInsVocabulary.ENDS_WITH.getIRI();
                    else if (e.builtInName == "matches-string")
                        buitIn = SWRLBuiltInsVocabulary.MATCHES.getIRI();
                    else if (e.builtInName == "contains-case-ignored-string")
                        buitIn = SWRLBuiltInsVocabulary.CONTAINS_IGNORE_CASE.getIRI();
                    else if (e.builtInName == "sounds-like-string")
                        buitIn = IRI.create("http://ontorion.com/swrlb#" + "soundsLike");
                }

                ret.Add(factory.getSWRLBuiltInAtom(buitIn, lst));
            }
            return ret;
        }

        public object Visit(CNL.DL.SwrlDataRange e)
        {
            OWLDataRange dr = e.B.accept(this) as OWLDataRange;
            SWRLDArgument arg2 = e.DO.accept(this) as SWRLDArgument;
            return factory.getSWRLDataRangeAtom(dr, arg2);
        }

        ////////// SWRL //////////////////////////////////////////

        public object Visit(SwrlIterate e)
        {
            return null;
        }

        public object Visit(ExeStatement e)
        {
            return null;
        }

        public object Visit(SwrlVarList e)
        {
            Debugger.Break();
            throw new NotImplementedException();
        }


        public object Visit(SwrlDVal e)
        {
            return factory.getSWRLLiteralArgument(getLiteralVal(e.Val));
        }

        public object Visit(SwrlDVar e)
        {
            return factory.getSWRLVariable(owlNC.getIRIFromId(e.VAR, EntityKind.SWRLVariable));
        }

        public object Visit(SwrlIVal e)
        {
            return factory.getSWRLIndividualArgument(GetNamedIndividual(e.I));
        }

        public object Visit(SwrlIVar e)
        {
            return factory.getSWRLVariable(owlNC.getIRIFromId(e.VAR, EntityKind.SWRLVariable));
        }


        public object Visit(CodeStatement e)
        {
            return null;
        }
    }
}
