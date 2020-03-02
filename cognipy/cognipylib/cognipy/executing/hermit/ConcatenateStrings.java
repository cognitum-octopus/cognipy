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

public class ConcatenateStrings extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "concatenateStrings";
	}

	@Override
	public int getArgLength()
	{
		return 0;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		if (length < 1)
		{
			throw new BuiltinException(this, context, "Must have at least 1 argument to " + getName());
		}
		org.apache.jena.graph.Node n0 = getArg(0, args, context);

		StringBuilder sb = new StringBuilder();
		for (int i = 1; i < length; i++)
		{
			sb.append(RuleExtensions.lex(getArg(i, args, context), this, context));
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