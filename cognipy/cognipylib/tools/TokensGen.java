package tools;

import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
public abstract class TokensGen extends GenBase
{
	public TokensGen(ErrorHandler eh)
	{
		super(eh);
	}
	protected boolean m_showDfa;
	public YyLexer m_tokens; // the YyLexer class under construction
	// %defines in script
	public Hashtable defines = new Hashtable(); // string->string
	// support for Nfa networks
	private int state = 0;
	public final int NewState()
	{
		return ++state;
	} // for LNodes
	public ObjectList states = new ObjectList(); // Dfa
	public final String FixActions(String str)
	{
		return str.replace("yybegin","yym.yy_begin").replace("yyl","((" + m_outname + ")yym)");
	}
}