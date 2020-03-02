package tools;

import java.util.*;

public class TokClassDef
{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public String m_refToken = "";
	public String m_initialisation = "";
	public String m_implement = "";
	public TokClassDef(GenBase gbs, String name, String bas)
	{
		if (gbs instanceof TokensGen)
		{
			TokensGen tks = (TokensGen) gbs;
			m_name = name;
			tks.m_tokens.tokens.put(name, this);
			m_refToken = bas;
		}
		m_yynum = ++gbs.LastSymbol;
	}
//#endif
	private TokClassDef()
	{
	}
	public String m_name = "";
	public int m_yynum = 0;
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new TokClassDef();
		}
		TokClassDef t = (TokClassDef)o;
		if (s.getEncode())
		{
			s.Serialise(t.m_name);
			s.Serialise(t.m_yynum);
			return null;
		}
		t.m_name = (String)s.Deserialise();
		t.m_yynum = (Integer)s.Deserialise();
		return t;
	}
}