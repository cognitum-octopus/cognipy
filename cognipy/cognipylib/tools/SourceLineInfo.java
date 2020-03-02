package tools;

import java.util.*;
import java.io.*;

public class SourceLineInfo
{
	public int lineNumber;
	public int charPosition;
	public int startOfLine;
	public int endOfLine;
	public int rawCharPosition;
	public Lexer lxr = null;
	public SourceLineInfo(int pos) // this constructor is not used in anger
	{
		lineNumber = 1;
		startOfLine = 0;
		endOfLine = rawCharPosition = charPosition = pos;
	}
	public SourceLineInfo(LineManager lm, int pos)
	{
		lineNumber = lm.lines;
		startOfLine = 0;
		endOfLine = lm.end;
		charPosition = pos;
		rawCharPosition = pos;
		for (LineList p = lm.list; p != null; p = p.tail, lineNumber--)
		{
			if (p.head > pos)
			{
				endOfLine = p.head;
			}
			else
			{
				startOfLine = p.head + 1;
				rawCharPosition = p.getpos(pos);
				charPosition = pos - startOfLine + 1;
				break;
			}
		}
	}
	public SourceLineInfo(Lexer lx, int pos)
	{
		this(lx.m_LineManager, pos);
	lxr = lx;
	}
	@Override
	public String toString()
	{
		return "Line " + lineNumber + ", char " + rawCharPosition;
	}
	public final String getSourceLine()
	{
		if (lxr == null)
		{
			return "";
		}
		return lxr.sourceLine(this);
	}
}