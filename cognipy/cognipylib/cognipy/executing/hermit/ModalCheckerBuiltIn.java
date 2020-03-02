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

public class ModalCheckerBuiltIn extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "modalCheckerBuiltIn";
	}

	@Override
	public int getArgLength()
	{
		return 3;
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		checkArgs(length, context);
		Object tempVar = getArg(0, args, context).getLiteralValue();
		int tpy = (tempVar instanceof java.lang.Integer ? (java.lang.Integer)tempVar : null).intValue();
		Object tempVar2 = getArg(1, args, context).getLiteralValue();
		boolean normal = (tempVar2 instanceof java.lang.Integer ? (java.lang.Integer)tempVar2 : null).intValue() == 0;
		String str = getArg(2, args, context).getLiteralValue().toString();

		HashMap<Integer, String> indexToVarName = new HashMap<Integer, String>();
		int bodyL = context.getRule().bodyLength();
		for (int i = 0; i < bodyL; i++)
		{
			ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable> rV = new ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
			Object tempVar3 = context.getRule().getBodyElement(i);
			org.apache.jena.reasoner.TriplePattern elem = tempVar3 instanceof org.apache.jena.reasoner.TriplePattern ? (org.apache.jena.reasoner.TriplePattern)tempVar3 : null;
			if (elem != null)
			{
				Object tempVar4 = elem.getObject();
				org.apache.jena.reasoner.rulesys.Node_RuleVariable o = tempVar4 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar4 : null;
				Object tempVar5 = elem.getSubject();
				org.apache.jena.reasoner.rulesys.Node_RuleVariable s = tempVar5 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar5 : null;
				if (o != null)
				{
					rV.add(o);
				}
				if (s != null)
				{
					rV.add(s);
				}
			}
			else
			{
				Object tempVar6 = context.getRule().getBodyElement(i);
				org.apache.jena.reasoner.rulesys.Functor func = tempVar6 instanceof org.apache.jena.reasoner.rulesys.Functor ? (org.apache.jena.reasoner.rulesys.Functor)tempVar6 : null;
				if (func != null)
				{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var argsX = func.getArgs();
					for (int j = 0; j < func.getArgLength(); j++)
					{
						org.apache.jena.reasoner.rulesys.Node_RuleVariable o = argsX[j] instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)argsX[j] : null;
						if (o != null)
						{
							rV.add(o);
						}
					}
				}
			}
			for (org.apache.jena.reasoner.rulesys.Node_RuleVariable r : rV)
			{
				if (!indexToVarName.containsKey(r.getIndex()))
				{
					indexToVarName.put(r.getIndex(), r.getName().replace("_", "-"));
				}
			}
		}


		LinkedDictionary<String, JenaValue> vals = new LinkedDictionary<String, JenaValue>();

		Object tempVar7 = context.getEnv();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var env = (tempVar7 instanceof org.apache.jena.reasoner.rulesys.impl.BindingVector ? (org.apache.jena.reasoner.rulesys.impl.BindingVector)tempVar7 : null).getEnvironment();
		for (int i = 0; i < env.Length; i++)
		{
			if (indexToVarName.containsKey(i))
			{
				if (!vals.containsKey(indexToVarName.get(i)))
				{
					if (env[i].isURI())
					{
						JenaValue tempVar8 = new JenaValue();
						tempVar8.IsInstance = true;
						tempVar8.Value = ext.TheAccessObject.CnlFromUri(env[i].getURI(), "instance");
						vals.put(indexToVarName.get(i), tempVar8);
					}
					else if (env[i].isLiteral())
					{
						JenaValue tempVar9 = new JenaValue();
						tempVar9.IsInstance = false;
						tempVar9.Value = RuleExtensions.getValFromJenaLiteral(env[i].getLiteralValue());
						vals.put(indexToVarName.get(i), tempVar9);
					}
					else
					{
						ext.SetModalVals(tpy == 0, normal, ext.TheAccessObject.CnlFromDLString(str), new LinkedDictionary<String, JenaValue>());
						return;
					}
				}
			}
		}

		ext.SetModalVals(tpy == 0, normal, ext.TheAccessObject.CnlFromDLString(str), vals);

	}

}