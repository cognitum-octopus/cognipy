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

public class ListTestSubjectPairewise extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "listTestSubjectPairewise";
	}

	@Override
	public int getArgLength()
	{
		return 2;
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
		checkArgs(length, context);
		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		org.apache.jena.graph.Node n1 = getArg(1, args, context);
		List l = Util.convertList(n1, context);
		Iterator liter = l.iterator();
		boolean allOk = true;
		while (liter.hasNext())
		{
			Object tempVar = liter.next();
			org.apache.jena.graph.Node x = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;
			org.apache.jena.graph.Node a = Util.getPropValue(n0, x, context);
			org.apache.jena.graph.Node b = Util.getPropValue(n1, x, context);
			if (a == null || b == null || !a.equals(b))
			{
				allOk = false;
				break;
			}
		}
		return allOk;
	}

}