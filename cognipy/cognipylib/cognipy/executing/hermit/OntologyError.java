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

public class OntologyError extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "ontologyError";
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		String title = getArg(0, args, context).getLiteralValue().toString();
		String message = getArg(1, args, context).getLiteralValue().toString();

		org.apache.jena.reasoner.InfGraph infgraph = context.getGraph();

		ArrayList<Tuple<Object, String>> vals = new ArrayList<Tuple<Object, String>>();
		for (int i = 2; i < args.length; i++)
		{
			org.apache.jena.graph.Node n = args[i];
			if (n.isLiteral())
			{
				vals.add(Tuple.<Object, String>Create(JenaRuleManager.getObject(n), "value"));
			}
			else if (n.isURI())
			{
				String type = ext.GetTypeOfNode(context, n);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var name = ext.TheAccessObject.CnlFromUri(n.getURI(), type);
				vals.add(Tuple.<Object, String>Create(name, type));
			}
		}
		ext.AddOntologyError(title, message, vals);
	}
}