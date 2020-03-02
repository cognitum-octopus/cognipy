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

public class ReasonerExt
{
	public HashMap<String, Regex> MatchedRegexes = new HashMap<String, Regex>();
	public HashMap<Integer, Tuple<String, ArrayList<IExeVar>>> ExeRules;
	public HashMap<Integer, SwrlIterate> SwrlIterators;
	public tangible.Action2Param<String, HashMap<String, Tuple<String, Object>>> DebugAction;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public dynamic TheAccessObject;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public dynamic Outer;
	public cognipy.ars.InvTransform TheInvTransform;
	public SwrlIterateProc TheSwrlIterateProc;

	private java.util.concurrent.ConcurrentHashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> bodies = new java.util.concurrent.ConcurrentHashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>>();
	private HashSet<String> normals = new HashSet<String>();
	private java.util.concurrent.ConcurrentHashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> heads = new java.util.concurrent.ConcurrentHashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>>();
	private ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>> ontologyErrors = new ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>>();

	public final String GetTypeOfNode(RuleContext context, org.apache.jena.graph.Node n)
	{
		if (TheAccessObject.SWRLOnly)
		{
			return "instance";
		}
		else
		{
			org.apache.jena.reasoner.InfGraph infgraph = context.getGraph();

			boolean isInstance = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			boolean isRole = false;
			boolean isDataRole = false;
			if (!isInstance)
			{
				isRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode());
			}
			if (!isInstance && !isRole)
			{
				isDataRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());
			}

			boolean isConcept = !isInstance && !isRole && !isDataRole;

			return isInstance ? "instance" : (isConcept ? "concept" : (isRole ? "role" : "datarole"));
		}
	}

	public final void AddOntologyError(String title, String content, ArrayList<Tuple<Object, String>> vals)
	{
		ontologyErrors.add(Tuple.Create(title, content, vals));
	}

	public final void SetModalVals(boolean isBody, boolean isNormal, String str, LinkedDictionary<String, JenaValue> vals)
	{
		java.util.concurrent.ConcurrentHashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> dic = (isBody ? bodies : heads);
		if (!dic.containsKey(str))
		{
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			dic.TryAdd(str, new ArrayList<LinkedDictionary<String, JenaValue>>());
		}

		dic[str].Add(vals);
		if (isNormal)
		{
			normals.add(str);
		}
	}

	private boolean AIsB(LinkedDictionary<String, JenaValue> A, LinkedDictionary<String, JenaValue> B)
	{
		for (JenaValue kv : A)
		{
			if (!B.containsKey(kv.Key))
			{
				return false;
			}
			if (!kv.Value.Value.equals(B.get(kv.Key).Value))
			{
				return false;
			}
		}
		return true;
	}

	public final ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>> GetOntologyErrors()
	{
		return ontologyErrors;
	}

	public final HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> Validate()
	{
		HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> res = new HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>>();
		for (ArrayList<LinkedDictionary<String, JenaValue>> b : bodies)
		{
			if (normals.contains(b.Key))
			{
				if (!heads.containsKey(b.Key))
				{
					if (!res.containsKey(b.Key))
					{
						res.put(b.Key, new ArrayList<LinkedDictionary<String, JenaValue>>());
					}
					res.get(b.Key).addAll(b.Value);
				}
				else
				{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					for (var body : b.Value)
					{
						boolean found = false;
						for (LinkedDictionary<String, JenaValue> head : heads.get(b.Key))
						{
							if (AIsB(body, head))
							{
								found = true;
								break;
							}
						}
						if (!found)
						{
							if (!res.containsKey(b.Key))
							{
								res.put(b.Key, new ArrayList<LinkedDictionary<String, JenaValue>>());
							}
							res.get(b.Key).add(body);
						}
					}
				}
			}
			else
			{
				if (heads.containsKey(b.Key))
				{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					for (var body : b.Value)
					{
						boolean found = false;
						for (LinkedDictionary<String, JenaValue> head : heads.get(b.Key))
						{
							if (AIsB(body, head))
							{
								found = true;
								break;
							}
						}
						if (found)
						{
							if (!res.containsKey(b.Key))
							{
								res.put(b.Key, new ArrayList<LinkedDictionary<String, JenaValue>>());
							}
							res.get(b.Key).add(body);
						}
					}
				}
			}
		}
		return res;
	}

}