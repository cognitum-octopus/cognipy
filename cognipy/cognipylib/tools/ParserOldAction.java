package tools;

import java.util.*;
import java.io.*;

public class ParserOldAction extends ParserAction
{
	public int m_action;
	@Override
	public SYMBOL tangible.Action0Param(Parser yyp)
	{
		SYMBOL s = super.Action(yyp);
		Object ob = yyp.m_symbols.Action(yyp, s, m_action);
		if (ob != null)
		{
			s.m_dollar = ob;
		}
		return s;
	}
	@Override
	public int ActNum()
	{
		return m_action;
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public ParserOldAction(SymbolsGen yyp)
	{
		super(yyp);
		m_action = yyp.action_num++;
		yyp.actions.Add(this);
		m_sym = null;
		m_symtype = CSymbol.SymType.oldaction;
		yyp.OldAction(this);
	}
//#endif
	private ParserOldAction()
	{
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ParserOldAction();
		}
		ParserOldAction p = (ParserOldAction)o;
		if (s.getEncode())
		{
			ParserAction.Serialise(p, s);
			s.Serialise(p.m_action);
			return null;
		}
		ParserAction.Serialise(p, s);
		p.m_action = (Integer)s.Deserialise();
		return p;
	}
}