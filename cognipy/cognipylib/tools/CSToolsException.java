package tools;

import java.util.*;
import java.io.*;

public class CSToolsException extends RuntimeException
{
	public int nExceptionNumber;
	public SourceLineInfo slInfo;
	public String sInput;
	public SYMBOL sym = null;
	public boolean handled = false;
	public CSToolsException(int n, String s)
	{
		this(n, new SourceLineInfo(0), "", s);
	}
	public CSToolsException(int n, Lexer yl, String s)
	{
		this(n, yl, yl.yytext, s);
	}
	public CSToolsException(int n, Lexer yl, String yy, String s)
	{
		this(n, yl, yl.m_pch, yy, s);
	}
	public CSToolsException(int n, TOKEN t, String s)
	{
		this(n, t.yylx, t.pos, t.getYytext(), s);
	sym = t;
	}
	public CSToolsException(int n, SYMBOL t, String s)
	{
		this(n, t.yylx, t.pos, t.getYyname(), s);
	sym = t;
	}
	public CSToolsException(int en, Lexer yl, int p, String y, String s)
	{
		this(en, yl.sourceLineInfo(p), y, s);
	}
	public CSToolsException(int en, SourceLineInfo s, String y, String m)
	{
		super(s.toString() + ": " + m);
		nExceptionNumber = en;
		slInfo = s;
		sInput = y;
	}
	public void Handle(ErrorHandler erh) // provides the default ErrorHandling implementation
	{
		if (erh.throwExceptions)
		{
			throw this;
		}
		if (handled)
		{
			return;
		}
		handled = true;
		erh.Report(this); // the parse table may allow recovery from this error
	}
}