package tools;

import java.util.*;

public class ReAlt extends Regex
{
	public ReAlt(TokensGen tks, Regex sub, int p, String str)
	{
		m_sub = sub;
		m_alt = new Regex(tks, p, str);
	}
	public Regex m_alt;
	@Override
	public void Print(TextWriter s)
	{
		s.Write("(");
		if (m_sub != null)
		{
			m_sub.Print(s);
		}
		s.Write("|");
		if (m_alt != null)
		{
			m_alt.Print(s);
		}
		s.Write(")");
	}
	@Override
	public int Match(String str, int pos, int max)
	{
		int a = -1, b = -1;
		if (m_sub != null)
		{
			a = m_sub.Match(str, pos, max);
		}
		if (m_alt != null)
		{
			b = m_sub.Match(str, pos, max);
		}
		return (a > b)?a:b;
	}
	@Override
	public void Build(Nfa nfa)
	{
		if (m_alt != null)
		{
			Nfa alt = new Nfa(nfa.m_tks, m_alt);
			nfa.AddEps(alt);
			alt.m_end.AddEps(nfa.m_end);
		}
		super.Build(nfa);
	}
}