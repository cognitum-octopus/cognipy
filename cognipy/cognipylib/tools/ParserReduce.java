package tools;

import java.util.*;
import java.io.*;

public class ParserReduce extends ParserEntry
{
	public int m_depth;
	public Production m_prod;
	public ParserReduce(ParserAction action, int depth, Production prod)
	{
		super(action);
	m_depth = depth;
	m_prod = prod;
	}
	private ParserReduce()
	{
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public SymbolSet m_lookAhead = null;
	public final void BuildLookback(Transition a)
	{
		SymbolsGen sg = a.m_ps.m_sgen;
		if (m_lookAhead != null)
		{
			return;
		}
		m_lookAhead = new SymbolSet(sg);
		for (ParseState p : sg.m_symbols.m_states.values())
		{
			Transition b = (Transition)p.m_transitions.get(m_prod.m_lhs.getYytext());
			if (b == null)
			{
				continue;
			}
			Path pa = new Path(p, m_prod.Prefix(m_prod.m_rhs.getCount()));
			if (pa.valid && pa.getTop() == a.m_ps)
			{
				b.m_lookbackOf.put(this, true);
			}
		}
	}
//#endif
	@Override
	public void Pass(tangible.RefObject<ParseStackEntry> top)
	{
		Parser yyp = top.argValue.yyps;
		SYMBOL ns = m_action.Action(yyp); // before we change the stack
		yyp.m_ungot = top.argValue.m_value;
		if (yyp.m_debug)
		{
			System.out.printf("about to pop %1$s count is %2$s" + "\r\n", m_depth, yyp.m_stack.getCount());
		}
		yyp.Pop(top, m_depth, ns);
		if (ns.pos == 0)
		{
			ns.pos = top.argValue.m_value.pos; // Guess symbol position
		}
		top.argValue.m_value = ns;
	}
	@Override
	public boolean IsReduce()
	{
		return true;
	}
	@Override
	public String getStr()
	{
		if (m_prod == null)
		{
			return "?? null reduce";
		}
		return String.format("reduce %1$s", m_prod.m_pno);
	}
//C# TO JAVA CONVERTER WARNING: There is no Java equivalent to C#'s shadowing via the 'new' keyword:
//ORIGINAL LINE: public new static object Serialise(object o, Serialiser s)
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new ParserReduce();
		}
		ParserReduce p = (ParserReduce)o;
		if (s.getEncode())
		{
			ParserEntry.Serialise(p, s);
			s.Serialise(p.m_depth);
			s.Serialise(p.m_prod);
			return null;
		}
		ParserEntry.Serialise(p, s);
		p.m_depth = (Integer)s.Deserialise();
		p.m_prod = (Production)s.Deserialise();
		return p;
	}
}