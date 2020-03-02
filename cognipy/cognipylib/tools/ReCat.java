package tools;

import java.util.*;

public class ReCat extends Regex
{
	public ReCat(TokensGen tks, Regex sub, int p, String str)
	{
		m_sub = sub;
		m_next = new Regex(tks, p, str);
	}
	private Regex m_next;
	@Override
	public void Print(TextWriter s)
	{
		s.Write("(");
		if (m_sub != null)
		{
			m_sub.Print(s);
		}
		s.Write(")(");
		if (m_next != null)
		{
			m_next.Print(s);
		}
		s.Write(")");
	}
	@Override
	public int Match(String str, int pos, int max)
	{
		int first, a, b, r = -1;

		if (m_next == null)
		{
			return super.Match(str, pos, max);
		}
		if (m_sub == null)
		{
			return m_next.Match(str, pos, max);
		}
		for (first = max;first >= 0;first = a - 1)
		{
			a = m_sub.Match(str, pos, first);
			if (a < 0)
			{
				break;
			}
			b = m_next.Match(str, pos + a, max);
			if (b < 0)
			{
				continue;
			}
			if (a + b > r)
			{
				r = a + b;
			}
		}
		return r;
	}
	@Override
	public void Build(Nfa nfa)
	{
		if (m_next != null)
		{
			if (m_sub != null)
			{
				Nfa first = new Nfa(nfa.m_tks, m_sub);
				Nfa second = new Nfa(nfa.m_tks, m_next);
				nfa.AddEps(first);
				first.m_end.AddEps(second);
				second.m_end.AddEps(nfa.m_end);
			}
			else
			{
				m_next.Build(nfa);
			}
		}
		else
		{
			super.Build(nfa);
		}
	}
}