package tools;

import java.util.*;
import java.io.*;

public class Tfactory
{
	public static Object create(String cls_name, Lexer yyl)
	{
		TCreator cr = (TCreator)yyl.getTokens().types[cls_name];
		// Console.WriteLine("TCreating {0} <{1}>",cls_name,yyl.yytext);
		if (cr == null)
		{
			yyl.getTokens().erh.Error(new CSToolsException(6, yyl, cls_name, String.format("no factory for %1$s", cls_name)));
		}
		try
		{
			return cr.invoke(yyl);
		}
		catch (CSToolsException x)
		{
			yyl.getTokens().erh.Error(x);
		}
		catch (RuntimeException e)
		{
			yyl.getTokens().erh.Error(new CSToolsException(7, yyl, cls_name, String.format("Line %1$s: Create of %2$s failed (%3$s)", yyl.Saypos(yyl.m_pch), cls_name, e.getMessage())));
		}
		int j = cls_name.lastIndexOf('_');
		if (j > 0)
		{
			cr = (TCreator)yyl.getTokens().types[cls_name.substring(0, j)];
			if (cr != null)
			{
				return cr.invoke(yyl);
			}
		}
		return null;
	}
	public Tfactory(YyLexer tks, String cls_name, TCreator cr)
	{
		tks.types.put(cls_name, cr);
	}
}