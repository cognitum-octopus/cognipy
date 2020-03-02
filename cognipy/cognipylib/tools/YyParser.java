package tools;

import java.util.*;
import java.io.*;

// Malcolm Crowe 1995, 2000
// a yacc-style implementation


public class YyParser
{
	public ErrorHandler erh = new ErrorHandler(true); // should get overwritten by Parser constructor
	public YyParser()
	{
	}
	// symbols
	public Hashtable symbols = new Hashtable(); // string -> CSymbol
	public Hashtable literals = new Hashtable(); // string -> Literal
												 // support for parsing
	public Hashtable symbolInfo = new Hashtable(); // yynum -> ParsingInfo
	public boolean m_concrete; // whether to build the concrete syntax tree
	public Hashtable m_states = new Hashtable(); // int->ParseState
	public CSymbol EOFSymbol;
	public CSymbol Special;
	public CSymbol m_startSymbol;
	public final String getStartSymbol()
	{
		return (m_startSymbol != null) ? m_startSymbol.getYytext() : "<null>";
	}
	public final void setStartSymbol(String value)
	{
		CSymbol s = (CSymbol)symbols.get(value);
		if (s == null)
		{
			erh.Error(new CSToolsException(25, "No such symbol <" + value + ">"));
		}
		m_startSymbol = s;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public final ParsingInfo GetSymbolInfo(String name, int num)
	{
		ParsingInfo pi = (ParsingInfo)symbolInfo.get(num);
		if (pi == null)
		{
			symbolInfo.put(num, pi = new ParsingInfo(name, num));
		}
		return pi;
	}
	public final void ClassInit(SymbolsGen yyp)
	{
		Special = new CSymbol(yyp);
		Special.setYytext("S'");
		EOFSymbol = (new EOF(yyp)).Resolve();
	}
	public final void Transitions(Builder b)
	{
		for (ParseState ps : m_states.values())
		{
			for (Transition t : ps.m_transitions.values())
			{
				b.invoke(t);
			}
		}
	}
	public final void PrintTransitions(Func f, String s)
	{
		for (ParseState ps : m_states.values())
		{
			for (Transition t : ps.m_transitions.values())
			{
				t.Print(f.invoke(t), s);
			}
		}
	}
//#endif
	public ParseState m_accept;
	// support for actions
	public Object tangible.Action0Param(Parser yyp, SYMBOL yysym, int yyact)
	{
		return null;
	} // will be generated for the generated parser
	public Hashtable types = new Hashtable(); // string->SCreator
											  // support for serialization
	public int[] arr; // defined in generated subclass

	public final void GetEOF(Lexer yyl)
	{
		EOFSymbol = (EOF)symbols.get("EOF");
		if (EOFSymbol == null)
		{
			EOFSymbol = new EOF(yyl);
		}
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public final void Emit(TextWriter m_outFile)
	{
		Serialiser b = new Serialiser(m_outFile);
		b.VersionCheck();
		System.out.println("Serialising the parser");
		b.Serialise(m_startSymbol);
		b.Serialise(m_accept);
		b.Serialise(m_states);
		b.Serialise(literals);
		b.Serialise(symbolInfo);
		b.Serialise(m_concrete);
		m_outFile.WriteLine("0};");
	}
//#endif
	private static class cachedParser
	{
		public CSymbol m_startSymbol;
		public ParseState m_accept;
		public Hashtable m_states;
		public Hashtable literals;
		public Hashtable symbolInfo;
		public boolean m_concrete;
		public CSymbol m_eof;
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [ThreadStatic] static Dictionary<Type, cachedParser> cachedPrs = null;
	private static HashMap<java.lang.Class, cachedParser> cachedPrs = null;

	public final void GetParser(Lexer m_lexer)
	{
		if (cachedPrs == null)
		{
			cachedPrs = new HashMap<java.lang.Class, cachedParser>();
		}
		if (!cachedPrs.containsKey(this.getClass()))
		{
			Serialiser b = new Serialiser(arr);
			b.VersionCheck();
			m_startSymbol = (CSymbol)b.Deserialise();
			m_startSymbol.kids = new ObjectList(); // 4.2a
			m_accept = (ParseState)b.Deserialise();
			m_states = (Hashtable)b.Deserialise();
			literals = (Hashtable)b.Deserialise();
			symbolInfo = (Hashtable)b.Deserialise();
			m_concrete = (Boolean)b.Deserialise();
			GetEOF(m_lexer);
			cachedParser tempVar = new cachedParser();
			tempVar.m_startSymbol = m_startSymbol;
			tempVar.m_accept = m_accept;
			tempVar.m_states = m_states;
			tempVar.literals = literals;
			tempVar.m_concrete = m_concrete;
			tempVar.symbolInfo = symbolInfo;
			tempVar.m_eof = EOFSymbol;
			cachedPrs.put(this.getClass(), tempVar);
		}
		else
		{
			java.lang.Class t = this.getClass();
			m_startSymbol = cachedPrs.get(t).m_startSymbol;
			m_startSymbol.kids = new ObjectList(); // 4.2a
			m_accept = cachedPrs.get(t).m_accept;
			m_states = cachedPrs.get(t).m_states;
			literals = cachedPrs.get(t).literals;
			symbolInfo = cachedPrs.get(t).symbolInfo;
			m_concrete = cachedPrs.get(t).m_concrete;
			EOFSymbol = cachedPrs.get(t).m_eof;
		}
	}
}