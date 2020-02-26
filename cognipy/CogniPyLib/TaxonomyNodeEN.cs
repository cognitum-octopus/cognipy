using CNL.NET;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CogniPy.CNL.EN
{
    public class TaxonomyNodeEN :TaxonomyNode
    {
        public TaxonomyNodeEN(CogniPy.Collections.IInvokableProvider invokableProvider) :base(invokableProvider)
        {
        }

        public override string ENText
        {
            get
            {
                if (cachedENText == null)
                {
                    StringBuilder sb = new StringBuilder();
                    bool first = true;
                    foreach (var s in names)
                    {
                        if (s == null)
                            continue;
                        if (first)
                            first = false;
                        else
                            sb.Append(" ≡ ");

                        string ident;
                        if (s.EndsWith("⁻"))
                            ident = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = s.EndsWith("⁻") ? s.Substring(0, s.Length - 1) : s }, CNL.EN.endict.WordKind.SimplePast, false).id + " by";
                        else
                            ident = CogniPy.CNL.EN.ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = s }, false).id;

                        if (s.StartsWith("["))
                        {
                            var sts = s.Substring(1, s.Length - 2).Trim();
                            if (sts == "⊤") sts = "thing";
                            sb.Append("the " + sts);
                        }
                        else
                            sb.Append(ident);

                    }
                    cachedENText = sb.ToString();
                }
                return cachedENText;
            }
        }

        public string TextNoPrefix
        {
            get 
            {
                string enText=this.ENText;
                if (enText.EndsWith("]"))
                {
                    // we should check here if we are in Ontorion mode, otherwise it will change the way in which it is displayed also on the normal taxonomy!
                    int indx = enText.IndexOf("[");
                    if (indx != -1)
                    {
                        var str = enText.Substring(indx);
                        if (str != null)
                        {
                            this.Prefix = str;
                            enText = enText.Remove(indx);
                        }
                    }
                }

                return enText;
            }
        }
    }
}
