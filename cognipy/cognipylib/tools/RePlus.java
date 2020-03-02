package tools;

import java.util.*;

public class RePlus extends Regex
{
	public RePlus(Regex sub)
	{
		m_sub = sub;
	}
	@Override
	public void Print(TextWriter s)
	{
		m_sub.Print(s);
		s.Write("+");
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
		for (n = r;r > 0;n += r)
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
		super.Build(nfa);
		nfa.m_end.AddEps(nfa);
	}
}