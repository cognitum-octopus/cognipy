package tools;

import java.util.*;
import java.io.*;

public class Sfactory
{
	public static Object create(String cls_name, Parser yyp)
	{
		SCreator cr = (SCreator)yyp.m_symbols.types.get(cls_name);
		// Console.WriteLine("TCreating {0} <{1}>",cls_name,yyl.yytext);
		if (cr == null)
		{
			yyp.m_symbols.erh.Error(new CSToolsException(16, yyp.m_lexer, "no factory for {" + cls_name + ")"));
		}
		try
		{
			return cr.invoke(yyp);
		}
		catch (CSToolsException e)
		{
			yyp.m_symbols.erh.Error(e);
		}
		catch (RuntimeException e)
		{
			yyp.m_symbols.erh.Error(new CSToolsException(17, yyp.m_lexer, String.format("Create of %1$s failed (%2$s)", cls_name, e.getMessage())));
		}
		int j = cls_name.lastIndexOf('_');
		if (j > 0)
		{
			cr = (SCreator)yyp.m_symbols.types.get(cls_name.substring(0, j));
			if (cr != null)
			{
				SYMBOL s = (SYMBOL)cr.invoke(yyp);
				s.m_dollar = 0;
				return s;
			}
		}
		return null;
	}
	public Sfactory(YyParser syms, String cls_name, SCreator cr)
	{
		syms.types.put(cls_name, cr);
	}
}