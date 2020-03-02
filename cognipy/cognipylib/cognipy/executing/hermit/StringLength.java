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

public class StringLength extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "stringLength";
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
		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		org.apache.jena.graph.Node n1 = getArg(1, args, context);
		org.apache.jena.graph.Node n2 = getArg(2, args, context);
		Object tempVar = n0.getLiteralValue();
		int len = (tempVar instanceof java.lang.Integer ? (java.lang.Integer)tempVar : null).intValue();
		String kind = n1.getLiteralValue().toString();
		String str = n2.getLiteralValue().toString();
		if (kind.equals("="))
		{
			return str.length() == len;
		}
		else if (kind.equals("≤"))
		{
			return str.length() <= len;
		}
		else if (kind.equals("≥"))
		{
			return str.length() >= len;
		}
		return false;
	}

}