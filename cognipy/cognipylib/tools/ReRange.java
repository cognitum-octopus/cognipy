package tools;

import java.util.*;

public class ReRange extends Regex
{
	public ReRange(TokensGen tks, String str)
	{
		StringBuilder ns = new StringBuilder();
		int n = str.length() - 1,v;
		int p;

		for (p = 1;p < n;p++) // fix \ escapes
		{
			if (str.charAt(p) == '\\')
			{
				if (p + 1 < n)
				{
					p++;
				}
				if (str.charAt(p) >= '0' && str.charAt(p) <= '7')
				{
					for (v = str.charAt(p++) - '0';p < n && str.charAt(p) >= '0' && str.charAt(p) <= '7';p++)
					{
						v = v * 8 + str.charAt(p) - '0';
					}
					ns.append((char)v);
				}
				else
				{
					switch (str.charAt(p))
					{
						case 'n' :
							ns.append('\n');
							break;
						case 't' :
							ns.append('\t');
							break;
						case 'r' :
							ns.append('\r');
							break;
						case 'v' :
							ns.append('\v');
							break;
						default:
							ns.append(str.charAt(p));
							break;
					}
				}
			}
			else
			{
				ns.append(str.charAt(p));
			}
		}
		n = ns.length();
		if (n > 0 && ns[0] == '^')
		{ // invert range
			m_invert = true;
			ns.deleteCharAt(0).append(new Character(0)).append((char)0xFFFF);
		}
		for (p = 0;p < n;p++)
		{
			if (p + 1 < n && ns[p + 1] == '-')
			{
				for (v = ns[p];v <= ns[p + 2];v++)
				{
					Set(tks, (char)v);
				}
				p += 2;
			}
			else
			{
				Set(tks, ns[p]);
			}
		}
	}
	public Hashtable m_map = new Hashtable(); // char->bool
	public boolean m_invert = false; // implement ^
	@Override
	public void Print(TextWriter s)
	{
		s.Write("[");
		if (m_invert)
		{
			s.Write("^");
		}
		for (char x : m_map.keySet())
		{
			s.Write(x);
		}
		s.Write("]");
	}
	private void Set(TokensGen tks, char ch)
	{
		m_map.put(ch, true);
		tks.m_tokens.UsingChar(ch);
	}
	@Override
	public boolean Match(char ch)
	{
		if (m_invert)
		{
			return !m_map.containsKey(ch);
		}
		return m_map.containsKey(ch);
	}
	@Override
	public int Match(String str, int pos, int max)
	{
		if (max < pos)
		{
			return -1;
		}
		return Match(str.charAt(pos))?1:-1;
	}
	@Override
	public void Build(Nfa nfa)
	{
		nfa.AddArcEx(this, nfa.m_end);
	}
}