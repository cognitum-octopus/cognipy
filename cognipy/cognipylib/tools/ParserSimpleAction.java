package tools;

import java.util.*;
import java.io.*;

public class ParserSimpleAction extends ParserAction
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	@Override
	public String TypeStr()
	{
		return m_sym.getYytext();
	}
	@Override
	public void Print()
	{
		System.out.printf(" %%%1$s", m_sym.getYytext());
	}
	public ParserSimpleAction(SymbolsGen yyp)
	{
		super(yyp);
		yyp.actions.Add(this);
		m_symtype = CSymbol.SymType.simpleaction;
		yyp.SimpleAction(this);
	}
//#endif
	private ParserSimpleAction()
	{
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ParserSimpleAction();
		}
		if (s.getEncode())
		{
			ParserAction.Serialise(o, s);
			return null;
		}
		return ParserAction.Serialise(o, s);
	}
}