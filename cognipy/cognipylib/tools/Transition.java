package tools;

import java.util.*;
import java.io.*;

public class Transition
{
	public int m_tno;
	public ParseState m_ps;
	public CSymbol m_A;
	public Transition(ParseState p, CSymbol a)
	{
		m_ps = p;
		m_A = a;
		m_tno = p.m_sgen.m_trans++;
		p.m_transitions.put(a.getYytext(), this);
	}
	private ParsingInfo getParsingInfo()
	{
		YyParser syms = m_ps.m_sgen.m_symbols;
		return syms.GetSymbolInfo(m_A.getYytext(), m_A.m_yynum);
	}
	public ParserShift m_next = null;
	public Hashtable m_reduce = new Hashtable(); // Production->ParserReduce
	private Hashtable m_reads = new Hashtable(); // Transition->bool
	private Hashtable m_includes = new Hashtable(); // Transition->bool
	public Hashtable m_lookbackOf = new Hashtable(); // ParserReduce->bool
	public static Hashtable reads(Transition a)
	{
		return a.m_reads;
	}
	public static Hashtable includes(Transition a)
	{
		return a.m_includes;
	}
	public static SymbolSet DR(Transition a)
	{
		return a.m_DR;
	}
	public static SymbolSet Read(Transition a)
	{
		return a.m_Read;
	}
	public static SymbolSet Follow(Transition a)
	{
		return a.m_Follow;
	}
	public static void AddToRead(Transition a, SymbolSet s)
	{
		a.m_Read.Add(s);
	}
	public static void AddToFollow(Transition a, SymbolSet s)
	{
		a.m_Follow.Add(s);
	}
	private SymbolSet m_DR; // built by BuildDR, called from parserGenerator
	private SymbolSet m_Read; // built using Digraph Compute called from ParserGenerator
	private SymbolSet m_Follow; // ditto
	public static void BuildDR(Transition t)
	{
		SymbolsGen sg = t.m_ps.m_sgen;
		t.m_DR = new SymbolSet(sg);
		if (t.m_next == null)
		{
			return;
		}
		for (Transition u : t.m_next.m_next.m_transitions.values())
		{
			if (u.m_next != null)
			{
				if (u.m_A.m_symtype == CSymbol.SymType.terminal || u.m_A.m_symtype == CSymbol.SymType.eofsymbol)
				{
					t.m_DR.AddIn(u.m_A);
				}
			}
		}
	}
	public static void Final(Transition t)
	{
		t.m_DR.AddIn(t.m_ps.m_sgen.m_symbols.EOFSymbol);
	}
	public static void BuildReads(Transition t)
	{
		t.m_Read = new SymbolSet(t.m_ps.m_sgen);
		ParseState ps = t.m_A.Next(t.m_ps);
		if (ps == null)
		{
			return;
		}
		for (Transition b : ps.m_transitions.values())
		{
			if (b.m_A.IsNullable())
			{
				t.m_reads.put(b, true);
			}
		}
	}
	public static void BuildIncludes(Transition t) // code improved by Wayne Kelly
	{
		t.m_Follow = new SymbolSet(t.m_ps.m_sgen);
		for (Production p : t.m_A.m_prods)
		{
			for (int i = p.m_rhs.getCount() - 1; i >= 0; i--)
			{
				CSymbol s = (CSymbol)p.m_rhs.get(i);
				if (s.m_symtype == CSymbol.SymType.nonterminal)
				{
					ParseState ps;
					if (i > 0)
					{
						ps = (new Path(t.m_ps, p.Prefix(i))).getTop();
					}
					else
					{
						ps = t.m_ps;
					}

					Transition b = (Transition) ps.m_transitions.get(s.getYytext());
					b.m_includes.put(t, true);
				}
				if (!s.IsNullable())
				{
					break;
				}
			}
		}
	}
	public static void BuildLookback(Transition t)
	{
		for (ParserReduce pr : t.m_reduce.values())
		{
			pr.BuildLookback(t);
		}
	}
	public static void BuildLA(Transition t)
	{
		for (ParserReduce pr : t.m_lookbackOf.keySet())
		{
			pr.m_lookAhead.Add(t.m_Follow);
		}
	}
	public static void BuildParseTable(Transition t)
	{
		YyParser syms = t.m_ps.m_sgen.m_symbols;
		ParsingInfo pi = t.getParsingInfo();
		ParserReduce red = null;
		for (ParserReduce pr : t.m_reduce.values())
		{
				if (t.m_ps.m_sgen.m_lalrParser? pr.m_lookAhead.Contains(t.m_A): pr.m_prod.m_lhs.m_follow.Contains(t.m_A))
				{
					if (red != null)
					{
						syms.erh.Error(new CSToolsException(12, String.format("reduce/reduce conflict %1$s vs %2$s",red.m_prod.m_pno,pr.m_prod.m_pno) + String.format(" state %1$s on %2$s",t.m_ps.m_state,t.m_A.getYytext())));
					}
					red = pr;
				}
			//	else 
			//		t.Print(pr.m_lookAhead,"discarding reduce ("+pr.m_prod.m_pno+") LA ");
		}
		if (t.m_next != null && t.m_A != syms.EOFSymbol)
		{
			if (red == null)
			{
				pi.m_parsetable.put(t.m_ps.m_state, t.m_next);
			}
			else
			{
				int p = Precedence.Check(t.m_A, red.m_prod, 0); // 4.7m
				if (p > 0)
				{
					pi.m_parsetable.put(t.m_ps.m_state, t.m_next);
					t.m_reduce.remove(red.m_prod);
				}
				else if (p < 0)
				{
					pi.m_parsetable.put(t.m_ps.m_state, red);
					t.m_next = null;
				}
				else
				{
					switch (t.m_A.ShiftPrecedence(red.m_prod, t.m_ps)) // 4.5h
					{
						case left:
							pi.m_parsetable.put(t.m_ps.m_state, t.m_next);
							t.m_reduce.remove(red.m_prod);
							break;
						case right:
							pi.m_parsetable.put(t.m_ps.m_state, red);
							t.m_next = null;
							break;
					}
				}
			}
		}
		else if (red != null)
		{
			pi.m_parsetable.put(t.m_ps.m_state, red);
		}
	}
	public final void Print0()
	{
		System.out.print("    " + m_A.getYytext());
		int actions = 0;
		if (m_next != null)
		{
			System.out.print("  shift " + m_next.m_next.m_state);
			actions++;
		}
		for (Production p : m_reduce.keySet())
		{
			System.out.print("  reduce (" + p.m_pno + ")");
		}
		if (actions + m_reduce.keySet().size() > 1)
		{
			System.out.print(": conflict");
		}
		System.out.println();
	}
	public final void Print(SymbolSet x, String s)
	{
		System.out.print("Transition (" + m_ps.m_state + "," + m_A.getYytext() + ") " + s + " ");
		x.Print();
	}
}