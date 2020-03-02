package cognipy.cnl.dl;

import cognipy.ars.*;
import cognipy.cnl.en.*;
import cognipy.executing.hermit.*;
import org.apache.jena.rdf.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class GenerateJenaRules extends GenericVisitor
{

	public static class NotInProfileException extends RuntimeException
	{
	}

	private void NotInProfile()
	{
		throw new NotInProfileException();
	}

	private Model model;
	private boolean useSWRL;
	private String defaultNS;
	private boolean debugSWRL = false;
	private boolean runExeRules = false;
	private boolean debugExeRules = false;
	private boolean modalCheckerRules = false;
	private boolean swrlOnly = false;


	public GenerateJenaRules(Model model, boolean modalChecker, boolean useSWRL, boolean debugSWRL, boolean runExeRules, boolean debugExeRules)
	{
		this(model, modalChecker, useSWRL, debugSWRL, runExeRules, debugExeRules, false);
	}

	public GenerateJenaRules(Model model, boolean modalChecker, boolean useSWRL, boolean debugSWRL, boolean runExeRules)
	{
		this(model, modalChecker, useSWRL, debugSWRL, runExeRules, false, false);
	}

	public GenerateJenaRules(Model model, boolean modalChecker, boolean useSWRL, boolean debugSWRL)
	{
		this(model, modalChecker, useSWRL, debugSWRL, false, false, false);
	}

	public GenerateJenaRules(Model model, boolean modalChecker, boolean useSWRL)
	{
		this(model, modalChecker, useSWRL, false, false, false, false);
	}

	public GenerateJenaRules(Model model, boolean modalChecker)
	{
		this(model, modalChecker, true, false, false, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public GenerateJenaRules(Model model, bool modalChecker, bool useSWRL = true, bool debugSWRL = false, bool runExeRules = false, bool debugExeRules = false, bool swrlOnly = false)
	public GenerateJenaRules(Model model, boolean modalChecker, boolean useSWRL, boolean debugSWRL, boolean runExeRules, boolean debugExeRules, boolean swrlOnly)
	{
		this.model = model;
		this.useSWRL = useSWRL;
		this.debugSWRL = debugSWRL;
		this.runExeRules = runExeRules;
		this.debugExeRules = debugExeRules;
		this.modalCheckerRules = modalChecker;
		this.swrlOnly = swrlOnly;
	}

	private DLToOWLNameConv owlNC = new DLToOWLNameConv();

	public final void setOWLDataFactory(String defaultNS, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		this.owlNC.setOWLFormat(defaultNS, namespaceManager, lex);
	}


	private HashMap<String, Statement> id2stmt;

	public final void setId2stmt(HashMap<String, Statement> id2stmt)
	{
		this.id2stmt = id2stmt;
	}

	private StringBuilder sb = null;
	private static int prp_spo2_cnt = 0;
	private static int swrl_cnt = 0;
	private static int prp_key_cnt = 0;
	private static int sme_cnt = 0;

	public final String Generate(Paragraph p)
	{
		sb = new StringBuilder();
		p.accept(this);
		return sb.toString();
	}

	public final boolean Validate(Statement stmt)
	{
		try
		{
			sb = new StringBuilder();
			stmt.accept(this);
			return true;
		}
		catch (NotInProfileException e)
		{
			return false;
		}
	}

	@Override
	public Object Visit(Paragraph e)
	{
		for (Statement stmt : e.Statements)
		{
			if (!useSWRL && stmt instanceof SwrlStatement)
			{
				continue;
			}
			StringBuilder osb = sb;
			try
			{
				sb = new StringBuilder();
				stmt.accept(this);
				osb.append(sb.toString());
			}
			catch (NotInProfileException e)
			{
			}
			finally
			{
				sb = osb;
			}
		}
		return null;
	}

	private Tuple<String, String, String> SolveSingleSome(String cid, Object restr)
	{
		if (restr instanceof cognipy.cnl.dl.SomeRestriction)
		{
			SomeRestriction Rest = restr instanceof SomeRestriction ? (SomeRestriction)restr : null;
			if (Rest.C instanceof InstanceSet & Rest.R instanceof Atomic)
			{
				InstanceSet iSet = Rest.C instanceof InstanceSet ? (InstanceSet)Rest.C : null;
				if (iSet.Instances.size() == 1 && iSet.Instances.get(0) instanceof NamedInstance)
				{
					IRI q = owlNC.getIRIFromId(cid, EntityKind.Concept);
					IRI r = owlNC.getIRIFromId((Rest.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)Rest.R : null).id, EntityKind.Role);
					IRI isn = owlNC.getIRIFromId((iSet.Instances.get(0) instanceof NamedInstance ? (NamedInstance)iSet.Instances.get(0) : null).name, EntityKind.Instance);

					return Tuple.Create("<" + q.toString() + ">", "<" + r.toString() + ">", "<" + isn.toString() + ">");

				}
			}
		}
		else if (restr instanceof SomeValueRestriction)
		{
			SomeValueRestriction Rest = restr instanceof SomeValueRestriction ? (SomeValueRestriction)restr : null;
			if (Rest.B instanceof ValueSet & Rest.R instanceof Atomic)
			{
				ValueSet vSet = Rest.B instanceof ValueSet ? (ValueSet)Rest.B : null;
				if (vSet.Values.size() == 1)
				{

					IRI q = owlNC.getIRIFromId(cid, EntityKind.Concept);
					IRI r = owlNC.getIRIFromId((Rest.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)Rest.R : null).id, EntityKind.Role);
					String vn = getLiteralVal2(vSet.Values.get(0) instanceof Value ? (Value)vSet.Values.get(0) : null);
					return Tuple.Create("<" + q.toString() + ">", "<" + r.toString() + ">", vn);
				}
			}
		}
		return null;
	}

	public final void appendDebugString(StringBuilder sb, CNL.DL.Statement stmt)
	{
		if (model != null)
		{
			if (debugExeRules)
			{
				DL.Serializer ser = new Serializer();
				String dl = ser.Serialize(stmt);
				String iid = model.createTypedLiteral("\'" + dl.replace("\'", "\\\'").toString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
				sb.append(" debugTraceBuiltIn (" + iid + "),");
			}
		}
	}

	@Override
	public Object Visit(Subsumption e)
	{
		if (e.modality == Statement.Modality.IS)
		{
			if (e.C instanceof cognipy.cnl.dl.Atomic)
			{
				if (e.D instanceof cognipy.cnl.dl.SomeRestriction || e.D instanceof cognipy.cnl.dl.SomeValueRestriction)
				{
					Tuple<String, String, String> tpl = SolveSingleSome((e.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.C : null).id, e.D);
					if (tpl != null)
					{
						tangible.RefObject<Integer> tempRef_sme_cnt = new tangible.RefObject<Integer>(sme_cnt);
						String idx = String.valueOf(Interlocked.Increment(tempRef_sme_cnt));
					sme_cnt = tempRef_sme_cnt.argValue;
						sb.append("[sme-" + idx + ": ");
						id2stmt.put("sme-" + idx, e);
						sb.append("(?X rdf:type " + tpl.Item1 + ") -> (?X " + tpl.Item2 + " " + tpl.Item3 + ")");
						appendDebugString(sb, e);
						sb.append("]" + "\r\n");
					}
				}
				else if (e.D instanceof CNL.DL.ConceptAnd)
				{
					CNL.DL.ConceptAnd A = e.D instanceof CNL.DL.ConceptAnd ? (CNL.DL.ConceptAnd)e.D : null;
					boolean found = false;
					for (Node x : A.Exprs)
					{
						Tuple<String, String, String> tpl = SolveSingleSome((e.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.C : null).id, x);
						if (tpl != null)
						{
							if (!found)
							{
								found = true;
								tangible.RefObject<Integer> tempRef_sme_cnt2 = new tangible.RefObject<Integer>(sme_cnt);
								String idx = String.valueOf(Interlocked.Increment(tempRef_sme_cnt2));
							sme_cnt = tempRef_sme_cnt2.argValue;
								sb.append("[sme-" + idx + ": (?X rdf:type " + tpl.Item1 + ") -> ");
								id2stmt.put("sme-" + idx, e);
							}
							sb.append("(?X " + tpl.Item2 + " " + tpl.Item3 + ")");
						}
					}
					if (found)
					{
						appendDebugString(sb, e);
						sb.append("]" + "\r\n");
					}
				}
			}
			return super.Visit(e);
		}
		else if (modalCheckerRules)
		{
			tangible.RefObject<Integer> tempRef_swrl_cnt = new tangible.RefObject<Integer>(swrl_cnt);
			String id = String.valueOf(Interlocked.Increment(tempRef_swrl_cnt));
		swrl_cnt = tempRef_swrl_cnt.argValue;
			id2stmt.put("subsumption-modal-body-" + id, e);
			String iid = null;
			if (model != null)
			{
				//                iid = model.createTypedLiteral("\"" + id.ToString() + "\"", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
				DL.Serializer ser = new Serializer();
				String dl = ser.Serialize(e);
				iid = model.createTypedLiteral("\'" + dl.replace("\'", "\\\'").toString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
			}

			TransformToJenaRules jenarulesTransform = new TransformToJenaRules();
			jenarulesTransform.setOWLDataFactory(owlNC);
			String ruleC = null;
			String ruleD = null;
			try
			{
				ruleC = jenarulesTransform.ConvertToGetInstancesOf(e.C);
				ruleD = jenarulesTransform.ConvertToGetInstancesOf(e.D);

				boolean normal = (e.modality == Statement.Modality.CAN || e.modality == Statement.Modality.SHOULD || e.modality == Statement.Modality.MUST);
				sb.append("[subsumption-modal-body-" + id + ": ");
				sb.append(ruleC);
				sb.append(" -> ");
				if (model != null)
				{
					sb.append(" modalCheckerBuiltIn (0," + (normal ? "0," : "1,") + iid + ")");
				}
				sb.append("]" + "\r\n");
				sb.append("[subsumption-modal-head-" + id + ": ");
				sb.append(ruleC);
				sb.append(",");
				sb.append(ruleD);
				sb.append(" -> ");
				if (model != null)
				{
					sb.append(" modalCheckerBuiltIn (1," + (normal ? "0," : "1,") + iid + ")");
				}
				appendDebugString(sb, e);
				sb.append("]" + "\r\n");
			}
			catch (cognipy.executing.hermitclient.ReasoningServiceException e)
			{
				ruleC = "";
				ruleD = "";
			}
			catch (java.lang.Exception e2)
			{
			}
			return null;
		}
		else
		{
			return null;
		}
	}

	@Override
	public Object Visit(ComplexRoleInclusion e)
	{
		tangible.RefObject<Integer> tempRef_prp_spo2_cnt = new tangible.RefObject<Integer>(prp_spo2_cnt);
		String id = "prp-spo2-" + String.valueOf(Interlocked.Increment(tempRef_prp_spo2_cnt));
	prp_spo2_cnt = tempRef_prp_spo2_cnt.argValue;
		id2stmt.put(id, e);

		sb.append("[" + id + ": ");
		int varid = 0;
		for (Node r : e.RoleChain)
		{
			if (varid > 0)
			{
				sb.append(", ");
			}
			if (r instanceof CNL.DL.Atomic)
			{
				IRI q = owlNC.getIRIFromId((r instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)r : null).id, EntityKind.Role);
				sb.append("(?X" + String.valueOf(varid) + " <" + q + "> " + "?X" + (String.valueOf(varid) + ")");
			}
			else if (r instanceof CNL.DL.RoleInversion)
			{
				cognipy.cnl.dl.Node rr = (r instanceof CNL.DL.RoleInversion ? (CNL.DL.RoleInversion)r : null).R;
				if (rr instanceof CNL.DL.Atomic)
				{
					IRI q = owlNC.getIRIFromId((rr instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)rr : null).id, EntityKind.Role);
					sb.append("(?X" + (String.valueOf(varid) + " <" + q + "> " + "?X" + String.valueOf(varid) + ")");
				}
			}
			varid++;
		}

		sb.append(" -> ");

		{
			cognipy.cnl.dl.Node r = e.R;
			if (r instanceof CNL.DL.Atomic)
			{
				IRI q = owlNC.getIRIFromId((r instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)r : null).id, EntityKind.Role);
				sb.append("(?X0 <" + q + "> ?X" + String.valueOf(varid) + ")");
			}
			else if (r instanceof CNL.DL.RoleInversion)
			{
				cognipy.cnl.dl.Node rr = (r instanceof CNL.DL.RoleInversion ? (CNL.DL.RoleInversion)r : null).R;
				if (rr instanceof CNL.DL.Atomic)
				{
					IRI q = owlNC.getIRIFromId((rr instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)rr : null).id, EntityKind.Role);
					sb.append("(?X" + String.valueOf(varid) + " <" + q + "> " + "?X0)");
				}
			}
		}
		appendDebugString(sb, e);
		sb.append("]" + "\r\n");
		return null;
	}

	@Override
	public Object Visit(HasKey e)
	{
		StringBuilder[] side = new StringBuilder[]
		{
			new StringBuilder(),
			new StringBuilder()
		};

		IRI conc = owlNC.getIRIFromId((e.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.C : null).id, EntityKind.Concept);
		for (int i = 0; i <= 1; i++)
		{
			side[i].append("(?X" + String.valueOf(i) + " rdf:type <" + conc + ">)");
		}

		int varid = 0;
		for (Node r : e.Roles)
		{
			for (int i = 0; i <= 1; i++)
			{
				side[i].append(", ");

				if (r instanceof CNL.DL.Atomic)
				{
					IRI q = owlNC.getIRIFromId((r instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)r : null).id, EntityKind.Role);
					side[i].append("(?X" + String.valueOf(i) + " <" + q + "> " + "?Y" + String.valueOf(varid) + "X" + String.valueOf(i) + ")");
				}
				else if (r instanceof CNL.DL.RoleInversion)
				{
					cognipy.cnl.dl.Node rr = (r instanceof CNL.DL.RoleInversion ? (CNL.DL.RoleInversion)r : null).R;
					if (rr instanceof CNL.DL.Atomic)
					{
						IRI q = owlNC.getIRIFromId((rr instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)rr : null).id, EntityKind.Role);
						side[i].append("(?Y" + String.valueOf(varid) + "X" + String.valueOf(i) + " <" + q + "> ?X" + String.valueOf(i) + ")");
					}
				}
			}
			varid++;
		}
		for (Node r : e.DataRoles)
		{
			for (int i = 0; i <= 1; i++)
			{
				side[i].append(", ");

				if (r instanceof CNL.DL.Atomic)
				{
					IRI q = owlNC.getIRIFromId((r instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)r : null).id, EntityKind.DataRole);
					side[i].append("(?X" + String.valueOf(i) + " <" + q + "> " + "?Y" + String.valueOf(varid) + "X" + String.valueOf(i) + ")");
				}
			}
			varid++;
		}

		// for sameas

		tangible.RefObject<Integer> tempRef_prp_key_cnt = new tangible.RefObject<Integer>(prp_key_cnt);
		String idx = String.valueOf(Interlocked.Increment(tempRef_prp_key_cnt)).toString();
	prp_key_cnt = tempRef_prp_key_cnt.argValue;
		sb.append("[prp-key-1-" + idx + ": ");
		id2stmt.put("prp-key-1-" + idx, e);

		sb.append(side.First() + "," + side.Last() + ", notEqual(?X0, ?X1)");
		for (int z = 0; z < varid; z++)
		{
			sb.append(", equal(");
			for (int i = 0; i <= 1; i++)
			{
				if (i > 0)
				{
					sb.append(" ,");
				}
				sb.append("?Y" + String.valueOf(z) + "X" + String.valueOf(i));
			}
			sb.append(")");
		}
		sb.append(" -> (?X0 owl:sameAs ?X1)");
		appendDebugString(sb, e);
		sb.append("]" + "\r\n");

		//for different

		tangible.RefObject<Integer> tempRef_prp_key_cnt2 = new tangible.RefObject<Integer>(prp_key_cnt);
		String idx2 = String.valueOf(Interlocked.Increment(tempRef_prp_key_cnt2));
	prp_key_cnt = tempRef_prp_key_cnt2.argValue;
		sb.append("[prp-key-2-" + idx2 + ": ");
		id2stmt.put("prp-key-2-" + idx2, e);

		sb.append(side.First() + "," + side.Last() + ", notEqual(?X0, ?X1)");
		sb.append(", pairwizeDifferentAtleastOnce(");
		boolean first = true;
		for (int z = 0; z < varid; z++)
		{
			for (int i = 0; i <= 1; i++)
			{
				if (!first)
				{
					sb.append(" ,");
				}
				else
				{
					first = false;
				}
				sb.append("?Y" + String.valueOf(z) + "X" + String.valueOf(i));
			}
		}

		sb.append(")");
		sb.append(" -> (?X0 owl:differentFrom ?X1)");
		appendDebugString(sb, e);
		sb.append("]" + "\r\n");

		return null;
	}

	private boolean inSwrlBody = false;
	@Override
	public Object Visit(SwrlStatement e)
	{
		tangible.RefObject<Integer> tempRef_swrl_cnt = new tangible.RefObject<Integer>(swrl_cnt);
		String id = String.valueOf(Interlocked.Increment(tempRef_swrl_cnt));
	swrl_cnt = tempRef_swrl_cnt.argValue;
		id2stmt.put("swrl-" + id, e);
		String iid = null;
		if (model != null)
		{
			//                iid = model.createTypedLiteral("\"" + id.ToString() + "\"", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
			DL.Serializer ser = new Serializer();
			String dl = ser.Serialize(e);
			iid = model.createTypedLiteral("\'" + dl.replace("\'", "\\\'").toString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
		}
		if (e.modality == Statement.Modality.IS)
		{
			sb.append("[swrl-" + id + ": ");
			inSwrlBody = true;
			e.slp.accept(this);
			sb.append(" -> ");
			if (model != null && debugSWRL)
			{
				sb.append(" debugTraceBuiltIn (" + iid + "),");
			}
			inSwrlBody = false;
			e.slc.accept(this);
			sb.append("]" + "\r\n");
		}
		else if (modalCheckerRules)
		{
			boolean normal = (e.modality == Statement.Modality.CAN || e.modality == Statement.Modality.SHOULD || e.modality == Statement.Modality.MUST);
			sb.append("[swrl-modal-body-" + id + ": ");
			inSwrlBody = true;
			e.slp.accept(this);
			sb.append(" -> ");
			if (model != null)
			{
				sb.append(" modalCheckerBuiltIn (0," + (normal ? "0," : "1,") + iid + ")");
			}
			inSwrlBody = false;
			sb.append("]" + "\r\n");

			sb.append("[swrl-modal-head-" + id + ": ");
			inSwrlBody = true;
			e.slp.accept(this);
			sb.append(", ");
			e.slc.accept(this);
			sb.append(" -> ");
			if (model != null)
			{
				sb.append(" modalCheckerBuiltIn (1," + (normal ? "0," : "1,") + iid + ")");
			}
			inSwrlBody = false;
			sb.append("]" + "\r\n");
		}
		return null;
	}

	public HashMap<Integer, SwrlIterate> TheIterators = new HashMap<Integer, SwrlIterate>();

	@Override
	public Object Visit(SwrlIterate rule)
	{
		if (runExeRules)
		{
			tangible.RefObject<Integer> tempRef_swrl_cnt = new tangible.RefObject<Integer>(swrl_cnt);
			int idx = Interlocked.Increment(tempRef_swrl_cnt);
		swrl_cnt = tempRef_swrl_cnt.argValue;
			id2stmt.put("swrl-" + String.valueOf(idx), rule);
			sb.append("[swrl-" + String.valueOf(idx) + ": ");
			rule.slp.accept(this);
			sb.append(" -> ");
			if (model != null)
			{
				DL.Serializer ser = new Serializer();
				String dl = ser.Serialize(rule);
				String iid = model.createTypedLiteral("\'" + dl.replace("\'", "\\\'").toString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
				if (debugExeRules)
				{
					sb.append(" debugTraceBuiltIn (" + iid + "),");
				}
			}
			sb.append("swrlIterator(" + String.valueOf(idx) + ")");
			sb.append("]" + "\r\n");

			TheIterators.put(idx, rule);
		}
		return null;
	}

	@Override
	public Object Visit(SwrlItemList e)
	{
		boolean firstOne = true;
		for (SwrlItem i : e.list)
		{
			if (firstOne)
			{
				firstOne = false;
			}
			else
			{
				sb.append(", ");
			}
			i.accept(this);
		}
		return null;
	}

	//SwrlItems

	@Override
	public Object Visit(SwrlInstance e)
	{
		if (e.C instanceof CNL.DL.Atomic)
		{
			sb.append("(");
			Object inst = e.I.accept(this);
			sb.append(inst);
			sb.append(" rdf:type ");
			IRI cls = owlNC.getIRIFromId((e.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.C : null).id, EntityKind.Concept);
			sb.append("<" + cls + ">");
			sb.append(")");
			if (!inSwrlBody && !swrlOnly)
			{
				sb.append("(");
				sb.append(inst);
				sb.append(" rdf:type ");
				sb.append("owl:NamedIndividual");
				sb.append(")");
				sb.append("(");
				sb.append("<" + cls + ">");
				sb.append(" rdf:type ");
				sb.append("owl:Class");
				sb.append(")");
			}
		}
		else
		{
			NotInProfile();
			return super.Visit(e);
		}
		return null;
	}

	@Override
	public Object Visit(SwrlRole e)
	{
		sb.append("(");
		Object inst = e.I.accept(this);
		sb.append(inst);
		IRI rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
		sb.append(" <");
		sb.append(rel);
		sb.append("> ");
		Object jnst = e.J.accept(this);
		sb.append(jnst);
		sb.append(")");
		if (!inSwrlBody && !swrlOnly)
		{
			sb.append("(");
			sb.append(inst);
			sb.append(" rdf:type ");
			sb.append("owl:NamedIndividual");
			sb.append(")");
			sb.append("(");
			sb.append(jnst);
			sb.append(" rdf:type ");
			sb.append("owl:NamedIndividual");
			sb.append(")");
			sb.append("(");
			sb.append("<" + rel + ">");
			sb.append(" rdf:type ");
			sb.append("owl:ObjectProperty");
			sb.append(")");
		}
		return null;
	}

	@Override
	public Object Visit(SwrlSameAs e)
	{
		Object inst = e.I.accept(this);
		Object jnst = e.J.accept(this);
		if (inSwrlBody)
		{
			sb.append("equal(");
			sb.append(inst);
			sb.append(",");
			sb.append(jnst);
			sb.append(")");
		}
		else
		{
			sb.append("(");
			sb.append(inst);
			sb.append(", owl:sameAs, ");
			sb.append(jnst);
			sb.append(")");

			if (!swrlOnly)
			{
				sb.append("(");
				sb.append(inst);
				sb.append(" rdf:type ");
				sb.append("owl:NamedIndividual");
				sb.append(")");
				sb.append("(");
				sb.append(jnst);
				sb.append(" rdf:type ");
				sb.append("owl:NamedIndividual");
				sb.append(")");
			}
		}
		return null;
	}

	@Override
	public Object Visit(SwrlDifferentFrom e)
	{
		Object inst = e.I.accept(this);
		Object jnst = e.J.accept(this);
		if (inSwrlBody)
		{
			sb.append("notEqual(");
			sb.append(inst);
			sb.append(",");
			sb.append(jnst);
			sb.append(")");
		}
		else
		{
			sb.append("(");
			sb.append(inst);
			sb.append(", owl:differentFrom, ");
			sb.append(jnst);
			sb.append(")");

			if (!swrlOnly)
			{
				sb.append("(");
				sb.append(inst);
				sb.append(" rdf:type ");
				sb.append("owl:NamedIndividual");
				sb.append(")");
				sb.append("(");
				sb.append(jnst);
				sb.append(" rdf:type ");
				sb.append("owl:NamedIndividual");
				sb.append(")");
			}
		}
		return null;
	}

	@Override
	public Object Visit(SwrlDataRange e)
	{
		try (curFacetVal.set(e.DO.accept(this).toString()))
		{
			sb.append(e.B.accept(this));
		}
		return null;
	}

	@Override
	public Object Visit(SwrlDataProperty e)
	{
		sb.append("(");
		Object inst = e.IO.accept(this);
		sb.append(inst);
		IRI rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
		sb.append(" <");
		sb.append(rel);
		sb.append("> ");
		sb.append(e.DO.accept(this));
		sb.append(")");
		if (!inSwrlBody && !swrlOnly)
		{
			sb.append("(");
			sb.append(inst);
			sb.append(" rdf:type ");
			sb.append("owl:NamedIndividual");
			sb.append(")");
			sb.append("(");
			sb.append("<" + rel + ">");
			sb.append(" rdf:type ");
			sb.append("owl:DatatypeProperty");
			sb.append(")");
		}
		return null;
	}

	private void SwrlBuiltInNoImpl(SwrlBuiltIn e)
	{
		throw new UnsupportedOperationException("Builtin :" + e.builtInName + " is not supported yet.");
	}

	private void AppendComparator(StringBuilder sb, String comparator, String A, String B)
	{
		if (comparator.equals("≤"))
		{
			sb.append("le(" + A + ", " + B + ")");
		}
		else if (comparator.equals("<"))
		{
			sb.append("lessThan(" + A + ", " + B + ")");
		}
		else if (comparator.equals("≥"))
		{
			sb.append("ge(" + A + ", " + B + ")");
		}
		else if (comparator.equals(">"))
		{
			sb.append("greaterThan(" + A + ", " + B + ")");
		}
		else if (comparator.equals("="))
		{
			sb.append("equal(" + A + ", " + B + ")");
		}
		else if (comparator.equals("≠"))
		{
			sb.append("notEqual(" + A + ", " + B + ")");
		}
		else
		{
			throw new UnsupportedOperationException("Builtin :" + comparator + " is not supported yet.");
		}
	}

	private String mapCode(String code)
	{
		switch (code)
		{
			case "≤":
				return "<=";
			case "≥":
				return ">=";
			case "≠":
				return "<>";
			default:
				return code;
		}
	}

	@Override
	public Object Visit(SwrlBuiltIn e)
	{
		if (model == null)
		{
			return null;
		}
		String builtInName = e.builtInName;
		String btag = KeyWords.Me.GetTag(mapCode(builtInName));
		if (btag.equals("CMP") || btag.equals("EQ"))
		{
			ISwrlObject A = e.Values.get(1);
			ISwrlObject B = e.Values.get(0);
			AppendComparator(sb, e.builtInName, A.accept(this).toString(), B.accept(this).toString());
		}
		else
		{
			String lst = e.Values.get(e.Values.size() - 1).accept(this).toString();
			;

			for (int i = 0; i < e.Values.size() - 1; i++)
			{
				String dn = e.Values.get(i).accept(this).toString();
				lst += ", ";
				lst += dn;
			}

			if (builtInName.equals("plus") || builtInName.equals("times") || builtInName.equals("followed-by"))
			{
				if (builtInName.equals("followed-by"))
				{
					sb.append("concatenateStrings(" + lst + ")");
				}
				else if (builtInName.equals("plus"))
				{
					sb.append("sumNumbers(" + lst + ")");
				}
				else if (builtInName.equals("times"))
				{
					sb.append("multiplyNumbers(" + lst + ")");
				}
				else
				{
					SwrlBuiltInNoImpl(e);
				}
			}
			else if (builtInName.equals("datetime") || builtInName.equals("duration"))
			{
				if (builtInName.equals("datetime"))
				{
					sb.append("createDatetime(" + lst + ")");
				}
				else if (builtInName.equals("duration"))
				{
					sb.append("createDuration(" + lst + ")");
				}
			}
			else if (e.builtInName.equals("alpha-representation-of"))
			{
				sb.append("alpha(" + lst + ")");
			}
			else if (e.builtInName.equals("annotation"))
			{
				sb.append("annotation(" + lst + ")");
			}
			else if (e.builtInName.equals("execute"))
			{
				sb.append("executeExternalFunction(" + lst + ")");
			}
			else if (builtInName.equals("translated") || builtInName.equals("replaced"))
			{
				if (builtInName.equals("translated"))
				{
					sb.append("complexStringOperation('translate'," + lst + ")");
				}
				else if (builtInName.equals("replaced"))
				{
					sb.append("complexStringOperation('replace'," + lst + ")");
				}
			}
			else if (builtInName.equals("from") || builtInName.equals("before") || builtInName.equals("after"))
			{
				if (e.builtInName.equals("from"))
				{
					sb.append("simpleStringOperation('substring'," + lst + ")");
				}
				else if (e.builtInName.equals("before"))
				{
					sb.append("simpleStringOperation('substring-before'," + lst + ")");
				}
				else if (e.builtInName.equals("after"))
				{
					sb.append("simpleStringOperation('substring-after'," + lst + ")");
				}
				else
				{
					SwrlBuiltInNoImpl(e);
				}
			}
			else if (e.Values.size() == 3)
			{
				if (e.builtInName.equals("minus"))
				{
					sb.append("mathBinary('subtract'," + lst + ")");
				}
				else if (e.builtInName.equals("divided-by"))
				{
					sb.append("mathBinary('divide'," + lst + ")");
				}
				else if (e.builtInName.equals("integer-divided-by"))
				{
					sb.append("mathBinary('int-divide'," + lst + ")");
				}
				else if (e.builtInName.equals("modulo"))
				{
					sb.append("mathBinary('modulo'," + lst + ")");
				}
				else if (e.builtInName.equals("raised-to-the-power-of"))
				{
					sb.append("mathBinary('power'," + lst + ")");
				}
				else if (e.builtInName.equals("rounded-with-the-precision-of"))
				{
					sb.append("mathBinary('round-half-to-even'," + lst + ")");
				}
				else
				{
					SwrlBuiltInNoImpl(e);
				}
			}
			else if (e.Values.size() == 2)
			{
				if (e.builtInName.equals("not"))
				{
					sb.append("booleanUnary('not'," + lst + ")");
				}
				else if (e.builtInName.equals("minus"))
				{
					sb.append("mathUnary('minus'," + lst + ")");
				}
				else if (e.builtInName.equals("absolute-value-of"))
				{
					sb.append("mathUnary('absolute'," + lst + ")");
				}
				else if (e.builtInName.equals("ceiling-of"))
				{
					sb.append("mathUnary('ceiling'," + lst + ")");
				}
				else if (e.builtInName.equals("floor-of"))
				{
					sb.append("mathUnary('floor'," + lst + ")");
				}
				else if (e.builtInName.equals("round-of"))
				{
					sb.append("mathUnary('round'," + lst + ")");
				}
				else if (e.builtInName.equals("sine-of"))
				{
					sb.append("mathUnary('sine'," + lst + ")");
				}
				else if (e.builtInName.equals("cosine-of"))
				{
					sb.append("mathUnary('cosine'," + lst + ")");
				}
				else if (e.builtInName.equals("tangent-of"))
				{
					sb.append("mathUnary('tangent'," + lst + ")");
				}
				else if (e.builtInName.equals("case-ignored"))
				{
					sb.append("stringUnary('case-ignore'," + lst + ")");
				}
				else if (e.builtInName.equals("length-of"))
				{
					sb.append("stringUnary('length'," + lst + ")");
				}
				else if (e.builtInName.equals("space-normalized"))
				{
					sb.append("stringUnary('space-normalize'," + lst + ")");
				}
				else if (e.builtInName.equals("upper-cased"))
				{
					sb.append("stringUnary('upper-case'," + lst + ")");
				}
				else if (e.builtInName.equals("lower-cased"))
				{
					sb.append("stringUnary('lower-case'," + lst + ")");
				}
				else if (e.builtInName.equals("contains-string"))
				{
					sb.append("stringUnary('contains'," + lst + ")");
				}
				else if (e.builtInName.equals("starts-with-string"))
				{
					sb.append("stringUnary('starts-with'," + lst + ")");
				}
				else if (e.builtInName.equals("ends-with-string"))
				{
					sb.append("stringUnary('ends-with'," + lst + ")");
				}
				else if (e.builtInName.equals("matches-string"))
				{
					sb.append("stringUnary('matches'," + lst + ")");
				}
				else if (e.builtInName.equals("contains-case-ignored-string"))
				{
					sb.append("stringUnary('contains-case-ignore'," + lst + ")");
				}
				else if (e.builtInName.equals("sounds-like-string"))
				{
					sb.append("stringUnary('sounds-like'," + lst + ")");
				}
				else
				{
					SwrlBuiltInNoImpl(e);
				}
			}
			else
			{
				SwrlBuiltInNoImpl(e);
			}

		}
		return null;
	}

	//SwrlNodes

	@Override
	public Object Visit(SwrlIVal e)
	{
		DlName dl = new DlName();
		dl.id = e.I;
		cognipy.cnl.dl.DlName.Parts dlp = dl.Split();
		if (Character.isLowerCase(dlp.name.charAt(0)))
		{
			return "<" + owlNC.getIRIFromId(e.I, EntityKind.Role) + ">";
		}
		else
		{
			return "<" + owlNC.getIRIFromId(e.I, EntityKind.Instance) + ">";
		}
	}

	@Override
	public Object Visit(SwrlIVar e)
	{
		return "?" + e.VAR.replace("-", "_");
	}

	private static Regex DtmRg = new Regex("(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?)(Z|((\\+|\\-)[0-2][0-9]:[0-5][0-9]))?)?", RegexOptions.Compiled);
	private static String completeDTMVal(String val)
	{
		System.Text.RegularExpressions.Match m = DtmRg.Match(val);
		String dta = m.Groups["date"].Value;
		String tm = m.Groups["time"].Value;
		StringBuilder sb = new StringBuilder();
		sb.append(dta);
		if (tangible.StringHelper.isNullOrEmpty(tm))
		{
			sb.append("T00:00:00");
		}
		else
		{
			sb.append(tm);
		}
		if (tm.length() == "T00:00".length())
		{
			sb.append(":00");
		}
		return sb.toString();
	}

	private Literal getLiteralVal(Value v)
	{
		if (model == null)
		{
			return null;
		}

		if (v instanceof CNL.DL.Bool)
		{
			return model.createTypedLiteral(v.ToBool() ? "true" : "false", org.apache.jena.datatypes.xsd.XSDDatatype.XSDboolean);
		}
		if (v instanceof CNL.DL.String)
		{
			return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
		}
		if (v instanceof CNL.DL.Float)
		{
			return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdouble);
		}
		if (v instanceof CNL.DL.Number)
		{
			return model.createTypedLiteral(new java.lang.Integer(v.ToInt()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger);
		}
		if (v instanceof CNL.DL.DateTimeVal)
		{
			return model.createTypedLiteral(completeDTMVal(v.ToStringExact()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdateTime);
		}
		if (v instanceof CNL.DL.Duration)
		{
			return model.createTypedLiteral(v.ToStringExact(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDduration);
		}

		return model.createTypedLiteral(v.toString()); //TODO xsd:date i inne typy
	}

	private String getLiteralVal2(Value v)
	{
		Literal vv = getLiteralVal(v);
		if (vv == null)
		{
			return null;
		}
		String l = vv.toString();
		int p = l.lastIndexOf('^');
		if (p == -1)
		{
			return vv.toString();
		}
		else
		{
			return "'" + l.substring(0, p - 1).replace("\\", "\\\\") + "'^^" + l.substring(p + 1).replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
		}
	}

	@Override
	public Object Visit(SwrlDVal e)
	{
		return getLiteralVal2(e.Val);
	}

	@Override
	public Object Visit(SwrlDVar e)
	{
		return "?" + e.VAR.replace("-", "_");
	}
	//bounds

	@Override
	public Object Visit(BoundFacets e)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		return tangible.StringHelper.join(", ", from f in e.FL.List select f.accept(this).toString());
	}

	private boolean boundNot = false;

	@Override
	public Object Visit(BoundNot e)
	{
		boundNot = !boundNot;
		Object r = e.B.accept(this);
		boundNot = !boundNot;
		return r;
	}

	@Override
	public Object Visit(BoundAnd e)
	{
		if (!boundNot)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return tangible.StringHelper.join(", ", from f in e.List select f.accept(this).toString());
		}
		else
		{
			NotInProfile();
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(BoundOr e)
	{
		if (boundNot)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return tangible.StringHelper.join(", ", from f in e.List select f.accept(this).toString());
		}
		else
		{
			NotInProfile();
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(BoundVal e)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var v = curFacetVal.get();
		String val = getLiteralVal2(e.V);
		if ((!boundNot && e.Kind.equals("≠")) || (boundNot && e.Kind.equals("=")))
		{
			return "notEqual(" + v + ", " + val + ")";
		}
		if ((boundNot && e.Kind.equals("≠")) || (!boundNot && e.Kind.equals("=")))
		{
			return "equal(" + v + ", " + val + ")";
		}
		NotInProfile();
		return super.Visit(e);
	}

	@Override
	public Object Visit(TotalBound e)
	{
		NotInProfile();
		return super.Visit(e);
	}

	@Override
	public Object Visit(DTBound e)
	{
		NotInProfile();
		return super.Visit(e);
	}

	@Override
	public Object Visit(TopBound e)
	{
		NotInProfile();
		return super.Visit(e);
	}

	@Override
	public Object Visit(ValueSet e)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var v = curFacetVal.get();
		if (!boundNot)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return tangible.StringHelper.join(", ", from f in e.Values select "equal(" + v + ", " + getLiteralVal2(f) + ")");
		}
		else if (e.Values.size() == 1)
		{
			String val = getLiteralVal2(e.Values.get(0));
			return "notEqual(" + v + ", " + val + ")";
		}
		else
		{
			NotInProfile();
		}
		return super.Visit(e);
	}

	private VisitingParam<String> curFacetVal = new VisitingParam<String>(null);

	@Override
	public Object Visit(Facet e)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var v = curFacetVal.get();
		String val = getLiteralVal2(e.V);
		if (val == null)
		{
			return "";
		}

		if (e.Kind.equals("≤"))
		{
			return (boundNot ? "ge" : "le") + "(" + v + ", " + val + ")";
		}
		else if (e.Kind.equals("<"))
		{
			return (boundNot ? "greaterThan" : "lessThan") + "(" + v + ", " + val + ")";
		}
		else if (e.Kind.equals("≥"))
		{
			return (boundNot ? "le" : "ge") + "(" + v + ", " + val + ")";
		}
		else if (e.Kind.equals(">"))
		{
			return (boundNot ? "lessThan" : "greaterThan") + "(" + v + ", " + val + ")";
		}
		else if (e.Kind.equals("#") && !boundNot)
		{
			return "regex(" + val + ", " + v + ")";
		}
		else if (e.Kind.equals("<->") && !boundNot)
		{
			return "stringLength(" + val + ", '=', " + v + ")";
		}
		else if (e.Kind.equals("<-> ≥") && !boundNot)
		{
			return "stringLength(" + val + ", '≥', " + v + ")";
		}
		else if (e.Kind.equals("<-> ≤") && !boundNot)
		{
			return "stringLength(" + val + ", '≤', " + v + ")";
		}
		else if (e.Kind.equals("<->") && !boundNot)
		{
			return String.format("stringLength(" + val + ", '=', %1$s)", v);
		}
		else if (e.Kind.equals("<-> ≥") && !boundNot)
		{
			return String.format("stringLength(" + val + ", '≥', %1$s)", v);
		}
		else if (e.Kind.equals("<-> ≤") && !boundNot)
		{
			return String.format("stringLength(" + val + ", '≤', %1$s)", v);
		}

		NotInProfile();

		return super.Visit(e);
	}

	public HashMap<Integer, Tuple<String, ArrayList<IExeVar>>> TheRules = new HashMap<Integer, Tuple<String, ArrayList<IExeVar>>>();

	@Override
	public Object Visit(ExeStatement rule)
	{
		if (runExeRules)
		{
			tangible.RefObject<Integer> tempRef_swrl_cnt = new tangible.RefObject<Integer>(swrl_cnt);
			int idx = Interlocked.Increment(tempRef_swrl_cnt);
		swrl_cnt = tempRef_swrl_cnt.argValue;
			id2stmt.put("swrl-" + String.valueOf(idx), rule);
			sb.append("[swrl-" + String.valueOf(idx) + ": ");
			rule.slp.accept(this);
			sb.append(" -> ");
			if (model != null)
			{
				DL.Serializer ser = new Serializer();
				CNL.DL.ExeStatement nrule = new CNL.DL.ExeStatement(null);
				nrule.args = rule.args;
				nrule.exe = "<?...?>";
				nrule.slp = rule.slp;
				String dl = ser.Serialize(nrule);
				String iid = model.createTypedLiteral("\'" + dl.replace("\'", "\\\'").toString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
				if (debugExeRules)
				{
					sb.append(" debugTraceBuiltIn (" + iid + "),");
				}
			}

			String lst = rule.exe;

			for (int i = 0; i < rule.args.list.size(); i++)
			{
				String dn = rule.args.list.get(i).accept(this).toString();
				lst += ", ";
				lst += dn;
			}

			sb.append("executeExternalRule(" + lst + ")");
			sb.append("]" + "\r\n");

		}
		return null;
	}
}