using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace FluentEditorClientLib
{
    public class DLENConverter
    {
        Ontorion.CNL.CNLTools tools;
        Func<string, string> _ns2pfx;
        Func<string, string> getPfx2NsSource;
        string _defaultNamespace;

        public DLENConverter(Ontorion.CNL.CNLTools tools, Func<string, string> ns2pfx, Func<string, string> getPfx2NsSource, string defaultNamespaceProvider) //Func<string, string, string> pfx2ns,
        {
            this.tools = tools;
            this._ns2pfx = ns2pfx;
            this.getPfx2NsSource = getPfx2NsSource;
            this._defaultNamespace = defaultNamespaceProvider;
        }

        public string DL(string en, Ontorion.CNL.EN.endict.WordKind wkrd = Ontorion.CNL.EN.endict.WordKind.NormalForm)
        {
            if (en.StartsWith("a ") || en.StartsWith("an "))
                en = en.Split(' ').Last();
            var allParts = new Ontorion.CNL.EN.EnName() { id = en }.Split();
            if (!System.String.IsNullOrWhiteSpace(allParts.term) && !allParts.term.Contains("<"))
            {
                var tterm = getPfx2NsSource(allParts.term);
                if (!System.String.IsNullOrWhiteSpace(tterm))
                    allParts.term = "<" + tterm + ">";
                else
                    throw new Exception("No namespace found for prefix " + allParts.term + ". You need to define it before saving into Ontorion.");
            }
            else if (!System.String.IsNullOrWhiteSpace(allParts.term) && allParts.term.StartsWith("<") && allParts.term.EndsWith(">"))
            {
                var tterm = Ontorion.CNL.CNLTools.GetCanonicalNs(allParts.term.Substring(1, allParts.term.Length - 2)); // string without < and >
                allParts.term = "<" + tterm + ">";
            }
            else //add the default namespace
            {
                var defaultNs = _defaultNamespace;
                allParts.term = string.Format("<{0}>", defaultNs);
            }

            return Ontorion.CNL.EN.ENNameingConvention.ToDL(allParts.Combine(), wkrd).id;
        }

        public string CanonToEng(string symbol)
        {
            var ea = symbol.Split(new char[] { ':' }, StringSplitOptions.RemoveEmptyEntries);
            bool BigName = false;
            string pfx = "";
            if (ea[0] == "C")
            {
                pfx = "a ";
            }
            else if (ea[0] == "I")
            {
                BigName = true;
            }
            return pfx + EN(symbol.Substring(2, symbol.Length - 2), BigName);
        }

        public string CNLQueryToDL(string query)
        {
            query = query.Replace((char)160, ' ');
            var stmt = tools.GetEN2DLAst("Every loooooked-for is " + query + " .", true, true, getPfx2NsSource).Statements.First();
            return tools.SerializeDLAst(new Ontorion.CNL.DL.Paragraph(null) { Statements = new List<Ontorion.CNL.DL.Statement>() { stmt } });
        }

        public string EngToCanon(string expr)
        {
            var ea = expr.Split(new char[] { '\r', '\n', '\t', ' ' }, StringSplitOptions.RemoveEmptyEntries);
            var pfx = "";
            var wk = Ontorion.CNL.EN.endict.WordKind.NormalForm;
            if (ea.Length == 2 && (ea[0].ToLower() == "a" || ea[0].ToLower() == "an"))
            {
                pfx = "C:";
            }
            else if (ea.Length == 1)
            {
                if (ea[0].Length > 1)
                {
                    if (char.IsUpper(ea[0][0]))
                    {
                        pfx = "I:";
                    }
                    else
                    {
                        pfx = "R:";
                        wk = Ontorion.CNL.EN.endict.WordKind.PastParticiple;
                    }
                }
            }
            return pfx + DL(ea[ea.Count() - 1], wk);
        }

        public string ENNamespaceToPrefix(string en)
        {
            var allParts = new Ontorion.CNL.EN.EnName() { id = en }.Split();
            if (!System.String.IsNullOrWhiteSpace(allParts.term) && allParts.term.StartsWith("<") && allParts.term.EndsWith(">"))
            {
                var nss = allParts.term.Substring(1, allParts.term.Length - 2);
                if (nss == _defaultNamespace) // remove if the namespace is the default one.
                    allParts.term = null;
                else
                {
                    var tterm = _ns2pfx(nss);
                    if (!System.String.IsNullOrWhiteSpace(tterm))
                        allParts.term = tterm;
                }
            }

            return allParts.Combine().id;
        }

        public string EN(string dl, bool bigName, Ontorion.CNL.EN.endict.WordKind wrdKnd = Ontorion.CNL.EN.endict.WordKind.NormalForm)
        {
            if (dl == "⊤")
                return "thing";

            var allParts = Ontorion.CNL.EN.ENNameingConvention.FromDL(new Ontorion.CNL.DL.DlName() { id = dl }, wrdKnd, bigName).Split();
            if (!System.String.IsNullOrWhiteSpace(allParts.term) && allParts.term.StartsWith("<") && allParts.term.EndsWith(">"))
            {
                var nss = allParts.term.Substring(1, allParts.term.Length - 2);
                if (nss == _defaultNamespace) // remove if the namespace is the default one.
                    allParts.term = null;
                else
                {
                    var tterm = _ns2pfx(nss);
                    if (!System.String.IsNullOrWhiteSpace(tterm))
                        allParts.term = tterm;
                }
            }

            return allParts.Combine().id;
        }
    }
}
