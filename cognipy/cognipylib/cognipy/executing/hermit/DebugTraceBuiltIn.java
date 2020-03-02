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

public class DebugTraceBuiltIn extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "debugTraceBuiltIn";
	}

	@Override
	public int getArgLength()
	{
		return 1;
	}

	@Override
	public void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);

		checkArgs(length, context);
		org.apache.jena.graph.Node n0 = getArg(0, args, context);
		String str = n0.getLiteralValue().toString();

		HashMap<Integer, String> indexToVarName = new HashMap<Integer, String>();
		int bodyL = context.getRule().bodyLength();
		for (int i = 0; i < bodyL; i++)
		{
			ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable> rV = new ArrayList<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
			Object tempVar = context.getRule().getBodyElement(i);
			org.apache.jena.reasoner.TriplePattern elem = tempVar instanceof org.apache.jena.reasoner.TriplePattern ? (org.apache.jena.reasoner.TriplePattern)tempVar : null;
			if (elem != null)
			{
				Object tempVar2 = elem.getObject();
				org.apache.jena.reasoner.rulesys.Node_RuleVariable o = tempVar2 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar2 : null;
				Object tempVar3 = elem.getSubject();
				org.apache.jena.reasoner.rulesys.Node_RuleVariable s = tempVar3 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar3 : null;
				Object tempVar4 = elem.getPredicate();
				org.apache.jena.reasoner.rulesys.Node_RuleVariable p = tempVar4 instanceof org.apache.jena.reasoner.rulesys.Node_RuleVariable ? (org.apache.jena.reasoner.rulesys.Node_RuleVariable)tempVar4 : null;
				if (o != null)
				{
					rV.add(o);
				}
				if (s != null)
				{
					rV.add(s);
				}
				if (p != null)
				{
					rV.add(p);
				}
			}
			else
			{
				Object tempVar5 = context.getRule().getBodyElement(i);
				org.apache.jena.reasoner.rulesys.Functor func = tempVar5 instanceof org.apache.jena.reasoner.rulesys.Functor ? (org.apache.jena.reasoner.rulesys.Functor)tempVar5 : null;
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

		HashMap<String, Tuple<String, Object>> vals = new HashMap<String, Tuple<String, Object>>();

		Object tempVar6 = context.getEnv();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var env = (tempVar6 instanceof org.apache.jena.reasoner.rulesys.impl.BindingVector ? (org.apache.jena.reasoner.rulesys.impl.BindingVector)tempVar6 : null).getEnvironment();
		for (int i = 0; i < env.Length; i++)
		{
			if (indexToVarName.containsKey(i))
			{
				if (!vals.containsKey(indexToVarName.get(i)))
				{
					if (env[i].isURI())
					{
						org.apache.jena.reasoner.InfGraph G = context.getGraph();
						cognipy.ars.EntityKind ek = ARS.EntityKind.Concept;
						if (G.contains(env[i], org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode()))
						{
							ek = ARS.EntityKind.Instance;
						}
						else if (G.contains(env[i], org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode()))
						{
							ek = ARS.EntityKind.Role;
						}
						else if (G.contains(env[i], org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode()))
						{
							ek = ARS.EntityKind.DataRole;
						}

						vals.put(indexToVarName.get(i), Tuple.<String, Object>Create(ek.toString(), ext.TheInvTransform.renderEntity(env[i].getURI(), ek)));
					}
					else if (env[i].isLiteral())
					{
						vals.put(indexToVarName.get(i), Tuple.<String, Object>Create(null, JenaRuleManager.getObject(env[i])));
					}
					else //unnamed entity detected
					{
						return;
					}
				}
			}
		}

		if (ext.DebugAction != null)
		{
			ext.DebugAction.invoke(str, vals);
		}
	}
}