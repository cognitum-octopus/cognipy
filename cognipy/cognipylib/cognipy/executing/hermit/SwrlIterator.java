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

public class SwrlIterator extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "swrlIterator";
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		Object tempVar = this.getArg(0, args, context).getLiteralValue();
		int ruleIdx = (tempVar instanceof java.lang.Integer ? (java.lang.Integer)tempVar : null).intValue();
		SwrlIterate rule = JenaRuleManager.GetReasonerExt(context).SwrlIterators.get(ruleIdx);


		HashMap<String, Integer> varNameToIndex = new HashMap<String, Integer>();
		int bodyL = context.getRule().bodyLength();
		for (int i = 0; i < bodyL; i++)
		{
			ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable> rV = new ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
			{
				Object tempVar2 = context.getRule().getBodyElement(i);
				org.apache.jena.reasoner.TriplePattern elem = tempVar2 instanceof org.apache.jena.reasoner.TriplePattern ? (org.apache.jena.reasoner.TriplePattern)tempVar2 : null;
				if (elem != null)
				{

					Object tempVar3 = elem.getObject();
					org.apache.jena.reasoner.rulesys.Node_RuleVariable o = tempVar3 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar3 : null;
					Object tempVar4 = elem.getSubject();
					org.apache.jena.reasoner.rulesys.Node_RuleVariable s = tempVar4 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar4 : null;
					if (o != null)
					{
						rV.add(o);
					}
					if (s != null)
					{
						rV.add(s);
					}
				}
			}
			{
				Object tempVar5 = context.getRule().getBodyElement(i);
				org.apache.jena.reasoner.rulesys.Functor elem = tempVar5 instanceof org.apache.jena.reasoner.rulesys.Functor ? (org.apache.jena.reasoner.rulesys.Functor)tempVar5 : null;
				if (elem != null)
				{

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					for (var v : elem.getArgs())
					{
						org.apache.jena.reasoner.rulesys.Node_RuleVariable o = v instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)v : null;
						if (o != null)
						{
							rV.add(o);
						}
					}
				}
			}

			for (org.apache.jena.reasoner.rulesys.Node_RuleVariable r : rV)
			{
				if (!varNameToIndex.containsKey(r.getName()))
				{
					varNameToIndex.put(r.getName(), r.getIndex());
				}
			}
		}

		Object tempVar6 = context.getEnv();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var env = (tempVar6 instanceof org.apache.jena.reasoner.rulesys.impl.BindingVector ? (org.apache.jena.reasoner.rulesys.impl.BindingVector)tempVar6 : null).getEnvironment();
		Object[] vals = new Object[env.Length];
		for (int i = 0; i < env.Length; i++)
		{
			if (env[i].isURI())
			{
				vals[i] = ext.TheInvTransform.renderEntity(env[i].getURI(), ARS.EntityKind.Instance);
			}
			else if (env[i].isLiteral())
			{
				vals[i] = RuleExtensions.getValFromJenaLiteral(env[i].getLiteralValue());
			}
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var iteratorVarP = rule.vars.list.get(0);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var collectionVarP = rule.vars.list.get(rule.vars.list.size() - 1);

		if (collectionVarP.isVar() && iteratorVarP.isVar())
		{
			String cv = "?" + (collectionVarP instanceof ISwrlVar ? (ISwrlVar)collectionVarP : null).getVar().replace("-", "_");
			String iv = "?" + (iteratorVarP instanceof ISwrlVar ? (ISwrlVar)iteratorVarP : null).getVar().replace("-", "_");
			Object collectionVar = vals[varNameToIndex.get(cv)];

			ext.TheSwrlIterateProc.context = context;
			ext.TheSwrlIterateProc.allVars = vals;
			ext.TheSwrlIterateProc.iterVar = iv;
			ext.TheSwrlIterateProc.varNameToIndex = varNameToIndex;

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var it : ext.Outer.ItarateOver(JenaRuleManager.getObject(collectionVar.toString())))
			{
				ext.TheSwrlIterateProc.iterVal = it;
				ext.TheSwrlIterateProc.Visit(rule.slc);
			}
		}
	}
}