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

public class BooleanUnary extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "booleanUnary";
	}

	@Override
	public int getArgLength()
	{
		return 3;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);

		String kind = getArg(0, args, context).getLiteral().toString();

		org.apache.jena.graph.Node n0 = getArg(1, args, context);
		if (kind.equals("not"))
		{
			org.apache.jena.graph.Node n1 = getArg(2, args, context);

			Object p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
			if (!(p1 instanceof Boolean))
			{
				return false;
			}

			if (n0.isLiteral())
			{
				Object p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!(p0 instanceof Boolean))
				{
					return false;
				}

				return (Boolean)p1 == !(Boolean)p0;
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else
		{
			throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
		}
	}
}