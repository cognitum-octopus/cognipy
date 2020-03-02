package tools;

import java.util.*;
import java.io.*;

// [Serializable] 
public class Literal extends CSymbol // used for %TOKEN in LexerGenerator script and quoted strings
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public Literal(SymbolsGen yyp)
	{
		super(yyp);
	m_symtype = SymType.terminal;
	}
	@Override
	public CSymbol Resolve()
	{ // to the first occurrence
		int n = getYytext().length();
		String ns = "";
		for (int p = 1;p + 1 < n;p++) // fix \ escapes
		{
			if (getYytext().charAt(p) == '\\')
			{
				if (p + 1 < n)
				{
					p++;
				}
				if (getYytext().charAt(p) >= '0' && getYytext().charAt(p) <= '7')
				{
					int v;
					for (v = getYytext().charAt(p++) - '0';p < n && getYytext().charAt(p) >= '0' && getYytext().charAt(p) <= '7';p++)
					{
						v = v * 8 + getYytext().charAt(p) - '0';
					}
					ns += (char)v;
				}
				else
				{
					switch (getYytext().charAt(p))
					{
						case 'n' :
							ns += '\n';
							break;
						case 't' :
							ns += '\t';
							break;
						case 'r' :
							ns += '\r';
							break;
						default:
							ns += getYytext().charAt(p);
							break;
					}
				}
			}
			else
			{
				ns += getYytext().charAt(p);
			}
		}
		setYytext(ns);
		CSymbol ob = (CSymbol)m_parser.m_symbols.literals.get(getYytext());
		if (ob != null)
		{
			return ob;
		}
		m_yynum = ++m_parser.LastSymbol;
		m_parser.m_symbols.literals.put(getYytext(), this);
		m_parser.m_symbols.symbolInfo.put(m_yynum, new ParsingInfo(getYytext(), m_yynum));
		return this;
	}
	public final boolean CouldStart(CSymbol nonterm)
	{
		return false;
	}
	@Override
	public String TypeStr()
	{
		return "TOKEN";
	}
//#endif
	private Literal()
	{
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new Literal();
		}
		return CSymbol.Serialise(o, s);
	}
}