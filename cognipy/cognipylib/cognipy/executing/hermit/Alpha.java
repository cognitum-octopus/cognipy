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

public class Alpha extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "alpha";
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		org.apache.jena.graph.Node n1 = getArg(1, args, context);

		if (n1.isVariable())
		{
			if (!n0.isLiteral())
			{
				return false;
			}

			String lx = RuleExtensions.lex(getArg(0, args, context), this, context);

			String uri = ext.TheAccessObject.UriFromCnl(lx, "instance");

			org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createURI(uri);
			return context.getEnv().bind(n1, result);

		}
		else
		{
			if (!n1.isURI())
			{
				return false;
			}

			String n1type = ext.GetTypeOfNode(context, n1);
			String n1n = ext.TheAccessObject.CnlFromUri(n1.getURI(), n1type);

			if (n0.isLiteral())
			{
				String lx = RuleExtensions.lex(getArg(0, args, context), this, context);
				return n1n.compareTo(lx) == 0;
			}
			else
			{
				org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(n1n);
				return context.getEnv().bind(n0, result);
			}
		}
	}

}