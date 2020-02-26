using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using Tools;

namespace CogniPy.CNL.EN
{

    public class CNLFactory : ICNLFactory
    {
        static Stream FindResourceString(string shortName)
        {
            var name = (from x in System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceNames() where x.EndsWith("." + shortName) select x).First();
            return System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceStream(name);
        }

        public static CogniPy.CNL.EN.endict lex = new CogniPy.CNL.EN.endict(FindResourceString("en.dict"));

        [ThreadStatic]
        static Tools.Parser enParser = null;

        public Tools.Lexer getLexer()
        {
            if (enParser == null)
                enParser = new CogniPy.CNL.EN.endl(new yyendl(), new ErrorHandler(false));
            return enParser.m_lexer;
        }
        public Tools.Parser getParser()
        {
            if (enParser == null)
                enParser = new CogniPy.CNL.EN.endl(new yyendl(), new ErrorHandler(false));
            return enParser;
        }

        public bool isEOL(TOKEN tok)
        {
            return (tok is CogniPy.CNL.EN.END) || (tok is CogniPy.CNL.EN.COMMENT);
        }

        public bool isANNNOT(TOKEN tok)
        {
            return (tok is CogniPy.CNL.EN.COMMENT);
        }

        public bool isParagraph(SYMBOL smb)
        {
            return (smb is CogniPy.CNL.EN.paragraph);
        }

        Func<string, string> pfx2Ns = null;
        public void setPfx2NsSource(Func<string, string> pfx2Ns)
        {
            this.pfx2Ns = pfx2Ns;
        }

        public DL.Paragraph InvConvert(SYMBOL smb, bool useFullUri = false, Func<string, string> pfx2nsEx = null)
        {
            CogniPy.CNL.EN.InvTransform trans = new CogniPy.CNL.EN.InvTransform();
            return trans.Convert(smb as CogniPy.CNL.EN.paragraph, useFullUri, (pfx2nsEx != null) ? pfx2nsEx : pfx2Ns);
        }

        public object Convert(DL.Statement stmast, bool usePrefixes = false, Func<string, string> ns2pfx = null)
        {
            CogniPy.CNL.EN.Transform trans = new CogniPy.CNL.EN.Transform();
            return trans.Convert(stmast, usePrefixes, ns2pfx);
        }

        public object Convert(DL.Paragraph para, bool usePrefixes = false, Func<string, string> ns2pfx = null)
        {
            CogniPy.CNL.EN.Transform trans = new CogniPy.CNL.EN.Transform();
            return trans.Convert(para, usePrefixes, ns2pfx);
        }


        public object Convert(DL.IAccept nodeast, bool usePrefixes = false, Func<string, string> ns2pfx = null)
        {
            CogniPy.CNL.EN.Transform trans = new CogniPy.CNL.EN.Transform();
            return trans.Convert(nodeast, usePrefixes, ns2pfx);
        }

        public string Serialize(object enast, bool serializeAnnotations, out AnnotationManager annotMan, bool templateMode)
        {
            annotMan = new AnnotationManager();
            string ret = null;
            if (enast is IEnumerable<object>)
            {
                StringBuilder retTT = new StringBuilder();
                foreach (var e in enast as IEnumerable<object>)
                {
                    var localManager = new AnnotationManager();
                    retTT.Append(Serialize(e, serializeAnnotations, out localManager, templateMode) + " ");
                    annotMan.appendAnnotations(localManager);
                }
                return retTT.ToString().Trim();
            }
            else
            {
                var ser = new CogniPy.CNL.EN.Serializer2();
                ser.SerializeAnnotations = serializeAnnotations;
                ser.TemplateMode = templateMode;
                if (enast is CogniPy.CNL.EN.paragraph)
                {
                    ret = ser.Serialize(enast as CogniPy.CNL.EN.paragraph);
                }
                else if (enast is CogniPy.CNL.EN.sentence)
                {
                    ret = ser.Serialize(enast as CogniPy.CNL.EN.sentence);
                }
                else if (enast is CogniPy.CNL.EN.orloop)
                {
                    ret = ser.Serialize(enast as CogniPy.CNL.EN.orloop);
                }
                else if (enast is CogniPy.CNL.EN.boundFacets)
                {
                    ret = ser.Serialize(enast as CogniPy.CNL.EN.boundFacets);
                }
                else if (enast is CogniPy.CNL.EN.boundTop)
                {
                    ret = ser.Serialize(enast as CogniPy.CNL.EN.boundTop);
                }
                else if (enast is CogniPy.CNL.EN.boundTotal)
                {
                    ret = ser.Serialize(enast as CogniPy.CNL.EN.boundTotal);
                }
                else
                    throw new NotImplementedException("Could not serialize. Not implemented.");

                annotMan = ser.annotMan;

                return ret;
            }
        }

        static string FromDL(string name, endict.WordKind kind, bool bigName)
        {
            return ENNameingConvention.FromDL(new CogniPy.CNL.DL.DlName() { id = name }, kind, bigName).id;
        }
        public IEnumerable<string> Morphology(IEnumerable<string> col, string str, string form, bool bigName)
        {
            if (form == "NormalForm")
                return from r in col where FromDL(r, endict.WordKind.NormalForm, false).StartsWith(str) select FromDL(r, endict.WordKind.NormalForm, bigName);
            else
            {
                CogniPy.CNL.EN.endict.WordKind k = CogniPy.CNL.EN.endict.WordKind.PastParticiple;
                if (form == "SimplePast")
                    k = CogniPy.CNL.EN.endict.WordKind.SimplePast;
                else if (form == "PluralFormNoun")
                    k = CogniPy.CNL.EN.endict.WordKind.PluralFormNoun;
                else if (form == "PluralFormVerb")
                    k = CogniPy.CNL.EN.endict.WordKind.PluralFormVerb;
                var q = from r in col where FromDL(r, k, bigName).StartsWith(str) select FromDL(r, k, bigName);
                return q;
            }
        }

        public string[] GetAllKeywords()
        {
            return KeyWords.Me.GetAllKeywords();
        }

        public bool KeywordTagExists(string kw)
        {
            return KeyWords.Me.keywordExist(kw);
        }

        public bool IsKeyword(string kw)
        {
            return KeyWords.Me.isKeyword(kw) || kw == "x" || kw == "y" || kw == "z" || kw == "X" || kw == "Y" || kw == "Z";
        }

        public string GetKeyword(string kw)
        {
            return KeyWords.Me.Get(kw);
        }

        public HashSet<string> GetAllMatchingKeywords(string kw)
        {
            return KeyWords.Me.GetAll(kw);
        }

        public string GetKeywordTag(string wrd)
        {
            return KeyWords.Me.GetTag(wrd);
        }

        public void FindMark(SYMBOL smb, string mark, out string kind, out string form)
        {
            CogniPy.CNL.EN.InvTransform trans = new InvTransform(mark);
            trans.Convert(smb as CogniPy.CNL.EN.paragraph);
            var ckind = trans.GetMarkerKind();
            var cform = trans.GetMarkerForm();
            switch (ckind)
            {
                case InvTransform.EntityKind.Concept:
                    kind = "concept"; break;
                case InvTransform.EntityKind.AnyRole:
                    kind = "role"; break;
                case InvTransform.EntityKind.DataRole:
                    kind = "datarole"; break;
                case InvTransform.EntityKind.DataType:
                    kind = "datatype"; break;
                case InvTransform.EntityKind.Instance:
                    kind = "instance"; break;
                default:
                    throw new InvalidOperationException();
            }
            form = cform == endict.WordKind.NormalForm ? "NormalForm" : (cform == endict.WordKind.PastParticiple ? "PastParticiple" : (cform == endict.WordKind.SimplePast ? "SimplePast" : (cform == endict.WordKind.PluralFormNoun ? "PluralFormNoun" : "PluralFormVerb")));
        }

        public string GetDefaultTagValue(string prop)
        {
            string pfx = "";
            if (KeywordTagExists(prop))
            {
                pfx = GetKeyword(prop);
            }
            else if (prop == "NAME")
            {
                pfx = "name";
            }
            else if (prop == "BIGNAME")
            {
                pfx = "Bigname";
            }
            else if (prop == "VERYBIGNAME")
            {
                pfx = "BIGNAME";
            }
            else if (prop == "DBL")
            {
                pfx = "3.14";
            }
            else if (prop == "DBL")
            {
                pfx = "3.14";
            }
            else if (prop == "DTM")
            {
                pfx = "2012-02-16";
            }
            else if (prop == "DUR")
            {
                pfx = "P1234DT12H35M30.234S";
            }
            else if (prop == "NAT")
            {
                pfx = "1";
            }
            else if (prop == "WORDNUM")
            {
                pfx = "one";
            }
            else if (prop == "BOL")
            {
                pfx = "true";
            }
            else if (prop == "NUM")
            {
                pfx = "-1";
            }
            else if (prop == "STR")
            {
                pfx = "\'...\'";
            }
            else if (prop == "COMMA")
            {
                pfx = ",";
            }
            else if (prop == "END")
            {
                pfx = ".";
            }
            else if (prop == "CMP")
            {
                pfx = "<=";
            }
            else if (prop == "EQ")
            {
                pfx = "=";
            }
            else if (prop == "COMMENT")
            {
                pfx = "-";
            }
            else if (prop == "CODE")
            {
                pfx = "<? ?>";
            }
            else if (prop != "EOF")
            {
                System.Diagnostics.Debug.Assert(false);
            }
            return pfx;
        }

        public bool TagIsName(string prop)
        {
            return prop == "NAME";
        }

        public bool TagIsDatatype(string prop)
        {
            return prop == "DBL" || prop == "NAT" || prop == "NUM" || prop == "STR" || prop == "DTM" || prop == "DUR";
        }

        public string[] GetTagSuffixes()
        {
            return new string[] { "", "by" };
        }

        public bool TagIsInstanceName(string prop)
        {
            return prop == "BIGNAME" || prop == "VERYBIGNAME";
        }

        public string GetSymbol(string prop)
        {
            if (prop == "BIGNAME")
            {
                return "Bigname";
            }
            else if (prop == "VERYBIGNAME")
            {
                return "BIGNAME";
            }
            else if (prop == "DBL")
            {
                return "floating point number IEEE-754";
            }
            else if (prop == "NAT")
            {
                return "natural number";
            }
            else if (prop == "WORDNUM")
            {
                return "name of natural number";
            }
            else if (prop == "BOL")
            {
                return "true or false";
            }
            else if (prop == "NUM")
            {
                return "integer";
            }
            else if (prop == "STR")
            {
                return "string";
            }
            else if (prop == "DTM")
            {
                return "dateTime ISO-8601";
            }
            else if (prop == "DUR")
            {
                return "duration ISO-8601";
            }
            else if (prop == "COMMA")
            {
                return "Comma";
            }
            else if (prop == "COMMENT")
            {
                return "Comment";
            }
            else if (prop == "CMP")
            {
                return "Comparator";
            }
            else if (prop == "END")
            {
                return "FullStop";
            }
            else if (KeywordTagExists(prop))
            {
                return "Keyword";
            }
            else
            {
                return null;
            }
        }

        public string GetEOLTag()
        {
            return "END";
        }

        public string GetKeywordTip(string kwtag)
        {
            if (kwtag == "EVERY")
                return "Every cat is an animal.\nEvery chair should have four legs.";
            else if (kwtag == "EVERYTHING")
                return "Every-single-thing that has a cat is a cat-owner.";
            else
                return null;
        }

        public string GetTooltipDesc(KeyValuePair<string, string> kv)
        {
            switch (kv.Key)
            {
                case "role":
                    if (kv.Value == "PastParticiple")
                    {
                        return "verb in past-participle (e.g.:loves, is-part-of)";
                    }
                    else if (kv.Value == "PluralFormVerb")
                    {
                        return "verb in  in plural-form (e.g.:love, are-part-of)";
                    }
                    else if (kv.Value == "SimplePast")
                    {
                        return "verb in simple-past  (e.g.:loved, had)";
                    }
                    else //"NormalForm"
                    {
                        return "verb in present-simple (e.g.: love, be-part-of)";
                    }
                case "concept":
                    if (kv.Value == "PluralFormNoun")
                    {
                        return "noun in plural-form (e.g.: cats, girls, big-ships, young-women)";
                    }
                    else //"NormalForm"
                    {
                        return "noun in singular-form (e.g.: cat, girl, big-ship, young-woman)";
                    }
                case "BIGNAME":
                    return "Globally avaliable proper name (e.g.: CERN, POLAND, EURO)";
                case "Bigname":
                    return "Proper name (e.g.: Mary, John-Smith, Great-Canyon)";
                case "Comparator":
                    return "Comparator (e.g.: =, <>, <, >, <=, >=)";
                case "FullStop":
                    return "full stop sign (it is .)";
                case "Comma":
                    return "comma sign (it is ,)";
                case "floating point number IEEE-754":
                    return "floating point number (e.g.: 0.1, 3.14, 31.4e-1)";
                case "dateTime ISO-8601":
                    return "date and time (e.g.: 2001-10-26, 2001-10-26T21:32:52.321)";
                case "duration ISO-8601":
                    return "duration (e.g.: P3DT5H20M30.123S)";
                case "natural number":
                    return "natural number (e.g.: 0,1,2,3)";
                case "name of natural number":
                    return "name of natural number (e.g.: one, two,...)";
                case "true or false":
                    return "true or false";
                case "integer":
                    return "integer (e.g.:-3,-2,-1,0,1,2,3)";
                case "string":
                    return "string (e.g.: 'a long time ago', 'the one I know', '爱')";
                case "Keyword":
                    return "keyword";
                default:
                    return "";
            }
        }

        public bool LoadSmallestSentenceCache(Dictionary<string, string> cache)
        {
            var stream = FindResourceString("ssc");
            using (System.IO.StreamReader str = new System.IO.StreamReader(stream))
            {
                var ver = str.ReadLine();

                while (true)
                {
                    var k = str.ReadLine();
                    if (k == null) return true;
                    var v = str.ReadLine();
                    if (v == null) return true;
                    if (string.IsNullOrEmpty(v))
                        v = null;
                    cache[k] = v;
                }
            }
        }

        public void SaveSmallestSentenceCache(Dictionary<string, string> cache)
        {
            var pth = Path.Combine(Path.GetDirectoryName(Assembly.GetExecutingAssembly().Location), @"..\..\..\..\..\Common\CNL\ssc");
            var stream = new System.IO.FileStream(pth, System.IO.FileMode.Create);
            using (System.IO.StreamWriter str = new System.IO.StreamWriter(stream))
            {
                Assembly assembly = Assembly.GetExecutingAssembly();
                String version = assembly.FullName.Split(',')[1];
                String fullversion = version.Split('=')[1];

                str.WriteLine(fullversion);
                foreach (var kv in cache)
                {
                    str.WriteLine(kv.Key);
                    str.WriteLine(kv.Value);
                }
            }
        }

        bool valcond(CNL.EN.condition cnd)
        {
            if (cnd is condition_exists)
            {
                var o = (cnd as condition_exists).objectA;
                if (o is objectr_io)
                {
                    if ((o as objectr_io).identobject is identobject_inst)
                    {
                        return false;
                    }
                }
            }
            return true;
        }

        bool validateSingleStmt(CNL.EN.sentence stmt)
        {
            if (stmt is CNL.EN.swrlrule)
            {
                foreach (var cnd in (stmt as CNL.EN.swrlrule).Predicate.Conditions)
                    if (!valcond(cnd))
                        return false;
            }
            if (stmt is CNL.EN.exerule)
            {
                foreach (var cnd in (stmt as CNL.EN.exerule).slp.Conditions)
                    if (!valcond(cnd))
                        return false;
            }
            return true;
        }

        public bool ValidateSafeness(object ast)
        {
            if (ast is CNL.EN.paragraph)
            {
                foreach (var stmt in (ast as CNL.EN.paragraph).sentences)
                {
                    if (!validateSingleStmt(stmt))
                        return false;
                }
            }
            else if (ast is CNL.EN.sentence)
            {
                return validateSingleStmt(ast as CNL.EN.sentence);
            }
            return true;
        }
    }
}
