package tools;

import java.util.*;
import java.io.*;

//#endif
public class ParserAction extends CSymbol
{
	public SYMBOL tangible.Action0Param(Parser yyp)
	{
		SYMBOL s = (SYMBOL)Sfactory.create(m_sym.getYytext(), yyp);
		if (s.getYyname().equals(m_sym.getYytext()))
		{ // provide for the default $$ = $1 action if possible
			SYMBOL t = yyp.StackAt(m_len - 1).m_value;
			s.m_dollar = (m_len == 0 || t == null) ? null : t.m_dollar;
		}
		return s;
	}
	@Override
	public void Print()
	{
		System.out.print(m_sym.getYytext());
	}
	public CSymbol m_sym;
	public int m_len;
	@Override
	public boolean IsAction()
	{
		return true;
	}
	public int ActNum()
	{
		return 0;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public ParserAction(SymbolsGen yyp)
	{
		super(yyp);
	}
//#endif
	protected ParserAction()
	{
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		ParserAction p = (ParserAction)o;
		if (s.getEncode())
		{
			CSymbol.Serialise(p, s);
			s.Serialise(p.m_sym);
			s.Serialise(p.m_len);
			return null;
		}
		CSymbol.Serialise(p, s);
		p.m_sym = (CSymbol)s.Deserialise();
		p.m_len = (Integer)s.Deserialise();
		return p;
	}
}