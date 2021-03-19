using System;
using System.Collections.Generic;
using System.Text;
using Ontorion.CNL.DL;

namespace Ontorion.CNL.PL
{
    public class Transform : Ontorion.CNL.DL.IVisitor
    {
        public Transform() {}

        public Ontorion.CNL.PL.paragraph Convert(Ontorion.CNL.DL.Paragraph p)
        {
            return p.accept(this) as Ontorion.CNL.PL.paragraph;
        }
        public Ontorion.CNL.PL.sentence Convert(Ontorion.CNL.DL.Statement s)
        {
            return s.accept(this) as Ontorion.CNL.PL.sentence;
        }

        public object Visit(Ontorion.CNL.DL.Paragraph p) 
        {
            Ontorion.CNL.PL.paragraph ret = new CNL.PL.paragraph(null) { sentences = new List<CNL.PL.sentence>() };
            foreach (var x in p.Statements)
            {
                ret.sentences.Add(x.accept(this) as CNL.PL.sentence);
            }
            return ret;
        }

        interface TransNode
        {
            CNL.PL.subject subject();
            CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal);
            CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal);
            CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination);
            CNL.PL.oobject oobject(bool isPlural, bool isModal, endict.Declination declination);
            CNL.PL.role role(bool isModal, bool isInverse, bool xyMode);
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

        public object Visit(Ontorion.CNL.DL.Subsumption p) 
        {
            var LeftSide = p.C.accept(this);
            var RightSide = p.D.accept(this);
            Assert(LeftSide is TransNode);
            Assert(RightSide is TransNode);
            return new CNL.PL.subsumption(null, (LeftSide as TransNode).subject()
                , resolveModality(p.modality)
                , (RightSide as TransNode).objectRoleExpr(false, p.modality != Statement.Modality.IS));
        }

        public object Visit(Ontorion.CNL.DL.Equivalence p)
        {
            if (p.Equivalents.Count <= 2)
            {
                var LeftSide = p.Equivalents[0].accept(this);
                var RightSide = p.Equivalents[1].accept(this);
                Assert(LeftSide is TransNode);
                Assert(RightSide is TransNode);
                return new CNL.PL.equivalence_def(null, (LeftSide as TransNode).subject()
                                , resolveModality(p.modality)
                                , (RightSide as TransNode).defObjectRoleExpr(false, p.modality != Statement.Modality.IS));
            }
            else
            {
                var ret = new CNL.PL.equivalence(null) { equals = new List<subject>() };
                foreach (var e in p.Equivalents)
                {
                    var d = e.accept(this);
                    Assert(d is TransNode);

                    ret.equals.Add((d as TransNode).subject());
                }
                return ret;
            }
        }

        public object Visit(Ontorion.CNL.DL.Disjoint p)
        {
            var ret = new CNL.PL.disjoint(null) { different = new List<subject>() };
            foreach (var e in p.Disjoints)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.different.Add((d as TransNode).subject());
            }
            return ret;
        }

        static string FromDL(string name, bool bigName)
        {
            return ENNameingConvention.FromDL( new DlName() { id = name }, bigName).id;
        }
        static string FromDL(string name, endict.IPlWord kind, bool bigName)
        {
            return ENNameingConvention.FromDL(new DlName() { id = name }, kind, bigName).id;
        }

        public object Visit(Ontorion.CNL.DL.DisjointUnion p)
        {
            var ret = new CNL.PL.disjointunion(null) { union = new List<subject>() };
            ret.name = FromDL(p.name, false); 
            foreach (var e in p.Union)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.union.Add((d as TransNode).subject());
            }
            return ret;
        }

        public object Visit(Ontorion.CNL.DL.RoleInclusion e)
        {
            object r = e.C.accept(this);
            object s = e.D.accept(this);
            Assert(r is TransNode);
            Assert(s is TransNode);
            return new CNL.PL.rolesubsumption(null, new CNL.PL.chain(null, (r as TransNode).role(false, false, false)),
                              resolveModality(e.modality),
                              (s as TransNode).role(e.modality != Statement.Modality.IS, false, true));
        }

        public object Visit(Ontorion.CNL.DL.RoleEquivalence p)
        {
            var ret = new CNL.PL.roleequivalence(null) { equals = new List<roleWithXY>() };
            foreach (var e in p.Equivalents)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.equals.Add(new roleWithXY(null) { inverse = false, r = (d as TransNode).role(false, false, true) });
            }
            return ret;
        }

        public object Visit(Ontorion.CNL.DL.RoleDisjoint p)
        {
            var ret = new CNL.PL.roledisjoint(null) { different = new List<roleWithXY>() };
            foreach (var e in p.Disjoints)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.different.Add(new roleWithXY(null) { inverse = false, r = (d as TransNode).role(false, false, true) });
            }
            return ret;
        }
        
        public object Visit(Ontorion.CNL.DL.ComplexRoleInclusion e)
        {
            CNL.PL.chain z = new CNL.PL.chain(null) { roles = new List<CNL.PL.role>() };
            foreach (var r in e.RoleChain)
            {
                z.roles.Add((r.accept(this) as TransNode).role(false, false, false));
            }
            object t = e.R.accept(this);
            Assert(t is TransNode);
            return new CNL.PL.rolesubsumption(null, z,
                              resolveModality(e.modality),
                              (t as TransNode).role(false, false, true));
        }

        public object Visit(Ontorion.CNL.DL.DataRoleInclusion e)
        {
            object r = e.C.accept(this);
            object s = e.D.accept(this);
            Assert(r is TransNode);
            Assert(s is TransNode);
            return new CNL.PL.datarolesubsumption(null, (r as TransNode).role(false, false, false),
                              resolveModality(e.modality),
                              (s as TransNode).role(e.modality != Statement.Modality.IS, false, false));
        }

        public object Visit(Ontorion.CNL.DL.DataRoleEquivalence p)
        {
            var ret = new CNL.PL.dataroleequivalence(null) { equals = new List<role>() };
            foreach (var e in p.Equivalents)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.equals.Add((d as TransNode).role(false, false, false));
            }
            return ret;
        }

        public object Visit(Ontorion.CNL.DL.DataRoleDisjoint p)
        {
            var ret = new CNL.PL.dataroledisjoint(null) { different = new List<role>() };
            foreach (var e in p.Disjoints)
            {
                var d = e.accept(this);
                Assert(d is TransNode);

                ret.different.Add((d as TransNode).role(false, false, false));
            }
            return ret;
        }

        public object Visit(Ontorion.CNL.DL.InstanceOf e)
        {
            TransNamedInstance i = new TransNamedInstance() { id = e.I };
            object c = e.C.accept(this);
            var ii = new TransInstanceSingle() { Instance = i};

            return new CNL.PL.subsumption(null, ii.subject()
                , resolveModality(e.modality)
                , (c as TransNode).objectRoleExpr(false, e.modality != Statement.Modality.IS));
        }

        public object Visit(Ontorion.CNL.DL.RelatedInstances p)
        {
            var i = new TransInstanceSingle() { Instance = new TransNamedInstance() { id = p.I } };
            var j = new TransInstanceSingle() { Instance = new TransNamedInstance() { id = p.J } };
            var Role = p.R.accept(this);
            Assert(Role is TransNode);
            return new CNL.PL.subsumption(null, i.subject()
                , resolveModality(p.modality)
                , j.objectRoleExpr(false, p.modality != Statement.Modality.IS));
        }

        public object Visit(Ontorion.CNL.DL.InstanceValue e)
        {
            Assert(false);
            return null;
        }

        public object Visit(Ontorion.CNL.DL.SameInstances p)
        {
            var ret = new CNL.PL.equivalence(null) { equals = new List<subject>() };
            ret.modality = resolveModality(p.modality);
            foreach (var e in p.Instances)
            {
                Assert(e is NamedInstance);

                var ii = new TransInstanceSingle() { Instance = new TransNamedInstance() { id = (e as NamedInstance).name } };

                ret.equals.Add(ii.subject());
            }
            return ret;
        }

        public object Visit(Ontorion.CNL.DL.DifferentInstances p)
        {
            var ret = new CNL.PL.disjoint(null) { different = new List<subject>() };
            ret.modality=resolveModality(p.modality);
            foreach (var e in p.Instances)
            {
                Assert(e is NamedInstance);

                var ii = new TransInstanceSingle() { Instance = new TransNamedInstance() { id = (e as NamedInstance).name } };

                ret.different.Add(ii.subject());
            }
            return ret;
        }

        public object Visit(Ontorion.CNL.DL.HasKey p)
        {
            var ret = new CNL.PL.haskey(null) { dataroles = new List<role>(), roles = new List<role>() };
            var x = p.C.accept(this);
            Assert(x is TransNode);
            ret.s = (x as TransNode).objectRoleExpr(false, false);
            foreach (var e in p.Roles)
            {
                var d = e.accept(this);
                Assert(d is TransNode);
                ret.roles.Add((d as TransNode).role(false, false, true));
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
            CNL.PL.instance instance(bool isPlural, bool isModal, endict.Declination declination);
        }

        class TransNamedInstance : TransInstance
        {
            public string id;
            public CNL.PL.instance instance(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.instanceBigName(null, FromDL(id, getDefaultNoun(isPlural, declination),true));
            }
        }

        public object Visit(Ontorion.CNL.DL.NamedInstance e)
        {
            return new TransNamedInstance() {  id = e.name };
        }

        class TransUnnamedInstance : TransInstance
        {
            public TransNode C;
            public CNL.PL.instance instance(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.instanceThe(null,false, C.single(isPlural, isModal, declination));
            }
        }

        class TransUnnamedOnlyInstance : TransInstance
        {
            public TransNode C;
            public CNL.PL.instance instance(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.instanceThe(null, true, C.single(isPlural, isModal, declination));
            }
        }

        public object Visit(Ontorion.CNL.DL.UnnamedInstance e)
        {
            object o =  e.C.accept(this);
            Assert ( o  is TransNode);
            if (e.Only)
                return new TransUnnamedOnlyInstance() { C = o as TransNode };
            else
                return new TransUnnamedInstance() { C = o as TransNode };
        }

        interface TransValue
        {
            CNL.PL.bound bound(string Kind);
        }

        class TransNumber:TransValue
        {
            public string val;
            public CNL.PL.bound bound(string Kind)
            {
                return new CNL.PL.bound(null,Kind,new CNL.PL.Number(null,val));
            }
        }
        public object Visit(Ontorion.CNL.DL.Number e)
        {
            return new TransNumber() { val = e.val };
        }

        class TransBool : TransValue
        {
            public string val;
            public CNL.PL.bound bound(string Kind)
            {
                return new CNL.PL.bound(null, Kind, new CNL.PL.Bool(null, val));
            }
        }
        public object Visit(Ontorion.CNL.DL.Bool e)
        {
            return new TransBool() { val = e.val == "[1]" ? "true" : "false" };
        }

        class TransString : TransValue
        {
            public string val;
            public CNL.PL.bound bound(string Kind)
            {
                return new CNL.PL.bound(null,Kind,new CNL.PL.StrData(null,val));
            }
        }
        public object Visit(Ontorion.CNL.DL.String e)
        {
            return new TransString() { val = e.val };
        }

        class TransFloat:TransValue
        {
            public string val;
            public CNL.PL.bound bound(string Kind)
            {
                return new CNL.PL.bound(null,Kind,new CNL.PL.Float(null,val));
            }
        }
        public object Visit(Ontorion.CNL.DL.Float e)
        {
            return new TransFloat() { val = e.val };
        }

        interface TransAbstractBound
        {
            CNL.PL.abstractbound bound();
        }

        class TransBound : TransAbstractBound
        {
            public string Kind;
            public TransValue V;
            public CNL.PL.abstractbound bound()
            {
                return V.bound(Kind);
            }
        }

        class TransDataSetBound : TransAbstractBound
        {
            public List<Value> Values;
            public CNL.PL.abstractbound bound()
            {
                var eset = new boundOneOf(null) { vals = new List<dataval>() };
                foreach (var v in Values)
                {
                    if (v is Ontorion.CNL.DL.String)
                        eset.vals.Add(new StrData(null) { val = v.getVal() });
                    else if (v is Ontorion.CNL.DL.Float)
                        eset.vals.Add(new Ontorion.CNL.PL.Float(null) { val = v.getVal() });
                    else if (v is Ontorion.CNL.DL.Number)
                        eset.vals.Add(new Ontorion.CNL.PL.Number(null) { val = v.getVal() });
                    else if (v is Ontorion.CNL.DL.Bool)
                        eset.vals.Add(new Ontorion.CNL.PL.Bool(null) { val = v.getVal() });
                    else
                        Assert(false);
                }
                return eset;
            }
        }

        class TransTotalBound : TransAbstractBound
        {
            public Value V;
            public CNL.PL.abstractbound bound()
            {
                if (V is CNL.DL.Float)
                    return new CNL.PL.boundTotal(null, "DBL");
                else if (V is CNL.DL.Number)
                    return new CNL.PL.boundTotal(null, "NUM");
                else if (V is CNL.DL.Bool)
                    return new CNL.PL.boundTotal(null, "BOL");
                else if (V is CNL.DL.String)
                    return new CNL.PL.boundTotal(null, "STR");
                else
                {
                    Assert(false);
                    return null;
                }
            }
        }

        class TransTopBound : TransAbstractBound
        {
            public CNL.PL.abstractbound bound()
            {
                return new CNL.PL.boundTop(null);
            }
        }
        
        public object Visit(Ontorion.CNL.DL.Bound e)
        {
            return new TransBound() { Kind = e.Kind, V = e.V.accept(this) as TransValue };
        }

        public object Visit(Ontorion.CNL.DL.TotalBound e)
        {
            return new TransTotalBound() { V = e.V };
        }
        public object Visit(Ontorion.CNL.DL.TopBound e)
        {
            return new TransTopBound();
        }

        class TransAtomic : TransNode
        {
            public string id;
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEvery(null, new CNL.PL.singleName(null, FromDL(id, false)));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.objectRoleExpr1(null,false,new CNL.PL.oobjectA(null, new CNL.PL.singleName(null,FromDL(id,getDefaultNoun(isPlural,new endict.Instrumental()),true))),false); 
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.defObjectRoleExpr1(null, false, new CNL.PL.oobjectA(null, new CNL.PL.singleName(null, FromDL(id, getDefaultNoun(isPlural, new endict.Nominative()), false))));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleName(null, FromDL(id, getDefaultNoun(isPlural, declination), false));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null,single(isPlural,isModal,declination));
            }
            public CNL.PL.role role(bool isModal,bool isInverse,bool xyMode)
            {
                if (isInverse)
                {
                    if (xyMode)
                    {
                        if (isModal)
                            return new CNL.PL.role(null, FromDL(id,false), true, false);
                        else
                            return new CNL.PL.role(null, FromDL(id, getDefaultVerbForGivenConjugation(new endict.ThirdPerson()), false), true, false);//PastParticiple
                    }
                    else
                        return new CNL.PL.role(null, FromDL(id, getDefaultVerbForGivenConjugation(new endict.Passive()), false), true, false);//SimplePast
                }
                else
                {
                    if (isModal)
                        return new CNL.PL.role(null, FromDL(id, false), false, false);
                    else
                        return new CNL.PL.role(null, FromDL(id, getDefaultVerbForGivenConjugation(new endict.ThirdPerson()), false), false, false);//PastParticiple
                }
            }
        }
        public object Visit(Ontorion.CNL.DL.Atomic e)
        {
            return new TransAtomic() { id = e.id };
        }

        class TransTop : TransNode
        {
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null);
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.objectRoleExpr1(null, false, new CNL.PL.oobjectSomething(null));
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.defObjectRoleExpr1(null, false, new CNL.PL.oobjectSomething(null));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThing(null);
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectSomething(null);
            }
            public CNL.PL.role role(bool isModal,bool isInverse,bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.Top e)
        {
            return new TransTop();
        }

        class TransBottom : TransNode
        {
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectNothing(null);
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.objectRoleExpr1(null, false, new CNL.PL.oobjectNothing(null));
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.defObjectRoleExpr1(null, false, new CNL.PL.oobjectNothing(null));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, new CNL.PL.objectRoleExpr1(null, false, new CNL.PL.oobjectNothing(null))))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectNothing(null);
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.Bottom e)
        {
            return new TransBottom();
        }

        class TransRoleInversion : TransNode
        {
            public TransNode R;
            public CNL.PL.subject subject()
            {
                Assert(false);
                return null;
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                Assert(false);
                return null;
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                return R.role(isModal, !isInverse, xyMode);
            }
        }
        public object Visit(Ontorion.CNL.DL.RoleInversion e)
        {
            object o = e.R.accept(this);
            Assert(o is TransNode);
            return new TransRoleInversion() { R = o as TransNode };
        }

        class TransInstanceSingle : TransNode
        {
            public TransInstance Instance;
            public CNL.PL.subject subject()
            {
                object o = Instance.instance(false, false, new endict.Nominative());
                if (o is CNL.PL.instanceThe)
                    return new CNL.PL.subjectThe(null, (o as CNL.PL.instanceThe).only, (o as CNL.PL.instanceThe).s);
                else if (o is CNL.PL.instanceBigName)
                    return new CNL.PL.subjectBigName(null, (o as CNL.PL.instanceBigName).name);
                Assert(false);
                return null;
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                var o = Instance.instance(isPlural, isModal, new endict.Nominative());
                if (o != null)
                    return new CNL.PL.objectRoleExpr1(null, false, new CNL.PL.oobjectInstance(null, o));
                Assert(false);
                return null;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                var o = Instance.instance(isPlural, isModal, new endict.Nominative());
                if (o != null)
                    return new CNL.PL.defObjectRoleExpr1(null, false, new CNL.PL.oobjectInstance(null, o));
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, new CNL.PL.objectRoleExpr1(null, false, oobject(isPlural, isModal, new endict.Instrumental()))))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                var o = Instance.instance(isPlural, isModal, new endict.Nominative());//TODO uwaga
                if (o != null)
                    return new CNL.PL.oobjectInstance(null, o);
                Assert(false);
                return null;
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        class TransInstanceSet : TransNode
        {
            public List<TransInstance> Instances = new List<TransInstance>();
            public CNL.PL.subject subject()
            {
                Assert(Instances.Count > 1);

                List<CNL.PL.instance> insts = new List<CNL.PL.instance>();
                
                foreach (var i in Instances)
                    insts.Add(i.instance(false, false, new endict.Nominative()));

                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOneOf(null, new CNL.PL.instanceList(null) { insts = insts }));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(Instances.Count > 1);

                List<CNL.PL.instance> insts = new List<CNL.PL.instance>();

                foreach (var i in Instances)
                    insts.Add(i.instance(false, false, new endict.Nominative()));

                return new CNL.PL.objectRoleExpr1(null, false,
                    new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOneOf(null, new CNL.PL.instanceList(null) { insts = insts })),
                    true);
                
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(Instances.Count > 1);

                List<CNL.PL.instance> insts = new List<CNL.PL.instance>();

                foreach (var i in Instances)
                    insts.Add(i.instance(false, false, new endict.Nominative()));

                return new CNL.PL.defObjectRoleExpr1(null, false,
                    new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOneOf(null, new CNL.PL.instanceList(null) { insts = insts })));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                Assert(Instances.Count > 1);

                List<CNL.PL.instance> insts = new List<CNL.PL.instance>();

                foreach (var i in Instances)
                    insts.Add(i.instance(false, false, new endict.Nominative()));//TODO uwaga

                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOneOf(null, new CNL.PL.instanceList(null) { insts = insts }));

            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.InstanceSet e)
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

        public object Visit(Ontorion.CNL.DL.ValueSet e)
        {
            if (e.Values.Count == 1)
                return new TransBound() { Kind = "=", V = e.Values[0].accept(this) as TransValue };
            else
            {
                TransDataSetBound ret = new TransDataSetBound();
                ret.Values = e.Values;
                return ret;
            }
        }

        class TransConceptOr : TransNode
        {
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

            CNL.PL.orloop makeorloop(bool isPlural,bool isModal,TransAtomic atom)
            {
                CNL.PL.orloop orloop = new CNL.PL.orloop(null) { exprs = new List<CNL.PL.andloop>() };
                foreach (var C in Exprs)
                {
                    if (C != atom)
                    {
                        CNL.PL.andloop andloop = new CNL.PL.andloop(null) { exprs = new List<CNL.PL.objectRoleExpr>() };
                        andloop.exprs.Add(C.objectRoleExpr(isPlural, isModal));
                        orloop.exprs.Add(andloop);
                    }
                }
                return orloop;
            }

            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, makeorloop(false,false,null)));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.objectRoleExpr1(null, false, new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, null))),true);
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.defObjectRoleExpr1(null, false, new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, null))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                if (Exprs.Count == 1)
                {
                    var atom = findFirstAtom();
                    if (atom == null)
                        return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
                    else
                        return new CNL.PL.singleNameThat(null, FromDL(atom.id, false), new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, atom)));
                }
                else
                {
                    return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
                }
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.ConceptOr e)
        {
            TransConceptOr ret = new TransConceptOr();
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
            CNL.PL.orloop makeorloop(bool isPlural,bool isModal,TransAtomic atom)
            {
                CNL.PL.andloop andloop = new CNL.PL.andloop(null) { exprs = new List<CNL.PL.objectRoleExpr>() };
                foreach (var C in Exprs)
                {
                    if(C!=atom)
                        andloop.exprs.Add(C.objectRoleExpr(isPlural, isModal));
                }
                return new CNL.PL.orloop(null,andloop);
            }

            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, makeorloop(false,false,null)));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.objectRoleExpr1(null, false, new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal,null))));
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                return new CNL.PL.defObjectRoleExpr1(null, false, new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, null))));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                var atom = findFirstAtom();
                if (atom == null)
                    return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
                else
                    return new CNL.PL.singleNameThat(null, FromDL(atom.id, false), new CNL.PL.thatOrLoop(null, makeorloop(isPlural, isModal, atom)));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.ConceptAnd e)
        {
            TransConceptAnd ret = new TransConceptAnd();
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
            public TransNode C;
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectNo(null, C.single(false,false, new endict.Nominative()));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                if (C is TransInstanceSingle)
                {
                    var ic = C as TransInstanceSingle;
                    if(ic.Instance is TransNamedInstance)
                        return new CNL.PL.objectRoleExpr1(null, true, new CNL.PL.oobjectInstance(null, new instanceBigName(null, FromDL((ic.Instance as TransNamedInstance).id,true))));
                }
                else if (C is TransSomeRestriction)
                {
                    var ic2 = C as TransSomeRestriction;
                    if (ic2.C is TransInstanceSingle)
                    {
                        var ic = ic2.C as TransInstanceSingle;
                        if (ic.Instance is TransNamedInstance)
                            return new CNL.PL.objectRoleExpr2(null, true, new CNL.PL.oobjectInstance(null, new instanceBigName(null, FromDL((ic.Instance as TransNamedInstance).id, true))), ic2.R.role(false, false, false));
                    }
                }
                return new CNL.PL.objectRoleExpr1(null, true, new CNL.PL.oobjectA(null, C.single(isPlural, isModal, new endict.Nominative())), true);
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                if (C is TransInstanceSingle)
                {
                    var ic = C as TransInstanceSingle;
                    if (ic.Instance is TransNamedInstance)
                        return new CNL.PL.defObjectRoleExpr1(null, true, new CNL.PL.oobjectInstance(null, new instanceBigName(null, FromDL((ic.Instance as TransNamedInstance).id, true))));
                }
                else if (C is TransSomeRestriction)
                {
                    Assert(false);
                }
                return new CNL.PL.defObjectRoleExpr1(null, true, new CNL.PL.oobjectA(null, C.single(isPlural, isModal, new endict.Nominative())));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, new CNL.PL.objectRoleExpr1(null, true, new CNL.PL.oobjectA(null, C.single(isPlural,isModal, declination)))))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.ConceptNot e)
        {
            object o = e.C.accept(this);
            Assert(o is TransNode);
            return new TransConceptNot() { C = o as TransNode };
        }

        class TransOnlyRestriction : TransNode
        {
            public TransNode R;
            public TransNode C;
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false,false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, new CNL.PL.oobjectOnly(null, C.single(false, false, new endict.Accusative())), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.OnlyRestriction e)
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
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false, false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, C.oobject(false,false,new endict.Accusative()), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public oobject oobject(bool isPlural, bool isModal , endict.Declination declination)
            {
                return new CNL.PL.oobjectSomethingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural,isModal)))));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.SomeRestriction e)
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
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false, false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, new CNL.PL.oobjectOnlyBnd(null, B.bound() ), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.OnlyValueRestriction e)
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
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false, false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, new CNL.PL.oobjectBnd(null, B.bound()), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.SomeValueRestriction e)
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
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false, false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, new CNL.PL.oobjectSelf(null), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.SelfReference e)
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
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false, false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, new CNL.PL.oobjectCmp(null, Kind, N, C.single(long.Parse(N) != 1, false, new endict.Nominative())), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.NumberRestriction e)
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
            public CNL.PL.subject subject()
            {
                return new CNL.PL.subjectEverything(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(false, false)))));
            }
            public CNL.PL.objectRoleExpr objectRoleExpr(bool isPlural, bool isModal)
            {
                CNL.PL.objectRoleExpr ore = null;
                var RN = R.role(isModal, false, false);
                ore = new CNL.PL.objectRoleExpr2(null, false, new CNL.PL.oobjectCmpBnd(null, Kind, N, B.bound()), RN);
                return ore;
            }
            public CNL.PL.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
            {
                Assert(false);
                return null;
            }
            public CNL.PL.single single(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.singleThingThat(null, new CNL.PL.thatOrLoop(null, new CNL.PL.orloop(null, new CNL.PL.andloop(null, objectRoleExpr(isPlural, isModal)))));
            }
            public oobject oobject(bool isPlural, bool isModal, endict.Declination declination)
            {
                return new CNL.PL.oobjectA(null, single(isPlural, isModal, declination));
            }
            public CNL.PL.role role(bool isModal, bool isInverse, bool xyMode)
            {
                Assert(false);
                return null;
            }
        }
        public object Visit(Ontorion.CNL.DL.NumberValueRestriction e) 
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
                throw new Exception("Conversion Assertion Failed.");
            }
        }

        private static endict.PlWordKindNoun getDefaultNoun(bool isPlural, endict.Declination declination) 
        {
            if(isPlural)
            {
                return new CNL.PL.endict.PlWordKindNoun(null, 
                                                        new endict.Plural(),
                                                        declination);
            }
            else
            {
                return new CNL.PL.endict.PlWordKindNoun(null,
                                                        new endict.Singular(),
                                                        declination);
            }        
        }

        private static endict.PlWordKindVerb getDefaultVerbForGivenConjugation(endict.Conjugation conjugation)
        {
            return new endict.PlWordKindVerb(null, new endict.Singular(), conjugation);
        }
    }  
}
