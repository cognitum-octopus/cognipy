// Malcolm Crowe 1995,2000
// As far as possible the regular expression notation follows that of lex

using System;
using System.Collections;
using System.Globalization;
using System.IO;
using System.Text;

namespace Tools
{

    // We cleverly arrange for the YyLexer class to serialize itself out of a simple integer array.
    // So: to use the lexer generated for a script, include the generated tokens.cs file in the build,
    // This defines classes tokens (subclass of Lexer) and yytokens (subclass of YyLexer). 

    // Call Lexer::Start() to start the input engine going, and then use the
    // Lexer::Next() function to get successive TOKENs.
    // Note that if you are using ParserGenerator, this is done for you. 

    public class YyLexer // we will gather all formerly static definitions for lexing here and in LexerGenerate
    {
        // Deserializing 
        public void GetDfa()
        {
            if (tokens.Count > 0)
                return;
            Serialiser f = new Serialiser(arr);
            f.VersionCheck();
            m_encoding = (Encoding)f.Deserialise();
            toupper = (bool)f.Deserialise();
            cats = (Hashtable)f.Deserialise();
            m_gencat = (UnicodeCategory)f.Deserialise();
            usingEOF = (bool)f.Deserialise();
            starts = (Hashtable)f.Deserialise();
            Dfa.SetTokens(this, starts);
            tokens = (Hashtable)f.Deserialise();
            reswds = (Hashtable)f.Deserialise();
        }
#if (GENTIME)
		public void EmitDfa(TextWriter outFile)
		{
			Console.WriteLine("Serializing the lexer"); 
			Serialiser f = new Serialiser(outFile);
			f.VersionCheck();
			f.Serialise(m_encoding);
			f.Serialise(toupper);
			f.Serialise(cats);  
			f.Serialise(m_gencat);
			f.Serialise(usingEOF);
			f.Serialise(starts);  
			f.Serialise(tokens); 
			f.Serialise(reswds);
			outFile.WriteLine("0};");
		}
#endif
        // support for Unicode character sets
        public Encoding m_encoding = Encoding.UTF8; // overwritten by Deserialize
        public string InputEncoding
        {
            set
            {
                m_encoding = Charset.GetEncoding(value, ref toupper, erh);
            }
        }
        public bool usingEOF = false;
        public bool toupper = false; // for ASCIICAPS
        public Hashtable cats = new Hashtable(); // UnicodeCategory -> Charset
        public UnicodeCategory m_gencat; // not a UsingCat unless all usbale cats in use
                                         // support for lexer states
        public Hashtable starts = new Hashtable(); // string->Dfa
                                                   // support for serialization
        protected int[] arr; // defined in generated tokens class
                             // support for token classes
        public Hashtable types = new Hashtable(); // string->TCreator
        public Hashtable tokens = new Hashtable(); // string->TokClassDef
                                                   // support for reserved word sets
        public Hashtable reswds = new Hashtable(); // int->ResWds
        public ErrorHandler erh;
        public YyLexer(ErrorHandler eh)
        {
            erh = eh;
#if (GENTIME)
			UsingCat(UnicodeCategory.OtherPunctuation);
			m_gencat = UnicodeCategory.OtherPunctuation;
#endif
            new Tfactory(this, "TOKEN", new TCreator(Tokenfactory));
        }
        protected object Tokenfactory(Lexer yyl)
        {
            return new TOKEN(yyl);
        }
#if (GENTIME)
		public Charset UsingCat(UnicodeCategory cat) 
		{
			if (cat==m_gencat) 
			{
				for (int j=0;j<28;j++) 
				{
					if (!Enum.IsDefined(typeof(UnicodeCategory),j))
						continue;
					UnicodeCategory u = (UnicodeCategory)j;
					if (u==UnicodeCategory.Surrogate)
						continue;
					if (cats[u]==null) 
					{
						UsingCat(u);
						m_gencat = u;						
					}
				}
				return (Charset)cats[cat];
			} 
			if (cats[cat]!=null)
				return (Charset)cats[cat];
			Charset rv = new Charset(cat);
			cats[cat] = rv;
			return rv;
		}
		internal void UsingChar(char ch) 
		{
			UnicodeCategory cat = Char.GetUnicodeCategory(ch);
			Charset cs = UsingCat(cat);
			if (cs.m_generic==ch) 
			{
				do 
				{
					if (cs.m_generic==char.MaxValue) 
					{
						cs.m_generic = ch; // all used: this m_generic will never be used
						return;
					}
					cs.m_generic++;
				} while (Char.GetUnicodeCategory(cs.m_generic)!=cs.m_cat ||
					cs.m_chars.Contains(cs.m_generic)); 
				cs.m_chars[cs.m_generic] = true;
			} 
			else
				cs.m_chars[ch] = true;
		}
#endif
        internal char Filter(char ch)
        {
            UnicodeCategory cat = Char.GetUnicodeCategory(ch);
            Charset cs = (Charset)cats[cat];
            if (cs == null)
                cs = (Charset)cats[m_gencat];
            if (cs.m_chars.Contains(ch))
                return ch;
            return cs.m_generic;
        }
        bool testEOF(char ch)
        {
            UnicodeCategory cat = Char.GetUnicodeCategory(ch);
            return (cat == UnicodeCategory.OtherNotAssigned);
        }
#if (GENTIME)
		bool CharIsSymbol(char c)
		{
			UnicodeCategory u = Char.GetUnicodeCategory(c);
			return (u==UnicodeCategory.OtherSymbol || u==UnicodeCategory.ModifierSymbol || 
				u==UnicodeCategory.CurrencySymbol || u==UnicodeCategory.MathSymbol);
		}
		bool CharIsSeparator(char c)
		{
			UnicodeCategory u = Char.GetUnicodeCategory(c);
			return (u==UnicodeCategory.ParagraphSeparator || u==UnicodeCategory.LineSeparator || 
				u==UnicodeCategory.SpaceSeparator);
		}
		internal ChTest GetTest(string name) 
		{
			try {
				object o = Enum.Parse(typeof(UnicodeCategory),name);
				if (o!=null)
				{
					UnicodeCategory cat = (UnicodeCategory)o;
					UsingCat(cat);
					return new ChTest(new CatTest(cat).Test);
				}
			} catch (Exception) {}
			switch (name) 
			{
				case "Symbol": 
					UsingCat(UnicodeCategory.OtherSymbol);
					UsingCat(UnicodeCategory.ModifierSymbol);
					UsingCat(UnicodeCategory.CurrencySymbol);
					UsingCat(UnicodeCategory.MathSymbol);
					return new ChTest(CharIsSymbol); 
				case "Punctuation": 
					UsingCat(UnicodeCategory.OtherPunctuation);
					UsingCat(UnicodeCategory.FinalQuotePunctuation);
					UsingCat(UnicodeCategory.InitialQuotePunctuation);
					UsingCat(UnicodeCategory.ClosePunctuation);
					UsingCat(UnicodeCategory.OpenPunctuation);
					UsingCat(UnicodeCategory.DashPunctuation);
					UsingCat(UnicodeCategory.ConnectorPunctuation);
					return new ChTest(Char.IsPunctuation); 
					/*			case "PrivateUse": 
									UsingCat(UnicodeCategory.PrivateUse);
									return new ChTest(Char.IsPrivateUse); */
				case "Separator": 
					UsingCat(UnicodeCategory.ParagraphSeparator);
					UsingCat(UnicodeCategory.LineSeparator);
					UsingCat(UnicodeCategory.SpaceSeparator);
					return new ChTest(CharIsSeparator); 
				case "WhiteSpace": 
					UsingCat(UnicodeCategory.Control);
					UsingCat(UnicodeCategory.ParagraphSeparator);
					UsingCat(UnicodeCategory.LineSeparator);
					UsingCat(UnicodeCategory.SpaceSeparator);
					return new ChTest(Char.IsWhiteSpace); 
				case "Number": 
					UsingCat(UnicodeCategory.OtherNumber);
					UsingCat(UnicodeCategory.LetterNumber);
					UsingCat(UnicodeCategory.DecimalDigitNumber);
					return new ChTest(Char.IsNumber); 
				case "Digit": 
					UsingCat(UnicodeCategory.DecimalDigitNumber);
					return new ChTest(Char.IsDigit); 
					/*			case "Mark": 
									UsingCat(UnicodeCategory.EnclosingMark);
									UsingCat(UnicodeCategory.SpacingCombiningMark);
									UsingCat(UnicodeCategory.NonSpacingMark);
									return new ChTest(Char.IsMark); */
				case "Letter": 
					UsingCat(UnicodeCategory.OtherLetter);
					UsingCat(UnicodeCategory.ModifierLetter);
					UsingCat(UnicodeCategory.TitlecaseLetter);
					UsingCat(UnicodeCategory.LowercaseLetter);
					UsingCat(UnicodeCategory.UppercaseLetter);
					return new ChTest(Char.IsLetter); 
				case "Lower": 
					UsingCat(UnicodeCategory.LowercaseLetter);
					return new ChTest(Char.IsLower); 
				case "Upper": 
					UsingCat(UnicodeCategory.UppercaseLetter);
					return new ChTest(Char.IsUpper); 
				case "EOF":
					UsingCat(UnicodeCategory.OtherNotAssigned);
					UsingChar((char)0xFFFF);
					usingEOF=true;
					return new ChTest(testEOF);
				default:
					erh.Error(new CSToolsException(24,"No such Charset "+name));
					break;
			}
			return new ChTest(Char.IsControl); // not reached
		}
#endif
        public virtual TOKEN OldAction(Lexer yyl, ref string yytext, int action, ref bool reject)
        {
            return null;
        }
        public IEnumerator GetEnumerator()
        {
            return tokens.Values.GetEnumerator();
        }
    }
    public class LineManager
    {
        public int lines = 1;  // for error messages etc
        public int end = 0;  // high water mark of positions
        public LineList list = null;
        public LineManager() { }
        public void newline(int pos)
        {
            lines++;
            backto(pos);
            list = new LineList(pos, list);
        }
        public void backto(int pos)
        {
            if (pos > end)
                end = pos;
            while (list != null && list.head >= pos)
            {
                list = list.tail;
                lines--;
            }
        }
        public void comment(int pos, int len)
        { // only for C-style comments not C++
            if (pos > end)
                end = pos;
            if (list == null)
            {
                list = new LineList(0, list);
                lines = 1;
            }
            list.comments = new CommentList(pos, len, list.comments);
        }
    }
#if (GENTIME)
	public abstract class TokensGen : GenBase
	{
		public TokensGen(ErrorHandler eh):base(eh) {}
		protected bool m_showDfa;
		public YyLexer m_tokens; // the YyLexer class under construction
		// %defines in script
		public Hashtable defines = new Hashtable(); // string->string
		// support for Nfa networks
		int state = 0;
		public int NewState() { return ++state; } // for LNodes
		public ObjectList states = new ObjectList(); // Dfa
		public string FixActions(string str)
		{
			return str.Replace("yybegin","yym.yy_begin").Replace("yyl","(("+m_outname+")yym)");
		}
	}
#endif
    // support for Unicode character sets

    internal delegate bool ChTest(char ch);

    public class CatTest
    {
        UnicodeCategory cat;
        public CatTest(UnicodeCategory c) { cat = c; }
        public bool Test(char ch)
        {
            return Char.GetUnicodeCategory(ch) == cat;
        }
    }

    public class Charset
    {
        internal UnicodeCategory m_cat;
        internal char m_generic; // not explicitly Using'ed allUsed
        internal Hashtable m_chars = new Hashtable(); // char->bool
        Charset() { }
#if (GENTIME)
		internal Charset(UnicodeCategory cat) 
		{ 
			m_cat = cat;
			for (m_generic=char.MinValue;Char.GetUnicodeCategory(m_generic)!=cat;m_generic++)
				;
			m_chars[m_generic] = true;
		}
#endif
        public static Encoding GetEncoding(string enc, ref bool toupper, ErrorHandler erh)
        {
            switch (enc)
            {
#if !SILVERLIGHT
                case "": return Encoding.Default; // locale-specific
                case "ASCII": return Encoding.ASCII;
                case "ASCIICAPS": toupper = true; return Encoding.ASCII; // toupper is currently ignored in scripts
                case "UTF7": return Encoding.UTF7;
#endif
                case "UTF8": return Encoding.UTF8;
                case "Unicode": return Encoding.Unicode;
                default:
                    try
                    {
#if! SILVERLIGHT
                        if (char.IsDigit(enc[0]))
                            return Encoding.GetEncoding(int.Parse(enc)); // codepage
#endif
                        return Encoding.GetEncoding(enc);
                    }
                    catch (Exception)
                    {
                        erh.Error(new CSToolsException(43, "Warning: Encoding " + enc + " unknown: ignored"));
                    }
                    break;
            }
            return Encoding.UTF8;
        }
        public static object Serialise(object o, Serialiser s)
        {
            if (s == null)
                return new Charset();
            Charset c = (Charset)o;
            if (s.Encode)
            {
                s.Serialise((int)c.m_cat);
                s.Serialise(c.m_generic);
                s.Serialise(c.m_chars);
                return null;
            }
            c.m_cat = (UnicodeCategory)s.Deserialise();
            c.m_generic = (char)s.Deserialise();
            c.m_chars = (Hashtable)s.Deserialise();
            return c;
        }
    }

    // Support for runtime object creation

    public delegate object TCreator(Lexer yyl);

    public class Tfactory
    {
        public static object create(string cls_name, Lexer yyl)
        {
            TCreator cr = (TCreator)yyl.tokens.types[cls_name];
            // Console.WriteLine("TCreating {0} <{1}>",cls_name,yyl.yytext);
            if (cr == null)
                yyl.tokens.erh.Error(new CSToolsException(6, yyl, cls_name, String.Format("no factory for {0}", cls_name)));
            try
            {
                return cr(yyl);
            }
            catch (CSToolsException x)
            {
                yyl.tokens.erh.Error(x);
            }
            catch (Exception e)
            {
                yyl.tokens.erh.Error(new CSToolsException(7, yyl, cls_name,
                    String.Format("Line {0}: Create of {1} failed ({2})", yyl.Saypos(yyl.m_pch), cls_name, e.Message)));
            }
            int j = cls_name.LastIndexOf('_');
            if (j > 0)
            {
                cr = (TCreator)yyl.tokens.types[cls_name.Substring(0, j)];
                if (cr != null)
                    return cr(yyl);
            }
            return null;
        }
        public Tfactory(YyLexer tks, string cls_name, TCreator cr)
        {
            tks.types[cls_name] = cr;
        }
    }
    public class SourceLineInfo
    {
        public int lineNumber;
        public int charPosition;
        public int startOfLine;
        public int endOfLine;
        public int rawCharPosition;
        public Lexer lxr = null;
        public SourceLineInfo(int pos) // this constructor is not used in anger
        {
            lineNumber = 1;
            startOfLine = 0;
            endOfLine = rawCharPosition = charPosition = pos;
        }
        public SourceLineInfo(LineManager lm, int pos)
        {
            lineNumber = lm.lines;
            startOfLine = 0;
            endOfLine = lm.end;
            charPosition = pos;
            rawCharPosition = pos;
            for (LineList p = lm.list; p != null; p = p.tail, lineNumber--)
                if (p.head > pos)
                    endOfLine = p.head;
                else
                {
                    startOfLine = p.head + 1;
                    rawCharPosition = p.getpos(pos);
                    charPosition = pos - startOfLine + 1;
                    break;
                }
        }
        public SourceLineInfo(Lexer lx, int pos) : this(lx.m_LineManager, pos) { lxr = lx; } // 4.5c
        public override string ToString()
        {
            return "Line " + lineNumber + ", char " + rawCharPosition;
        }
        public string sourceLine { get { if (lxr == null) return ""; return lxr.sourceLine(this); } }
    }
    public class LineList
    {
        public int head;
        public CommentList comments = null;
        public LineList tail; // previous line!
        public LineList(int h, LineList t)
        {
            head = h;
            comments = null;
            tail = t;
        }
        public int getpos(int pos)
        {
            int n = pos - head;
            for (CommentList c = comments; c != null; c = c.tail)
                if (pos > c.spos)
                    n += c.len;
            return n;
        }
    }
    public class CommentList
    {
        public int spos, len;
        public CommentList tail = null;
        public CommentList(int st, int ln, CommentList t)
        {
            spos = st; len = ln;
            tail = t;
        }
    }

    // the following class gets rid of comments for us

    public class CsReader
    {
        public string fname = "";
        TextReader m_stream;
        public LineManager lm = new LineManager();
        int back; // one-char pushback
        enum State
        {
            copy, sol, c_com, cpp_com, c_star, at_eof, transparent
        }
        State state;
        int pos = 0;
        bool sol = true;
        public CsReader(string data)
        {
            m_stream = new StringReader(data);
            state = State.copy;
            back = -1;
        }
        public CsReader(string fileName, Encoding enc)
        {
            fname = fileName;
            FileStream fs = new FileStream(fileName, FileMode.Open, FileAccess.Read);
            m_stream = new StreamReader(fs, enc);
            state = State.copy; back = -1;
        }
        public CsReader(CsReader inf, Encoding enc)
        {
            fname = inf.fname;
            if (inf.m_stream is StreamReader)
                m_stream = new StreamReader(((StreamReader)inf.m_stream).BaseStream, enc);
            else
                m_stream = new StreamReader(inf.m_stream.ReadToEnd());
            state = State.copy; back = -1;
        }
        public bool Eof() { return state == State.at_eof; }
        public int Read(char[] arr, int offset, int count)
        {
            int c, n;
            for (n = 0; count > 0; count--, n++)
            {
                c = Read();
                if (c < 0)
                    break;
                arr[offset + n] = (char)c;
            }
            return n;
        }
        public string ReadLine()
        {
            int c = 0, n;
            char[] buf = new char[1024];
            int count = 1024;
            for (n = 0; count > 0; count--)
            {
                c = Read();
                if (((char)c) == '\r')
                    continue;
                if (c < 0 || ((char)c) == '\n')
                    break;
                buf[n++] = (char)c;
            }
            if (c < 0)
                state = State.at_eof;
            return new string(buf, 0, n);
        }
        public int Read()
        {
            int c, comlen = 0;
            if (state == State.at_eof)
                return -1;
            while (true)
            {
                // get a character
                if (back >= 0)
                { // back is used only in copy mode
                    c = back; back = -1;
                }
                else if (state == State.at_eof)
                    c = -1;
                else
                    c = m_stream.Read();
                if (c == '\r')
                    continue;
                while (sol && c == '#') // deal with #line directive
                {
                    while (c != ' ')
                        c = m_stream.Read();
                    lm.lines = 0;
                    while (c == ' ')
                        c = m_stream.Read();
                    while (c >= '0' && c <= '9')
                    {
                        lm.lines = lm.lines * 10 + (c - '0');
                        c = m_stream.Read();
                    }
                    while (c == ' ')
                        c = m_stream.Read();
                    if (c == '"')
                    {
                        fname = "";
                        c = m_stream.Read();
                        while (c != '"')
                        {
                            fname += c;
                            c = m_stream.Read();
                        }
                    }
                    while (c != '\n')
                        c = m_stream.Read();
                    if (c == '\r')
                        c = m_stream.Read();
                }
                if (c < 0)
                {  // at EOF we must leave the loop
                    if (state == State.sol)
                        c = '/';
                    state = State.at_eof;
                    pos++;
                    return c;
                }
                sol = false;
                // otherwise work through a state machine
                switch (state)
                {
                    case State.copy:
                        if (c == '/')
                            state = State.sol;
                        else
                        {
                            if (c == '\n')
                            {
                                lm.newline(pos);
                                sol = true;
                            }
                            pos++;
                            return c;
                        }
                        continue;
                    case State.sol: // solidus '/'
                        if (c == '*')
                            state = State.c_com;
                        else if (c == '/')
                        {
                            comlen = 2;
                            state = State.cpp_com;
                        }
                        else
                        {
                            back = c;
                            state = State.copy;
                            pos++;
                            return '/';
                        }
                        continue;
                    case State.c_com:
                        comlen++;
                        if (c == '\n')
                        {
                            lm.newline(pos);
                            comlen = 0;
                            sol = true;
                        }
                        if (c == '*')
                            state = State.c_star;
                        continue;
                    case State.c_star:
                        comlen++;
                        if (c == '/')
                        {
                            lm.comment(pos, comlen);
                            state = State.copy;
                        }
                        else if (c == '*')   // 4.7j
                            state = State.c_star; // 4.7j
                        else
                            state = State.c_com;
                        continue;
                    case State.cpp_com:
                        if (c == '\n')
                        {
                            state = State.copy;
                            sol = true;
                            pos++;
                            return c;
                        }
                        else
                            comlen++;
                        continue;
                }
            }
            /* notreached */
        }
    }
    public class SYMBOL
    {
        public object m_dollar;
        public static implicit operator int(SYMBOL s) // 4.0c
        {
            int rv = 0;
            object d;
            while (((d = s.m_dollar) is SYMBOL) && d != null)
                s = (SYMBOL)d;
            try
            {
                rv = (int)d;
            }
            catch (Exception e)
            {
                Console.WriteLine("attempt to convert from " + s.m_dollar.GetType());
                throw e;
            }
            return rv;
        }
        public int pos;
        public int Line { get { return yylx.sourceLineInfo(pos).lineNumber; } }
        public int Position { get { return yylx.sourceLineInfo(pos).rawCharPosition; } }
        public string Pos { get { return yylx.Saypos(pos); } }
        protected SYMBOL() { }
        public Lexer yylx;
        public object yylval { get { return m_dollar; } set { m_dollar = value; } }
        public SYMBOL(Lexer yyl) { yylx = yyl; }
        public virtual int yynum { get { return 0; } }
        public virtual bool IsTerminal() { return false; }
        public virtual bool IsAction() { return false; }
        public virtual bool IsCSymbol() { return false; }
        public Parser yyps = null;
        public YyParser yyact { get { return (yyps != null) ? yyps.m_symbols : null; } }
        public SYMBOL(Parser yyp) { yyps = yyp; yylx = yyp != null ? yyp.m_lexer : null; }
        public virtual bool Pass(YyParser syms, int snum, out ParserEntry entry)
        {
            ParsingInfo pi = (ParsingInfo)syms.symbolInfo[yynum];
            if (pi == null)
            {
                string s = string.Format("No parsinginfo for symbol {0} {1}", yyname, yynum);
                syms.erh.Error(new CSToolsFatalException(9, yylx, yyname, s));
            }
            bool r = pi.m_parsetable.Contains(snum);
            entry = r ? ((ParserEntry)pi.m_parsetable[snum]) : null;
            return r;
        }
        public virtual string yyname { get { return "SYMBOL"; } }
        public override string ToString() { return yyname; }
        public virtual bool Matches(string s) { return false; }
        public virtual void Print() { Console.WriteLine(ToString()); }
        // 4.2a Support for automatic display of concrete syntax tree
        public ObjectList kids = new ObjectList();
        void ConcreteSyntaxTree(string n)
        {
            if (this is error)
                Console.WriteLine(n + " " + ToString());
            else
                Console.WriteLine(n + "-" + ToString());
            int j = 0;
            foreach (SYMBOL s in kids)
                s.ConcreteSyntaxTree(n + ((j++ == kids.Count - 1) ? "  " : " |"));
        }
        public virtual void ConcreteSyntaxTree() { ConcreteSyntaxTree(""); }
    }

    //public class PartialSymbol : SYMBOL
    //{
    //    public object _;
    //    public PartialSymbol(Parser yyp) : base(yyp) { }
    //}

    public class TOKEN : SYMBOL
    {
        public string yytext { get { return m_str; } set { m_str = value; } }
        string m_str;
        public TOKEN(Parser yyp) : base(yyp) { }
        public TOKEN(Lexer yyl) : base(yyl) { if (yyl != null) m_str = yyl.yytext; }
        public TOKEN(Lexer yyl, string s) : base(yyl) { m_str = s; }
        protected TOKEN() { }
        public override bool IsTerminal() { return true; }
        int num = 1;
        public override bool Pass(YyParser syms, int snum, out ParserEntry entry)
        {
            if (yynum == 1)
            {
                Literal lit = (Literal)syms.literals[yytext];
                if (lit != null)
                    num = (int)lit.m_yynum;
            }
            ParsingInfo pi = (ParsingInfo)syms.symbolInfo[yynum];
            if (pi == null)
            {
                string s = string.Format("Parser does not recognise literal {0}", yytext);
                syms.erh.Error(new CSToolsFatalException(9, yylx, yyname, s));
            }
            bool r = pi.m_parsetable.Contains(snum);
            entry = r ? ((ParserEntry)pi.m_parsetable[snum]) : null;
            return r;
        }
        public override string yyname { get { return "TOKEN"; } }
        public override int yynum { get { return num; } }
        public override bool Matches(string s) { return s.Equals(m_str); }
        public override string ToString() { return yyname + "<" + yytext + ">"; }
        public override void Print() { Console.WriteLine(ToString()); }
    }
    public class Lexer
    {
        public bool m_debug = false;
        // source line control
        public string m_buf;
        internal LineManager m_LineManager = new LineManager(); // 4.5b see EOF
        public SourceLineInfo sourceLineInfo(int pos)
        {
            return new SourceLineInfo(this, pos); // 4.5c
        }
        public string sourceLine(SourceLineInfo s)
        {
            // This is the sourceLine after removal of comments
            // The position in this line is s.charPosition
            // If you want the comments as well, then you should re-read the source file
            // and the position in the line is s.rawCharPosition
            return m_buf.Substring(s.startOfLine, s.endOfLine - s.startOfLine);
        }
        public string Saypos(int pos)
        {
            return sourceLineInfo(pos).ToString();
        }

        // the heart of the lexer is the DFA
        public Dfa m_start { get { return (Dfa)m_tokens.starts[m_state]; } }
        public string m_state = "YYINITIAL"; // exposed for debugging (by request)

        public Lexer(YyLexer tks)
        {
            m_state = "YYINITIAL";
            tokens = tks;
        }

        YyLexer m_tokens;
        public YyLexer tokens
        {
            get { return m_tokens; }  // 4.2d
            set { m_tokens = value; m_tokens.GetDfa(); }
        }
        public string yytext; // for collection when a TOKEN is created
        public int m_pch = 0;
        public int yypos { get { return m_pch; } }

        public void yy_begin(string newstate)
        {
            m_state = newstate;
        }
        bool m_matching;
        int m_startMatch;
        // match a Dfa against lexer's input
        bool Match(ref TOKEN tok, Dfa dfa, int depth = 0)
        {
            char ch = PeekChar();
            int op = m_pch, mark = 0;
            Dfa next;

            if (m_debug)
            {
                Console.Write("state {0} with ", dfa.m_state);
                if (char.IsLetterOrDigit(ch) || char.IsPunctuation(ch))
                    Console.WriteLine(ch);
                else
                    Console.WriteLine("#" + (int)ch);
            }
            if (dfa.m_actions != null)
            {
                mark = Mark();
            }
            if (// ch==0 || 
                (next = ((Dfa)dfa.m_map[m_tokens.Filter(ch)])) == null)
            {
                if (m_debug)
                    Console.Write("{0} no arc", dfa.m_state);
                if (dfa.m_actions != null)
                {
                    if (m_debug)
                        Console.WriteLine(" terminal");
                    return TryActions(dfa, ref tok); // fails on REJECT
                }
                if (m_debug)
                    Console.WriteLine(" fails");
                return false;
            }
            Advance();
            if (depth > 16)
            {
            }
            if (!Match(ref tok, next, depth + 1))
            { // rest of string fails
                if (m_debug)
                    Console.WriteLine("back to {0} with {1}", dfa.m_state, ch);
                if (dfa.m_actions != null)
                { // this is still okay at a terminal
                    if (m_debug)
                        Console.WriteLine("{0} succeeds", dfa.m_state);
                    Restore(mark);
                    return TryActions(dfa, ref tok);
                }
                if (m_debug)
                    Console.WriteLine("{0} fails", dfa.m_state);
                return false;
            }
            if (dfa.m_reswds >= 0)
            {
                ((ResWds)m_tokens.reswds[dfa.m_reswds]).Check(this, ref tok);
            }
            if (m_debug)
            {
                Console.Write("{0} matched ", dfa.m_state);
                if (m_pch <= m_buf.Length)
                    Console.WriteLine(m_buf.Substring(op, m_pch - op));
                else
                    Console.WriteLine(m_buf.Substring(op));
            }
            return true;
        }

        // start lexing
        public void Start(StreamReader inFile)
        {
            m_state = "YYINITIAL"; // 4.3e
            m_LineManager.lines = 1; //
            m_LineManager.list = null; // 
            inFile = new StreamReader(inFile.BaseStream, m_tokens.m_encoding);
            m_buf = inFile.ReadToEnd();
            if (m_tokens.toupper)
                m_buf = m_buf.ToUpper();
            for (m_pch = 0; m_pch < m_buf.Length; m_pch++)
                if (m_buf[m_pch] == '\n')
                    m_LineManager.newline(m_pch);
            m_pch = 0;
        }
        public void Start(CsReader inFile)
        {
            m_state = "YYINITIAL"; // 4.3e
            inFile = new CsReader(inFile, m_tokens.m_encoding);
            m_LineManager = inFile.lm;
            if (!inFile.Eof())
                for (m_buf = inFile.ReadLine(); !inFile.Eof(); m_buf += inFile.ReadLine())
                    m_buf += "\n";
            if (m_tokens.toupper)
                m_buf = m_buf.ToUpper();
            m_pch = 0;
        }
        public void Start(string buf)
        {
            m_state = "YYINITIAL"; // 4.3e
            m_LineManager.lines = 1; //
            m_LineManager.list = null; //
            m_buf = buf + "\n";
            for (m_pch = 0; m_pch < m_buf.Length; m_pch++)
                if (m_buf[m_pch] == '\n')
                    m_LineManager.newline(m_pch);
            if (m_tokens.toupper)
                m_buf = m_buf.ToUpper();
            m_pch = 0;
        }
        public TOKEN Next()
        {
            TOKEN rv = null;
            while (PeekChar() != 0)
            {
                Matching(true);
                if (!Match(ref rv, (Dfa)m_tokens.starts[m_state]))
                {
                    if (yypos == 0)
                        System.Console.Write("Check text encoding.. ");
                    int c = PeekChar();
                    m_tokens.erh.Error(new CSToolsStopException(2, this, "illegal character <" + (char)c + "> " + c));
                    return null;
                }
                Matching(false);
                if (rv != null)
                { // or special value for empty action? 
                    rv.pos = m_pch - yytext.Length;
                    return rv;
                }
            }
            return null;
        }
        bool TryActions(Dfa dfa, ref TOKEN tok)
        {
            int len = m_pch - m_startMatch;
            if (len == 0)
                return false;
            if (m_startMatch + len <= m_buf.Length)
                yytext = m_buf.Substring(m_startMatch, len);
            else // can happen with {EOF} rules
                yytext = m_buf.Substring(m_startMatch);
            // actions is a list of old-style actions for this DFA in order of priority
            // there is a list because of the chance that any of them may REJECT
            Dfa.Action a = dfa.m_actions;
            bool reject = true;
            while (reject && a != null)
            {
                int action = a.a_act;
                reject = false;
                a = a.a_next;
                if (a == null && dfa.m_tokClass != "")
                { // last one might not be an old-style action
                    if (m_debug)
                        Console.WriteLine("creating a " + dfa.m_tokClass);
                    tok = (TOKEN)Tfactory.create(dfa.m_tokClass, this);
                }
                else
                {
                    tok = m_tokens.OldAction(this, ref yytext, action, ref reject);
                    if (m_debug && !reject)
                        Console.WriteLine("Old action " + action);
                }
            }
            return !reject;
        }
        public char PeekChar()
        {
            if (m_pch < m_buf.Length)
                return m_buf[m_pch];
            if (m_pch == m_buf.Length && m_tokens.usingEOF)
                return (char)0xFFFF;
            return (char)0;
        }
        public void Advance() { ++m_pch; }
        public virtual int GetChar()
        {
            int r = PeekChar(); ++m_pch;
            return r;
        }
        public void UnGetChar() { if (m_pch > 0) --m_pch; }
        int Mark()
        {
            return m_pch - m_startMatch;
        }
        void Restore(int mark)
        {
            m_pch = m_startMatch + mark;
        }
        void Matching(bool b)
        {
            m_matching = b;
            if (b)
                m_startMatch = m_pch;
        }
        public _Enumerator GetEnumerator()
        {
            return new _Enumerator(this);
        }
        public void Reset()
        {
            m_pch = 0;
            m_LineManager.backto(0);
        }
        public class _Enumerator
        {
            Lexer lxr;
            TOKEN t;
            public _Enumerator(Lexer x) { lxr = x; t = null; }
            public bool MoveNext()
            {
                t = lxr.Next();
                return t != null;
            }
            public TOKEN Current { get { return t; } }
            public void Reset() { lxr.Reset(); }
        }
    }

    public class CSToolsException : Exception
    {
        public int nExceptionNumber;
        public SourceLineInfo slInfo;
        public string sInput;
        public SYMBOL sym = null;
        public bool handled = false;
        public CSToolsException(int n, string s) : this(n, new SourceLineInfo(0), "", s) { }
        public CSToolsException(int n, Lexer yl, string s) : this(n, yl, yl.yytext, s) { }
        public CSToolsException(int n, Lexer yl, string yy, string s) : this(n, yl, yl.m_pch, yy, s) { }
        public CSToolsException(int n, TOKEN t, string s) : this(n, t.yylx, t.pos, t.yytext, s) { sym = t; }
        public CSToolsException(int n, SYMBOL t, string s) : this(n, t.yylx, t.pos, t.yyname, s) { sym = t; }
        public CSToolsException(int en, Lexer yl, int p, string y, string s) : this(en, yl.sourceLineInfo(p), y, s) { }
        public CSToolsException(int en, SourceLineInfo s, string y, string m) : base(s.ToString() + ": " + m)
        {
            nExceptionNumber = en;
            slInfo = s;
            sInput = y;
        }
        public virtual void Handle(ErrorHandler erh) // provides the default ErrorHandling implementation
        {
            if (erh.throwExceptions)
                throw this;
            if (handled)
                return;
            handled = true;
            erh.Report(this); // the parse table may allow recovery from this error
        }
    }

    public class CSToolsFatalException : CSToolsException
    {
        public CSToolsFatalException(int n, string s) : base(n, s) { }
        public CSToolsFatalException(int n, Lexer yl, string s) : base(n, yl, yl.yytext, s) { }
        public CSToolsFatalException(int n, Lexer yl, string yy, string s) : base(n, yl, yl.m_pch, yy, s) { }
        public CSToolsFatalException(int n, Lexer yl, int p, string y, string s) : base(n, yl, p, y, s) { }
        public CSToolsFatalException(int n, TOKEN t, string s) : base(n, t, s) { }
        public CSToolsFatalException(int en, SourceLineInfo s, string y, string m) : base(en, s, y, m) { }
        public override void Handle(ErrorHandler erh)
        {
            throw this; // we expect to bomb out to the environment with CLR traceback
        }
    }

    public class CSToolsStopException : CSToolsException
    {
        public CSToolsStopException(int n, string s) : base(n, s) { }
        public CSToolsStopException(int n, Lexer yl, string s) : base(n, yl, yl.yytext, s) { }
        public CSToolsStopException(int n, Lexer yl, string yy, string s) : base(n, yl, yl.m_pch, yy, s) { }
        public CSToolsStopException(int n, Lexer yl, int p, string y, string s) : base(n, yl, p, y, s) { }
        public CSToolsStopException(int n, TOKEN t, string s) : base(n, t, s) { }
        public CSToolsStopException(int n, SYMBOL t, string s) : base(n, t, s) { }
        public CSToolsStopException(int en, SourceLineInfo s, string y, string m) : base(en, s, y, m) { }
        public override void Handle(ErrorHandler erh)
        {
            // 4.5b
            if (erh.throwExceptions)
                throw this; // we expect Parser.Parse() to catch this but stop the parse
        }
    }

    public class ErrorHandler
    {
        public int counter = 0;
        public bool throwExceptions = false;
        public ErrorHandler() { }
        public ErrorHandler(bool ee) { throwExceptions = ee; }
        public virtual void Error(CSToolsException e)
        {
            counter++;
            e.Handle(this);
        }
        public virtual void Report(CSToolsException e)
        {
            //Console.WriteLine(e.Message); 
        }
    }
}