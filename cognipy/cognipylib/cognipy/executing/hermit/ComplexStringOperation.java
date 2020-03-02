package cognipy.executing.hermit;

import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.*;
import cognipy.*;
import java.util.*;
import java.io.*;

// SWRL BuILTINS

public class ComplexStringOperation extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "complexStringOperation";
	}

	@Override
	public int getArgLength()
	{
		return 5;
	}

	public static String replace(String str, String pattern, String replacement)
	{
		if (tangible.StringHelper.isNullOrEmpty(str))
		{
			return str;
		}
		System.Text.RegularExpressions.Regex rgx = new System.Text.RegularExpressions.Regex(pattern);
		return rgx.Replace(str, replacement);
	}

	public static String translate(String str, String searchChars, String replaceChars)
	{
		if (tangible.StringHelper.isNullOrEmpty(str))
		{
			return str;
		}
		StringBuilder buffer = new StringBuilder(str.length());
		char[] chrs = str.toCharArray();
		char[] withChrs = replaceChars.toCharArray();
		int sz = chrs.length;
		int withMax = replaceChars.length() - 1;
		for (int i = 0; i < sz; i++)
		{
			int idx = searchChars.indexOf(chrs[i]);
			if (idx != -1)
			{
				if (idx <= withMax)
				{
					buffer.append(withChrs[idx]);
				}
			}
			else
			{
				buffer.append(chrs[i]);
			}
		}
		return buffer.toString();
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);

		String kind = getArg(0, args, context).getLiteral().toString();

		org.apache.jena.graph.Node n0 = getArg(1, args, context);
		String n1 = RuleExtensions.lex(getArg(3, args, context), this, context);
		String n2 = RuleExtensions.lex(getArg(2, args, context), this, context);
		String n3 = RuleExtensions.lex(getArg(4, args, context), this, context);

		String sb = "";

		if (kind.equals("translate"))
		{
			sb = translate(n1, n2, n3);
		}
		else if (kind.equals("replace"))
		{
			sb = replace(n1, n2, n3);
		}
		else
		{
			throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
		}

		if (n0.isLiteral())
		{
			return RuleExtensions.lex(n0, this, context).compareTo(sb.toString()) == 0;
		}
		else
		{
			org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb.toString());
			return context.getEnv().bind(n0, result);
		}


	}
}