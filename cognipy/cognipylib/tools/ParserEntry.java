package tools;

import java.util.*;
import java.io.*;

public abstract class ParserEntry
{
	public ParserAction m_action;
	public int m_priority = 0;
	public ParserEntry()
	{
		m_action = null;
	}
	public ParserEntry(ParserAction action)
	{
		m_action = action;
	}
	public void Pass(tangible.RefObject<ParseStackEntry> top)
	{
	}
	public boolean IsReduce()
	{
		return false;
	}
	public String getStr()
	{
		return "";
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		ParserEntry p = (ParserEntry)o;
		if (s.getEncode())
		{
			s.Serialise(p.m_action);
			return null;
		}
		p.m_action = (ParserAction)s.Deserialise();
		return p;
	}
}