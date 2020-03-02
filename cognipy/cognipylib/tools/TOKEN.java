package tools;

import java.util.*;
import java.io.*;

//public class PartialSymbol : SYMBOL
//{
//    public object _;
//    public PartialSymbol(Parser yyp) : base(yyp) { }
//}

public class TOKEN extends SYMBOL
{
	public final String getYytext()
	{
		return m_str;
	}
	public final void setYytext(String value)
	{
		m_str = value;
	}
	private String m_str;
	public TOKEN(Parser yyp)
	{
		super(yyp);
	}
	public TOKEN(Lexer yyl)
	{
		super(yyl);
	if (yyl != null)
	{
		m_str = yyl.yytext;
	}
	}
	public TOKEN(Lexer yyl, String s)
	{
		super(yyl);
	m_str = s;
	}
	protected TOKEN()
	{
	}
	@Override
	public boolean IsTerminal()
	{
		return true;
	}
	private int num = 1;
	@Override
	public boolean Pass(YyParser syms, int snum, tangible.OutObject<ParserEntry> entry)
	{
		if (getYynum() == 1)
		{
			Literal lit = (Literal)syms.literals.get(getYytext());
			if (lit != null)
			{
				num = (int)lit.m_yynum;
			}
		}
		ParsingInfo pi = (ParsingInfo)syms.symbolInfo.get(getYynum());
		if (pi == null)
		{
			String s = String.format("Parser does not recognise literal %1$s", getYytext());
			syms.erh.Error(new CSToolsFatalException(9, yylx, getYyname(), s));
		}
		boolean r = pi.m_parsetable.containsKey(snum);
		entry.argValue = r ? ((ParserEntry)pi.m_parsetable.get(snum)) : null;
		return r;
	}
	@Override
	public String getYyname()
	{
		return "TOKEN";
	}
	@Override
	public int getYynum()
	{
		return num;
	}
	@Override
	public boolean Matches(String s)
	{
		return s.equals(m_str);
	}
	@Override
	public String toString()
	{
		return getYyname() + "<" + getYytext() + ">";
	}
	@Override
	public void Print()
	{
		System.out.println(ToString());
	}
}