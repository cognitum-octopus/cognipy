package tools;

import java.util.*;
import java.io.*;

public class Production
{
	public int m_pno;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public CSymbol m_lhs;
	public boolean m_actionsOnly;
	public int m_prec;
	public Production(SymbolsGen syms)
	{
		m_lhs = null;
		m_prec = 0;
		m_pno = syms.pno++;
		m_actionsOnly = true;
		syms.prods.Add(this);
	}
	public Production(SymbolsGen syms, CSymbol lhs)
	{
		m_lhs = lhs;
		m_prec = 0;
		m_pno = syms.pno++;
		m_actionsOnly = true;
		syms.prods.Add(this);
		lhs.m_prods.Add(this);
	}
	public ObjectList m_rhs = new ObjectList(); // CSymbol
	public Hashtable m_alias = new Hashtable(); // string->int
	public final void AddToRhs(CSymbol s)
	{
		m_rhs.Add(s);
		m_actionsOnly = m_actionsOnly && s.IsAction();
	}
	public final void AddFirst(CSymbol s, int j)
	{
		for (;j < m_rhs.getCount();j++)
		{
			CSymbol r = (CSymbol)m_rhs.get(j);
			s.AddFollow(r.m_first);
			if (!r.IsNullable())
			{
				return;
			}
		}
	}
	public final boolean CouldBeEmpty(int j)
	{
		for (;j < m_rhs.getCount();j++)
		{
			CSymbol r = (CSymbol)m_rhs.get(j);
			if (!r.IsNullable())
			{
				return false;
			}
		}
		return true;
	}
	public final CSymbol[] Prefix(int i)
	{
		CSymbol[] r = new CSymbol[i];
		for (int j = 0;j < i;j++)
		{
			r[j] = (CSymbol)m_rhs.get(j);
		}
		return r;
	}

	// inside ACTIONs, $N translates to ((SomeSymbol *)(parser.StackAt(K-N-1).m_value))
	// where K is the position of the action in the production

	public final void StackRef(tangible.RefObject<String> str, int ch, int ix)
	{
		int ln = m_rhs.getCount() + 1;
		CSymbol ts = (CSymbol)m_rhs.get(ix - 1);
		str.argValue += String.format("\n\t((%1$s)(yyq.StackAt(%2$s).m_value))\n\t",ts.TypeStr(),ln - ix - 1);
	}
//#endif
	private Production()
	{
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new Production();
		}
		Production p = (Production)o;
		if (s.getEncode())
		{
			s.Serialise(p.m_pno);
			return null;
		}
		p.m_pno = (Integer)s.Deserialise();
		return p;
	}
}