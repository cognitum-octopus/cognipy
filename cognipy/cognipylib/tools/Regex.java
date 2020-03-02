package tools;

import java.util.*;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
public class Regex
{
	/* 
		Construct a Regex from a given string

	1.  First examine the given string. 
		If it is empty, there is nothing to do, so return (having cleared m_sub as a precaution).
	2.  Look to see if the string begins with a bracket ( . If so, find the matching ) . 
		This is not as simple as it might be because )s inside quotes or [] or escaped will not count.
		Recursively call the constructor for the regular expression between the () s. 
		Mark everything up to the ) as used, and go to step 9.
	3.  Look to see if the string begins with a bracket [ . If so, find the matching ] , watching for escapes.
		Construct a ReRange for everything between the []s. 
		Mark everything up to the ] as used, and go to step 9.
	4.  Look to see if the string begins with a ' or " . If so, build the contents interpreting 
		escaped special characters correctly, until the matching quote is reached. 
		Construct a ReStr for the contents, mark everything up to the final quote as used, and go to step 9.
	4a.  Look to see if the string begins with a U' or U" . If so, build the contents interpreting 
		escaped special characters correctly, until the matching quote is reached. 
		Construct a ReUStr for the contents, mark everything up to the final quote as used, and go to step 9.
	5.  Look to see if the string begins with a \ . 
		If so, build a ReStr for the next character (special action for ntr), 
		mark it as used, and go to step 9.
	6.  Look to see if the string begins with a { . 
		If so, find the matching }, lookup the symbolic name in the definitions table, 
		recursively call this constructor on the contents, 
		mark everything up to the } as used, and go to step 9.
	7.  Look to see if the string begins with a dot. 
		If so, construct a ReRange("^\n"), mark the . as used, and go to step 9.
	8.  At this point we conclude that there is a simple character at the start of the regular expression. 
		Construct a ReStr for it, mark it as used, and go to step 9.
	9.  If the string is exhausted, return. 
		We have a simple Regex whose m_sub contains what we can constructed.
	10.  If the next character is a ? , *, or +, construct a ReOpt, ReStart, or RePlus respectively 
		out of m_sub, and make m_sub point to this new class instead. Mark the character as used.
	11.  If the string is exhausted, return.
	12.  If the next character is a | , build a ReAlt using the m_sub we have and the rest of the string.
	13.  Otherwise build a ReCat using the m_sub we have and the rest of the string.
	*/
	public Regex(TokensGen tks, int p, String str)
	{
		int n = str.length();
		int nlp = 0;
		int lbrack = 0;
		int quote = 0;
		int j;
		char ch;

		//1.  First examine the given string. 
		//	If it is empty, there is nothing to do, so return (having cleared m_sub as a precaution).
		m_sub = null;
		if (n == 0)
		{
			return;
		}
			//2.  Look to see if the string begins with a bracket ( . If so, find the matching ) . 
			//	This is not as simple as it might be because )s inside quotes or [] or escaped will not count.
			// 	Recursively call the constructor for the regular expression between the () s. 
			// 	Mark everything up to the ) as used, and go to step 9.
		else if (str.charAt(0) == '(')
		{ // identify a bracketed expression
			for (j = 1; j < n; j++)
			{
				if (str.charAt(j) == '\\')
				{
					j++;
				}
				else if (str.charAt(j) == ']' && lbrack > 0)
				{
					lbrack = 0;
				}
				else if (lbrack > 0)
				{
					continue;
				}
				else if (str.charAt(j) == '"' || str.charAt(j) == '\'')
				{
					if (quote == str.charAt(j))
					{
						quote = 0;
					}
					else if (quote == 0)
					{
						quote = str.charAt(j);
					}
				}
				else if (quote > 0)
				{
					continue;
				}
				else if (str.charAt(j) == '[')
				{
					lbrack++;
				}
				else if (str.charAt(j) == '(')
				{
					nlp++;
				}
				else if (str.charAt(j) == ')' && nlp-- == 0)
				{
					break;
				}
			}
			if (j == n)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
				goto bad;
			}
			m_sub = new Regex(tks, p + 1, str.substring(1, j));
			j++;
			//3.  Look to see if the string begins with a bracket [ . If so, find the matching ] , watching for escapes.
			//	Construct a ReRange for everything between the []s. 
			//	Mark everything up to the ] as used, and go to step 9.
		}
		else if (str.charAt(0) == '[')
		{ // range of characters
			for (j = 1;j < n && str.charAt(j) != ']';j++)
			{
				if (str.charAt(j) == '\\')
				{
					j++;
				}
			}
			if (j == n)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
				goto bad;
			}
			m_sub = new ReRange(tks, str.substring(0,j + 1));
			j++;
		}
		//4.  Look to see if the string begins with a ' or " . If so, build the contents interpreting 
		//	escaped special characters correctly, until the matching quote is reached. 
		//	Construct a CReStr for the contents, mark everything up to the final quote as used, and go to step 9.
		else if (str.charAt(0) == '\'' || str.charAt(0) == '"')
		{ // quoted string needs special treatment
			StringBuilder qs = new StringBuilder();
			for (j = 1;j < n && str.charAt(j) != str.charAt(0);j++)
			{
				if (str.charAt(j) == '\\')
				{
					switch (str.charAt(++j))
					{
						case 'n':
							qs.append('\n');
							break;
						case 'r':
							qs.append('\r');
							break;
						case 't':
							qs.append('\t');
							break;
						case 'v':
							qs.append('\v');
							break;
						case '\\':
							qs.append('\\');
							break;
						case '\'':
							qs.append('\'');
							break;
						case '0':
							qs.append((char) 0);
							break; // 4.7f
						case '"':
							qs.append('"');
							break;
						case '\n':
							break;
						default:
							qs.append(str.charAt(j));
							break;
					}
				}
				else
				{
					qs.append(str.charAt(j));
				}
			}
			if (j == n)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
				goto bad;
			}
			j++;
			m_sub = new ReStr(tks, qs.toString());
		}
			//4a.  Look to see if the string begins with a U' or U" . If so, build the contents interpreting 
			//	escaped special characters correctly, until the matching quote is reached. 
			//	Construct a ReUStr for the contents, mark everything up to the final quote as used, and go to step 9.
		else if (str.startsWith("U\"") || str.startsWith("U'"))
		{ // quoted string needs special treatment
			StringBuilder qs = new StringBuilder();
			for (j = 2;j < n && str.charAt(j) != str.charAt(1);j++)
			{
				if (str.charAt(j) == '\\')
				{
					switch (str.charAt(++j))
					{
						case 'n':
							qs.append('\n');
							break;
						case 'r':
							qs.append('\r');
							break;
						case 't':
							qs.append('\t');
							break;
						case 'v':
							qs.append('\v');
							break;
						case '\\':
							qs.append('\\');
							break;
						case '\'':
							qs.append('\'');
							break;
						case '"':
							qs.append('"');
							break;
						case '\n':
							break;
						default:
							qs.append(str.charAt(j));
							break;
					}
				}
				else
				{
					qs.append(str.charAt(j));
				}
			}
			if (j == n)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
				goto bad;
			}
			j++;
			m_sub = new ReUStr(tks, qs.toString());
		}
			//5.  Look to see if the string begins with a \ . 
		//	If so, build a ReStr for the next character (special action for ntr),
		//	mark it as used, and go to step 9.
		else if (str.charAt(0) == '\\')
		{
			switch (ch = str.charAt(1))
			{
				case 'n':
					ch = '\n';
					break;
				case 't':
					ch = '\t';
					break;
				case 'r':
					ch = '\r';
					break;
				case 'v':
					ch = '\v';
					break;
			}
			m_sub = new ReStr(tks, ch);
			j = 2;
			//6.  Look to see if the string begins with a { . 
			//	If so, find the matching }, lookup the symbolic name in the definitions table, 
			//	recursively call this constructor on the contents, 
			//	mark everything up to the } as used, and go to step 9.
		}
		else if (str.charAt(0) == '{')
		{
			for (j = 1;j < n && str.charAt(j) != '}';j++)
			{
				;
			}
			if (j == n)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
				goto bad;
			}
			String ds = str.substring(1, j);
			String s = (String)tks.defines.get(ds);
			if (s == null)
			{
				m_sub = new ReCategory(tks, ds);
			}
			else
			{
				m_sub = new Regex(tks, p + 1, s);
			}
			j++;
		}
		else
		{ // simple character at start of regular expression
			//7.  Look to see if the string begins with a dot. 
			//	If so, construct a CReDot, mark the . as used, and go to step 9.
			if (str.charAt(0) == '.')
			{
				m_sub = new ReRange(tks, "[^\n]");
			}
				//8.  At this point we conclude that there is a simple character at the start of the regular expression. 
				//	Construct a ReStr for it, mark it as used, and go to step 9.
			else
			{
				m_sub = new ReStr(tks, str.charAt(0));
			}
			j = 1;
		}
		//9.  If the string is exhausted, return. 
		//	We have a simple Regex whose m_sub contains what we can constructed.
		if (j >= n)
		{
			return;
		}
		//10.  If the next character is a ? , *, or +, construct a CReOpt, CReStart, or CRePlus respectively 
		//	out of m_sub, and make m_sub point to this new class instead. Mark the character as used.
		if (str.charAt(j) == '?')
		{
			m_sub = new ReOpt(m_sub);
			j++;
		}
		else if (str.charAt(j) == '*')
		{
			m_sub = new ReStar(m_sub);
			j++;
		}
		else if (str.charAt(j) == '+')
		{
			m_sub = new RePlus(m_sub);
			j++;
		}
		// 11.  If the string is exhausted, return.
		if (j >= n)
		{
			return;
		}
		// 12.  If the next character is a | , build a ReAlt using the m_sub we have and the rest of the string.
		if (str.charAt(j) == '|')
		{
			m_sub = new ReAlt(tks, m_sub, p + j + 1, str.substring(j + 1, j + 1 + n - j - 1));
		}
			// 13.  Otherwise build a ReCat using the m_sub we have and the rest of the string.
		else if (j < n)
		{
			m_sub = new ReCat(tks, m_sub, p + j, str.substring(j, n));
		}
		return;
		bad:
			tks.erh.Error(new CSToolsFatalException(1, tks.sourceLineInfo(p), str, "ill-formed regular expression " + str));
	}
	protected Regex()
	{
	} // private
	public Regex m_sub;
	public void Print(TextWriter s)
	{
		if (m_sub != null)
		{
			m_sub.Print(s);
		}
	}
	// Match(ch) is used only in arc handling for ReRange
	public boolean Match(char ch)
	{
		return false;
	}
	// These two Match methods are only required if you want to use
	// the Regex direcly for lexing. This is a very strange thing to do: 
	// it is non-deterministic and rather slow.
	public final int Match(String str)
	{
		return Match(str, 0, str.length());
	}
	public int Match(String str, int pos, int max)
	{
		if (max < 0)
		{
			return -1;
		}
		if (m_sub != null)
		{
			return m_sub.Match(str, pos, max);
		}
		return 0;
	}
	public void Build(Nfa nfa)
	{
		if (m_sub != null)
		{
			Nfa sub = new Nfa(nfa.m_tks, m_sub);
			nfa.AddEps(sub);
			sub.m_end.AddEps(nfa.m_end);
		}
		else
		{
			nfa.AddEps(nfa.m_end);
		}
	}
}