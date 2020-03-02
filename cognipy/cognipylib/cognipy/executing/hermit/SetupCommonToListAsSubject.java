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

public class SetupCommonToListAsSubject extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "setupCommonToListAsSubject";
	}

	@Override
	public int getArgLength()
	{
		return 3;
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
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);
		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		org.apache.jena.graph.Node n1 = getArg(2, args, context);
		org.apache.jena.graph.Node n2 = getArg(1, args, context);
		List l = Util.convertList(n0, context);

		org.apache.jena.reasoner.InfGraph infgraph = context.getGraph();
		Iterator liter = l.iterator();
		HashMap<String, org.apache.jena.graph.Node> nodes = new HashMap<String, org.apache.jena.graph.Node>();
		HashSet<String> commons = new HashSet<String>();
		commons.add(n2.toString());
		nodes.put(n2.toString(), n2);
		liter.next();
		while (liter.hasNext())
		{
			Object tempVar = liter.next();
			org.apache.jena.graph.Node x = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;
			org.apache.jena.util.iterator.ExtendedIterator ci = infgraph.find(null, org.apache.jena.vocabulary.RDF.Nodes.type, x);
			HashSet<String> hs = new HashSet<String>();
			while (ci.hasNext())
			{
				Object tempVar2 = ci.next();
				org.apache.jena.graph.Triple trip = tempVar2 instanceof org.apache.jena.graph.Triple ? (org.apache.jena.graph.Triple)tempVar2 : null;
				org.apache.jena.graph.Node nod = trip.getSubject();
				hs.add(nod.toString());
			}

			commons.IntersectWith(hs);
			ci.close();
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var c : commons)
		{
			Triple t = new Triple(nodes.get(c), org.apache.jena.vocabulary.RDF.Nodes.type, n1);
			if (!context.contains(t))
			{

				context.add(t);
				ForwardRuleInfGraphI infGraph = (ForwardRuleInfGraphI)context.getGraph();
				if (infGraph.shouldLogDerivations())
				{
					org.apache.jena.reasoner.rulesys.Rule rule = context.getRule();
					List matchList = null;
					// Create derivation record
					matchList = new ArrayList(rule.bodyLength());
					for (int i = 0; i < rule.bodyLength(); i++)
					{
						Object clause = rule.getBodyElement(i);
						if (clause instanceof org.apache.jena.reasoner.TriplePattern)
						{
							org.apache.jena.graph.Triple trp = context.getEnv().instantiate((org.apache.jena.reasoner.TriplePattern)clause);
							matchList.add(trp);
						}
					}
					infGraph.logDerivation(t, new RuleDerivation(context.getRule(), t, matchList, infGraph));
				}
			}
		}
	}

}