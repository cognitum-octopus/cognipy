package tools;

import java.util.*;
import java.io.*;

public class ProdItemList
{
	public ProdItem m_pi;
	public ProdItemList m_next;
	public ProdItemList(ProdItem pi, ProdItemList n)
	{
		m_pi = pi;
		m_next = n;
	}
	public ProdItemList()
	{
		m_pi = null;
		m_next = null;
	} // sentinel only
	public final boolean Add(ProdItem pi)
	{
		if (m_pi == null)
		{ // m_pi==null iff m_next==null
			m_next = new ProdItemList();
			m_pi = pi;
		}
		else if (m_pi.m_prod.m_pno < pi.m_prod.m_pno || (m_pi.m_prod.m_pno == pi.m_prod.m_pno && m_pi.m_pos < pi.m_pos))
		{
			m_next = new ProdItemList(m_pi, m_next);
			m_pi = pi;
		}
		else if (m_pi.m_prod.m_pno == pi.m_prod.m_pno && m_pi.m_pos == pi.m_pos)
		{
			return false;
		}
		else
		{
			return m_next.Add(pi);
		}
		return true; // was added
	}
	public final boolean getAtEnd()
	{
		return m_pi == null;
	}

}