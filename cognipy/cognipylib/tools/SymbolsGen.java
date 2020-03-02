package tools;

import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
// Context free Grammar is a quadruple G=<T,N,S,P>
// where T is a finite set of terminal symbols
// [namely, the instances of TOKEN]
// N is a finite set of nonterminal symbols 
// [namely, the instances of SYMBOL for which IsTerminal() is false]
// such that T intersect N = emptyset.
// S [= m_symbols.m_startSymbol] is the start symbol
// P is a finite subset of N x V* where V = T union N
// [i.e. V is the set of instances of SYMBOL]
// and each member (A,w) of P is called a production
// [i.e. an instance p of Production] written A->w .
// A is called the left part [p.m_lhs] 
// and w the right part [the ObjectList p.m_rhs]
public abstract class SymbolsGen extends GenBase
{
	protected SymbolsGen(ErrorHandler eh)
	{
		super(eh);
	}
	public boolean m_lalrParser = true;
	public Lexer m_lexer;
	public YyParser m_symbols = new YyParser();
	// Productions
	public int pno = 0;
	public ObjectList prods = new ObjectList(); // Production
	public int m_trans = 0; // #Transitions
	// support for actions
	public int action = 0;
	public ObjectList actions = new ObjectList(); // ParserAction
	public int action_num = 0; // for old actions
	public SymbolType stypes = null; // the list of grammar symbols
	public int state = 0; // for parsestates
	public SymbolSet lahead = null; // support for lookahead sets
	public final boolean Find(CSymbol sym)
	{
		if (sym.getYytext().equals("Null")) // special case
		{
			return true;
		}
		if (sym.getYytext().charAt(0) == '\'')
		{
			return true;
		}
		if (stypes == null)
		{
			return false;
		}
		return stypes._Find(sym.getYytext()) != null;
	}
	public abstract void ParserDirective();
	public abstract void Declare();
	public abstract void SetNamespace();

	public abstract void SetPartial();

	public abstract void SetName();
	public abstract void SetStartSymbol();
	public abstract void ClassDefinition(String s);
	public abstract void AssocType(Precedence.PrecType pt, int n);
	public abstract void CopySegment();
	public abstract void SimpleAction(ParserSimpleAction a);
	public abstract void OldAction(ParserOldAction a);
}