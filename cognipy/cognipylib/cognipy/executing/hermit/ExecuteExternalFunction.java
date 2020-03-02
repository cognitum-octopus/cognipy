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

public class ExecuteExternalFunction extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "executeExternalFunction";
	}

	@Override
	public int getArgLength()
	{
		return 0;
	}

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		checkArgs(length, context);
		org.apache.jena.graph.Node result = getArg(0, args, context);
		String method = getArg(1, args, context).getLiteral().toString();
		ArrayList<Object> vals = new ArrayList<Object>();
		//vals.Add(ext.TheAccessObject);
		for (int i = 2; i < args.length; i++)
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
				return false;
			}
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var mth = ext.Outer.GetMethod(method);

		if (result.isLiteral())
		{
			Object resu = JenaRuleManager.getObject(result);
			if (!(resu instanceof Boolean) || (((Boolean)resu) != true))
			{
				vals.add(resu);
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var res = mth.Invoke(ext.Outer, vals.toArray(new Object[0]));
			return (boolean)res;
		}
		else
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var res = mth.Invoke(ext.Outer, vals.toArray(new Object[0]));
			return context.getEnv().bind(result, JenaRuleManager.getLiteral(res));
		}
	}

}