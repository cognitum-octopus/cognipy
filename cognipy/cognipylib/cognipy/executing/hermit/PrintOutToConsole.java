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

public class PrintOutToConsole extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "printOutToConsole";
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		print(args, length, context);
		return true;
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		print(args, length, context);
	}

	public final void print(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		for (int i = 0; i < length; i++)
		{
			System.out.print(PrintUtil.print(this.getArg(i, args, context)) + " ");
		}
		System.out.println();
	}
}