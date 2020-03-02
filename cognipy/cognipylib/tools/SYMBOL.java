package tools;

import java.util.*;
import java.io.*;

public class SYMBOL
{
	public Object m_dollar;
//C# TO JAVA CONVERTER TODO TASK: The following operator overload is not converted by C# to Java Converter:
	public static implicit operator int(SYMBOL s) // 4.0c
	{
		int rv = 0;
		Object d;
		while (((d = s.m_dollar) instanceof SYMBOL) && d != null)
		{
			s = (SYMBOL)d;
		}
		try
		{
			rv = (Integer)d;
		}
		catch (RuntimeException e)
		{
			System.out.println("attempt to convert from " + s.m_dollar.getClass());
			throw e;
		}
		return rv;
	}
	public int pos;
	public final int getLine()
	{
		return yylx.sourceLineInfo(pos).lineNumber;
	}
	public final int getPosition()
	{
		return yylx.sourceLineInfo(pos).rawCharPosition;
	}
	public final String getPos()
	{
		return yylx.Saypos(pos);
	}
	protected SYMBOL()
	{
	}
	public Lexer yylx;
	public final Object getYylval()
	{
		return m_dollar;
	}
	public final void setYylval(Object value)
	{
		m_dollar = value;
	}
	public SYMBOL(Lexer yyl)
	{
		yylx = yyl;
	}
	public int getYynum()
	{
		return 0;
	}
	public boolean IsTerminal()
	{
		return false;
	}
	public boolean IsAction()
	{
		return false;
	}
	public boolean IsCSymbol()
	{
		return false;
	}
	public Parser yyps = null;
	public final YyParser getYyact()
	{
		return (yyps != null) ? yyps.m_symbols : null;
	}
	public SYMBOL(Parser yyp)
	{
		yyps = yyp;
		yylx = yyp != null ? yyp.m_lexer : null;
	}
	public boolean Pass(YyParser syms, int snum, tangible.OutObject<ParserEntry> entry)
	{
		ParsingInfo pi = (ParsingInfo)syms.symbolInfo.get(getYynum());
		if (pi == null)
		{
			String s = String.format("No parsinginfo for symbol %1$s %2$s", getYyname(), getYynum());
			syms.erh.Error(new CSToolsFatalException(9, yylx, getYyname(), s));
		}
		boolean r = pi.m_parsetable.containsKey(snum);
		entry.argValue = r ? ((ParserEntry)pi.m_parsetable.get(snum)) : null;
		return r;
	}
	public String getYyname()
	{
		return "SYMBOL";
	}
	@Override
	public String toString()
	{
		return getYyname();
	}
	public boolean Matches(String s)
	{
		return false;
	}
	public void Print()
	{
		System.out.println(ToString());
	}
	// 4.2a Support for automatic display of concrete syntax tree
	public ObjectList kids = new ObjectList();
	private void ConcreteSyntaxTree(String n)
	{
		if (this instanceof error)
		{
			System.out.println(n + " " + ToString());
		}
		else
		{
			System.out.println(n + "-" + ToString());
		}
		int j = 0;
		for (SYMBOL s : kids)
		{
			s.ConcreteSyntaxTree(n + ((j++ == kids.getCount() - 1) ? "  " : " |"));
		}
	}
	public void ConcreteSyntaxTree()
	{
		ConcreteSyntaxTree("");
	}
}