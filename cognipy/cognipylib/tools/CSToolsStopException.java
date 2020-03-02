package tools;

import java.util.*;
import java.io.*;

public class CSToolsStopException extends CSToolsException
{
	public CSToolsStopException(int n, String s)
	{
		super(n, s);
	}
	public CSToolsStopException(int n, Lexer yl, String s)
	{
		super(n, yl, yl.yytext, s);
	}
	public CSToolsStopException(int n, Lexer yl, String yy, String s)
	{
		super(n, yl, yl.m_pch, yy, s);
	}
	public CSToolsStopException(int n, Lexer yl, int p, String y, String s)
	{
		super(n, yl, p, y, s);
	}
	public CSToolsStopException(int n, TOKEN t, String s)
	{
		super(n, t, s);
	}
	public CSToolsStopException(int n, SYMBOL t, String s)
	{
		super(n, t, s);
	}
	public CSToolsStopException(int en, SourceLineInfo s, String y, String m)
	{
		super(en, s, y, m);
	}
	@Override
	public void Handle(ErrorHandler erh)
	{
		// 4.5b
		if (erh.throwExceptions)
		{
			throw this; // we expect Parser.Parse() to catch this but stop the parse
		}
	}
}