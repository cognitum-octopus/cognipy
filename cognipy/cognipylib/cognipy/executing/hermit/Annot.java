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

public class Annot extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "annotation";
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		org.apache.jena.graph.Node n3 = getArg(0, args, context);
		org.apache.jena.graph.Node n0 = getArg(1, args, context);
		org.apache.jena.graph.Node n1 = getArg(2, args, context);
		org.apache.jena.graph.Node n2 = getArg(3, args, context);

		if (n3.isVariable())
		{
			String n0type = ext.GetTypeOfNode(context, n0);
			String n0n = ext.TheAccessObject.CnlFromUri(n0.getURI(), n0type);

			String prop = RuleExtensions.lex(n1, this, context);
			String lng = RuleExtensions.lex(n2, this, context);

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var annotVal = ext.TheAccessObject.GetAnnotationValue(n0n, prop, lng, null);
			if (annotVal == null)
			{
				return false;
			}
			return context.getEnv().bind(n3, org.apache.jena.graph.NodeFactory.createLiteral(annotVal.toString()));
		}
		else
		{

			String n0type = ext.GetTypeOfNode(context, n0);
			String n0n = ext.TheAccessObject.CnlFromUri(n0.getURI(), n0type);

			String n1type = ext.GetTypeOfNode(context, n1);
			String n1n = ext.TheAccessObject.CnlFromUri(n0.getURI(), n1type);

			String lng = RuleExtensions.lex(n2, this, context);

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var annotVal = ext.TheAccessObject.GetAnnotationValue(n0n, n1, lng, null);

			String lx = RuleExtensions.lex(n3, this, context);
			return annotVal.toString().compareTo(lx) == 0;
		}
	}

}