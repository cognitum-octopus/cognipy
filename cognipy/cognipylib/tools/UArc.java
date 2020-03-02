package tools;

import java.util.*;

public class UArc extends Arc
{
	public UArc()
	{
	}
	public UArc(char ch, NfaNode next)
	{
		super(ch, next);
	}
	@Override
	public boolean Match(char ch)
	{
		return Character.toUpperCase(ch) == Character.toUpperCase(m_ch);
	}
	@Override
	public void Print(TextWriter s)
	{
		s.WriteLine(String.format("  U\'%1$s\' %2$s",m_ch,m_next.m_state));
	}
}