package tools;

import java.util.*;

// An NFA is defined by a start and end state
// Here we derive the Nfa from a NfaNode which acts as the start state

public class Nfa extends NfaNode
{
	public NfaNode m_end;
	public Nfa(TokensGen tks)
	{
		super(tks);
		m_end = new NfaNode(m_tks);
	}
	// build an NFA for a given regular expression
	public Nfa(TokensGen tks, Regex re)
	{
		super(tks);
		m_end = new NfaNode(tks);
		re.Build(this);
	}
}