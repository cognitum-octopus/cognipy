package tools;

import java.util.*;

/* The .NET Framework has its own Regex class which is an NFA recogniser
We don't want to use this for lexing because 
	it would be too slow (DFA is always faster)
	programming in actions looks difficult
	we want to explain the NFA->DFA algorithm to students
So in this project we are not using the Framework's Regex class but the one defined in regex.cs
*/

public class Arc
{
	public char m_ch;
	public NfaNode m_next;
	public Arc()
	{
	}
	public Arc(char ch, NfaNode next)
	{
		m_ch = ch;
		m_next = next;
	}
	public boolean Match(char ch)
	{
		return ch == m_ch;
	}
	public void Print(TextWriter s)
	{
		s.WriteLine(String.format("  %1$s %2$s",m_ch,m_next.m_state));
	}
}