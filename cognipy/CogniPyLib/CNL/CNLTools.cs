using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using Tools;


namespace CogniPy.CNL
{
    public interface ICNLFactory
    {
        Tools.Lexer getLexer();

        Tools.Parser getParser();

        bool isEOL(TOKEN tok);

        bool IsAnnot(TOKEN tok);

        bool isParagraph(SYMBOL smb);

        void setPfx2NsSource(Func<string, string> pfx2Ns);

        DL.Paragraph InvConvert(SYMBOL smb, bool useFullUri = false, Func<string, string> pfx2ns = null);

        object Convert(DL.Statement stmast, bool usePrefixes = false, Func<string, string> ns2pfx = null);

        object Convert(DL.IAccept nodeast, bool usePrefixes = false, Func<string, string> ns2pfx = null);

        object Convert(DL.Paragraph para, bool usePrefixes = false, Func<string, string> ns2pfx = null);

        string Serialize(object enast, bool serializeAnnotations, out AnnotationManager annotMan, bool templateMode = false);

        IEnumerable<string> Morphology(IEnumerable<string> col, string str, string form, bool bigName);

        string GetEOLTag();

        string[] GetAllKeywords();

        bool IsKeyword(string kw);

        bool KeywordTagExists(string kw);

        void FindMark(SYMBOL smb, string mark, out string kind, out string form);

        string GetDefaultTagValue(string prop);

        bool TagIsName(string prop);

        bool TagIsDatatype(string prop);

        string[] GetTagSuffixes();

        bool TagIsInstanceName(string prop);

        string GetSymbol(string prop);

        string GetKeyword(string prop);

        HashSet<string> GetAllMatchingKeywords(string kw);

        string GetKeywordTag(string wrd);

        string GetTooltipDesc(KeyValuePair<string, string> kv);

        string GetKeywordTip(string kwtag);

        bool LoadSmallestSentenceCache(Dictionary<string, string> cache);

        void SaveSmallestSentenceCache(Dictionary<string, string> cache);

        bool ValidateSafeness(object ast);
    }

    public class ParseException : Exception
    {
        public int Line { get; private set; }

        public int Column { get; private set; }

        public int Pos { get; private set; }

        public string Context { get; private set; }

        public ParseException(string message, int line, int column, int pos, string context)
            : base(message)
        {
            this.Context = context;
            this.Line = line;
            this.Column = column;
            this.Pos = pos - 1;
            if (this.Pos < 0) this.Pos = 0;
            if (this.Pos > context.Length - 1) this.Pos = context.Length - 1;
        }

        public string Hint
        {
            get
            {
                try
                {
                    int delta = 100;
                    int min = Pos - delta > 0 ? Pos - delta : 0;
                    int max = Pos + delta < Context.Length ? Pos + delta : Context.Length - 1;
                    StringBuilder sb = new StringBuilder();
                    if (Pos - min + 1 > 0)
                        sb.Append(Context.Substring(min, Pos - min + 1));
                    sb.Append("^");
                    if (Pos + 1 < Context.Length - 1 && max - Pos - 1 > 0)
                        sb.Append(Context.Substring(Pos + 1, max - Pos - 1));
                    return sb.ToString();
                }
                catch
                {
                    return "fatal error while generating the hint";
                }
            }
        }
    }

    public class CNLTools
    {
        private static Dictionary<string, Type> factories = new Dictionary<string, Type>();

        public static void RegisterCNLFactory(string name, Type factory)
        {
            lock (factories)
            {
                name = name.ToLower(System.Globalization.CultureInfo.InvariantCulture);
                factories[name] = factory;
            }
        }

        public string currentLang = string.Empty;

        public CNLTools(string languageSymbol, Func<string, string> pfx2Ns = null)
        {
            Type t_factory = null;
            languageSymbol = languageSymbol.ToLower(System.Globalization.CultureInfo.InvariantCulture);
            lock (factories)
            {
                if (!factories.ContainsKey(languageSymbol))
                    throw new ArgumentException(String.Format("CNL not loaded for given language: {0}", languageSymbol));
                else
                    t_factory = factories[languageSymbol];
            }
            var cnstr = t_factory.GetConstructor(new Type[] { });
            factory = cnstr.Invoke(new object[] { }) as ICNLFactory;
            factory.setPfx2NsSource(pfx2Ns);
            currentLang = languageSymbol;
        }

        public static bool isSurelyDLEntity(string entity, CogniPy.ARS.EntityKind entKind)
        {
            if (String.IsNullOrWhiteSpace(entity))
                return false;

            // if it starts with _ or {" it is an instance and it is already in DL!
            if (entKind == CogniPy.ARS.EntityKind.Instance && (entity.StartsWith("_") || entity.StartsWith("{\"")))
                return true;

            var nn = new CogniPy.CNL.DL.DlName() { id = entity };
            var sp = nn.Split();
            return !string.IsNullOrWhiteSpace(sp.term);
        }

        public string GetNamespaceFromNamespaceLine(string input)
        {
            var dp = input.IndexOf(':');
            var x = input.Substring(0, dp);
            var ontologyIri = input.Substring(dp + 1).Trim();
            if (ontologyIri.EndsWith(".")) ontologyIri = ontologyIri.Substring(0, ontologyIri.Length - 1);
            if (ontologyIri.StartsWith("\'") && ontologyIri.Length > 2)
                ontologyIri = ontologyIri.Substring(1, ontologyIri.Length - 2).Replace("\'\'", "\'");
            ontologyIri = ontologyIri.Replace(" ", "");
            ontologyIri = ontologyIri.Replace("\\", "/");
            if (System.IO.Path.IsPathRooted(ontologyIri))
                ontologyIri = "file:" + ontologyIri;

            if (!ontologyIri.EndsWith("/") && !ontologyIri.EndsWith("#") && !ontologyIri.Contains("#"))
                ontologyIri += "#";
            return ontologyIri;
        }

        public static string GetCanonicalNs(string ns)
        {
            if (!string.IsNullOrWhiteSpace(ns) && !ns.EndsWith("/") && !ns.EndsWith("#") && !ns.Contains("#"))
                return ns + "#";
            else
                return ns;
        }

        /// <summary>
        /// Checks if the two namespaces are different
        /// NB: http://aaa/ and http://aaa# are the same namespace!
        /// </summary>
        /// <param name="ns1"></param>
        /// <param name="ns2"></param>
        /// <returns></returns>
        public static bool AreNamespacesEqual(string ns1, string ns2)
        {
            if (string.IsNullOrWhiteSpace(ns1) || String.IsNullOrWhiteSpace(ns2))
                return false;

            if (ns1 == ns2)
                return true;

            // trying to understand here if the two namespaces differ only for the last character.
            if (ns1.EndsWith("#") || ns1.EndsWith("/"))
                ns1 = ns1.Remove(ns1.Length - 1);
            if (ns2.EndsWith("#") || ns2.EndsWith("/"))
                ns2 = ns2.Remove(ns2.Length - 1);
            if (ns1 == ns2)
                return true;

            return false;
        }

        public static string DLToFullUri(string entity, ARS.EntityKind entKind, Func<string, string> pfx2ns = null, string defaultNs = null)
        {
            if (pfx2ns == null)
                return entity;

            if (entKind == ARS.EntityKind.Statement)
                return entity;
            else
            {
                var dlName = new CogniPy.CNL.DL.DlName() { id = entity };
                var allParts = dlName.Split();
                if (!System.String.IsNullOrWhiteSpace(allParts.term) && !allParts.term.StartsWith("<") && !allParts.term.EndsWith(">"))
                {
                    var tterm = pfx2ns(allParts.term);
                    if (!System.String.IsNullOrWhiteSpace(tterm))
                        allParts.term = "<" + tterm + ">";
                    else
                        throw new Exception("No namespace found for prefix " + allParts.term + ". You need to define it before saving into Ontorion.");
                }
                else if (System.String.IsNullOrWhiteSpace(allParts.term) && !System.String.IsNullOrWhiteSpace(defaultNs)) // if a default namespace is given, add it to the entity if it does not have a namespace associated
                {
                    allParts.term = "<" + defaultNs + ">";
                }
                else if (!System.String.IsNullOrWhiteSpace(allParts.term) && allParts.term.StartsWith("<") && allParts.term.EndsWith(">"))
                {
                    var tterm = CNLTools.GetCanonicalNs(allParts.term.Substring(1, allParts.term.Length - 2)); // string without < and >
                    allParts.term = "<" + tterm + ">";
                }
                dlName = allParts.Combine();

                return dlName.id;
            }
        }

        public void setPfx2NsSource(Func<string, string> pfx2Ns)
        {
            factory.setPfx2NsSource(pfx2Ns);
        }

        private ICNLFactory factory = null;

        public static MatchCollection ParseReferences(string str)
        {
            return CacheLine.refsRg.Matches(str);
        }

        private static string GetPathSafeString(string path)
        {
            string invalid = new string(Path.GetInvalidPathChars()) + "*";

            foreach (char c in invalid)
            {
                path = path.Replace(c.ToString(), "");
            }

            return path;
        }

        public static void GetReferencePieces(Match match, out string pfx, out string onto, out string ns)
        {
            var pfxT = match.Groups["pfx"];
            var nsT = match.Groups["ns"];
            var ontT = match.Groups["ont"];

            // we have to be sure that onto is path safe. Otherwise, everyWhere we use Path.GetFullPath(Path.Combine(....,onto)) will return an exception!
            onto = pfx = ns = null;
            if (ontT != null)
            {
                onto = GetPathSafeString(ontT.Value);
                if (onto.Trim().StartsWith("\'") && onto.Length > 2)
                    onto = onto.Trim().Substring(1, onto.Length - 2).Replace("\'", "");
            }
            pfx = match.Groups["pfx"].Value;
            ns = match.Groups["ns"].Value.Trim();
            if (ns.Trim().StartsWith("\'") && ns.Length > 2)
                ns = ns.Trim().Substring(1, ns.Length - 2).Replace("\'", "").Trim();
            if (!String.IsNullOrEmpty(ns))
            {
                ns = Regex.Replace(ns, @"\r", "");
                ns = Regex.Replace(ns, @"\n", "");
                ns = Regex.Replace(ns, "'", "");
                ns = CogniPy.CNL.CNLTools.GetCanonicalNs(ns);
            }
        }

        public class CacheLine
        {
            public int start
            {
                get { return val.start; }
            }

            public string line
            {
                get { return val.line; }
                set
                {
                    val = val.Clone();
                    val.line = value;
                    isAnnot = null;
                    isRef = null;
                    isRule = null;
                    isNs = null;
                }
            }

            public string wsBefore
            {
                get { return val.wsBefore; }
            }

            public string text
            {
                get { return val.text; }
            }

            public string wsAfter
            {
                get { return val.wsAfter; }
            }



            public CacheLine(InternalCacheLine val)
            {
                this.val = val;
            }
            InternalCacheLine val;
            private bool? isAnnot;
            private bool? isRule;
            private bool? isRef;
            private bool? isNs;


            static string UPPERL = @"[A-Z]";
            static string LOWERL = @"[a-z]";
            static string LETTER = @"(" + UPPERL + @"|" + LOWERL + @")";
            static string DIGIT = @"[0-9]";
            static string BIGNAME = UPPERL + LOWERL + @"*(\-(" + UPPERL + LOWERL + @"*|" + DIGIT + "+))*";
            static string NAME = LOWERL + @"+(\-(" + LOWERL + @"+|" + DIGIT + @"+))*";
            static string STRING = @"('([^']|''|'[^?=@])+')";
            static string BSTRING = @"(""([^""]|"""")+"")";
            static string ANNOT = "^(?<item>" + BIGNAME + @"):([^|']|(\.\.)|" + STRING + "|" + BSTRING + @")+\." + "$";
            static Regex annot = new Regex(ANNOT, RegexOptions.Compiled);

            // regex to extract the references. Expecting something like : [prefix] namespace (ontology_location)

            public static Regex refsRg = new Regex(@"^([A-Z]{1}[a-z]+?:)?\s*\[(?<pfx>[^\]]*)\]\s*(?<ns>[^\(|\[]*)(\((?<ont>[^\|\[]*)\))?\s*($|(?<dot>\.))", RegexOptions.Compiled | RegexOptions.Multiline);

            private List<string> _pfx;

            public List<string> pfx
            {
                get
                {
                    if (isReference)
                        return _pfx;
                    else
                        return null;
                }
            }

            private List<string> _ont;

            public List<string> ont
            {
                get
                {
                    if (isReference)
                        return _ont;
                    else
                        return null;
                }
            }

            private List<string> _ns;

            public List<string> ns
            {
                get
                {
                    if (isReference)
                        return _ns;
                    else
                        return null;
                }
            }

            // returns true if the cacheline is an annotation
            public bool isAnnotation
            {
                get
                {
                    if (isAnnot.HasValue)
                        return isAnnot.Value;
                    else
                    {
                        isAnnot = annot.IsMatch(val.text.Trim());
                        return isAnnot.Value;
                    }
                }
            }

            // returns true if the cacheline is a rule code
            public bool isRuleCode
            {
                get
                {
                    if (isRule.HasValue)
                        return isRule.Value;
                    else
                    {
                        isRule = val.text.TrimEnd().EndsWith(@"?>.");
                        return isRule.Value;
                    }
                }
            }

            public bool isNamespace
            {
                get
                {
                    if (!isNs.HasValue && !this.isAnnotation)
                    {
                        isNs = false;
                    }
                    else if (!isNs.HasValue && this.isAnnotation)
                    {
                        var dp = val.line.IndexOf(':');
                        var x = val.line.Substring(0, dp);
                        isNs = (x.Trim() == "Namespace");
                    }
                    return isNs.Value;
                }
            }

            // returns true if the cache line is a Reference (annotation + "Reference:...")
            public bool isReference
            {
                get
                {
                    if (!isRef.HasValue && !this.isAnnotation)
                    {
                        isRef = false;
                    }
                    else if (!isRef.HasValue && this.isAnnotation)
                    {
                        var dp = val.line.IndexOf(':');
                        var x = val.line.Substring(0, dp);
                        if (x.Trim() == "References")
                        {
                            isRef = true;
                            var refs = refsRg.Matches(val.line.Substring(dp));
                            if (refs.Count > 0)
                            {
                                _ont = new List<string>();
                                _pfx = new List<string>();
                                _ns = new List<string>();
                            }

                            foreach (Match match in refs)
                            {
                                string onto, pfx, ns;
                                CNLTools.GetReferencePieces(match, out pfx, out onto, out ns);
                                _ont.Add(onto);
                                _pfx.Add(pfx);
                                _ns.Add(ns);
                            }
                        }
                        else
                            isRef = false;
                    }
                    return isRef.Value;
                }
            }

            static public bool CheckIfAnnotation(string txt)
            {
                return annot.IsMatch(txt);
            }

            static public bool CheckIfAnnotation(string txt, out string annotationType)
            {
                var mth = annot.Match(txt);
                if (mth.Success)
                    annotationType = mth.Groups["item"].Value;
                else
                    annotationType = null;

                return mth.Success;
            }
        }

        public class InternalCacheLine
        {
            public int start;

            public string line
            {
                get { return wsBefore + text + wsAfter; }
                set
                {
                    parseFormating(value, out wsBefore, out text, out wsAfter);
                }
            }

            public string wsBefore;
            public string text;
            public string wsAfter;



            public InternalCacheLine Clone()
            {
                var l = new InternalCacheLine();
                l.line = this.line;
                return l;
            }
        }

        private object cacheLinesGuard = new object();
        private List<InternalCacheLine> cacheLines = null;
        private string cachedTxt = null;

        private static void parseFormating(string str, out string wsBefore, out string text, out string wsAfter)
        {
            text = str.TrimStart();
            if (text.Length == 0)
            {
                wsAfter = "";
                wsBefore = "";
                return;
            }
            wsBefore = str.Substring(0, str.Length - text.Length);
            text = text.TrimEnd();
            if (text.Length == 0)
            {
                wsAfter = "";
                return;
            }
            wsAfter = str.Substring(text.Length + wsBefore.Length);
        }



        public IEnumerable<string> SplitDLIntoLines(string txt)
        {
            var dlast = GetDLAst(txt, true);
            var ser = new CogniPy.CNL.DL.Serializer(false);

            foreach (var stmt in dlast.Statements)
                yield return ser.Serialize(stmt);
        }

        public IEnumerable<string> SplitENIntoLines(string txt)
        {
            var lex = factory.getLexer();
            lex.Start(txt);
            int lastIdx = 0;
            int newIdx = 0;
            while (true)
            {
                while (true)
                {
                    var tok = lex.Next();
                    if (tok == null)
                    {
                        if (lex.PeekChar() == 0)
                        {
                            if (lastIdx != newIdx)
                                yield return txt.Substring(lastIdx, newIdx - lastIdx);
                            else
                                yield return txt.Substring(lastIdx);

                            yield break;
                        }
                        else
                        {
                            lex.GetChar();
                            continue;
                        }
                    }

                    if (factory.isEOL(tok))
                    {
                        newIdx = tok.pos + tok.yytext.Length;
                        break;
                    }
                }

                yield return txt.Substring(lastIdx, newIdx - lastIdx);
                lastIdx = newIdx;
            }
        }


        public void LoadCache(string txt)
        {
            lock (cacheLinesGuard)
            {
                List<TOKEN> trace = new List<TOKEN>();
                if (cacheLines == null || txt != cachedTxt)
                {
                    cachedTxt = txt;
                    cacheLines = new List<InternalCacheLine>();
                    if (factory == null)
                        return;
                    var lex = factory.getLexer();
                    lex.Start(txt);
                    int lastIdx = 0;
                    int newIdx = 0;
                    while (true)
                    {
                        while (true)
                        {
                            var tok = lex.Next();
                            trace.Add(tok);
                            if (tok == null)
                            {
                                if (lex.PeekChar() == 0)
                                {
                                    if (lastIdx != newIdx)
                                        cacheLines.Add(new InternalCacheLine() { start = lastIdx, line = txt.Substring(lastIdx, newIdx - lastIdx) });
                                    else
                                        cacheLines.Add(new InternalCacheLine() { start = lastIdx, line = txt.Substring(lastIdx) });

                                    return;
                                }
                                else
                                {
                                    lex.GetChar();
                                    continue;
                                }
                            }

                            if (factory.isEOL(tok))
                            {
                                newIdx = tok.pos + tok.yytext.Length;
                                break;
                            }
                        }

                        cacheLines.Add(new InternalCacheLine() { start = lastIdx, line = txt.Substring(lastIdx, newIdx - lastIdx) });
                        lastIdx = newIdx;
                    }
                }
            }
        }

        public IEnumerable<CacheLine> splitSentences(string txt)
        {
            lock (cacheLinesGuard)
            {
                LoadCache(txt);
                return (from l in cacheLines select new CacheLine(l)).ToList();
            }
        }

        public IEnumerable<CacheLine> splitSentences(string txt, ref int start, ref int end)
        {
            lock (cacheLinesGuard)
            {
                LoadCache(txt);
                GetOverlappingFragment(txt, ref start, ref end);
                int strt = start, ed = end;
                return (from l in cacheLines where l.start >= strt && l.start < ed select new CacheLine(l)).ToList();
            }
        }

        private class GetOverlappingFragmentComparer : IComparer<InternalCacheLine>
        {
            int IComparer<InternalCacheLine>.Compare(InternalCacheLine x, InternalCacheLine y)
            {
                return x.start.CompareTo(y.start);
            }
        }

        public void GetOverlappingFragment(string txt, ref int start, ref int end)
        {
            lock (cacheLinesGuard)
            {
                LoadCache(txt);
                int idx = cacheLines.BinarySearch(new InternalCacheLine() { line = "", start = start }, new GetOverlappingFragmentComparer());
                if (idx >= 0)
                    start = cacheLines[idx].start;
                else
                {
                    idx = ~idx;
                    if (idx < cacheLines.Count)
                    {
                        if (idx > 0)
                            start = cacheLines[idx - 1].start;
                        else
                            start = 0;
                    }
                    else
                    {
                        if (cacheLines.Count > 0)
                            start = cacheLines[cacheLines.Count - 1].start;
                        else
                            start = 0;
                    }
                }

                idx = cacheLines.BinarySearch(new InternalCacheLine() { line = "", start = end }, new GetOverlappingFragmentComparer());
                if (idx >= 0)
                    end = cacheLines[idx].start + cacheLines[idx].line.Length;
                else
                {
                    idx = ~idx;
                    if (idx < cacheLines.Count)
                    {
                        if (idx > 0)
                            end = cacheLines[idx - 1].start + cacheLines[idx - 1].line.Length;
                        else
                            end = 0;
                    }
                    else
                        end = txt.Length - 1;
                }
            }
        }

        public CogniPy.CNL.DL.Paragraph GetENDNL2DLForRoleBody(string text, out string pattern, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2Ns = null)
        {
            pattern = null;

            if (text.Trim() == "") return null;
            var PRE = "If ";
            var POST = " then for the loooooked-for execute <? ?>.\r\n";
            Tools.SYMBOL smb = factory.getParser().Parse(PRE + text + POST);
            if (!factory.isParagraph(smb))   // get null on syntax error
            {
                if (smb is Tools.error)
                {
                    if (throwOnError)
                        throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
                }
                return null;
            }
            else
            {
                if (!factory.ValidateSafeness(smb))
                {
                    if (throwOnError)
                        throw new InvalidOperationException();
                    else
                        return null;
                }
                pattern = this.GetENFromAstSentence(smb, false, true);
                var TF = "then for";
                var thenForPos = pattern.LastIndexOf(TF);
                pattern = pattern.Substring(0, thenForPos - 1);
                pattern = pattern.Substring(PRE.Length);
                return factory.InvConvert(smb, useFullUri, pfx2Ns);
            }
        }

        public CogniPy.CNL.DL.Node GetEN2DLNode(string text, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2Ns = null)
        {
            if (text.Trim() == "") return null;
            Tools.SYMBOL smb = factory.getParser().Parse("Every loooooked-for is " + text + " .");
            if (!factory.isParagraph(smb))   // get null on syntax error
            {
                if (smb is Tools.error)
                {
                    if (throwOnError)
                        throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
                }
                return null;
            }
            else
            {
                if (!factory.ValidateSafeness(smb))
                {
                    if (throwOnError)
                        throw new InvalidOperationException();
                    else
                        return null;
                }
                var stmt = factory.InvConvert(smb, useFullUri, pfx2Ns);
                return (stmt.Statements.First() as CogniPy.CNL.DL.Subsumption).D;
            }
        }

        public object GetENAst(string text, bool throwOnError = true)
        {
            if (text.Trim() == "") return null;
            Tools.SYMBOL smb = factory.getParser().Parse(text);
            if (!factory.isParagraph(smb))   // get null on syntax error
            {
                if (smb is Tools.error)
                {
                    if (throwOnError)
                        throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
                }
                return null;
            }
            else
            {
                if (!factory.ValidateSafeness(smb))
                {
                    if (throwOnError)
                        throw new InvalidOperationException();
                    else
                        return null;
                }
                return smb;
            }
        }

        public string GetENFromAstSentence(object astSent, bool serializeAnnotations = false, bool templateMode = false)
        {
            return factory.Serialize(astSent, serializeAnnotations, out _annotMan, templateMode);
        }

        /// <summary>
        /// Translate a EN-CNL text to a DL ast
        /// </summary>
        /// <param name="text">Input EN-CNL text</param>
        /// <param name="throwOnError"></param>
        /// <returns></returns>
        public CogniPy.CNL.DL.Paragraph GetEN2DLAst(string text, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2ns = null)
        {
            if (text.Trim() == "") return null;
            Tools.SYMBOL smb = factory.getParser().Parse(text);
            if (!factory.isParagraph(smb))   // get null on syntax error
            {
                if (smb is Tools.error)
                {
                    if (throwOnError)
                        throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
                }
                return null;
            }
            else
            {
                if (!factory.ValidateSafeness(smb))
                {
                    if (throwOnError)
                        throw new InvalidOperationException();
                    else
                        return null;
                }
                return factory.InvConvert(smb, useFullUri, pfx2ns);
            }
        }

        public CogniPy.CNL.DL.Paragraph GetDLAstFromEnAst(object smb, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2ns = null)
        {
            if (!factory.ValidateSafeness(smb))
            {
                if (throwOnError)
                    throw new InvalidOperationException();
                else
                    return null;
            }
            return factory.InvConvert((Tools.SYMBOL)smb, useFullUri, pfx2ns);
        }

        static readonly Regex regxForPrefixes = new Regex(@"(?<=\[).*?(?=\])", RegexOptions.Compiled);
        /// <summary>
        /// Returns the prefixes found in the cnl string given
        /// </summary>
        /// <param name="cnlString"></param>
        /// <returns></returns>
        public List<string> getPrefixFromCNL(string cnlString)
        {
            List<string> allPrefixes = new List<string>();

            if (!regxForPrefixes.IsMatch(cnlString))
                return allPrefixes;

            var lex = factory.getLexer();
            lex.Start(cnlString);
            while (true)
            {
                while (true)
                {
                    var tok = lex.Next();
                    if (tok == null)
                    {
                        if (lex.PeekChar() == 0)
                        {
                            return allPrefixes;
                        }
                        else
                        {
                            lex.GetChar();
                            continue;
                        }
                    }

                    if (factory.TagIsName(tok.GetType().Name) || factory.TagIsInstanceName(tok.GetType().Name))
                    {
                        if (regxForPrefixes.IsMatch(tok.yytext))
                        {
                            foreach (var prefMatch in regxForPrefixes.Matches(cnlString))
                            {
                                if (!allPrefixes.Contains(prefMatch.ToString()))
                                    allPrefixes.Add(prefMatch.ToString());
                            }
                        }
                        break;
                    }
                }
            }
        }

        public Dictionary<ARS.EntityKind, CogniPy.CNL.DL.Paragraph> GetENAnnotations2DLAst(bool useFullUri = false, Func<string, string> pfx2ns = null)
        {
            if (annotMan != null)
            {
                var res = new Dictionary<ARS.EntityKind, CogniPy.CNL.DL.Paragraph>();
                var annAx = annotMan.getDLAnnotationAxioms(pfx2ns);
                foreach (var ann in annAx)
                {
                    if (!res.ContainsKey(ann.Key))
                        res.Add(ann.Key, new DL.Paragraph(null) { Statements = new List<DL.Statement>() });
                    res[ann.Key].Statements.AddRange(ann.Value.Select(x => x as DL.Statement).ToList());
                }
                return res;
            }
            else
                return null;
        }

        private Tools.Parser dlParser = new CogniPy.CNL.DL.dl();

        /// <summary>
        /// Convert a string representation of DL to a DL ast.
        /// </summary>
        /// <param name="text"></param>
        /// <param name="throwOnError"></param>
        /// <returns></returns>
        public CogniPy.CNL.DL.Paragraph GetDLAst(string text, bool throwOnError = true)
        {
            if (text.Trim() == "") return null;
            Tools.SYMBOL smb = dlParser.Parse(text);
            if (!(smb is CogniPy.CNL.DL.Paragraph))   // get null on syntax error
            {
                if (smb is Tools.error)
                {
                    if (throwOnError)
                        throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
                }
                return null;
            }
            else
            {
                return smb as CogniPy.CNL.DL.Paragraph;
            }
        }

        public string SerializeDLAst(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
        {
            var ser = new CogniPy.CNL.DL.Serializer(simplifyBrackets);
            return (ser.Serialize(dlast));
        }

        public HashSet<Tuple<CogniPy.ARS.EntityKind, string>> GetDLAstSignature(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
        {
            var ser = new CogniPy.CNL.DL.Serializer(simplifyBrackets);
            ser.Serialize(dlast);
            return ser.GetTaggedSignature();
        }

        public HashSet<Tuple<string, string, string>> GetDLAstDataSignature(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
        {
            var ser = new CogniPy.CNL.DL.Serializer(simplifyBrackets);
            ser.Serialize(dlast);
            return ser.GetDataValues();
        }

        public Tuple<HashSet<Tuple<CogniPy.ARS.EntityKind, string>>, HashSet<Tuple<string, string, string>>> GetDLAstFullSignature(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
        {
            var ser = new CogniPy.CNL.DL.Serializer(simplifyBrackets);
            ser.Serialize(dlast);
            return Tuple.Create(ser.GetTaggedSignature(), ser.GetDataValues());
        }

        public string GetDL(string text, bool throwOnError, bool simplifyBrackets = false)
        {
            var ast = GetEN2DLAst(text, throwOnError);
            return ast == null ? null : SerializeDLAst(ast, simplifyBrackets);
        }

        public string GetENDLFromAst(CogniPy.CNL.DL.IAccept nodeast, bool serializeAnnotations = false, Func<string, string> ns2pfx = null)
        {
            var enast = factory.Convert(nodeast, (ns2pfx == null) ? false : true, ns2pfx);
            return factory.Serialize(enast, serializeAnnotations, out _annotMan);
        }

        public string GetENDLFromAst(CogniPy.CNL.DL.Statement stmast, bool serializeAnnotations = false, Func<string, string> ns2pfx = null)
        {
            var enast = factory.Convert(stmast, (ns2pfx == null) ? false : true, ns2pfx);
            return factory.Serialize(enast, serializeAnnotations, out _annotMan);
        }

        public string GetENDLFromAst(CogniPy.CNL.DL.Paragraph dlast, bool serializeAnnotations = false, Func<string, string> ns2pfx = null)
        {
            var enast = factory.Convert(dlast, (ns2pfx == null) ? false : true, ns2pfx);
            return factory.Serialize(enast, serializeAnnotations, out _annotMan);
        }

        AnnotationManager _annotMan;
        public AnnotationManager annotMan
        {
            get { return _annotMan; }
            set { _annotMan = value; }
        }

        public IEnumerable<string> Morphology(IEnumerable<string> col, string str, string form, bool bigName)
        {
            return factory.Morphology(col, str, form, bigName);
        }

        public string[] GetAllKeywords()
        {
            return factory.GetAllKeywords();
        }

        public List<string> GetModalKeywords()
        {
            List<string> modalities = new List<string>();
            modalities.Add(GetKeyword("MUST"));
            modalities.Add(GetKeyword("SHOULD"));
            modalities.Add(GetKeyword("CAN"));
            modalities.Add(GetKeyword("CANNOT"));
            modalities.Add(GetKeyword("SHOULDNOT"));
            modalities.Add(GetKeyword("MUSTNOT"));

            return modalities;
        }

        public bool IsKeyword(string kw)
        {
            return factory.IsKeyword(kw);
        }

        public bool KeywordExists(string kw)
        {
            return factory.KeywordTagExists(kw);
        }

        public string GetKeyword(string kw)
        {
            return factory.GetKeyword(kw);
        }

        public string GetKeywordTag(string kw)
        {
            return factory.GetKeywordTag(kw);
        }

        private static string MARK = "mark-mark";
        private static Random rnd = new Random(0);

        public KeyValuePair<string, string> GetMorphologyAndTypeOfNextName(string begigning, string suffix)
        {
            string snt = BuildSmallestSentenceStartigWith(begigning, MARK, suffix);
            if (snt == null)
                return new KeyValuePair<string, string>(null, null);
            Tools.SYMBOL smb = factory.getParser().Parse(snt);
            if (!factory.isParagraph(smb))   // get null on syntax error
            {
                //                if (smb is Tools.error)
                //                  System.Diagnostics.Debug.Assert(false);
                return new KeyValuePair<string, string>();
            }
            else
            {
                if (!factory.ValidateSafeness(smb))
                    return new KeyValuePair<string, string>();
                string kind;
                string form;
                factory.FindMark(smb, MARK, out kind, out form);
                return new KeyValuePair<string, string>(kind, form);
            }
        }

        private Regex alnumRegex = new Regex(@"\A[\w|\d|\-|\""|\s]*\Z", RegexOptions.IgnoreCase | RegexOptions.Compiled);

        public bool TryGetTypeOfNextWord(string sentence, string beg, out HashSet<string> whatToLoad, out HashSet<string> keys, out List<KeyValuePair<string, string>> symbols)
        {
            keys = new HashSet<string>();
            whatToLoad = new HashSet<string>();
            symbols = new List<KeyValuePair<string, string>>();

            var props = factory.getParser().CompletionProposals(sentence);

            if (props == null) return false;

            string[] suffixes = factory.GetTagSuffixes();

            foreach (string prop in props)
            {
                string pfx = factory.GetDefaultTagValue(prop);

                if (factory.getParser().TestParse(sentence + " " + pfx))
                {
                    if (factory.TagIsDatatype(prop))
                    {
                        symbols.Add(new KeyValuePair<string, string>(factory.GetSymbol(prop), pfx));
                        keys.Add("<" + factory.GetSymbol(prop) + ">");
                    }
                    if (factory.TagIsName(prop))
                    {
                        if (alnumRegex.IsMatch(beg))
                        {
                            foreach (var suf in suffixes)
                            {
                                var mn = GetMorphologyAndTypeOfNextName(sentence, suf);
                                if (mn.Key != null)
                                {
                                    symbols.Add(mn);
                                    whatToLoad.Add(mn.Key + ":" + mn.Value);
                                }
                            }
                        }
                    }
                    else if (factory.TagIsInstanceName(prop))
                    {
                        if (alnumRegex.IsMatch(beg))
                        {
                            symbols.Add(new KeyValuePair<string, string>(factory.GetSymbol(prop), ""));
                            whatToLoad.Add("instance" + ":NormalForm");
                        }
                    }
                    else
                    {
                        foreach (var suf in suffixes)
                        {
                            if (factory.KeywordTagExists(prop))
                            {
                                var kwds = factory.GetAllMatchingKeywords(prop);
                                var smb = factory.GetSymbol(prop);
                                foreach (var kwd in kwds)
                                    if (kwd != beg.ToLower() && kwd.StartsWith(beg.ToLower()))
                                        if (IsCorrectKeyword(sentence, kwd, suf))
                                        {
                                            symbols.Add(new KeyValuePair<string, string>(smb, ""));
                                            keys.Add(kwd);
                                        }
                            }
                        }
                    }
                }
            }
            return true;
        }

        public List<string> AutoComplete(CogniPy.CNL.DL.Populator populator, string full, out List<KeyValuePair<string, string>> symbols, int max)
        {
            symbols = new List<KeyValuePair<string, string>>();
            var kv = GetSentenceWitoutLastWord(full);
            string sentence = kv.Key;
            string beg = kv.Value;

            if (IsKeyword(GetFirstWord(sentence)))
                sentence = LoCase(sentence);

            HashSet<string> names = new HashSet<string>();
            HashSet<string> bignames = new HashSet<string>();
            HashSet<string> extnames = new HashSet<string>();
            HashSet<string> extbignames = new HashSet<string>();

            HashSet<string> whatToLoad;
            HashSet<string> keys;

            if (!TryGetTypeOfNextWord(sentence, beg, out whatToLoad, out keys, out symbols))
                return null;

            if (populator != null)
            {
                // get the autcompleted elements from the populator (only for the whatToLoad elements (role,instance,...))
                var pops = populator.Populate(sentence, beg, whatToLoad.ToList(), max);
                foreach (var kvx in pops) //populate the names list
                {
                    var x = kvx.Key.Split(':');
                    var v = kvx.Value;
                    if (v != beg)
                    {
                        if (x[1] == "role" || x[1] == "datarole" || x[1] == "concept")
                        {
                            if (x[0] == "i")
                                names.Add(v);
                            else
                                extnames.Add(v);
                        }
                        else
                        {
                            if (x[0] == "i")
                                bignames.Add(v);
                            else
                                extbignames.Add(v);
                        }
                    }
                }
            }

            bool wasDot = false;
            List<string> possibleKeywords = new List<string>();
            foreach (string w in keys) // add the keywords
            {
                if (!possibleKeywords.Contains(w))
                {
                    if (w.Contains(GetKeyword(factory.GetEOLTag())))
                        wasDot = true;
                    else
                    {
                        possibleKeywords.Add(w);
                    }
                }
            }
            possibleKeywords.Sort();

            List<string> possibleNames = new List<string>();
            foreach (string w in names)
            {
                if (!possibleNames.Contains(w))
                    possibleNames.Add(w);
            }
            possibleNames.Sort();

            List<string> possibleBigames = new List<string>();
            foreach (string w in bignames)
            {
                if (!possibleBigames.Contains(w))
                    possibleBigames.Add(w);
            }
            possibleBigames.Sort();

            List<string> possibleWords = new List<string>();
            possibleWords.AddRange(possibleKeywords);

            if (possibleWords.Count < max)
            {
                int delta = max - possibleWords.Count;
                if (possibleNames.Count == 0 || possibleBigames.Count == 0)
                {
                    possibleWords.AddRange(possibleNames.Take(delta));
                    possibleWords.AddRange(possibleBigames.Take(delta));
                    var tot = delta - (possibleNames.Count + possibleBigames.Count);
                    possibleWords.AddRange(extnames.Take(tot / 2));
                    possibleWords.AddRange(extbignames.Take(tot / 2));
                }
                else if (possibleNames.Count + possibleBigames.Count <= delta)
                {
                    possibleWords.AddRange(possibleNames);
                    possibleWords.AddRange(possibleBigames);
                    var tot = delta - (possibleNames.Count + possibleBigames.Count);
                    possibleWords.AddRange(extnames.Take(tot / 2));
                    possibleWords.AddRange(extbignames.Take(tot / 2));
                }
                else
                {
                    if (possibleNames.Count <= delta / 2)
                    {
                        possibleWords.AddRange(possibleNames);
                        possibleWords.AddRange(possibleBigames.Take(delta - possibleNames.Count));
                    }
                    else if (possibleBigames.Count <= delta / 2)
                    {
                        possibleWords.AddRange(possibleNames.Take(delta - possibleBigames.Count));
                        possibleWords.AddRange(possibleBigames);
                    }
                    else
                    {
                        possibleWords.AddRange(possibleNames.Take(delta / 2));
                        possibleWords.AddRange(possibleBigames.Take(delta / 2));
                    }
                }
            }
            if (wasDot)
                possibleWords.Add(GetKeyword(factory.GetEOLTag()));
            return possibleWords;
        }

        /// <summary>
        /// This function has similar functionality that Autocomplete. 
        /// The difference are:
        ///    * no sorting of the result --> we are assuming that sorting is already done correctly by the populator
        ///    * before proposals from the populator and then from the keywords
        ///    * max used to constrain all autocompletition proposals.
        /// </summary>
        /// <param name="populator"></param>
        /// <param name="full"></param>
        /// <param name="symbols"></param>
        /// <param name="max"></param>
        /// <param name="wordsToSkip"></param>
        /// <returns></returns>
        public List<string> AutoComplete2(CogniPy.CNL.DL.Populator populator, string full, out List<KeyValuePair<string, string>> symbols, int max, List<string> wordsToSkip = null)
        {
            if (wordsToSkip == null)
                wordsToSkip = new List<string>();

            symbols = new List<KeyValuePair<string, string>>();
            var kv = GetSentenceWitoutLastWord(full);
            string sentence = kv.Key;
            string beg = kv.Value;

            if (IsKeyword(GetFirstWord(sentence)))
                sentence = LoCase(sentence);

            HashSet<string> names = new HashSet<string>();
            HashSet<string> bignames = new HashSet<string>();
            HashSet<string> extnames = new HashSet<string>();
            HashSet<string> extbignames = new HashSet<string>();

            HashSet<string> whatToLoad;
            HashSet<string> keys;

            if (!TryGetTypeOfNextWord(sentence, beg, out whatToLoad, out keys, out symbols))
                return new List<string>();

            if (populator != null)
            {
                // get the autcompleted elements from the populator (only for the whatToLoad elements (role,instance,...))
                var pops = populator.Populate(sentence, beg, whatToLoad.ToList(), max);
                foreach (var kvx in pops) //populate the names list
                {
                    var x = kvx.Key.Split(':');
                    var v = kvx.Value;
                    if (v != beg)
                    {
                        if (x[1] == "role" || x[1] == "datarole" || x[1] == "concept")
                        {
                            if (x[0] == "i")
                                names.Add(v);
                            else
                                extnames.Add(v);
                        }
                        else
                        {
                            if (x[0] == "i")
                                bignames.Add(v);
                            else
                                extbignames.Add(v);
                        }
                    }
                }
            }

            bool wasDot = false;
            List<string> possibleKeywords = new List<string>();
            foreach (string w in keys) // add the keywords
            {
                if (!possibleKeywords.Contains(w) && !wordsToSkip.Contains(w))
                {
                    if (w.Contains(GetKeyword(factory.GetEOLTag())))
                        wasDot = true;
                    else
                    {
                        possibleKeywords.Add(w);
                    }
                }
            }
            possibleKeywords.Sort();

            List<string> possibleWords = new List<string>();

            foreach (string w in names)
            {
                if (!possibleWords.Contains(w) && !wordsToSkip.Contains(w))
                    possibleWords.Add(w);
            }

            foreach (string w in bignames)
            {
                if (!possibleWords.Contains(w) && !wordsToSkip.Contains(w))
                    possibleWords.Add(w);
            }

            possibleWords.AddRange(possibleKeywords);

            if (wasDot)
                possibleWords.Add(GetKeyword(factory.GetEOLTag()));

            return possibleWords.Take(max).ToList();
        }

        private string GetSentenceTemplate(string sentence)
        {
            var lex = factory.getLexer();
            lex.Start(sentence);
            StringBuilder templ = new StringBuilder();
            while (true)
            {
                var tok = lex.Next();
                if (tok == null)
                {
                    if (lex.PeekChar() == 0)
                    {
                        return templ.ToString();
                    }
                    else
                    {
                        lex.GetChar();
                        continue;
                    }
                }

                if (!IsKeyword(tok.yytext) && !factory.TagIsDatatype(tok.yyname))
                    templ.Append((char.IsLetter(tok.yytext[0]) && (char.ToUpper(tok.yytext[0]) == tok.yytext[0])) ? "Bigname" : "name");
                else
                    templ.Append(tok.yytext);

                templ.Append(" ");

                if (factory.isEOL(tok))
                    break;
            }
            return templ.ToString();
        }

        private static Dictionary<string, string> smallestSentenceCache = new Dictionary<string, string>();
        private bool smallestSentenceCacheIsBuilding = false;

        private void BuildSmallestSentenceCacheForGenerator(Func<string> generator)
        {
            for (int i = 0; i < 1; i++)
            {
                var sent = generator();
                var wrds = sent.Replace("^", "").Split(new char[] { ' ', '.' }, StringSplitOptions.RemoveEmptyEntries);
                StringBuilder sb = new StringBuilder();
                foreach (var w in wrds)
                {
                    List<KeyValuePair<string, string>> symbols;
                    var cpls = AutoComplete(null, sb.ToString() + " ", out symbols, 1);
                    sb.Append((sb.Length != 0 ? " " : "") + w);
                }
            }
        }

        public void BuildSmallestSentenceCache()
        {
            lock (smallestSentenceCache)
            {
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl1);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl2);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl3);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl4);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl5);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl6);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl7);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrl8);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrlWithBuiltins1);
                BuildSmallestSentenceCacheForGenerator(GenerateSwrlWithBuiltins2);
                BuildSmallestSentenceCacheForGenerator(() => GenerateSwrlWithUnaryBuiltinNamed("sine-of"));
                BuildSmallestSentenceCacheForGenerator(() => GenerateSwrlWithUnaryBuiltinNamed("ends-with-string"));
                BuildSmallestSentenceCacheForGenerator(() => GenerateSwrlWithBinaryBuiltinNamed("raised-to-the-power-of"));

                BuildSmallestSentenceCacheForGenerator(GenerateEvery1);
                BuildSmallestSentenceCacheForGenerator(GenerateEvery2);
                BuildSmallestSentenceCacheForGenerator(GenerateEvery3);
                BuildSmallestSentenceCacheForGenerator(GenerateAssert1);
                BuildSmallestSentenceCacheForGenerator(GenerateAssert2);
                BuildSmallestSentenceCacheForGenerator(GenerateAssertOnly);
                BuildSmallestSentenceCacheForGenerator(() => GenerateComplexRoleSubsumption(2));
                BuildSmallestSentenceCacheForGenerator(() => GenerateComplexRoleSubsumption(3));
                BuildSmallestSentenceCacheForGenerator(GenerateDisjoint1);
                BuildSmallestSentenceCacheForGenerator(GenerateDisjoint2);
                BuildSmallestSentenceCacheForGenerator(GenerateDisjointRoles);
                BuildSmallestSentenceCacheForGenerator(GenerateEquiv1);
                BuildSmallestSentenceCacheForGenerator(GenerateEquiv2);
                BuildSmallestSentenceCacheForGenerator(GenerateEquivalentRoles);
                BuildSmallestSentenceCacheForGenerator(GenerateEveryOnly);
                BuildSmallestSentenceCacheForGenerator(GenerateEveryOnlyValue);
                BuildSmallestSentenceCacheForGenerator(GenerateEverySingle1);
                BuildSmallestSentenceCacheForGenerator(GenerateEverySingle2);
                BuildSmallestSentenceCacheForGenerator(GenerateEveryValue2);
                BuildSmallestSentenceCacheForGenerator(GenerateNegativeAssert1);
                BuildSmallestSentenceCacheForGenerator(GenerateNegativeAssert2);
                BuildSmallestSentenceCacheForGenerator(GenerateValueAssert1);
                foreach (var a in AdvancedSamples)
                {
                    var tmp = a;
                    BuildSmallestSentenceCacheForGenerator(() => tmp);
                }
            }
        }

        public static string[] AdvancedSamples = new string[]
        {
"Every man must be a cat.",
"Every man must do-not love a pig.",
"Anything either is a cat, is a dog, is a cat, is a lok or is a pig or-something-else.",
"Something is a cat if-and-only-if-it-either is a dog, is a pig or is a cat that loves a mice.",
"Nothing is a cat.",
"The-one-and-only cat is a pig and is a lok.",
"The cat loves.",
"Every value-of kooo is something ((either 10 or 20) as-well-as different-from 20).",
"Every X that is a cat is-unique-if X is loved by something and X lokes something and X is loved by equal-to something.",
        };

        public string ExampleSmallestSentenceStartigWith(string begining)
        {
            return BuildSmallestSentenceStartigWith(begining, "", "");
        }

        //public string ExampleSmallestSentenceStartigWith(string begining, string mark)
        //{
        //    return BuildSmallestSentenceStartigWith(begining, mark, "");
        //}

        //public SYMBOL CreateAuxiliaryTreeWithMarkForSentenceStartingWith(string begining, string mark)
        //{
        //    string snt = BuildSmallestSentenceStartigWith(begining, mark, "");

        //    if (snt == null)
        //        return null;
        //    string[] snts = snt.Split(' ');
        //    string[] beginings = begining.Split(' ');
        //    for (int i = 0; i < beginings.Length; i++)
        //    {
        //        snts[i] = beginings[i];
        //    }
        //    snt = string.Join(" ", snts);
        //    Tools.SYMBOL smb = factory.getParser().Parse(snt);
        //    if (!factory.isParagraph(smb))   // get null on syntax error
        //    {
        //        //                if (smb is Tools.error)
        //        //                  System.Diagnostics.Debug.Assert(false);
        //        return null;
        //    }
        //    else
        //    {
        //        return smb;
        //    }
        //}

        private string BuildSmallestSentenceStartigWith(string begining, string mrk, string suffix)
        {
            int valretryCnt = 0;
            int retryCnt = 0;
        retry:
            string sentenceX = GetSentenceWitoutLastWord(begining).Key;

            var sentence = GetSentenceTemplate(sentenceX).Trim();

            if (IsKeyword(GetFirstWord(sentence)))
                sentence = LoCase(sentence);

            if (!string.IsNullOrEmpty(mrk))
                sentence += " " + mrk;

            if (!string.IsNullOrEmpty(suffix))
                sentence += " " + suffix;

            lock (smallestSentenceCache)
            {
                if (smallestSentenceCache.Count == 0)
                {
                    if (!factory.LoadSmallestSentenceCache(smallestSentenceCache))
                    {
                        if (!smallestSentenceCacheIsBuilding)
                        {
                            smallestSentenceCacheIsBuilding = true;
                            BuildSmallestSentenceCache();
                            factory.SaveSmallestSentenceCache(smallestSentenceCache);
                            smallestSentenceCacheIsBuilding = false;
                        }
                    }
                }
            }
            lock (smallestSentenceCache)
            {
                if (smallestSentenceCache.ContainsKey(sentence))
                    return smallestSentenceCache[sentence];

                if (!factory.getParser().TestParse(sentence))
                {
                    smallestSentenceCache.Add(sentence, null);
                    return null;
                }
                var ast = factory.getParser().Parse(sentence);
                if (!factory.ValidateSafeness(ast))
                {
                    smallestSentenceCache.Add(sentence, null);
                    return null;
                }
            }

            HashSet<string> sentencekey = new HashSet<string>();
            sentencekey.Add(sentence);

            while (true)
            {
                if (sentence.Length - begining.Length > 300)
                {
                    if (retryCnt < 5)
                    {
                        retryCnt++;
                        goto retry;
                    }
                    else
                        return null;
                }

                var props = factory.getParser().CompletionProposals(sentence);
                if (props == null)
                    return null;

                Dictionary<string, string> propset = new Dictionary<string, string>();

                foreach (string p in props)
                {
                    if (!KeywordExists(p))
                        propset[p] = "";
                    else
                    {
                        if (GetKeyword(p) != suffix/* && !KeyWords.Me.IsProducer(p)*/)
                            propset[p] = "";
                    }
                }

                var proparr = propset.Keys.ToList();
                if (propset.ContainsKey(factory.GetEOLTag()))
                {
                    if (factory.getParser().TestParse(sentence + " " + GetKeyword(factory.GetEOLTag())))
                    {
                        if (factory.ValidateSafeness(factory.getParser().Parse(sentence + " " + GetKeyword(factory.GetEOLTag()))))
                        {
                            sentence += " " + GetKeyword(factory.GetEOLTag());
                            break;
                        }
                        else
                        {
                            if (valretryCnt < 20)
                            {
                                valretryCnt++;
                                goto retry;
                            }
                            else
                            {
                                sentence = null;
                                break;
                            }
                        }
                    }
                }

                if (proparr.Count == 0)
                {
                    sentence += " " + suffix;
                    sentencekey.Add(sentence);
                }
                else
                {
                    while (proparr.Count > 0)
                    {
                        string prop = proparr[rnd.Next(0, proparr.Count)];
                        proparr.Remove(prop);
                        string pfx = factory.GetDefaultTagValue(prop);

                        if (factory.getParser().TestParse(sentence + " " + pfx))
                        {
                            sentence += " " + pfx;
                            sentencekey.Add(sentence);
                            break;
                        }
                    }
                }
            }

            foreach (var key in sentencekey)
                if (!smallestSentenceCache.ContainsKey(key))
                    smallestSentenceCache.Add(key, sentence);

            return sentence;
        }

        public string UpCase(string str)
        {
            if (str.Length > 0)
                return char.ToUpper(str[0]) + str.Substring(1, str.Length - 1);
            else
                return "";
        }

        public string LoCase(string str)
        {
            if (str.Length > 0)
                return char.ToLower(str[0]) + str.Substring(1, str.Length - 1);
            else
                return "";
        }

        public string GetFirstWord(string sentence)
        {
            int i = 0;
            for (i = 0; i < sentence.Length; i++)
            {
                if (char.IsWhiteSpace(sentence[i]))
                {
                    return sentence.Substring(0, i);
                }
            }
            return sentence;
        }

        private bool isPartOfWord(char c)
        {
            return char.IsLetterOrDigit(c) || c == '-' || c == '\"';
        }

        private bool isSpecialSign(char c)
        {
            if (c == '\'' || c == '"')
                return false;
            else
                return char.IsPunctuation(c) && c != '-';
        }

        private char[] sentent = new char[] { '(', ')', '.', '[', ']' };

        public KeyValuePair<string, string> GetSentenceWitoutLastWord(string sentence)
        {
            if (sentence.Length > 0)
            {
                if (sentent.Contains(sentence[sentence.Length - 1]))
                    return new KeyValuePair<string, string>(sentence, "");

                if (isSpecialSign(sentence[sentence.Length - 1]))
                {
                    return new KeyValuePair<string, string>(sentence.Substring(0, sentence.Length - 1), sentence.Substring(sentence.Length - 1, 1).Trim());
                }
            }
            int i = 0;
            for (i = sentence.Length - 1; i >= 0; i--)
            {
                if (!isPartOfWord(sentence[i]))
                {
                    return new KeyValuePair<string, string>(sentence.Substring(0, i + 1), sentence.Substring(i, sentence.Length - i).Trim());
                }
            }
            return new KeyValuePair<string, string>("", sentence.Trim());
        }

        /// <summary>
        /// This function is very similar to GetSentenceWithoutLastWord. 
        /// There is a second one mainly because it was not recognizing correctly the last word when the word had [] inside.
        /// </summary>
        /// <param name="sentence"></param>
        /// <returns></returns>
        public KeyValuePair<string, string> GetSentenceWitoutLastWord2(string sentence)
        {
            int i = 0;
            bool inRef = false;
            for (i = sentence.Length - 1; i >= 0; i--)
            {
                if (sentence[i] == ']')
                    inRef = true;

                if (!inRef && !isPartOfWord(sentence[i]))
                    return new KeyValuePair<string, string>(sentence.Substring(0, i + 1), sentence.Substring(i, sentence.Length - i).Trim());

                if (sentence[i] == '[')
                    inRef = false;
            }
            return new KeyValuePair<string, string>("", sentence.Trim());
        }

        private static Regex splitter = new Regex(@"(?<gr>([^\[\(\)\s\.\,]+(\s*\[[^\]\(\)]+\])?))+|(?<gr>[\,\.])", RegexOptions.IgnoreCase | RegexOptions.Compiled);

        private MatchCollection splitToWords(string snt)
        {
            return splitter.Matches(snt);
        }

        private string dropAAn(string str)
        {
            if (str.ToLower().StartsWith("if "))
                return str.Replace(" an name ", " a name ").Replace(" an thing ", " a thing ");
            else
                return str.Replace(" an name ", " name ").Replace(" a name ", " name ");
        }

        private bool IsCorrectKeyword(string begigning, string kwd, string suf)
        {
            if (kwd.Trim().StartsWith("."))
                return true;

            bool ok = false;
            string reformated = null;
            string snt = BuildSmallestSentenceStartigWith(begigning + kwd + " name", suf != "" ? "name" : "", suf);
            if (snt != null)
            {
                RewriteSentence(snt, out reformated);
                ok = reformated != null;
            }
            if (ok)
            {
                var refWrds = splitToWords(dropAAn(reformated));

                if (!char.IsLetter(kwd[0]))
                {
                    var begWrds = splitToWords(dropAAn(GetSentenceTemplate(begigning + kwd)));
                    if (begWrds.Count < 1) return true;
                    var kwd2 = LoCase(refWrds[begWrds.Count - 1].Value);
                    var kwd3 = LoCase(begWrds[begWrds.Count - 1].Value);
                    return ((kwd2 == "or" && kwd3 == ",") || (kwd3 == "or" && kwd2 == ",")) || kwd2 == kwd3;
                }
                else
                {
                    var begWrds = splitToWords(dropAAn(GetSentenceTemplate(begigning)));
                    var kwd2 = LoCase(refWrds[begWrds.Count].Value);
                    return kwd2 == LoCase(kwd);
                }
            }
            else
            {
                var sentence = begigning + kwd;
                var props = factory.getParser().CompletionProposals(sentence);

                if (props != null)
                {
                    string[] suffixes = factory.GetTagSuffixes();

                    HashSet<string> whatToLoad = new HashSet<string>();

                    foreach (string prop in props)
                    {
                        string pfx = factory.GetDefaultTagValue(prop);

                        if (factory.getParser().TestParse(sentence + " " + pfx))
                        {
                            if (IsKeyword(prop))
                            {
                                var bb = BuildSmallestSentenceStartigWith(sentence + " " + pfx, "", "");
                                return bb != null;
                            }
                        }
                    }
                }
            }
            return false;
        }

        public object RewriteSentence(string str, out string reformated)
        {
            if (IsKeyword(GetFirstWord(str)))
                str = LoCase(str.Trim());

            try
            {
                Tools.SYMBOL smb = factory.getParser().Parse(str);
                if (!factory.isParagraph(smb))   // get null on syntax error
                {
                    reformated = str;
                    return smb == null ? new Tools.error(null) : smb;
                }
                else
                {
                    if (!factory.ValidateSafeness(smb))
                    {
                        reformated = null;
                        return new Tools.error(null);
                    }
                    //TODO
                    //var dl = factory.InvConvert(smb);
                    //var en = GetENDLFromAst(dl);
                    //reformated = UpCase(en.Trim());
                    AnnotationManager annotManLoc;
                    reformated = UpCase(factory.Serialize(smb, false, out annotManLoc).Trim());
                    return null;
                }
            }
            catch (Exception)
            {
                reformated = str;
                return new Tools.error(null);
            }
        }

        public string GetTooltipDesc(KeyValuePair<string, string> kv)
        {
            return factory.GetTooltipDesc(kv);
        }

        public string GetKeywordTip(string ketag)
        {
            return factory.GetKeywordTip(ketag);
        }

        private static int lastNounIdx = 0;

        private string make_noun()
        {
            int r = rnd.Next(9);
            while (r == lastNounIdx)
                r = rnd.Next(9);
            lastNounIdx = r;
            switch (r)
            {
                case 0: return "cat";
                case 1: return "dog";
                case 2: return "monkey";
                case 3: return "giraffe";
                case 4: return "bird";
                case 5: return "fly";
                case 6: return "snake";
                case 7: return "elephant";
                default: return "mouse";
            }
        }

        private static int lastRoleIdx = 0;

        private string make_role()
        {
            int r = rnd.Next(6);
            while (r == lastRoleIdx)
                r = rnd.Next(6);
            lastRoleIdx = r;
            switch (r)
            {
                case 0: return "listen-to";
                case 1: return "love";
                case 2: return "like";
                case 3: return "dislike";
                case 4: return "eat";
                default: return "hate";
            }
        }

        private static int lastDataRoleIdx = 0;

        private string make_datarole()
        {
            int r = rnd.Next(6);
            while (r == lastDataRoleIdx)
                r = rnd.Next(6);
            lastDataRoleIdx = r;
            switch (r)
            {
                case 0: return "have-age";
                case 1: return "have-temperature";
                case 2: return "have-height";
                case 3: return "have-width";
                case 4: return "have-volume";
                default: return "have-speed";
            }
        }

        private static int lastBigNameIdx = 0;

        private string make_big_name()
        {
            int r = rnd.Next(10);
            while (r == lastBigNameIdx)
                r = rnd.Next(10);
            lastBigNameIdx = r;
            switch (r)
            {
                case 0: return "John";
                case 1: return "Mary";
                case 2: return "Mickey";
                case 3: return "Jerry";
                case 4: return "Leon";
                case 5: return "Paul";
                case 6: return "Caroline";
                case 7: return "Sylvia";
                case 8: return "Cloe";
                default: return "Rene";
            }
        }

        public string GenerateAssert1()
        {
            var ex = new DL.Subsumption(null,
                new DL.InstanceSet(null, new DL.InstanceList(null, new DL.NamedInstance(null) { name = make_big_name() + "^" })),
                new DL.Atomic(null) { id = make_noun() }, DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateValueAssert1()
        {
            var ex = new DL.Subsumption(null,
                new DL.InstanceSet(null, new DL.InstanceList(null, new DL.NamedInstance(null) { name = make_big_name() + "^" })),
                new DL.SomeValueRestriction(null,
                    new DL.Atomic(null) { id = make_datarole() },
                    new DL.ValueSet(null, new DL.ValueList(null, new DL.Number(null, rnd.Next(10).ToString())))),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateAssert2()
        {
            var ex = new DL.Subsumption(null,
                new DL.InstanceSet(null, new DL.InstanceList(null, new DL.NamedInstance(null) { name = make_big_name() + "^" })),
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() }),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateAssertOnly()
        {
            var ex = new DL.Subsumption(null,
                new DL.InstanceSet(null, new DL.InstanceList(null, new DL.NamedInstance(null) { name = make_big_name() + "^" })),
                new DL.OnlyRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() }),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateNegativeAssert1()
        {
            var ex = new DL.Subsumption(null,
                new DL.InstanceSet(null, new DL.InstanceList(null, new DL.NamedInstance(null) { name = make_big_name() + "^" })),
                new DL.ConceptNot(null, new DL.Atomic(null) { id = make_noun() }), DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateNegativeAssert2()
        {
            var ex = new DL.Subsumption(null,
                new DL.InstanceSet(null, new DL.InstanceList(null, new DL.NamedInstance(null) { name = make_big_name() + "^" })),
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.ConceptNot(null, new DL.Atomic(null) { id = make_noun() })),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEvery1()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.Atomic(null) { id = make_noun() }, DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEvery2()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() }),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEveryValue2()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.SomeValueRestriction(null,
                    new DL.Atomic(null) { id = make_datarole() },
                new DL.BoundFacets(null, new DL.FacetList(null, new DL.Facet(null, "<", new DL.Number(null, rnd.Next(10).ToString()))))),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEveryOnly()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.OnlyRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() }),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEveryOnlyValue()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.OnlyValueRestriction(null,
                    new DL.Atomic(null) { id = make_datarole() },
                new DL.BoundFacets(null, new DL.FacetList(null, new DL.Facet(null, "<", new DL.Number(null, rnd.Next(10).ToString()))))),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEvery3()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.SomeRestriction(null,
                    new DL.RoleInversion(null, new DL.Atomic(null) { id = make_role() }),
                    new DL.ConceptAnd(null,
                new DL.Atomic(null) { id = make_noun() }
                , new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() })
                )),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEquiv1()
        {
            var ex = new DL.Equivalence(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.Atomic(null) { id = make_noun() }, DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEquiv2()
        {
            var ex = new DL.Equivalence(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() }),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateDisjoint1()
        {
            var ex = new DL.Subsumption(null,
                new DL.Atomic(null) { id = make_noun() + "^" },
                new DL.ConceptNot(null, new DL.Atomic(null) { id = make_noun() })
                , DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateDisjoint2()
        {
            var ex = new DL.Subsumption(null,
                 new DL.Atomic(null) { id = make_noun() + "^" },
                 new DL.ConceptNot(null, new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() })),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEverySingle1()
        {
            var ex = new DL.Subsumption(null,
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() + "^" },
                new DL.Atomic(null) { id = make_noun() }),
                new DL.Atomic(null) { id = make_noun() },
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEverySingle2()
        {
            var ex = new DL.Subsumption(null,
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() + "^" },
                new DL.Atomic(null) { id = make_noun() }),
                new DL.SomeRestriction(null,
                    new DL.Atomic(null) { id = make_role() },
                new DL.Atomic(null) { id = make_noun() }),
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateComplexRoleSubsumption(int n)
        {
            var chain = new DL.RoleChain(null) { List = new List<DL.Node>() };
            for (int i = 0; i < n; i++)
                chain.List.Add(new DL.Atomic(null) { id = make_role() + (i == 0 ? "^" : "") });
            var ex = new DL.ComplexRoleInclusion(null, chain,
                    new DL.Atomic(null) { id = make_role() }
               , DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateEquivalentRoles()
        {
            var ex = new DL.RoleEquivalence(null,
                 new DL.Atomic(null) { id = make_role() + "^" },
                new DL.Atomic(null) { id = make_role() },
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateDisjointRoles()
        {
            var ex = new DL.RoleDisjoint(null,
                 new DL.Atomic(null) { id = make_role() + "^" },
                new DL.Atomic(null) { id = make_role() },
                DL.Statement.Modality.IS);
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrlWithBuiltins1()
        {
            var t1 = make_noun();
            var p1 = new DL.ID(null) { yytext = make_datarole() };
            var p2 = new DL.ID(null) { yytext = make_datarole() };
            var t2 = make_noun();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = "val" + "_0" };
            var v3 = new DL.ID(null) { yytext = t2 + "_1" };
            var v4 = new DL.ID(null) { yytext = "val" + "_1" };
            var rol = new DL.ID(null) { yytext = make_role() + "^" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlDataProperty(null, p1, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v2)),
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v3)),
                new DL.SwrlDataProperty(null, p2, new DL.SwrlIVar(null, v3), new DL.SwrlDVar(null, v4)),
                new DL.SwrlBuiltIn(null, "<",new List<DL.ISwrlObject>(){new DL.SwrlDVar(null, v2),new DL.SwrlDVar(null, v4)})
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlRole(null, rol, new DL.SwrlIVar(null, v1), new DL.SwrlIVar(null, v3))}
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrlWithBuiltins2()
        {
            var t1 = make_noun();
            var p1 = new DL.ID(null) { yytext = make_datarole() };
            var p2 = new DL.ID(null) { yytext = make_datarole() };
            var p3 = new DL.ID(null) { yytext = make_datarole() };
            var p4 = new DL.ID(null) { yytext = make_datarole() + "^" };
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = "val" + "_0" };
            var v3 = new DL.ID(null) { yytext = "val" + "_1" };
            var v4 = new DL.ID(null) { yytext = "val" + "_2" };
            var v5 = new DL.ID(null) { yytext = "val" + "_3" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlDataProperty(null, p1, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v2)),
                new DL.SwrlDataProperty(null, p2, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v3)),
                new DL.SwrlDataProperty(null, p3, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v4)),
                new DL.SwrlBuiltIn(null, "plus",new List<DL.ISwrlObject>(){new DL.SwrlDVar(null, v2),new DL.SwrlDVar(null, v3),new DL.SwrlDVar(null, v4),new DL.SwrlDVar(null, v5)})
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlDataProperty(null, p4, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v5)),
                }
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrlWithUnaryBuiltinNamed(string builtinname)
        {
            var t1 = make_noun();
            var p1 = new DL.ID(null) { yytext = make_datarole() };
            var p4 = new DL.ID(null) { yytext = make_datarole() + "^" };
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = "val" + "_0" };
            var v3 = new DL.ID(null) { yytext = "val" + "_1" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlDataProperty(null, p1, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v2)),
                new DL.SwrlBuiltIn(null, builtinname,new List<DL.ISwrlObject>(){new DL.SwrlDVar(null, v2),new DL.SwrlDVar(null, v3)})
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlDataProperty(null, p4, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v3)),
                }
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrlWithBinaryBuiltinNamed(string builtinname)
        {
            var t1 = make_noun();
            var p1 = new DL.ID(null) { yytext = make_datarole() };
            var p2 = new DL.ID(null) { yytext = make_datarole() };
            var p4 = new DL.ID(null) { yytext = make_datarole() + "^" };
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = "val" + "_0" };
            var v3 = new DL.ID(null) { yytext = "val" + "_1" };
            var v4 = new DL.ID(null) { yytext = "val" + "_2" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlDataProperty(null, p1, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v2)),
                new DL.SwrlDataProperty(null, p2, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v3)),
                new DL.SwrlBuiltIn(null, builtinname,new List<DL.ISwrlObject>(){new DL.SwrlDVar(null, v2),new DL.SwrlDVar(null, v3),new DL.SwrlDVar(null, v4)})
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlDataProperty(null, p4, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v4)),
                }
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl8()
        {
            var t1 = make_noun();
            var p1 = new DL.ID(null) { yytext = make_datarole() + "^" };
            var p2 = new DL.ID(null) { yytext = make_datarole() };
            var t2 = make_noun();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = "val" + "_0" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlDataProperty(null, p1, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v2))
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlDataProperty(null, p2, new DL.SwrlIVar(null, v1), new DL.SwrlDVar(null, v2))}
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl7()
        {
            var t0 = make_noun();
            var c0 = new DL.Atomic(null, new DL.ID(null) { yytext = t0 });

            var c1 = new DL.OnlyValueRestriction(null,
                    new DL.Atomic(null) { id = make_datarole() },
                new DL.BoundFacets(null, new DL.FacetList(null, new DL.Facet(null, "<", new DL.Number(null, rnd.Next(10).ToString())))));

            var t2 = make_noun() + "^";
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });

            var v1 = new DL.ID(null) { yytext = "bnd_0" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c0, new DL.SwrlIVar(null, v1)),
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1))
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v1))},
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl6()
        {
            var t1 = make_noun();
            var t2 = make_noun() + "^";
            var t3 = make_noun();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });
            var c3 = new DL.Atomic(null, new DL.ID(null) { yytext = t3 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v1))
                },
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c3, new DL.SwrlIVar(null, v1))},
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl5()
        {
            var t1 = make_noun();
            var t2 = make_noun() + "^";
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1))},
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v1))},
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl4()
        {
            var t1 = make_noun();
            var t2 = make_noun();
            var r1 = make_role();
            var r2 = make_role();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = t2 + "_0" };
            var rol1 = new DL.Atomic(null, new DL.ID(null) { yytext = r1 + "^" });
            var rol2 = new DL.Atomic(null, new DL.ID(null) { yytext = r2 });

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v2)),
                new DL.SwrlRole(null,rol1.id, new DL.SwrlIVar(null, v1),new DL.SwrlIVar(null, v2))}
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlRole(null,rol2.id, new DL.SwrlIVar(null, v1),new DL.SwrlIVar(null, v2))}
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl3()
        {
            var t1 = make_noun();
            var r1 = make_role();
            var r2 = make_role();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var v1 = new DL.ID(null) { yytext = t1 + "_1" };
            var v2 = new DL.ID(null) { yytext = t1 + "_2" };
            var rol1 = new DL.Atomic(null, new DL.ID(null) { yytext = r1 + "^" });
            var rol2 = new DL.Atomic(null, new DL.ID(null) { yytext = r2 });

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v2)),
                new DL.SwrlRole(null,rol1.id,  new DL.SwrlIVar(null, v1),new DL.SwrlIVar(null, v2))}
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlRole(null,rol2.id,  new DL.SwrlIVar(null, v1),new DL.SwrlIVar(null, v2))}
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl2()
        {
            var t1 = make_noun();
            var t2 = make_noun();
            var t3 = make_noun();
            var r1 = make_role();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });
            var c3 = new DL.Atomic(null, new DL.ID(null) { yytext = t3 });
            var v1 = new DL.ID(null) { yytext = t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = t2 + "_0" };
            var rol1 = new DL.Atomic(null, new DL.ID(null) { yytext = r1 + "^" });

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v2)),
                new DL.SwrlRole(null,rol1.id, new DL.SwrlIVar(null, v1),new DL.SwrlIVar(null, v2))}
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null,c3,new DL.SwrlIVar(null, v2))}
            }
            );
            return GetENDLFromAst(ex);
        }

        public string GenerateSwrl1()
        {
            var t1 = make_noun() + "^";
            var t2 = make_noun();
            var t3 = make_noun();
            var c1 = new DL.Atomic(null, new DL.ID(null) { yytext = t1 });
            var c2 = new DL.Atomic(null, new DL.ID(null) { yytext = t2 });
            var c3 = new DL.Atomic(null, new DL.ID(null) { yytext = t3 });
            var v1 = new DL.ID(null) { yytext = "?" + t1 + "_0" };
            var v2 = new DL.ID(null) { yytext = "?" + t2 + "_0" };

            var ex = new DL.SwrlStatement(null, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null, c1, new DL.SwrlIVar(null, v1)),
                new DL.SwrlInstance(null, c2, new DL.SwrlIVar(null, v2)),
                new DL.SwrlSameAs(null, new DL.SwrlIVar(null, v1),new DL.SwrlIVar(null, v2))}
            }, new DL.SwrlItemList(null)
            {
                list = new List<DL.SwrlItem>(){
                new DL.SwrlInstance(null,c3,new DL.SwrlIVar(null, v2))}
            }
            );
            return GetENDLFromAst(ex);
        }
    }
}