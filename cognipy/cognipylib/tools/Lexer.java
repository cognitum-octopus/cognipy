package tools;

import java.util.*;
import java.io.*;

public class Lexer
{
	public boolean m_debug = false;
	// source line control
	public String m_buf;
	public LineManager m_LineManager = new LineManager(); // 4.5b see EOF
	public final SourceLineInfo sourceLineInfo(int pos)
	{
		return new SourceLineInfo(this, pos); // 4.5c
	}
	public final String sourceLine(SourceLineInfo s)
	{
		// This is the sourceLine after removal of comments
		// The position in this line is s.charPosition
		// If you want the comments as well, then you should re-read the source file
		// and the position in the line is s.rawCharPosition
		return m_buf.substring(s.startOfLine, s.endOfLine);
	}
	public final String Saypos(int pos)
	{
		return sourceLineInfo(pos).toString();
	}

	// the heart of the lexer is the DFA
	public final Dfa getMStart()
	{
		return (Dfa)m_tokens.starts.get(m_state);
	}
	public String m_state = "YYINITIAL"; // exposed for debugging (by request)

	public Lexer(YyLexer tks)
	{
		m_state = "YYINITIAL";
		setTokens(tks);
	}

	private YyLexer m_tokens;
	public final YyLexer getTokens()
	{
		return m_tokens;
	} // 4.2d
	public final void setTokens(YyLexer value)
	{
		m_tokens = value;
		m_tokens.GetDfa();
	}
	public String yytext; // for collection when a TOKEN is created
	public int m_pch = 0;
	public final int getYypos()
	{
		return m_pch;
	}

	public final void yy_begin(String newstate)
	{
		m_state = newstate;
	}
	private boolean m_matching;
	private int m_startMatch;
	// match a Dfa against lexer's input

	private boolean Match(tangible.RefObject<TOKEN> tok, Dfa dfa)
	{
		return Match(tok, dfa, 0);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: bool Match(ref TOKEN tok, Dfa dfa, int depth = 0)
	private boolean Match(tangible.RefObject<TOKEN> tok, Dfa dfa, int depth)
	{
		char ch = PeekChar();
		int op = m_pch, mark = 0;
		Dfa next;

		if (m_debug)
		{
			System.out.printf("state %1$s with ", dfa.m_state);
			if (Character.isLetterOrDigit(ch) || Character.IsPunctuation(ch))
			{
				System.out.println(ch);
			}
			else
			{
				System.out.println("#" + (int)ch);
			}
		}
		if (dfa.m_actions != null)
		{
			mark = Mark();
		}
		if ((next = ((Dfa)dfa.m_map.get(m_tokens.Filter(ch)))) == null)
		{
			if (m_debug)
			{
				System.out.printf("%1$s no arc", dfa.m_state);
			}
			if (dfa.m_actions != null)
			{
				if (m_debug)
				{
					System.out.println(" terminal");
				}
				return TryActions(dfa, tok); // fails on REJECT
			}
			if (m_debug)
			{
				System.out.println(" fails");
			}
			return false;
		}
		Advance();
		if (depth > 16)
		{
		}
		if (!Match(tok, next, depth + 1))
		{ // rest of string fails
			if (m_debug)
			{
				System.out.printf("back to %1$s with %2$s" + "\r\n", dfa.m_state, ch);
			}
			if (dfa.m_actions != null)
			{ // this is still okay at a terminal
				if (m_debug)
				{
					System.out.printf("%1$s succeeds" + "\r\n", dfa.m_state);
				}
				Restore(mark);
				return TryActions(dfa, tok);
			}
			if (m_debug)
			{
				System.out.printf("%1$s fails" + "\r\n", dfa.m_state);
			}
			return false;
		}
		if (dfa.m_reswds >= 0)
		{
			((ResWds)m_tokens.reswds.get(dfa.m_reswds)).Check(this, tok);
		}
		if (m_debug)
		{
			System.out.printf("%1$s matched ", dfa.m_state);
			if (m_pch <= m_buf.length())
			{
				System.out.println(m_buf.substring(op, m_pch));
			}
			else
			{
				System.out.println(m_buf.substring(op));
			}
		}
		return true;
	}

	// start lexing
	public final void Start(InputStreamReader inFile)
	{
		m_state = "YYINITIAL"; // 4.3e
		m_LineManager.lines = 1;
		m_LineManager.list = null;
//C# TO JAVA CONVERTER WARNING: The java.io.InputStreamReader constructor does not accept all the arguments passed to the System.IO.StreamReader constructor:
//ORIGINAL LINE: inFile = new StreamReader(inFile.BaseStream, m_tokens.m_encoding);
		inFile = new InputStreamReader(inFile.BaseStream);
		m_buf = inFile.ReadToEnd();
		if (m_tokens.toupper)
		{
			m_buf = m_buf.toUpperCase();
		}
		for (m_pch = 0; m_pch < m_buf.length(); m_pch++)
		{
			if (m_buf.charAt(m_pch) == '\n')
			{
				m_LineManager.newline(m_pch);
			}
		}
		m_pch = 0;
	}
	public final void Start(CsReader inFile)
	{
		m_state = "YYINITIAL"; // 4.3e
		inFile = new CsReader(inFile, m_tokens.m_encoding);
		m_LineManager = inFile.lm;
		if (!inFile.Eof())
		{
			for (m_buf = inFile.ReadLine(); !inFile.Eof(); m_buf += inFile.ReadLine())
			{
				m_buf += "\n";
			}
		}
		if (m_tokens.toupper)
		{
			m_buf = m_buf.toUpperCase();
		}
		m_pch = 0;
	}
	public final void Start(String buf)
	{
		m_state = "YYINITIAL"; // 4.3e
		m_LineManager.lines = 1;
		m_LineManager.list = null;
		m_buf = buf + "\n";
		for (m_pch = 0; m_pch < m_buf.length(); m_pch++)
		{
			if (m_buf.charAt(m_pch) == '\n')
			{
				m_LineManager.newline(m_pch);
			}
		}
		if (m_tokens.toupper)
		{
			m_buf = m_buf.toUpperCase();
		}
		m_pch = 0;
	}
	public final TOKEN Next()
	{
		TOKEN rv = null;
		while (PeekChar() != 0)
		{
			Matching(true);
			tangible.RefObject<tools.TOKEN> tempRef_rv = new tangible.RefObject<tools.TOKEN>(rv);
			if (!Match(tempRef_rv, (Dfa)m_tokens.starts.get(m_state)))
			{
			rv = tempRef_rv.argValue;
				if (getYypos() == 0)
				{
					System.out.print("Check text encoding.. ");
				}
				int c = PeekChar();
				m_tokens.erh.Error(new CSToolsStopException(2, this, "illegal character <" + (char)c + "> " + c));
				return null;
			}
		else
		{
			rv = tempRef_rv.argValue;
		}
			Matching(false);
			if (rv != null)
			{ // or special value for empty action?
				rv.pos = m_pch - yytext.length();
				return rv;
			}
		}
		return null;
	}
	private boolean TryActions(Dfa dfa, tangible.RefObject<TOKEN> tok)
	{
		int len = m_pch - m_startMatch;
		if (len == 0)
		{
			return false;
		}
		if (m_startMatch + len <= m_buf.length())
		{
			yytext = m_buf.substring(m_startMatch, m_startMatch + len);
		}
		else // can happen with {EOF} rules
		{
			yytext = m_buf.substring(m_startMatch);
		}
		// actions is a list of old-style actions for this DFA in order of priority
		// there is a list because of the chance that any of them may REJECT
		Dfa.Action a = dfa.m_actions;
		boolean reject = true;
		while (reject && a != null)
		{
			int action = a.a_act;
			reject = false;
			a = a.a_next;
			if (a == null && !dfa.m_tokClass.equals(""))
			{ // last one might not be an old-style action
				if (m_debug)
				{
					System.out.println("creating a " + dfa.m_tokClass);
				}
				tok.argValue = (TOKEN)Tfactory.create(dfa.m_tokClass, this);
			}
			else
			{
				tangible.RefObject<String> tempRef_yytext = new tangible.RefObject<String>(yytext);
				tangible.RefObject<Boolean> tempRef_reject = new tangible.RefObject<Boolean>(reject);
				tok.argValue = m_tokens.OldAction(this, tempRef_yytext, action, tempRef_reject);
			reject = tempRef_reject.argValue;
			yytext = tempRef_yytext.argValue;
				if (m_debug && !reject)
				{
					System.out.println("Old action " + action);
				}
			}
		}
		return !reject;
	}
	public final char PeekChar()
	{
		if (m_pch < m_buf.length())
		{
			return m_buf.charAt(m_pch);
		}
		if (m_pch == m_buf.length() && m_tokens.usingEOF)
		{
			return (char)0xFFFF;
		}
		return (char)0;
	}
	public final void Advance()
	{
		++m_pch;
	}
	public int GetChar()
	{
		int r = PeekChar();
		++m_pch;
		return r;
	}
	public final void UnGetChar()
	{
		if (m_pch > 0)
		{
			--m_pch;
		}
	}
	private int Mark()
	{
		return m_pch - m_startMatch;
	}
	private void Restore(int mark)
	{
		m_pch = m_startMatch + mark;
	}
	private void Matching(boolean b)
	{
		m_matching = b;
		if (b)
		{
			m_startMatch = m_pch;
		}
	}
	public final _Enumerator GetEnumerator()
	{
		return new _Enumerator(this);
	}
	public final void Reset()
	{
		m_pch = 0;
		m_LineManager.backto(0);
	}
	public static class _Enumerator
	{
		private Lexer lxr;
		private TOKEN t;
		public _Enumerator(Lexer x)
		{
			lxr = x;
			t = null;
		}
		public final boolean MoveNext()
		{
			t = lxr.Next();
			return t != null;
		}
		public final TOKEN getCurrent()
		{
			return t;
		}
		public final void Reset()
		{
			lxr.Reset();
		}
	}
}