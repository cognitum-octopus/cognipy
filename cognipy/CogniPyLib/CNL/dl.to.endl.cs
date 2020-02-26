using System;

using System.Collections.Generic;
using System.Text;
using System.Linq;
using System.Text.RegularExpressions;
using CogniPy.CNL.DL;
using System.Diagnostics;

namespace CogniPy.CNL.EN
{
    public class Transform : CogniPy.CNL.DL.IVisitor
    {

        public Transform() {}

        public CogniPy.CNL.EN.paragraph Convert(CogniPy.CNL.DL.Paragraph p, bool usePrefixes = false, Func<string, string> ns2pfxEx = null)
        {
            this.usePrefixes = usePrefixes;
            this._ns2Pfx = ns2pfxEx;
            return p.accept(this) as CogniPy.CNL.EN.paragraph;
        }
        public IEnumerable<CogniPy.CNL.EN.sentence> Convert(CogniPy.CNL.DL.Statement s, bool usePrefixes = false, Func<string, string> ns2pfxEx = null)
        {
            this.usePrefixes = usePrefixes;
            this._ns2Pfx = ns2pfxEx;
            var o = s.accept(this);
            if (o is IEnumerable<CNL.EN.sentence>)
                return o as IEnumerable<CogniPy.CNL.EN.sentence>;
            else 
                return new List<CNL.EN.sentence>(){ o as CogniPy.CNL.EN.sentence};
        }
        public object Convert(CogniPy.CNL.DL.IAccept n, bool usePrefixes = false, Func<string, string> ns2pfxEx = null)
        {
            this.usePrefixes = usePrefixes;
            this._ns2Pfx = ns2pfxEx;
            var aa = n.accept(this);
            if (aa is TransNode)
                return ((TransNode)aa).makeorloop(false, false);
            else if (aa is TransTotalBound)
                return ((TransTotalBound)aa).bound();
            else if (aa is TransDTBound)
                return ((TransDTBound)aa).bound();
            else
                throw new NotImplementedException("Was neither TransNode nor TransTotalBound. Should implement it.");
        }

        public object Visit(CogniPy.CNL.DL.Paragraph p)
        {
            CogniPy.CNL.EN.paragraph ret = new CNL.EN.paragraph(null) { sentences = new List<CNL.EN.sentence>() };
            foreach (var x in p.Statements)
            {
                var o = x.accept(this);
                if (o is IEnumerable<CNL.EN.sentence>)
                    ret.sentences.AddRange(o as IEnumerable<CNL.EN.sentence>);
                else
                    ret.sentences.Add(o as CNL.EN.sentence);
            }
            return ret;
        }

        class TransNode
        {
            public virtual CNL.EN.orloop makeorloop(bool isPlural, bool isModal, TransAtomic atom = null)
            {
                CNL.EN.orloop orloop = new CNL.EN.orloop(null) { exprs = new List<CNL.EN.andloop>() };
                CNL.EN.andloop andloop = new CNL.EN.andloop(null) { exprs = new List<CNL.EN.objectRoleExpr>() };
                andloop.exprs.Add(objectRoleExpr(isPlural, isModal));
                orloop.exprs.Add(andloop);
                return orloop;
            }

            public virtual CNL.EN.subject subject()
            {
                Assert(false);
                return null;
            }

            public virtual CNL.EN.nosubject nosubject()
            {
                return null;
            }

            public virtual CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            //public virtual CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    Assert(false);
            //    return null;
            //}
            public virtual CNL.EN.single single(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public virtual oobject oobject(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public virtual CNL.EN.role role(bool isPlural, bool isModal, bool? isInverse)
            {
                Assert(false);
                return null;
            }
        }

        string resolveModality(Statement.Modality m)
        {
            switch (m)
            {
                case Statement.Modality.MUST: return "□";
                case Statement.Modality.SHOULD: return "◊";
                case Statement.Modality.CAN: return "◊◊";
                case Statement.Modality.MUSTNOT: return "~◊◊";
                case Statement.Modality.SHOULDNOT: return "~◊";
                case Statement.Modality.CANNOT: return "~□";
                default: return "";
            }
        }

        public object Visit(CogniPy.CNL.DL.Subsumption p)
        {
            var LeftSide = p.C.accept(this);
            if (p.D is ConceptNot)
            {
                var lss = (LeftSide as TransNode).nosubject();
                if (lss != null)
                {
                    var RightSide = (p.D as ConceptNot).C.accept(this);
                    Assert(LeftSide is TransNode);
                    Assert(RightSide is TransNode);
                    return new CNL.EN.nosubsumption(null, (LeftSide as TransNode).nosubject()
                        , resolveModality(p.modality)
                        , (RightSide as TransNode).makeorloop(false, p.modality != Statement.Modality.IS));
                }
            }
            {
                var RightSide = p.D.accept(this);
                Assert(LeftSide is TransNode);
                Assert(RightSide is TransNode);

                var sent = new CNL.EN.subsumption(null, (LeftSide as TransNode).subject()
                    , resolveModality(p.modality)
                    , (RightSide as TransNode).makeorloop(false, p.modality != Statement.Modality.IS));
                
                return sent;
            }
        }

        public object Visit(Annotation a)
        {
            return new CNL.EN.annotation(null, a.txt.Substring(1));
        }

        public object Visit(DLAnnotationAxiom a)
        {
            return new dlannotationassertion(null) { subject =a.subject, subjKind = a.subjKind, annotName = a.annotName, value = a.value, language = a.language };
        }

        public object Visit(CogniPy.CNL.DL.Equivalence p)
        {
            var ret = new List<CNL.EN.sentence>();
            var LeftSide = p.Equivalents[0].accept(this);
            Assert(LeftSide is TransNode);
            if (p.Equivalents.Count == 1)
            {
                ret.Add(new CNL.EN.equivalence2(null, (LeftSide as TransNode).makeorloop(false, p.modality != Statement.Modality.IS)
                                , resolveModality(p.modality)
                                , (LeftSide as TransNode).makeorloop(false, false)));
            }
            else
            {
                for (int i = 1; i < p.Equivalents.Count; i++)
                {
                    var RightSide = p.Equivalents[1].accept(this);
                    Assert(RightSide is TransNode);
                    ret.Add(new CNL.EN.equivalence2(null, (LeftSide as TransNode).makeorloop(false, p.modality != Statement.Modality.IS)
                                    , resolveModality(p.modality)
                                    , (RightSide as TransNode).makeorloop(false, false)));
                }
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.Disjoint p)
        {
            if (p.Disjoints.Count == 2)
            {
                var LeftSide =  p.Disjoints[0].accept(this);
                var RightSide = new DL.ConceptNot(null, p.Disjoints[1]).accept(this);
                Assert(LeftSide is TransNode);
                Assert(RightSide is TransNode);
                var sent = new CNL.EN.subsumption(null, (LeftSide as TransNode).subject()
                    , resolveModality(p.modality)
                    , (RightSide as TransNode).makeorloop(false, p.modality != Statement.Modality.IS));
                return sent;
            }
            else
            {
                var ret = new CNL.EN.exclusives(null) { objectRoleExprs = new List<objectRoleExpr>() };
                foreach (var e in p.Disjoints)
                {
                    var d = e.accept(this);
                    Assert(d is TransNode);

                    ret.objectRoleExprs.Add((d as TransNode).objectRoleExpr(false, p.modality != Statement.Modality.IS));
                }
                return ret;
            }
        }


        string defNs2Pfx(string ns)
        {
            if (!ns.EndsWith("/") && !ns.EndsWith("#") && !ns.Contains("#"))
                return ns +"#";
			else
            	return ns;
        }

        bool usePrefixes=false;
        Func<string, string> _ns2Pfx = null;
        Func<string, string> ns2Pfx
        {
            get
            {
                if (_ns2Pfx == null)
                    return defNs2Pfx;
                else
                    return _ns2Pfx;
            }
        }

        string FromDL(string name, bool bigName)
        {
            return FromDL(name, endict.WordKind.NormalForm, bigName);
        }
        
        string FromDL(string name, endict.WordKind kind, bool bigName)
        {
            var allParts = (new DlName() { id = name }).Split();
            if (usePrefixes)
            {
                if (!System.String.IsNullOrWhiteSpace(allParts.term) && allParts.term.StartsWith("<") && allParts.term.EndsWith(">"))
                {
                    var ns = allParts.term.Substring(1, allParts.term.Length - 2);
                    var tterm = ns2Pfx(ns);
                    if (!System.String.IsNullOrWhiteSpace(tterm))
                        allParts.term = tterm;
                    // if nothing is found there is no problem.
                }
            }

            return ENNameingConvention.FromDL(allParts.Combine(), kind, bigName).id;
        }

        public object Visit(CogniPy.CNL.DL.DisjointUnion p)
        {
            var ret = new CNL.EN.exclusiveunion(null) { objectRoleExprs = new List<objectRoleExpr>() };
            ret.name = FromDL(p.name, false);
            foreach (var e in p.Union)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.objectRoleExprs.Add((d as TransNode).objectRoleExpr(false, false));
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.DataTypeDefinition e)
        {
            return new CNL.EN.datatypedef(null, FromDL(e.name, false), (e.B.accept(this) as TransAbstractBound).bound());
        }

        public object Visit(CogniPy.CNL.DL.RoleInclusion e)
        {
            object r = e.C.accept(this);
            object s = e.D.accept(this);
            Assert(r is TransNode);
            Assert(s is TransNode);

            bool inv = e.D is CogniPy.CNL.DL.RoleInversion;
            var rr = convertToRoleWithXY((s as TransNode).role(false, e.modality != Statement.Modality.IS, inv));
            rr.inverse = inv;
            var sent = new CNL.EN.rolesubsumption(null,  (r as TransNode).role(false, false, false), rr);
            return sent;
        }

        roleWithXY convertToRoleWithXY(role r)
        {
            return new roleWithXY(r.yyps, r.name, r.inverse);
        }

        notRoleWithXY convertToNotRoleWithXY(role r)
        {
            return new notRoleWithXY(r.yyps, r.name, r.inverse);
        }

        public object Visit(CogniPy.CNL.DL.RoleEquivalence p)
        {
            var ret = new List<CNL.EN.sentence>();

            CNL.EN.role rol;
            {
                bool inv = p.Equivalents[0] is CogniPy.CNL.DL.RoleInversion;
                var d = p.Equivalents[0].accept(this);
                Assert(d is TransNode);
                rol = (d as TransNode).role(false, false, false);
                
            }
            if (p.Equivalents.Count == 1)
            {
                var it = new CNL.EN.roleequivalence2(null);
                it.r = rol;
                it.s = convertToRoleWithXY(rol);
                ret.Add(it);
            }
            else
            {
                for (int j = 1; j < p.Equivalents.Count; j++)
                {
                    var it = new CNL.EN.roleequivalence2(null);
                    it.r = rol;
                    bool inv = p.Equivalents[j] is CogniPy.CNL.DL.RoleInversion;
                    var d = p.Equivalents[j].accept(this);
                    Assert(d is TransNode);
                    var rr = convertToRoleWithXY((d as TransNode).role(false, false, null));
                    rr.inverse = inv;
                    it.s = rr;
                    ret.Add(it);
                }
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.DataRoleEquivalence p)
        {
            var ret = new List<CNL.EN.sentence>();

            CNL.EN.role rol;
            {
                var d = p.Equivalents[0].accept(this);
                Assert(d is TransNode);
                rol = (d as TransNode).role(false, false, false);
            }
            if (p.Equivalents.Count == 1)
            {
                var it = new CNL.EN.dataroleequivalence2(null);
                it.r = rol;
                it.s = rol;
                ret.Add(it);
            }
            else
            {
                for (int j = 1; j < p.Equivalents.Count; j++)
                {
                    var it = new CNL.EN.dataroleequivalence2(null);
                    it.r = rol;

                    var d = p.Equivalents[j].accept(this);
                    Assert(d is TransNode);

                    it.s = (d as TransNode).role(false, false, false);
                    ret.Add(it);
                }
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.RoleDisjoint p)
        {
            var ret = new CNL.EN.roledisjoint2(null);
            Assert(p.Disjoints.Count == 2);
            for(int i=0; i<p.Disjoints.Count;i++)
            {
                bool inv = p.Disjoints[i] is CogniPy.CNL.DL.RoleInversion;
                var d = p.Disjoints[i].accept(this);
                Assert(d is TransNode);

                if(i==0)
                    ret.r=(d as TransNode).role(false, false, false);
                if (i == 1)
                {
                    var rr = convertToNotRoleWithXY((d as TransNode).role(false, true, null));
                    rr.inverse = inv;
                    ret.s = rr;
                }
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.ComplexRoleInclusion e)
        {
            CNL.EN.chain z = new CNL.EN.chain(null) { roles = new List<CNL.EN.role>() };
            foreach (var r in e.RoleChain)
            {
                z.roles.Add((r.accept(this) as TransNode).role(false, false, false));
            }
            bool inv = e.R is CogniPy.CNL.DL.RoleInversion;
            object t = e.R.accept(this);
            Assert(t is TransNode);
            var rr = convertToRoleWithXY((t as TransNode).role(false, false, false));
            rr.inverse = inv;
            var sent = new CNL.EN.rolesubsumption(null, z, rr);
            return sent;
        }

        public object Visit(CogniPy.CNL.DL.DataRoleInclusion e)
        {
            object r = e.C.accept(this);
            object s = e.D.accept(this);
            Assert(r is TransNode);
            Assert(s is TransNode);
            var sent = new CNL.EN.datarolesubsumption(null, (r as TransNode).role(false, false, false),
                              (s as TransNode).role(false, e.modality != Statement.Modality.IS, false));
            return sent;
        }

        public object Visit(CogniPy.CNL.DL.DataRoleDisjoint p)
        {
            var ret = new CNL.EN.dataroledisjoint2(null);
            Assert(p.Disjoints.Count == 2);
            for (int i = 0; i < p.Disjoints.Count; i++)
            {
                var d = p.Disjoints[i].accept(this);
                Assert(d is TransNode);

                if (i == 0)
                    ret.r = (d as TransNode).role(false, false, false);
                if (i == 1)
                    ret.s = (d as TransNode).role(false, true, false);
            }
            return ret; 
        }

        public object Visit(CogniPy.CNL.DL.InstanceOf e)
        {
            object c = e.C.accept(this);
            var i = new TransInstanceSingle() { Instance = e.I.accept(this) as TransInstance};

            var sent = new CNL.EN.subsumption(null, i.subject()
                , resolveModality(e.modality)
                , (c as TransNode).makeorloop(false, e.modality != Statement.Modality.IS));
            return sent;
        }

        public object Visit(CogniPy.CNL.DL.RelatedInstances p)
        {
            var i = new TransInstanceSingle() { Instance = p.I.accept(this) as TransInstance };
            var j = new TransInstanceSingle() { Instance = p.J.accept(this) as TransInstance };
            var Role = p.R.accept(this) as TransNode;

            var ore = new TransSomeRestriction(){ C = j, R = Role};

            var sent = new CNL.EN.subsumption(null, i.subject()
                , resolveModality(p.modality)
                , ore.makeorloop(false, p.modality != Statement.Modality.IS));          
            return sent;
        }

        public object Visit(CogniPy.CNL.DL.InstanceValue e)
        {
            var i = new TransInstanceSingle() { Instance = e.I.accept(this) as TransInstance };
            var v = e.V.accept(this) as TransValue;
            var Role = e.R.accept(this) as TransNode;

            var ore = new TransSomeValueRestriction() { B = new TransBoundVal() { Kind = "=", V = v }, R = Role };

            var sent = new CNL.EN.subsumption(null, i.subject()
                , resolveModality(e.modality)
                , ore.makeorloop(false, e.modality != Statement.Modality.IS));
            return sent;
        }

        public object Visit(CogniPy.CNL.DL.SameInstances p)
        {
            var ret = new CNL.EN.equivalence2(null);
            ret.modality = resolveModality(p.modality);

            Assert(p.Instances.Count == 2);
            for (int i = 0; i < p.Instances.Count; i++)
            {
                Assert(p.Instances[i] is NamedInstance);

                var ii = new TransInstanceSingle() { Instance = new TransNamedInstance(this) { id = (p.Instances[i] as NamedInstance).name } };

                if (i == 0)
                    ret.c = ii.makeorloop(false, false);
                if (i == 1)
                    ret.d = ii.makeorloop(false, false);
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.DifferentInstances p)
        {
            var ret = new CNL.EN.exclusives(null) { objectRoleExprs = new List<objectRoleExpr>() };
            ret.modality = resolveModality(p.modality);
            foreach (var e in p.Instances)
            {
                Assert(e is NamedInstance);

                var ii = new TransInstanceSingle() { Instance = new TransNamedInstance(this) { id = (e as NamedInstance).name } };

                ret.objectRoleExprs.Add(ii.objectRoleExpr(false, p.modality != Statement.Modality.IS));
            }
            return ret;
        }

        public object Visit(CogniPy.CNL.DL.HasKey p)
        {
            var ret = new CNL.EN.haskey(null) { dataroles = new List<role>(), roles = new List<role>() };
            var x = p.C.accept(this);
            Assert(x is TransNode);
            ret.s = (x as TransNode).objectRoleExpr(false, false);
            foreach (var e in p.Roles)
            {
                var d = e.accept(this);
                Assert(d is TransNode);
                ret.roles.Add((d as TransNode).role(false, false, false));
            }
            foreach (var e in p.DataRoles)
            {
                var d = e.accept(this);
                Assert(d is TransNode);
                ret.dataroles.Add((d as TransNode).role(false, false, false));
            }
            return ret;
        }

        interface TransInstance
        {
            CNL.EN.instance instance(bool isPlural, bool isModal);
        }

        class TransNamedInstance : TransInstance
        {
            public TransNamedInstance(Transform me)
            {
                _me = me;
            }
            private Transform _me;
            public string id;
            public CNL.EN.instance instance(bool isPlural, bool isModal)
            {
                if (id.StartsWith("_"))
                    return new CNL.EN.instanceBigName(null,_me.FromDL(id.Substring(1), true), false);
                else
                    return new CNL.EN.instanceBigName(null, _me.FromDL(id, true), true);
            }
        }

        public object Visit(CogniPy.CNL.DL.NamedInstance e)
        {
            return new TransNamedInstance(this) {  id = e.name };
        }

        class TransUnnamedInstance : TransInstance
        {
            public TransNode C;
            public CNL.EN.instance instance(bool isPlural, bool isModal)
            {
                return new CNL.EN.instanceThe(null,false, C.single(isPlural, isModal));
            }
        }

        class TransUnnamedOnlyInstance : TransInstance
        {
            public TransNode C;
            public CNL.EN.instance instance(bool isPlural, bool isModal)
            {
                return new CNL.EN.instanceThe(null, true, C.single(isPlural, isModal));
            }
        }

        public object Visit(CogniPy.CNL.DL.UnnamedInstance e)
        {
            object o =  e.C.accept(this);
            Assert ( o  is TransNode);
            if (e.Only)
                return new TransUnnamedOnlyInstance() { C = o as TransNode };
            else
                return new TransUnnamedInstance() { C = o as TransNode };
        }

        abstract class TransValue
        {
            public abstract CNL.EN.dataval dataval();
            public virtual CNL.EN.facet facet(string Kind)
            {
                return new CNL.EN.facet(null, Kind, dataval());
            }
        }

        class TransNumber : TransValue
        {
            public string val;
            public override dataval dataval()
            {
                return new CNL.EN.Number(null, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.Number e)
        {
            return new TransNumber() { val = e.val };
        }

        class TransBool : TransValue
        {
            public string val;
            public override dataval dataval()
            {
                return new CNL.EN.Bool(null, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.Bool e)
        {
            return new TransBool() { val = e.val == "[1]" ? "true" : "false" };
        }

        class TransDateTimeVal : TransValue
        {
            public string val;
            public override dataval dataval()
            {
                return new CNL.EN.DateTimeData(null, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.DateTimeVal e)
        {
            return new TransDateTimeVal() { val = e.val };
        }

        class TransDuration : TransValue
        {
            public string val;
            public override dataval dataval()
            {
                return new CNL.EN.Duration(null, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.Duration e)
        {
            return new TransDuration() { val = e.val };
        }

        class TransString : TransValue
        {
            public string val;
            public override dataval dataval()
            {
                return new CNL.EN.StrData(null, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.String e)
        {
            return new TransString() { val = e.val };
        }

        class TransFloat:TransValue
        {
            public string val;
            public override dataval dataval()
            {
                return new CNL.EN.Float(null, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.Float e)
        {
            return new TransFloat() { val = e.val };
        }

        interface TransAbstractBound
        {
            CNL.EN.abstractbound bound();
        }


        class TransDataSetBound : TransAbstractBound
        {
            public List<Value> Values;
            public CNL.EN.abstractbound bound()
            {
                var eset = new boundOneOf(null) { vals = new List<dataval>() };
                foreach (var v in Values)
                {
                    if (v is CogniPy.CNL.DL.String)
                        eset.vals.Add(new StrData(null) { val = v.getVal() });
                    else if (v is CogniPy.CNL.DL.Float)
                        eset.vals.Add(new CogniPy.CNL.EN.Float(null) { val = v.getVal() });
                    else if (v is CogniPy.CNL.DL.Number)
                        eset.vals.Add(new CogniPy.CNL.EN.Number(null) { val = v.getVal() });
                    else if (v is CogniPy.CNL.DL.Bool)
                        eset.vals.Add(new CogniPy.CNL.EN.Bool(null) { val = (v.getVal() == "[1]") ? "true" : "false" });
                    else if (v is CogniPy.CNL.DL.DateTimeVal)
                        eset.vals.Add(new CogniPy.CNL.EN.DateTimeData(null) { val = v.getVal() });
                    else if (v is CogniPy.CNL.DL.Duration)
                        eset.vals.Add(new CogniPy.CNL.EN.Duration(null) { val = v.getVal() });
                    else
                        Assert(false);
                }
                return eset;
            }
        }

        class TransTotalBound : TransAbstractBound
        {
            public Value V;
            public CNL.EN.abstractbound bound()
            {
                if (V is CNL.DL.Float)
                    return new CNL.EN.boundTotal(null, "DBL");
                else if (V is CNL.DL.Number)
                    return new CNL.EN.boundTotal(null, "NUM");
                else if (V is CNL.DL.Bool)
                    return new CNL.EN.boundTotal(null, "BOL");
                else if (V is CNL.DL.String)
                    return new CNL.EN.boundTotal(null, "STR");
                else if (V is CNL.DL.DateTimeVal)
                    return new CNL.EN.boundTotal(null, "DTM");
                else if (V is CNL.DL.Duration)
                    return new CNL.EN.boundTotal(null, "DUR");
                else
                {
                    Assert(false);
                    return null;
                }
            }
        }

        class TransDTBound : TransAbstractBound
        {
            private Transform _me;
            public TransDTBound(Transform me)
            {
                _me = me;
            }
            public string name;
            public CNL.EN.abstractbound bound()
            {
                return new CNL.EN.boundDataType(null, _me.FromDL(name, false));
            }
        }
        class TransTopBound : TransAbstractBound
        {
            public CNL.EN.abstractbound bound()
            {
                return new CNL.EN.boundTop(null);
            }
        }

        class TransFacet 
        {
            public string Kind;
            public TransValue V;
            public CNL.EN.facet facet()
            {
                return V.facet(Kind);
            }
        }

        class TransBoundFacet : TransAbstractBound
        {
            public List<TransFacet> TF;
            public CNL.EN.abstractbound bound()
            {
                var bnd = new boundFacets(null) { l = new facetList(null) { Facets = new List<facet>() } };
                foreach (var f in TF)
                    bnd.l.Facets.Add(f.facet());
                return bnd;
            }
        }

        public object Visit(CogniPy.CNL.DL.Facet e)
        {
            throw new InvalidOperationException();
        }

        public object Visit(CogniPy.CNL.DL.FacetList e)
        {
            throw new InvalidOperationException();
        }

        public object Visit(CogniPy.CNL.DL.BoundFacets e)
        {
            var tf = new List<TransFacet>();
            foreach(var f in e.FL.List)
                tf.Add(new TransFacet(){ Kind = f.Kind, V = f.V.accept(this) as TransValue});
            return new TransBoundFacet() { TF= tf};
        }

        class TransBoundOr : TransAbstractBound
        {
            public List<TransAbstractBound> Bnds;
            public abstractbound bound()
            {
                var o = new boundOr(null) { List = new List<abstractbound>() };
                foreach (var f in Bnds)
                    o.List.Add(f.bound());
                return o;
            }
        }

        public object Visit(BoundOr e)
        {
           var bnds= new List<TransAbstractBound>();
           foreach (var b in e.List)
               bnds.Add(b.accept(this) as TransAbstractBound);
           return new TransBoundOr() { Bnds = bnds };
        }

        class TransBoundAnd : TransAbstractBound
        {
            public List<TransAbstractBound> Bnds;
            public abstractbound bound()
            {
                var o = new boundAnd(null) { List = new List<abstractbound>() };
                foreach (var f in Bnds)
                    o.List.Add(f.bound());
                return o;
            }
        }

        public object Visit(BoundAnd e)
        {
            var bnds = new List<TransAbstractBound>();
            foreach (var b in e.List)
                bnds.Add(b.accept(this) as TransAbstractBound);
            return new TransBoundAnd() { Bnds = bnds };
        }

        class TransBoundNot : TransAbstractBound
        {
            public TransAbstractBound Bnd;
            public abstractbound bound()
            {
                return new boundNot(null) { bnd = Bnd.bound() };
            }
        }
        public object Visit(BoundNot e)
        {
            return new TransBoundNot() { Bnd = e.B.accept(this) as TransAbstractBound };
        }
        
        class TransBoundVal : TransAbstractBound
        {
            public string Kind;
            public TransValue V;
            public CNL.EN.abstractbound bound()
            {
                var f = V.facet(Kind);
                return new boundVal(null) { Cmp = f.Cmp, V = f.V };
            }
        }

        public object Visit(BoundVal e)
        {
            return new TransBoundVal() { Kind = e.Kind, V = e.V.accept(this) as TransValue };
        }

        public object Visit(CogniPy.CNL.DL.TotalBound e)
        {
            return new TransTotalBound() { V = e.V };
        }

        public object Visit(DTBound e)
        {
            return new TransDTBound(this) { name = e.name };
        }

        public object Visit(CogniPy.CNL.DL.TopBound e)
        {
            return new TransTopBound();
        }

        class TransAtomic : TransNode
        {
            private Transform _me;
            public TransAtomic(Transform me)
            {
                _me = me;
            }
            public string id;
            public override CNL.EN.nosubject nosubject()
            {
                return new CNL.EN.subjectNo(null, new CNL.EN.singleName(null, _me.FromDL(id, false)));
            }
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEvery(null, new CNL.EN.singleName(null, _me.FromDL(id, false)));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectA(null, new CNL.EN.singleName(null, isPlural ? _me.FromDL(id, CNL.EN.endict.WordKind.PluralFormNoun, false) : _me.FromDL(id, false))));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectA(null, new CNL.EN.singleName(null, isPlural ? (new Transform()).FromDL(id, CNL.EN.endict.WordKind.PluralForm, false) : (new Transform()).FromDL(id, false))));
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleName(null, isPlural ? _me.FromDL(id, CNL.EN.endict.WordKind.PluralFormNoun, false) : _me.FromDL(id, false));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
            public override CNL.EN.role role(bool isPlural, bool isModal, bool? isInverse)
            {
                if (isInverse.HasValue && isInverse.Value)
                    return new CNL.EN.role(null, _me.FromDL(id, CNL.EN.endict.WordKind.SimplePast, false), true);
                else
                    return new CNL.EN.role(null, _me.FromDL(id, isPlural ? CNL.EN.endict.WordKind.PluralFormVerb : (isModal ? CNL.EN.endict.WordKind.NormalForm : CNL.EN.endict.WordKind.PastParticiple), false), false);
            }
        }
        public object Visit(CogniPy.CNL.DL.Atomic e)
        {
            return new TransAtomic(this) { id = e.id };
        }

        class TransTop : TransNode
        {
            public override CNL.EN.nosubject nosubject()
            {
                return new CNL.EN.subjectNothing(null);
            }
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null);
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectSomething(null));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectSomething(null));
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThing(null);
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectSomething(null);
            }
            public override CNL.EN.role role(bool isPlural, bool isModal, bool? isInverse)
            {
                return new CNL.EN.role(null, ENNameingConvention.TOPROLENAME, isInverse ?? false);
            }
        }
        public object Visit(CogniPy.CNL.DL.Top e)
        {
            return new TransTop();
        }

        class TransBottom : TransNode
        {
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectNothing(null));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectNothing(null));
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectNothing(null))))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectNothing(null);
            }
            public override CNL.EN.role role(bool isPlural, bool isModal, bool? isInverse)
            {
                return new CNL.EN.role(null, ENNameingConvention.BOTTOMROLENAME, isInverse??false);
            }
        }
        public object Visit(CogniPy.CNL.DL.Bottom e)
        {
            return new TransBottom();
        }

        class TransRoleInversion : TransNode
        {
            public TransNode R;
            public override CNL.EN.role role(bool isPlural, bool isModal, bool? isInverse)
            {
                bool? val = null;
                if (isInverse.HasValue)
                    val = !isInverse.Value;
                return R.role(isPlural, isModal, val);
            }
        }
        public object Visit(CogniPy.CNL.DL.RoleInversion e)
        {
            object o = e.R.accept(this);
            Assert(o is TransNode);
            return new TransRoleInversion() { R = o as TransNode };
        }

        class TransInstanceSingle : TransNode
        {
            public TransInstance Instance;
            public override CNL.EN.subject subject()
            {
                object o = Instance.instance(false, false);
                if (o is CNL.EN.instanceThe)
                    return new CNL.EN.subjectThe(null, (o as CNL.EN.instanceThe).only, (o as CNL.EN.instanceThe).s);
                else if (o is CNL.EN.instanceBigName)
                {
                    var n = (o as CNL.EN.instanceBigName).name;
                    if (n.StartsWith("_"))
                        return new CNL.EN.subjectBigName(null, n.Substring(1), false);
                    else
                        return new CNL.EN.subjectBigName(null, n, true);
                }
                Assert(false);
                return null;
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                var o = Instance.instance(isPlural, isModal);
                if (o != null)
                    return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectInstance(null, o));
                Assert(false);
                return null;
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    var o = Instance.instance(isPlural, isModal);
            //    if (o != null)
            //        return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectInstance(null, o));
            //    Assert(false);
            //    return null;
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, new CNL.EN.objectRoleExpr1(null, false, oobject(isPlural, isModal))))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                var o = Instance.instance(isPlural, isModal);
                if (o != null)
                    return new CNL.EN.oobjectInstance(null, o);
                Assert(false);
                return null;
            }
        }
        class TransInstanceSet : TransNode
        {
            public List<TransInstance> Instances = new List<TransInstance>();
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEvery(null, single(false, false));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.EN.objectRoleExpr1(null, false,
                    new CNL.EN.oobjectA(null, single(false, false)));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false,
            //        new CNL.EN.oobjectA(null, single(false, false)));
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                Assert(Instances.Count > 1);

                List<CNL.EN.instance> insts = new List<CNL.EN.instance>();

                foreach (var i in Instances)
                    insts.Add(i.instance(false, false));

                return new CNL.EN.singleOneOf(null, new CNL.EN.instanceList(null) { insts = insts });
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.InstanceSet e)
        {
            if (e.Instances.Count == 0)
                return new TransBottom();
            else if (e.Instances.Count == 1)
                return new TransInstanceSingle() { Instance = (e.Instances[0].accept(this) as TransInstance) };
            else
            {
                TransInstanceSet ret = new TransInstanceSet();
                foreach (var I in e.Instances)
                {
                    object inner = I.accept(this);
                    Assert(inner is TransInstance);
                    ret.Instances.Add(inner as TransInstance);
                }
                return ret;
            }
        }

        public object Visit(CogniPy.CNL.DL.ValueSet e)
        {
            if (e.Values.Count == 1)
                return new TransBoundFacet() { TF = new List<TransFacet>() { new TransFacet() { Kind = "=", V = e.Values[0].accept(this) as TransValue } } };
            else
            {
                TransDataSetBound ret = new TransDataSetBound();
                ret.Values = e.Values;
                return ret;
            }
        }

        class TransConceptOr : TransNode
        {
            private Transform _me;
            public TransConceptOr(Transform me)
            {
                _me = me;
            }
            public List<TransNode> Exprs = new List<TransNode>();
            TransAtomic findFirstAtom()
            {
                foreach (var C in Exprs)
                {
                    if (C is TransAtomic)
                        return C as TransAtomic;
                }
                return null;
            }

            public override CNL.EN.orloop makeorloop(bool isPlural, bool isModal, TransAtomic atom)
            {
                CNL.EN.orloop orloop = new CNL.EN.orloop(null) { exprs = new List<CNL.EN.andloop>() };
                foreach (var C in Exprs)
                {
                    if (C != atom)
                    {
                        CNL.EN.andloop andloop = new CNL.EN.andloop(null) { exprs = new List<CNL.EN.objectRoleExpr>() };
                        andloop.exprs.Add(C.objectRoleExpr(isPlural, isModal));
                        orloop.exprs.Add(andloop);
                    }
                }
                return orloop;
            }

            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, makeorloop(false,false,null)));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectSomethingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null))));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectSomethingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null))));
            //}
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                if (Exprs.Count == 1)
                {
                    var atom = findFirstAtom();
                    if (atom == null)
                        return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
                    else
                        return new CNL.EN.singleNameThat(null, _me.FromDL(atom.id, false), new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, atom)));
                }
                else
                {
                    return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
                }
            }
        }
        public object Visit(CogniPy.CNL.DL.ConceptOr e)
        {
            TransConceptOr ret = new TransConceptOr(this);
            foreach (var C in e.Exprs)
            {
                object inner = C.accept(this);
                Assert(inner is TransNode);
                ret.Exprs.Add(inner as TransNode);
            }
            return ret;
        }

        class TransConceptAnd : TransNode
        {
            Transform _me;
            public TransConceptAnd(Transform me)
            {
                _me = me;
            }

            public List<TransNode> Exprs = new List<TransNode>();
            TransAtomic findFirstAtom()
            {
                foreach (var C in Exprs)
                {
                    if (C is TransAtomic)
                        return C as TransAtomic;
                }
                return null;
            }
            public override CNL.EN.orloop makeorloop(bool isPlural, bool isModal, TransAtomic atom)
            {
                CNL.EN.andloop andloop = new CNL.EN.andloop(null) { exprs = new List<CNL.EN.objectRoleExpr>() };
                foreach (var C in Exprs)
                {
                    if(C!=atom)
                        andloop.exprs.Add(C.objectRoleExpr(isPlural, isModal));
                }
                return new CNL.EN.orloop(null,andloop);
            }

            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, makeorloop(false,false,null)));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.EN.objectRoleExpr1(null, false, oobject(isPlural,isModal));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, oobject(isPlural, isModal));
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                var atom = findFirstAtom();
                if (atom == null)
                    return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
                else
                    return new CNL.EN.singleNameThat(null, _me.FromDL(atom.id, false), new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, atom)));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.ConceptAnd e)
        {
            TransConceptAnd ret = new TransConceptAnd(this);
            foreach (var C in e.Exprs)
            {
                object inner = C.accept(this);
                Assert(inner is TransNode);
                ret.Exprs.Add(inner as TransNode);
            }
            return ret;
        }

        class TransConceptNot : TransNode
        {
            Transform _me;
            public TransConceptNot(Transform me)
            {
                _me = me;
            }
            public TransNode C;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null,new CNL.EN.andloop(null, objectRoleExpr(false,false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                if (C is TransInstanceSingle)
                {
                    var ic = C as TransInstanceSingle;
                    if (ic.Instance is TransNamedInstance)
                    {
                        var n = (ic.Instance as TransNamedInstance).id;
                        var i = n.StartsWith("_") ?
                            new instanceBigName(null, _me.FromDL(n.Substring(1), true), false)
                           : new instanceBigName(null, _me.FromDL(n, true), true);
                        return new CNL.EN.objectRoleExpr1(null, true, new CNL.EN.oobjectInstance(null, i));

                    }
                }
                else if (C is TransSomeRestriction)
                {
                    var ic2 = C as TransSomeRestriction;
                    if (ic2.C is TransInstanceSingle)
                    {
                        var ic = ic2.C as TransInstanceSingle;
                        if (ic.Instance is TransNamedInstance)
                        {
                            var n = (ic.Instance as TransNamedInstance).id;
                            var i = n.StartsWith("_") ?
                                new instanceBigName(null, _me.FromDL(n.Substring(1), true), false)
                               : new instanceBigName(null, _me.FromDL(n, true), true);
                            return new CNL.EN.objectRoleExpr2(null, true, new CNL.EN.oobjectInstance(null, i), ic2.R.role(false, true, false));
                        }
                    }
                }
                return new CNL.EN.objectRoleExpr1(null, true, new CNL.EN.oobjectA(null, C.single(isPlural, isModal)));
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    if (C is TransInstanceSingle)
            //    {
            //        var ic = C as TransInstanceSingle;
            //        if (ic.Instance is TransNamedInstance)
            //        {
            //            var n = (ic.Instance as TransNamedInstance).id;
            //            var i = n.StartsWith("_") ?
            //                new instanceBigName(null, (new Transform()).FromDL(n.Substring(1), true), false)
            //               : new instanceBigName(null, (new Transform()).FromDL(n, true), true);
            //            return new CNL.EN.defObjectRoleExpr1(null, true, new CNL.EN.oobjectInstance(null, i));
            //        }
            //    }
            //    else if (C is TransSomeRestriction)
            //    {
            //        Assert(false);
            //    }
            //    return new CNL.EN.defObjectRoleExpr1(null, true, new CNL.EN.oobjectA(null, C.single(isPlural, isModal)));
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, new CNL.EN.objectRoleExpr1(null, true, new CNL.EN.oobjectA(null, C.single(isPlural,isModal)))))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.ConceptNot e)
        {
            object o = e.C.accept(this);
            Assert(o is TransNode);
            return new TransConceptNot(this) { C = o as TransNode };
        }

        class TransOnlyRestriction : TransNode
        {
            public TransNode R;
            public TransNode C;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false,false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectOnly(null, C.single(true,false)), RN);
                return ore;
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, oobject(isPlural, isModal));
            //}
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
        }
        public object Visit(CogniPy.CNL.DL.OnlyRestriction e)
        {
            object r = e.R.accept(this);
            object c = e.C.accept(this);
            Assert(r is TransNode);
            Assert(c is TransNode);
            return new TransOnlyRestriction() { R = r as TransNode, C = c as TransNode };
        }

        class TransSomeRestriction : TransNode
        {
            public TransNode R;
            public TransNode C;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                if ((C is TransConceptNot) || (C is TransConceptOr) || (C is TransConceptAnd))
                {
                    ore = new CNL.EN.objectRoleExpr3(null, new thatOrLoop(null, C.makeorloop(isPlural, false)), RN);
                    return ore;
                }
                else
                {
                    ore = new CNL.EN.objectRoleExpr2(null, false, C is TransTop ? null : C.oobject(isPlural, false), RN);
                    return ore;
                }
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    return new CNL.EN.defObjectRoleExpr1(null, false, oobject(isPlural, isModal));
            //}
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectSomethingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural,isModal)))));
            }
        }
        public object Visit(CogniPy.CNL.DL.SomeRestriction e)
        {
            object r = e.R.accept(this);
            object c = e.C.accept(this);
            Assert(r is TransNode);
            Assert(c is TransNode);
            return new TransSomeRestriction() { R = r as TransNode, C = c as TransNode };
        }

        class TransOnlyValueRestriction : TransNode
        {
            public TransNode R;
            public TransAbstractBound B;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectOnlyBnd(null, B.bound() ), RN);
                return ore;
            }
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.OnlyValueRestriction e)
        {
            object r = e.R.accept(this);
            object b = e.B.accept(this);
            Assert(r is TransNode);
            Assert(b is TransAbstractBound);
            return new TransOnlyValueRestriction() { R = r as TransNode, B = b as TransAbstractBound };
        }

        class TransSomeValueRestriction : TransNode
        {
            public TransNode R;
            public TransAbstractBound B;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectBnd(null, B.bound()), RN);
                return ore;
            }
                //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
                //{
                //    Assert(false);
                //    return null;
                //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.SomeValueRestriction e)
        {
            object r = e.R.accept(this);
            object b = e.B.accept(this);
            Assert(r is TransNode);
            Assert(b is TransAbstractBound);
            return new TransSomeValueRestriction() { R = r as TransNode, B = b as TransAbstractBound };
        }

        class TransSelfReference : TransNode
        {
            public TransNode R;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectSelf(null), RN);
                return ore;
            }
            //public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            //{
            //    Assert(false);
            //    return null;
            //}
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.SelfReference e)
        {
            object r = e.R.accept(this);
            Assert(r is TransNode);
            return new TransSelfReference() { R = r as TransNode };
        }

        class TransNumberRestriction : TransNode
        {
            public string Kind;
            public string N;
            public TransNode R;
            public TransNode C;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectCmp(null, Kind, N, C.single(long.Parse(N)!=1,false)), RN);
                return ore;
            }
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.NumberRestriction e)
        {
            object r = e.R.accept(this);
            object c = e.C.accept(this);
            Assert(r is TransNode);
            Assert(c is TransNode);
            return new TransNumberRestriction() { R = r as TransNode, C = c as TransNode, Kind = e.Kind, N = e.N };
        }

        class TransNumberValueRestriction : TransNode
        {
            public string Kind;
            public string N;
            public TransNode R;
            public TransAbstractBound B;
            public override CNL.EN.subject subject()
            {
                return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
            }
            public override CNL.EN.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.EN.objectRoleExpr ore = null;
                var RN = R.role(isPlural, isModal, false);
                ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectCmpBnd(null, Kind, N, B.bound()), RN);
                return ore;
            }
            public override CNL.EN.single single(bool isPlural, bool isModal)
            {
                return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public override oobject oobject(bool isPlural, bool isModal)
            {
                return new CNL.EN.oobjectA(null, single(isPlural, isModal));
            }
        }
        public object Visit(CogniPy.CNL.DL.NumberValueRestriction e) 
        {
            object r = e.R.accept(this);
            object b = e.B.accept(this);
            Assert(r is TransNode);
            Assert(b is TransAbstractBound);
            return new TransNumberValueRestriction() { R = r as TransNode, B = b as TransAbstractBound, Kind = e.Kind, N = e.N };
        }


        public static void Assert(bool b)
        {
            if (!b)
            {
#if DEBUG
                System.Diagnostics.Debugger.Break();
#endif
                throw new InvalidOperationException("Conversion Assertion Failed.");
            }
        }

        //////////////// SWRL DL //////////////////////////////////////////////////////////////

        Dictionary<string, string> mapped_dvars = new Dictionary<string, string>();
        Dictionary<string, string> mapped_ivars = new Dictionary<string, string>();
               
        VisitingParam<bool> inResult = new VisitingParam<bool>(false);

        public object Visit(SwrlStatement e)
        {
            mapped_dvars.Clear();
            mapped_ivars.Clear();
            var2class.Clear();
            definedVars.Clear();
            class2var.Clear();
            remappedIdx.Clear();
            var2dataProp.Clear();
            var2dataRange.Clear();
            allVars.Clear();
            identifiedVars.Clear();

            {
                //checking
                CNL.EN.clause slp = e.slp.accept(this) as CNL.EN.clause;

                CNL.EN.clause_result slc;
                using (inResult.set(true))
                    slc = e.slc.accept(this) as CNL.EN.clause_result;
            }

            {
                using (checkinSimplifier.set(false))
                {
                    //solving
                    CNL.EN.clause slp;
                    slp = e.slp.accept(this) as CNL.EN.clause;
                    foreach (var v in var2class.Keys)
                    {
                        if(!definedVars.Contains(v))
                            slp.Conditions.Add(new condition_exists(null, new objectr_nio(null, new notidentobject(null, FromDL(var2class[v], false)))));
                    }
                    CNL.EN.clause_result slc;
                    using (inResult.set(true))
                        slc = e.slc.accept(this) as CNL.EN.clause_result;

                    return new CNL.EN.swrlrule(null)
                    {
                        Predicate = slp,
                        Result = slc,
                        modality = resolveModality(e.modality)
                    };
                }
            }
        }

        abstract class TransSwrlItemAtom
        {
            public abstract condition condition();
            public abstract condition_result condition_result();
        }

        VisitingParam<bool> checkinSimplifier = new VisitingParam<bool>(true);

        public object Visit(SwrlItemList e)
        {
            if (!inResult.get())
            {
                CNL.EN.clause mc = new CNL.EN.clause(null);
                mc.Conditions = new System.Collections.Generic.List<condition>();
                for (int i = 0; i < e.list.Count; i++)
                {
                    TransSwrlItemAtom el = e.list[i].accept(this) as TransSwrlItemAtom;
                    var cnd = el.condition();
                    if (cnd != null)
                        mc.Conditions.Add(cnd);
                }
                return mc;
            }
            else
            {
                CNL.EN.clause_result mc = new CNL.EN.clause_result(null);
                mc.Conditions = new System.Collections.Generic.List<condition_result>();
                for (int i = 0; i < e.list.Count; i++)
                {
                    TransSwrlItemAtom el = e.list[i].accept(this) as TransSwrlItemAtom;
                    var cnd = el.condition_result();
                    if (cnd != null)
                        mc.Conditions.Add(cnd);
                }
                return mc;
            }
        }

        Dictionary<string, string> var2class = new Dictionary<string, string>();
        Dictionary<string, HashSet<string>> class2var = new Dictionary<string, HashSet<string>>();
        Dictionary<string, Dictionary<string, string>> remappedIdx = new Dictionary<string, Dictionary<string, string>>();

        HashSet<string> definedVars = new HashSet<string>();

        class TransSwrlInstance : TransSwrlItemAtom
        {
            private Transform me;

            public TransNode C;
            public TransSwrlIObject I;
            
            public TransSwrlInstance(Transform me){this.me = me;}

            public override condition condition()
            {
                if (me.checkinSimplifier.get())
                {
                    if (C is TransAtomic && I is TransSwrlIVar)
                    {
                        var cls = (C as TransAtomic).id;
                        var ivar = (I as TransSwrlIVar).VAR;
                        if (!me.var2class.ContainsKey(ivar))
                            me.var2class.Add(ivar, cls);
                        if (!me.class2var.ContainsKey(cls))
                            me.class2var.Add(cls, new HashSet<string>());
                        me.class2var[cls].Add(ivar);
                    }
                    return new condition_definition(null) { objectClass = C.oobject(false, false), objectA = I.objectr() };
                }   
                else
                {
                    if (C is TransAtomic && I is TransSwrlIVar)
                    {
                        var cls = (C as TransAtomic).id;
                        var ivar = (I as TransSwrlIVar).VAR;
                        if (me.var2class[ivar] == cls)
                            return null;
                    }
                    return new condition_definition(null) { objectClass = C.oobject(false, false), objectA = I.objectr() };
                }
            }

            public override condition_result condition_result()
            {
                return new condition_result_definition(null) { objectClass = C.oobject(false, false), objectA = I.identobject() };
            }
        }

        public object Visit(CogniPy.CNL.DL.SwrlInstance e)
        {
            return new TransSwrlInstance(this) { C = e.C.accept(this) as TransNode, I = e.I.accept(this) as TransSwrlIObject };
        }

        class TransSwrlRole : TransSwrlItemAtom
        {
            public string R;
            public TransSwrlIObject I, J;
            Transform _me;
            public TransSwrlRole(Transform me)
            {
                _me = me;
            }
            public override condition condition()
            {
                return new condition_role(null, I.objectr(), _me.FromDL(R, endict.WordKind.PastParticiple, false), J.objectr(), condition_kind.None);
            }

            public override condition_result condition_result()
            {
                return new condition_result_role(null, I.identobject(), _me.FromDL(R, endict.WordKind.PastParticiple, false), J.identobject(), condition_kind.None);
            }
        }

        public object Visit(CogniPy.CNL.DL.SwrlRole e)
        {
            return new TransSwrlRole(this) { I = e.I.accept(this) as TransSwrlIObject, R = e.R, J = e.J.accept(this) as TransSwrlIObject };
        }

        class TransSwrlSameOrDifferent : TransSwrlItemAtom
        {
            public TransSwrlIObject I, J;
            public bool TrueForSame;

            public override condition condition()
            {
                return new condition_is(null, I.objectr(), J.objectr(), TrueForSame?condition_kind.None:condition_kind.Not);
            }

            public override condition_result condition_result()
            {
                return new condition_result_is(null, I.identobject(), J.identobject(), TrueForSame ? condition_kind.None : condition_kind.Not);
            }
        }

        public object Visit(CogniPy.CNL.DL.SwrlSameAs e)
        {
            return new TransSwrlSameOrDifferent() { I = e.I.accept(this) as TransSwrlIObject, J = e.J.accept(this) as TransSwrlIObject, TrueForSame = true };
        }

        public object Visit(SwrlDifferentFrom e)
        {
            return new TransSwrlSameOrDifferent() { I = e.I.accept(this) as TransSwrlIObject, J = e.J.accept(this) as TransSwrlIObject, TrueForSame = false };
        }

        Dictionary<string, List<Tuple<string,TransSwrlIObject>>> var2dataProp = new Dictionary<string,List<Tuple<string,TransSwrlIObject>>>();
        Dictionary<string, List<TransAbstractBound>> var2dataRange = new Dictionary<string, List<TransAbstractBound>>();

        public bool mergablePropAndRange(string var)
        {
            return var2dataProp.ContainsKey(var) && var2dataProp[var].Count == 1
               && var2dataRange.ContainsKey(var) && var2dataRange[var].Count == 1;
        }

        public condition_data_property_bound mergePropAndRange(string var)
        {
            return new condition_data_property_bound(null,
                var2dataProp[var][0].Item2.objectr(), var2dataProp[var][0].Item1, var2dataRange[var][0].bound());
        }

        class TransSwrlDataProperty : TransSwrlItemAtom
        {
            Transform me;
            public TransSwrlDataProperty(Transform me) { this.me = me; }
            public string R;
            public TransSwrlIObject IO;
            public TransSwrlDObject DO;

            public override condition condition()
            {
                if (DO.isVar())
                {
                    if (me.checkinSimplifier.get())
                    {
                        var rR = me.FromDL(R, endict.WordKind.PastParticiple, false);
                        var r = new condition_data_property(null, IO.objectr(), rR, DO.getVar());
                        if (!me.var2dataProp.ContainsKey(DO.getVar()))
                            me.var2dataProp.Add(DO.getVar(), new List<Tuple<string, TransSwrlIObject>>());
                        me.var2dataProp[DO.getVar()].Add(Tuple.Create(rR, IO));
                        return r;
                    }
                    else
                    {
                        if (me.mergablePropAndRange(DO.getVar()))
                            return me.mergePropAndRange(DO.getVar());
                        else
                            return new condition_data_property(null, IO.objectr(), me.FromDL(R, endict.WordKind.PastParticiple, false), DO.getVar());
                    }
                }
                else
                    return new condition_data_property_bound(null, IO.objectr(), me.FromDL(R, endict.WordKind.PastParticiple, false), new boundFacets(null, new facetList(null, new facet(null, "=", DO.getVal()))));
            }

            public override condition_result condition_result()
            {
                return new condition_result_data_property(null, IO.identobject(), me.FromDL(R, endict.WordKind.PastParticiple, false), DO.datavaler());
            }
        }

        public object Visit(SwrlDataProperty e)
        {
            return new TransSwrlDataProperty(this) { IO = e.IO.accept(this) as TransSwrlIObject, R = e.R, DO = e.DO.accept(this) as TransSwrlDObject };
        }

        class TransSwrlDataRange : TransSwrlItemAtom
        {
            Transform me;
            public TransSwrlDataRange(Transform me) { this.me = me; }
            public TransAbstractBound B;
            public TransSwrlDObject DO;

            public override condition condition()
            {
                var r = new condition_data_bound(null, DO.datavaler(), B.bound());
                if (DO.isVar())
                {
                    if (me.checkinSimplifier.get())
                    {
                        if (!me.var2dataRange.ContainsKey(DO.getVar()))
                            me.var2dataRange.Add(DO.getVar(), new List<TransAbstractBound>());
                        me.var2dataRange[DO.getVar()].Add(B);
                    }
                    else
                    {
                        if (me.mergablePropAndRange(DO.getVar()))
                            return null;
                        else
                            return r;
                    }
                }
                return r;
            }

            public override condition_result condition_result()
            {
                throw new InvalidOperationException("No bound for such a case");
            }
        }

        public object Visit(SwrlDataRange e)
        {
            return new TransSwrlDataRange(this) { DO = e.DO.accept(this) as TransSwrlDObject, B = e.B.accept(this) as TransAbstractBound };
        }

        class TransSwrlBuiltIn : TransSwrlItemAtom
        {
            public List<ITransSwrlObject> Values;
            public string builtInName;

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

            builtin getBuiltIn()
            {
                var btag = KeyWords.Me.GetTag(mapCode(builtInName));
                if (btag == "CMP" || btag == "EQ")
                    return new builtin_cmp(null, Values[1].datavaler(), builtInName, Values[0].datavaler());
                else if (builtInName == "plus" || builtInName == "times" || builtInName == "followed-by")
                {
                    List<datavaler> lst = new List<datavaler>();
                    for (int i = 0; i < Values.Count - 1; i++)
                        lst.Add(Values[i].datavaler());
                    return new builtin_list(null, lst, builtInName, Values.Last().datavaler());
                }
                else if (btag == "TRANSLATEDREPLACED")
                {
                    return new builtin_trans(null, builtInName, Values[0].datavaler(), Values[1].datavaler(), Values[2].datavaler(), Values[3].datavaler());
                }
                else if (btag == "ANNOTATION")
                {
                    return new builtin_annot(null, Values[0].objectr(), Values[1].datavaler(), Values[2].datavaler(), Values[3].datavaler());
                }
                else if (builtInName == "from" || builtInName == "before" || builtInName == "after")
                {
                    return new builtin_substr(null, Values[0].datavaler(), builtInName, Values[1].datavaler(), Values[2].datavaler());
                }
                else if (builtInName == "duration" || builtInName == "datetime")
                {
                    var t = Values[1].getVal().getVal();
                    var lst = new List<datavaler>();
                    for (int i = 2; i < Values.Count; i++)
                        lst.Add(Values[i].datavaler());

                    if (builtInName == "duration")
                    {
                        duration dur = null;
                        if (t == "'M'")
                            dur = new duration_m(null, lst[0], lst[1], lst[2], lst[3], lst[4], lst[5]);
                        else
                            dur = new duration_w(null, lst[0], lst[1], lst[2], lst[3], lst[4], lst[5]);
                        return new builtin_duration(null, dur, Values[0].datavaler());
                    }
                    else
                    {
                        datetime dtm = null;
                        dtm = new datetime(null, lst[0], lst[1], lst[2], lst[3], lst[4], lst[5]);
                        return new builtin_datetime(null, dtm, Values[0].datavaler());
                    }
                }
                else if (builtInName == "execute")
                {
                    var name = Values[0].getVal().getVal();
                    var exevars = new List<iexevar>();
                    for (int i = 1; i < Values.Count - 1; i++)
                    {
                        var el = Values[i];
                        if ((el is TransSwrlDVar) || (el is TransSwrlDVal))
                            exevars.Add(el.datavaler());
                        else if ((el is TransSwrlIVar) || (el is TransSwrlIVal))
                            exevars.Add(el.identobject());
                        else
                            throw new InvalidOperationException();
                    }
                    return new builtin_exe(null, name, new exeargs(null) { exevars = exevars }, Values.Last().datavaler());
                }
                else if (Values.Count == 3)
                {
                    return new builtin_bin(null, Values[0].datavaler(), builtInName, Values[1].datavaler(), Values[2].datavaler());
                }
                else if (Values.Count == 2)
                {
                    if (KeyWords.Me.GetTag(builtInName) == "UNOP2")
                        return new builtin_unary_free(null, Values[1].datavaler(), builtInName, Values[0].datavaler());
                    else if (builtInName == "alpha-representation-of")
                        return new builtin_alpha(null, Values[0].objectr(), Values[1].datavaler());
                    else
                        return new builtin_unary_cmp(null, builtInName, Values[0].datavaler(), Values[1].datavaler());
                }


                throw new NotImplementedException();
            }

            public override condition condition()
            {
                return new condition_builtin(null, getBuiltIn());
            }

            public override condition_result condition_result()
            {
                return new condition_result_builtin(null, getBuiltIn());
            }
        }

        public object Visit(SwrlBuiltIn e)
        {
            return new TransSwrlBuiltIn() { Values = (from x in e.Values select (x.accept(this) as ITransSwrlObject)).ToList(), builtInName = e.builtInName };
        }

        interface ITransSwrlObject
        {
            identobject identobject();
            objectr objectr();
            datavaler datavaler();
            dataval getVal();
        }

        abstract class TransSwrlIObject : ITransSwrlObject
        {
            public abstract objectr objectr();
            public abstract identobject identobject();

            public datavaler datavaler()
            {
                throw new NotImplementedException();
            }

            public dataval getVal()
            {
                throw new NotImplementedException();
            }

        }

        abstract class TransSwrlDObject : ITransSwrlObject
        {
            public abstract datavaler datavaler();
            public abstract bool isVar();
            public abstract dataval getVal();
            public abstract string getVar();

            public objectr objectr()
            {
                throw new NotImplementedException();
            }

            public identobject identobject()
            {
                throw new NotImplementedException();
            }
        }

        class TransSwrlDVal : TransSwrlDObject
        {
            public TransValue Val;

            public override datavaler datavaler()
            {
                return new datavalval(null, Val.dataval());
            }

            public override bool isVar()
            {
                return false;
            }

            public override dataval getVal()
            {
                return Val.dataval();
            }

            public override string getVar()
            {
                throw new InvalidOperationException();
            }
        }

        public object Visit(SwrlDVal e)
        {
            return new TransSwrlDVal() { Val = e.Val.accept(this) as TransValue };
        }

        class TransSwrlDVar : TransSwrlDObject
        {
            public string VAR;

            public override datavaler datavaler()
            {
                return new datavalvar(null, VAR);
            }

            public override bool isVar()
            {
                return true;
            }

            public override dataval getVal()
            {
                throw new InvalidOperationException();
            }

            public override string getVar()
            {
                return VAR;
            }
        }

        class TransSwrlRoleAtom : TransSwrlIObject
        {
            public string name;

            public override identobject identobject()
            {
                throw new NotImplementedException();
            }

            public override objectr objectr()
            {
                throw new NotImplementedException();
            }

        }

        class TransSwrlIVal : TransSwrlIObject
        {
            Transform _me;
            public TransSwrlIVal(Transform me)
            {
                _me = me;
            }
            public string I;

            public override objectr objectr()
            {
                return new objectr_io(null, identobject());
            }

            public override identobject identobject()
            {
                if (I.StartsWith("_"))
                    return new identobject_inst(null, new instancer(null, _me.FromDL(I.Substring(1), true), false));
                else
                    return new identobject_inst(null, new instancer(null, _me.FromDL(I, true), true));
            }
        }

        public object Visit(SwrlIVal e)
        {
            return new TransSwrlIVal(this) { I = e.I };
        }

        HashSet<Tuple<string, string>> identifiedVars = new HashSet<Tuple<string, string>>();
        HashSet<string> allVars = new HashSet<string>();

        class TransSwrlIVar : TransSwrlIObject
        {
            private Transform _me;
            public TransSwrlIVar(Transform me) { _me = me; }

            public string VAR;

            public override objectr objectr()
            {
                if (_me.checkinSimplifier.get())
                {
                    _me.allVars.Add(VAR);
                    return new objectr_io(null, identobject());
                }
                else
                {
                    _me.definedVars.Add(VAR);
                    string varidx = null;
                    if (_me.var2class.ContainsKey(VAR))
                    {
                        if (!_me.preserveVarsNumbering.get())
                        {
                            if (!_me.remappedIdx.ContainsKey(_me.var2class[VAR]))
                                _me.remappedIdx.Add(_me.var2class[VAR], new Dictionary<string, string>());

                            if (!_me.remappedIdx[_me.var2class[VAR]].ContainsKey(VAR))
                                _me.remappedIdx[_me.var2class[VAR]].Add(VAR, (_me.remappedIdx[_me.var2class[VAR]].Count + 1).ToString());

                            bool addIdx = _me.class2var.ContainsKey(_me.var2class[VAR]) ? _me.class2var[_me.var2class[VAR]].Count > 1 : true;

                            varidx = addIdx ? _me.remappedIdx[_me.var2class[VAR]][VAR] : null;
                        }
                        else
                        {
                            var arr = VAR.Split('-'); varidx = arr[arr.Length - 1];
                            int iid; if (!int.TryParse(varidx, out iid)) varidx = null;
                        }
                        if (_me.identifiedVars.Add(Tuple.Create(_me.var2class[VAR], VAR)))
                            return new objectr_nio(null, new notidentobject(null, _me.FromDL(_me.var2class[VAR], false), varidx));
                        else
                            return new objectr_io(null, new identobject_name(null, _me.FromDL(_me.var2class[VAR], false), varidx));
                    }
                    else
                    {
                        if (!_me.preserveVarsNumbering.get())
                        {
                            if (!_me.remappedIdx.ContainsKey(""))
                                _me.remappedIdx.Add("", new Dictionary<string, string>());

                            if (!_me.remappedIdx[""].ContainsKey(VAR))
                                _me.remappedIdx[""].Add(VAR, (_me.remappedIdx[""].Count + 1).ToString());

                            bool addIdx = _me.allVars.Count != _me.class2var.Keys.Count + 1;
                            varidx = addIdx ? _me.remappedIdx[""][VAR] : null;
                        }
                        else
                        {
                            var arr = VAR.Split('-'); varidx = arr[arr.Length - 1];
                            int iid; if (!int.TryParse(varidx, out iid)) varidx = null;
                        }
                        if (_me.identifiedVars.Add(Tuple.Create("", VAR)))
                            return new objectr_nio(null, new notidentobject(null, null, varidx));
                        else
                            return new objectr_io(null, new identobject_name(null, null, varidx));
                    }
                }
            }

            public override identobject identobject()
            {
                if (_me.checkinSimplifier.get())
                {
                    _me.allVars.Add(VAR);
                    return new identobject_name(null, null, VAR);
                }
                else
                {
                    _me.definedVars.Add(VAR);

                    string varidx = null;
                    if (_me.var2class.ContainsKey(VAR))
                    {
                        if (!_me.preserveVarsNumbering.get())
                        {
                            if (!_me.remappedIdx.ContainsKey(_me.var2class[VAR]))
                                _me.remappedIdx.Add(_me.var2class[VAR], new Dictionary<string, string>());

                            if (!_me.remappedIdx[_me.var2class[VAR]].ContainsKey(VAR))
                                _me.remappedIdx[_me.var2class[VAR]].Add(VAR, (_me.remappedIdx[_me.var2class[VAR]].Count + 1).ToString());

                            bool addIdx = _me.class2var.ContainsKey(_me.var2class[VAR]) ? _me.class2var[_me.var2class[VAR]].Count > 1 : true;
                            varidx = addIdx ? _me.remappedIdx[_me.var2class[VAR]][VAR] : null;
                        }
                        else
                        {
                            var arr = VAR.Split('-'); varidx = arr[arr.Length - 1];
                            int iid; if (!int.TryParse(varidx, out iid)) varidx = null;
                        }
                        return new identobject_name(null, _me.FromDL(_me.var2class[VAR], false), varidx);
                    }
                    else
                    {
                        if (!_me.preserveVarsNumbering.get())
                        {
                            if (!_me.remappedIdx.ContainsKey(""))
                                _me.remappedIdx.Add("", new Dictionary<string, string>());

                            if (!_me.remappedIdx[""].ContainsKey(VAR))
                                _me.remappedIdx[""].Add(VAR, (_me.remappedIdx[""].Count + 1).ToString());

                            bool addIdx = _me.allVars.Count != _me.class2var.Keys.Count + 1;
                            varidx = addIdx ? _me.remappedIdx[""][VAR] : null;
                        }
                        else
                        {
                            var arr = VAR.Split('-'); varidx = arr[arr.Length - 1];
                            int iid; if (!int.TryParse(varidx, out iid)) varidx = null;
                        }

                        return new identobject_name(null, null, varidx);
                    }
                }
            }
        }

        VisitingParam<bool> preserveVarsNumbering = new VisitingParam<bool>(false);

        public object Visit(SwrlIVar e)
        {
            if(preserveVarsNumbering.get())
            {
                if (!mapped_ivars.ContainsKey(e.VAR))
                    mapped_ivars.Add(e.VAR, e.VAR);
            }
            else
            {
                if (!mapped_ivars.ContainsKey(e.VAR))
                    mapped_ivars.Add(e.VAR, (mapped_ivars.Count+1).ToString());
            }
            return new TransSwrlIVar(this) { VAR = mapped_ivars[e.VAR] };
        }
        
        public object Visit(SwrlDVar e)
        {
            if(preserveVarsNumbering.get())
            {
                var arr = e.VAR.Split('-'); 
                var varidx = arr[arr.Length - 1];
                
                if (!mapped_dvars.ContainsKey(e.VAR))
                    mapped_dvars.Add(e.VAR, varidx);
            }
            else
            {
                if (!mapped_dvars.ContainsKey(e.VAR))
                    mapped_dvars.Add(e.VAR, (mapped_dvars.Count + 1).ToString());
            }
            return new TransSwrlDVar() { VAR = mapped_dvars[e.VAR] };
        }

        /////////// SWRL DL ///////////////////////////////////////


        public object Visit(SwrlIterate e)
        {
            mapped_dvars.Clear();
            mapped_ivars.Clear();
            var2class.Clear();
            class2var.Clear();
            remappedIdx.Clear();
            definedVars.Clear();
            var2dataProp.Clear();
            var2dataRange.Clear();
            allVars.Clear();
            identifiedVars.Clear();

            using (preserveVarsNumbering.set(true))
            {

                {
                    //checking
                    CNL.EN.clause slp = e.slp.accept(this) as CNL.EN.clause;

                    CNL.EN.clause_result slc;
                    using (inResult.set(true))
                        slc = e.slc.accept(this) as CNL.EN.clause_result;
                }

                {
                    using (checkinSimplifier.set(false))
                    {
                        //solving
                        CNL.EN.clause slp;
                        slp = e.slp.accept(this) as CNL.EN.clause;
                        foreach (var v in var2class.Keys)
                        {
                            if (!definedVars.Contains(v))
                                slp.Conditions.Add(new condition_exists(null, new objectr_nio(null, new notidentobject(null, FromDL(var2class[v], false)))));
                        }

                        CNL.EN.clause_result slc;
                        CNL.EN.exeargs args;
                        using (inResult.set(true))
                        {
                            args = e.vars.accept(this) as CNL.EN.exeargs;
                            slc = e.slc.accept(this) as CNL.EN.clause_result;
                        }
                        return new CNL.EN.swrlrulefor(null)
                        {
                            Predicate = slp,
                            Collection = args,
                            Result = slc,
                        };
                    }
                }
            }
        }


        /////////// EXE DL ///////////////////////////////////////

        public object Visit(ExeStatement e)
        {
            mapped_dvars.Clear();
            mapped_ivars.Clear();
            var2class.Clear();
            class2var.Clear();
            remappedIdx.Clear();
            definedVars.Clear();
            var2dataProp.Clear();
            var2dataRange.Clear();
            allVars.Clear();
            identifiedVars.Clear();

            using(preserveVarsNumbering.set(true))
            {

                {
                    //checking
                    CNL.EN.clause slp = e.slp.accept(this) as CNL.EN.clause;
                }

                {
                    using (checkinSimplifier.set(false))
                    {
                        //solving
                        CNL.EN.clause slp;
                        slp = e.slp.accept(this) as CNL.EN.clause;
                        foreach (var v in var2class.Keys)
                        {
                            if (!definedVars.Contains(v))
                                slp.Conditions.Add(new condition_exists(null, new objectr_nio(null, new notidentobject(null, FromDL(var2class[v], false)))));
                        }
                        CNL.EN.exeargs args; 
                        using (inResult.set(true))
                            args = e.args.accept(this) as CNL.EN.exeargs;

                        return new CNL.EN.exerule(null)
                        {
                            slp = slp,
                            args = args,
                            exe = e.exe
                        };
                    }
                }
            }
        }

        public object Visit(SwrlVarList e)
        {
            var exeargs = new CNL.EN.exeargs(null) { exevars = new List<iexevar>() };
            foreach (var el in e.list)
            {
                if (el is SwrlIVar)
                {
                    var x = (el as SwrlIVar).accept(this) as TransSwrlIVar;
                    exeargs.exevars.Add(x.identobject());
                }
                else
                {
                    var x = (el as SwrlDVar).accept(this) as TransSwrlDVar;
                    exeargs.exevars.Add(x.datavaler());
                }
            }
            return exeargs;
        }

        /////////// EXE DL ///////////////////////////////////////



        public object Visit(CodeStatement e)
        {
            return new CNL.EN.code(null)
            {
                exe = e.exe
            };
        }
    }  
}
