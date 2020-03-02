package tools;

import java.util.*;

//#endif
public class ResWds
{
	public boolean m_upper = false;
	public Hashtable m_wds = new Hashtable(); // string->string (token class name)
	public ResWds()
	{
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public static ResWds New(TokensGen tks, String str)
	{
		ResWds r = new ResWds();
		str = str.trim();
		if (str.charAt(0) == 'U')
		{
			r.m_upper = true;
			str = str.substring(1).trim();
		}
		if (str.charAt(0) != '{' || str.charAt(str.length() - 1) != '}')
		{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
			goto bad;
		}
		str = str.substring(1, 1 + str.length() - 2).trim();
		String[] wds = str.split("[,]", -1);
		for (int j = 0;j < wds.length;j++)
		{
			String w = wds[j].trim();
			String a = w;
			int i = w.indexOf(' ');
			if (i > 0)
			{
				a = w.substring(i).trim();
				w = w.substring(0,i);
			}
			r.m_wds.put(w, a);
			if (tks.m_tokens.tokens.get(a) == null)
			{
				TokClassDef t = new TokClassDef(tks, a, "TOKEN");
				tks.m_outFile.WriteLine("//%{0}+{1}",a,t.m_yynum);
				tks.m_outFile.Write("public class {0} : TOKEN",a);
				tks.m_outFile.WriteLine("{ public override string yyname { get { return \"" + a + "\";}}");
				tks.m_outFile.WriteLine("public override int yynum { get { return " + t.m_yynum + "; }}");
				tks.m_outFile.WriteLine(" public " + a + "(Lexer yyl):base(yyl) {}}");
			}
		}
		return r;
		bad:
			tks.m_tokens.erh.Error(new CSToolsException(47, "bad ResWds element"));
		return null;
	}
//#endif
	public final void Check(Lexer yyl, tangible.RefObject<TOKEN> tok)
	{
		String str = tok.argValue.getYytext();
		if (m_upper)
		{
			str = str.toUpperCase();
		}
		Object o = m_wds.get(str);
		if (o == null)
		{
			return;
		}
		tok.argValue = (TOKEN)Tfactory.create((String)o, yyl);
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ResWds();
		}
		ResWds r = (ResWds)o;
		if (s.getEncode())
		{
			s.Serialise(r.m_upper);
			s.Serialise(r.m_wds);
			return null;
		}
		r.m_upper = (Boolean)s.Deserialise();
		r.m_wds = (Hashtable)s.Deserialise();
		return r;
	}
}