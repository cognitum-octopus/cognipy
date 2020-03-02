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

public class ListAllEntriesAre extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "listAllEntriesAre";
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
		org.apache.jena.reasoner.InfGraph infgraph = context.getGraph();
		while (liter.hasNext())
		{
			Object tempVar = liter.next();
			org.apache.jena.graph.Node x = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;

			if (!infgraph.contains(n0, org.apache.jena.vocabulary.RDF.Nodes.type, x))
			{
				allOk = false;
				break;
			}
		}

		return allOk;
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		ForwardRuleInfGraphI infGraph = (ForwardRuleInfGraphI)context.getGraph();
		if (infGraph.shouldLogDerivations())
		{
			org.apache.jena.reasoner.rulesys.Rule rule = context.getRule();
			List matchList = null;
			// Create derivation record
			matchList = new ArrayList(rule.bodyLength());
			org.apache.jena.graph.Node n0 = getArg(0, args, context);
			org.apache.jena.graph.Node n1 = getArg(1, args, context);
			org.apache.jena.graph.Node n2 = getArg(2, args, context);
			List l = Util.convertList(n1, context);
			Iterator liter = l.iterator();
			org.apache.jena.reasoner.InfGraph infgraph = context.getGraph();
			while (liter.hasNext())
			{
				Object tempVar = liter.next();
				org.apache.jena.graph.Node x = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;

				if (infgraph.contains(n0, org.apache.jena.vocabulary.RDF.Nodes.type, x))
				{
					matchList.add(new Triple(n0, org.apache.jena.vocabulary.RDF.Nodes.type, x));
				}
				else
				{
					return;
				}
			}
			Triple t = new Triple(n0, org.apache.jena.vocabulary.RDF.Nodes.type, n2);
			infGraph.logDerivation(t, new RuleDerivation(context.getRule(), t, matchList, infGraph));
		}
	}
}