using Ontorion.CNL.DL;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ontorion.CNL
{
    public class SwrlRuleStruct
    {
        public List<string> head = new List<string>();
        public List<string> body = new List<string>();
    }

    public class SwrlBodyHeadExtractor : GenericVisitor
    {

        public SwrlRuleStruct GetBodyAndHead(Paragraph e)
        {
            return e.accept(this) as SwrlRuleStruct;
        }

        private List<string> allheadPart = new List<string>();
        private List<string> allbodyPart = new List<string>();
        private bool isBody = false;
        private void addToHeadOrBody(string val,string type)
        {
            if (!isBody && !allheadPart.Contains(val+type))
                    allheadPart.Add(val + type);
            else if(isBody && !allbodyPart.Contains(val + type))
                    allbodyPart.Add(val+type);
        }

        public override object Visit(Paragraph e)
        {
            var swrlStr = new SwrlRuleStruct();
            foreach (var x in e.Statements)
            {
                allheadPart.Clear();
                allbodyPart.Clear();
                x.accept(this);
                foreach (var hd in allheadPart)
                {
                    if (!swrlStr.head.Contains(hd))
                        swrlStr.head.Add(hd);
                }
                foreach (var bd in allbodyPart)
                {
                    if (!swrlStr.body.Contains(bd))
                        swrlStr.body.Add(bd);
                }
            }
            return swrlStr;
        }


        public override object Visit(SwrlStatement e)
        {
            isBody = false;
            e.slp.accept(this);
            isBody = true;
            e.slc.accept(this);
            return e;
        }

        public override object Visit(SwrlItemList e)
        {
            foreach (var i in e.list)
                i.accept(this);
            return e;
        }

        public override object Visit(SwrlInstance e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
            }
            e.I.accept(this);

            return e;
        }

        public override object Visit(SwrlRole e)
        {
            addToHeadOrBody(e.R.ToString(), ":R");
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public override object Visit(SwrlSameAs e)
        {
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public override object Visit(SwrlDifferentFrom e)
        {
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public override object Visit(SwrlDataProperty e)
        {
            using (isKindOf.set("D"))
            {
                e.IO.accept(this);
                e.DO.accept(this);
            }
            addToHeadOrBody(e.R.ToString(), ":R");
            return e;
        }

        public override object Visit(SwrlDataRange e)
        {
            using (isKindOf.set("D"))
            {
                e.B.accept(this);
                e.DO.accept(this);
            }
            return e;
        }

        public override object Visit(SwrlBuiltIn e)
        {
            return e;
        }

        public override object Visit(ExeStatement e)
        {
            e.slp.accept(this);
            return e;
        }

        public override object Visit(SwrlIterate e)
        {
            e.slp.accept(this);
            return e;
        }

        public override object Visit(SwrlVarList e)
        {
            foreach (var x in e.list)
                x.accept(this);
            return e;
        }


        public override object Visit(SwrlDVal e)
        {
            e.Val.accept(this);
            return e;
        }

        public override object Visit(SwrlDVar e)
        {
            return e.VAR;
        }

        public override object Visit(SwrlIVal e)
        {
            addToHeadOrBody(e.ToString(), ":I");
            return e;
        }

        public override object Visit(SwrlIVar e)
        {
            return e.VAR;
        }

        public override object Visit(Ontorion.CNL.DL.Atomic e)
        {
            if(isKindOf.get() == "C")
                addToHeadOrBody(e.id, ":C");
            return e;
        }

        public override object Visit(Ontorion.CNL.DL.Number e)
        {
            addToHeadOrBody(e.val, ":V");
            return e;
        }
        public override object Visit(Ontorion.CNL.DL.String e)
        {
            addToHeadOrBody(e.val, ":V");
            return e;
        }
        public override object Visit(Ontorion.CNL.DL.Float e)
        {
            addToHeadOrBody(e.val, ":V");
            return e;
        }
        public override object Visit(Ontorion.CNL.DL.Bool e)
        {
            addToHeadOrBody(e.val, ":V");
            return e;
        }

        public override object Visit(DateTimeVal e)
        {
            addToHeadOrBody(e.val, ":V");
            return e;
        }
    }
}
