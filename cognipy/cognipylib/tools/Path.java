package tools;

import java.util.*;
import java.io.*;

public class Path
{
	public boolean valid = true;
	private ParseState[] m_states;
	public Path(ParseState[] s)
	{
		m_states = s;
	}
	public Path(ParseState q, CSymbol[] x)
	{
		m_states = new ParseState[x.length + 1];
		ParseState c;
		c = m_states[0] = q;
		for (int j = 0;j < x.length;j++)
		{
			int k;
			for (k = j;k < x.length;k++)
			{
				if (!x[k].IsAction())
				{
					break;
				}
			}
			if (k >= x.length)
			{
				m_states[j + 1] = c;
				continue;
			}
			Transition t = (Transition)c.m_transitions.get(x[k].getYytext());
			if (t == null || t.m_next == null)
			{
				valid = false;
				break;
			}
			c = m_states[j + 1] = t.m_next.m_next;
		}
	}
	public Path(CSymbol[] x)
	{
		this((ParseState)(x[0].m_parser.m_symbols.m_states.get(0)), x);
	}
	public final CSymbol[] getSpelling()
	{
		CSymbol[] r = new CSymbol[m_states.length - 1];
		for (int j = 0;j < r.length;j++)
		{
			r[j] = m_states[j].m_accessingSymbol;
		}
		return r;
	}
	public final ParseState getTop()
	{
		return m_states[m_states.length - 1];
	}
}