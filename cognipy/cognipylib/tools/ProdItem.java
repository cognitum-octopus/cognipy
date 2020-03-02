package tools;

import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
public class ProdItem
{
	public ProdItem(Production prod, int pos)
	{
		m_prod = prod;
		m_pos = pos;
		m_done = false;
	}
	public ProdItem()
	{
		m_prod = null;
		m_pos = 0;
		m_done = false;
	}
	public Production m_prod;
	public int m_pos;
	public boolean m_done;
	public final CSymbol Next()
	{
		if (m_pos < m_prod.m_rhs.getCount())
		{
			return (CSymbol)m_prod.m_rhs.get(m_pos);
		}
		return null;

	}
	public final boolean IsReducingAction()
	{
		return (m_pos == m_prod.m_rhs.getCount() - 1) && Next().IsAction();
	}
	private SymbolSet follow = null;
	public final SymbolSet FirstOfRest(SymbolsGen syms)
	{
		if (follow != null)
		{
			return follow;
		}
		follow = new SymbolSet(syms);
		boolean broke = false;
		int n = m_prod.m_rhs.getCount();
		for (int j = m_pos + 1;j < n;j++)
		{
			CSymbol s = (CSymbol)m_prod.m_rhs.get(j);
			for (CSymbol a : s.m_first.getKeys())
			{
					follow.CheckIn(a);
			}
			if (!s.IsNullable())
			{
				broke = true;
				break;
			}
		}
		if (!broke)
		{
			follow.Add(m_prod.m_lhs.m_follow);
		}
		follow = follow.Resolve();
		return follow;
	}
	public final void Print()
	{
		int j;
		String str,s;

		if (m_prod.m_lhs != null)
		{
			str = m_prod.m_lhs.getYytext();
		}
		else
		{
			str = "$start";
		}
		System.out.printf("   %1$s    %2$s : ", m_prod.m_pno, str);
		for (j = 0;j < m_prod.m_rhs.getCount();j++)
		{
			if (j == m_pos)
			{
				System.out.print("_");
			}
			else
			{
				System.out.print(" ");
			}
			s = ((CSymbol)m_prod.m_rhs.get(j)).getYytext();
			if (s.equals("\n"))
			{
				s = "\\n";
			}
			System.out.print(s);
		}
		if (j == m_pos)
		{
			System.out.print("_");
		}
		System.out.print("  ");
	}
}