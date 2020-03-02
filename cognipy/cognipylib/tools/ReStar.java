package tools;

import java.util.*;

public class ReStar extends Regex
{
	public ReStar(Regex sub)
	{
		m_sub = sub;
	}
	@Override
	public void Print(TextWriter s)
	{
		m_sub.Print(s);
		s.Write("*");
	}
	@Override
	public int Match(String str, int pos, int max)
	{
		int n,r;

		r = m_sub.Match(str, pos, max);
		if (r < 0)
		{
			return -1;
		}
		for (n = 0;r > 0;n += r)
		{
			r = m_sub.Match(str, pos + n, max);
			if (r < 0)
			{
				break;
			}
		}
		return n;
	}
	@Override
	public void Build(Nfa nfa)
	{
		Nfa sub = new Nfa(nfa.m_tks, m_sub);
		nfa.AddEps(sub);
		nfa.AddEps(nfa.m_end);
		sub.m_end.AddEps(nfa);
	}
}