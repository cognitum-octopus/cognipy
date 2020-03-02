package tools;

import java.util.*;

// shame we have to do this ourselves, but SortedList doesn't allow incremental building of Dfas
public class NList
{ // sorted List of NfaNode
	public NfaNode m_node; // null for the sentinel
	public NList m_next;
	public NList()
	{
		m_node = null;
		m_next = null;
	} // sentinel only
	private NList(NfaNode nd, NList nx)
	{
		m_node = nd;
		m_next = nx;
	}
	public final boolean Add(NfaNode n)
	{
		if (m_node == null)
		{ // m_node==null iff m_next==null
			m_next = new NList();
			m_node = n;
		}
		else if (m_node.m_state < n.m_state)
		{
			m_next = new NList(m_node, m_next);
			m_node = n;
		}
		else if (m_node.m_state == n.m_state)
		{
			return false; // Add fails, there already
		}
		else
		{
			return m_next.Add(n);
		}
		return true; // was added
	}
	public final boolean getAtEnd()
	{
		return m_node == null;
	}
}