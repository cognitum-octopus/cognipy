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

public class SumNumbers extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "sumNumbers";
	}

	@Override
	public int getArgLength()
	{
		return 0;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		if (length < 2)
		{
			throw new BuiltinException(this, context, "Must have at least 2 arguments to " + getName());
		}

		boolean allInts = true;

		for (int i = 0; i < length; i++)
		{
			org.apache.jena.graph.Node n = getArg(i, args, context);
			if (!n.isLiteral() && (i == 0 && !n.isVariable()))
			{
				return false;
			}
			if (n.isLiteral())
			{
				Object v = RuleExtensions.getValFromJenaLiteral(n.getLiteralValue());
				if (!RuleExtensions.isInteger(v))
				{
					allInts = false;
					if (!RuleExtensions.isDouble(v))
					{
						return false;
					}
				}
			}
		}

		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		if (allInts)
		{
			long sum = 0;
			for (int i = 1; i < length; i++)
			{
				org.apache.jena.graph.Node nx = getArg(i, args, context);
				Object vx = RuleExtensions.getValFromJenaLiteral(nx.getLiteralValue());
				sum += (Long)vx;

			}
			if (n0.isLiteral())
			{
				return (Long)RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue()) == sum;
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeLongNode(sum));
			}
		}
		else
		{
			double sum = 0.0;
			for (int i = 1; i < length; i++)
			{
				org.apache.jena.graph.Node nx = getArg(i, args, context);
				Object vx = RuleExtensions.getValFromJenaLiteral(nx.getLiteralValue());
				sum += (Double)vx;

			}
			if (n0.isLiteral())
			{
				return (Double)RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue()) == sum;
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode(sum));
			}
		}
	}
}