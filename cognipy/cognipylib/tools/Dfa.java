package tools;

import java.util.*;

public class Dfa extends LNode
{
	private Dfa()
	{
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public Dfa(TokensGen tks)
	{
		super(tks);
		m_tokens = tks.m_tokens;
	}
//#endif
	private YyLexer m_tokens = null;
	public static void SetTokens(YyLexer tks, Hashtable h) // needed after deserialisation
	{
		for (Dfa v : h.values())
		{
			if (v.m_tokens != null)
			{
				continue;
			}
			v.m_tokens = tks;
			Dfa.SetTokens(tks, v.m_map);
		}
	}
	public Hashtable m_map = new Hashtable(); // char->Dfa: arcs leaving this node
	public static class Action
	{
		public int a_act;
		public Action a_next;
		public Action(int act, Action next)
		{
			a_act = act;
			a_next = next;
		}
		private Action()
		{
		}
		public static Object Serialise(Object o, Serialiser s)
		{
			if (s == null)
			{
				return new Action();
			}
			Action a = (Action)o;
			if (s.getEncode())
			{
				s.Serialise(a.a_act);
				s.Serialise(a.a_next);
				return null;
			}
			a.a_act = (Integer)s.Deserialise();
			a.a_next = (Action)s.Deserialise();
			return a;
		}
	}
	public String m_tokClass = ""; // token class name if m_actions!=null
	public Action m_actions = null; // for old-style REJECT
	public int m_reswds = -1; // 4.7 for ResWds handling
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	private void AddAction(int act)
	{
		Action a = new Action(act, m_actions);
		m_actions = a;
	}
	private void MakeLastAction(int act)
	{
		while (m_actions != null && m_actions.a_act >= act)
		{
			m_actions = m_actions.a_next;
		}
		AddAction(act);
	}
	public Dfa(Nfa nfa)
	{
		super(nfa.m_tks);
		m_tokens = m_tks.m_tokens;
		AddNfaNode(nfa); // the starting node is Closure(start)
		Closure();
		AddActions(); // recursively build the Dfa
	}
	public final boolean AddNfaNode(NfaNode nfa)
	{
		if (!m_nfa.Add(nfa))
		{
			return false;
		}
		if (!nfa.m_sTerminal.equals(""))
		{
			int qi,n = 0;
			String tokClass = "";
			String p = nfa.m_sTerminal;
			if (p.charAt(0) == '%')
			{ // check for %Tokname special action
				for (n = 0,qi = 1;qi < p.length();qi++,n++) // extract the class name
				{
					if (p.charAt(qi) == ' ' || p.charAt(qi) == '\t' || p.charAt(qi) == '\n' || p.charAt(qi) == '{' || p.charAt(qi) == ':')
					{
						break;
					}
				}
				tokClass = nfa.m_sTerminal.substring(1, 1 + n);
			}
			// check for ResWds machinery // 4.7
			if (n > 0 && n + 1 < p.length())
			{
				String st = nfa.m_sTerminal.substring(n + 1).trim();
				if (st.length() > 0)
				{
					if (st.startsWith("%except"))
					{
						m_reswds = nfa.m_state;
						m_tks.m_tokens.reswds.put(nfa.m_state, ResWds.New(m_tks, st.substring(7)));
					}
				}
			}
			// special action is always last in the list
			if (tokClass.equals(""))
			{ //nfa has an old action
				if (m_tokClass.equals("") || (m_actions.a_act) > nfa.m_state) // m_actions has at least one entry
				{
					AddAction(nfa.m_state);
				}
				// else we have a higher-precedence special action so we do nothing
			}
			else if (m_actions == null || m_actions.a_act > nfa.m_state)
			{
				MakeLastAction(nfa.m_state);
				m_tokClass = tokClass;
			} // else we have a higher-precedence special action so we do nothing
		}
		return true;
	}

	public NList m_nfa = new NList(); // nfa nodes in m_state order

	public final void AddActions()
	{
		// This routine is called for a new DFA node
		m_tks.states.Add(this);

		// Follow all the arcs from here
		for (Charset cs : m_tks.m_tokens.cats.values())
		{
			for (char j : cs.m_chars.keySet())
			{
				Dfa dfa = Target(j);
				if (dfa != null)
				{
					m_map.put(j, dfa);
				}
			}
		}
	}

	public final Dfa Target(char ch)
	{ // construct or lookup the target for a new arc
		Dfa n = new Dfa(m_tks);

		for (NList pos = m_nfa; !pos.getAtEnd(); pos = pos.m_next)
		{
			pos.m_node.AddTarget(ch, n);
		}
		// check we actually got something
		if (n.m_nfa.getAtEnd())
		{
			return null;
		}
		n.Closure();
		// now check we haven't got it already
		for (int pos1 = 0;pos1 < m_tks.states.getCount();pos1++)
		{
			if (((Dfa)m_tks.states.get(pos1)).SameAs(n))
			{
				return (Dfa)m_tks.states.get(pos1);
			}
		}
		// this is a brand new Dfa node so recursively build it
		n.AddActions();
		return n;
	}
	private void Closure()
	{
		for (NList pos = m_nfa; !pos.getAtEnd(); pos = pos.m_next)
		{
			ClosureAdd(pos.m_node);
		}
	}
	private void ClosureAdd(NfaNode nfa)
	{
		for (int pos = 0;pos < nfa.m_eps.getCount();pos++)
		{
			NfaNode p = (NfaNode)nfa.m_eps.get(pos);
			if (AddNfaNode(p))
			{
				ClosureAdd(p);
			}
		}
	}
	public final boolean SameAs(Dfa dfa)
	{
		NList pos1 = m_nfa;
		NList pos2 = dfa.m_nfa;
		while (pos1.m_node == pos2.m_node && !pos1.getAtEnd())
		{
			pos1 = pos1.m_next;
			pos2 = pos2.m_next;
		}
		return pos1.m_node == pos2.m_node;
	}
	// match a Dfa agsint a given string
	public final int Match(String str, int ix, tangible.RefObject<Integer> action)
	{ // return number of chars matched
		int r = 0;
		Dfa dfa = null;
		// if there is no arc or the string is exhausted, this is okay at a terminal
		if (ix >= str.length() || (dfa = ((Dfa)m_map.get(m_tokens.Filter(str.charAt(ix))))) == null || (r = dfa.Match(str, ix + 1, action)) < 0)
		{
			if (m_actions != null)
			{
				action.argValue = m_actions.a_act;
				return 0;
			}
			return -1;
		}
		return r + 1;
	}
	public final void Print()
	{
		System.out.printf("%1$s:",m_state);
		if (m_actions != null)
		{
			System.out.print(" (");
			for (Action a = m_actions; a != null; a = a.a_next)
			{
				System.out.printf("%1$s <",a.a_act);
			}
			if (!m_tokClass.equals(""))
			{
				System.out.print(m_tokClass);
			}
			System.out.print(">)");
		}
		System.out.println();
		Hashtable amap = new Hashtable(); // char->bool
		IDictionaryEnumerator idx = m_map.entrySet().iterator();
		for (int count = m_map.size(); count-- >0;)
		{
			idx.MoveNext();
			char j = (Character)idx.Key;
			Dfa pD = (Dfa)idx.Value;
			if (!amap.containsKey(j))
			{
				amap.put(j, true);
				System.out.printf("  %1$s  ",pD.m_state);
				int ij = (int)j;
				if (ij >= 32 && ij < 128)
				{
					System.out.print(j);
				}
				else
				{
					System.out.printf(" #%1$s ",ij);
				}
				IDictionaryEnumerator idy = m_map.entrySet().iterator();
				for (;;)
				{
					idy.MoveNext();
					Dfa pD1 = (Dfa)idy.Value;
					if (pD1 == pD)
					{
						break;
					}
				}
				for (int count1 = count;count1 > 0;count1--)
				{
					idy.MoveNext();
					j = (Character)idy.Key;
					Dfa pD1 = (Dfa)idy.Value;
					if (pD == pD1)
					{
						amap.put(j, true);
						ij = (int)j;
						if (ij >= 32 && ij < 128)
						{
							System.out.print(j);
						}
						else
						{
							System.out.printf(" #%1$s ",ij);
						}
					}
				}
				System.out.println();
			}
		}
	}
//#endif
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new Dfa();
		}
		Dfa d = (Dfa)o;
		if (s.getEncode())
		{
			s.Serialise(d.m_state);
			s.Serialise(d.m_map);
			s.Serialise(d.m_actions);
			s.Serialise(d.m_tokClass);
			s.Serialise(d.m_reswds);
			return null;
		}
		d.m_state = (Integer)s.Deserialise();
		d.m_map = (Hashtable)s.Deserialise();
		d.m_actions = (Action)s.Deserialise();
		d.m_tokClass = (String)s.Deserialise();
		d.m_reswds = (Integer)s.Deserialise();
		return d;
	}
}