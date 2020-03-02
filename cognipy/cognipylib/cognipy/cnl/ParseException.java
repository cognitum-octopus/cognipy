package cognipy.cnl;

import tools.*;
import cognipy.*;
import java.util.*;
import java.nio.file.*;

public class ParseException extends RuntimeException
{
	private int Line;
	public final int getLine()
	{
		return Line;
	}
	private void setLine(int value)
	{
		Line = value;
	}

	private int Column;
	public final int getColumn()
	{
		return Column;
	}
	private void setColumn(int value)
	{
		Column = value;
	}

	private int Pos;
	public final int getPos()
	{
		return Pos;
	}
	private void setPos(int value)
	{
		Pos = value;
	}

	private String Context;
	public final String getContext()
	{
		return Context;
	}
	private void setContext(String value)
	{
		Context = value;
	}

	public ParseException(String message, int line, int column, int pos, String context)
	{
		super(message);
		this.setContext(context);
		this.setLine(line);
		this.setColumn(column);
		this.setPos(pos - 1);
		if (this.getPos() < 0)
		{
			this.setPos(0);
		}
		if (this.getPos() > context.length() - 1)
		{
			this.setPos(context.length() - 1);
		}
	}

	public final String getHint()
	{
		try
		{
			int delta = 100;
			int min = getPos() - delta > 0 ? getPos() - delta : 0;
			int max = getPos() + delta < getContext().length() ? getPos() + delta : getContext().length() - 1;
			StringBuilder sb = new StringBuilder();
			if (getPos() - min + 1 > 0)
			{
				sb.append(getContext().substring(min, getPos() + 1));
			}
			sb.append("^");
			if (getPos() + 1 < getContext().length() - 1 && max - getPos() - 1 > 0)
			{
				sb.append(tangible.StringHelper.substring(getContext(), getPos() + 1, max - getPos() - 1));
			}
			return sb.toString();
		}
		catch (java.lang.Exception e)
		{
			return "fatal error while generating the hint";
		}
	}
}