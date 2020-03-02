package tools;

import java.util.*;

public class ReOpt extends Regex
{
	public ReOpt(Regex sub)
	{
		m_sub = sub;
	}
	@Override
	public void Print(TextWriter s)
	{
		m_sub.Print(s);
		s.Write("?");
	}
	@Override
	public int Match(String str, int pos, int max)
	{
		int r;

		r = m_sub.Match(str, pos, max);
		if (r < 0)
		{
			r = 0;
		}
		return r;
	}
	@Override
	public void Build(Nfa nfa)
	{
		nfa.AddEps(nfa.m_end);
		super.Build(nfa);
	}
}