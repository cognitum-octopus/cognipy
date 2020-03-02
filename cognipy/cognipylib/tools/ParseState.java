package tools;

import java.util.*;
import java.io.*;

// The Closure and AddActions functions represent the heart of the ParserGenerator
//#endif

public class ParseState
{
	public int m_state;
	public CSymbol m_accessingSymbol;
	private boolean m_changed = true;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public SymbolsGen m_sgen;
	public ProdItemList m_items; // ProdItem, in ProdItem order
	public Hashtable m_transitions = new Hashtable(); // string -> Transition
	public final Transition GetTransition(CSymbol s)
	{
		Transition t = (Transition)m_transitions.get(s.getYytext());
		if (t != null)
		{
			return t;
		}
		return new Transition(this, s);
	}
	public final boolean Accessor(CSymbol[] x)
	{
		return (new Path(x)).getTop() == this;
	}
	public final boolean Lookback(Production pr, ParseState p)
	{
		return (new Path(this, pr.Prefix(pr.m_rhs.getCount()))).getTop() == this;
	}
	public final void MaybeAdd(ProdItem item)
	{ // called by CSymbol.AddStartItems
		if (!m_items.Add(item))
		{
			return;
		}
		m_changed = true;
	}
	public final void Closure()
	{
		while (m_changed)
		{
			m_changed = false;
			for (ProdItemList pi = m_items; pi.m_pi != null; pi = pi.m_next)
			{
				CheckClosure(pi.m_pi);
			}
		}
	}
	public final void CheckClosure(ProdItem item)
	{
		CSymbol ss = item.Next();
		if (ss != null)
		{
			ss.AddStartItems(this, item.FirstOfRest(ss.m_parser));
			if (item.IsReducingAction())
			{
				MaybeAdd(new ProdItem(item.m_prod, item.m_pos + 1));
			}
		}
	}
	public final void AddEntries()
	{
		ProdItemList pil;
		for (pil = m_items; pil.m_pi != null; pil = pil.m_next)
		{
			ProdItem item = pil.m_pi;
			if (item.m_done)
			{
				continue;
			}
			CSymbol s = item.Next();
			if (s == null || item.IsReducingAction())
			{
				continue;
			}
			// shift/goto action
			// Build a new parse state as target: we will check later to see if we need it
			ParseState p = new ParseState(m_sgen, s);
			// the new state should have at least the successor of this item
			p.MaybeAdd(new ProdItem(item.m_prod, item.m_pos + 1));

			// check the rest of the items in this ParseState (leads to m_done for them)
			// looking for other items that allow this CSymbol to pass
			for (ProdItemList pil1 = pil.m_next; pil1 != null && pil1.m_pi != null; pil1 = pil1.m_next)
			{
				ProdItem another = pil1.m_pi;
				if (s == another.Next())
				{
					p.MaybeAdd(new ProdItem(another.m_prod, another.m_pos + 1));
					another.m_done = true;
				}
			}

			if (!m_items.getAtEnd())
			{
				if (s.IsAction())
				{
					p = p.CheckExists();
					for (CSymbol f : s.m_follow.getKeys())
					{
						if (f != m_sgen.m_symbols.EOFSymbol)
						{
							Transition t = GetTransition(f);
//								if (t.m_next!=null)
//									m_sgen.Error(15,s.pos,String.Format("Action/Action or Action/Shift conflict on {0}",f.yytext));
							t.m_next = new ParserShift((ParserAction)s, p);
						}
					}
				}
				else
				{ // we guarantee to make a nonzero entry in the parsetable
					GetTransition(s).m_next = new ParserShift(null, p.CheckExists());
				}
			}
		}
	}
	public final void ReduceStates()
	{
		ProdItemList pil;
		for (pil = m_items; pil.m_pi != null; pil = pil.m_next)
		{
			ProdItem item = pil.m_pi;
			CSymbol s = item.Next();
			if (s == null)
			{ // item is a reducing item
				Production rp = item.m_prod;
				if (rp.m_pno == 0) // except for production 0: S'->S-|
				{
					continue;
				}
				// reduce item: deal with it 
				int n = rp.m_rhs.getCount();
				CSymbol a;
				ParserReduce pr;
				if (n > 0 && (a = (CSymbol)rp.m_rhs.get(n - 1)) != null && a.IsAction())
				{
					ParserAction pa = (ParserAction)a;
					pa.m_len = n;
					pr = new ParserReduce(pa, n - 1, rp);
				}
				else
				{
					m_sgen.m_lexer.yytext = "%" + rp.m_lhs.getYytext();
					m_sgen.m_prod = rp;
					ParserSimpleAction sa = new ParserSimpleAction(m_sgen);
					sa.m_sym = (CSymbol)rp.m_lhs;
					sa.m_len = n;
					pr = new ParserReduce(sa, n, rp);
				}
				for (CSymbol ss : item.m_prod.m_lhs.m_follow.getKeys())
				{
					GetTransition(ss).m_reduce.put(rp, pr);
				}
			}
		}
	}
	public final boolean SameAs(ParseState p)
	{
		if (m_accessingSymbol != p.m_accessingSymbol)
		{
			return false;
		}
		ProdItemList pos1 = m_items;
		ProdItemList pos2 = p.m_items;
		while (!pos1.getAtEnd() && !pos2.getAtEnd() && pos1.m_pi.m_prod == pos2.m_pi.m_prod && pos1.m_pi.m_pos == pos2.m_pi.m_pos)
		{
			pos1 = pos1.m_next;
			pos2 = pos2.m_next;
		}
		return pos1.getAtEnd() && pos2.getAtEnd();
	}
	public final ParseState CheckExists()
	{
		Closure();
		for (ParseState p : m_sgen.m_symbols.m_states.values())
		{
			if (SameAs(p))
			{
				return p;
			}
		}
		m_sgen.m_symbols.m_states.put(m_state, this);
		AddEntries();
		return this;
	}
	protected void finalize() throws Throwable
	{
		if (m_sgen != null && m_state == m_sgen.state-1)
		{
			m_sgen.state--;
		}
	}
	public ParseState(SymbolsGen syms, CSymbol acc)
	{
		m_sgen = syms;
		m_state = syms.state++;
		m_accessingSymbol = acc;
		m_items = new ProdItemList();
	}
	public final void Print()
	{
		System.out.println();
		if (m_state == 0)
		{
			System.out.println("state 0");
		}
		else
		{
			System.out.printf("state %1$s accessed by %2$s" + "\r\n",m_state,m_accessingSymbol.getYytext());
		}
		// first about the state itself
		if (m_items != null)
		{
			for (ProdItemList pil = m_items; pil.m_pi != null; pil = pil.m_next)
			{
				pil.m_pi.Print();
				pil.m_pi.m_prod.m_lhs.m_follow.Print();
			}
		}
		for (Transition t : m_transitions.values())
		{
			t.Print0();
		}
	}
	public final void Print0()
	{
		System.out.println();
		if (m_state == 0)
		{
			System.out.println("state 0");
		}
		else
		{
			System.out.printf("state %1$s accessed by %2$s" + "\r\n",m_state,m_accessingSymbol.getYytext());
		}
		// first about the state itself
		if (m_items != null)
		{
			for (ProdItemList pil = m_items; pil.m_pi != null; pil = pil.m_next)
			{
				pil.m_pi.Print();
				System.out.println();
			}
		}
		// next about the transitions
		System.out.println();
		for (ParsingInfo pi : m_sgen.m_symbols.symbolInfo.values())
		{
			PrintTransition(pi);
		}
	}
	private void PrintTransition(ParsingInfo pi)
	{
		ParserEntry pe = (ParserEntry)pi.m_parsetable.get(m_state);
		if (pe != null)
		{
			System.out.printf("        %1$s  %2$s  ", pi.m_name,pe.getStr());
			if (pe.m_action != null)
			{
				pe.m_action.Print();
			}
			System.out.println();
		}
	}
//#endif
	private ParseState()
	{
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ParseState();
		}
		ParseState p = (ParseState)o;
		if (s.getEncode())
		{
			s.Serialise(p.m_state);
			s.Serialise(p.m_accessingSymbol);
			s.Serialise(p.m_changed);
			return true;
		}
		p.m_state = (Integer)s.Deserialise();
		p.m_accessingSymbol = (CSymbol)s.Deserialise();
		p.m_changed = (Boolean)s.Deserialise();
		return p;
	}
}