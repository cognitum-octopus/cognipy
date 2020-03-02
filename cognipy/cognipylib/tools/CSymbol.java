package tools;

import java.util.*;
import java.io.*;

//#endif
public class CSymbol extends TOKEN // may be terminal (symbolic or literal), non-terminal or ParserAction
{
	// because of forward declarations etc, a named symbol can appear in the rhs of a production
	// without us knowing if it is a terminal or a nonterminal
	// if something is a node, or an OldAction, we will know at once
	public enum SymType
	{
		unknown,
		terminal,
		nonterminal,
		nodesymbol,
		oldaction,
		simpleaction,
		eofsymbol;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static SymType forValue(int value)
		{
			return values()[value];
		}
	}
	public SymType m_symtype = SymType.values()[0];
	@Override
	public boolean IsTerminal()
	{
		return m_symtype == SymType.terminal;
	}
	public CSymbol(Lexer yyl)
	{
		super(yyl);
	}
	public int m_yynum = -1;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public SymbolsGen m_parser;
	// this list accumulates information about classes defined in the parser script
	// so that the relevant parts of the generated file can be written out afterwards
	public CSymbol Resolve()
	{
		if (getYytext().equals("EOF"))
		{
			m_yynum = 2;
		}
		CSymbol s = (CSymbol)m_parser.m_symbols.symbols.get(getYytext());
		if (s != null)
		{
			return s;
		}
		if (m_yynum < 0)
		{
			m_yynum = ++m_parser.LastSymbol;
		}
		m_parser.m_symbols.symbols.put(getYytext(), this);
		return this;
	}
	public CSymbol(SymbolsGen yyp)
	{
		super(yyp.m_lexer);
		m_parser = yyp;
		m_symtype = SymType.unknown;
		m_prec = null;
		m_prod = null;
		m_refSymbol = null;
		m_first = new SymbolSet(yyp);
		m_follow = new SymbolSet(yyp);
	}
	@Override
	public boolean Matches(String s)
	{
		return false;
	}

	public final ParseState Next(ParseState p)
	{
		if (!p.m_transitions.containsKey(getYytext()))
		{
			return null;
		}
		ParserShift ps = ((Transition)p.m_transitions.get(getYytext())).m_next;
		if (ps == null)
		{
			return null;
		}
		return ps.m_next;
	}
	public final Hashtable Reduce(ParseState p) // Objectlist of ParserReduce to distinct productions
	{
		if (!p.m_transitions.containsKey(getYytext()))
		{
			return null;
		}
		return ((Transition)p.m_transitions.get(getYytext())).m_reduce;
	}
	// for adding typecasts to $n
	public String TypeStr()
	{
		return getYytext();
	}

	// for terminals
	public Precedence m_prec;
	public final Precedence.PrecType ShiftPrecedence(Production prod, ParseState ps) // 4.5h
	{
		if (prod == null) // no reduce available
		{
			return Precedence.PrecType.left; // shift // 4.5h
		}
		if (!((SymbolSet)prod.m_lhs.m_follow).Contains(this)) // if this is not a follow symbol of the prod's lhs, there is no conflict
		{
			return Precedence.PrecType.left; // shift  // 4.5h
		}
		if (m_prec == null)
		{ // no precedence information
			System.out.printf("Shift/Reduce conflict %1$s on reduction %2$s in state %3$s" + "\r\n", getYytext(), prod.m_pno,ps.m_state);
			return Precedence.PrecType.left; // shift anyway // 4.5h
		}
		if (m_prec.m_type == Precedence.PrecType.nonassoc) // 4.5h
		{
			return Precedence.PrecType.nonassoc; // 4.5h
		}
		int p = Precedence.Check(this, prod, 0);
		if (p == 0)
		{
			if (Precedence.Check(m_prec, Precedence.PrecType.right, 0) != 0)
			{ // equal precedence but right associative: shift
				return Precedence.PrecType.left; // 4.5h
			}
			return Precedence.PrecType.right; // don't shift // 4.5h
		}
		return (p > 0)?Precedence.PrecType.left:Precedence.PrecType.right; // shift if symbol has higher precedence than production, else reduce // 4.5h
	}
	// for non-terminals
	public SymbolSet m_first;
	public SymbolSet m_follow; // for LR(0) phase: allow EOFSymbol
	public final boolean AddFollow(SymbolSet map)
	{ // CSymbol->bool : add contents of map to m_follow
		boolean r = false;
		for (CSymbol a : map.getKeys())
		{
			r |= m_follow.CheckIn(a);
		}
		return r;
	}
	public ObjectList m_prods = new ObjectList(); // Production:  productions with this symbol as left side
	public final void AddStartItems(ParseState pstate, SymbolSet follows)
	{
		for (int pos = 0;pos < m_prods.getCount();pos++)
		{
			Production p = (Production)m_prods.get(pos);
			pstate.MaybeAdd(new ProdItem(p, 0));
		}
	}
	private Object isNullable = null; // used to cache the value of IsNullable
	public final boolean IsNullable() // suggested by Wayne Kelly
	{
		if (isNullable == null) // if not already computed
		{
			switch (m_symtype)
			{
				case simpleaction:
					isNullable = true;
					break;
				case oldaction:
					isNullable = true;
					break;
				case terminal:
					isNullable = false;
					break;
				case eofsymbol:
					isNullable = false;
					break;
				case nonterminal:
					isNullable = false;
					for (Production p : m_prods)
					{
						boolean nullable = true;
						for (CSymbol rhs : p.m_rhs)
						{
							if (!rhs.IsNullable())
							{
								nullable = false;
								break;
							}
						}
						if (nullable)
						{
							isNullable = true;
							break;
						}
					}
					break;
				default:
					throw new RuntimeException("unexpected symbol type");
			}
		}
		return (Boolean) isNullable;
	}
	// for nodesymbols
	public CSymbol m_refSymbol; // maybe null

	// class definition info
	public String m_initialisation = ""; // may be empty
	public boolean m_defined = false;
	public boolean m_emitted = false;
	public Production m_prod; // production where this initialisation occurs: maybe null
//#endif
	protected CSymbol()
	{
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new CSymbol();
		}
		CSymbol c = (CSymbol)o;
		if (s.getEncode())
		{
			s.Serialise(c.getYytext());
			s.Serialise(c.m_yynum);
			s.Serialise(c.m_symtype.getValue());
			return null;
		}
		c.setYytext((String)s.Deserialise());
		c.m_yynum = (Integer)s.Deserialise();
		c.m_symtype = (SymType)s.Deserialise();
		return c;
	}

}