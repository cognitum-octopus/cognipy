using System;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using CogniPy.CNL.DL;
using System.Collections.Generic;

namespace CogniPy.ASTTools
{

    public class NameMangler : GenericVisitor
    {

        private HashSet<string> toMangle = new HashSet<string>();

        public NameMangler(bool mangleInstances = true, bool mangleConcepts = true, bool mangleRoles = true, bool mangleDataRoles = true)
        {
            if (mangleInstances)
                toMangle.Add("I");
            if (mangleConcepts)
                toMangle.Add("C");
            if (mangleRoles)
                toMangle.Add("R");
            if (mangleDataRoles)
                toMangle.Add("D");
        }

        public static string Rot13(string input)
        {
            StringBuilder result = new StringBuilder();
            Regex regex = new Regex("[A-Za-z]");

            foreach (char c in input)
            {
                if (regex.IsMatch(c.ToString()))
                {
                    int charCode = ((c & 223) - 52) % 26 + (c & 32) + 65;
                    result.Append((char)charCode);
                }
                else
                    result.Append(c);
            }

            return result.ToString();
        }


        public string mangle(string x)
        {
            var nm = new DlName(){ id=x };
            var prts = nm.Split();
            prts.name= Rot13(prts.name);
            return prts.Combine().id;
        }


        public override object Visit(Atomic e)
        {
            if (this.toMangle.Contains(isKindOf.get()))
                e.id = mangle(e.id);
            return base.Visit(e);
        }

        public override object Visit(NamedInstance e)
        {
            if (this.toMangle.Contains("I"))
                e.name = mangle(e.name);
            return base.Visit(e);
        }

        public override object Visit(DisjointUnion e)
        {
            if (this.toMangle.Contains("C"))
                e.name = mangle(e.name);
            return base.Visit(e);
        }

        public override object Visit(SwrlRole e)
        {
            if (this.toMangle.Contains("R"))
                e.R = mangle(e.R);
            return base.Visit(e);
        }

        public override object Visit(SwrlDataProperty e)
        {
            if (this.toMangle.Contains("D"))
                e.R = mangle(e.R);
            return base.Visit(e);
        }

        public override object Visit(SwrlIVal e)
        {
            if (this.toMangle.Contains("I"))
                e.I = mangle(e.I);
            return base.Visit(e);
        }

        public override object Visit(SwrlVarList e)
        {
            e.list = (from x in e.list select x.accept(this) as IExeVar).ToList();
            return base.Visit(e);
        }

    }
}
