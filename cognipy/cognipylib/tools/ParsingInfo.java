package tools;

import java.util.*;
import java.io.*;

public class ParsingInfo
{
	public String m_name;
	public int m_yynum;
	public Hashtable m_parsetable = new Hashtable(); // state:int -> ParserEntry
	public ParsingInfo(String name, int num)
	{
		m_name = name;
		m_yynum = num;
	}
	private ParsingInfo()
	{
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ParsingInfo();
		}
		ParsingInfo p = (ParsingInfo)o;
		if (s.getEncode())
		{
			s.Serialise(p.m_name);
			s.Serialise(p.m_yynum);
			s.Serialise(p.m_parsetable);
			return null;
		}
		p.m_name = (String)s.Deserialise();
		p.m_yynum = (Integer)s.Deserialise();
		p.m_parsetable = (Hashtable)s.Deserialise();
		return p;
	}
}