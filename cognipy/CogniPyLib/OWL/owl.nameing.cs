using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using org.semanticweb.owlapi.model;
using Ontorion.CNL.DL;
using org.semanticweb.owlapi.util;
using org.coode.xml;
using System.Text.RegularExpressions;
using System.Diagnostics;

namespace Ontorion.ARS
{
    public class IRIParser
    {
        static List<char> CharsForInternalUse = new List<char>() { '\u0001','\u0002' };
        public static string getFirstCharForInternalUse()
        {
            return CharsForInternalUse[0].ToString();
        }
        public static string getSecondCharForInternalUse()
        {
            return CharsForInternalUse[1].ToString();
        }
        static char[] URLUnreserved = new char[] { '-', '.', '_', '~' };

        /// <summary>
        /// Encodes an input string into an IRI
        /// </summary>
        /// <param name="input"></param>
        /// <returns></returns>
        public static string encodeToIRI(string input)
        {
            StringBuilder strBld = new StringBuilder();

            var charStr = input.ToCharArray();
            var len = charStr.Length;
            for (int i = 0; i < len; i++)
            {
                var el = charStr[i];
                if (CharsForInternalUse.Contains(el))
                {
                    strBld.Append(Uri.EscapeUriString(el.ToString()));
                }
                else if (char.IsLetter(el) || char.IsNumber(el) )
                {
                    strBld.Append(el);
                }
                else if (URLUnreserved.Contains(el))
                {
                    strBld.Append("%");
                    strBld.AppendFormat(((int)el).ToString("X2"));
                }
                else
                {
                    strBld.Append(Uri.EscapeUriString(el.ToString()));
                }
            }
            return strBld.ToString();
        }

        /// <summary>
        /// Decodes an IRI to a string
        /// </summary>
        /// <param name="IRI"></param>
        /// <returns></returns>
        public static string decodeIRI(string IRI)
        {
            StringBuilder strBld = new StringBuilder();
            var charStr = IRI.ToCharArray();
            for (int i = 0; i < charStr.Length; i++)
            {
                if (charStr[i] == '%' && i + 2 < charStr.Length) // case in which an hexadecimal character is found
                {
                    StringBuilder tmpStrBld = new StringBuilder();
                    tmpStrBld.Append(charStr[i]);
                    tmpStrBld.Append(charStr[i + 1]);
                    tmpStrBld.Append(charStr[i + 2]);
                    var unescapedChar = Uri.UnescapeDataString(tmpStrBld.ToString());
                    if (unescapedChar == tmpStrBld.ToString() && i + 5 < charStr.Length) // UTF8 escaped char
                    {
                        tmpStrBld.Append(charStr[i + 3]);
                        tmpStrBld.Append(charStr[i + 4]);
                        tmpStrBld.Append(charStr[i + 5]);
                        unescapedChar = Uri.UnescapeDataString(tmpStrBld.ToString());
                        i = i + 5;
                    }
                    else
                        i = i + 2;

                    strBld.Append(unescapedChar);
                }
                else if (char.IsLetter(charStr[i]) || char.IsNumber(charStr[i]))
                {
                    strBld.Append(charStr[i]);
                }
                else
                {
                    strBld.Append(Uri.UnescapeDataString(charStr[i].ToString()));
                }
            }
            return strBld.ToString();
        }

        public static bool AreNamespacesEqual(string ns1Ext,string ns2Ext)
        {
            var ns1 = ns1Ext;
            var ns2 = ns2Ext;
            if (ns1.StartsWith("<"))
                ns1 = ns1.Substring(1, ns1.Length - 2);

            if (ns2.StartsWith("<"))
                ns1 = ns1.Substring(1, ns1.Length - 2);

            if (ns1.EndsWith("#") || ns1.EndsWith("/"))
                ns1 = ns1.Substring(0, ns1.Length - 1);

            if (ns2.EndsWith("#") || ns2.EndsWith("/"))
                ns2 = ns2.Substring(0, ns2.Length - 1);

            return ns1.Equals(ns2);
        }
    }

    public class OwlName
    {
        static string globalInstanceIndicator = IRIParser.getSecondCharForInternalUse();

        public class Parts
        {
            public string ns;
            public string name;
            public bool global;
            /// <summary>
            /// IRI compliant name
            /// </summary>
            public string encodedName { get { return IRIParser.encodeToIRI((global ? globalInstanceIndicator : "") + name); } }
            public OwlName Combine()
            {
                string sep = "";
                if (!ns.EndsWith("/") && !ns.EndsWith("#") && !ns.Contains("#"))
                    sep = "#";

                if (name.Contains("/") && ns.EndsWith("/"))
                    ns = ns.Substring(0, ns.Length - 1) + "#";

                return new OwlName() { iri = IRI.create(ns + sep + encodedName) };
            }
        }

        [ThreadStatic]
        static Dictionary<string, Parts> cache = null;
        public IRI iri;
        public Parts Split()
        {
            if (cache == null)
                cache = new Dictionary<string, Parts>();
            try
            {
                string key = iri.toString();
                if (cache.ContainsKey(key))
                    return cache[key];
                else
                {
                    if (iri.getScheme() == "file")
                    {
                        string shortForm = iri.getFragment();
                        string ns = iri.getNamespace();
                        bool isGlobal = shortForm.StartsWith(globalInstanceIndicator);
                        if (isGlobal)  shortForm = shortForm.Substring(1);
                        cache[key] = new Parts() { ns = ns, name = shortForm, global = isGlobal };
                        return cache[key];
                    }
                    else
                    {
                        Uri u = new Uri(key,UriKind.RelativeOrAbsolute);
                        if (u.IsAbsoluteUri)
                        {
                            if (!System.String.IsNullOrWhiteSpace(u.Fragment))
                            {
                                if (u.Fragment.StartsWith("#"))
                                {
                                    string shortForm = Uri.UnescapeDataString(u.Fragment.Substring(1));
                                    string ns = key.Substring(0, key.Length - IRIParser.encodeToIRI(shortForm).Length-1) + "#";
                                    bool isGlobal = shortForm.StartsWith(globalInstanceIndicator);
                                    if (isGlobal) shortForm = shortForm.Substring(1);
                                    cache[key] = new Parts() { ns = ns, name = shortForm, global = isGlobal };
                                    return cache[key];
                                }
                            }
                            if (!System.String.IsNullOrWhiteSpace(u.Segments[u.Segments.Count() - 1]))
                            {
                                string shortForm = Uri.UnescapeDataString(u.Segments[u.Segments.Count() - 1]);
                                var segmFullyEncoded = IRIParser.encodeToIRI(IRIParser.decodeIRI(u.Segments[u.Segments.Count()-1]));
                                string ns = u.OriginalString.Substring(0, u.OriginalString.Length - segmFullyEncoded.Length);
                                bool isGlobal = shortForm.StartsWith(globalInstanceIndicator);
                                if (isGlobal) shortForm = shortForm.Substring(1);
                                cache[key] = new Parts() { ns = ns, name = shortForm, global = isGlobal };
                                return cache[key];
                            }
                        }
                        else
                        {
                            string str = IRIParser.decodeIRI(iri.toString());
                            if (str.Contains('#'))
                            {
                                var pr = str.Split(new char[] { '#' });
                                string shortForm = pr[1];
                                string ns = pr[0]+"#"; 
                                bool isGlobal = shortForm.StartsWith(globalInstanceIndicator);
                                if (isGlobal) shortForm = shortForm.Substring(1);
                                return new Parts() { ns = ns, name = shortForm, global = isGlobal };
                            }
                            else
                            {
                                bool isGlobal = str.StartsWith(globalInstanceIndicator);
                                if (isGlobal) str = str.Substring(1);
                                return new Parts() { name = str, global = isGlobal };
                            }
                        }
                    }
                }
                return null;
            }
            catch (Exception)
            {
                string str = IRIParser.decodeIRI(iri.toString());
                if (str.Contains('#'))
                {
                    var pr = str.Split(new char[] { '#' });
                    string shortForm = pr[1];
                    string ns = pr[0] + "#";
                    bool isGlobal = shortForm.StartsWith(globalInstanceIndicator);
                    if (isGlobal) shortForm = shortForm.Substring(1);
                    return new Parts() { ns = ns, name = shortForm, global = isGlobal };
                }
                else
                {
                    bool isGlobal = str.StartsWith(globalInstanceIndicator);
                    if (isGlobal) str = str.Substring(1);
                    return new Parts() { name = str, global = isGlobal };
                }
            }

        }

        public static Parts Split(string uri)
        {
            return new OwlName() { iri = IRI.create(uri) }.Split();
        }

        public static EntityKind getKind(OWLEntity ent)
        {
            if (ent is OWLClass)
                return EntityKind.Concept;
            else if (ent is OWLIndividual)
                return EntityKind.Instance;
            else if (ent is OWLDataProperty)
                return EntityKind.DataRole;
            else if (ent is OWLObjectProperty)
                return EntityKind.Role;
            else if (ent is OWLDatatype)
                return EntityKind.DataType;
            else if (ent is OWLAnnotationProperty)
                return EntityKind.Annotation;
            else if (ent is SWRLVariable)
                return EntityKind.SWRLVariable;
            else
            {
                throw new InvalidOperationException();
            }
        }

    }

    public interface IOwlNameingConvention
    {
        Ontorion.CNL.DL.DlName ToDL(OwlName owlname, CNL.EN.endict lex, Func<string, string> ns2pfx, EntityKind madeFor);
        OwlName FromDL(Ontorion.CNL.DL.DlName dl, CNL.EN.endict lex, Func<string, string> pfx2ns, EntityKind madeFor);
    }

    //public class OwlNameingConventionSmartImport : IOwlNameingConvention
    //{
    //    static string UPPERL = @"[A-Z]";
    //    static string LOWERL = @"[a-z]";
    //    static string DIGIT = @"[0-9]";
    //    static string NAME = "\\A((?<g>" + UPPERL + "?" + LOWERL + @"*)|(?<g>" + DIGIT + "+)|_|\\-|\\.)*\\Z";
    //    static string BIGNAME = "\\A((?<g>" + UPPERL + "+" + @")|(?<g>" + DIGIT + "+)|_|\\-|\\.)*\\Z";


    //    static Regex normregexp = new Regex(NAME, RegexOptions.Compiled);
    //    static Regex bigregexp = new Regex(BIGNAME, RegexOptions.Compiled);

    //    Func<string, IEnumerable<string>> getForms = null;

    //    public OwlNameingConventionSmartImport(Func<string, IEnumerable<string>> getForms)
    //    {
    //        this.getForms = getForms;
    //    }

    //    static HashSet<string> pfxes = new HashSet<string>(new string[] { "sub", "super", "full" });

    //    static WordNetClasses.WN wordnet = new WordNetClasses.WN();

    //    private bool isVerb(CNL.EN.endict lex, string word, IEnumerable<string> forms)
    //    {
    //        foreach (var frm in forms)
    //        {
    //            bool b = false;
    //            Wnlib.SearchSet bobj2 = null;
    //            var list = new System.Collections.ArrayList();
    //            lock(wordnet)
    //                wordnet.OverviewFor(frm, "verb", ref b, ref bobj2, list);
    //            foreach (var e in list)
    //            {
    //                Wnlib.Search s = e as Wnlib.Search;
    //                var pp = lex.toN_Simple(s.word.ToLower(), CNL.EN.endict.WordKind.PastParticiple);
    //                var sp = lex.toN_Simple(s.word.ToLower(), CNL.EN.endict.WordKind.SimplePast);
    //                if (word.ToLower() == pp.ToLower() || word.ToLower() == sp.ToLower())
    //                    return true;
    //            }
    //        }
    //        return false;
    //    }

    //    private bool isPluralNoun(CNL.EN.endict lex, string word, IEnumerable<string> forms)
    //    {
    //        foreach (var frm in forms)
    //        {
    //            bool b = false;
    //            Wnlib.SearchSet bobj2 = null;
    //            var list = new System.Collections.ArrayList();
    //            lock (wordnet)
    //                wordnet.OverviewFor(frm, "noun", ref b, ref bobj2, list);
    //            foreach (var e in list)
    //            {
    //                Wnlib.Search s = e as Wnlib.Search;
    //                var spp = lex.toDL_Simple(s.word.ToLower(), CNL.EN.endict.WordKind.PluralFormNoun);
    //                var sp = lex.toN_Simple(spp, CNL.EN.endict.WordKind.NormalForm);
    //                if (word.ToLower() != sp.ToLower())
    //                    return true;
    //            }
    //        }
    //        return false;
    //    }

    //    private bool isPOS(string word, IEnumerable<string> forms, string pos, bool equalToBaseForm)
    //    {
    //        foreach (var frm in forms)
    //        {
    //            bool b = false;
    //            Wnlib.SearchSet bobj2 = null;
    //            var list = new System.Collections.ArrayList();
    //            lock (wordnet)
    //                wordnet.OverviewFor(frm, pos, ref b, ref bobj2, list);
    //            foreach (var e in list)
    //            {
    //                if (equalToBaseForm)
    //                {
    //                    Wnlib.Search s = e as Wnlib.Search;
    //                    if (word.ToLower() == s.word.ToLower())
    //                        return true;
    //                }
    //                else
    //                    return true;
    //            }
    //        }
    //        return false;
    //    }

    //    public DlName ToDL(OwlName owlname, CNL.EN.endict lex, Func<string, string> ns2pfx, EntityKind madeFor)
    //    {
    //        var parts = owlname.Split();
    //        string defaultNs = ns2pfx(null);
    //        if (string.IsNullOrWhiteSpace(parts.ns))
    //            parts.ns = defaultNs;

    //        if (parts.name == null)
    //            return new DlName() { id = null };

    //        string pfx = null;
            
    //        if(!IRIParser.AreNamespacesEqual(defaultNs,parts.ns))
    //            pfx = ns2pfx(parts.ns);

    //        var name_part = parts.name;
    //        var sufix = "";
    //        if (madeFor == EntityKind.SWRLVariable)
    //        {
    //            var a = name_part.Split('_');
    //            if (a.Length == 2)
    //            {
    //                name_part = a.First();
    //                sufix = a.Last();
    //            }
    //        }

    //        List<string> grps = new List<string>();
    //        var dlp = new DlName.Parts() { term = pfx };

    //        Match mth = null;

    //        if (bigregexp.IsMatch(name_part))
    //            mth = bigregexp.Match(name_part);
    //        else
    //            mth = normregexp.Match(name_part);

    //        if (mth.Success)
    //        {
    //            var grps2 = mth.Groups["g"].Captures;
    //            foreach (var g in grps2)
    //                if (g.ToString().Length > 0)
    //                    grps.Add(g.ToString().ToLower());
    //        }
    //        else
    //        {
    //            grps = name_part.ToLower().Split('-', '_', '.').ToList();
    //        }

    //        dlp.quoted = false;

    //        StringBuilder sb = new StringBuilder();
    //        var first = true;
    //        bool addOf = false;
    //        foreach (var ss in grps)
    //        {
    //            if (first)
    //            {
    //                first = false;
    //                if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
    //                {
    //                    var word = grps.First();
    //                    var forms = getForms(word);
    //                    if (!isVerb(lex, word, forms))
    //                    {
    //                        if (grps.Count() == 1 && (isPOS(word, forms, "adj", false) || isPOS(word, forms, "adv", false)) && !isPOS(word, forms, "noun", true))
    //                            sb.Append("be-" + ss);
    //                        else if (grps.Last() == "of")
    //                            sb.Append("be-" + ss);
    //                        else
    //                            sb.Append("have-" + ss);
    //                    }
    //                    else
    //                    {
    //                        var spp = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.SimplePast);
    //                        var sp = lex.toN_Simple(spp, CNL.EN.endict.WordKind.SimplePast);
    //                        if (pfxes.Contains(ss))
    //                        {
    //                            sb.Append("be-" + ss);
    //                            addOf = true;
    //                        }
    //                        else if (sp == ss)
    //                        {
    //                            sb.Append("be-" + ss);
    //                        }
    //                        else
    //                        {
    //                            var nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PastParticiple);
    //                            sb.Append(nn);
    //                        }
    //                    }
    //                    continue;
    //                }
    //                else if (madeFor == EntityKind.Concept)
    //                {
    //                    var word = grps.First();
    //                    var forms = getForms(word);
    //                    if (isPluralNoun(lex, word, forms))
    //                    {
    //                        var nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PluralFormNoun);
    //                        if (nn.ToLower() != word.ToLower() && nn.Length > 2)
    //                        {
    //                            sb.Append(char.ToLower(nn[0]));
    //                            sb.Append(nn.Substring(1));
    //                            continue;
    //                        }
    //                    }
    //                }
    //            }
    //            else
    //                sb.Append("-");
    //            if (ss.Length == 0)
    //            {
    //                sb.Append(madeFor == EntityKind.Instance ? "U" : "u");
    //                sb.Append("nknown");
    //            }
    //            else
    //            {
    //                sb.Append(madeFor == EntityKind.Instance ? char.ToUpper(ss[0]) : char.ToLower(ss[0]));
    //                sb.Append(ss.Substring(1));
    //            }
    //        }
    //        if (addOf)
    //        {
    //            if (!sb.ToString().ToLower().EndsWith("-of"))
    //                sb.Append("-of");
    //        }
    //        dlp.name = sb.ToString() + (string.IsNullOrEmpty(sufix)?"":"_" + sufix);
    //        dlp.local = ((madeFor== EntityKind.Instance) && !parts.global);
    //        return dlp.Combine();
    //    }

    //    public OwlName FromDL(DlName dl, CNL.EN.endict lex, Func<string, string> pfx2ns, EntityKind madeFor)
    //    {
    //        return new OwlNameingConventionCamelCase().FromDL(dl, lex, pfx2ns, madeFor);
    //    }
    //}

    public class OwlNameingConventionCamelCase : IOwlNameingConvention
    {

        static string UPPERL = @"[A-Z]";
        static string LOWERL = @"[a-z]";
        static string DIGIT = @"[0-9]";
        static string DIGIT_D = @"((\-)?" + DIGIT+"+)";
        static string NAME = "\\A(?<g>" + LOWERL + "+)?((?<g>" + UPPERL + LOWERL + @"*)|(?<g>" + DIGIT + DIGIT_D + "*))*\\b" + "(?<u>\\.(" + UPPERL + "|" + LOWERL + "|" + DIGIT + ")+)?\\Z";
        static string exoticUTFForUnderscore = IRIParser.getFirstCharForInternalUse();
        static string VBIGNAME = "\\A(?<g>" + UPPERL + UPPERL + @"+)(" + exoticUTFForUnderscore + "((?<g>" + UPPERL + @"+)|(?<g>" + DIGIT + "+)))*\\b" + "(?<u>\\.(" + UPPERL + "|" + LOWERL + "|" + DIGIT + ")+)?\\Z";
        
        static Regex regexp = new Regex(NAME, RegexOptions.Compiled);
        static Regex vbregexp = new Regex(VBIGNAME, RegexOptions.Compiled);

        /// <summary>
        /// Translate an OWLname to a CNL (DL) name
        /// </summary>
        /// <param name="owlname">the OWL name</param>
        /// <param name="lex"></param>
        /// <param name="ns2pfx"></param>
        /// <param name="madeFor">the type of this entity (Concept, Instance, Role,...)</param>
        /// <returns></returns>
        public Ontorion.CNL.DL.DlName ToDL(OwlName owlname, CNL.EN.endict lex, Func<string, string> ns2pfx, EntityKind madeFor)
        {
            var parts = owlname.Split();
            string defaultNs = ns2pfx(null);
            if (string.IsNullOrWhiteSpace(parts.ns))
                parts.ns = defaultNs;

            string pfx = null;
            if (!IRIParser.AreNamespacesEqual(defaultNs, parts.ns))
                pfx = ns2pfx(parts.ns);

            var dlp = new DlName.Parts() { term = pfx };

            if (parts.name.StartsWith("⊤"))
            {
                dlp.name = "⊤";
                if (parts.name.Contains("."))
                    dlp.name = "[" + dlp.name + "]";
                dlp.quoted = false;
            }
            else
            {
                var name_part = parts.name;
                bool isVBigName = parts.global;
                var sufix = "";
                if (madeFor == EntityKind.SWRLVariable)
                {
                    var a = name_part.Split('_');
                    if (a.Length == 2)
                    {
                        name_part = a.First();
                        sufix = a.Last();
                    }
                }
                Match mth = null;
                if (!isVBigName)
                    mth = regexp.Match(name_part);
                Match mth2 = null;
                if (isVBigName || !mth.Success )
                    mth2 = vbregexp.Match(name_part);
                if (name_part != "" &&
                     (
                     (mth != null && mth.Success
                     && (
                        (( madeFor == EntityKind.SWRLVariable || madeFor == EntityKind.Instance) && char.IsUpper(name_part.First()))
                     || ((madeFor == EntityKind.Concept || madeFor == EntityKind.DataRole || madeFor == EntityKind.SWRLVariable || madeFor == EntityKind.Role) && char.IsLower(name_part.First()))
                     )) ||
                     (mth2 != null && mth2.Success && madeFor == EntityKind.Instance)
                     ))
                {
                    var grps = (mth != null && mth.Success) ? mth.Groups["g"].Captures : mth2.Groups["g"].Captures;
                    var urps = (mth != null && mth.Success) ? mth.Groups["u"].Captures : mth2.Groups["u"].Captures;
                    dlp.quoted = false;
                    StringBuilder sb = new StringBuilder();
                    var first = true;
                    bool success = true;
                    foreach (var n in grps)
                    {
                        var ss = n.ToString();
                        if (first)
                        {
                            first = false;
                            if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
                            {
                                var nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PastParticiple);
                                if (lex.toN_Simple(nn, CNL.EN.endict.WordKind.PastParticiple) != ss)
                                {
                                    success = false;
                                    dlp.quoted = true;
                                    dlp.name = parts.name + (string.IsNullOrEmpty(sufix) ? "" : "_" + sufix);
                                    break;
                                }
                                sb.Append(nn);
                                continue;
                            }
                        }
                        else
                            sb.Append("-");
                        sb.Append(madeFor == EntityKind.Instance && !(urps.Count > 0) ? char.ToUpper(ss[0]) : char.ToLower(ss[0]));
                        sb.Append(ss.Substring(1));
                    }
                    if (success)
                    {
                        dlp.name = sb.ToString() + (string.IsNullOrEmpty(sufix) ? "" : "_" + sufix);
                        if (urps.Count > 0)
                            dlp.name = "[" + dlp.name + "]";
                    }
                }
                else
                {
                    dlp.quoted = true;
                    dlp.name = parts.name + (string.IsNullOrEmpty(sufix) ? "" : "_" + sufix);
                }
            }

            if (madeFor == EntityKind.Instance)
                dlp.name = IRIParser.decodeIRI(dlp.name);

            dlp.local = ((madeFor == EntityKind.Instance) && !parts.global);

            return dlp.Combine();
        }

        /// <summary>
        /// Translate a CNL(DL) name to a OWL name
        /// </summary>
        /// <param name="dl">the CNL name</param>
        /// <param name="lex"></param>
        /// <param name="pfx2ns"></param>
        /// <param name="madeFor">the type of this entity (Concept, Instance, Role,...)</param>
        /// <returns></returns>
        public OwlName FromDL(Ontorion.CNL.DL.DlName dl, CNL.EN.endict lex, Func<string, string> pfx2ns, EntityKind madeFor)
        {
            var owlParts = new OwlName.Parts();
            var dlParts = dl.Split();
            if (dlParts.term == null)
                owlParts.ns = pfx2ns(null);// OWLOntologyXMLNamespaceManager namespaceManager namespaceManager.getDefaultNamespace();
            else
            {
                owlParts.ns = pfx2ns(dlParts.term);//namespaceManager.getNamespaceForPrefix(dlParts.term);
                if (owlParts.ns == null)
                    owlParts.ns = "http://ontorion.com/unknown.owl/" + dlParts.term + "#";
            }
            if (!dlParts.quoted)
            {
                var name_part = dlParts.name;
                string sufix = "";
                if (madeFor == EntityKind.SWRLVariable)
                {
                    var a = name_part.Split('_');
                    if (a.Length == 2)
                    {
                        name_part = a.First();
                        sufix = a.Last();
                    }
                }

                var pp = name_part.Split(new char[] { '-' });
                StringBuilder sb = new StringBuilder();
                bool first = true;
                string oldpp = "";
                bool wereDigits = false;
                foreach (var p in pp)
                {
                    if (first)
                    {
                        first = false;
                        if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
                        {
                            var nn = lex.toN_Simple(p, CNL.EN.endict.WordKind.PastParticiple);
                            sb.Append(nn);
                        }
                        else
                        {
                            sb.Append((madeFor == EntityKind.Instance || (madeFor == EntityKind.SWRLVariable/* && p.Length==1 */)) ?
                                char.ToUpper(p[0]) : char.ToLower(p[0]));
                            sb.Append(p.Substring(1));
                        }
                    }
                    else
                    {
                        if (oldpp.Length > 1 && char.IsLetter(oldpp[0]) && char.ToUpper(oldpp.Last()) == oldpp.Last() && char.IsLetter(p[0]))
                            sb.Append(exoticUTFForUnderscore);
                        if (char.IsDigit(p[0]))
                        {
                            if (wereDigits)
                                sb.Append("-");
                            sb.Append(p);
                            wereDigits = true;
                        }
                        else
                        {
                            sb.Append(char.ToUpper(p[0]));
                            sb.Append(p.Substring(1));
                            wereDigits = false;
                        }
                    }
                    oldpp = p;
                }
                owlParts.name = sb.ToString() + (string.IsNullOrEmpty(sufix) ? "" : "_" + sufix);
            }
            else
            {
                owlParts.name = dlParts.name;
            }

            owlParts.global = (madeFor==EntityKind.Instance && !dlParts.local);

            return owlParts.Combine();
        }
    }

    public class OwlNameingConventionUnderscore : IOwlNameingConvention
    {

        static string UPPERL = @"[A-Z]";
        static string LOWERL = @"[a-z]";
        static string DIGIT = @"[0-9]";
        static string NAME = @"\A(?<g>" + UPPERL + "?" + LOWERL + "+)(_((?<g>" + UPPERL + "?" + LOWERL + @"+)|(?<g>" + DIGIT + @"+)))*\Z";
        static string NAME2 = @"\A(?<g>" + UPPERL + "?" + LOWERL + @"+)(\-((?<g>" + UPPERL + "?" + LOWERL + @"+)|(?<g>" + DIGIT + @"+)))*\Z";

        static Regex aregexp = new Regex(NAME, RegexOptions.Compiled);
        static Regex aregexp2 = new Regex(NAME2, RegexOptions.Compiled);
        bool bigConcept;

        public OwlNameingConventionUnderscore(char separator, bool bigConcept)
        {
            bregexp = separator == '_' ? aregexp : aregexp2;
            this.bigConcept = bigConcept;
        }

        Regex bregexp = null;

        public Ontorion.CNL.DL.DlName ToDL(OwlName owlname, CNL.EN.endict lex, Func<string, string> ns2pfx, EntityKind madeFor)
        {
            var parts = owlname.Split();
            string defaultNs = ns2pfx(null);
            if (string.IsNullOrWhiteSpace(parts.ns))
                parts.ns = defaultNs;

            string pfx = null;
            if (!IRIParser.AreNamespacesEqual(defaultNs, parts.ns))
                pfx = ns2pfx(parts.ns);

            var mth = bregexp.Match(parts.name);
            var captures = mth.Groups["g"].Captures;
            var dlp = new DlName.Parts() { term = pfx };
            if (mth.Success
                && ((!bigConcept && (
                    ((madeFor == EntityKind.Instance) && char.IsUpper(parts.name.First()))
                 || ((madeFor == EntityKind.Concept || madeFor == EntityKind.DataRole || madeFor == EntityKind.Role) && char.IsLower(parts.name.First()))
                 ))
                  || (bigConcept && (
                    ((madeFor == EntityKind.Instance) && char.IsUpper(parts.name.First()))
                 || ((madeFor == EntityKind.Concept || madeFor == EntityKind.DataRole || madeFor == EntityKind.Role) && char.IsLower(parts.name.First()))
                  ))))
            {
                dlp.quoted = false;
                StringBuilder sb = new StringBuilder();
                var first = true;
                foreach (var n in captures)
                {
                    var ss = n.ToString();
                    if (first)
                    {
                        first = false;
                        if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
                        {
                            var nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PastParticiple);
                            sb.Append(nn);
                            continue;
                        }
                    }
                    else
                        sb.Append("-");
                    sb.Append(madeFor == EntityKind.Instance ? char.ToUpper(ss[0]) : char.ToLower(ss[0]));
                    sb.Append(ss.Substring(1));
                }
                dlp.name = sb.ToString();
            }
            else
            {
                dlp.quoted = true;
                dlp.name = parts.name;
            }

            return dlp.Combine();
        }


        public OwlName FromDL(Ontorion.CNL.DL.DlName dl, CNL.EN.endict lex, Func<string, string> pfx2ns, EntityKind madeFor)
        {
            Debugger.Break();
            throw new NotImplementedException();
        }
    }

}
