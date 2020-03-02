package tools;

import java.util.*;
import java.io.*;

public class EOF extends CSymbol
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public EOF(SymbolsGen yyp)
	{
		super(yyp);
	setYytext("EOF");
	m_yynum = 2;
	m_symtype = SymType.eofsymbol;
	}
//#endif
	public EOF(Lexer yyl)
	{
		super(yyl);
		setYytext("EOF");
		pos = yyl.m_LineManager.end; // 4.5b
		m_symtype = SymType.eofsymbol;
	}
	private EOF()
	{
	}
	@Override
	public String getYyname()
	{
		return "EOF";
	}
	@Override
	public int getYynum()
	{
		return 2;
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new EOF();
		}
		return CSymbol.Serialise(o, s);
	}
}