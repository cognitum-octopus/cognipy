package tools;

import java.util.*;

public class ArcEx extends Arc
{
	public Regex m_ref;
	public ArcEx(Regex re, NfaNode next)
	{
		m_ref = re;
		m_next = next;
	}
	@Override
	public boolean Match(char ch)
	{
		return m_ref.Match(ch);
	}
	@Override
	public void Print(TextWriter s)
	{
		s.Write("  ");
		m_ref.Print(s);
		s.WriteLine(m_next.m_state);
	}
}