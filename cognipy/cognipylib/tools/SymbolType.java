package tools;

import java.util.*;
import java.io.*;

public class SymbolType
{
	private String m_name;
	private SymbolType m_next;
	public SymbolType(SymbolsGen yyp, String name)
	{
		this(yyp, name, false);
	}
	public SymbolType(SymbolsGen yyp, String name, boolean defined)
	{
		Lexer yyl = yyp.m_lexer;
		int p = name.indexOf("+");
		int num = 0;
		if (p > 0)
		{
			num = Integer.parseInt(name.substring(p + 1));
			if (num > yyp.LastSymbol)
			{
				yyp.LastSymbol = num;
			}
			name = name.substring(0,p);
		}
		yyl.yytext = name;
		CSymbol s = new CSymbol(yyp);
		if (num > 0)
		{
			s.m_yynum = num;
		}
		s = s.Resolve();
		if (defined)
		{
			s.m_defined = true;
		}
		m_name = name;
		m_next = yyp.stypes;
		yyp.stypes = this;
	}
	public final SymbolType _Find(String name)
	{
		if (name.equals(m_name))
		{
			return this;
		}
		if (m_next == null)
		{
			return null;
		}
		return m_next._Find(name);
	}
}