package cognipy.cnl.dl;

import cognipy.ars.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class Serializer implements cognipy.cnl.dl.IVisitor
{
	private HashSet<Tuple<EntityKind, String>> signature = new HashSet<Tuple<EntityKind, String>>();
	private HashMap<String, Value> foundValues = new HashMap<String, Value>();
	private HashSet<Tuple<String, String, String>> dataValues = new HashSet<Tuple<String, String, String>>();
	private VisitingParam<EntityKind> isKindOf = new VisitingParam<EntityKind>(EntityKind.Concept);
	private VisitingParam<String> attributeName = new VisitingParam<String>(null);
	private HashMap<String, HashSet<String>> dependentAttrs = new HashMap<String, HashSet<String>>();
	private HashSet<String> swrlAttrsBody = new HashSet<String>();
	private HashSet<String> swrlAttrsHead = new HashSet<String>();
	private ArrayList<CNL.DL.InstanceValue> instanceValues = new ArrayList<InstanceValue>();
	private VisitingParam<HashSet<String>> swrlCurAttr = new VisitingParam<HashSet<String>>(null);

	public final ArrayList<CNL.DL.InstanceValue> GetInstanceValues()
	{
		return instanceValues;
	}

	public final HashSet<Tuple<EntityKind, String>> GetTaggedSignature()
	{
		return signature;
	}

	public static String entName(ARS.EntityKind kind)
	{
		switch (kind)
		{
			case Concept:
				return "C";
			case Instance:
				return "I";
			case Role:
				return "R";
			case DataRole:
				return "D";
			case DataType:
				return "T";
			case Annotation:
				return "N";
			case Statement:
				return "S";
		}
		throw new IllegalStateException();
	}
	public final HashSet<String> GetSignature()
	{
		HashSet<String> ret = new HashSet<String>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var s : signature)
		{
			ret.add(entName(s.Item1) + ":" + s.Item2);
		}
		return ret;
	}

	public final HashMap<String, HashSet<String>> GetDependentAttrs()
	{
		return dependentAttrs;
	}


	public final HashSet<Tuple<String, String, String>> GetDataValues()
	{
		HashSet<Tuple<String, String, String>> ret = new HashSet<Tuple<String, String, String>>();
		HashMap<String, HashSet<Tuple<String, String>>> dtt = new HashMap<String, HashSet<Tuple<String, String>>>();
		HashSet<Tuple<String, String>> more = new HashSet<Tuple<String, String>>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var k : dataValues)
		{
			if (k.Item1.equals(""))
			{
				more.add(Tuple.Create(k.Item2, k.Item3.substring(1)));
			}
			else if (k.Item2[0] == '\r')
			{
				String ki = k.Item2.substring(1);
				if (!dtt.containsKey(ki))
				{
					dtt.put(ki, new HashSet<Tuple<String, String>>());
				}
				dtt.get(ki).add(Tuple.Create(k.Item1, k.Item3));
			}
			else
			{
				ret.add(k);
			}
		}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var m : more)
		{
			if (dtt.containsKey(m.Item2))
			{
				for (Tuple<String, String> n : dtt.get(m.Item2))
				{
					ret.add(Tuple.Create(n.Item1, m.Item1, n.Item2));
				}
			}
		}
		return ret;
	}


	public Serializer()
	{
		this(false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Serializer(bool simplifyBrackets = false)
	public Serializer(boolean simplifyBrackets)
	{
		this.simplifyBrackets = simplifyBrackets;
	}

	private boolean simplifyBrackets = false;

	private Object brack(Node parent, Node child)
	{
		if (simplifyBrackets && (child.priority() == 0 || (child.priority() >= parent.priority())))
		{
			return child.accept(this);
		}
		else
		{
			return "(" + child.accept(this) + ")";
		}
	}

	private Object brack(AbstractBound parent, AbstractBound child)
	{
		if (simplifyBrackets && (child.priority() == 0 || (child.priority() >= parent.priority())))
		{
			return child.accept(this);
		}
		else
		{
			return "(" + child.accept(this) + ")";
		}
	}

	public final String Serialize(cognipy.cnl.dl.Paragraph p)
	{
		signature = new HashSet<Tuple<EntityKind, String>>();
		dataValues = new HashSet<Tuple<String, String, String>>();
		foundValues = new HashMap<String, Value>();
		dependentAttrs = new HashMap<String, HashSet<String>>();
		instanceValues = new ArrayList<InstanceValue>();
		Object tempVar = p.accept(this);
		return tempVar instanceof String ? (String)tempVar : null;
	}

	public final String Serialize(cognipy.cnl.dl.Statement s)
	{
		signature = new HashSet<Tuple<EntityKind, String>>();
		dataValues = new HashSet<Tuple<String, String, String>>();
		foundValues = new HashMap<String, Value>();
		dependentAttrs = new HashMap<String, HashSet<String>>();
		instanceValues = new ArrayList<InstanceValue>();
		Object tempVar = s.accept(this);
		return tempVar instanceof String ? (String)tempVar : null;
	}

	public final String Serialize(cognipy.cnl.dl.Node n)
	{
		signature = new HashSet<Tuple<EntityKind, String>>();
		dataValues = new HashSet<Tuple<String, String, String>>();
		foundValues = new HashMap<String, Value>();
		dependentAttrs = new HashMap<String, HashSet<String>>();
		instanceValues = new ArrayList<InstanceValue>();
		Object tempVar = n.accept(this);
		return tempVar instanceof String ? (String)tempVar : null;
	}

	public final String Serialize(cognipy.cnl.dl.Instance n)
	{
		signature = new HashSet<Tuple<EntityKind, String>>();
		dataValues = new HashSet<Tuple<String, String, String>>();
		foundValues = new HashMap<String, Value>();
		dependentAttrs = new HashMap<String, HashSet<String>>();
		instanceValues = new ArrayList<InstanceValue>();
		Object tempVar = n.accept(this);
		return tempVar instanceof String ? (String)tempVar : null;
	}

	public final Object Visit(cognipy.cnl.dl.Paragraph e)
	{
		StringBuilder sb = new StringBuilder();
		for (Statement x : e.Statements)
		{
			if (x != null)
			{
				Object tempVar = x.accept(this);
				String str = tempVar instanceof String ? (String)tempVar : null;
				if (!tangible.StringHelper.isNullOrWhiteSpace(str))
				{
					sb.append(str + "\r\n");
				}
			}
		}
		return sb.toString();
	}

	public final String Modality(cognipy.cnl.dl.Statement.Modality m)
	{
		switch (m)
		{
			case MUST:
				return "□";
			case SHOULD:
				return "◊";
			case CAN:
				return "◊◊";
			case MUSTNOT:
				return "~◊◊";
			case SHOULDNOT:
				return "~◊";
			case CANNOT:
				return "~□";
			default:
				return "";
		}
	}

	public final Object Visit(cognipy.cnl.dl.Subsumption e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.C.accept(this));
		sb.append("⊑");
		sb.append(Modality(e.modality));
		sb.append(e.D.accept(this));
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.Equivalence e)
	{
		StringBuilder sb = new StringBuilder();
		if (e.Equivalents.size() == 2)
		{
			sb.append(e.Equivalents.get(0).accept(this));
			sb.append("≡");
			sb.append(Modality(e.modality));
			sb.append(e.Equivalents.get(1).accept(this));
		}
		else
		{
			sb.append("≡");
			sb.append(Modality(e.modality));
			sb.append("(");
			boolean first = true;
			for (Node x : e.Equivalents)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(x.accept(this));
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.Disjoint e)
	{
		StringBuilder sb = new StringBuilder();
		if (e.Disjoints.size() == 2)
		{
			sb.append(e.Disjoints.get(0).accept(this));
			sb.append("⊑");
			sb.append(Modality(e.modality));
			sb.append("￢");
			sb.append(e.Disjoints.get(1).accept(this));
			return sb.toString();
		}
		else
		{
			sb.append("￢≡(");
			boolean first = true;
			for (Node x : e.Disjoints)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(x.accept(this));
			}
			sb.append(")");
		}
		return sb.toString();
	}

	public final Object Visit(DataTypeDefinition e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.name);
		signature.add(Tuple.Create(EntityKind.DataType, e.name));
		sb.append("≡≡");
		try (attributeName.set("\r" + e.name))
		{
			sb.append(e.B.accept(this));
		}
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.DisjointUnion e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.name);
		sb.append("￢≡(");
		boolean first = true;
		for (Node x : e.Union)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(",");
			}
			sb.append(x.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.RoleInclusion e)
	{
		try (isKindOf.set(EntityKind.Role))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(e.C.accept(this));
			sb.append("⊆");
			sb.append(Modality(e.modality));
			sb.append(e.D.accept(this));
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.RoleEquivalence e)
	{
		try (isKindOf.set(EntityKind.Role))
		{
			StringBuilder sb = new StringBuilder();
			if (e.Equivalents.size() == 2)
			{
				sb.append(e.Equivalents.get(0).accept(this));
				sb.append("≗");
				sb.append(e.Equivalents.get(1).accept(this));
			}
			else
			{
				sb.append("≗(");
				boolean first = true;
				for (Node x : e.Equivalents)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append(x.accept(this));
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.RoleDisjoint e)
	{
		try (isKindOf.set(EntityKind.Role))
		{
			StringBuilder sb = new StringBuilder();
			if (e.Disjoints.size() == 2)
			{
				sb.append(e.Disjoints.get(0).accept(this));
				sb.append("⊆￢");
				sb.append(e.Disjoints.get(1).accept(this));
			}
			else
			{
				sb.append("￢≗(");
				boolean first = true;
				for (Node x : e.Disjoints)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append(x.accept(this));
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.ComplexRoleInclusion e)
	{
		try (isKindOf.set(EntityKind.Role))
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Node n : e.RoleChain)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append("○");
				}
				sb.append(n.accept(this));
			}
			sb.append("⊆");
			sb.append(Modality(e.modality));
			sb.append(e.R.accept(this));
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleInclusion e)
	{
		try (isKindOf.set(EntityKind.DataRole))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(e.C.accept(this));
			sb.append("⋐");
			sb.append(Modality(e.modality));
			sb.append(e.D.accept(this));
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleEquivalence e)
	{
		try (isKindOf.set(EntityKind.DataRole))
		{
			StringBuilder sb = new StringBuilder();
			if (e.Equivalents.size() == 2)
			{
				sb.append(e.Equivalents.get(0).accept(this));
				sb.append("≣");
				sb.append(e.Equivalents.get(1).accept(this));
			}
			else
			{
				sb.append("≣(");
				boolean first = true;
				for (Node x : e.Equivalents)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append(x.accept(this));
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleDisjoint e)
	{
		try (isKindOf.set(EntityKind.DataRole))
		{
			StringBuilder sb = new StringBuilder();
			if (e.Disjoints.size() == 2)
			{
				sb.append(e.Disjoints.get(0).accept(this));
				sb.append("⋐￢");
				sb.append(e.Disjoints.get(1).accept(this));
			}
			else
			{
				sb.append("￢≣(");
				boolean first = true;
				for (Node x : e.Disjoints)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append(x.accept(this));
				}
				sb.append(")");
			}
			return sb.toString();
		}
	}

	public final Object Visit(cognipy.cnl.dl.InstanceOf e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.C.accept(this));
		sb.append(Modality(e.modality));
		sb.append("(");
		sb.append(e.I.accept(this));
		if (e.I instanceof NamedInstance)
		{
			signature.add(Tuple.Create(EntityKind.Instance, (e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.RelatedInstances e)
	{
		StringBuilder sb = new StringBuilder();
		try (isKindOf.set(EntityKind.Role))
		{
			sb.append(e.R.accept(this));
			sb.append(Modality(e.modality));
		}
		sb.append("(");
		sb.append(e.I.accept(this));
		if (e.I instanceof NamedInstance)
		{
			signature.add(Tuple.Create(EntityKind.Instance, (e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name));
		}
		sb.append(",");
		sb.append(e.J.accept(this));
		if (e.J instanceof NamedInstance)
		{
			signature.add(Tuple.Create(EntityKind.Instance, (e.J instanceof NamedInstance ? (NamedInstance)e.J : null).name));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.NamedInstance e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.name);
		signature.add(Tuple.Create(EntityKind.Instance, e.name));
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.UnnamedInstance e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		if (e.Only)
		{
			sb.append("[");
		}
		String conceptExpr;
		try (isKindOf.set(EntityKind.Concept))
		{
			conceptExpr = (String)e.C.accept(this);
		}
		sb.append(conceptExpr);
		if (e.Only)
		{
			sb.append("]");
		}
		sb.append("]");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.InstanceValue e)
	{
		instanceValues.add(e);
		StringBuilder sb = new StringBuilder();
		try (isKindOf.set(EntityKind.DataRole))
		{
			sb.append(e.R.accept(this));
		}
		sb.append(Modality(e.modality));
		sb.append("(");
		sb.append(e.I.accept(this));
		if (e.I instanceof NamedInstance)
		{
			signature.add(Tuple.Create(EntityKind.Instance, (e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name));
		}
		sb.append(",");
		try (attributeName.set(null))
		{
			sb.append(e.V.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SameInstances e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("=");
		sb.append(Modality(e.modality));
		sb.append("{");
		boolean first = true;
		for (Instance I : e.Instances)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(",");
			}
			sb.append(I.accept(this));
		}
		sb.append("}");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.DifferentInstances e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("≠");
		sb.append(Modality(e.modality));
		sb.append("{");
		boolean first = true;
		for (Instance I : e.Instances)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(",");
			}
			sb.append(I.accept(this));
		}
		sb.append("}");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.HasKey e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.C.accept(this));
		sb.append("○⊑");
		if (!e.Roles.isEmpty())
		{
			try (isKindOf.set(EntityKind.Role))
			{
				sb.append("(");
				boolean first = true;
				for (Node x : e.Roles)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append(x.accept(this));
				}
				sb.append(")");
			}
		}
		if (!e.DataRoles.isEmpty())
		{
			sb.append("⊓");
			try (isKindOf.set(EntityKind.DataRole))
			{
				sb.append("(");
				boolean first = true;
				for (Node x : e.DataRoles)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						sb.append(",");
					}
					sb.append(x.accept(this));
				}
				sb.append(")");
			}
		}
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.Number e)
	{
		String sval = e.val.toString();
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
		}
		if (!foundValues.containsKey(e.getTypeTag() + ":" + sval))
		{
			foundValues.put(e.getTypeTag() + ":" + sval, e);
		}
		if (DeltaKind.equals("<") || DeltaKind.equals("≠"))
		{
			String sval2 = (String.valueOf(Integer.parseInt(sval));
			if (attributeName.get() != null)
			{
				dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval2));
			}
			if (!foundValues.containsKey(e.getTypeTag() + ":" + sval2))
			{
				foundValues.put(e.getTypeTag() + ":" + sval2, e);
			}
		}
		if (DeltaKind.equals(">") || DeltaKind.equals("≠"))
		{
			String sval2 = (String.valueOf(Integer.parseInt(sval));
			if (attributeName.get() != null)
			{
				dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval2));
			}
			if (!foundValues.containsKey(e.getTypeTag() + ":" + sval2))
			{
				foundValues.put(e.getTypeTag() + ":" + sval2, e);
			}
		}
		return sval;
	}
	public final Object Visit(cognipy.cnl.dl.String e)
	{
		String sval = e.val;
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
		}
		if (!foundValues.containsKey(e.getTypeTag() + ":" + sval))
		{
			foundValues.put(e.getTypeTag() + ":" + sval, e);
		}
		return sval;
	}
	public final Object Visit(cognipy.cnl.dl.Float e)
	{
		String sval = e.val.toString();
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
		}
		if (!foundValues.containsKey(e.getTypeTag() + ":" + sval))
		{
			foundValues.put(e.getTypeTag() + ":" + sval, e);
		}
		return sval;
	}
	public final Object Visit(cognipy.cnl.dl.Bool e)
	{
		String sval = e.val.toString();
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
		}
		if (!foundValues.containsKey(e.getTypeTag() + ":" + sval))
		{
			foundValues.put(e.getTypeTag() + ":" + sval, e);
		}
		return sval;
	}
	public final Object Visit(cognipy.cnl.dl.DateTimeVal e)
	{
		String sval = e.val.toString();
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
		}
		if (!foundValues.containsKey(e.getTypeTag() + ":" + sval))
		{
			foundValues.put(e.getTypeTag() + ":" + sval, e);
		}
		return sval;
	}
	public final Object Visit(Duration e)
	{
		String sval = e.val.toString();
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
		}
		if (!foundValues.containsKey(e.getTypeTag() + ":" + sval))
		{
			foundValues.put(e.getTypeTag() + ":" + sval, e);
		}
		return sval;
	}

	private String DeltaKind = null;
	public final Object Visit(Facet e)
	{
		DeltaKind = e.Kind;
		String r = e.Kind + e.V.accept(this);
		DeltaKind = null;
		return r;
	}

	public final Object Visit(FacetList e)
	{
		boolean multi = e.List.size() > 1;
		StringBuilder sb = new StringBuilder();

		if (multi)
		{
			sb.append("(");
		}
		boolean first = true;
		for (Facet f : e.List)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(", ");
			}
			sb.append(f.accept(this));
		}
		if (multi)
		{
			sb.append(")");
		}
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.BoundFacets e)
	{
		return e.FL.accept(this);
	}

	public final Object Visit(BoundOr e)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (AbstractBound B : e.List)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append("⊔");
			}
			sb.append(brack(e, B));
		}
		return sb.toString();
	}

	public final Object Visit(BoundAnd e)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (AbstractBound B : e.List)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append("⊓");
			}
			sb.append(brack(e, B));
		}
		return sb.toString();
	}

	public final Object Visit(BoundNot e)
	{
		return "￢" + brack(e, e.B);
	}

	public final Object Visit(BoundVal e)
	{
		return e.Kind + e.V.accept(this);
	}

	public final Object Visit(cognipy.cnl.dl.TotalBound e)
	{
		try (attributeName.set(null))
		{
			return "≤⊔≥" + e.V.accept(this);
		}
	}

	public final Object Visit(DTBound e)
	{
		signature.add(Tuple.Create(EntityKind.DataType, e.name));
		if (attributeName.get() != null)
		{
			dataValues.add(Tuple.Create("", attributeName.get(), "\r" + e.name));
		}
		return "≤⊔≥" + e.name;
	}

	public final Object Visit(cognipy.cnl.dl.TopBound e)
	{
		return "⊤";
	}

	public final Object Visit(cognipy.cnl.dl.ValueSet e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		boolean first = true;
		for (Value n : e.Values)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(",");
			}
			sb.append(n.accept(this));
		}
		sb.append("}");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.Atomic e)
	{
		signature.add(Tuple.Create(isKindOf.get(), e.id));
		return e.id;
	}
	public final Object Visit(cognipy.cnl.dl.Top e)
	{
		return "⊤";
	}
	public final Object Visit(cognipy.cnl.dl.Bottom e)
	{
		return "⊥";
	}
	public final Object Visit(cognipy.cnl.dl.RoleInversion e)
	{
		try (isKindOf.set(EntityKind.Role))
		{
			return brack(e, e.R) + "⁻";
		}
	}
	public final Object Visit(cognipy.cnl.dl.InstanceSet e)
	{
		try (isKindOf.set(EntityKind.Instance))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("{");
			boolean first = true;
			for (Instance I : e.Instances)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(",");
				}
				sb.append(I.accept(this));
			}
			sb.append("}");
			return sb.toString();
		}
	}
	public final Object Visit(cognipy.cnl.dl.ConceptOr e)
	{
		try (isKindOf.set(EntityKind.Concept))
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Node C : e.Exprs)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append("⊔");
				}
				sb.append(brack(e, C));
			}
			return sb.toString();
		}
	}
	public final Object Visit(cognipy.cnl.dl.ConceptAnd e)
	{
		try (isKindOf.set(EntityKind.Concept))
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (Node C : e.Exprs)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append("⊓");
				}
				sb.append(brack(e, C));
			}
			return sb.toString();
		}
	}
	public final Object Visit(cognipy.cnl.dl.ConceptNot e)
	{
		try (isKindOf.set(EntityKind.Concept))
		{
			return "￢" + brack(e, e.C);
		}
	}
	public final Object Visit(cognipy.cnl.dl.OnlyRestriction e)
	{
		StringBuilder sb = new StringBuilder();
		try (isKindOf.set(EntityKind.Role))
		{
			sb.append("∀" + brack(e, e.R) + ".");
		}
		try (isKindOf.set(EntityKind.Concept))
		{
			sb.append(brack(e, e.C));
		}
		return sb.toString();
	}
	public final Object Visit(cognipy.cnl.dl.SomeRestriction e)
	{
		StringBuilder sb = new StringBuilder();
		try (isKindOf.set(EntityKind.Role))
		{
			sb.append("∃" + brack(e, e.R) + ".");
		}
		try (isKindOf.set(EntityKind.Concept))
		{
			sb.append(brack(e, e.C));
		}
		return sb.toString();
	}
	public final Object Visit(cognipy.cnl.dl.OnlyValueRestriction e)
	{
		StringBuilder sb = new StringBuilder();
		String attrName;
		try (isKindOf.set(EntityKind.DataRole))
		{
			attrName = (String)e.R.accept(this);
			sb.append("∀" + brack(e, e.R));
		}
		try (attributeName.set(attrName))
		{
			sb.append(e.B.accept(this));
		}
		return sb.toString();
	}
	public final Object Visit(cognipy.cnl.dl.SomeValueRestriction e)
	{
		StringBuilder sb = new StringBuilder();
		String attrName;
		try (isKindOf.set(EntityKind.DataRole))
		{
			attrName = (String)e.R.accept(this);
			sb.append("∃" + brack(e, e.R));
		}
		try (attributeName.set(attrName))
		{
			sb.append(e.B.accept(this));
		}
		return sb.toString();
	}
	public final Object Visit(cognipy.cnl.dl.SelfReference e)
	{
		try (isKindOf.set(EntityKind.Role))
		{
			return "∃" + brack(e, e.R) + ".○";
		}
	}
	public final Object Visit(cognipy.cnl.dl.NumberRestriction e)
	{
		StringBuilder sb = new StringBuilder();
		try (isKindOf.set(EntityKind.Role))
		{
			sb.append(e.Kind + e.N.toString() + " " + brack(e, e.R) + ".");
		}
		try (isKindOf.set(EntityKind.Concept))
		{
			sb.append(brack(e, e.C));
		}
		return sb.toString();
	}
	public final Object Visit(cognipy.cnl.dl.NumberValueRestriction e)
	{
		StringBuilder sb = new StringBuilder();
		String attrName;
		try (isKindOf.set(EntityKind.DataRole))
		{
			attrName = (String)e.R.accept(this);
			sb.append(e.Kind + e.N.toString() + " " + brack(e, e.R));
		}
		try (attributeName.set(attrName))
		{
			sb.append(e.B.accept(this));
		}
		return sb.toString();
	}


	public final Object Visit(Annotation a)
	{
		return a.txt;
	}

	public final Object Visit(DLAnnotationAxiom a)
	{
		signature.add(Tuple.Create(EntityKind.Annotation, a.annotName));
		cognipy.ars.EntityKind result = cognipy.cnl.AnnotationManager.ParseSubjectKind(a.getSubjKind());
		if (result != EntityKind.Statement)
		{
			signature.add(Tuple.Create(result, a.getSubject()));
		}
		// in case the annotation is a statement, the subject is not added to the signature. We could do this but we would need to parse the statement and it could be time consuming.
		// TODO [Annotations on Statements] is it needed to add to the signature the entities that are inside the statement?

		String val = a.value;
		// if the subject is null we are probably dealing with a non-standard rdf --> return null
		if (tangible.StringHelper.isNullOrWhiteSpace(a.getSubject()) || tangible.StringHelper.isNullOrWhiteSpace(a.getSubjKind()))
		{
			return null;
		}
		String subj = a.getSubject().trim();
		if (result == EntityKind.Statement)
		{
			// !! at this point the statement subject is expected NOT inside quotes!! If it is inside quotes, somewhere it was incorrectly constructed.
			subj = "\"" + subj.replace("\"", "''") + "\"";
		}


		String kind = a.getSubjKind().trim();
		String annotName = a.annotName.trim();
		String lang = "";
		if (!tangible.StringHelper.isNullOrWhiteSpace(a.language))
		{
			lang = a.language.trim();
		}

		if (!tangible.StringHelper.isNullOrWhiteSpace(val))
		{
			if (!val.startsWith("'"))
			{
				val = "'" + val;
			}
			if (!val.endsWith("'"))
			{
				val += "'";
			}
		}
		else
		{
			val += "''";
		}

		String retur = "# " + subj + " " + kind + " " + annotName + " " + lang + " " + val;
		return retur;
	}

	public final Object Visit(cognipy.cnl.dl.SwrlStatement e)
	{
		swrlAttrsBody = new HashSet<String>();
		swrlAttrsHead = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("⌂");
		try (swrlCurAttr.set(swrlAttrsBody))
		{
			sb.append(e.slp.accept(this));
		}
		sb.append(" ");
		sb.append(Modality(e.modality));
		sb.append(" →");
		try (swrlCurAttr.set(swrlAttrsHead))
		{
			sb.append(e.slc.accept(this));
		}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var b : swrlAttrsBody)
		{
			if (!dependentAttrs.containsKey(b))
			{
				dependentAttrs.put(b, new HashSet<String>());
			}
			dependentAttrs.get(b).UnionWith(swrlAttrsHead);
		}
		return sb.toString();
	}

	public final Object Visit(SwrlIterate e)
	{
		swrlAttrsBody = new HashSet<String>();
		swrlAttrsHead = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("⌂");
		try (swrlCurAttr.set(swrlAttrsBody))
		{
			sb.append(e.slp.accept(this));
		}
		sb.append("→→");
		try (swrlCurAttr.set(swrlAttrsHead))
		{
			sb.append(e.slc.accept(this));
		}

		sb.append("(");
		try (swrlCurAttr.set(swrlAttrsHead))
		{
			sb.append(e.vars.accept(this));
		}
		sb.append(")");

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var b : swrlAttrsBody)
		{
			if (!dependentAttrs.containsKey(b))
			{
				dependentAttrs.put(b, new HashSet<String>());
			}
			dependentAttrs.get(b).UnionWith(swrlAttrsHead);
		}
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SwrlItemList e)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (SwrlItem I : e.list)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append("⋀");
			}
			sb.append(I.accept(this));
		}
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SwrlInstance e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("○");
		try (isKindOf.set(EntityKind.Concept))
		{
			sb.append(e.C.accept(this));
		}
		sb.append("(");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.I.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SwrlRole e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.R);
		signature.add(Tuple.Create(EntityKind.Role, e.R));
		sb.append("(");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.I.accept(this));
		}
		sb.append(",");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.J.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SwrlSameAs e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("=(");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.I.accept(this));
		}
		sb.append(",");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.J.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}


	public final Object Visit(cognipy.cnl.dl.SwrlDifferentFrom e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("≠(");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.I.accept(this));
		}
		sb.append(",");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.J.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SwrlDataProperty e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("°");
		String attrName = e.R;
		swrlCurAttr.get().add(attrName);
		signature.add(Tuple.Create(EntityKind.DataRole, attrName));
		sb.append(attrName);
		sb.append("(");
		try (isKindOf.set(EntityKind.Instance))
		{
			sb.append(e.IO.accept(this));
		}
		sb.append(",");
		try (attributeName.set(attrName))
		{
			sb.append(e.DO.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(cognipy.cnl.dl.SwrlDataRange e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("°");
		sb.append(e.B.accept(this));
		sb.append("(");
		sb.append(e.DO.accept(this));
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(SwrlBuiltIn e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("° ");
		sb.append(":");
		sb.append(e.builtInName);
		sb.append(" ");
		sb.append("(");
		for (int i = 0; i < e.Values.size(); i++)
		{
			if (i > 0)
			{
				sb.append(" , ");
			}
			sb.append(e.Values.get(i).accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(ExeStatement e)
	{
		swrlAttrsBody = new HashSet<String>();
		swrlAttrsHead = new HashSet<String>();
		StringBuilder sb = new StringBuilder();
		sb.append("⌂");
		try (swrlCurAttr.set(swrlAttrsBody))
		{
			sb.append(e.slp.accept(this));
		}
		sb.append("~→");
		sb.append(e.exe);
		sb.append("(");
		try (swrlCurAttr.set(swrlAttrsHead))
		{
			sb.append(e.args.accept(this));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(CodeStatement e)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(e.exe);
		return sb.toString();
	}

	public Object Visit(SwrlVarList e)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (IExeVar I : e.list)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(", ");
			}
			sb.append(I.accept(this));
		}
		return sb.toString();
	}

	public final Object Visit(SwrlDVal e)
	{
		return e.Val.accept(this).toString();
	}

	public Object Visit(SwrlDVar e)
	{
		return "?:" + e.VAR;
	}

	public final Object Visit(SwrlIVal e)
	{
		signature.add(Tuple.Create(EntityKind.Instance, e.I));
		return e.I;
	}

	public Object Visit(SwrlIVar e)
	{
		return "?" + e.VAR;
	}




}