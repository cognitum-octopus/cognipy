package cognipy.cnl.en;

import cognipy.cnl.dl.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class InvTransform implements cognipy.cnl.en.IVisitor
{
	public enum EntityKind
	{
		Concept,
		AnyRole,
		DataRole,
		DataType,
		Instance,
		Annotation;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static EntityKind forValue(int value)
		{
			return values()[value];
		}
	}

	private endict.WordKind markerForm = endict.WordKind.NormalForm;
	private EntityKind markerKind = EntityKind.Concept;
	private String marker = null;
	private String ToDL(String name, cognipy.ars.EntityKind kind, endict.WordKind form)
	{
		EntityKind newKind;
		switch (kind)
		{
			case Concept:
				newKind = EntityKind.Concept;
				break;
			case DataRole:
				newKind = EntityKind.DataRole;
				break;
			case Role:
				newKind = EntityKind.AnyRole;
				break;
			case DataType:
				newKind = EntityKind.DataType;
				break;
			case SWRLVariable:
				throw new RuntimeException("Cannot translate from SWRLVariable to EN");
			case Instance:
				newKind = EntityKind.Instance;
				break;
			case Annotation:
				newKind = EntityKind.Annotation;
				break;
			default:
				throw new RuntimeException("Don't know what to do with " + kind.toString());
		}

		return ToDL(name, newKind, form);
	}

	private String ToDL(String name, EntityKind kind, endict.WordKind form)
	{
		if (marker != null && marker.equals(name))
		{
			markerKind = (kind == EntityKind.AnyRole) ? (isDataRoleStatement.get() ? EntityKind.DataRole : EntityKind.AnyRole) : kind;
			markerForm = form;
		}

		EnName tempVar = new EnName();
		tempVar.id = name;
		cognipy.cnl.en.EnName.Parts allParts = (tempVar).Split();
		if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && !allParts.term.contains("<") && _useFullUri)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var tterm = pfx2Ns(allParts.term);
			if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
			{
				allParts.term = "<" + tterm + ">";
			}
			else
			{
				throw new RuntimeException("No namespace found for prefix " + allParts.term + ". You need to define it before saving into Ontorion.");
			}
		}

		return ENNameingConvention.ToDL(allParts.Combine(), form).id;
	}
	public final EntityKind GetMarkerKind()
	{
		return markerKind;
	}
	public final endict.WordKind GetMarkerForm()
	{
		return markerForm;
	}

	private VisitingParam<Boolean> isModal = new VisitingParam<Boolean>(false);
	private VisitingParam<Boolean> isPlural = new VisitingParam<Boolean>(false);
	private VisitingParam<Boolean> isDataRoleStatement = new VisitingParam<Boolean>(false);
	private VisitingParam<CNL.DL.Node> roleNode = new VisitingParam<CNL.DL.Node>(null);

	// This is used from EN TO DL --> pfx can be a namespace inside <>, the namespace returned should be in <>
	private String defPfx2Ns(String pfx)
	{
		if (tangible.StringHelper.isNullOrWhiteSpace(pfx))
		{
			throw new RuntimeException("No default namespace is defined.");
		}

		if (pfx.startsWith("<") && pfx.endsWith(">"))
		{
			String argg = pfx.substring(1, 1 + pfx.length() - 2);
			if (!argg.endsWith("/") && !argg.endsWith("#") && !argg.contains("#"))
			{
				argg += "#";
			}
			return "<" + argg + ">";
		}
		else
		{
			throw new RuntimeException("No namespace defined for prefix: " + pfx);
		}
	}

	private boolean _useFullUri = false;
	private tangible.Func1Param<String, String> _pfx2Ns = null;
	private tangible.Func1Param<String, String> getPfx2Ns()
	{
		if (_pfx2Ns == null)
		{
			return (String arg) -> defPfx2Ns(arg);
		}
		else
		{
			return _pfx2Ns;
		}
	}

	public InvTransform()
	{
	}
	public InvTransform(String marker)
	{
		this.marker = marker;
	}


	public final cognipy.cnl.dl.Paragraph Convert(cognipy.cnl.en.paragraph p, boolean useFullUri)
	{
		return Convert(p, useFullUri, null);
	}

	public final cognipy.cnl.dl.Paragraph Convert(cognipy.cnl.en.paragraph p)
	{
		return Convert(p, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Paragraph Convert(CogniPy.CNL.EN.paragraph p, bool useFullUri = false, Func<string, string> pfx2Ns = null)
	public final cognipy.cnl.dl.Paragraph Convert(cognipy.cnl.en.paragraph p, boolean useFullUri, tangible.Func1Param<String, String> pfx2Ns)
	{
		_useFullUri = useFullUri;
		_pfx2Ns = (String arg) -> pfx2Ns.invoke(arg);
		Object tempVar = p.accept(this);
		return tempVar instanceof cognipy.cnl.dl.Paragraph ? (cognipy.cnl.dl.Paragraph)tempVar : null;
	}
	public final cognipy.cnl.dl.Statement Convert(cognipy.cnl.en.sentence s)
	{
		Object tempVar = s.accept(this);
		return tempVar instanceof cognipy.cnl.dl.Statement ? (cognipy.cnl.dl.Statement)tempVar : null;
	}

	public final Object Visit(cognipy.cnl.en.paragraph p)
	{
		cognipy.cnl.dl.Paragraph ret = new CNL.DL.Paragraph(null);
		ret.Statements = new ArrayList<CNL.DL.Statement>();
		for (sentence x : p.sentences)
		{
			Object tempVar = x.accept(this);
			ret.Statements.add(tempVar instanceof CNL.DL.Statement ? (CNL.DL.Statement)tempVar : null);
		}
		return ret;
	}

	public final cognipy.cnl.dl.Statement.Modality Modality(String tok)
	{
		switch (tok)
		{
			case "□":
				return Statement.Modality.MUST;
			case "◊":
				return Statement.Modality.SHOULD;
			case "◊◊":
				return Statement.Modality.CAN;
			case "~◊◊":
				return Statement.Modality.MUSTNOT;
			case "~◊":
				return Statement.Modality.SHOULDNOT;
			case "~□":
				return Statement.Modality.CANNOT;
			default:
				return Statement.Modality.IS;
		}
	}

	public final Object Visit(cognipy.cnl.en.subsumption p)
	{
		cognipy.cnl.dl.Subsumption ret = new CNL.DL.Subsumption(null);

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
		Object tempVar = p.c.accept(this);
		ret.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
		try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
		{
			Object tempVar2 = p.d.accept(this);
			ret.D = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
		}
		ret.modality = modal;
		return ret;
	}

	public final Object Visit(cognipy.cnl.en.nosubsumption p)
	{
		cognipy.cnl.dl.Subsumption ret = new CNL.DL.Subsumption(null);

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
		Object tempVar = p.c.accept(this);
		ret.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
		try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
		{
			Object tempVar2 = p.d.accept(this);
			ret.D = new cognipy.cnl.dl.ConceptNot(null);
			ret.D.C = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
		}
		ret.modality = modal;

		return ret;
	}

	public final Object Visit(cognipy.cnl.en.subsumption_if p)
	{
		cognipy.cnl.dl.Subsumption ret = new CNL.DL.Subsumption(null);

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
		Object tempVar = p.c.accept(this);
		ret.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
		try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
		{
			Object tempVar2 = p.d.accept(this);
			ret.D = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
		}
		ret.modality = modal;
		return ret;
	}

	public final Object Visit(datatypedef p)
	{
		Object tempVar = p.db.accept(this);
		CNL.DL.ID tempVar2 = new CNL.DL.ID(null);
		tempVar2.setyytext(ToDL(p.name, EntityKind.DataType, endict.WordKind.NormalForm));
		cognipy.cnl.dl.DataTypeDefinition ret = new DataTypeDefinition(null, tempVar2, tempVar instanceof AbstractBound ? (AbstractBound)tempVar : null);
		return ret;
	}

	public final Object Visit(cognipy.cnl.en.equivalence2 p)
	{
		cognipy.cnl.dl.Equivalence ret = new CNL.DL.Equivalence(null);
		ret.Equivalents = new ArrayList<Node>();

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
		Object tempVar = p.c.accept(this);
		ret.Equivalents.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
		try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
		{
			Object tempVar2 = p.d.accept(this);
			ret.Equivalents.add(tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);
		}
		ret.modality = modal;
		return ret;
	}
	//public object Visit(Ontorion.CNL.EN.equivalence_def p)
	//{
	//    Ontorion.CNL.DL.Equivalence ret = new CNL.DL.Equivalence(null) { Equivalents = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    ret.Equivalents.Add(p.c.accept(this) as CNL.DL.Node);
	//    ret.Equivalents.Add(p.d.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}
	//public object Visit(Ontorion.CNL.EN.disjoint p)
	//{
	//    Ontorion.CNL.DL.Disjoint ret = new CNL.DL.Disjoint(null) { Disjoints = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    foreach (var e in p.different)
	//        ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}

	public final Object Visit(cognipy.cnl.en.exclusives p)
	{
		cognipy.cnl.dl.Disjoint ret = new CNL.DL.Disjoint(null);
		ret.Disjoints = new ArrayList<Node>();

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
		try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
		{

			for (objectRoleExpr e : p.objectRoleExprs)
			{
				Object tempVar = e.accept(this);
				ret.Disjoints.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
			}
		}

		ret.modality = modal;
		return ret;
	}


	//public object Visit(Ontorion.CNL.EN.disjointunion p)
	//{
	//    Ontorion.CNL.DL.DisjointUnion ret = new CNL.DL.DisjointUnion(null) { Union = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    ret.name = ToDL(p.name, EntityKind.Concept, endict.WordKind.NormalForm);

	//    foreach (var e in p.union)
	//        ret.Union.Add(e.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}

	public final Object Visit(cognipy.cnl.en.exclusiveunion p)
	{
		cognipy.cnl.dl.DisjointUnion ret = new CNL.DL.DisjointUnion(null);
		ret.Union = new ArrayList<Node>();

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);

		ret.name = ToDL(p.name, EntityKind.Concept, endict.WordKind.NormalForm);

		for (objectRoleExpr e : p.objectRoleExprs)
		{
			Object tempVar = e.accept(this);
			ret.Union.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
		}

		ret.modality = modal;
		return ret;
	}

	public final Object Visit(rolesubsumption p)
	{
		if (p.subChain.size() == 1)
		{
			cognipy.cnl.dl.RoleInclusion ret = new RoleInclusion(null);
			cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
			Object tempVar = p.subChain.get(0).accept(this);
			ret.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
			{
				Object tempVar2 = p.superRole.accept(this);
				ret.D = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
			}

			ret.modality = modal;
			return ret;
		}
		else if (p.subChain.size() > 1)
		{
			cognipy.cnl.dl.ComplexRoleInclusion ret = new CNL.DL.ComplexRoleInclusion(null);
			ret.RoleChain = new ArrayList<Node>();
			cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);

			for (role x : p.subChain)
			{
				Object tempVar3 = x.accept(this);
				ret.RoleChain.add(tempVar3 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar3 : null);
			}

			try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
			{
				Object tempVar4 = p.superRole.accept(this);
				ret.R = tempVar4 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar4 : null;
			}

			ret.modality = modal;
			return ret;
		}
		else
		{
			Assert(false);
			return null;
		}
	}
	//public object Visit(Ontorion.CNL.EN.roleequivalence p)
	//{
	//    Ontorion.CNL.DL.RoleEquivalence ret = new CNL.DL.RoleEquivalence(null) { Equivalents = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    foreach (var e in p.equals)
	//        ret.Equivalents.Add(e.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}

	public final Object Visit(cognipy.cnl.en.roleequivalence2 p)
	{
		cognipy.cnl.dl.RoleEquivalence ret = new CNL.DL.RoleEquivalence(null);
		ret.Equivalents = new ArrayList<Node>();

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);

		Object tempVar = p.r.accept(this);
		ret.Equivalents.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
		Object tempVar2 = p.s.accept(this);
		ret.Equivalents.add(tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);

		ret.modality = modal;
		return ret;
	}

	//public object Visit(Ontorion.CNL.EN.roledisjoint p)
	//{
	//    Ontorion.CNL.DL.RoleDisjoint ret = new CNL.DL.RoleDisjoint(null) { Disjoints = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    foreach (var e in p.different)
	//        ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}

	public final Object Visit(cognipy.cnl.en.roledisjoint2 p)
	{
		cognipy.cnl.dl.RoleDisjoint ret = new CNL.DL.RoleDisjoint(null);
		ret.Disjoints = new ArrayList<Node>();

		cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);

		Object tempVar = p.r.accept(this);
		ret.Disjoints.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
		Object tempVar2 = p.s.accept(this);
		ret.Disjoints.add(tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);

		ret.modality = modal;
		return ret;
	}

	public final Object Visit(datarolesubsumption p)
	{
		try (isDataRoleStatement.set(true))
		{
			cognipy.cnl.dl.DataRoleInclusion ret = new DataRoleInclusion(null);
			cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);
			Object tempVar = p.subRole.accept(this);
			ret.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			try (isModal.set(modal != cognipy.cnl.dl.Statement.Modality.IS))
			{
				Object tempVar2 = p.superRole.accept(this);
				ret.D = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
			}

			ret.modality = modal;
			return ret;
		}
	}

	//public object Visit(Ontorion.CNL.EN.dataroleequivalence p)
	//{
	//    Ontorion.CNL.DL.DataRoleEquivalence ret = new CNL.DL.DataRoleEquivalence(null) { Equivalents = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    foreach (var e in p.equals)
	//        ret.Equivalents.Add(e.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}

	public final Object Visit(cognipy.cnl.en.dataroleequivalence2 p)
	{
		try (isDataRoleStatement.set(true))
		{
			cognipy.cnl.dl.DataRoleEquivalence ret = new CNL.DL.DataRoleEquivalence(null);
			ret.Equivalents = new ArrayList<Node>();

			Object tempVar = p.r.accept(this);
			ret.Equivalents.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
			Object tempVar2 = p.s.accept(this);
			ret.Equivalents.add(tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);

			return ret;
		}
	}
	//public object Visit(Ontorion.CNL.EN.dataroledisjoint p)
	//{
	//    Ontorion.CNL.DL.DataRoleDisjoint ret = new CNL.DL.DataRoleDisjoint(null) { Disjoints = new List<Node>() };

	//    Ontorion.CNL.DL.Statement.Modality modal = Modality(p.modality);

	//    foreach (var e in p.different)
	//        ret.Disjoints.Add(e.accept(this) as CNL.DL.Node);

	//    ret.modality = modal;
	//    return ret;
	//}
	public final Object Visit(cognipy.cnl.en.dataroledisjoint2 p)
	{
		try (isDataRoleStatement.set(true))
		{
			cognipy.cnl.dl.DataRoleDisjoint ret = new CNL.DL.DataRoleDisjoint(null);
			ret.Disjoints = new ArrayList<Node>();

			cognipy.cnl.dl.Statement.Modality modal = Modality(p.modality);

			Object tempVar = p.r.accept(this);
			ret.Disjoints.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
			try (isModal.set(true))
			{
				Object tempVar2 = p.s.accept(this);
				ret.Disjoints.add(tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);
			}

			ret.modality = modal;
			return ret;
		}
	}

	public final Object Visit(cognipy.cnl.en.haskey p)
	{
		cognipy.cnl.dl.HasKey ret = new CNL.DL.HasKey(null);
		ret.Roles = new ArrayList<Node>();
		ret.DataRoles = new ArrayList<Node>();

		for (role e : p.roles)
		{
			Object tempVar = e.accept(this);
			ret.Roles.add(tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
		}
		try (isDataRoleStatement.set(true))
		{
			for (role e : p.dataroles)
			{
				Object tempVar2 = e.accept(this);
				ret.DataRoles.add(tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);
			}
		}
		Object tempVar3 = p.s.accept(this);
		ret.C = tempVar3 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar3 : null;

		return ret;
	}

	public final Object Visit(cognipy.cnl.en.subjectEvery p)
	{
		return p.s.accept(this);
	}
	public final Object Visit(cognipy.cnl.en.subjectEverything p)
	{
		return p.t != null ? p.t.accept(this) : new cognipy.cnl.dl.Top(null);
	}

	//go into nosubsumptions
	public final Object Visit(cognipy.cnl.en.subjectNo p)
	{
		return p.s.accept(this);
	}
	public final Object Visit(subjectNothing p)
	{
		return new cognipy.cnl.dl.Top(null);
	}


	private cognipy.cnl.dl.Instance BigName(String name)
	{
		CNL.DL.NamedInstance tempVar = new CNL.DL.NamedInstance(null);
		tempVar.name = ToDL(name, EntityKind.Instance, endict.WordKind.NormalForm);
		return tempVar;
	}

	private cognipy.cnl.dl.Instance Unnamed(boolean only, single s)
	{
		Object tempVar = s.accept(this);
		CNL.DL.UnnamedInstance tempVar2 = new CNL.DL.UnnamedInstance(null);
		tempVar2.Only = only;
		tempVar2.C = tempVar instanceof cognipy.cnl.dl.Node ? (cognipy.cnl.dl.Node)tempVar : null;
		return tempVar2;
	}

	public final Object Visit(subjectBigName p)
	{
		return new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, BigName(p.name)));
	}
	public final Object Visit(subjectThe p)
	{
		return new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, Unnamed(p.only, p.s)));
	}

	//public object Visit(defObjectRoleExpr1 p)
	//{
	//    using (roleNode.set(null))
	//    {
	//        if (p.Negated)
	//            return new Ontorion.CNL.DL.ConceptNot(null) { C = p.s.accept(this) as Ontorion.CNL.DL.Node };
	//        else
	//            return p.s.accept(this);
	//    }
	//}
	public final Object Visit(objectRoleExpr1 p)
	{
		try (roleNode.set(null))
		{
			if (p.Negated)
			{
				Object tempVar = p.s.accept(this);
				cognipy.cnl.dl.ConceptNot tempVar2 = new cognipy.cnl.dl.ConceptNot(null);
				tempVar2.C = tempVar instanceof cognipy.cnl.dl.Node ? (cognipy.cnl.dl.Node)tempVar : null;
				return tempVar2;
			}
			else
			{
				return p.s.accept(this);
			}
		}
	}

	public final Object Visit(roleWithXY p)
	{
		if (p.name.equals(ENNameingConvention.TOPROLENAME))
		{
			CNL.DL.Node n = new CNL.DL.Top(null);
			if (p.inverse)
			{
				n = new CNL.DL.RoleInversion(null);
				n.R = n;
			}
			return n;
		}
		else if (p.name.equals(ENNameingConvention.BOTTOMROLENAME))
		{
			CNL.DL.Node n = new CNL.DL.Bottom(null);
			if (p.inverse)
			{
				n = new CNL.DL.RoleInversion(null);
				n.R = n;
			}
			return n;
		}
		else
		{
			if (!p.inverse)
			{
				CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
				tempVar.id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
				return tempVar;
			}
			else
			{
				CNL.DL.RoleInversion tempVar2 = new CNL.DL.RoleInversion(null);
				tempVar2.R = new CNL.DL.Atomic(null);
				tempVar2.R.id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
				return tempVar2;
			}
		}
	}

	public final Object Visit(notRoleWithXY p)
	{
		if (p.name.equals(ENNameingConvention.TOPROLENAME))
		{
			CNL.DL.Node n = new CNL.DL.Top(null);
			if (p.inverse)
			{
				n = new CNL.DL.RoleInversion(null);
				n.R = n;
			}
			return n;
		}
		else if (p.name.equals(ENNameingConvention.BOTTOMROLENAME))
		{
			CNL.DL.Node n = new CNL.DL.Bottom(null);
			if (p.inverse)
			{
				n = new CNL.DL.RoleInversion(null);
				n.R = n;
			}
			return n;
		}
		else
		{
			if (!p.inverse)
			{
				CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
				tempVar.id = ToDL(p.name, EntityKind.AnyRole, endict.WordKind.PastParticiple);
				return tempVar;
			}
			else
			{
				CNL.DL.RoleInversion tempVar2 = new CNL.DL.RoleInversion(null);
				tempVar2.R = new CNL.DL.Atomic(null);
				tempVar2.R.id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
				return tempVar2;
			}
		}
	}

	public final Object Visit(role p)
	{
		if (p.name.equals(ENNameingConvention.TOPROLENAME))
		{
			CNL.DL.Node n = new CNL.DL.Top(null);
			if (p.inverse)
			{
				n = new CNL.DL.RoleInversion(null);
				n.R = n;
			}
			return n;
		}
		else if (p.name.equals(ENNameingConvention.BOTTOMROLENAME))
		{
			CNL.DL.Node n = new CNL.DL.Bottom(null);
			if (p.inverse)
			{
				n = new CNL.DL.RoleInversion(null);
				n.R = n;
			}
			return n;
		}
		else
		{
			if (!p.inverse)
			{
				CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
				tempVar.id = ToDL(p.name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : (isPlural.get() ? endict.WordKind.PluralFormVerb : endict.WordKind.PastParticiple));
				return tempVar;
			}
			else
			{
				CNL.DL.RoleInversion tempVar2 = new CNL.DL.RoleInversion(null);
				tempVar2.R = new CNL.DL.Atomic(null);
				tempVar2.R.id = ToDL(p.name, EntityKind.AnyRole, endict.WordKind.SimplePast);
				return tempVar2;
			}
		}
	}
	public final Object Visit(objectRoleExpr2 p)
	{
		try (isModal.set(isModal.get() || p.Negated))
		{
			try (roleNode.set(p.r.accept(this) instanceof CNL.DL.Node ? (CNL.DL.Node)p.r.accept(this) : null))
			{
				if (p.Negated)
				{
					Object tempVar = p.s.accept(this);
					cognipy.cnl.dl.ConceptNot tempVar2 = new cognipy.cnl.dl.ConceptNot(null);
					tempVar2.C = tempVar instanceof cognipy.cnl.dl.Node ? (cognipy.cnl.dl.Node)tempVar : null;
					return tempVar2;
				}
				else
				{
					if (p.s != null)
					{
						return p.s.accept(this);
					}
					else
					{
						return (new CNL.EN.oobjectSomething(null)).accept(this);
					}
				}
			}
		}
	}
	public final Object Visit(objectRoleExpr3 p)
	{
		try (isModal.set(isModal.get()))
		{
			Object tempVar = p.t.accept(this);
			Object tempVar2 = p.r.accept(this);
			cognipy.cnl.dl.SomeRestriction tempVar3 = new cognipy.cnl.dl.SomeRestriction(null);
			tempVar3.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			tempVar3.R = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
			return tempVar3;
		}
	}

	public final Object Visit(oobjectA p)
	{
		try (isModal.set(false))
		{
			if (roleNode.get() != null)
			{
				Object tempVar = p.s.accept(this);
				CNL.DL.SomeRestriction tempVar2 = new CNL.DL.SomeRestriction(null);
				tempVar2.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
				tempVar2.R = roleNode.get();
				return tempVar2;
			}
			else
			{
				return p.s.accept(this);
			}
		}
	}
	public final Object Visit(oobjectOnly p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			try (isPlural.set(true))
			{
				Object tempVar = p.s.accept(this);
				CNL.DL.OnlyRestriction tempVar2 = new CNL.DL.OnlyRestriction(null);
				tempVar2.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
				tempVar2.R = roleNode.get();
				return tempVar2;
			}
		}
	}

	public final void Assert(boolean b)
	{
		if (!b)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
			System.Diagnostics.Debugger.Break();
//#endif
			throw new RuntimeException("Conversion Assertion Failed.");
		}
	}
	public final Object Visit(oobjectCmp p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			CNL.DL.Node aC = null;
			try (isPlural.set(Long.parseLong(p.Cnt) != 1))
			{
				Object tempVar = p.s.accept(this);
				aC = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			}
			CNL.DL.NumberRestriction tempVar2 = new CNL.DL.NumberRestriction(null);
			tempVar2.N = p.Cnt;
			tempVar2.Kind = p.Cmp;
			tempVar2.C = aC;
			tempVar2.R = roleNode.get();
			return tempVar2;
		}
	}

	public final Object Visit(oobjectCmpInstance p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			CNL.DL.Node aC = null;
			try (isPlural.set(Long.parseLong(p.Cnt) != 1))
			{
				Object tempVar = p.i.accept(this);
				aC = new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, tempVar instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar : null));
			}
			CNL.DL.NumberRestriction tempVar2 = new CNL.DL.NumberRestriction(null);
			tempVar2.N = p.Cnt;
			tempVar2.Kind = p.Cmp;
			tempVar2.C = aC;
			tempVar2.R = roleNode.get();
			return tempVar2;
		}
	}
	public final Object Visit(oobjectBnd p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			Object tempVar = p.b.accept(this);
			CNL.DL.SomeValueRestriction tempVar2 = new CNL.DL.SomeValueRestriction(null);
			tempVar2.R = roleNode.get();
			tempVar2.B = tempVar instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)tempVar : null;
			return tempVar2;
		}
	}
	public final Object Visit(oobjectOnlyBnd p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			Object tempVar = p.b.accept(this);
			CNL.DL.OnlyValueRestriction tempVar2 = new CNL.DL.OnlyValueRestriction(null);
			tempVar2.R = roleNode.get();
			tempVar2.B = tempVar instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)tempVar : null;
			return tempVar2;
		}
	}
	public final Object Visit(oobjectCmpBnd p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			Object tempVar = p.b.accept(this);
			CNL.DL.NumberValueRestriction tempVar2 = new CNL.DL.NumberValueRestriction(null);
			tempVar2.N = p.Cnt;
			tempVar2.Kind = p.Cmp;
			tempVar2.R = roleNode.get();
			tempVar2.B = tempVar instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)tempVar : null;
			return tempVar2;
		}
	}
	public final Object Visit(oobjectInstance p)
	{
		try (isModal.set(false))
		{
			if (roleNode.get() != null)
			{
				Object tempVar = p.i.accept(this);
				CNL.DL.SomeRestriction tempVar2 = new CNL.DL.SomeRestriction(null);
				tempVar2.C = new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, tempVar instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar : null));
				tempVar2.R = roleNode.get();
				return tempVar2;
			}
			else
			{
				Object tempVar3 = p.i.accept(this);
				return new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, tempVar3 instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar3 : null));
			}
		}
	}
	public final Object Visit(instanceBigName p)
	{
		return BigName(p.name);
	}
	public final Object Visit(instanceThe p)
	{
		return Unnamed(p.only, p.s);
	}
	public final Object Visit(oobjectOnlyInstance p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			Object tempVar = p.i.accept(this);
			CNL.DL.OnlyRestriction tempVar2 = new CNL.DL.OnlyRestriction(null);
			tempVar2.C = new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, tempVar instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar : null));
			tempVar2.R = roleNode.get();
			return tempVar2;
		}
	}

	public final Object Visit(oobjectSelf p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			CNL.DL.SelfReference tempVar = new CNL.DL.SelfReference(null);
			tempVar.R = roleNode.get();
			return tempVar;
		}
	}
	public final Object Visit(oobjectSomething p)
	{
		try (isModal.set(false))
		{
			if (roleNode.get() != null)
			{
				CNL.DL.SomeRestriction tempVar = new CNL.DL.SomeRestriction(null);
				tempVar.C = new CNL.DL.Top(null);
				tempVar.R = roleNode.get();
				return tempVar;
			}
			else
			{
				return new CNL.DL.Top(null);
			}
		}
	}
	public final Object Visit(oobjectNothing p)
	{
		try (isModal.set(false))
		{
			if (roleNode.get() != null)
			{
				CNL.DL.SomeRestriction tempVar = new CNL.DL.SomeRestriction(null);
				tempVar.C = new CNL.DL.Bottom(null);
				tempVar.R = roleNode.get();
				return tempVar;
			}
			else
			{
				return new CNL.DL.Bottom(null);
			}
		}
	}
	public final Object Visit(oobjectOnlyNothing p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			CNL.DL.OnlyRestriction tempVar = new CNL.DL.OnlyRestriction(null);
			tempVar.C = new CNL.DL.Bottom(null);
			tempVar.R = roleNode.get();
			return tempVar;
		}
	}
	public final Object Visit(oobjectSomethingThat p)
	{
		try (isModal.set(false))
		{
			if (roleNode.get() != null)
			{
				Object tempVar = p.t.accept(this);
				CNL.DL.SomeRestriction tempVar2 = new CNL.DL.SomeRestriction(null);
				tempVar2.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
				tempVar2.R = roleNode.get();
				return tempVar2;
			}
			else
			{
				return p.t.accept(this);
			}
		}
	}
	public final Object Visit(oobjectOnlySomethingThat p)
	{
		try (isModal.set(false))
		{
			Assert(p instanceof oobjectRelated && roleNode.get() != null);
			Object tempVar = p.t.accept(this);
			CNL.DL.OnlyRestriction tempVar2 = new CNL.DL.OnlyRestriction(null);
			tempVar2.C = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			tempVar2.R = roleNode.get();
			return tempVar2;
		}
	}

	private String name(String str)
	{
		return ToDL(str, EntityKind.Concept, isPlural.get() ? endict.WordKind.PluralFormNoun : endict.WordKind.NormalForm);
	}

	public final Object Visit(singleName p)
	{
		CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
		tempVar.id = name(p.name);
		return tempVar;
	}
	public final Object Visit(singleThing p)
	{
		return new CNL.DL.Top(null);
	}
	public final Object Visit(singleNameThat p)
	{
		Object tempVar = p.t.accept(this);
		CNL.DL.Atomic tempVar2 = new CNL.DL.Atomic(null);
		tempVar2.id = name(p.name);
		return new CNL.DL.ConceptAnd(null, tempVar2, tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null);
	}
	public final Object Visit(singleThingThat p)
	{
		return p.t.accept(this);
	}
	public final Object Visit(thatOrLoop p)
	{
		return p.o.accept(this);
	}
	public final Object Visit(singleOneOf p)
	{
		try (isModal.set(false))
		{
			CNL.DL.InstanceSet iset = null;
			try (isPlural.set(false))
			{
				iset = new CNL.DL.InstanceSet(null);
				iset.Instances = new ArrayList<CNL.DL.Instance>();
				for (instance i : p.insts)
				{
					Object tempVar = i.accept(this);
					iset.Instances.add(tempVar instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar : null);
				}
			}
			return iset;
		}
	}
	public final Object Visit(andloop p)
	{
		CNL.DL.Node first = null;
		for (objectRoleExpr e : p.exprs)
		{
			if (first == null)
			{
				Object tempVar = e.accept(this);
				first = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			}
			else
			{
				Object tempVar2 = e.accept(this);
				first = new CNL.DL.ConceptAnd(null, first, tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);
			}
		}
		return first;
	}
	public final Object Visit(orloop p)
	{
		CNL.DL.Node first = null;
		for (andloop e : p.exprs)
		{
			if (first == null)
			{
				Object tempVar = e.accept(this);
				first = tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null;
			}
			else
			{
				Object tempVar2 = e.accept(this);
				first = new CNL.DL.ConceptOr(null, first, tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null);
			}
		}
		return first;
	}
	public final Object Visit(instanceList p)
	{
		ArrayList<CNL.DL.Instance> list = new ArrayList<CNL.DL.Instance>();
		for (instance e : p.insts)
		{
			Object tempVar = e.accept(this);
			list.add(tempVar instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar : null);
		}
		cognipy.cnl.dl.InstanceSet tempVar2 = new cognipy.cnl.dl.InstanceSet(null);
		tempVar2.Instances = list;
		return tempVar2;
	}

	public final Object Visit(facet p)
	{
		Object tempVar = p.V.accept(this);
		return new CNL.DL.Facet(null, p.Cmp, tempVar instanceof CNL.DL.Value ? (CNL.DL.Value)tempVar : null);
	}

	public final Object Visit(facetList p)
	{
		CNL.DL.FacetList fl = new CNL.DL.FacetList(null);
		fl.List = new ArrayList<Facet>();
		for (facet f : p.Facets)
		{
			Object tempVar = f.accept(this);
			fl.List.add(tempVar instanceof CNL.DL.Facet ? (CNL.DL.Facet)tempVar : null);
		}
		return fl;
	}

	public final Object Visit(boundFacets p)
	{
		Object tempVar = p.l.accept(this);
		return new CNL.DL.BoundFacets(null, tempVar instanceof CNL.DL.FacetList ? (CNL.DL.FacetList)tempVar : null);
	}

	public final Object Visit(boundNot p)
	{
		Object tempVar = p.bnd.accept(this);
		return new CNL.DL.BoundNot(null, tempVar instanceof AbstractBound ? (AbstractBound)tempVar : null);
	}

	public final Object Visit(boundAnd p)
	{
		if (p.List.size() == 1)
		{
			return p.List.get(0).accept(this);
		}
		else
		{
			CNL.DL.BoundAnd ret = new CNL.DL.BoundAnd(null);
			ret.List = new ArrayList<AbstractBound>();
			for (abstractbound l : p.List)
			{
				Object tempVar = l.accept(this);
				ret.List.add(tempVar instanceof AbstractBound ? (AbstractBound)tempVar : null);
			}
			return ret;
		}
	}

	public final Object Visit(boundOr p)
	{
		if (p.List.size() == 1)
		{
			return p.List.get(0).accept(this);
		}
		else
		{
			CNL.DL.BoundOr ret = new CNL.DL.BoundOr(null);
			ret.List = new ArrayList<AbstractBound>();
			for (abstractbound l : p.List)
			{
				Object tempVar = l.accept(this);
				ret.List.add(tempVar instanceof AbstractBound ? (AbstractBound)tempVar : null);
			}
			return ret;
		}
	}

	public final Object Visit(boundVal p)
	{
		Object tempVar = p.V.accept(this);
		return new CNL.DL.BoundVal(null, p.Cmp, tempVar instanceof CNL.DL.Value ? (CNL.DL.Value)tempVar : null);
	}

	public final Object Visit(boundTotal p)
	{
		Value v = null;
		switch (p.Kind)
		{
			case "NUM":
				v = new CNL.DL.Number(null, "1");
				break;
			case "STR":
				v = new CNL.DL.String(null, "\'...\'");
				break;
			case "DBL":
				v = new CNL.DL.Float(null, "3.14");
				break;
			case "BOL":
				v = new CNL.DL.Bool(null, "[1]");
				break;
			case "DTM":
				v = new CNL.DL.DateTimeVal(null, "2012-02-16");
				break;
			case "DUR":
				v = new CNL.DL.Duration(null, "P1DT12H35M30.234S");
				break;
			default:
				Assert(false);
				break;
		}
		return new CNL.DL.TotalBound(null, v);
	}
	public final Object Visit(boundDataType p)
	{
		CNL.DL.ID tempVar = new CNL.DL.ID(null);
		tempVar.setyytext(ToDL(p.name, EntityKind.DataType, endict.WordKind.NormalForm));
		return new CNL.DL.DTBound(null, tempVar);
	}
	public final Object Visit(boundTop p)
	{
		return new CNL.DL.TopBound(null);
	}
	public final Object Visit(boundOneOf p)
	{
		CNL.DL.ValueSet ret = new CNL.DL.ValueSet(null);
		ret.Values = new ArrayList<Value>();
		for (dataval val : p.vals)
		{
			Object tempVar = val.accept(this);
			ret.Values.add(tempVar instanceof Value ? (Value)tempVar : null);
		}
		return ret;
	}
	public final Object Visit(Number p)
	{
		return new CNL.DL.Number(null, p.val);
	}
	public final Object Visit(Bool p)
	{
		return new CNL.DL.Bool(null, p.val.equals("true") ? "[1]" : "[0]");
	}
	public final Object Visit(StrData p)
	{
		return new CNL.DL.String(null, p.val);
	}
	public final Object Visit(DateTimeData p)
	{
		return new CNL.DL.DateTimeVal(null, p.val);
	}
	public final Object Visit(Duration p)
	{
		return new CNL.DL.Duration(null, p.val);
	}

	public final Object Visit(Float p)
	{
		return new CNL.DL.Float(null, p.val);
	}

	////////// SWRL /////////////////////////////////////////////////////////////////////

	public final Object Visit(swrlrule p)
	{
		listVars.clear();
		newDataValVar = 1;
		newInstanceValVar.clear();

		cognipy.cnl.dl.SwrlStatement swrl_statement = new CNL.DL.SwrlStatement(null);
		Object tempVar = p.Predicate.accept(this);
		swrl_statement.slp = tempVar instanceof CNL.DL.SwrlItemList ? (CNL.DL.SwrlItemList)tempVar : null;
		Object tempVar2 = p.Result.accept(this);
		swrl_statement.slc = tempVar2 instanceof CNL.DL.SwrlItemList ? (CNL.DL.SwrlItemList)tempVar2 : null;
		swrl_statement.modality = Modality(p.modality);
		return swrl_statement;
	}

	public final Object Visit(clause p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		for (condition e : p.Conditions)
		{
			Object tempVar = e.accept(this);
			listT.addAll(tempVar instanceof ArrayList<CNL.DL.SwrlItem> ? (ArrayList<CNL.DL.SwrlItem>)tempVar : null);
		}
		cognipy.cnl.dl.SwrlItemList tempVar2 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar2.list = listT;
		return tempVar2;
	}

	public final Object Visit(clause_result p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		for (condition_result e : p.Conditions)
		{
			Object tempVar = e.accept(this);
			listT.addAll(tempVar instanceof ArrayList<CNL.DL.SwrlItem> ? (ArrayList<CNL.DL.SwrlItem>)tempVar : null);
		}
		cognipy.cnl.dl.SwrlItemList tempVar2 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar2.list = listT;
		return tempVar2;
	}

	public final Object Visit(condition_is p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		SwrlIObject id_o = tempVar instanceof SwrlIObject ? (SwrlIObject)tempVar : null;
		CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
		if (inst != null)
		{
			listT.add(inst);
		}

		Object tempVar2 = p.objectB.accept(this);
		SwrlIObject id_o2 = tempVar2 instanceof SwrlIObject ? (SwrlIObject)tempVar2 : null;
		CNL.DL.SwrlInstance inst2 = createSwrlInstanceFromObjectR(p.objectB, id_o2);
		if (inst2 != null)
		{
			id_o2 = inst2.I;
			listT.add(inst2);
		}

		if (p.condition_kind == condition_kind.None)
		{
			CNL.DL.SwrlSameAs tempVar3 = new CNL.DL.SwrlSameAs(null);
			tempVar3.I = id_o;
			tempVar3.J = id_o2;
			listT.add(tempVar3);
		}
		else
		{
			CNL.DL.SwrlDifferentFrom tempVar4 = new CNL.DL.SwrlDifferentFrom(null);
			tempVar4.I = id_o;
			tempVar4.J = id_o2;
			listT.add(tempVar4);
		}

		return listT;
	}

	public final Object Visit(condition_exists p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		SwrlIObject id_o = tempVar instanceof SwrlIObject ? (SwrlIObject)tempVar : null;
		cognipy.cnl.dl.SwrlInstance itm = createSwrlInstanceFromObjectR(p.objectA, id_o);
		if (itm == null)
		{
			itm = createNewSwrlInstance(null, id_o);
		}
		listT.add(itm);
		return listT;
	}

	public final Object Visit(condition_definition p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		String clsName = null;
		if (p.objectA instanceof objectr_nio)
		{
			clsName = (p.objectA instanceof objectr_nio ? (objectr_nio)p.objectA : null).notidentobject.name;
		}
		else if ((p.objectA instanceof objectr_io) && (p.objectA instanceof objectr_io ? (objectr_io)p.objectA : null).identobject instanceof identobject_name)
		{
			clsName = ((p.objectA instanceof objectr_io ? (objectr_io)p.objectA : null).identobject instanceof identobject_name ? (identobject_name)(p.objectA instanceof objectr_io ? (objectr_io)p.objectA : null).identobject : null).name;
		}
		if (clsName != null)
		{
			CNL.DL.Atomic myAtom = new CNL.DL.Atomic(null);
			myAtom.id = ToDL(clsName, EntityKind.Concept, endict.WordKind.NormalForm);
			listT.add(new CNL.DL.SwrlInstance(null, myAtom, id_o));
		}
		Object tempVar2 = p.objectClass.accept(this);
		CNL.DL.Node id_o2 = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
		listT.add(new CNL.DL.SwrlInstance(null, id_o2, id_o));

		return listT;
	}

	public final Object Visit(condition_role p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
		if (inst != null)
		{
			listT.add(inst);
		}

		Object tempVar2 = p.objectB.accept(this);
		CNL.DL.SwrlIObject id_o2 = tempVar2 instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar2 : null;
		CNL.DL.SwrlInstance inst2 = createSwrlInstanceFromObjectR(p.objectB, id_o2);
		if (inst2 != null)
		{
			id_o2 = inst2.I;
			listT.add(inst2);
		}
		if (p.condition_kind == condition_kind.None)
		{
			CNL.DL.SwrlRole tempVar3 = new CNL.DL.SwrlRole(null);
			tempVar3.I = id_o;
			tempVar3.J = id_o2;
			tempVar3.R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.PastParticiple);
			listT.add(tempVar3);
		}
		else if (p.condition_kind == condition_kind.Inv)
		{
			CNL.DL.SwrlRole tempVar4 = new CNL.DL.SwrlRole(null);
			tempVar4.I = id_o2;
			tempVar4.J = id_o;
			tempVar4.R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.SimplePast);
			listT.add(tempVar4);
		}

		return listT;
	}

	public final Object Visit(condition_data_property p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();

		Object tempVar = p.objectA.accept(this);
		SwrlIObject id_o = tempVar instanceof SwrlIObject ? (SwrlIObject)tempVar : null;
		CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
		if (inst != null)
		{
			listT.add(inst);
		}

		Object tempVar2 = p.d_object.accept(this);
		SwrlDObject db2 = tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null;

		CNL.DL.SwrlDataProperty tempVar3 = new CNL.DL.SwrlDataProperty(null);
		tempVar3.IO = id_o;
		tempVar3.DO = db2;
		tempVar3.R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
		listT.add(tempVar3);
		return listT;
	}

	public final Object Visit(condition_result_is p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		Object tempVar2 = p.objectB.accept(this);
		CNL.DL.SwrlIObject id_o2 = tempVar2 instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar2 : null;

		if (p.condition_kind == condition_kind.None)
		{
			CNL.DL.SwrlSameAs tempVar3 = new CNL.DL.SwrlSameAs(null);
			tempVar3.I = id_o;
			tempVar3.J = id_o2;
			listT.add(tempVar3);
		}
		else
		{
			CNL.DL.SwrlDifferentFrom tempVar4 = new CNL.DL.SwrlDifferentFrom(null);
			tempVar4.I = id_o;
			tempVar4.J = id_o2;
			listT.add(tempVar4);
		}

		return listT;
	}

	public final Object Visit(condition_result_definition p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		Object tempVar2 = p.objectClass.accept(this);
		CNL.DL.Node id_o2 = tempVar2 instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar2 : null;
		listT.add(new CNL.DL.SwrlInstance(null, id_o2, id_o));
		return listT;
	}

	public final Object Visit(condition_result_role p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		boolean id_ex = true;
		if (p.objectA instanceof identobject_name)
		{
			id_ex = isVarAlreadyIntroduced((identobject_name)p.objectA);
		}

		Object tempVar = p.objectA.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		if (!id_ex)
		{
			cognipy.cnl.dl.SwrlInstance inst = createSwrlInstanceFromObject_name((identobject_name)p.objectA, id_o);
			if (inst != null)
			{
				listT.add(inst);
			}
		}

		boolean id_ex2 = true;
		if (p.objectB instanceof identobject_name)
		{
			id_ex2 = isVarAlreadyIntroduced((identobject_name)p.objectB);
		}

		Object tempVar2 = p.objectB.accept(this);
		CNL.DL.SwrlIObject id_o2 = tempVar2 instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar2 : null;
		if (!id_ex2)
		{
			cognipy.cnl.dl.SwrlInstance inst = createSwrlInstanceFromObject_name((identobject_name)p.objectB, id_o2);
			if (inst != null)
			{
				listT.add(inst);
			}
		}

		if (p.condition_kind == condition_kind.None)
		{
			CNL.DL.SwrlRole tempVar3 = new CNL.DL.SwrlRole(null);
			tempVar3.I = id_o;
			tempVar3.J = id_o2;
			tempVar3.R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.PastParticiple);
			listT.add(tempVar3);
		}
		else if (p.condition_kind == condition_kind.Inv)
		{
			CNL.DL.SwrlRole tempVar4 = new CNL.DL.SwrlRole(null);
			tempVar4.I = id_o2;
			tempVar4.J = id_o;
			tempVar4.R = ToDL(p.role, EntityKind.AnyRole, endict.WordKind.SimplePast);
			listT.add(tempVar4);
		}
		return listT;
	}

	public final Object Visit(condition_result_data_property p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		SwrlIObject id_o = tempVar instanceof SwrlIObject ? (SwrlIObject)tempVar : null;

		Object tempVar2 = p.d_object.accept(this);
		SwrlDObject db2 = tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null;

		CNL.DL.SwrlDataProperty tempVar3 = new CNL.DL.SwrlDataProperty(null);
		tempVar3.IO = id_o;
		tempVar3.DO = db2;
		tempVar3.R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
		listT.add(tempVar3);

		return listT;
	}

	private int newDataValVar = 1;
	public final Object Visit(condition_data_property_bound p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.objectA.accept(this);
		SwrlIObject id_o = tempVar instanceof SwrlIObject ? (SwrlIObject)tempVar : null;
		CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.objectA, id_o);
		if (inst != null)
		{
			listT.add(inst);
		}

		if (p.bnd.isStrict())
		{
			datavalval d_val = new datavalval(null, p.bnd.getStrictVal());
			Object tempVar2 = d_val.accept(this);
			SwrlDObject db2 = tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null;

			CNL.DL.SwrlDataProperty tempVar3 = new CNL.DL.SwrlDataProperty(null);
			tempVar3.IO = id_o;
			tempVar3.DO = db2;
			tempVar3.R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
			listT.add(tempVar3);
		}
		else
		{
			datavalvar d_val = new datavalvar(null, "tmp-" + (newDataValVar++).toString());
			Object tempVar4 = d_val.accept(this);
			SwrlDObject db2 = tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null;

			CNL.DL.SwrlDataProperty tempVar5 = new CNL.DL.SwrlDataProperty(null);
			tempVar5.IO = id_o;
			tempVar5.DO = db2;
			tempVar5.R = ToDL(p.property_name, EntityKind.AnyRole, isModal.get() ? endict.WordKind.NormalForm : endict.WordKind.PastParticiple);
			listT.add(tempVar5);

			Object tempVar6 = p.bnd.accept(this);
			AbstractBound b = tempVar6 instanceof AbstractBound ? (AbstractBound)tempVar6 : null;
			CNL.DL.SwrlDataRange tempVar7 = new CNL.DL.SwrlDataRange(null);
			tempVar7.DO = db2;
			tempVar7.B = b;
			listT.add(tempVar7);
		}
		return listT;
	}

	public final Object Visit(condition_data_bound p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.bound.accept(this);
		AbstractBound b = tempVar instanceof AbstractBound ? (AbstractBound)tempVar : null;
		Object tempVar2 = p.d_object.accept(this);
		SwrlDObject db2 = tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null;
		CNL.DL.SwrlDataRange tempVar3 = new CNL.DL.SwrlDataRange(null);
		tempVar3.DO = db2;
		tempVar3.B = b;
		listT.add(tempVar3);
		return listT;
	}

	public final Object Visit(objectr_nio p)
	{
		return p.notidentobject.accept(this);
	}

	public final Object Visit(objectr_io p)
	{
		return p.identobject.accept(this);
	}

	public final Object Visit(notidentobject p)
	{
		int idx = p.num == null ? -2 : Integer.parseInt(p.num);
		return getNewVarIdentifier(p.name != null ? p.name : "thing", idx);
	}

	public final Object Visit(identobject_name p)
	{
		int idx = p.num == null ? -1 : Integer.parseInt(p.num);
		return getNewVarIdentifier(p.name != null ? p.name : "thing", idx);
	}

	public final Object Visit(identobject_inst p)
	{
		return p.i.accept(this);
	}

	public final Object Visit(instancer p)
	{
		CNL.DL.SwrlIVal tempVar = new CNL.DL.SwrlIVal(null);
		tempVar.I = ToDL(p.name, EntityKind.Instance, endict.WordKind.NormalForm);
		return tempVar;
	}

	public final Object Visit(datavalvar p)
	{
		CNL.DL.SwrlDVar tempVar = new CNL.DL.SwrlDVar(null);
		tempVar.VAR = "value-" + p.num;
		return tempVar;
	}

	public final Object Visit(datavalval p)
	{
		Object tempVar = p.dv.accept(this);
		CNL.DL.Value val = tempVar instanceof CNL.DL.Value ? (CNL.DL.Value)tempVar : null;
		return new CNL.DL.SwrlDVal(null, val);
	}

	public final Object Visit(condition_builtin p)
	{
		Object bii = p.bi.accept(this);
		if (bii instanceof CNL.DL.SwrlBuiltIn)
		{
			return new ArrayList<CNL.DL.SwrlItem>(Arrays.asList(bii instanceof CNL.DL.SwrlItem ? (CNL.DL.SwrlItem)bii : null));
		}
		else
		{
			return bii;
		}
	}

	public final Object Visit(condition_result_builtin p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.bi.accept(this);
		CNL.DL.SwrlBuiltIn bi = tempVar instanceof CNL.DL.SwrlBuiltIn ? (CNL.DL.SwrlBuiltIn)tempVar : null;
		listT.add(bi);
		return listT;
	}

	private static String BuiltinTpyToCmp(String tpy)
	{
		String cmp = "";
		switch (tpy)
		{
			case "<=":
				cmp = "≤";
				break;
			case ">=":
				cmp = "≥";
				break;
			case "<>":
				cmp = "≠";
				break;
			default:
				cmp = tpy;
				break;
		}

		return cmp;
	}

	public final Object Visit(builtin_cmp p)
	{
		Object tempVar = p.b.accept(this);
		Object tempVar2 = p.a.accept(this);
		return new CNL.DL.SwrlBuiltIn(null, BuiltinTpyToCmp(p.cmp), new ArrayList<ISwrlObject>(Arrays.asList(tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null)));
	}

	public final Object Visit(builtin_list p)
	{
		Object tempVar = x.accept(this);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		ArrayList<Object> lst = (from x in p.vals select (tempVar instanceof ISwrlObject ? (ISwrlObject)tempVar : null)).ToList();
		Object tempVar2 = p.result.accept(this);
		lst.add(tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null);
		return new CNL.DL.SwrlBuiltIn(null, p.tpy, lst);
	}

	public final Object Visit(builtin_bin p)
	{
		Object tempVar = p.b.accept(this);
		Object tempVar2 = p.d.accept(this);
		Object tempVar3 = p.result.accept(this);
		return new CNL.DL.SwrlBuiltIn(null, p.tpy, new ArrayList<ISwrlObject>(Arrays.asList(tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null, tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null)));
	}

	public final Object Visit(builtin_alpha p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.a.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.a, id_o);
		if (inst != null)
		{
			listT.add(inst);
		}

		Object tempVar2 = p.b.accept(this);
		listT.add(new CNL.DL.SwrlBuiltIn(null, "alpha-representation-of", new ArrayList<ISwrlObject>(Arrays.asList(id_o, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null))));

		return listT;
	}

	public final Object Visit(builtin_annot p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.a.accept(this);
		CNL.DL.SwrlIObject id_o = tempVar instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)tempVar : null;
		Object tempVar2 = p.prop.accept(this);
		CNL.DL.SwrlDObject prop_o = tempVar2 instanceof CNL.DL.SwrlDObject ? (CNL.DL.SwrlDObject)tempVar2 : null;
		CNL.DL.SwrlInstance inst = createSwrlInstanceFromObjectR(p.a, id_o);
		if (inst != null)
		{
			listT.add(inst);
		}

		Object tempVar3 = p.lang.accept(this);
		Object tempVar4 = p.b.accept(this);
		listT.add(new CNL.DL.SwrlBuiltIn(null, "annotation", new ArrayList<ISwrlObject>(Arrays.asList(id_o, prop_o, tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null, tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null))));

		return listT;
	}

	public final Object Visit(builtin_exe p)
	{
		ArrayList<CNL.DL.SwrlItem> listT = new ArrayList<CNL.DL.SwrlItem>();
		Object tempVar = p.ea.accept(this);
		CNL.DL.SwrlVarList el = tempVar instanceof CNL.DL.SwrlVarList ? (CNL.DL.SwrlVarList)tempVar : null;

		SwrlDVal tempVar2 = new SwrlDVal(null);
		tempVar2.Val = new CNL.DL.String(null);
		tempVar2.Val.val = p.name;
		ArrayList<ISwrlObject> l = new ArrayList<ISwrlObject>(Arrays.asList(tempVar2));

		for (IExeVar e : el.list)
		{
			l.add(e instanceof ISwrlObject ? (ISwrlObject)e : null);
		}

		Object tempVar3 = p.a.accept(this);
		l.add(tempVar3 instanceof ISwrlObject ? (ISwrlObject)tempVar3 : null);

		listT.add(new CNL.DL.SwrlBuiltIn(null, "execute", l));

		return listT;
	}

	public final Object Visit(builtin_unary_cmp p)
	{
		Object tempVar = p.b.accept(this);
		Object tempVar2 = p.result.accept(this);
		return new CNL.DL.SwrlBuiltIn(null, p.tpy, new ArrayList<ISwrlObject>(Arrays.asList(tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null)));
	}

	public final Object Visit(builtin_unary_free p)
	{
		Object tempVar = p.b.accept(this);
		Object tempVar2 = p.a.accept(this);
		return new CNL.DL.SwrlBuiltIn(null, p.tpy, new ArrayList<ISwrlObject>(Arrays.asList(tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null)));
	}

	public final Object Visit(builtin_substr p)
	{
		Object tempVar = p.b.accept(this);
		Object tempVar2 = p.c.accept(this);
		ArrayList<ISwrlObject> l = new ArrayList<ISwrlObject>(Arrays.asList(tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null));

		if (p.d != null)
		{
			Object tempVar3 = p.d.accept(this);
			l.add(tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null);
		}

		Object tempVar4 = p.result.accept(this);
		l.add(tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null);

		return new CNL.DL.SwrlBuiltIn(null, p.tpy, l);
	}

	public final Object Visit(builtin_trans p)
	{
		Object tempVar = p.b.accept(this);
		Object tempVar2 = p.c.accept(this);
		Object tempVar3 = p.d.accept(this);
		Object tempVar4 = p.result.accept(this);
		return new CNL.DL.SwrlBuiltIn(null, p.tpy, new ArrayList<ISwrlObject>(Arrays.asList(tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null, tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null, tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null)));
	}

	public final Object Visit(builtin_duration p)
	{
		Object tempVar = p.d.accept(this);
		ArrayList<ISwrlObject> DT = tempVar instanceof ArrayList<ISwrlObject> ? (ArrayList<ISwrlObject>)tempVar : null;
		Object tempVar2 = p.a.accept(this);
		DT.add(tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null);
		return new CNL.DL.SwrlBuiltIn(null, "duration", DT);
	}

	public final Object Visit(builtin_datetime p)
	{
		Object tempVar = p.d.accept(this);
		ArrayList<ISwrlObject> DT = tempVar instanceof ArrayList<ISwrlObject> ? (ArrayList<ISwrlObject>)tempVar : null;
		Object tempVar2 = p.a.accept(this);
		DT.add(tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null);
		return new CNL.DL.SwrlBuiltIn(null, "datetime", DT);
	}

	public final Object Visit(duration_w p)
	{
		Object tempVar = p.y.accept(this);
		Object tempVar2 = p.W.accept(this);
		Object tempVar3 = p.d.accept(this);
		Object tempVar4 = p.h.accept(this);
		Object tempVar5 = p.m.accept(this);
		Object tempVar6 = p.s.accept(this);
		return new ArrayList<ISwrlObject>(Arrays.asList(new SwrlDVal(null, new cognipy.cnl.dl.String(null, "'W'")), tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null, tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null, tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null, tempVar5 instanceof SwrlDObject ? (SwrlDObject)tempVar5 : null, tempVar6 instanceof SwrlDObject ? (SwrlDObject)tempVar6 : null));
	}

	public final Object Visit(duration_m p)
	{
		Object tempVar = p.y.accept(this);
		Object tempVar2 = p.M.accept(this);
		Object tempVar3 = p.d.accept(this);
		Object tempVar4 = p.h.accept(this);
		Object tempVar5 = p.m.accept(this);
		Object tempVar6 = p.s.accept(this);
		return new ArrayList<ISwrlObject>(Arrays.asList(new SwrlDVal(null, new cognipy.cnl.dl.String(null, "'M'")), tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null, tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null, tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null, tempVar5 instanceof SwrlDObject ? (SwrlDObject)tempVar5 : null, tempVar6 instanceof SwrlDObject ? (SwrlDObject)tempVar6 : null));

	}

	public final Object Visit(datetime p)
	{
		Object tempVar = p.y.accept(this);
		Object tempVar2 = p.M.accept(this);
		Object tempVar3 = p.d.accept(this);
		Object tempVar4 = p.h.accept(this);
		Object tempVar5 = p.m.accept(this);
		Object tempVar6 = p.s.accept(this);
		return new ArrayList<ISwrlObject>(Arrays.asList(new SwrlDVal(null, new cognipy.cnl.dl.String(null, "'M'")), tempVar instanceof SwrlDObject ? (SwrlDObject)tempVar : null, tempVar2 instanceof SwrlDObject ? (SwrlDObject)tempVar2 : null, tempVar3 instanceof SwrlDObject ? (SwrlDObject)tempVar3 : null, tempVar4 instanceof SwrlDObject ? (SwrlDObject)tempVar4 : null, tempVar5 instanceof SwrlDObject ? (SwrlDObject)tempVar5 : null, tempVar6 instanceof SwrlDObject ? (SwrlDObject)tempVar6 : null));
	}

	private ArrayList<String> listVars = new ArrayList<String>(); //list of chosen (or given) identifiers of instances used in swrl rule

	private String getExistingIdentifier(String name, int idx) // bierze istniejący identyfikator, w pp zwraca pusty string
	{
		DlName parst = new DlName();
		parst.id = ToDL(name, EntityKind.Concept, endict.WordKind.NormalForm);

		String nn;

		Assert(idx != -2);
		if (!newInstanceValVar.containsKey(name))
		{
			newInstanceValVar.put(name, 1);
		}

		if (idx == -1)
		{
			nn = parst.name + (!newInstanceValVar.get(name).equals(1) ? "-0" + newInstanceValVar.get(name).toString() : "-x");
		}
		else
		{
			nn = parst.name + "-" + idx;
		}

		DlName.Parts id = new DlName.Parts();
		id.name = nn;

		if (!listVars.contains(id))
		{
			listVars.add(id);
			return id;
		}
		else
		{
			return "";
		}
	}

	private HashMap<String, Integer> newInstanceValVar = new HashMap<String, Integer>();

	private boolean isVarAlreadyIntroduced(identobject_name io)
	{
		String namet = io.name != null ? io.name : "thing";
		DlName parst = new DlName();
		parst.id = ToDL(namet, EntityKind.Concept, endict.WordKind.NormalForm);

		int idx = io.num == null ? -1 : Integer.parseInt(io.num);

		if (!newInstanceValVar.containsKey(namet))
		{
			return false;
		}

		String nn = "";
		if (idx < 0)
		{
			nn = parst.name + (!newInstanceValVar.get(namet).equals(1) ? "-0" + newInstanceValVar.get(namet).toString() : "-x");
		}
		else
		{
			nn = parst.name + "-" + idx;
		}

		DlName.Parts id = new DlName.Parts();
		id.name = nn;

		if (!listVars.contains(id))
		{
			return false;
		}

		return true;

	}


	private CNL.DL.SwrlIVar getNewVarIdentifier(String namet, int idx)
	//tworzy nowy identyfikator instancji danej klasy wg konwencji "nazwa-klasy"_numer 
	//jesli już istnieje, to zwraca ten istniejący
	{
		DlName parst = new DlName();
		parst.id = ToDL(namet, EntityKind.Concept, endict.WordKind.NormalForm);

		String nn;

		if (idx == -2 || !newInstanceValVar.containsKey(namet))
		{
			if (!newInstanceValVar.containsKey(namet))
			{
				newInstanceValVar.put(namet, 0);
			}
			newInstanceValVar.put(namet, newInstanceValVar.get(namet) + 1);
		}

		if (idx < 0)
		{
			nn = parst.name + (!newInstanceValVar.get(namet).equals(1) ? "-0" + newInstanceValVar.get(namet).toString() : "-x");
		}
		else
		{
			nn = parst.name + "-" + idx;
		}

		DlName.Parts id = new DlName.Parts();
		id.name = nn;

		if (!listVars.contains(id))
		{
			listVars.add(id);
		}

		SwrlIVar tempVar = new SwrlIVar(null);
		tempVar.VAR = id;
		return tempVar;
	}

	private CNL.DL.SwrlInstance createNewSwrlInstance(String class_name, SwrlIObject id)
	{
		Object tempVar = new cognipy.cnl.dl.Top(null);
		CNL.DL.SwrlInstance tempVar2 = new CNL.DL.SwrlInstance(null);
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = class_name;
		tempVar2.C = (class_name == null) ? tempVar instanceof CNL.DL.Node ? (CNL.DL.Node)tempVar : null : tempVar3;
		tempVar2.I = id;
		return tempVar2;
	}

	private CNL.DL.SwrlInstance createSwrlInstanceFromObject_name(CNL.EN.identobject_name o, SwrlIObject id)
	{
		String class_name = o.name;
		int idx = o.num == null ? -1 : Integer.parseInt(o.num);

		if (class_name == null)
		{
			return null;
		}
		else
		{

			String vname = getExistingIdentifier(class_name != null ? class_name : "thing", idx);
			if (!listVars.contains(vname))
			{
				cognipy.cnl.dl.SwrlIVar vid = getNewVarIdentifier(class_name != null ? class_name : "thing", idx);
				return createNewSwrlInstance(class_name, vid);
			}
			else
			{
				return null;
			}
		}
	}

	private CNL.DL.SwrlInstance createSwrlInstanceFromObjectR(CNL.EN.objectr o)
	{
		if (o instanceof CNL.EN.objectr_nio)
		{
			String class_name = (o instanceof CNL.EN.objectr_nio ? (CNL.EN.objectr_nio)o : null).notidentobject.name;

			int idx = (o instanceof CNL.EN.objectr_nio ? (CNL.EN.objectr_nio)o : null).notidentobject.num == null ? -2 : Integer.parseInt((o instanceof CNL.EN.objectr_nio ? (CNL.EN.objectr_nio)o : null).notidentobject.num);
			return createNewSwrlInstance(name(class_name), getNewVarIdentifier(class_name != null ? class_name : "thing", idx));
		}
		else if (o instanceof CNL.EN.objectr_io)
		{
			Debugger.Break();
			throw new UnsupportedOperationException();
		}
		return null;
	}

	private CNL.DL.SwrlInstance createSwrlInstance(CNL.EN.identobject io, SwrlIObject id)
	{
		if (io instanceof identobject_name)
		{
			String class_name = (io instanceof identobject_name ? (identobject_name)io : null).name;
			if (class_name == null)
			{
				return null;
			}
			else
			{
				return createNewSwrlInstance(name(class_name), id);
			}
		}
		else
		{
			return null;
		}
	}

	private CNL.DL.SwrlInstance createSwrlInstance(CNL.EN.notidentobject nio, SwrlIObject id)
	{
		if (nio instanceof notidentobject)
		{
			String class_name = nio.name;
			if (class_name == null)
			{
				return null;
			}
			else
			{
				return createNewSwrlInstance(name(class_name), id);
			}
		}
		else
		{
			return null;
		}
	}

	private CNL.DL.SwrlInstance createSwrlInstanceFromObjectR(CNL.EN.objectr o, SwrlIObject id)
	{
		if (o instanceof CNL.EN.objectr_nio)
		{
			String class_name = (o instanceof CNL.EN.objectr_nio ? (CNL.EN.objectr_nio)o : null).notidentobject.name;
			if (class_name == null)
			{
				return null;
			}
			else
			{
				return createNewSwrlInstance(name(class_name), id);
			}
		}
		else if (o instanceof CNL.EN.objectr_io)
		{
			if ((o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject instanceof CNL.EN.identobject_name)
			{
				String class_name = ((o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject instanceof CNL.EN.identobject_name ? (CNL.EN.identobject_name)(o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject : null).name;
				int idx = ((o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject instanceof CNL.EN.identobject_name ? (CNL.EN.identobject_name)(o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject : null).num == null ? -1 : Integer.parseInt(((o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject instanceof CNL.EN.identobject_name ? (CNL.EN.identobject_name)(o instanceof CNL.EN.objectr_io ? (CNL.EN.objectr_io)o : null).identobject : null).num);
				if (class_name == null)
				{
					return null;
				}
				else
				{
					String vname = getExistingIdentifier(class_name != null ? class_name : "thing", idx);
					if (!listVars.contains(vname))
					{
						cognipy.cnl.dl.SwrlIVar vid = getNewVarIdentifier(class_name != null ? class_name : "thing", idx);
						return createNewSwrlInstance(name(class_name), vid);
					}
					else
					{
						return createNewSwrlInstance(name(class_name), id);
					}
				}
			}
			else
			{
				return null;
			}
		}
		else
		{
			throw new IllegalStateException("Unknown Swrl Term");
		}
	}

	public final Object Visit(swrlrulefor p)
	{
		listVars.clear();
		newDataValVar = 1;
		newInstanceValVar.clear();

		cognipy.cnl.dl.SwrlIterate swrl_statement = new CNL.DL.SwrlIterate(null);
		Object tempVar = p.Predicate.accept(this);
		swrl_statement.slp = tempVar instanceof CNL.DL.SwrlItemList ? (CNL.DL.SwrlItemList)tempVar : null;
		Object tempVar2 = p.Result.accept(this);
		swrl_statement.slc = tempVar2 instanceof CNL.DL.SwrlItemList ? (CNL.DL.SwrlItemList)tempVar2 : null;
		Object tempVar3 = p.Collection.accept(this);
		swrl_statement.vars = tempVar3 instanceof CNL.DL.SwrlVarList ? (CNL.DL.SwrlVarList)tempVar3 : null;

		return swrl_statement;
	}


	//////////// SWRL //////////////////////////////////////////////////////////////

	//////////// EXE //////////////////////////////////////////////////////////////

	public final Object Visit(exerule p)
	{
		listVars.clear();
		newDataValVar = 1;
		newInstanceValVar.clear();
		CNL.DL.ExeStatement exe_statement = new CNL.DL.ExeStatement(null);
		Object tempVar = p.slp.accept(this);
		exe_statement.slp = tempVar instanceof CNL.DL.SwrlItemList ? (CNL.DL.SwrlItemList)tempVar : null;
		Object tempVar2 = p.args.accept(this);
		exe_statement.args = tempVar2 instanceof CNL.DL.SwrlVarList ? (CNL.DL.SwrlVarList)tempVar2 : null;
		exe_statement.exe = p.exe;
		return exe_statement;
	}

	public final Object Visit(exeargs p)
	{
		CNL.DL.SwrlVarList idlist = new CNL.DL.SwrlVarList(null);
		idlist.list = new ArrayList<IExeVar>();
		for (iexevar el : p.exevars)
		{
			Object tempVar = el.accept(this);
			idlist.list.add(tempVar instanceof IExeVar ? (IExeVar)tempVar : null);
		}
		return idlist;
	}

	public final Object Visit(code p)
	{
		CodeStatement code_statement = new CodeStatement(null);
		code_statement.exe = p.exe;
		return code_statement;
	}

	//////////// EXE //////////////////////////////////////////////////////////////

	public final Object Visit(annotation p)
	{
		return new CNL.DL.Annotation(null, "%" + p.txt);
	}

	public final Object Visit(dlannotationassertion p)
	{
		cognipy.ars.EntityKind result = cognipy.cnl.AnnotationManager.ParseSubjectKind(p.subjKind);
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if !SILVERLIGHT
		CNL.DL.DLAnnotationAxiom tempVar = new CNL.DL.DLAnnotationAxiom(null);
		tempVar.setsubject(p.subject);
		tempVar.setsubjKind(p.subjKind);
		tempVar.annotName = p.annotName;
		tempVar.value = System.Net.WebUtility.HtmlEncode(p.value);
		tempVar.language = p.language;
		return tempVar;
//#else
		CNL.DL.DLAnnotationAxiom tempVar2 = new CNL.DL.DLAnnotationAxiom(null);
		tempVar2.setsubject(p.subject);
		tempVar2.setsubjKind(p.subjKind);
		tempVar2.annotName = p.annotName;
		tempVar2.value = p.value;
		tempVar2.language = p.language;
		return tempVar2;
//#endif
	}

}