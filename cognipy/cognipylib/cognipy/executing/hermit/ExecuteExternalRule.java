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

public class ExecuteExternalRule extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "executeExternalRule";
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		String method = getArg(0, args, context).getLiteralValue().toString();
		ArrayList<Object> vals = new ArrayList<Object>();
		//vals.Add(ext.TheAccessObject);
		for (int i = 1; i < args.length; i++)
		{
			org.apache.jena.graph.Node n = getArg(i, args, context);
			if (n.isLiteral())
			{
				vals.add(JenaRuleManager.getObject(n));
			}
			else if (n.isURI())
			{
				String type = ext.GetTypeOfNode(context, n);
				if (ext.TheAccessObject.PassParamsInCNL)
				{
					cognipy.GraphEntity tempVar = new cognipy.GraphEntity();
					tempVar.setName(ext.TheAccessObject.CnlFromUri(n.getURI(), type));
					tempVar.setKind(type);
					vals.add(tempVar.clone());
				}
				else
				{
					cognipy.GraphEntity tempVar2 = new cognipy.GraphEntity();
					tempVar2.setName(n.getURI().toString());
					tempVar2.setKind(type);
					vals.add(tempVar2.clone());
				}
			}
			else
			{
				return;
			}
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var mth = ext.Outer.GetMethod(method);
		mth.Invoke(ext.Outer, vals.toArray(new Object[0]));
	}
}