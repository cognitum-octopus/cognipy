package tools;

import java.util.*;
import java.io.*;

public class CSToolsFatalException extends CSToolsException
{
	public CSToolsFatalException(int n, String s)
	{
		super(n, s);
	}
	public CSToolsFatalException(int n, Lexer yl, String s)
	{
		super(n, yl, yl.yytext, s);
	}
	public CSToolsFatalException(int n, Lexer yl, String yy, String s)
	{
		super(n, yl, yl.m_pch, yy, s);
	}
	public CSToolsFatalException(int n, Lexer yl, int p, String y, String s)
	{
		super(n, yl, p, y, s);
	}
	public CSToolsFatalException(int n, TOKEN t, String s)
	{
		super(n, t, s);
	}
	public CSToolsFatalException(int en, SourceLineInfo s, String y, String m)
	{
		super(en, s, y, m);
	}
	@Override
	public void Handle(ErrorHandler erh)
	{
		throw this; // we expect to bomb out to the environment with CLR traceback
	}
}