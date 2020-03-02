package tools;

import java.util.*;

public class ReCategory extends Regex
{
	public ReCategory(TokensGen tks, String str)
	{
		m_str = str;
		m_test = tks.m_tokens.GetTest(str);
	}
	private String m_str;
	private ChTest m_test;
	@Override
	public boolean Match(char ch)
	{
		return m_test.invoke(ch);
	}
	@Override
	public void Print(TextWriter s)
	{
		s.WriteLine("{" + m_str + "}");
	}
	@Override
	public void Build(Nfa nfa)
	{
		nfa.AddArcEx(this, nfa.m_end);
	}
}