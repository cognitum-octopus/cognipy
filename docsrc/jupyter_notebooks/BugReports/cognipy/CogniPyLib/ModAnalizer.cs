//#define USE_SWRL

using Ontorion.CNL.DL;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ontorion.Splitting
{
    public class ModAnalizer : IVisitor
    {
        VisitingParam<LocalityKind> lockind = new VisitingParam<LocalityKind>(LocalityKind.Bottom);
        VisitingParam<LocalityKind> analizeCase = new VisitingParam<LocalityKind>(LocalityKind.Bottom);
        VisitingParam<string> currentKind = new VisitingParam<string>(null);

        public List<List<string>> AnalizeLocality(IAccept stmt, LocalityKind lockind)
        {
            using (this.lockind.set(lockind))
                return stmt.accept(this) as List<List<string>>;
        }

        public object Visit(Paragraph p)
        {
            throw new NotImplementedException();
        }

        public object Visit(Annotation a)
        {
            return CreateNull();
        }

        public static Ontorion.ARS.EntityKind ParseSubjectKind(string kind)
        {
            Ontorion.ARS.EntityKind result;
            if (Enum.TryParse(kind, out result))
            {
                return result;
            }
            else
                throw new Exception("Could not parse " + kind + " to an EntityKind.");
        }

        public object Visit(DLAnnotationAxiom a)
        {
            using (this.analizeCase.set(LocalityKind.Bottom))
            using (this.currentKind.set(CNL.DL.Serializer.entName(ParseSubjectKind(a.subjKind))))
                return ATOM(a.subject);
        }

        public object Visit(Subsumption e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            //for Functional Role
            if (lockind.get() == LocalityKind.Bottom)
            {
                if (e.C is Top)
                {
                    if (e.D is NumberRestriction)
                    {
                        var restr = e.D as NumberRestriction;
                        if ((restr.C is Top) && ((restr.Kind == "≤" && int.Parse(restr.N) == 1) || (restr.Kind == "<" && int.Parse(restr.N) == 2)))
                            return FUNCR(restr.R);
                    }
                }
            }

            //Subsumption of concepts
            return SUBS(e.C, e.D);
        }

        public object Visit(Equivalence e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            // forall X,Y in e.Equivalence return X[=Y & Y[=X 
            return INTERSECT_SYMETRIC_FUN(e.Equivalents, SUBS);
        }

        public object Visit(Disjoint e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            // forall X,Y in e.Disjoints return X[=~Y | Y[=~X 
            return INTERSECT_SYMETRIC_FUN(e.Disjoints, SUBS_NEG);
        }

        public object Visit(DisjointUnion e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            // forall X,Y in e.Union return X[=~Y | Y[=~X 
            // & X|Y|... [= e.name
            var dis = INTERSECT_SYMETRIC_FUN(e.Union, SUBS_NEG);
            var sum = SUBS(new ConceptOr(null) { Exprs = e.Union }, new CNL.DL.Atomic(null) { id = e.name });
            return Intersect(dis, sum);
        }

        public object Visit(DataTypeDefinition e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return CreateNull();
        }

        public object Visit(RoleInclusion e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return ROLINC(e.C, e.D);
        }

        public object Visit(ComplexRoleInclusion e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return COMPLEXROLINC(e.RoleChain, e.R);
        }

        public object Visit(RoleEquivalence e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            // forall X,Y in e.Equivalence return X[=Y & Y[=X 
            return INTERSECT_SYMETRIC_FUN(e.Equivalents, ROLINC);
        }

        public object Visit(RoleDisjoint e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return CreateNull();
        }

        public object Visit(DataRoleInclusion e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return DATAROLINC(e.C, e.D);
        }

        public object Visit(DataRoleEquivalence e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            // forall X,Y in e.Equivalence return X[=Y & Y[=X 
            return INTERSECT_SYMETRIC_FUN(e.Equivalents, DATAROLINC);
        }

        public object Visit(DataRoleDisjoint e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return CreateNull();
        }

        public object Visit(InstanceOf e)
        {
            if (e.modality != Statement.Modality.IS && !(e.I is NamedInstance))
                return CreateNull();

            return INSOF(e.C, (e.I as NamedInstance).name);
        }

        public object Visit(RelatedInstances e)
        {
            if (e.modality != Statement.Modality.IS && (!(e.I is NamedInstance) || !(e.J is NamedInstance)))
                return CreateNull();

            return RELINST((e.I as NamedInstance).name, e.R, (e.J as NamedInstance).name);
        }

        public object Visit(InstanceValue e)
        {
            if (e.modality != Statement.Modality.IS && !(e.I is NamedInstance))
                return CreateNull();

            return INSVAL((e.I as NamedInstance).name, e.R);
        }

        public object Visit(SameInstances e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return INTERSECT_SYMETRIC_FUN(e.Instances, SUBS);
        }

        public object Visit(DifferentInstances e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return INTERSECT_SYMETRIC_FUN(e.Instances, SUBS_NEG);
        }

        public object Visit(HasKey e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return CreateNull();
        }

        public object Visit(Number e)
        {
            return CreateNull();
        }

        public object Visit(CNL.DL.String e)
        {
            return CreateNull();
        }

        public object Visit(CNL.DL.Float e)
        {
            return CreateNull();
        }

        public object Visit(CNL.DL.Bool e)
        {
            return CreateNull();
        }

        public object Visit(CNL.DL.DateTimeVal e)
        {
            return CreateNull();
        }

        public object Visit(CNL.DL.Duration e)
        {
            return CreateNull();
        }

        public object Visit(Facet e)
        {
            return CreateNull();
        }

        public object Visit(FacetList e)
        {
            return CreateNull();
        }

        public object Visit(BoundFacets e)
        {
            return CreateNull();
        }

        public object Visit(BoundOr e)
        {
            return CreateNull();
        }

        public object Visit(BoundAnd e)
        {
            return CreateNull();
        }

        public object Visit(BoundNot e)
        {
            return CreateNull();
        }

        public object Visit(BoundVal e)
        {
            return CreateNull();
        }

        public object Visit(ValueSet e)
        {
            return CreateNull();
        }

        public object Visit(TotalBound e)
        {
            return CreateNull();
        }

        public object Visit(DTBound e)
        {
            return CreateNull();
        }

        public object Visit(TopBound e)
        {
            return CreateNull();
        }

        public object Visit(Atomic e)
        {
            return ATOM(e.id);
        }

        public object Visit(NamedInstance e)
        {
            return CreateNull();
        }

        public object Visit(UnnamedInstance e)
        {
            return CreateNull();
        }

        public object Visit(Top e)
        {
            return TOP();
        }

        public object Visit(Bottom e)
        {
            return BOT();
        }

        public object Visit(RoleInversion e)
        {
            return RINV(e.R);
        }

        public object Visit(InstanceSet e)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
                if (e.Instances.Count == 0)
                    return CreateSimple("∀");

            return CreateNull();
        }

        public object Visit(ConceptOr e)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
                return INTERSECT_LIST(e.Exprs, analizeCase.get());
            else
                return CUMULATE_LIST(e.Exprs, analizeCase.get());
        }

        public object Visit(ConceptAnd e)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
                return CUMULATE_LIST(e.Exprs, analizeCase.get());
            else
                return INTERSECT_LIST(e.Exprs, analizeCase.get());
        }

        public object Visit(ConceptNot e)
        {
            return NEGA(e.C);
        }

        public object Visit(SomeRestriction e)
        {
            return EXISTS(e.R, e.C);
        }

        public object Visit(OnlyRestriction e)
        {
            return ONLY(e.R, e.C);
        }

        public object Visit(OnlyValueRestriction e)
        {
            return ONLYVAL(e.R);
        }

        public object Visit(SomeValueRestriction e)
        {
            return EXISTSVAL(e.R);
        }

        public object Visit(SelfReference e)
        {
            return SELFREF(e.R);
        }

        public object Visit(NumberRestriction e)
        {
            if (e.Kind == "≥" || e.Kind == ">")
            {
                return EXISTS(e.R, e.C);
            }
            else if (e.Kind == "≤" || e.Kind == "<")
            {
                return ONLY_C(e.R, e.C);
            }
            else
            {
                return Intersect(EXISTS(e.R, e.C), ONLY_C(e.R, e.C));
            }
        }

        public object Visit(NumberValueRestriction e)
        {
            if (e.Kind == "≥" || e.Kind == ">")
            {
                return EXISTSVAL(e.R);
            }
            else if (e.Kind == "≤" || e.Kind == "<")
            {
                return ONLYVAL(e.R);
            }
            else
            {
                return Intersect(EXISTSVAL(e.R), ONLYVAL(e.R));
            }
        }

        public object Visit(SwrlStatement e)
        {
#if USE_SWRL
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return SUBS(e.slp, e.slc);
#else
            return CreateNull();
#endif
        }

        public object Visit(SwrlItemList e)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
                return INTERSECT_LIST(e.list, analizeCase.get());
            else
                return CreateNull();
        }

        public object Visit(SwrlInstance e)
        {
            return AnalizeConBottom(e.C, "C");
        }

        public object Visit(SwrlRole e)
        {
            return CreateNull();
        }

        public object Visit(SwrlSameAs e)
        {
            return CreateNull();
        }

        public object Visit(SwrlDifferentFrom e)
        {
            return CreateNull();
        }

        public object Visit(SwrlDataProperty e)
        {
            return CreateNull();
        }

        public object Visit(SwrlDataRange e)
        {
            return CreateNull();
        }

        public object Visit(SwrlBuiltIn e)
        {
            return CreateNull();
        }

        public object Visit(SwrlDVal e)
        {
            return CreateNull();
        }

        public object Visit(SwrlDVar e)
        {
            return CreateNull();
        }

        public object Visit(SwrlIVal e)
        {
            return ATOM_INST(e.I);
        }

        public object Visit(SwrlIVar e)
        {
            return CreateNull();
        }

        public object Visit(ExeStatement e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return CreateNull();
        }

        public object Visit(CodeStatement e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();
            return CreateNull();
        }

        public object Visit(SwrlIterate e)
        {
            if (e.modality != Statement.Modality.IS)
                return CreateNull();

            return CreateNull();
        }

        public object Visit(SwrlVarList e)
        {
            return CreateNull();
        }


// TOOLS
        List<List<string>> FUNCR(CNL.DL.Node R)
        {
            return AnalizeConBottom(R, "R");
        }


        List<List<string>> SUBS(CNL.DL.Node C, CNL.DL.Node D)
        {
            return Cumulate(AnalizeConBottom(C, "C"), AnalizeConTop(D, "C"));
        }

        List<List<string>> SUBS(string I, CNL.DL.Node D)
        {
            return Cumulate(ATOM_INST(I), AnalizeConTop(D, "C"));
        }

        List<List<string>> SUBS(CNL.DL.SwrlItemList P, CNL.DL.SwrlItemList C)
        {
            return Cumulate(AnalizeConBottom(P, "C"), AnalizeConTop(C, "C"));
        }

        List<List<string>> SUBS_NEG(CNL.DL.Node C, CNL.DL.Node D)
        {
            return Cumulate(AnalizeConBottom(C, "C"), AnalizeConTop(new CNL.DL.ConceptNot(null, D), "C"));
        }

        List<List<string>> SUBS(CNL.DL.Instance I, CNL.DL.Instance J)
        {
            if (J is CNL.DL.NamedInstance && isLocalInstanceName((J as CNL.DL.NamedInstance).name))
                return ATOM_INST((J as CNL.DL.NamedInstance).name);
            else
                return CreateNull();
        }

        List<List<string>> SUBS_NEG(CNL.DL.Instance I, CNL.DL.Instance J)
        {
            if (J is CNL.DL.NamedInstance && isLocalInstanceName((J as CNL.DL.NamedInstance).name))
            {
                if (I is CNL.DL.NamedInstance && isLocalInstanceName((I as CNL.DL.NamedInstance).name))
                {
                    return Cumulate(ATOM_INST((I as CNL.DL.NamedInstance).name), ATOM_INST((J as CNL.DL.NamedInstance).name));
                }
                else
                    return ATOM_INST((J as CNL.DL.NamedInstance).name);
            }
            else
                return CreateNull();
        }

        List<List<string>> ROLINC(CNL.DL.Node R, CNL.DL.Node S)
        {
            return AnalizeCon(lockind.get() == LocalityKind.Bottom ? R : S, "R", lockind.get());
        }

        List<List<string>> COMPLEXROLINC(List<CNL.DL.Node> Chain, CNL.DL.Node R)
        {
            if (lockind.get() == LocalityKind.Bottom)
            {
                List<List<string>> ret = CreateNull();
                foreach (var S in Chain)
                    ret = Cumulate(ret, AnalizeConBottom(S, "R"));
                return ret;
            }
            else
            {
                return AnalizeConTop(R, "R");
            }
        }

        List<List<string>> DATAROLINC(CNL.DL.Node R, CNL.DL.Node S)
        {
            return AnalizeCon(lockind.get() == LocalityKind.Bottom ? R : S, "D", lockind.get());
        }

        List<List<string>> INTERSECT_SYMETRIC_FUN<T>(List<T> List, Func<T, T, List<List<string>>> F)
        {
            List<List<string>> ret = null;
            for (int i = 0; i < List.Count - 1; i++)
            {
                var X = List[i];
                for (int j = i + 1; j < List.Count; j++)
                {
                    var Y = List[j];
                    var n = F(X, Y);
                    var m = F(Y, X);
                    ret = (ret == null) ? Intersect(n, m) : Intersect(ret, Intersect(n, m));
                }
            }
            return ret;
        }

        public List<List<string>> INSOF(CNL.DL.Node C, string I)
        {
            if (isLocalInstanceName(I))
            {
                // for local instance we have e.I [= e.C where e.I = (e.I | BOT)
                return SUBS(I, C);
            }
            else
                return AnalizeConTop(C, "C");
        }

        List<List<string>> RELINST(string I, CNL.DL.Node R, string J)
        {
            if (isLocalInstanceName(I) && isLocalInstanceName(J))
            {
                // for local instance we have e.I = E R. J where e.I = (e.I | BOT)

                List<List<string>> A;
                List<List<string>> B;
                {
                    var inter = lockind.get() == LocalityKind.Top ? Intersect(AnalizeConTop(R, "R"),
                        CreateSimple("I" + ":" + J)) : CreateNull();
                    A = Cumulate(CreateSimple("I" + ":" + I), inter);
                }
                {
                    var inter = lockind.get() == LocalityKind.Top ? Intersect(AnalizeConTop(R, "R"),
                        CreateSimple("I" + ":" + I)) : CreateNull();
                    B = Cumulate(CreateSimple("I" + ":" + J), inter);
                }
                return Intersect(A, B);
            }
            else
            {
                if (lockind.get() == LocalityKind.Top)
                {
                    return AnalizeConTop(R, "R");
                }
            }

            return CreateNull();
        }

        List<List<string>> RELINST(SwrlIObject I, string R, SwrlIObject J)
        {
            if ((I is SwrlIVal && isLocalInstanceName((I as SwrlIVal).I)) || (J is SwrlIVal && isLocalInstanceName((J as SwrlIVal).I)))
            {
                // for local instance we have e.I = E R. J where e.I = (e.I | BOT)

                List<List<string>> A = null;
                List<List<string>> B = null;
                if (I is SwrlIVal && isLocalInstanceName((I as SwrlIVal).I))
                {
                    if (J is SwrlIVal && isLocalInstanceName((J as SwrlIVal).I))
                    {
                        var inter = lockind.get() == LocalityKind.Top ? Intersect(CreateSimple("R:" + R),
                            CreateSimple("I" + ":" + (J as SwrlIVal).I)) : CreateNull();
                        A = Cumulate(CreateSimple("I" + ":" + (I as SwrlIVal).I), inter);
                    }
                    else
                    {
                        A = CreateSimple("I" + ":" + (I as SwrlIVal).I);
                    }
                }
                if (J is SwrlIVal && isLocalInstanceName((J as SwrlIVal).I))
                {
                    if (I is SwrlIVal && isLocalInstanceName((I as SwrlIVal).I))
                    {
                        var inter = lockind.get() == LocalityKind.Top ? Intersect(CreateSimple("R:" + R),
                            CreateSimple("I" + ":" + (I as SwrlIVal).I)) : CreateNull();
                        B = Cumulate(CreateSimple("I" + ":" + (J as SwrlIVal).I), inter);
                    }
                    else
                    {
                        A = CreateSimple("I" + ":" + (J as SwrlIVal).I);
                    }
                }
                if(A!=null && B!=null)
                    return Intersect(A, B);
            }
            else
            {
                if (lockind.get() == LocalityKind.Top)
                {
                    return CreateSimple("R:" + R);
                }
            }

            return CreateNull();
        }

        List<List<string>> INSVAL(string I, CNL.DL.Node D)
        {

            if (isLocalInstanceName(I))
            {
                var inter = lockind.get() == LocalityKind.Top ? AnalizeConTop(D, "D") : CreateNull();
                return Cumulate(CreateSimple("I" + ":" + I), inter);
            }
            else
            {
                if (lockind.get() == LocalityKind.Top)
                {
                    return AnalizeConTop(D, "D");
                }
            }

            return CreateNull();
        }

        List<List<string>> ATOM(string id)
        {
            if (analizeCase.get() == lockind.get())
                return CreateSimple(currentKind.get() + ":" + id);
            else
                return CreateNull();
        }

        List<List<string>> ATOM_R(string id)
        {
            if (analizeCase.get() == lockind.get())
                return CreateSimple("R:" + id);
            else
                return CreateNull();
        }

        List<List<string>> ATOM_INST(string id)
        {
            if (analizeCase.get() == lockind.get())
                return CreateSimple("I:" + id);
            else
                return CreateNull();
        }

        List<List<string>> TOP()
        {
            if (analizeCase.get() == LocalityKind.Top)
                return CreateSimple("∀");
            return CreateNull();
        }

        List<List<string>> BOT()
        {
            if (analizeCase.get() == LocalityKind.Bottom)
                return CreateSimple("∀");
            else
                return CreateNull();
        }

        List<List<string>> RINV(CNL.DL.Node R)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
                return AnalizeConBottom(R, "R");
            else
                return CreateNull();
        }

        public List<List<string>> INTERSECT_LIST<T>(List<T> List, LocalityKind analizeCase) where T : IAccept
        {
            List<List<string>> ret = null;
            foreach (var X in List)
                ret = (ret == null) ? AnalizeCon(X, "C", analizeCase) : Intersect(ret, AnalizeCon(X, "C", analizeCase));
            return ret;
        }

        public List<List<string>> CUMULATE_LIST<T>(List<T> List, LocalityKind analizeCase) where T : IAccept
        {
            List<List<string>> ret = null;
            foreach (var X in List)
                ret = (ret == null) ? AnalizeCon(X, "C", analizeCase) : Cumulate(ret, AnalizeCon(X, "C", analizeCase));
            return ret;
        }

        List<List<string>> NEGA(CNL.DL.Node C)
        {
            return AnalizeCon(C, "C", analizeCase.get() == LocalityKind.Bottom ? LocalityKind.Top : LocalityKind.Bottom);
        }

        List<List<string>> NEGA(Func<List<List<string>>> F)
        {
            using (this.analizeCase.set(analizeCase.get() == LocalityKind.Bottom ? LocalityKind.Top : LocalityKind.Bottom))
            using (this.currentKind.set("C"))
                return F();
        }

        List<List<string>> EXISTS(CNL.DL.Node R, CNL.DL.Node C)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
            {
                if (lockind.get() == LocalityKind.Bottom)
                    return Cumulate(AnalizeConBottom(R, "R"), AnalizeConBottom(C, "C"));
                else
                    return AnalizeConBottom(C, "C");
            }
            else
            {
                if (lockind.get() == LocalityKind.Top)
                {
                    if (C is InstanceSet)
                    {
                        if ((C as InstanceSet).Instances.Count() > 0)
                            return AnalizeConTop(R, "R");
                    }
                    else
                        return Intersect(AnalizeConTop(R, "R"), AnalizeConTop(C, "C"));
                }
            }
            return CreateNull();
        }

        List<List<string>> EXISTSVAL(CNL.DL.Node R)
        {
            if (analizeCase.get() == LocalityKind.Bottom)
            {
                if (lockind.get() == LocalityKind.Bottom)
                    return AnalizeConBottom(R, "D");
            }
            return CreateNull();
        }

        List<List<string>> ONLY(CNL.DL.Node R, CNL.DL.Node C)
        {
            return NEGA(()=>EXISTS(R, new ConceptNot(null, C)));
        }

        List<List<string>> ONLYVAL(CNL.DL.Node R)
        {
            return NEGA(() => EXISTSVAL(R));
        }

        List<List<string>> ONLY_C(CNL.DL.Node R, CNL.DL.Node C)
        {
            return NEGA(()=>EXISTS(R, C));
        }

        List<List<string>> SELFREF(CNL.DL.Node R)
        {
            if (analizeCase.get() == lockind.get())
                return AnalizeCon(R, "R", lockind.get());
            else
                return CreateNull();
        }

        static bool isLocalInstanceName(string name)
        {
            return name.StartsWith("_");
        }

        ///Locality
        class LXComporer : IComparer<List<string>>
        {
            int IComparer<List<string>>.Compare(List<string> lx, List<string> ly)
            {
                int ms = lx.Count < ly.Count ? lx.Count : ly.Count;
                List<string>.Enumerator lxEn = lx.GetEnumerator();
                List<string>.Enumerator lyEn = ly.GetEnumerator();
                lxEn.MoveNext();
                lyEn.MoveNext();
                for (int i = 0; i < ms; i++)
                {
                    if (lxEn.Current.CompareTo(lyEn.Current) < 0)
                        return -1;
                    else if (lxEn.Current.CompareTo(lyEn.Current) > 0)
                        return 1;
                    lxEn.MoveNext();
                    lyEn.MoveNext();
                }
                if (lx.Count < ly.Count)
                    return -1;
                else if (lx.Count > ly.Count)
                    return 1;
                else
                    return 0;
            }
        }


        List<List<string>> AnalizeCon(IAccept C, string kind, LocalityKind analizeCase)
        {
            using (this.analizeCase.set(analizeCase))
            using (this.currentKind.set(kind))
                return C.accept(this) as List<List<string>>;
        }

        List<List<string>> AnalizeConBottom(IAccept C, string kind)
        {
            using (this.analizeCase.set(LocalityKind.Bottom))
            using (this.currentKind.set(kind))
                return C.accept(this) as List<List<string>>;
        }

        List<List<string>> AnalizeConTop(IAccept C, string kind)
        {
            using (this.analizeCase.set(LocalityKind.Top))
            using (this.currentKind.set(kind))
                return C.accept(this) as List<List<string>>;
        }

        static List<List<string>> CreateSimple(string s)
        {
            var ret = new List<List<string>>();
            ret.Add(new List<string>());
            ret[0].Add(s);
            return ret;
        }

        static List<List<string>> CreateNull()
        {
            var ret = new List<List<string>>();
            return ret;
        }

        public static bool IsAny(List<List<string>> s)
        {
            if (s.Count == 1)
            {
                if (s[0].Count == 1)
                    return s[0][0] == "∀";
            }
            return false;
        }

        static List<List<string>> Cumulate(List<List<string>> a, List<List<string>> b)
        {
            if (IsAny(a)) return a;
            if (IsAny(b)) return b;
            var cmp = new LXComporer();
            var ret = new List<List<string>>();
            foreach (List<string> l in b)
            {
                if (a.BinarySearch(l, cmp) < 0)
                    ret.Add(l);
            }
            ret.AddRange(a);
            ret.Sort(cmp);
            return ret;
        }

        static List<List<string>> Intersect(List<List<string>> a, List<List<string>> b)
        {
            if (IsAny(a)) return b;
            if (IsAny(b)) return a;
            var cmp = new LXComporer();
            var ret = new List<List<string>>();
            foreach (List<string> k in a)
            {
                foreach (List<string> l in b)
                {
                    var inter = new List<string>();
                    foreach (string x in l)
                    {
                        if (k.BinarySearch(x) < 0)
                        {
                            inter.Add(x);
                        }
                    }
                    inter.AddRange(k);
                    inter.Sort();
                    if (ret.BinarySearch(inter, cmp) < 0)
                        ret.Add(inter);
                }
            }
            ret.Sort(cmp);
            return ret;
        }



    }
}
