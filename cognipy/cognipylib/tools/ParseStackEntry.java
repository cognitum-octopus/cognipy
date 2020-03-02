package tools;

import java.util.*;
import java.io.*;

public class ParseStackEntry
{
	public Parser yyps;
	public int m_state;
	public SYMBOL m_value;
	public ParseStackEntry(Parser yyp)
	{
		yyps = yyp;
	}
	public ParseStackEntry(Parser yyp, int state, SYMBOL value)
	{
		yyps = yyp;
		m_state = state;
		m_value = value;
	}
}