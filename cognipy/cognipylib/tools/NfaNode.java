package tools;

import java.util.*;

public class NfaNode extends LNode
{
	public String m_sTerminal = ""; // or something for the Lexer
	public ObjectList m_arcs = new ObjectList(); // of Arc for labelled arcs
	public ObjectList m_eps = new ObjectList(); // of NfaNode for unlabelled arcs
	public NfaNode(TokensGen tks)
	{
		super(tks);
	}

	// build helpers
	public final void AddArc(char ch, NfaNode next)
	{
		m_arcs.Add(new Arc(ch, next));
	}
	public final void AddUArc(char ch, NfaNode next)
	{
		m_arcs.Add(new UArc(ch, next));
	}
	public final void AddArcEx(Regex re, NfaNode next)
	{
		m_arcs.Add(new ArcEx(re, next));
	}
	public final void AddEps(NfaNode next)
	{
		m_eps.Add(next);
	}

	// helper for building DFa
	public final void AddTarget(char ch, Dfa next)
	{
		for (int j = 0; j < m_arcs.getCount(); j++)
		{
			Arc a = (Arc)m_arcs.get(j);
			if (a.Match(ch))
			{
				next.AddNfaNode(a.m_next);
			}
		}
	}
}