package tools;

import java.util.*;
import java.io.*;

public class recoveredError extends error
{
	public recoveredError(Parser yyp, ParseStackEntry s)
	{
		super(yyp, s);
	}
	@Override
	public String toString()
	{
		return "Parse contained " + yyps.m_symbols.erh.counter + " errors";
	}
	@Override
	public void ConcreteSyntaxTree()
	{
		System.out.println(ToString());
		if (sym != null)
		{
			sym.ConcreteSyntaxTree();
		}
	}
	@Override
	public void Print()
	{
		System.out.println(ToString());
		if (sym != null)
		{
			sym.Print();
		}
	}
}