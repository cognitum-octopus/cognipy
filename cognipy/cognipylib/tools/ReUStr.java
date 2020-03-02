package tools;

import java.util.*;

public class ReUStr extends ReStr
{
	public ReUStr(TokensGen tks, String str)
	{
		m_str = str;
		for (int i = 0;i < str.length();i++)
		{
			tks.m_tokens.UsingChar(Character.toLowerCase(str.charAt(i)));
			tks.m_tokens.UsingChar(Character.toUpperCase(str.charAt(i)));
		}
	}
	public ReUStr(TokensGen tks, char ch)
	{
		m_str = tangible.StringHelper.repeatChar(ch, 1);
		tks.m_tokens.UsingChar(Character.toLowerCase(ch));
		tks.m_tokens.UsingChar(Character.toUpperCase(ch));
	}
	@Override
	public void Print(TextWriter s)
	{
		s.Write(String.format("(U\"%1$s\")",m_str));
	}
	@Override
	public int Match(String str, int pos, int max)
	{
		int j,n = m_str.length();

		if (n > max)
		{
			return -1;
		}
		if (n > max - pos)
		{
			return -1;
		}
		for (j = 0;j < n;j++)
		{
			if (Character.toUpperCase(str.charAt(j)) != Character.toUpperCase(m_str.charAt(j)))
			{
				return -1;
			}
		}
		return n;
	}
	@Override
	public void Build(Nfa nfa)
	{
		int j,n = m_str.length();
		NfaNode p, pp = nfa;

		for (j = 0;j < n;pp = p,j++)
		{
			p = new NfaNode(nfa.m_tks);
			pp.AddUArc(m_str.charAt(j), p);
		}
		pp.AddEps(nfa.m_end);
	}
}