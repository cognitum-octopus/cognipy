using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using Ontorion.CNL.DL;

namespace Ontorion.CNL.EN
{
    public class EnName
    {
        public string id;

        private const string PFX = "The-";

        public class Parts
        {
            public bool quoted;
            public enum Kind { Name, BigName, VeryBigName };
            public Kind kind;

            public string name;
            public string term = null;

            static bool IsUrlWellFormed(string url)
            {
                Uri uriRes;
                if (url != null)
                {
                    if (!url.StartsWith("http://"))
                    {
                        return false;
                    }
                    else if (!Uri.TryCreate(url, UriKind.Absolute, out uriRes))
                    {
                        return false;
                    }
                }
                else
                    return false;
                return true;
            }

            string encode(string str)
            {
                return "\"" + str.Replace("\"", "\"\"") + "\"";
            }
            public EnName Combine()
            {
                var sb = new StringBuilder();
                if (quoted)
                {
                    if (kind == Kind.BigName)
                        sb.Append(PFX);
                    else if (kind == Kind.VeryBigName)
                        sb.Append(PFX.ToUpper());
                    sb.Append(encode(name));
                }
                else
                {
                    sb.Append(name);
                }
                if (term != null)
                {
                    sb.Append("[");
                    if (IsUrlWellFormed(term))
                        sb.Append("<" + term + ">");
                    else
                        sb.Append(term);
                    sb.Append("]");
                }
                return new EnName() { id = sb.ToString() };
            }

            internal Parts Clone()
            {
                return (new Parts() { kind=this.kind,name=this.name,quoted=this.quoted,term=this.term});
            }
        }

        [ThreadStatic]
        static Dictionary<string, Parts> cache = null;

        public Parts Split()
        {
            if (cache == null)
                cache = new Dictionary<string, Parts>();
            if (id == null)
            {
            }
            if (!cache.ContainsKey(id))
            {
                var factory = new CNLFactory();
                var lexer = factory.getLexer();
                lexer.Start(id);
                var token = lexer.Next();
                Parts ret = new Parts();
                if (token == null)
                    return ret;
                var tokStr = token.yytext;
                var termsStr = "";
                if (token.yytext.EndsWith("]"))
                {
                    var trmp = token.yytext.LastIndexOf('[');
                    termsStr = token.yytext.Substring(trmp);
                    tokStr = token.yytext.Substring(0, token.yytext.Length - termsStr.Length).Trim();
                    ret.term = termsStr.Substring(1, termsStr.Length - 2);
                }

                if (token is VERYBIGNAME)
                    ret.kind = Parts.Kind.VeryBigName;
                else if (token is BIGNAME)
                    ret.kind = Parts.Kind.BigName;
                else if (token is NAME)
                    ret.kind = Parts.Kind.Name;

                ret.quoted = (tokStr.StartsWith(PFX + "\"") ||
                                tokStr.StartsWith(PFX.ToUpper() + "\"")) && tokStr.EndsWith("\"");
                if (ret.quoted)
                    ret.name = tokStr.Substring(PFX.Length + 1, tokStr.Length - PFX.Length - 2).Replace("\"\"", "\"");
                else
                {
                    ret.quoted = tokStr.StartsWith("\"") && tokStr.EndsWith("\"");
                    if (ret.quoted)
                        ret.name = tokStr.Substring( 1, tokStr.Length - 2).Replace("\"\"", "\"");
                    else
                        ret.name = tokStr;
                }

                cache[id] = ret;
                return ret.Clone();
            }
            return cache[id].Clone();
        }
    }

    public static class ENNameingConvention
    {
        public const string TOPROLENAME = "\"<->\"";
        public const string BOTTOMROLENAME = "\"<x>\"";

        public static DlName ToDL(EnName eng, Ontorion.CNL.EN.endict.WordKind kind)
        {
            var parts = eng.Split();
            DlName.Parts dlp = new DlName.Parts();
            dlp.term = parts.term;
            dlp.local = parts.kind == EnName.Parts.Kind.BigName;
            dlp.quoted = parts.quoted;

            if (parts.quoted || parts.kind != EnName.Parts.Kind.Name || kind == endict.WordKind.NormalForm)
                dlp.name = parts.name;
            else
            {
                var name = parts.name;
                var arr = name.Split(new char[] { '-' });
                if (kind == endict.WordKind.SimplePast || kind == endict.WordKind.PastParticiple || kind == endict.WordKind.PluralFormVerb)
                    arr[0] = CNLFactory.lex.toDL_Simple(arr[0], kind);
                else if (kind == endict.WordKind.PluralFormNoun)
                    arr[arr.Length - 1] = CNLFactory.lex.toDL_Simple(arr[arr.Length - 1], kind);
                dlp.name= string.Join("-", arr);
            }
            return dlp.Combine();
        }

        public static EnName FromDL(DlName dl, endict.WordKind kind, bool bigName)
        {
            var dlp = dl.Split();
            var parts = new EnName.Parts();
            parts.kind = dlp.local ? EnName.Parts.Kind.BigName : (bigName ? EnName.Parts.Kind.VeryBigName : EnName.Parts.Kind.Name);
            parts.term = dlp.term;
            parts.quoted = dlp.quoted;
            if (dlp.quoted || bigName || kind == endict.WordKind.NormalForm)
            {
                if (Ontorion.CNL.EN.KeyWords.Me.isKeyword(dlp.name))
                    parts.quoted = true;
                parts.name = dlp.name;
            }
            else
            {
                var name = dlp.name;
                var arr = name.Split(new char[] { '-' });
                if (kind == endict.WordKind.SimplePast || kind == endict.WordKind.PastParticiple || kind == endict.WordKind.PluralFormVerb)
                    arr[0] = CNLFactory.lex.toN_Simple(arr[0], kind);
                else if (kind == endict.WordKind.PluralFormNoun)
                    arr[arr.Length - 1] = CNLFactory.lex.toN_Simple(arr[arr.Length - 1], kind);
                parts.name= string.Join("-", arr);
            }
            return parts.Combine();
        }

        public static EnName FromDL(DlName dl, bool bigName)
        {
            return FromDL(dl, endict.WordKind.NormalForm,bigName);
        }
    }
}
