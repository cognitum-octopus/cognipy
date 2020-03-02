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

public class ReactiveCSharpRule extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "reactiveRule";
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		Object tempVar = this.getArg(0, args, context).getLiteralValue();
		int ruleIdx = (tempVar instanceof java.lang.Integer ? (java.lang.Integer)tempVar : null).intValue();
		Tuple<String, ArrayList<IExeVar>> rule = JenaRuleManager.GetReasonerExt(context).ExeRules.get(ruleIdx);
		java.lang.Class ruleType = owlservices.ReactiveRuleCompiler.LoadRuleType(rule.Item1.substring(2, 2 + rule.Item1.length() - 4), "", rule.Item2);

		HashMap<String, Integer> varNameToIndex = new HashMap<String, Integer>();
		int bodyL = context.getRule().bodyLength();
		for (int i = 0; i < bodyL; i++)
		{
			Object tempVar2 = context.getRule().getBodyElement(i);
			org.apache.jena.reasoner.TriplePattern elem = tempVar2 instanceof org.apache.jena.reasoner.TriplePattern ? (org.apache.jena.reasoner.TriplePattern)tempVar2 : null;
			Object tempVar3 = elem.getObject();
			org.apache.jena.reasoner.rulesys.Node_RuleVariable o = tempVar3 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar3 : null;
			Object tempVar4 = elem.getSubject();
			org.apache.jena.reasoner.rulesys.Node_RuleVariable s = tempVar4 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar4 : null;
			ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable> rV = new ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
			if (o != null)
			{
				rV.add(o);
			}
			if (s != null)
			{
				rV.add(s);
			}
			for (org.apache.jena.reasoner.rulesys.Node_RuleVariable r : rV)
			{
				if (!varNameToIndex.containsKey(r.getName()))
				{
					varNameToIndex.put(r.getName(), r.getIndex());
				}
			}
		}

		HashMap<Integer, Integer> thosToThat = new HashMap<Integer, Integer>();
		ArrayList<IExeVar> ruleVars = rule.Item2;
		int idx = 0;
		for (IExeVar v : ruleVars)
		{
			if (v.isVar())
			{
				String vn = "?" + (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().replace("-", "_");
				thosToThat.put(idx, varNameToIndex.get(vn));
			}
			idx++;
		}

		Object tempVar5 = context.getEnv();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var env = (tempVar5 instanceof org.apache.jena.reasoner.rulesys.impl.BindingVector ? (org.apache.jena.reasoner.rulesys.impl.BindingVector)tempVar5 : null).getEnvironment();
		Object[] vals = new Object[env.Length];
		for (int i = 0; i < env.Length; i++)
		{
			if (env[i].isURI())
			{
				vals[thosToThat.get(i)] = ext.TheInvTransform.renderEntity(env[i].getURI(), ARS.EntityKind.Instance);
			}
			else if (env[i].isLiteral())
			{
				vals[thosToThat.get(i)] = RuleExtensions.getValFromJenaLiteral(env[i].getLiteralValue());
			}
		}


//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
		dynamic ruleInstance = ruleType.newInstance();

		ruleInstance.Ontorion = ext.TheAccessObject;
		ruleInstance.Outer = ext.Outer;

		ruleInstance.Execute(vals, (HashSet<String> obj) ->
		{
					ext.TheAccessObject.KnowledgeInsert(kb);
		}, (HashSet<String> obj) ->
		{
					ext.TheAccessObject.KnowledgeDelete(kb);
				}, null);
	}
}