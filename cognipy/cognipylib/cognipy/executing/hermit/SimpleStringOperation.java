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

public class SimpleStringOperation extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "simpleStringOperation";
	}

	@Override
	public int getArgLength()
	{
		return 0;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		String kind = getArg(0, args, context).getLiteral().toString();

		org.apache.jena.graph.Node n0 = getArg(1, args, context);
		String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
		org.apache.jena.graph.Node n2 = getArg(3, args, context);

		String sb = "";

		if (kind.equals("substring"))
		{
			Object p1 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
			if (!RuleExtensions.isInteger(p1))
			{
				return false;
			}

			if (length < 4)
			{
				sb = n1.substring((Integer)p1);
			}
			else
			{
				org.apache.jena.graph.Node n3 = getArg(4, args, context);
				Object p2 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
				if (!RuleExtensions.isInteger(p2))
				{
					return false;
				}
				sb = tangible.StringHelper.substring(n1, (Integer)p1, (Integer)p2);
			}
		}
		else if (kind.equals("substring-before"))
		{
			String arg = RuleExtensions.lex(n2, this, context);
			int p = n1.indexOf(arg);
			if (p > 0)
			{
				sb = n1.substring(0, p);
			}
		}
		else if (kind.equals("substring-after"))
		{
			String arg = RuleExtensions.lex(n2, this, context);
			int p = n1.indexOf(arg);
			if (p >= 0)
			{
				if (p + arg.length() < n1.length())
				{
					sb = n1.substring(p + arg.length());
				}
			}
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