using System;
using System.Linq;
using System.Collections.Generic;
using System.Text;
using Ontorion.CNL.DL;

namespace Ontorion.CNL.EN
{
    public class ENSerializeException : Exception
    {

        public ENSerializeException(Tools.SYMBOL node, string message)
            : base(message)
        {
            //    base(message/* + (node!=null?" in :"+node.toString():"")*/);
            //    this.node = node;
        }
    }
    public class Serializer2 : Ontorion.CNL.EN.IVisitor
    {
        VisitingParam<bool> useBrack = new VisitingParam<bool>(false);
        VisitingParam<bool> isModal = new VisitingParam<bool>(false);
        VisitingParam<bool> isPlural = new VisitingParam<bool>(false);
        public AnnotationManager annotMan = new AnnotationManager();

        public string Serialize(paragraph p) 
        {
            if(annotMan.GetAnnotationSubjects().Count > 0)
                annotMan.clearAnnotations();
            var r = p.accept(this) as string;
            return r;
        }
        public string Serialize(sentence s) 
        {
            if (annotMan.GetAnnotationSubjects().Count > 0)
                annotMan.clearAnnotations();
            var r = s.accept(this) as string;
            if (!(s is dlannotationassertion))
                r = EnsureBigStart(r);
            return r;
        }
        public string Serialize(orloop p)
        {
            if (annotMan.GetAnnotationSubjects().Count > 0)
                annotMan.clearAnnotations();
            var r = p.accept(this) as string;
            return r;
        }
        public string Serialize(boundFacets p)
        {
            if (annotMan.GetAnnotationSubjects().Count > 0)
                annotMan.clearAnnotations();
            var r = p.accept(this) as string;
            return r;
        }

        public string Serialize(boundTop p)
        {
            if (annotMan.GetAnnotationSubjects().Count > 0)
                annotMan.clearAnnotations();
            var r = p.accept(this) as string;
            return r;
        }
        public string Serialize(boundTotal p)
        {
            if (annotMan.GetAnnotationSubjects().Count > 0)
                annotMan.clearAnnotations();
            var r = p.accept(this) as string;
            return r;
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

        private string EnsureBigStart(string snt)
        {
            if (snt.Length > 0)
                return char.ToUpper(snt[0]) + (snt.Length > 1 ? snt.Substring(1) : "");
            else
                return snt;
        }

        public object Visit(paragraph p) 
        {
            StringBuilder sb = new StringBuilder();
            foreach (var x in p.sentences)
            {
                var str = x.accept(this) as string;
                if (SerializeAnnotations && x is annotation && str.StartsWith("Annotations:"))
                    annotMan.loadW3CAnnotationsFromText(str, true);

                if (!(x is dlannotationassertion || (x is annotation && str.StartsWith("Annotations:"))))
                    sb.AppendLine(EnsureBigStart(str));
            }

            if(SerializeAnnotations)
                sb.Append(annotMan.SerializeAnnotations());

            return sb.ToString();
        }

        public string Modality(string tok)
        {
            switch (tok)
            {
                case "□": return "must";
                case "◊": return "should";
                case "◊◊": return "can";
                case "~◊◊": return "must-not";
                case "~◊": return "should-not";
                case "~□": return "can-not";
                default:
                    return null;
            }
        }

        public string Modality2(string tok)
        {
            var r = Modality(tok);
            if (r == null)
                return null;
            else
                return KeyWords.Me.Get("IT") + " " + r + " " + KeyWords.Me.Get("BETRUETHAT");
        }

        public object Visit(subsumption p) 
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append(p.c.accept(this));
            sb.Append(" ");
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(modal);
                    sb.Append(" ");
                    sb.Append(p.d.accept(this));
                }
                else
                    sb.Append(p.d.accept(this));
            }
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }

        public object Visit(nosubsumption p)
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append(p.c.accept(this));
            sb.Append(" ");
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(modal);
                    sb.Append(" ");
                    sb.Append(p.d.accept(this));
                }
                else
                    sb.Append(p.d.accept(this));
            }
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }

        public object Visit(equivalence2 p)
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append(KeyWords.Me.Get("SOMETHING"));
            sb.Append(" ");
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(modal);
                    sb.Append(" ");
                    sb.Append(p.c.accept(this));
                }
                else
                    sb.Append(p.c.accept(this));
            }
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IFANDONLYIFIT"));
            sb.Append(" ");
            sb.Append(p.d.accept(this));
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }
        
        //public object Visit(equivalence_def p)
        //{
        //    StringBuilder sb = new StringBuilder();
        //    string modal = Modality(p.modality);
        //    sb.Append(p.c.accept(this));
        //    sb.Append(" ");
        //    using (isModal.set(modal != null))
        //    {
        //        if (modal != null)
        //        {
        //            sb.Append(modal);
        //            sb.Append(" ");
        //            sb.Append(p.d.accept(this));
        //        }
        //        else
        //            sb.Append(p.d.accept(this));
        //    }
        //    sb.Append(KeyWords.Me.Get("END"));
        //    return sb.ToString();
        //}

        public object Visit(subsumption_if p)
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append(KeyWords.Me.Get("IF"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("SOMETHING"));
            sb.Append(" ");
            sb.Append(p.c.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("THEN"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IT"));
            sb.Append(" ");
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(modal);
                    sb.Append(" ");
                    sb.Append(p.d.accept(this));
                }
                else
                    sb.Append(p.d.accept(this));
            }
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }

        public object Visit(datatypedef p)
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append(KeyWords.Me.Get("EVERY"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("VALUEOF"));
            sb.Append(" ");
            sb.Append(p.name);
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IS"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("SOMETHING"));
            sb.Append(" ");
            sb.Append(p.db.accept(this));
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }

        public object Visit(exclusives p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("anything");
            string modal = Modality(p.modality);
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(" ");
                    sb.Append(modal);
                }
                sb.Append(" ");
                sb.Append(KeyWords.Me.Get("EITHER"));
                for (int i = 0; i < p.objectRoleExprs.Count; i++)
                {
                    using (useBrack.setIf(i < p.objectRoleExprs.Count - 1, true))
                    {
                        var e = p.objectRoleExprs[i];
                        if (i == 0)
                        {
                            sb.Append(" ");
                            sb.Append(e.accept(this));
                        }
                        else
                        {
                            if (i == p.objectRoleExprs.Count - 1)
                                sb.Append(" or");
                            else
                                sb.Append(",");
                            sb.Append(" ");
                            sb.Append(e.accept(this));
                        }
                    }
                }
            }
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("ORSOMETHINGELSE"));
            sb.Append(".");
            return sb.ToString();
        }

        public object Visit(exclusiveunion p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("something");
            sb.Append(" ");
            string modal = Modality(p.modality);
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(" ");
                    sb.Append(modal);
                    sb.Append(" ");
                }
                sb.Append(isBeAre());
            }
            sb.Append(" ");
            sb.Append(addAOrAn(p.name));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IFANDONLYIFITEITHER"));
            for (int i = 0; i < p.objectRoleExprs.Count; i++)
            {
                var e = p.objectRoleExprs[i];
                using (useBrack.setIf(i < p.objectRoleExprs.Count - 1, true))
                {
                    if (i == 0)
                    {
                        sb.Append(" ");
                        sb.Append(e.accept(this));
                    }
                    else
                    {
                        if (i == p.objectRoleExprs.Count - 1)
                            sb.Append(" or");
                        else
                            sb.Append(",");
                        sb.Append(" ");
                        sb.Append(e.accept(this));
                    }
                }
            }
            sb.Append(".");
            return sb.ToString();
        }
        
        public object Visit(rolesubsumption p)
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append("If");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            bool first = true;
            foreach (var x in p.subChain)
            {
                if (first)
                    first = false;
                else
                    sb.Append(" something that ");
                sb.Append(x.accept(this));
            }
            sb.Append(" ");
            sb.Append("Y");
            sb.Append(" ");
            sb.Append("then");
            sb.Append(" ");
            sb.Append(p.superRole.accept(this));
            sb.Append(".");
            return sb.ToString();
        }

        //public object Visit(roleequivalence p)
        //{
        //    StringBuilder sb = new StringBuilder();
        //    bool first = true;
        //    foreach (var e in p.equals)
        //    {
        //        if (first)
        //        {
        //            sb.Append(e.accept(this));
        //            first = false;
        //        }
        //        else
        //        {
        //            if (p.equals.IndexOf(e) == p.equals.Count - 1)
        //                sb.Append(" and");
        //            else
        //                sb.Append(",");
        //            sb.Append(" ");
        //            sb.Append(e.accept(this));
        //        }
        //    }

        //    sb.Append(" ");

        //    string modal = Modality(p.modality);
        //    using (isModal.set(modal != null))
        //    {
        //        if (modal != null)
        //        {
        //            sb.Append(modal);
        //            sb.Append(" ");
        //        }
        //    }
        //    sb.Append("means-the-same");
        //    sb.Append(".");
        //    return sb.ToString();
        //}

        public object Visit(roleequivalence2 p)
        {
            StringBuilder sb = new StringBuilder();
            //bool first = true;

            sb.Append("X");
            sb.Append(" ");
            sb.Append(p.r.accept(this));
            sb.Append(" ");
            sb.Append("Y");
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IFANDONLYIF"));
            sb.Append(" ");
            sb.Append(p.s.accept(this));
            sb.Append(".");
            return sb.ToString();
        }
        
        //public object Visit(roledisjoint p)
        //{
        //    StringBuilder sb = new StringBuilder();
        //    bool first = true;
        //    foreach (var e in p.different)
        //    {
        //        if (first)
        //        {
        //            sb.Append(e.accept(this));
        //            first = false;
        //        }
        //        else
        //        {
        //            if (p.different.IndexOf(e) == p.different.Count - 1)
        //                sb.Append(" and");
        //            else
        //                sb.Append(",");
        //            sb.Append(" ");
        //            sb.Append(e.accept(this));
        //        }
        //    }

        //    sb.Append(" ");

        //    string modal = Modality(p.modality);
        //    using (isModal.set(modal != null))
        //    {
        //        if (modal != null)
        //        {
        //            sb.Append(modal);
        //            sb.Append(" ");
        //        }
        //        using (isPlural.set(true))
        //        {
        //            sb.Append(isBeAre());
        //        }
        //    }
        //    sb.Append(" ");
        //    sb.Append("different");
        //    sb.Append(".");
        //    return sb.ToString();
        //}

        public object Visit(roledisjoint2 p)
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("If");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            sb.Append(p.r.accept(this));
            sb.Append(" ");
            sb.Append("Y");
            sb.Append(" ");
            sb.Append("then");
            sb.Append(" ");
            sb.Append(p.s.accept(this));
            sb.Append(".");
            return sb.ToString();
        }
        
        public object Visit(datarolesubsumption p)
        {
            StringBuilder sb = new StringBuilder();
            string modal = Modality(p.modality);
            sb.Append("If");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            sb.Append(p.subRole.accept(this));
            sb.Append(" ");
            sb.Append("equal-to Y");
            sb.Append(" ");
            sb.Append("then");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            using (isModal.set(modal != null))
            {
                if (modal != null)
                {
                    sb.Append(modal);
                    sb.Append(" ");
                    sb.Append(p.superRole.accept(this));
                }
                else
                    sb.Append(p.superRole.accept(this));
            }
            sb.Append(" ");
            sb.Append("equal-to Y");
            sb.Append(".");
            return sb.ToString();
        }

        //public object Visit(dataroleequivalence p)
        //{
        //    StringBuilder sb = new StringBuilder();
        //    bool first = true;
        //    foreach (var e in p.equals)
        //    {
        //        if (first)
        //        {
        //            sb.Append("X");
        //            sb.Append(" ");
        //            sb.Append(e.accept(this));
        //            sb.Append(" ");
        //            sb.Append("value");
        //            first = false;
        //        }
        //        else
        //        {
        //            if (p.equals.IndexOf(e) == p.equals.Count - 1)
        //                sb.Append(" and");
        //            else
        //                sb.Append(",");
        //            sb.Append(" ");
        //            sb.Append("X");
        //            sb.Append(" ");
        //            sb.Append(e.accept(this));
        //            sb.Append(" ");
        //            sb.Append("value");
        //        }
        //    }

        //    sb.Append(" ");

        //    string modal = Modality(p.modality);
        //    using (isModal.set(modal != null))
        //    {
        //        if (modal != null)
        //        {
        //            sb.Append(modal);
        //            sb.Append(" ");
        //        }
        //    }
        //    sb.Append("means-the-same");
        //    sb.Append(".");
        //    return sb.ToString();
        //}

        public object Visit(dataroleequivalence2 p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("X");
            sb.Append(" ");
            sb.Append(p.r.accept(this));
            sb.Append(" ");
            sb.Append("equal-to Y");
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IFANDONLYIF"));
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            sb.Append(p.s.accept(this));
            sb.Append(" ");
            sb.Append("equal-to Y");
            sb.Append(".");
            return sb.ToString();
        }
        
        //public object Visit(dataroledisjoint p)
        //{
        //    StringBuilder sb = new StringBuilder();
        //    bool first = true;
        //    foreach (var e in p.different)
        //    {
        //        if (first)
        //        {
        //            sb.Append("X");
        //            sb.Append(" ");
        //            sb.Append(e.accept(this));
        //            sb.Append(" ");
        //            sb.Append("value");
        //            first = false;
        //        }
        //        else
        //        {
        //            if (p.different.IndexOf(e) == p.different.Count - 1)
        //                sb.Append(" and");
        //            else
        //                sb.Append(",");
        //            sb.Append(" ");
        //            sb.Append("X");
        //            sb.Append(" ");
        //            sb.Append(e.accept(this));
        //            sb.Append(" ");
        //            sb.Append("value");
        //        }
        //    }

        //    sb.Append(" ");

        //    string modal = Modality(p.modality);
        //    using (isModal.set(modal != null))
        //    {
        //        if (modal != null)
        //        {
        //            sb.Append(modal);
        //            sb.Append(" ");
        //        }
        //        using (isPlural.set(true))
        //        {
        //            sb.Append(isBeAre());
        //        }
        //    }
        //    sb.Append(" ");
        //    sb.Append("different");
        //    sb.Append(".");
        //    return sb.ToString();
        //}

        public object Visit(dataroledisjoint2 p)
        {
            StringBuilder sb = new StringBuilder();

            sb.Append("If");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            sb.Append(p.r.accept(this));
            sb.Append(" ");
            sb.Append("equal-to Y");
            sb.Append(" ");
            sb.Append("then");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            sb.Append("does-not");
            sb.Append(" ");
            sb.Append(p.s.accept(this));
            sb.Append(" ");
            sb.Append("equal-to Y");
            sb.Append(".");
            return sb.ToString();
        }
        
        public object Visit(haskey p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("Every");
            sb.Append(" ");
            sb.Append("X");
            sb.Append(" ");
            sb.Append("that");
            sb.Append(" ");
            sb.Append(p.s.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("ISUNIQUEIF"));
            sb.Append(" ");
            bool first = true;
            {
                foreach (var e in p.roles)
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        sb.Append(" ");
                        sb.Append("and");
                        sb.Append(" ");
                    }
                    sb.Append("X");
                    sb.Append(" ");
                    sb.Append(e.accept(this));
                    sb.Append(" ");
                    sb.Append("something");
                }
            }
            {
                foreach (var e in p.dataroles)
                {
                    if (first)
                    {
                        first = false;
                    }
                    else
                    {
                        sb.Append(" ");
                        sb.Append("and");
                        sb.Append(" ");
                    }
                    sb.Append("X");
                    sb.Append(" ");
                    sb.Append(e.accept(this));
                    sb.Append(" equal-to ");
                    sb.Append("something");
                }
            }
            sb.Append(".");
            return sb.ToString();
        }
        public object Visit(subjectEvery p) 
        {
            return KeyWords.Me.Get("EVERY") + " " + p.s.accept(this);
        }
        public object Visit(subjectEverything p)
        {
            return KeyWords.Me.Get("EVERYTHING") + (p.t != null ? (" " + p.t.accept(this)) : "");
        }
        public object Visit(subjectNo p) 
        {
            return KeyWords.Me.Get("NO") + " " + p.s.accept(this);
        }
        public object Visit(subjectNothing p)
        {
            return KeyWords.Me.Get("NOTHING");
        }
        public object Visit(subjectBigName p)
        {
            return bigname(p.name);
        }
        public object Visit(subjectThe p)
        {
            if(p.only)
                return KeyWords.Me.Get("THEONEANDONLY") + " " + p.s.accept(this);
            else
                return KeyWords.Me.Get("THE") + " " + p.s.accept(this);
        }

        public string isBeAre()
        {
            if (isModal.get())
                return "be";
            else if(isPlural.get())
                return "are";
            else
                return "is";
        }

        public object Visit(objectRoleExpr1 p)
        {
            StringBuilder sb= new StringBuilder();
            sb.Append(isBeAre());
            if (p.Negated)
            {
                sb.Append(" ");
                sb.Append("not");
            }
            sb.Append(" ");
            sb.Append(p.s.accept(this));
            return sb.ToString();
        }
        public object Visit(roleWithXY p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.inverse ? "Y" : "X");
            sb.Append(" ");
            sb.Append(p.name);
            sb.Append(" ");
            sb.Append(p.inverse ? "X" : "Y");
            return sb.ToString();
        }
        public object Visit(notRoleWithXY p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.inverse ? "Y" : "X");
            sb.Append(" does-not ");
            sb.Append(p.name);
            sb.Append(" ");
            sb.Append(p.inverse ? "X" : "Y");
            return sb.ToString();
        }
        public object Visit(role p)
        {
            if (!p.inverse)
                return p.name;
            else
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(isBeAre());
                sb.Append(" ");
                sb.Append(p.name);
                sb.Append(" ");
                sb.Append("by");
                return sb.ToString();
            }
        }
        public object Visit(objectRoleExpr2 p)
        {
            StringBuilder sb = new StringBuilder();
            if (p.Negated)
            {
                if (!p.r.inverse)
                {
                    if (isModal.get())
                        sb.Append("do-not");
                    else
                        sb.Append("does-not");
                    sb.Append(" ");
                    sb.Append(p.r.accept(this));
                }
                else
                {
                    if (isModal.get())
                        sb.Append("be-not");
                    else if (isPlural.get())
                        sb.Append("are-not");
                    else
                        sb.Append("is-not");
                    sb.Append(" ");
                    sb.Append(p.r.name);
                    sb.Append(" ");
                    sb.Append("by");
                }
            }
            else
                sb.Append(p.r.accept(this));
            if (p.s != null)
            {
                sb.Append(" ");
                using (isModal.set(p.Negated))
                {
                    sb.Append(p.s.accept(this));
                }
            }
            return sb.ToString();
        }

        public object Visit(objectRoleExpr3 p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.r.accept(this));
            sb.Append(" ");
            if (p.r.name==null || !p.r.name.StartsWith("has-"))
                sb.Append("something ");
            sb.Append(p.t.accept(this));
            return sb.ToString();
        }
        
        public object Visit(oobjectA p)
        {
            using (isModal.set(false))
            {
                string nm=p.s.accept(this) as string;
                if (p.s is singleOneOf)
                    return nm;
                else
                    return addAOrAn(nm);
            }
        }

        string addAOrAn(string nm)
        {
            if (nm.StartsWith("(") || nm.StartsWith("that ")) return nm;
            else
                return (isPlural.get() ? "" : ((nm.StartsWith("a") || nm.StartsWith("e") || nm.StartsWith("i") || nm.StartsWith("o")) ? "an" : "a") + " ") + nm;
        }

        public object Visit(instanceThe p)
        {
            using (isModal.set(false))
            {
                return (p.only?KeyWords.Me.Get("THEONEANDONLY"):KeyWords.Me.Get("THE"))+" " + p.s.accept(this);
            }
        }
        public object Visit(instanceBigName p)
        {
            return bigname(p.name);
        }
        public object Visit(oobjectInstance p)
        {
            using (isModal.set(false))
            {
                return p.i.accept(this);
            }
        }

        public object Visit(oobjectOnly p)
        {
            using (isModal.set(false))
            {
                using (isPlural.set(true))
                    return "nothing-but" + " " + p.s.accept(this);
            }
        }

        public object Visit(oobjectOnlyInstance p)
        {
            using (isModal.set(false))
            {
                return "nothing-but" + " " + p.i.accept(this);
            }
        }
        
        private string word_number(string wcnt)
        {
            long cnt = long.Parse(wcnt);
            switch (cnt)
            {
                case 0: return "zero";
                case 1: return "one";
                case 2: return "two";
                case 3: return "three";
                case 4: return "four";
                case 5: return "five";
                case 6: return "six";
                case 7: return "seven";
                case 8: return "eight";
                case 9: return "nine";
                default: return cnt.ToString();
            }
        }

        private string comparer(string str)
        {
            if (str.Contains("<>"))
                return "different-than ";
            else if (str.Contains("<"))
                return "less-than ";
            else if (str.Contains(">"))
                return "more-than ";
            else if (str.Contains("="))
                return "";
            else if (str.Contains("≤"))
                return "at-most ";
            else if (str.Contains("≥"))
                return "at-least ";
            else if (str.Contains("≠"))
                return "different-than ";
            else
                return null;
        }

        private string comparer2(string str)
        {
            if (str.StartsWith("<->"))
                return "that-has-length " + ((str.Length > "<->".Length) ? (comparer2(str.Substring("<->".Length + 1))) : "");
            else if (str.Contains("<>"))
                return "different-from ";
            else if (str.Contains("<"))
                return "lower-than ";
            else if (str.Contains(">"))
                return "greater-than ";
            else if (str.Contains("="))
                return "equal-to ";
            else if (str.Contains("≤"))
                return "lower-or-equal-to ";
            else if (str.Contains("≥"))
                return "greater-or-equal-to ";
            else if (str.Contains("≠"))
                return "different-from ";
            else if (str.Contains("#"))
                return "that-matches-pattern ";
            else
                throw new InvalidOperationException("Unknown Facet in Grammar");
        }
        
        public object Visit(oobjectCmp p)
        {
            using (isModal.set(false))
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(comparer(p.Cmp));
                sb.Append(word_number(p.Cnt));
                sb.Append(" ");
                using (isPlural.set(long.Parse(p.Cnt) != 1))
                    sb.Append(p.s.accept(this));
                return sb.ToString();
            }
        }
        public object Visit(oobjectCmpInstance p)
        {
            using (isModal.set(false))
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(comparer(p.Cmp));
                sb.Append(word_number(p.Cnt));
                sb.Append(" ");
                using (isPlural.set(long.Parse(p.Cnt) != 1))
                    sb.Append(p.i.accept(this));
                return sb.ToString();
            }
        }

        public object Visit(oobjectBnd p)
        {
            return p.b.accept(this);
        }
        public object Visit(oobjectOnlyBnd p)
        {
            return "nothing-but" + " " + p.b.accept(this);
        }
        public object Visit(oobjectCmpBnd p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(comparer(p.Cmp));
            sb.Append(word_number(p.Cnt));
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            return sb.ToString();
        }

        public object Visit(oobjectSelf p)
        {
            return "itself";
        }
        public object Visit(oobjectSomething p)
        {
            return "something";
        }
        public object Visit(oobjectNothing p)
        {
            return "nothing";
        }
        public object Visit(oobjectOnlyNothing p)
        {
            return "none";
        }
        public object Visit(oobjectSomethingThat p)
        {
            using (isModal.set(false))
            {
                return "something" + " " + p.t.accept(this);
            }
        }
        public object Visit(oobjectOnlySomethingThat p)
        {
            using (isModal.set(false))
            {
                return "nothing-but" + " " + "something" + " " + p.t.accept(this);
            }
        }

        string name(string str)
        {
                return str;
        }

        string bigname(string str)
        {
                return str;
        }

        string thing()
        {
            if (isPlural.get())
                return "things";
            else 
                return "thing";
        }

        public object Visit(singleName p)
        {
            return name(p.name);
        }
        public object Visit(singleThing p)
        {
            return thing();
        }
        public object Visit(singleNameThat p)
        {
            string str = name(p.name) + " ";
            using (isModal.set(false))
            {
                return str + p.t.accept(this);
            }
        }
        public object Visit(singleThingThat p)
        {
            string str = thing() + " ";
            using (isModal.set(false))
            {
                return str + p.t.accept(this);
            }
        }

        public object Visit(thatOrLoop p)
        {
            var snt = p.o.accept(this);
            var th = "that" + " " + snt;
            if (useBrack.get())
                th = "(" + th + ")";
            return th;
        }
        public object Visit(singleOneOf p)
        {
            using (isModal.set(false))
            {
                StringBuilder sb = new StringBuilder();
                sb.Append("either ");
                using (isPlural.set(false))
                {
                    bool first = true;
                    foreach (var i in p.insts)
                    {
                        if (first)
                            first = false;
                        else
                        {
                            if (p.insts.IndexOf(i) == p.insts.Count - 1)
                                sb.Append(" or");
                            else
                                sb.Append(",");
                            sb.Append(" ");
                        }
                        sb.Append(i.accept(this));
                    }
                }
                return useBrack.get() ? "(" + sb.ToString() + ")" : sb.ToString();
            }
        }
        public object Visit(andloop p)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < p.exprs.Count; i++)
            {
                var e = p.exprs[i];
                if (i > 0)
                    sb.Append(" and ");
                using (useBrack.setIf(i < p.exprs.Count - 1, true))
                    sb.Append(e.accept(this));
            }
            return sb.ToString();
        }
        public object Visit(orloop p)
        {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < p.exprs.Count; i++)
            {
                var e = p.exprs[i];
                if (i > 0)
                    sb.Append(" and-or ");
                using (useBrack.setIf(i < p.exprs.Count - 1, true))
                    sb.Append(e.accept(this));
            }
            return sb.ToString();
        }

        public object Visit(facet p)
        {
            return comparer2(p.Cmp) + p.V.accept(this);
        }

        public object Visit(boundVal p)
        {
            return comparer2(p.Cmp) + p.V.accept(this);
        }

        public object Visit(facetList p)
        {
            bool multi = p.Facets.Count > 1;
            StringBuilder sb = new StringBuilder();

            if (multi)
                sb.Append(KeyWords.Me.Get("OPEN"));
            bool first = true;
            foreach (var f in p.Facets)
            {
                if (first)
                    first = false;
                else
                {
                    sb.Append(KeyWords.Me.Get("COMMA"));
                    sb.Append(" ");
                }
                sb.Append(f.accept(this));
            }
            if (multi)
                sb.Append(KeyWords.Me.Get("CLOSE"));
            return sb.ToString();
        }
        
        public object Visit(boundFacets p)
        {
            return p.l.accept(this);
        }

        public object Visit(boundNot p)
        {
            return "not " + brack(p, p.bnd);
        }

        public object Visit(boundAnd p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("(");
            for (int i = 0; i < p.List.Count; i++)
            {
                var e = p.List[i];
                if (i > 0)
                {
                    sb.Append(" ");
                    sb.Append(KeyWords.Me.Get("ASWELLAS"));
                    sb.Append(" ");
                }
                sb.Append(brack(p, e));
            }
            sb.Append(")");
            return sb.ToString();
        }

        string brack(abstractbound parent, abstractbound child)
        {
            if (child.priority() == 0 || (child.priority() >= parent.priority()))
            {
                return child.accept(this) as string;
            }
            else
            {
                return "(" + child.accept(this) as string + ")";
            }
        }

        public object Visit(boundOr p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("(");
            for (int i = 0; i < p.List.Count; i++)
            {
                var e = p.List[i];
                if (i > 0)
                    sb.Append(" or ");
                sb.Append(brack(p,e));
            }
            sb.Append(")");
            return sb.ToString();
        }
        
        public object Visit(boundTop p)
        {
            return "(some value)";
        }
        public object Visit(boundTotal p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("(some");
            sb.Append(" ");
            switch (p.Kind)
            {
                case "NUM":
                    sb.Append("integer");
                    break;
                case "BOL":
                    sb.Append("boolean");
                    break;
                case "DBL":
                    sb.Append("real");
                    break;
                case "DTM":
                    sb.Append("datetime");
                    break;
                case "DUR":
                    sb.Append("duration");
                    break;
                case "STR":
                    sb.Append("string");
                    break;
                default:
                    Assert(false);
                    break;
            }
            sb.Append(" ");
            sb.Append("value)");
            return sb.ToString();
        }

        public object Visit(boundDataType p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("(some");
            sb.Append(" ");
            sb.Append(p.name);
            sb.Append(" ");
            sb.Append("value)");
            return sb.ToString();
        }

        public object Visit(boundOneOf p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("either");
            bool First = true;
            foreach (var val in p.vals)
            {
                if (First)
                {
                    First = false;
                    sb.Append(" ");
                }
                else
                {
                    if (p.vals.IndexOf(val) == p.vals.Count - 1)
                        sb.Append(" or");
                    else
                        sb.Append(",");
                    sb.Append(" ");
                }
                sb.Append(val.accept(this).ToString());
            }
            return "(" + sb.ToString() + ")";
        }
        public object Visit(Number p)
        {
            return p.getVal();
        }
        public object Visit(Bool p)
        {
            return p.getVal();
        }
        public object Visit(StrData p)
        {
            return p.getVal();
        }
        public object Visit(DateTimeData p)
        {
            return p.getVal();
        }
        public object Visit(Duration p)
        {
            return p.getVal();
        }
        public object Visit(Float p)
        {
            return p.getVal();
        }

        public object Visit(annotation p)
        {
            return p.txt;
        }

        public object Visit(dlannotationassertion p)
        {
            var w3cAnnot = new W3CAnnotation(true) { Type = p.annotName, Value = p.value, Language = p.language };
            annotMan.appendAnnotations(p.subject, p.subjKind, new List<W3CAnnotation>(){w3cAnnot});
            return "Annotations:" + p.subject.Replace(".", "..") + " " + p.subjKind.Replace(".", "..") + ": " + w3cAnnot.ToString().Replace(".", "..") + ".";
        }

        public object Visit(swrlrule p)
        {
            string modal = Modality2(p.modality);

            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("IF"));
            sb.Append(" ");
            inRuleBody = true;
            sb.Append(p.Predicate.accept(this));
            inRuleBody = false;
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("THEN"));
            sb.Append(" ");
            if (modal != null)
            {
                sb.Append(modal);
                sb.Append(" ");
            }
            inModalSwrl = (modal != null);
            sb.Append(p.Result.accept(this));
            inModalSwrl = false;
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();          
        }

        public object Visit(swrlrulefor p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("IF"));
            sb.Append(" ");
            inRuleBody = true;
            sb.Append(p.Predicate.accept(this));
            inRuleBody = false;
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("THEN"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("FOR"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("EVERY"));
            sb.Append(" ");
            var frs = p.Collection.exevars.First();

            if (frs is datavalvar)
            {
                sb.Append(KeyWords.Me.Get("VALUE"));
                sb.Append(KeyWords.Me.Get("OPEN"));
                sb.Append((frs as datavalvar).num);
                sb.Append(KeyWords.Me.Get("CLOSE"));
            }
            else if (frs is identobject_name)
            {
                if ((frs as identobject_name).name == null)
                {
                    sb.Append(KeyWords.Me.Get("THING"));
                    sb.Append(KeyWords.Me.Get("OPEN"));
                    sb.Append((frs as identobject_name).num);
                    sb.Append(KeyWords.Me.Get("CLOSE"));
                }
                else
                    throw new NotImplementedException();
            }
            else
                throw new NotImplementedException();

            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("FROM"));
            sb.Append(" ");
            sb.Append(p.Collection.exevars.Last().accept(this));
            sb.Append(" ");
            sb.Append(p.Result.accept(this));
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }

        public object Visit(clause p)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var x in p.Conditions)
            {
                if (first) first = false;
                else
                {
                    sb.Append(" ");
                    sb.Append(KeyWords.Me.Get("AND"));
                    sb.Append(" ");
                }
                sb.Append(x.accept(this));
            }
            return sb.ToString();          
        }

        public object Visit(clause_result p)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var x in p.Conditions)
            {
                if (first) first = false;
                else
                {
                    sb.Append(" ");
                    sb.Append(KeyWords.Me.Get("AND"));
                    sb.Append(" ");
                }
                sb.Append(x.accept(this));
            }
            return sb.ToString();
        }
        
        public object Visit(condition_is p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            if (p.condition_kind == condition_kind.None)
            {
                sb.Append(KeyWords.Me.Get("ISTHESAMEAS"));
                sb.Append(" ");
            }
            else
            {
                sb.Append(KeyWords.Me.Get("ISNOTTHESAMEAS"));
                sb.Append(" ");
            }
            sb.Append(p.objectB.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_exists p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("EXISTS"));
            return sb.ToString();
        }

        public object Visit(condition_definition p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IS"));
            sb.Append(" ");
            sb.Append(p.objectClass.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_role p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            if (p.condition_kind == condition_kind.None)
                sb.Append(p.role);
            else
            {
                sb.Append(KeyWords.Me.Get("IS"));
                sb.Append(" ");
                sb.Append(p.role);
                sb.Append(" ");
                sb.Append(KeyWords.Me.Get("BY"));
            }
            sb.Append(" ");
            sb.Append(p.objectB.accept(this));
            return sb.ToString();
        }
        
        public object Visit(condition_data_property p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            sb.Append(p.property_name);
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("EQUALTO"));
            sb.Append(" ");
            sb.Append(p.d_object.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_data_property_bound p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            sb.Append(p.property_name);
            sb.Append(" ");
            sb.Append(p.bnd.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_data_bound p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.d_object.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IS"));
            sb.Append(" ");
            sb.Append(p.bound.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_result_is p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            if (p.condition_kind == condition_kind.None)
            {
                sb.Append(KeyWords.Me.Get("ISTHESAMEAS"));
                sb.Append(" ");
            }
            else
            {
                sb.Append(KeyWords.Me.Get("ISNOTTHESAMEAS"));
                sb.Append(" ");
            }
            sb.Append(p.objectB.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_result_definition p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IS"));
            sb.Append(" ");
            sb.Append(p.objectClass.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_result_role p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            if (p.condition_kind == condition_kind.None)
                sb.Append(p.role);
            else
            {
                sb.Append(KeyWords.Me.Get("IS"));
                sb.Append(" ");
                sb.Append(p.role);
                sb.Append(" ");
                sb.Append(KeyWords.Me.Get("BY"));
            }
            sb.Append(" ");
            sb.Append(p.objectB.accept(this));
            return sb.ToString();
        }

        public object Visit(condition_result_data_property p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.objectA.accept(this));
            sb.Append(" ");
            sb.Append(p.property_name);
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("EQUALTO"));
            sb.Append(" ");
            sb.Append(p.d_object.accept(this));
            return sb.ToString();
        }

        public object Visit(objectr_nio p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.notidentobject.accept(this));
            return sb.ToString();
        }

        public object Visit(objectr_io p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.identobject.accept(this));
            return sb.ToString();
        }

        public string a_name(string nm)
        {
           return ((nm.StartsWith("a") || nm.StartsWith("e") || nm.StartsWith("i") || nm.StartsWith("o") || nm.StartsWith("u")) ? "an" : "a") + " " + nm;       
        }

        HashSet<string> bodyVars = new HashSet<string>();

        public object Visit(notidentobject p)
        {
            if (TemplateMode)
            {
                var k = "name_" + p.name + "_" + (p.num != null ? "(" + p.num + ")" : "");
                if (inRuleBody || bodyVars.Contains(k))
                {
                    if (!templateVars.ContainsKey(k))
                        templateVars.Add(k, templateIdx++);

                    if (inRuleBody)
                        bodyVars.Add(k);

                    return "{" + templateVars[k] + "}";
                }
            } 
            
            StringBuilder sb = new StringBuilder();
            sb.Append((p.name == null ? "a thing" : a_name(p.name)) + (p.num != null ? "(" + p.num + ")" : ""));
            return sb.ToString();
        }

        public object Visit(identobject_name p)
        {
            if (TemplateMode)
            {
                var k = "name_" + p.name + "_" + (p.num != null ? "(" + p.num + ")" : "");
                if (inRuleBody || bodyVars.Contains(k))
                {
                    if (!templateVars.ContainsKey(k))
                        templateVars.Add(k, templateIdx++);

                    return "{" + templateVars[k] + "}";
                }
            }
            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("THE"));
            sb.Append(" ");
            sb.Append(p.name ?? "thing");
            if (p.num != null)
                sb.Append("(" + p.num + ")");
            return sb.ToString();
        }

        public object Visit(identobject_inst p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.i.accept(this));
            return sb.ToString();
        }

        public object Visit(instancer p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(bigname(p.name));
            return sb.ToString(); 
        }

        public object Visit(datavalval p)
        {
            return p.dv.accept(this).ToString();
        }

        int templateIdx = 0;
        Dictionary<string, int> templateVars = new Dictionary<string, int>();

        public object Visit(datavalvar p)
        {
            if (TemplateMode)
            {
                var k = "value" + p.num.ToString();
                if (!inModalSwrl || templateVars.ContainsKey(k))
                {
                    if (!templateVars.ContainsKey(k))
                        templateVars.Add(k, templateIdx++);

                    return "{" + templateVars[k] + "}";
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("THE"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("VALUE"));
            sb.Append(KeyWords.Me.Get("OPEN"));
            sb.Append(p.num);
            sb.Append(KeyWords.Me.Get("CLOSE"));
            return sb.ToString();
        }

        public object Visit(condition_builtin p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.bi.accept(this));
            return sb.ToString();
        }
        public object Visit(condition_result_builtin p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.bi.accept(this));
            return sb.ToString();
        }
        public object Visit(builtin_cmp p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.a.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("IS"));
            sb.Append(" ");
            sb.Append(comparer2(p.cmp));
            sb.Append(p.b.accept(this));
            return sb.ToString();
        }

        string equals()
        {
            return KeyWords.Me.Get("IS") + " " + KeyWords.Me.Get("EQUALTO");
        }

        public object Visit(builtin_list p)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var l in p.vals)
            {
                if (first)
                    first = false;
                else
                {
                    sb.Append(" ");
                    sb.Append(p.tpy);
                    sb.Append(" ");
                }
                sb.Append(l.accept(this));
            }
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.result.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_bin p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.b.accept(this));
            sb.Append(" ");
            sb.Append(p.tpy);
            sb.Append(" ");
            sb.Append(p.d.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.result.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_unary_cmp p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.tpy);
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.result.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_unary_free p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.a.accept(this));
            sb.Append(" ");
            sb.Append(p.tpy);
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_substr p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("SUBSTRING"));
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            sb.Append(" ");
            sb.Append(p.tpy);
            sb.Append(" ");
            sb.Append(p.c.accept(this));
            if(p.d!=null)
            {
                sb.Append(KeyWords.Me.Get("THATHASLENGTH"));
                sb.Append(" ");
                sb.Append(p.d.accept(this));
            }
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.result.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_trans p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.tpy);
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("FROM"));
            sb.Append(" ");
            sb.Append(p.c.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("WITH"));
            sb.Append(" ");
            sb.Append(p.d.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.result.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_duration p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.d.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.a.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_datetime p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(p.d.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.a.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_alpha p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("THE"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("ALPHA"));
            sb.Append(" ");
            sb.Append(p.a.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_annot p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("ANNOTATION"));
            sb.Append(" ");
            sb.Append(p.prop.accept(this));
            sb.Append(" ");
            sb.Append(p.lang.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("FROM"));
            sb.Append(" ");
            sb.Append(p.a.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.b.accept(this));
            return sb.ToString();
        }

        public object Visit(builtin_exe p)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("RESULTOF"));
            sb.Append(" ");
            sb.Append(p.name);
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("FOR"));
            sb.Append(" ");
            sb.Append(p.ea.accept(this));
            sb.Append(" ");
            sb.Append(equals());
            sb.Append(" ");
            sb.Append(p.a.accept(this));
            return sb.ToString();
        }

        static bool isZero(datavaler dv)
        {
            if (dv is datavalval)
            {
                if ((dv as datavalval).dv is Number)
                {
                    return ((dv as datavalval).dv as Number).val == "0";
                }
            }
            return false;
        }
        
        public object Visit(duration_w p)
        {
            List<datavaler> vlist = new List<datavaler>(){
                p.y,p.W,p.d,p.h,p.m,p.s
            };
            List<string> app = new List<string>(){
                "years","weeks","days","hours","minutes","seconds"
            };

            StringBuilder sb = new StringBuilder();
            int i; for (i = 0; i < vlist.Count; i++)
                if (!isZero(vlist[i]))
                    break;

            int j; for (j = vlist.Count-1; j > i; j--)
                if (!isZero(vlist[j]))
                    break;

            for (int x = i; x <= j; x++)
            {
                if (sb.Length > 0) sb.Append(" ");
                sb.Append(vlist[x].accept(this));
                sb.Append(" ");
                sb.Append(app[x]);
            }
            return sb.ToString();
        }

        public object Visit(duration_m p)
        {
            List<datavaler> vlist = new List<datavaler>(){
                p.y,p.M,p.d,p.h,p.m,p.s
            };
            List<string> app = new List<string>(){
                "years","months","days","hours","minutes","seconds"
            };

            StringBuilder sb = new StringBuilder();
            int i; for (i = 0; i < vlist.Count; i++)
                if (!isZero(vlist[i]))
                    break;

            int j; for (j = vlist.Count - 1; j > i; j--)
                if (!isZero(vlist[j]))
                    break;

            for (int x = i; x <= j; x++)
            {
                if (sb.Length > 0) sb.Append(" ");
                sb.Append(vlist[x].accept(this));
                sb.Append(" ");
                sb.Append(app[x]);
            }
            return sb.ToString();
        }

        public object Visit(datetime p)
        {
            StringBuilder sb = new StringBuilder();
            if (!isZero(p.y) || !isZero(p.M) || !isZero(p.d))
            {
                sb.Append(KeyWords.Me.Get("DATE"));
                sb.Append(" ");
                sb.Append(p.y.accept(this));
                if (sb.Length > 0) sb.Append(" - "); sb.Append(p.M.accept(this));
                if (sb.Length > 0) sb.Append(" - "); sb.Append(p.d.accept(this));
            }
            if (!isZero(p.h) || !isZero(p.m) || !isZero(p.s))
            {
                if (sb.Length > 0) sb.Append(" ");
                sb.Append(KeyWords.Me.Get("TIME"));
                sb.Append(" ");
                sb.Append(p.h.accept(this));
                if (sb.Length > 0) sb.Append(" : "); sb.Append(p.m.accept(this));
                if (sb.Length > 0) sb.Append(" : "); sb.Append(p.s.accept(this));
            }
            return sb.ToString();
        }
        
        //////////// EXE //////////////////////////////////////////////////////////////

        public object Visit(exerule p)
        {
            var sb = new StringBuilder();
            sb.Append(KeyWords.Me.Get("IF"));
            sb.Append(" ");
            sb.Append(p.slp.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("THEN"));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("FOR"));
            sb.Append(" ");
            sb.Append(p.args.accept(this));
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("EXECUTE"));
            sb.Append(" ");
            sb.Append(p.exe);
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }

        public object Visit(exeargs p)
        {
            var sb = new StringBuilder();
            bool first = true;
            foreach (var x in p.exevars)
            {
                if (first) first = false;
                else
                {
                    sb.Append(" ");
                    sb.Append(KeyWords.Me.Get("AND"));
                    sb.Append(" ");
                }
                sb.Append(x.accept(this));
            }
            return sb.ToString();    
        }


        public bool SerializeAnnotations { get; set; }

        public bool TemplateMode { get; set; }

        bool inRuleBody = false;
        bool inModalSwrl = false;

        public object Visit(code p)
        {
            var sb = new StringBuilder();
            sb.Append(p.exe);
            sb.Append(" ");
            sb.Append(KeyWords.Me.Get("END"));
            return sb.ToString();
        }
    }
}
