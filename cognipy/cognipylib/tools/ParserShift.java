package tools;

import java.util.*;
import java.io.*;

public class ParserShift extends ParserEntry
{
	public ParseState m_next;
	public ParserShift()
	{
	}
	public ParserShift(ParserAction action, ParseState next)
	{
		super(action);
	m_next = next;
	}
	@Override
	public void Pass(tangible.RefObject<ParseStackEntry> top)
	{
		Parser yyp = top.argValue.yyps;
		if (m_action == null)
		{
			yyp.Push(top.argValue);
			top.argValue = new ParseStackEntry(yyp, m_next.m_state, yyp.NextSym());
		}
		else
		{
			yyp.Push(new ParseStackEntry(yyp, top.argValue.m_state, m_action.Action(yyp)));
			top.argValue.m_state = m_next.m_state;
		}
	}
	@Override
	public String getStr()
	{
		if (m_next == null)
		{
			return "?? null shift";
		}
		return String.format("shift %1$s", m_next.m_state);
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ParserShift();
		}
		ParserShift p = (ParserShift)o;
		if (s.getEncode())
		{
			ParserEntry.Serialise(p, s);
			s.Serialise(p.m_next);
			return null;
		}
		ParserEntry.Serialise(p, s);
		p.m_next = (ParseState)s.Deserialise();
		return p;
	}
}