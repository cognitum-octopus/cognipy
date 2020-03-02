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

public class PairwizeDifferentAtleastOnce extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "pairwizeDifferentAtleastOnce";
	}

	@Override
	public boolean isMonotonic()
	{
		return true;
	}
	@Override
	public boolean isSafe()
	{
		return true;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		for (int i = 0; i < args.length; i += 2)
		{
			org.apache.jena.graph.Node n0 = getArg(i, args, context);
			org.apache.jena.graph.Node n1 = getArg(i + 1, args, context);
			if (!n0.equals(n1))
			{
				return true;
			}
		}
		return false;
	}

}