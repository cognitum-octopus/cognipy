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

public class ListAnyTwoEqualMembers extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "listAnyTwoEqualMembers";
	}

	@Override
	public int getArgLength()
	{
		return 1;
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
		List l = Util.convertList(n0, context);
		Iterator liter = l.iterator();
		int idx = 0;
		while (liter.hasNext())
		{
			Object tempVar = liter.next();
			org.apache.jena.graph.Node x = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;
			Iterator liter2 = l.iterator();
			for (int i = 0; i < idx; i++)
			{
				liter2.next();
			}
			while (liter2.hasNext())
			{
				Object tempVar2 = liter2.next();
				org.apache.jena.graph.Node y = tempVar2 instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar2 : null;
				if (x.equals(y))
				{
					return true;
				}
			}
			idx++;
		}
		return false;
	}

}