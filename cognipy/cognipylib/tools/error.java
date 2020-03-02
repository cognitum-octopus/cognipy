package tools;

import java.util.*;
import java.io.*;

public class error extends SYMBOL
{
	public int state = 0;
	public SYMBOL sym = null;
	public error(Parser yyp, ParseStackEntry s)
	{
		super(yyp);
	state = s.m_state;
	sym = s.m_value;
	}
	public error(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public String getYyname()
	{
		return "error";
	}
	@Override
	public String toString()
	{
		String r = "syntax error occurred in state " + state;
		if (sym == null)
		{
			return r;
		}
		if (sym instanceof TOKEN)
		{
			TOKEN t = (TOKEN)sym;
			return r + " on input token " + t.getYytext();
		}
		return r + " on symbol " + sym.getYyname();
	}
}