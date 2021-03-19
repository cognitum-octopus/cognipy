using CogniPy.CNL.DL;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading;

namespace CogniPy.Splitting
{
    public enum LocalityKind { Top = 1, Bottom = 0 };

    public static class DLToys
    {
        public static string EncodeToIdentifier(string toEncode)
        {
            byte[] toEncodeAsBytes = System.Text.Encoding.Unicode.GetBytes(toEncode);
            string returnValue = System.Convert.ToBase64String(toEncodeAsBytes);
            return "\"" + returnValue.Replace("/", "-").Replace("+", "-") + "\"";
        }

        public static LocalityKind[] LocalityKinds = new[] { LocalityKind.Bottom, LocalityKind.Top };

        private static ThreadLocal<Tools.Parser> p = new ThreadLocal<Tools.Parser>(() => new CogniPy.CNL.DL.dl());

        public static CogniPy.CNL.DL.Paragraph ParseDL(string DL)
        {
            if (DL.Trim() == "")
                return new Paragraph(null) { Statements = new List<Statement>() };

            Tools.SYMBOL smb = p.Value.Parse(DL);
            if (smb is CogniPy.CNL.DL.Paragraph)   // get null on syntax error
            {
                return smb as CogniPy.CNL.DL.Paragraph;
            }
            else
            {
                if (smb is Tools.error)
                {
                    throw new Exception(smb.ToString());
                }
                else
                {
                    throw new Exception("Unknown parse exception!");
                }
            }
        }

        public static string MakeScriptFromParagraph(Paragraph ast)
        {
            var ser = new CogniPy.CNL.DL.Serializer();
            return ser.Serialize(ast);
        }

        public static string MakeExpressionFromStatement(Statement stmt)
        {
            var ser = new CogniPy.CNL.DL.Serializer();
            return ser.Serialize(stmt);
        }

        public static HashSet<string> GetSignatureFromStatement(Statement stmt)
        {
            if (stmt is CodeStatement)
                return new HashSet<string>() { "∀" };
            else
            {
                var ser = new CogniPy.CNL.DL.Serializer();
                ser.Serialize(stmt);
                return ser.GetSignature();
            }
        }

        public static HashSet<string> GetSignatureFromParagraph(Paragraph stmt)
        {
            var ser = new CogniPy.CNL.DL.Serializer();
            ser.Serialize(stmt);
            return ser.GetSignature();
        }

        /////////////////


        class DLLink : CNL.DL.Node
        {
            public string I, J, R;
            public DLLink(string I, string J, string R) : base(null) { this.I = I; this.J = J; this.R = R; }
        }

        private static bool alreadyExists(List<object> lst, object n)
        {
            if (n is Atomic)
            {
                foreach (var l in lst)
                {
                    if (l is Atomic)
                    {
                        return (l as Atomic).id == (n as Atomic).id;
                    }
                }
            }
            return false;
        }

        private static Tuple<string, object> getSimpleDLFormOfSwrlItem(SwrlItem item)
        {
            {
                var conc = item as SwrlInstance;
                if (conc != null)
                {
                    var V = conc.I as SwrlIVar;
                    if (V != null)
                    {
                        return Tuple.Create(V.VAR, (object)conc.C);
                    }
                }
            }
            {
                var conc = item as SwrlRole;
                if (conc != null)
                {
                    var V1 = conc.I as SwrlIVar;
                    var V2 = conc.J as SwrlIVar;
                    if (V1 != null)
                    {
                        if (V2 != null)
                        {
                            return Tuple.Create((string)null, (object)new DLLink(V1.VAR, V2.VAR, conc.R));
                        }
                        else
                        {
                            var L2 = conc.J as SwrlIVal;
                            if (L2 != null)
                            {
                                return Tuple.Create(V1.VAR, (object)new CNL.DL.SomeRestriction(null, new CNL.DL.Atomic(null) { id = conc.R }, new CNL.DL.InstanceSet(null) { Instances = new List<Instance>() { new CNL.DL.NamedInstance(null) { name = L2.I } } }));
                            }
                        }
                    }
                    if (V2 != null)
                    {
                        var L1 = conc.I as SwrlIVal;
                        if (L1 != null)
                        {
                            return Tuple.Create(V2.VAR, (object)new CNL.DL.SomeRestriction(null, new CNL.DL.RoleInversion(null, new CNL.DL.Atomic(null) { id = conc.R }), new CNL.DL.InstanceSet(null) { Instances = new List<Instance>() { new CNL.DL.NamedInstance(null) { name = L1.I } } }));
                        }
                    }
                }
            }
            {
                var conc = item as SwrlDataProperty;
                if (conc != null)
                {
                    var V = conc.IO as SwrlIVar;
                    var D = conc.DO as SwrlDVal;
                    if (V != null && D != null)
                    {
                        return Tuple.Create(V.VAR, (object)new CNL.DL.SomeValueRestriction(null, new CNL.DL.Atomic(null) { id = conc.R }, new CNL.DL.ValueSet(null) { Values = new List<Value>() { D.Val } }));
                    }
                    var V2 = conc.DO as SwrlDVar;
                    if (V != null && V2 != null)
                    {
                        return Tuple.Create((string)null, (object)new DLLink(V.VAR, V2.VAR, conc.R));
                    }
                }
            }
            {
                var conc = item as SwrlDataRange;
                if (conc != null)
                {
                    var V = conc.DO as SwrlDVar;
                    if (V != null)
                    {
                        return Tuple.Create(V.VAR, (object)conc.B);
                    }
                }
            }

            return null;
        }


        public static Statement TransformSwrlToDL(SwrlStatement e)
        {
            if (e.slc.list.Count == 1)
            {
                var conc = getSimpleDLFormOfSwrlItem(e.slc.list[0]);
                if (conc != null && conc.Item1 != null && conc.Item2 is Node)
                {
                    Dictionary<string, List<object>> nodes = new Dictionary<string, List<object>>();
                    Dictionary<string, Dictionary<string, HashSet<string>>> links = new Dictionary<string, Dictionary<string, HashSet<string>>>();
                    Dictionary<string, Dictionary<string, HashSet<string>>> invlinks = new Dictionary<string, Dictionary<string, HashSet<string>>>();
                    HashSet<Tuple<string, string, string>> pendinglinks = new HashSet<Tuple<string, string, string>>();
                    foreach (var p in e.slp.list)
                    {
                        var pred = getSimpleDLFormOfSwrlItem(p);
                        if (pred == null)
                            return e;
                        if (pred.Item2 is DLLink)
                        {
                            var l = pred.Item2 as DLLink;
                            if (!links.ContainsKey(l.I))
                                links.Add(l.I, new Dictionary<string, HashSet<string>>());
                            if (!links[l.I].ContainsKey(l.J))
                                links[l.I].Add(l.J, new HashSet<string>());
                            links[l.I][l.J].Add(l.R);
                            pendinglinks.Add(Tuple.Create(l.I, l.J, l.R));
                            if (!invlinks.ContainsKey(l.J))
                                invlinks.Add(l.J, new Dictionary<string, HashSet<string>>());
                            if (!invlinks[l.J].ContainsKey(l.I))
                                invlinks[l.J].Add(l.I, new HashSet<string>());
                            invlinks[l.J][l.I].Add(l.R);
                        }
                        else
                        {
                            if (!nodes.ContainsKey(pred.Item1))
                                nodes.Add(pred.Item1, new List<object>());
                            if (!alreadyExists(nodes[pred.Item1], pred.Item2))
                                nodes[pred.Item1].Add(pred.Item2);
                        }
                    }

                    Func<string, List<Node>> act = null;

                    HashSet<string> alreadyDone = new HashSet<string>();

                    act = new Func<string, List<Node>>((xname) =>
                    {
                        alreadyDone.Add(xname);
                        List<Node> torkn = new List<Node>();

                        foreach (var kv in nodes)
                        {
                            if (kv.Key == xname)
                            {
                                var r = getAllAs<Node>(kv.Value);
                                if (r == null)
                                    return null;
                                torkn.AddRange(r);
                            }
                            else
                            {
                                if (links.ContainsKey(xname) && links[xname].ContainsKey(kv.Key))
                                {
                                    if (links[xname][kv.Key].Count > 1)
                                        return null;
                                    var R = links[xname][kv.Key].First();
                                    pendinglinks.Remove(Tuple.Create(xname, kv.Key, R));
                                    if (nodes[kv.Key].First() is Node)
                                    {
                                        var r = getAllAs<Node>(nodes[kv.Key]);
                                        if (r == null) return null;
                                        if (!alreadyDone.Contains(kv.Key))
                                        {
                                            var cc = act(kv.Key);
                                            if (cc == null) return null;
                                            r.AddRange(cc);
                                        }
                                        torkn.Add(new CNL.DL.SomeRestriction(null, new CNL.DL.Atomic(null) { id = R }, new CNL.DL.ConceptAnd(null) { Exprs = r }));
                                    }
                                    else
                                    {
                                        var r = getAllAs<AbstractBound>(nodes[kv.Key]);
                                        if (r == null) return null;
                                        torkn.Add(new CNL.DL.SomeValueRestriction(null, new CNL.DL.Atomic(null) { id = R }, new CNL.DL.BoundAnd(null) { List = r }));
                                    }
                                }
                                else if (invlinks.ContainsKey(xname) && invlinks[xname].ContainsKey(kv.Key))
                                {
                                    if (invlinks[xname][kv.Key].Count > 1)
                                        return null;
                                    var R = invlinks[xname][kv.Key].First();
                                    pendinglinks.Remove(Tuple.Create(xname, kv.Key, R));
                                    if (nodes[kv.Key].First() is Node)
                                    {
                                        var r = getAllAs<Node>(nodes[kv.Key]);
                                        if (r == null) return null;
                                        if (!alreadyDone.Contains(kv.Key))
                                        {
                                            var cc = act(kv.Key);
                                            if (cc == null) return null;
                                            r.AddRange(cc);
                                        }
                                        torkn.Add(new CNL.DL.SomeRestriction(null, new CNL.DL.RoleInversion(null, new CNL.DL.Atomic(null) { id = R }), new CNL.DL.ConceptAnd(null) { Exprs = r }));
                                    }
                                    else
                                    {
                                        return null;
                                    }
                                }
                                else
                                    return null;
                            }
                        }

                        return torkn;
                    });

                    var rr = act(conc.Item1);
                    if (rr == null)
                        return e;

                    if (pendinglinks.Count > 0)
                        return e;

                    return new CNL.DL.Subsumption(null, new CNL.DL.ConceptAnd(null) { Exprs = rr }, (Node)conc.Item2, Statement.Modality.IS);
                }
            }
            return e;
        }
        private static List<T> getAllAs<T>(List<object> lst)
        {
            List<T> ret = new List<T>();
            foreach (var o in lst)
            {
                if (o is T)
                    ret.Add((T)o);
                else
                    return null;
            }
            return ret;
        }


        /////////////////////


        ///Locality
        private class LXComporer : IComparer<List<string>>
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
                    {
                        return -1;
                    }
                    else if (lxEn.Current.CompareTo(lyEn.Current) > 0)
                    {
                        return 1;
                    }
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

        private static List<List<string>> CreateSimple(string s)
        {
            var ret = new List<List<string>>();
            ret.Add(new List<string>());
            ret[0].Add(s);
            return ret;
        }

        private static List<List<string>> CreateNull()
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

        private static List<List<string>> Cumulate(List<List<string>> a, List<List<string>> b)
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

        private static List<List<string>> Intersect(List<List<string>> a, List<List<string>> b)
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

        private static bool isLocalInstanceName(string name)
        {
            return name.StartsWith("_");
        }

        static List<List<string>> AnalizeLocality(Tools.SYMBOL stmt, LocalityKind lockind, out bool ret)
        {
            ret = true;
            if (stmt is Subsumption)
            {
                if ((stmt as Subsumption).modality == Statement.Modality.IS)
                {
                    //Subsumption of concepts
                    if (lockind == LocalityKind.Bottom)
                    {
                        //functional role
                        if ((stmt as Subsumption).C is Top)
                        {
                            if ((stmt as Subsumption).D is NumberRestriction)
                            {
                                var restr = (stmt as Subsumption).D as NumberRestriction;
                                if ((restr.C is Top) && ((restr.Kind == "≤" && int.Parse(restr.N) == 1) || (restr.Kind == "<" && int.Parse(restr.N) == 2)))
                                    return AnalizeConBottom(restr.R, lockind, "R");
                            }
                        }
                    }

                    return Cumulate(AnalizeConBottom((stmt as Subsumption).C, lockind, "C"), AnalizeConTop((stmt as Subsumption).D, lockind, "C"));
                }
            }
            else if (stmt is RoleInclusion)
            {
                if (lockind == LocalityKind.Bottom)
                {
                    return AnalizeConBottom((stmt as RoleInclusion).C, lockind, "R");
                }
                else
                {
                    return AnalizeConTop((stmt as RoleInclusion).D, lockind, "R");
                }
            }
            else if (stmt is ComplexRoleInclusion)
            {
                //ComplexRoleInclusion
                if (lockind == LocalityKind.Bottom)
                {
                    List<List<string>> r = CreateNull();
                    foreach (var S in (stmt as ComplexRoleInclusion).RoleChain)
                        r = Cumulate(r, AnalizeConBottom(S, lockind, "R"));
                    return r;
                }
                else
                {
                    return AnalizeConTop((stmt as ComplexRoleInclusion).R, lockind, "R");
                }
            }
            else if (stmt is Equivalence)
            {
                if ((stmt as Equivalence).modality == Statement.Modality.IS)
                {
                    //Equivalence of concepts
                    List<List<string>> frst = null;
                    var X = (stmt as Equivalence).Equivalents[0];
                    for (int i = 1; i < (stmt as Equivalence).Equivalents.Count; i++)
                    {
                        var Y = (stmt as Equivalence).Equivalents[i];
                        var n = Cumulate(AnalizeConBottom(X, lockind, "C"), AnalizeConTop(Y, lockind, "C"));
                        var m = Cumulate(AnalizeConBottom(Y, lockind, "C"), AnalizeConTop(X, lockind, "C"));
                        frst = frst == null ? Intersect(n, m) : Intersect(frst, Intersect(n, m));
                    }
                    return frst;
                }
            }
            else if (stmt is RoleEquivalence)
            {
                //Equivalence of roles
                if (lockind == LocalityKind.Bottom)
                {
                    List<List<string>> frst = null;
                    var X = (stmt as RoleEquivalence).Equivalents[0];
                    for (int i = 1; i < (stmt as RoleEquivalence).Equivalents.Count; i++)
                    {
                        var Y = (stmt as RoleEquivalence).Equivalents[i];
                        var n = Intersect(AnalizeConBottom(X, lockind, "R"), AnalizeConBottom(Y, lockind, "R"));
                        frst = frst == null ? n : Intersect(frst, n);
                    }
                    return frst;
                }
                else
                {
                    List<List<string>> frst = null;
                    var X = (stmt as RoleEquivalence).Equivalents[0];
                    for (int i = 1; i < (stmt as RoleEquivalence).Equivalents.Count; i++)
                    {
                        var Y = (stmt as RoleEquivalence).Equivalents[i];
                        var n = Intersect(AnalizeConTop(X, lockind, "R"), AnalizeConTop(Y, lockind, "R"));
                        frst = frst == null ? n : Intersect(frst, n);
                    }
                    return frst;
                }
            }
            else if (stmt is InstanceOf)
            {
                if ((stmt as InstanceOf).modality == Statement.Modality.IS && ((stmt as InstanceOf).I is NamedInstance))
                {
                    //InstanceOf
                    var n = ((stmt as InstanceOf).I as NamedInstance).name;
                    if (isLocalInstanceName(n))
                    {
                        return Cumulate(CreateSimple("I" + ":" + n), AnalizeConTop((stmt as InstanceOf).C, lockind, "C"));
                    }
                    else
                    {
                        return AnalizeConTop((stmt as InstanceOf).C, lockind, "C");
                    }
                }
            }
            else if (stmt is InstanceValue)
            {
                if ((stmt as InstanceValue).modality == Statement.Modality.IS && ((stmt as InstanceValue).I is NamedInstance))
                {
                    //InstanceValue
                    var n = ((stmt as InstanceValue).I as NamedInstance).name;
                    if (isLocalInstanceName(n))
                    {
                        var inter = lockind == LocalityKind.Top ? AnalizeConTop((stmt as InstanceValue).R, lockind, "D") : CreateNull();
                        return Cumulate(CreateSimple("I" + ":" + n), inter);
                    }
                    else
                    {
                        if (lockind == LocalityKind.Top)
                        {
                            return AnalizeConTop((stmt as InstanceValue).R, lockind, "D");
                        }
                    }
                }
            }
            else if (stmt is RelatedInstances)
            {
                if ((stmt as RelatedInstances).modality == Statement.Modality.IS && ((stmt as RelatedInstances).I is NamedInstance) && ((stmt as RelatedInstances).J is NamedInstance))
                {
                    //Related Instances
                    var n = ((stmt as RelatedInstances).I as NamedInstance).name;
                    var m = ((stmt as RelatedInstances).J as NamedInstance).name;
                    if (isLocalInstanceName(n) && isLocalInstanceName(m))
                    {
                        List<List<string>> A;
                        List<List<string>> B;
                        {
                            var inter = lockind == LocalityKind.Top ? Intersect(AnalizeConTop((stmt as RelatedInstances).R, lockind, "R"),
                                CreateSimple("I" + ":" + m)) : CreateNull();
                            A = Cumulate(CreateSimple("I" + ":" + n), inter);
                        }
                        {
                            var inter = lockind == LocalityKind.Top ? Intersect(AnalizeConTop((stmt as RelatedInstances).R, lockind, "R"),
                                CreateSimple("I" + ":" + n)) : CreateNull();
                            B = Cumulate(CreateSimple("I" + ":" + m), inter);
                        }
                        return Intersect(A, B);
                    }
                    else
                    {
                        if (lockind == LocalityKind.Top)
                        {
                            return AnalizeConTop((stmt as RelatedInstances).R, lockind, "R");
                        }
                    }
                }
            }
            ret = false;
            return CreateNull();
        }

        private static List<List<string>> AnalizeConBottom(Tools.SYMBOL C, LocalityKind lockind, string kind)
        {
            if (C is Bottom)
                return CreateSimple("∀");
            else if (C is InstanceSet)
            {
                if ((C as InstanceSet).Instances.Count == 0)
                    return CreateSimple("∀");
            }
            else if (C is Atomic)
            {
                if (lockind == LocalityKind.Bottom)
                    return CreateSimple(kind + ":" + (C as Atomic).id);
            }
            else if (C is RoleInversion)
            {
                return AnalizeConBottom((C as RoleInversion).R, lockind, "R");
            }
            else if (C is ConceptNot)
            {
                return AnalizeConTop((C as ConceptNot).C, lockind, "C");
            }
            else if (C is ConceptAnd)
            {
                List<List<string>> frst = null;
                foreach (var X in (C as ConceptAnd).Exprs)
                    frst = frst == null ? AnalizeConBottom(X, lockind, "C") : Cumulate(frst, AnalizeConBottom(X, lockind, "C"));
                return frst;
            }
            else if (C is ConceptOr)
            {
                List<List<string>> frst = null;
                foreach (var X in (C as ConceptOr).Exprs)
                    frst = frst == null ? AnalizeConBottom(X, lockind, "C") : Intersect(frst, AnalizeConBottom(X, lockind, "C"));
                return frst;
            }
            else if (C is SomeRestriction)
            {
                if (lockind == LocalityKind.Bottom)
                    return Cumulate(AnalizeConBottom((C as SomeRestriction).R, lockind, "R"), AnalizeConBottom((C as SomeRestriction).C, lockind, "C"));
                else
                    return AnalizeConBottom((C as SomeRestriction).C, lockind, "C");
            }
            else if (C is OnlyRestriction)
            {
                if (lockind == LocalityKind.Top)
                    return Intersect(AnalizeConBottom((C as OnlyRestriction).R, lockind, "R"), AnalizeConTop((C as OnlyRestriction).C, lockind, "C"));
            }
            else if (C is SelfReference)
            {
                if (lockind == LocalityKind.Bottom)
                    return AnalizeConBottom((C as SelfReference).R, lockind, "R");
            }
            else if (C is NumberRestriction)
            {
                if (lockind == LocalityKind.Bottom)
                {
                    if ((C as NumberRestriction).Kind == "≥" || (C as NumberRestriction).Kind == ">")
                        return Cumulate(AnalizeConBottom((C as NumberRestriction).R, lockind, "R"), AnalizeConBottom((C as NumberRestriction).C, lockind, "C"));
                }
                else
                {
                    if ((C as NumberRestriction).Kind == "≥" || (C as NumberRestriction).Kind == ">")
                        return AnalizeConBottom((C as NumberRestriction).C, lockind, "C");
                    else if ((C as NumberRestriction).Kind == "≤" || (C as NumberRestriction).Kind == "<")
                        return Intersect(AnalizeConTop((C as NumberRestriction).R, lockind, "R"), AnalizeConTop((C as NumberRestriction).C, lockind, "C"));
                    else
                        return Intersect(AnalizeConBottom((C as NumberRestriction).C, lockind, "C"), Intersect(AnalizeConTop((C as NumberRestriction).R, lockind, "R"), AnalizeConTop((C as NumberRestriction).C, lockind, "C")));
                }
            }
            return CreateNull();
        }

        private static List<List<string>> AnalizeConTop(Tools.SYMBOL C, LocalityKind lockind, string kind)
        {
            if (C is Top)
            {
                return CreateSimple("∀");
            }
            else if (C is Atomic)
            {
                if (lockind == LocalityKind.Top)
                    return CreateSimple(kind + ":" + (C as Atomic).id);
            }
            else if (C is ConceptNot)
            {
                return AnalizeConBottom((C as ConceptNot).C, lockind, "C");
            }
            else if (C is ConceptAnd)
            {
                List<List<string>> frst = null;
                foreach (var X in (C as ConceptAnd).Exprs)
                    frst = frst == null ? AnalizeConTop(X, lockind, "C") : Intersect(frst, AnalizeConTop(X, lockind, "C"));
                return frst;
            }
            else if (C is ConceptOr)
            {
                List<List<string>> frst = null;
                foreach (var X in (C as ConceptOr).Exprs)
                    frst = frst == null ? AnalizeConTop(X, lockind, "C") : Cumulate(frst, AnalizeConTop(X, lockind, "C"));
                return frst;
            }
            else if (C is SomeRestriction)
            {
                if (lockind == LocalityKind.Top)
                {
                    if ((C as SomeRestriction).C is InstanceSet)
                    {
                        if (((C as SomeRestriction).C as InstanceSet).Instances.Count() > 0)
                            return AnalizeConTop((C as SomeRestriction).R, lockind, "R");
                    }
                    else
                        return Intersect(AnalizeConTop((C as SomeRestriction).R, lockind, "R"), AnalizeConTop((C as SomeRestriction).C, lockind, "C"));
                }
            }
            else if (C is SelfReference)
            {
                if (lockind == LocalityKind.Top)
                    return AnalizeConTop((C as SelfReference).R, lockind, "R");
            }
            else if (C is OnlyRestriction)
            {
                if (lockind == LocalityKind.Bottom)
                    return Cumulate(AnalizeConBottom((C as OnlyRestriction).R, lockind, "R"), AnalizeConTop((C as OnlyRestriction).C, lockind, "C"));
                else
                    return AnalizeConTop((C as OnlyRestriction).C, lockind, "C");
            }
            else if (C is NumberRestriction)
            {
                if (lockind == LocalityKind.Bottom)
                {
                    if ((C as NumberRestriction).Kind == "≤" || (C as NumberRestriction).Kind == "<")
                        return Cumulate(AnalizeConBottom((C as NumberRestriction).R, lockind, "R"), AnalizeConBottom((C as NumberRestriction).C, lockind, "C"));
                }
                else
                {
                    if ((C as NumberRestriction).Kind == "≤" || (C as NumberRestriction).Kind == "<")
                        return AnalizeConBottom((C as NumberRestriction).C, lockind, "C");
                    else if ((C as NumberRestriction).Kind == "≥" || (C as NumberRestriction).Kind == ">")
                        return Intersect(AnalizeConTop((C as NumberRestriction).R, lockind, "R"), AnalizeConTop((C as NumberRestriction).C, lockind, "C"));
                    else
                        return Cumulate(AnalizeConBottom((C as NumberRestriction).C, lockind, "C"), Intersect(AnalizeConTop((C as NumberRestriction).R, lockind, "R"), AnalizeConTop((C as NumberRestriction).C, lockind, "C")));
                }
            }
            return CreateNull();
        }

    }
}