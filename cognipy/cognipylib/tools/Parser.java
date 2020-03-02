package tools;

import java.util.*;
import java.io.*;

//#endif
public class Parser
{
	public YyParser m_symbols;
	public boolean m_debug;
	public boolean m_stkdebug = false;
	public Parser(YyParser syms, Lexer lexer)
	{
		m_lexer = lexer;
		m_symbols = syms;
		m_symbols.erh = m_lexer.getTokens().erh;
	}
	public Lexer m_lexer;
	public ObjectList m_stack = new ObjectList(); // ParseStackEntry
	public SYMBOL m_ungot;

	private void Create()
	{
		m_symbols.GetParser(m_lexer);
	}

	protected final boolean Error(tangible.RefObject<ParseStackEntry> top, String str)
	{
		SYMBOL er = (SYMBOL)new error(this, top.argValue); // 4.4c
		if (m_debug)
		{
			System.out.println("Error encountered: " + str);
		}
		er.pos = top.argValue.m_value.pos;
		ParserEntry pe;
		if (m_symbols.symbolInfo.get(0) != null && m_symbols.erh.counter < 1000) // 4.4c
		{
																			 // first pop the stack until we find an item that can pass error
			for (; top.argValue != null && m_stack.getCount() > 0; Pop(top, 1, er))
			{
				if (m_debug)
				{
					System.out.printf("Error recovery uncovers state %1$s" + "\r\n", top.argValue.m_state);
				}
				tangible.OutObject<tools.ParserEntry> tempOut_pe = new tangible.OutObject<tools.ParserEntry>();
				if (er.Pass(m_symbols, top.argValue.m_state, tempOut_pe))
				{
				pe = tempOut_pe.argValue;
					SYMBOL oldtop = top.argValue.m_value;
					top.argValue.m_value = er;
					pe.Pass(top); // pass the error symbol
									  // now discard tokens until we find one we can pass
					tangible.OutObject<tools.ParserEntry> tempOut_pe2 = new tangible.OutObject<tools.ParserEntry>();
					while (top.argValue.m_value != m_symbols.EOFSymbol && !top.argValue.m_value.Pass(m_symbols, top.argValue.m_state, tempOut_pe2))
					{
					pe = tempOut_pe2.argValue;
						SYMBOL newtop;
						if (pe != null && pe.IsReduce())
						{
							newtop = null;
							if (pe.m_action != null)
							{
								newtop = pe.m_action.Action(this); // before we change the stack
							}
							m_ungot = top.argValue.m_value;
							Pop(top, ((ParserReduce)pe).m_depth, er);
							newtop.pos = top.argValue.m_value.pos;
							top.argValue.m_value = newtop;
						}
						else
						{ // discard it
							String cnm = top.argValue.m_value.getYyname();
							if (m_debug)
							{
								if (cnm.equals("TOKEN"))
								{
									System.out.printf("Error recovery discards literal %1$s" + "\r\n", (String)((TOKEN)top.argValue.m_value).getYytext());
								}
								else
								{
									System.out.printf("Error recovery discards token %1$s" + "\r\n", cnm);
								}
							}
							top.argValue.m_value = NextSym();
						}
					}
				pe = tempOut_pe2.argValue;
					if (m_debug)
					{
						System.out.println("Recovery complete");
					}
					m_symbols.erh.counter++;
					return true;
				}
			else
			{
				pe = tempOut_pe.argValue;
			}
			}
		}
		m_symbols.erh.Error(new CSToolsException(13, m_lexer, er.pos, "syntax error", str));
		top.argValue.m_value = er;
		return false;
	}

	public final SYMBOL Parse(InputStreamReader input)
	{
		m_lexer.Start(input);
		return Parse();
	}
	public final SYMBOL Parse(CsReader inFile)
	{
		m_lexer.Start(inFile);
		return Parse();
	}
	public final SYMBOL Parse(String buf)
	{
		m_lexer.Start(buf);
		return Parse();
	}

	public final boolean TestParse(String buf)
	{
		m_lexer.Start(buf);

		ParserEntry pe;
		Create();
		ParseStackEntry top = new ParseStackEntry(this, 0, NextSym());
		try
		{
			for (; ;)
			{
				String cnm = top.m_value.getYyname();
				tangible.OutObject<tools.ParserEntry> tempOut_pe = new tangible.OutObject<tools.ParserEntry>();
				if (top.m_value != null && top.m_value.Pass(m_symbols, top.m_state, tempOut_pe))
				{
				pe = tempOut_pe.argValue;
					tangible.RefObject<tools.ParseStackEntry> tempRef_top = new tangible.RefObject<tools.ParseStackEntry>(top);
					pe.Pass(tempRef_top);
				top = tempRef_top.argValue;
				}
				else
				{
				pe = tempOut_pe.argValue;
					if (top.m_value == m_symbols.EOFSymbol)
					{
						return true;
					}
					else
					{
						tangible.RefObject<tools.ParseStackEntry> tempRef_top2 = new tangible.RefObject<tools.ParseStackEntry>(top);
						if (!Error(tempRef_top2, "syntax error")) // unrecovered error
						{
						top = tempRef_top2.argValue;
							return false;
						}
					else
					{
						top = tempRef_top2.argValue;
					}
					}
				}
			}
			// not reached
		}
		catch (CSToolsStopException ex) // stop parsing
		{
			if (m_symbols.erh.throwExceptions)
			{
				throw ex; // 4.5b
			}
			m_symbols.erh.Report(ex); // 4.5b
		}
		return false;
	}

	public final ObjectList CompletionProposals(String buf)
	{
		int ern = m_lexer.getTokens().erh.counter;
		m_lexer.Start(buf);
		Create();

		ParserEntry pe;
		ObjectList ret = null;
		ParseStackEntry top = new ParseStackEntry(this, 0, NextSym());
		if (m_ungot == null)
		{
			ObjectList na = AutoCompl(0, m_symbols.arr[0]);
			if (na != null)
			{
				ret = na;
			}
		}

		try
		{
			for (; ;)
			{
				String cnm = top.m_value.getYyname();
				if (m_ungot == null)
				{
					ObjectList na = AutoCompl(top.m_state, top.m_value.getYynum());
					if (na != null)
					{
						ret = na;
					}
				}
				tangible.OutObject<tools.ParserEntry> tempOut_pe = new tangible.OutObject<tools.ParserEntry>();
				if (top.m_value != null && top.m_value.Pass(m_symbols, top.m_state, tempOut_pe))
				{
				pe = tempOut_pe.argValue;
					tangible.RefObject<tools.ParseStackEntry> tempRef_top = new tangible.RefObject<tools.ParseStackEntry>(top);
					pe.Pass(tempRef_top);
				top = tempRef_top.argValue;
				}
				else
				{
				pe = tempOut_pe.argValue;
					if (top.m_value == m_symbols.EOFSymbol)
					{
						if (ern != m_lexer.getTokens().erh.counter)
						{
							return null;
						}
						else
						{
							return ret;
						}
					}
					else
					{
						tangible.RefObject<tools.ParseStackEntry> tempRef_top2 = new tangible.RefObject<tools.ParseStackEntry>(top);
						if (!Error(tempRef_top2, "syntax error")) // unrecovered error
						{
						top = tempRef_top2.argValue;
							return null;
						}
					else
					{
						top = tempRef_top2.argValue;
					}
					}
				}
			}
			// not reached
		}
		catch (CSToolsStopException ex) // stop parsing
		{
			if (m_symbols.erh.throwExceptions)
			{
				throw ex; // 4.5b
			}
			m_symbols.erh.Report(ex); // 4.5b
		}
		return null;
	}

	// The Parsing Algorithm

	private ObjectList AutoCompl(int state, int symbol)
	{
		ParsingInfo pi = (ParsingInfo)m_symbols.symbolInfo.get(symbol);
		boolean r = pi.m_parsetable.containsKey(state);
		ParserEntry entry = r ? ((ParserEntry)pi.m_parsetable.get(state)) : null;
		if (entry instanceof ParserShift)
		{
			ObjectList ret = new ObjectList();
			ParseState nx = ((ParserShift)entry).m_next;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var x : m_symbols.symbolInfo.values())
			{
				if (((ParsingInfo)x).m_parsetable.containsKey(nx.m_state))
				{
					//                        if (((ParsingInfo)x).m_parsetable[nx.m_state] is ParserShift)
					//                      {
					if (((ParsingInfo)x).m_yynum <= m_lexer.getTokens().tokens.Count + 2)
					{
						ret.Add(((ParsingInfo)x).m_name);
					}
					//}
				}
			}
			return ret;
		}
		return null;
	}

	private SYMBOL Parse()
	{
		ParserEntry pe;
		SYMBOL newtop;
		Create();
		ParseStackEntry top = new ParseStackEntry(this, 0, NextSym());
		try
		{
			for (; ;)
			{
				String cnm = top.m_value.getYyname();
				if (m_debug)
				{
					if (cnm.equals("TOKEN"))
					{
						System.out.println(String.format("State %1$s with %2$s \"%3$s\"", top.m_state, cnm, ((TOKEN)top.m_value).getYytext()));
					}
					else
					{
						System.out.println(String.format("State %1$s with %2$s", top.m_state, cnm));
					}
				}
				tangible.OutObject<tools.ParserEntry> tempOut_pe = new tangible.OutObject<tools.ParserEntry>();
				if (top.m_value != null && top.m_value.Pass(m_symbols, top.m_state, tempOut_pe))
				{
				pe = tempOut_pe.argValue;
					tangible.RefObject<tools.ParseStackEntry> tempRef_top = new tangible.RefObject<tools.ParseStackEntry>(top);
					pe.Pass(tempRef_top);
				top = tempRef_top.argValue;
				}
				else
				{
				pe = tempOut_pe.argValue;
					if (top.m_value == m_symbols.EOFSymbol)
					{
						if (top.m_state == m_symbols.m_accept.m_state)
						{ // successful parse
							tangible.RefObject<tools.ParseStackEntry> tempRef_top2 = new tangible.RefObject<tools.ParseStackEntry>(top);
							Pop(tempRef_top2, 1, m_symbols.m_startSymbol);
						top = tempRef_top2.argValue;
    
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
	//#if !RUNNINGPARSER
								if (m_symbols.erh.counter > 0)
								{
									return new recoveredError(this, top);
								}
	//#endif
							newtop = top.m_value; // extract the return value
							top.m_value = null;
							return newtop;
						}
						tangible.RefObject<tools.ParseStackEntry> tempRef_top3 = new tangible.RefObject<tools.ParseStackEntry>(top);
						if (!Error(tempRef_top3, "Unexpected EOF")) // unrecovered error
						{
						top = tempRef_top3.argValue;
							return top.m_value;
						}
					else
					{
						top = tempRef_top3.argValue;
					}
					}
					else
					{
						tangible.RefObject<tools.ParseStackEntry> tempRef_top4 = new tangible.RefObject<tools.ParseStackEntry>(top);
						if (!Error(tempRef_top4, "syntax error")) // unrecovered error
						{
						top = tempRef_top4.argValue;
							return top.m_value;
						}
					else
					{
						top = tempRef_top4.argValue;
					}
					}
				}
				if (m_debug)
				{
					Object ob = null;
					if (top.m_value != null)
					{
						ob = top.m_value.m_dollar;
						System.out.printf("In state %1$s top %2$s value %3$s" + "\r\n", top.m_state, top.m_value.getYyname(), (ob == null) ? "null" : ob.getClass().getSimpleName());
						if (ob != null && ob.getClass().getSimpleName().equals("Int32"))
						{
							System.out.println((Integer)ob);
						}
						else
						{
							((SYMBOL)(top.m_value)).Print();
						}
					}
					else
					{
						System.out.printf("In state %1$s top NULL" + "\r\n", top.m_state);
					}
				}
			}
			// not reached
		}
		catch (CSToolsStopException ex) // stop parsing
		{
			if (m_symbols.erh.throwExceptions)
			{
				throw ex; // 4.5b
			}
			m_symbols.erh.Report(ex); // 4.5b
		}
		return null;
	}
	public final void Push(ParseStackEntry elt)
	{
		m_stack.Push(elt);
	}
	public final void Pop(tangible.RefObject<ParseStackEntry> elt, int depth, SYMBOL ns)
	{
		for (; m_stack.getCount() > 0 && depth > 0; depth--)
		{
			elt.argValue = (ParseStackEntry)m_stack.Pop();
			if (m_symbols.m_concrete) // building the concrete syntax tree
			{
				ns.kids.Push(elt.argValue.m_value); // else will be garbage collected
			}
		}
		if (depth != 0)
		{
			m_symbols.erh.Error(new CSToolsException(14, m_lexer, "Pop failed"));
		}
	}
	public final ParseStackEntry StackAt(int ix)
	{
		int n = m_stack.getCount();
		if (m_stkdebug)
		{
			System.out.printf("StackAt(%1$s),count %2$s" + "\r\n", ix, n);
		}
		ParseStackEntry pe = (ParseStackEntry)m_stack.get(ix);
		if (pe == null)
		{
			return new ParseStackEntry(this, 0, m_symbols.Special);
		}
		if (pe.m_value instanceof Null)
		{
			return new ParseStackEntry(this, pe.m_state, null);
		}
		if (m_stkdebug)
		{
			System.out.println(pe.m_value.getYyname());
		}
		return pe;
	}
	public final SYMBOL NextSym()
	{ // like lexer.Next but allows a one-token pushback for reduce
		SYMBOL ret = m_ungot;
		if (ret != null)
		{
			m_ungot = null;
			return ret;
		}
		ret = (SYMBOL)m_lexer.Next();
		if (ret == null)
		{
			ret = m_symbols.EOFSymbol;
		}
		return ret;
	}
	public final void Error(int n, SYMBOL sym, String s)
	{
		if (sym != null)
		{
			m_symbols.erh.Error(new CSToolsException(n, sym.yylx, sym.pos, "", s)); // 4.5b
		}
		else
		{
			m_symbols.erh.Error(new CSToolsException(n, s));
		}
	}
}