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

public class JenaRuleManager
{
	private static Map PfxMap = new HashMap();
	private static String[] aboxRules;
	private static String[] sameAsRules;
	private static String[] moreRules;
	private static String[] tboxRules;

	private static Regex splitter = new Regex("\\[\\s*([a-zA-Z0-9\\-]+)\\s*\\:[^\\]]*\\]", RegexOptions.Compiled.getValue() | RegexOptions.Multiline.getValue());
	private static List loadRules(String[] ruleFiles, Map map, boolean debugModeOn)
	{
		ArrayList rules = new ArrayList();
		for (String f : ruleFiles)
		{
			String ruls = (new InputStreamReader(FindResourceString(f))).ReadToEnd();
			if (debugModeOn)
			{
				ruls = splitter.Replace(ruls, (System.Text.RegularExpressions.Match match) ->
				{
					   String iid = "\'#" + m.Groups[1].Value + "\'";
					   return m.Value.Replace("]", ", debugTraceBuiltIn(" + iid + ")]");
				});
			}
			BufferedReader mem = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(ruls.getBytes(java.nio.charset.StandardCharsets.US_ASCII))));
			List rs = jena.RuleMap.loadRules(mem, map);
			rules.addAll(rs);
		}
		return rules;
	}

//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or output:
	private static Stream FindResourceString(String shortName)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		var name = (from x in System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceNames() where x.endsWith("." + shortName) select x).First();
		return System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceStream(name);
	}

	static
	{
		BuiltinRegistry.theRegistry.register(new PrintOutToConsole());
		BuiltinRegistry.theRegistry.register(new PairwizeDifferentAtleastOnce());
		BuiltinRegistry.theRegistry.register(new SetupCommonToListAsSubject());
		BuiltinRegistry.theRegistry.register(new ListTestSubjectPairewise());
		BuiltinRegistry.theRegistry.register(new ListAllEntriesAre());
		BuiltinRegistry.theRegistry.register(new ListAnyTwoEqualMembers());
		BuiltinRegistry.theRegistry.register(new StringLength());
		BuiltinRegistry.theRegistry.register(new SwrlIterator());
		BuiltinRegistry.theRegistry.register(new DebugTraceBuiltIn());
		BuiltinRegistry.theRegistry.register(new ModalCheckerBuiltIn());
		BuiltinRegistry.theRegistry.register(new OntologyError());

		BuiltinRegistry.theRegistry.register(new ConcatenateStrings());
		BuiltinRegistry.theRegistry.register(new SumNumbers());
		BuiltinRegistry.theRegistry.register(new MultiplyNumbers());
		BuiltinRegistry.theRegistry.register(new ComplexStringOperation());
		BuiltinRegistry.theRegistry.register(new SimpleStringOperation());
		BuiltinRegistry.theRegistry.register(new StringUnary());
		BuiltinRegistry.theRegistry.register(new BooleanUnary());
		BuiltinRegistry.theRegistry.register(new MathUnary());
		BuiltinRegistry.theRegistry.register(new MathBinary());
		BuiltinRegistry.theRegistry.register(new CreateDatetime());
		BuiltinRegistry.theRegistry.register(new CreateDuration());
		BuiltinRegistry.theRegistry.register(new Alpha());
		BuiltinRegistry.theRegistry.register(new Annot());
		BuiltinRegistry.theRegistry.register(new ExecuteExternalRule());
		BuiltinRegistry.theRegistry.register(new ExecuteExternalFunction());


		PfxMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		PfxMap.put("owl", "http://www.w3.org/2002/07/owl#");
		PfxMap.put("xsd", "http://www.w3.org/2001/XMLSchema#");
		PfxMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

		aboxRules = new String[]{"class-axioms.owlrl.jena", "cls.owlrl.jena", "props.owlrl.jena", "class-axioms.owlrl.val.jena", "cls.owlrl.val.jena", "props.owlrl.val.jena"};

		sameAsRules = new String[]{"same-as.owlrl.jena", "same-as.owlrl.val.jena"};

		moreRules = new String[]{"more.owlrl.jena", "more.owlrl.val.jena"};

		tboxRules = new String[]{"datatypes.owlrl.jena", "datatypes.owlrl.val.jena", "schema.owlrl.jena"};

	}

	public static List GetGeneralRules(MatMode mode, boolean extended, boolean sameAs, boolean debugModeOn)
	{
		List rules = new ArrayList();

		if (mode != MatMode.SWRLOnly)
		{
			if (mode != MatMode.Tbox)
			{
				rules.addAll(loadRules(aboxRules, PfxMap, debugModeOn));
			}
			rules.addAll(loadRules(tboxRules, PfxMap, debugModeOn));
			if (extended)
			{
				rules.addAll(loadRules(moreRules, PfxMap, debugModeOn));
			}
			if (sameAs)
			{
				rules.addAll(loadRules(sameAsRules, PfxMap, debugModeOn));
			}
		}
		return rules;
	}

	public static List GetRule(String scr)
	{
		return jena.RuleMap.loadRules(new BufferedReader(new StringReader(scr)), JenaRuleManager.PfxMap);
	}

	private static ConditionalWeakTable<GenericRuleReasoner, ReasonerExt> GenericRuleReasonerExt = new ConditionalWeakTable<GenericRuleReasoner, ReasonerExt>();

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public static GenericRuleReasoner CreateReasoner(List rules, tangible.Action2Param<String, HashMap<String, Tuple<String, Object>>> debugAction, HashMap<Integer, Tuple<String, ArrayList<IExeVar>>> ExeRules, HashMap<Integer, SwrlIterate> SwrlIterators, dynamic accessObject, dynamic outer, cognipy.ars.InvTransform invTransform, SwrlIterateProc sproc)
	{
		org.apache.jena.reasoner.rulesys.GenericRuleReasoner rete_reasoner = new org.apache.jena.reasoner.rulesys.GenericRuleReasoner(rules);
		rete_reasoner.setMode(org.apache.jena.reasoner.rulesys.GenericRuleReasoner.FORWARD_RETE);
		ReasonerExt tempVar = new ReasonerExt();
		tempVar.DebugAction = (String arg1, HashMap<String, Tuple<String, Object>> arg2) -> debugAction.invoke(arg1, arg2);
		tempVar.ExeRules = ExeRules;
		tempVar.SwrlIterators = SwrlIterators;
		tempVar.Outer = outer;
		tempVar.TheAccessObject = accessObject;
		tempVar.TheInvTransform = invTransform;
		tempVar.TheSwrlIterateProc = sproc;
		GenericRuleReasonerExt.Add(rete_reasoner, tempVar);
		return rete_reasoner;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public static GenericRuleReasoner CloneReasoner(GenericRuleReasoner reasoner, dynamic accessObject, dynamic outer)
	{
		org.apache.jena.reasoner.rulesys.GenericRuleReasoner rete_reasoner = new org.apache.jena.reasoner.rulesys.GenericRuleReasoner(reasoner.getRules());
		rete_reasoner.setMode(org.apache.jena.reasoner.rulesys.GenericRuleReasoner.FORWARD_RETE);
		ReasonerExt ret;
		tangible.OutObject<cognipy.executing.hermit.ReasonerExt> tempOut_ret = new tangible.OutObject<cognipy.executing.hermit.ReasonerExt>();
		if (!GenericRuleReasonerExt.TryGetValue(reasoner, tempOut_ret))
		{
		ret = tempOut_ret.argValue;
			return null;
		}
	else
	{
		ret = tempOut_ret.argValue;
	}
		ReasonerExt tempVar = new ReasonerExt();
		tempVar.DebugAction = (String arg1, HashMap<String, Tuple<String, Object>> arg2) -> ret.DebugAction.invoke(arg1, arg2);
		tempVar.ExeRules = ret.ExeRules;
		tempVar.SwrlIterators = ret.SwrlIterators;
		tempVar.Outer = outer;
		tempVar.TheAccessObject = accessObject;
		tempVar.TheInvTransform = ret.TheInvTransform;
		tempVar.TheSwrlIterateProc = ret.TheSwrlIterateProc;
		GenericRuleReasonerExt.Add(rete_reasoner, tempVar);
		return rete_reasoner;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public static GenericRuleReasoner BootstrapReasonerForAboxChanges(GenericRuleReasoner reasonerTo, GenericRuleReasoner reasonerFrom, dynamic accessObject, dynamic outer)
	{
		ReasonerExt ret;
		tangible.OutObject<cognipy.executing.hermit.ReasonerExt> tempOut_ret = new tangible.OutObject<cognipy.executing.hermit.ReasonerExt>();
		if (!GenericRuleReasonerExt.TryGetValue(reasonerFrom, tempOut_ret))
		{
		ret = tempOut_ret.argValue;
			return null;
		}
	else
	{
		ret = tempOut_ret.argValue;
	}
		ReasonerExt tempVar = new ReasonerExt();
		tempVar.DebugAction = (String arg1, HashMap<String, Tuple<String, Object>> arg2) -> ret.DebugAction.invoke(arg1, arg2);
		tempVar.ExeRules = ret.ExeRules;
		tempVar.SwrlIterators = ret.SwrlIterators;
		tempVar.Outer = outer;
		tempVar.TheAccessObject = accessObject;
		tempVar.TheInvTransform = ret.TheInvTransform;
		tempVar.TheSwrlIterateProc = ret.TheSwrlIterateProc;
		GenericRuleReasonerExt.Add(reasonerTo, tempVar);
		return reasonerTo;
	}

	public static ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>> GetOntologyErrors(GenericRuleReasoner reasoner)
	{
		ReasonerExt ret;
		tangible.OutObject<cognipy.executing.hermit.ReasonerExt> tempOut_ret = new tangible.OutObject<cognipy.executing.hermit.ReasonerExt>();
		if (GenericRuleReasonerExt.TryGetValue(reasoner, tempOut_ret))
		{
		ret = tempOut_ret.argValue;
			return ret.GetOntologyErrors();
		}
	else
	{
		ret = tempOut_ret.argValue;
	}
		return null;
	}

	public static HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> GetModalValidationResult(GenericRuleReasoner reasoner)
	{
		ReasonerExt ret;
		tangible.OutObject<cognipy.executing.hermit.ReasonerExt> tempOut_ret = new tangible.OutObject<cognipy.executing.hermit.ReasonerExt>();
		if (GenericRuleReasonerExt.TryGetValue(reasoner, tempOut_ret))
		{
		ret = tempOut_ret.argValue;
			return ret.Validate();
		}
	else
	{
		ret = tempOut_ret.argValue;
	}
		return null;
	}

	public static ReasonerExt GetReasonerExt(RuleContext context)
	{
		Object tempVar = context.getGraph().getReasoner();
		GenericRuleReasoner gr = tempVar instanceof GenericRuleReasoner ? (GenericRuleReasoner)tempVar : null;
		if (gr != null)
		{
			ReasonerExt ret;
			tangible.OutObject<cognipy.executing.hermit.ReasonerExt> tempOut_ret = new tangible.OutObject<cognipy.executing.hermit.ReasonerExt>();
			if (GenericRuleReasonerExt.TryGetValue(gr, tempOut_ret))
			{
			ret = tempOut_ret.argValue;
				return ret;
			}
		else
		{
			ret = tempOut_ret.argValue;
		}
		}
		throw new IllegalStateException();
	}

	public static void Setup()
	{
		Unique = new UniqueId<Object>();
	}

	public static class UniqueId<T>
	{
		private long counter = 0;
		private ConditionalWeakTable<T, Object> ids = new ConditionalWeakTable<T, Object>();
		private java.util.concurrent.ConcurrentHashMap<Long, Object> rew = new java.util.concurrent.ConcurrentHashMap<Long, Object>();

		public final long GetId(T obj)
		{
			tangible.RefObject<Long> tempRef_counter = new tangible.RefObject<Long>(counter);
			long id = (Long)ids.GetValue(obj, _ -> Interlocked.Increment(tempRef_counter));
		counter = tempRef_counter.argValue;
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			rew.TryAdd(id, obj);
			return id;
		}

		public final T GetObject(long id)
		{
			Object wr;
			tangible.OutObject<Object> tempOut_wr = new tangible.OutObject<Object>();
//C# TO JAVA CONVERTER TODO TASK: There is no Java ConcurrentHashMap equivalent to this .NET ConcurrentDictionary method:
			if (rew.TryGetValue(id, tempOut_wr))
			{
			wr = tempOut_wr.argValue;
				return (T)wr;
			}
			else
			{
			wr = tempOut_wr.argValue;
				return null;
			}
		}

	}

	private static UniqueId<Object> Unique = null;

	public static final String DynamicTypeURI = "http://ontorion.com/dynamicType";

	public static Object getObject(String lex)
	{
		String sid = lex.substring(0, lex.length() - 2 - DynamicTypeURI.length());
		long id = Long.parseLong(sid);
		return Unique.GetObject(id);
	}

	public static Object getObject(org.apache.jena.graph.Node n)
	{
		String lex = n.getLiteralLexicalForm();
		if (lex.endsWith("^^" + DynamicTypeURI))
		{
			String sid = lex.substring(0, lex.length() - 2 - DynamicTypeURI.length());
			long id = Long.parseLong(sid);
			return Unique.GetObject(id);
		}
		else
		{
			return RuleExtensions.getValFromJenaLiteral(n.getLiteralValue());
		}
	}

	public static org.apache.jena.graph.Node getLiteral(Object res)
	{
		if (RuleExtensions.isSimpleJenaObject(res))
		{
			return NodeFactory.createLiteral(LiteralLabelFactory.create((Object)res));
		}
		else
		{
			return NodeFactory.createLiteral(String.valueOf(Unique.GetId((Object)res)) + "^^" + DynamicTypeURI);
		}

	}

	public static String registerObject(Object o)
	{
		return String.valueOf(Unique.GetId(o)) + "^^" + DynamicTypeURI;
	}

}