package tools;

import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
public class SymbolSet
{
	public SymbolsGen m_symbols;
	public SymbolSet m_next;
	private Hashtable m_set = new Hashtable(); // CSymbol -> bool
	public SymbolSet(SymbolsGen syms)
	{
		m_symbols = syms;
	}
	public SymbolSet(SymbolSet s)
	{
		this(s.m_symbols);
	Add(s);
	}
	public final boolean Contains(CSymbol a)
	{
		return m_set.containsKey(a);
	}
	public final Collection getKeys()
	{
		return m_set.keySet();
	}
	public final IDictionaryEnumerator GetEnumerator()
	{
		return m_set.entrySet().iterator();
	}
	public final int getCount()
	{
		return m_set.size();
	}
	public final boolean CheckIn(CSymbol a)
	{
		if (Contains(a))
		{
			return false;
		}
		AddIn(a);
		return true;
	}
	public final SymbolSet Resolve()
	{
		return find(m_symbols.lahead);
	}
	private SymbolSet find(SymbolSet h)
	{
		if (h == null)
		{
			m_next = m_symbols.lahead;
			m_symbols.lahead = this;
			return this;
		}
		if (Equals(h, this))
		{
			return h;
		}
		return find(h.m_next);
	}
	private static boolean Equals(SymbolSet s, SymbolSet t)
	{
		if (s.m_set.size() != t.m_set.size())
		{
			return false;
		}
		IDictionaryEnumerator de = s.iterator();
		IDictionaryEnumerator ee = t.iterator();
		for (int pos = 0; pos < s.getCount(); pos++)
		{
			de.MoveNext();
			ee.MoveNext();
			if (de.Key != ee.Key)
			{
				return false;
			}
		}
		return true;
	}
	public final void AddIn(CSymbol t)
	{
		m_set.put(t, true);
	}
	public final void Add(SymbolSet s)
	{
		if (s == this)
		{
			return;
		}
		for (CSymbol k : s.getKeys())
		{
			AddIn(k);
		}
	}
	public static SymbolSet opAdd(SymbolSet s, SymbolSet t)
	{
		SymbolSet r = new SymbolSet(s);
		r.Add(t);
		return r.Resolve();
	}
	public final void Print()
	{
		String pr = "[";
		int pos = 0;
		for (CSymbol s : getKeys())
		{
			pos++;
			if (s.getYytext().equals("\n"))
			{
				pr += "\\n";
			}
			else
			{
				pr += s.getYytext();
			}
			if (pos < getCount())
			{
				pr += ",";
			}
		}
		pr += "]";
		System.out.println(pr);
	}
}