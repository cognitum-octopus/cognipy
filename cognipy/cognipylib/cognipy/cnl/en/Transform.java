package cognipy.cnl.en;

import cognipy.cnl.dl.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class Transform implements cognipy.cnl.dl.IVisitor
{

	public Transform()
	{
	}


	public final cognipy.cnl.en.paragraph Convert(cognipy.cnl.dl.Paragraph p, boolean usePrefixes)
	{
		return Convert(p, usePrefixes, null);
	}

	public final cognipy.cnl.en.paragraph Convert(cognipy.cnl.dl.Paragraph p)
	{
		return Convert(p, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.EN.paragraph Convert(CogniPy.CNL.DL.Paragraph p, bool usePrefixes = false, Func<string, string> ns2pfxEx = null)
	public final cognipy.cnl.en.paragraph Convert(cognipy.cnl.dl.Paragraph p, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfxEx)
	{
		this.usePrefixes = usePrefixes;
		this._ns2Pfx = (String arg) -> ns2pfxEx.invoke(arg);
		Object tempVar = p.accept(this);
		return tempVar instanceof cognipy.cnl.en.paragraph ? (cognipy.cnl.en.paragraph)tempVar : null;
	}

	public final java.lang.Iterable<cognipy.cnl.en.sentence> Convert(cognipy.cnl.dl.Statement s, boolean usePrefixes)
	{
		return Convert(s, usePrefixes, null);
	}

	public final java.lang.Iterable<cognipy.cnl.en.sentence> Convert(cognipy.cnl.dl.Statement s)
	{
		return Convert(s, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public IEnumerable<CogniPy.CNL.EN.sentence> Convert(CogniPy.CNL.DL.Statement s, bool usePrefixes = false, Func<string, string> ns2pfxEx = null)
	public final java.lang.Iterable<cognipy.cnl.en.sentence> Convert(cognipy.cnl.dl.Statement s, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfxEx)
	{
		this.usePrefixes = usePrefixes;
		this._ns2Pfx = (String arg) -> ns2pfxEx.invoke(arg);
		Object o = s.accept(this);
		if (o instanceof java.lang.Iterable<CNL.EN.sentence>)
		{
			return o instanceof java.lang.Iterable<cognipy.cnl.en.sentence> ? (java.lang.Iterable<cognipy.cnl.en.sentence>)o : null;
		}
		else
		{
			return new ArrayList<CNL.EN.sentence>(Arrays.asList(o instanceof cognipy.cnl.en.sentence ? (cognipy.cnl.en.sentence)o : null));
		}
	}

	public final Object Convert(cognipy.cnl.dl.IAccept n, boolean usePrefixes)
	{
		return Convert(n, usePrefixes, null);
	}

	public final Object Convert(cognipy.cnl.dl.IAccept n)
	{
		return Convert(n, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public object Convert(CogniPy.CNL.DL.IAccept n, bool usePrefixes = false, Func<string, string> ns2pfxEx = null)
	public final Object Convert(cognipy.cnl.dl.IAccept n, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfxEx)
	{
		this.usePrefixes = usePrefixes;
		this._ns2Pfx = (String arg) -> ns2pfxEx.invoke(arg);
		Object aa = n.accept(this);
		if (aa instanceof TransNode)
		{
			return ((TransNode)aa).makeorloop(false, false);
		}
		else if (aa instanceof TransTotalBound)
		{
			return ((TransTotalBound)aa).bound();
		}
		else if (aa instanceof TransDTBound)
		{
			return ((TransDTBound)aa).bound();
		}
		else
		{
			throw new UnsupportedOperationException("Was neither TransNode nor TransTotalBound. Should implement it.");
		}
	}

	public final Object Visit(cognipy.cnl.dl.Paragraph p)
	{
		cognipy.cnl.en.paragraph ret = new CNL.EN.paragraph(null);
		ret.sentences = new ArrayList<CNL.EN.sentence>();
		for (Statement x : p.Statements)
		{
			Object o = x.accept(this);
			if (o instanceof java.lang.Iterable<CNL.EN.sentence>)
			{
				ret.sentences.addAll(o instanceof java.lang.Iterable<CNL.EN.sentence> ? (java.lang.Iterable<CNL.EN.sentence>)o : null);
			}
			else
			{
				ret.sentences.add(o instanceof CNL.EN.sentence ? (CNL.EN.sentence)o : null);
			}
		}
		return ret;
	}

	private static class TransNode
	{

		public CNL.EN.orloop makeorloop(boolean isPlural, boolean isModal)
		{
			return makeorloop(isPlural, isModal, null);
		}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual CNL.EN.orloop makeorloop(bool isPlural, bool isModal, TransAtomic atom = null)
		public CNL.EN.orloop makeorloop(boolean isPlural, boolean isModal, TransAtomic atom)
		{
			CNL.EN.orloop orloop = new CNL.EN.orloop(null);
			orloop.exprs = new ArrayList<CNL.EN.andloop>();
			CNL.EN.andloop andloop = new CNL.EN.andloop(null);
			andloop.exprs = new ArrayList<CNL.EN.objectRoleExpr>();
			andloop.exprs.add(objectRoleExpr(isPlural, isModal));
			orloop.exprs.add(andloop);
			return orloop;
		}

		public CNL.EN.subject subject()
		{
			Assert(false);
			return null;
		}

		public CNL.EN.nosubject nosubject()
		{
			return null;
		}

		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			Assert(false);
			return null;
		}
		//public virtual CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    Assert(false);
		//    return null;
		//}
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			Assert(false);
			return null;
		}
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			Assert(false);
			return null;
		}
		public CNL.EN.role role(boolean isPlural, boolean isModal, Boolean isInverse)
		{
			Assert(false);
			return null;
		}
	}

	private String resolveModality(Statement.Modality m)
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

	public final Object Visit(cognipy.cnl.dl.Subsumption p)
	{
		Object LeftSide = p.C.accept(this);
		if (p.D instanceof ConceptNot)
		{
			cognipy.cnl.en.nosubject lss = (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).nosubject();
			if (lss != null)
			{
				Object RightSide = (p.D instanceof ConceptNot ? (ConceptNot)p.D : null).C.accept(this);
				Assert(LeftSide instanceof TransNode);
				Assert(RightSide instanceof TransNode);
				return new CNL.EN.nosubsumption(null, (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).nosubject(), resolveModality(p.modality), (RightSide instanceof TransNode ? (TransNode)RightSide : null).makeorloop(false, p.modality != Statement.Modality.IS));
			}
		}
		{
			Object RightSide = p.D.accept(this);
			Assert(LeftSide instanceof TransNode);
			Assert(RightSide instanceof TransNode);

			CNL.EN.subsumption sent = new CNL.EN.subsumption(null, (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).subject(), resolveModality(p.modality), (RightSide instanceof TransNode ? (TransNode)RightSide : null).makeorloop(false, p.modality != Statement.Modality.IS));

			return sent;
		}
	}

	public final Object Visit(Annotation a)
	{
		return new CNL.EN.annotation(null, a.txt.substring(1));
	}

	public final Object Visit(DLAnnotationAxiom a)
	{
		dlannotationassertion tempVar = new dlannotationassertion(null);
		tempVar.subject = a.getSubject();
		tempVar.subjKind = a.getSubjKind();
		tempVar.annotName = a.annotName;
		tempVar.value = a.value;
		tempVar.language = a.language;
		return tempVar;
	}

	public final Object Visit(cognipy.cnl.dl.Equivalence p)
	{
		ArrayList<CNL.EN.sentence> ret = new ArrayList<CNL.EN.sentence>();
		Object LeftSide = p.Equivalents.get(0).accept(this);
		Assert(LeftSide instanceof TransNode);
		if (p.Equivalents.size() == 1)
		{
			ret.add(new CNL.EN.equivalence2(null, (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).makeorloop(false, p.modality != Statement.Modality.IS), resolveModality(p.modality), (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).makeorloop(false, false)));
		}
		else
		{
			for (int i = 1; i < p.Equivalents.size(); i++)
			{
				Object RightSide = p.Equivalents.get(1).accept(this);
				Assert(RightSide instanceof TransNode);
				ret.add(new CNL.EN.equivalence2(null, (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).makeorloop(false, p.modality != Statement.Modality.IS), resolveModality(p.modality), (RightSide instanceof TransNode ? (TransNode)RightSide : null).makeorloop(false, false)));
			}
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.Disjoint p)
	{
		if (p.Disjoints.size() == 2)
		{
			Object LeftSide = p.Disjoints.get(0).accept(this);
			DL.ConceptNot RightSide = (new DL.ConceptNot(null, p.Disjoints.get(1))).accept(this);
			Assert(LeftSide instanceof TransNode);
			Assert(RightSide instanceof TransNode);
			CNL.EN.subsumption sent = new CNL.EN.subsumption(null, (LeftSide instanceof TransNode ? (TransNode)LeftSide : null).subject(), resolveModality(p.modality), (RightSide instanceof TransNode ? (TransNode)RightSide : null).makeorloop(false, p.modality != Statement.Modality.IS));
			return sent;
		}
		else
		{
			CNL.EN.exclusives ret = new CNL.EN.exclusives(null);
			ret.objectRoleExprs = new ArrayList<objectRoleExpr>();
			for (Node e : p.Disjoints)
			{
				Object d = e.accept(this);
				Assert(d instanceof TransNode);

				ret.objectRoleExprs.add((d instanceof TransNode ? (TransNode)d : null).objectRoleExpr(false, p.modality != Statement.Modality.IS));
			}
			return ret;
		}
	}


	private String defNs2Pfx(String ns)
	{
		if (!ns.endsWith("/") && !ns.endsWith("#") && !ns.contains("#"))
		{
			return ns + "#";
		}
		else
		{
			return ns;
		}
	}

	private boolean usePrefixes = false;
	private tangible.Func1Param<String, String> _ns2Pfx = null;
	private tangible.Func1Param<String, String> getNs2Pfx()
	{
		if (_ns2Pfx == null)
		{
			return (String arg) -> defNs2Pfx(arg);
		}
		else
		{
			return _ns2Pfx;
		}
	}

	private String FromDL(String name, boolean bigName)
	{
		return FromDL(name, endict.WordKind.NormalForm, bigName);
	}

	private String FromDL(String name, endict.WordKind kind, boolean bigName)
	{
		DlName tempVar = new DlName();
		tempVar.id = name;
		cognipy.cnl.dl.DlName.Parts allParts = (tempVar).Split();
		if (usePrefixes)
		{
			if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && allParts.term.startsWith("<") && allParts.term.endsWith(">"))
			{
				String ns = allParts.term.substring(1, 1 + allParts.term.length() - 2);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var tterm = ns2Pfx(ns);
				if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
				{
					allParts.term = tterm;
				}
				// if nothing is found there is no problem.
			}
		}

		return ENNameingConvention.FromDL(allParts.Combine(), kind, bigName).id;
	}

	public final Object Visit(cognipy.cnl.dl.DisjointUnion p)
	{
		CNL.EN.exclusiveunion ret = new CNL.EN.exclusiveunion(null);
		ret.objectRoleExprs = new ArrayList<objectRoleExpr>();
		ret.name = FromDL(p.name, false);
		for (Node e : p.Union)
		{
			Object d = e.accept(this);
			Assert(d instanceof TransNode);

			ret.objectRoleExprs.add((d instanceof TransNode ? (TransNode)d : null).objectRoleExpr(false, false));
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.DataTypeDefinition e)
	{
		Object tempVar = e.B.accept(this);
		return new CNL.EN.datatypedef(null, FromDL(e.name, false), (tempVar instanceof TransAbstractBound ? (TransAbstractBound)tempVar : null).bound());
	}

	public final Object Visit(cognipy.cnl.dl.RoleInclusion e)
	{
		Object r = e.C.accept(this);
		Object s = e.D.accept(this);
		Assert(r instanceof TransNode);
		Assert(s instanceof TransNode);

		boolean inv = e.D instanceof cognipy.cnl.dl.RoleInversion;
		cognipy.cnl.en.roleWithXY rr = convertToRoleWithXY((s instanceof TransNode ? (TransNode)s : null).role(false, e.modality != Statement.Modality.IS, inv));
		rr.inverse = inv;
		CNL.EN.rolesubsumption sent = new CNL.EN.rolesubsumption(null, (r instanceof TransNode ? (TransNode)r : null).role(false, false, false), rr);
		return sent;
	}

	private roleWithXY convertToRoleWithXY(role r)
	{
		return new roleWithXY(r.yyps, r.name, r.inverse);
	}

	private notRoleWithXY convertToNotRoleWithXY(role r)
	{
		return new notRoleWithXY(r.yyps, r.name, r.inverse);
	}

	public final Object Visit(cognipy.cnl.dl.RoleEquivalence p)
	{
		ArrayList<CNL.EN.sentence> ret = new ArrayList<CNL.EN.sentence>();

		CNL.EN.role rol;
		{
			boolean inv = p.Equivalents.get(0) instanceof cognipy.cnl.dl.RoleInversion;
			Object d = p.Equivalents.get(0).accept(this);
			Assert(d instanceof TransNode);
			rol = (d instanceof TransNode ? (TransNode)d : null).role(false, false, false);

		}
		if (p.Equivalents.size() == 1)
		{
			CNL.EN.roleequivalence2 it = new CNL.EN.roleequivalence2(null);
			it.r = rol;
			it.s = convertToRoleWithXY(rol);
			ret.add(it);
		}
		else
		{
			for (int j = 1; j < p.Equivalents.size(); j++)
			{
				CNL.EN.roleequivalence2 it = new CNL.EN.roleequivalence2(null);
				it.r = rol;
				boolean inv = p.Equivalents.get(j) instanceof cognipy.cnl.dl.RoleInversion;
				Object d = p.Equivalents.get(j).accept(this);
				Assert(d instanceof TransNode);
				cognipy.cnl.en.roleWithXY rr = convertToRoleWithXY((d instanceof TransNode ? (TransNode)d : null).role(false, false, null));
				rr.inverse = inv;
				it.s = rr;
				ret.add(it);
			}
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleEquivalence p)
	{
		ArrayList<CNL.EN.sentence> ret = new ArrayList<CNL.EN.sentence>();

		CNL.EN.role rol;
		{
			Object d = p.Equivalents.get(0).accept(this);
			Assert(d instanceof TransNode);
			rol = (d instanceof TransNode ? (TransNode)d : null).role(false, false, false);
		}
		if (p.Equivalents.size() == 1)
		{
			CNL.EN.dataroleequivalence2 it = new CNL.EN.dataroleequivalence2(null);
			it.r = rol;
			it.s = rol;
			ret.add(it);
		}
		else
		{
			for (int j = 1; j < p.Equivalents.size(); j++)
			{
				CNL.EN.dataroleequivalence2 it = new CNL.EN.dataroleequivalence2(null);
				it.r = rol;

				Object d = p.Equivalents.get(j).accept(this);
				Assert(d instanceof TransNode);

				it.s = (d instanceof TransNode ? (TransNode)d : null).role(false, false, false);
				ret.add(it);
			}
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.RoleDisjoint p)
	{
		CNL.EN.roledisjoint2 ret = new CNL.EN.roledisjoint2(null);
		Assert(p.Disjoints.size() == 2);
		for (int i = 0; i < p.Disjoints.size(); i++)
		{
			boolean inv = p.Disjoints.get(i) instanceof cognipy.cnl.dl.RoleInversion;
			Object d = p.Disjoints.get(i).accept(this);
			Assert(d instanceof TransNode);

			if (i == 0)
			{
				ret.r = (d instanceof TransNode ? (TransNode)d : null).role(false, false, false);
			}
			if (i == 1)
			{
				cognipy.cnl.en.notRoleWithXY rr = convertToNotRoleWithXY((d instanceof TransNode ? (TransNode)d : null).role(false, true, null));
				rr.inverse = inv;
				ret.s = rr;
			}
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.ComplexRoleInclusion e)
	{
		CNL.EN.chain z = new CNL.EN.chain(null);
		z.roles = new ArrayList<CNL.EN.role>();
		for (Node r : e.RoleChain)
		{
			Object tempVar = r.accept(this);
			z.roles.add((tempVar instanceof TransNode ? (TransNode)tempVar : null).role(false, false, false));
		}
		boolean inv = e.R instanceof cognipy.cnl.dl.RoleInversion;
		Object t = e.R.accept(this);
		Assert(t instanceof TransNode);
		cognipy.cnl.en.roleWithXY rr = convertToRoleWithXY((t instanceof TransNode ? (TransNode)t : null).role(false, false, false));
		rr.inverse = inv;
		CNL.EN.rolesubsumption sent = new CNL.EN.rolesubsumption(null, z, rr);
		return sent;
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleInclusion e)
	{
		Object r = e.C.accept(this);
		Object s = e.D.accept(this);
		Assert(r instanceof TransNode);
		Assert(s instanceof TransNode);
		CNL.EN.datarolesubsumption sent = new CNL.EN.datarolesubsumption(null, (r instanceof TransNode ? (TransNode)r : null).role(false, false, false), (s instanceof TransNode ? (TransNode)s : null).role(false, e.modality != Statement.Modality.IS, false));
		return sent;
	}

	public final Object Visit(cognipy.cnl.dl.DataRoleDisjoint p)
	{
		CNL.EN.dataroledisjoint2 ret = new CNL.EN.dataroledisjoint2(null);
		Assert(p.Disjoints.size() == 2);
		for (int i = 0; i < p.Disjoints.size(); i++)
		{
			Object d = p.Disjoints.get(i).accept(this);
			Assert(d instanceof TransNode);

			if (i == 0)
			{
				ret.r = (d instanceof TransNode ? (TransNode)d : null).role(false, false, false);
			}
			if (i == 1)
			{
				ret.s = (d instanceof TransNode ? (TransNode)d : null).role(false, true, false);
			}
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.InstanceOf e)
	{
		Object c = e.C.accept(this);
		Object tempVar = e.I.accept(this);
		TransInstanceSingle i = new TransInstanceSingle();
		i.Instance = tempVar instanceof TransInstance ? (TransInstance)tempVar : null;

		CNL.EN.subsumption sent = new CNL.EN.subsumption(null, i.subject(), resolveModality(e.modality), (c instanceof TransNode ? (TransNode)c : null).makeorloop(false, e.modality != Statement.Modality.IS));
		return sent;
	}

	public final Object Visit(cognipy.cnl.dl.RelatedInstances p)
	{
		Object tempVar = p.I.accept(this);
		TransInstanceSingle i = new TransInstanceSingle();
		i.Instance = tempVar instanceof TransInstance ? (TransInstance)tempVar : null;
		Object tempVar2 = p.J.accept(this);
		TransInstanceSingle j = new TransInstanceSingle();
		j.Instance = tempVar2 instanceof TransInstance ? (TransInstance)tempVar2 : null;
		Object tempVar3 = p.R.accept(this);
		TransNode Role = tempVar3 instanceof TransNode ? (TransNode)tempVar3 : null;

		TransSomeRestriction ore = new TransSomeRestriction();
		ore.C = j;
		ore.R = Role;

		CNL.EN.subsumption sent = new CNL.EN.subsumption(null, i.subject(), resolveModality(p.modality), ore.makeorloop(false, p.modality != Statement.Modality.IS));
		return sent;
	}

	public final Object Visit(cognipy.cnl.dl.InstanceValue e)
	{
		Object tempVar = e.I.accept(this);
		TransInstanceSingle i = new TransInstanceSingle();
		i.Instance = tempVar instanceof TransInstance ? (TransInstance)tempVar : null;
		Object tempVar2 = e.V.accept(this);
		TransValue v = tempVar2 instanceof TransValue ? (TransValue)tempVar2 : null;
		Object tempVar3 = e.R.accept(this);
		TransNode Role = tempVar3 instanceof TransNode ? (TransNode)tempVar3 : null;

		TransSomeValueRestriction ore = new TransSomeValueRestriction();
		ore.B = new TransBoundVal();
		ore.B.Kind = "=";
		ore.B.V = v;
		ore.R = Role;

		CNL.EN.subsumption sent = new CNL.EN.subsumption(null, i.subject(), resolveModality(e.modality), ore.makeorloop(false, e.modality != Statement.Modality.IS));
		return sent;
	}

	public final Object Visit(cognipy.cnl.dl.SameInstances p)
	{
		CNL.EN.equivalence2 ret = new CNL.EN.equivalence2(null);
		ret.modality = resolveModality(p.modality);

		Assert(p.Instances.size() == 2);
		for (int i = 0; i < p.Instances.size(); i++)
		{
			Assert(p.Instances.get(i) instanceof NamedInstance);

			TransInstanceSingle ii = new TransInstanceSingle();
			ii.Instance = new TransNamedInstance(this);
			ii.Instance.id = (p.Instances.get(i) instanceof NamedInstance ? (NamedInstance)p.Instances.get(i) : null).name;

			if (i == 0)
			{
				ret.c = ii.makeorloop(false, false);
			}
			if (i == 1)
			{
				ret.d = ii.makeorloop(false, false);
			}
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.DifferentInstances p)
	{
		CNL.EN.exclusives ret = new CNL.EN.exclusives(null);
		ret.objectRoleExprs = new ArrayList<objectRoleExpr>();
		ret.modality = resolveModality(p.modality);
		for (Instance e : p.Instances)
		{
			Assert(e instanceof NamedInstance);

			TransInstanceSingle ii = new TransInstanceSingle();
			ii.Instance = new TransNamedInstance(this);
			ii.Instance.id = (e instanceof NamedInstance ? (NamedInstance)e : null).name;

			ret.objectRoleExprs.add(ii.objectRoleExpr(false, p.modality != Statement.Modality.IS));
		}
		return ret;
	}

	public final Object Visit(cognipy.cnl.dl.HasKey p)
	{
		CNL.EN.haskey ret = new CNL.EN.haskey(null);
		ret.dataroles = new ArrayList<role>();
		ret.roles = new ArrayList<role>();
		Object x = p.C.accept(this);
		Assert(x instanceof TransNode);
		ret.s = (x instanceof TransNode ? (TransNode)x : null).objectRoleExpr(false, false);
		for (Node e : p.Roles)
		{
			Object d = e.accept(this);
			Assert(d instanceof TransNode);
			ret.roles.add((d instanceof TransNode ? (TransNode)d : null).role(false, false, false));
		}
		for (Node e : p.DataRoles)
		{
			Object d = e.accept(this);
			Assert(d instanceof TransNode);
			ret.dataroles.add((d instanceof TransNode ? (TransNode)d : null).role(false, false, false));
		}
		return ret;
	}

	private interface TransInstance
	{
		CNL.EN.instance instance(boolean isPlural, boolean isModal);
	}

	private static class TransNamedInstance implements TransInstance
	{
		public TransNamedInstance(Transform me)
		{
			_me = me;
		}
		private Transform _me;
		public String id;
		public final CNL.EN.instance instance(boolean isPlural, boolean isModal)
		{
			if (id.startsWith("_"))
			{
				return new CNL.EN.instanceBigName(null, _me.FromDL(id.substring(1), true), false);
			}
			else
			{
				return new CNL.EN.instanceBigName(null, _me.FromDL(id, true), true);
			}
		}
	}

	public final Object Visit(cognipy.cnl.dl.NamedInstance e)
	{
		TransNamedInstance tempVar = new TransNamedInstance(this);
		tempVar.id = e.name;
		return tempVar;
	}

	private static class TransUnnamedInstance implements TransInstance
	{
		public TransNode C;
		public final CNL.EN.instance instance(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.instanceThe(null, false, C.single(isPlural, isModal));
		}
	}

	private static class TransUnnamedOnlyInstance implements TransInstance
	{
		public TransNode C;
		public final CNL.EN.instance instance(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.instanceThe(null, true, C.single(isPlural, isModal));
		}
	}

	public final Object Visit(cognipy.cnl.dl.UnnamedInstance e)
	{
		Object o = e.C.accept(this);
		Assert(o instanceof TransNode);
		if (e.Only)
		{
			TransUnnamedOnlyInstance tempVar = new TransUnnamedOnlyInstance();
			tempVar.C = o instanceof TransNode ? (TransNode)o : null;
			return tempVar;
		}
		else
		{
			TransUnnamedInstance tempVar2 = new TransUnnamedInstance();
			tempVar2.C = o instanceof TransNode ? (TransNode)o : null;
			return tempVar2;
		}
	}

	private abstract static class TransValue
	{
		public abstract CNL.EN.dataval dataval();
		public CNL.EN.facet facet(String Kind)
		{
			return new CNL.EN.facet(null, Kind, dataval());
		}
	}

	private static class TransNumber extends TransValue
	{
		public String val;
		@Override
		public dataval dataval()
		{
			return new CNL.EN.Number(null, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.Number e)
	{
		TransNumber tempVar = new TransNumber();
		tempVar.val = e.val;
		return tempVar;
	}

	private static class TransBool extends TransValue
	{
		public String val;
		@Override
		public dataval dataval()
		{
			return new CNL.EN.Bool(null, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.Bool e)
	{
		TransBool tempVar = new TransBool();
		tempVar.val = e.val.equals("[1]") ? "true" : "false";
		return tempVar;
	}

	private static class TransDateTimeVal extends TransValue
	{
		public String val;
		@Override
		public dataval dataval()
		{
			return new CNL.EN.DateTimeData(null, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.DateTimeVal e)
	{
		TransDateTimeVal tempVar = new TransDateTimeVal();
		tempVar.val = e.val;
		return tempVar;
	}

	private static class TransDuration extends TransValue
	{
		public String val;
		@Override
		public dataval dataval()
		{
			return new CNL.EN.Duration(null, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.Duration e)
	{
		TransDuration tempVar = new TransDuration();
		tempVar.val = e.val;
		return tempVar;
	}

	private static class TransString extends TransValue
	{
		public String val;
		@Override
		public dataval dataval()
		{
			return new CNL.EN.StrData(null, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.String e)
	{
		TransString tempVar = new TransString();
		tempVar.val = e.val;
		return tempVar;
	}

	private static class TransFloat extends TransValue
	{
		public String val;
		@Override
		public dataval dataval()
		{
			return new CNL.EN.Float(null, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.Float e)
	{
		TransFloat tempVar = new TransFloat();
		tempVar.val = e.val;
		return tempVar;
	}

	private interface TransAbstractBound
	{
		CNL.EN.abstractbound bound();
	}


	private static class TransDataSetBound implements TransAbstractBound
	{
		public ArrayList<Value> Values;
		public final CNL.EN.abstractbound bound()
		{
			boundOneOf eset = new boundOneOf(null);
			eset.vals = new ArrayList<dataval>();
			for (Value v : Values)
			{
				if (v instanceof cognipy.cnl.dl.String)
				{
					StrData tempVar = new StrData(null);
					tempVar.val = v.getVal();
					eset.vals.add(tempVar);
				}
				else if (v instanceof cognipy.cnl.dl.Float)
				{
					cognipy.cnl.en.Float tempVar2 = new cognipy.cnl.en.Float(null);
					tempVar2.val = v.getVal();
					eset.vals.add(tempVar2);
				}
				else if (v instanceof cognipy.cnl.dl.Number)
				{
					cognipy.cnl.en.Number tempVar3 = new cognipy.cnl.en.Number(null);
					tempVar3.val = v.getVal();
					eset.vals.add(tempVar3);
				}
				else if (v instanceof cognipy.cnl.dl.Bool)
				{
					cognipy.cnl.en.Bool tempVar4 = new cognipy.cnl.en.Bool(null);
					tempVar4.val = (v.getVal().equals("[1]")) ? "true" : "false";
					eset.vals.add(tempVar4);
				}
				else if (v instanceof cognipy.cnl.dl.DateTimeVal)
				{
					cognipy.cnl.en.DateTimeData tempVar5 = new cognipy.cnl.en.DateTimeData(null);
					tempVar5.val = v.getVal();
					eset.vals.add(tempVar5);
				}
				else if (v instanceof cognipy.cnl.dl.Duration)
				{
					cognipy.cnl.en.Duration tempVar6 = new cognipy.cnl.en.Duration(null);
					tempVar6.val = v.getVal();
					eset.vals.add(tempVar6);
				}
				else
				{
					Assert(false);
				}
			}
			return eset;
		}
	}

	private static class TransTotalBound implements TransAbstractBound
	{
		public Value V;
		public final CNL.EN.abstractbound bound()
		{
			if (V instanceof CNL.DL.Float)
			{
				return new CNL.EN.boundTotal(null, "DBL");
			}
			else if (V instanceof CNL.DL.Number)
			{
				return new CNL.EN.boundTotal(null, "NUM");
			}
			else if (V instanceof CNL.DL.Bool)
			{
				return new CNL.EN.boundTotal(null, "BOL");
			}
			else if (V instanceof CNL.DL.String)
			{
				return new CNL.EN.boundTotal(null, "STR");
			}
			else if (V instanceof CNL.DL.DateTimeVal)
			{
				return new CNL.EN.boundTotal(null, "DTM");
			}
			else if (V instanceof CNL.DL.Duration)
			{
				return new CNL.EN.boundTotal(null, "DUR");
			}
			else
			{
				Assert(false);
				return null;
			}
		}
	}

	private static class TransDTBound implements TransAbstractBound
	{
		private Transform _me;
		public TransDTBound(Transform me)
		{
			_me = me;
		}
		public String name;
		public final CNL.EN.abstractbound bound()
		{
			return new CNL.EN.boundDataType(null, _me.FromDL(name, false));
		}
	}
	private static class TransTopBound implements TransAbstractBound
	{
		public final CNL.EN.abstractbound bound()
		{
			return new CNL.EN.boundTop(null);
		}
	}

	private static class TransFacet
	{
		public String Kind;
		public TransValue V;
		public final CNL.EN.facet facet()
		{
			return V.facet(Kind);
		}
	}

	private static class TransBoundFacet implements TransAbstractBound
	{
		public ArrayList<TransFacet> TF;
		public final CNL.EN.abstractbound bound()
		{
			boundFacets bnd = new boundFacets(null);
			bnd.l = new facetList(null);
			bnd.l.Facets = new ArrayList<facet>();
			for (TransFacet f : TF)
			{
				bnd.l.Facets.add(f.facet());
			}
			return bnd;
		}
	}

	public final Object Visit(cognipy.cnl.dl.Facet e)
	{
		throw new IllegalStateException();
	}

	public final Object Visit(cognipy.cnl.dl.FacetList e)
	{
		throw new IllegalStateException();
	}

	public final Object Visit(cognipy.cnl.dl.BoundFacets e)
	{
		ArrayList<TransFacet> tf = new ArrayList<TransFacet>();
		for (Facet f : e.FL.List)
		{
			Object tempVar = f.V.accept(this);
			TransFacet tempVar2 = new TransFacet();
			tempVar2.Kind = f.Kind;
			tempVar2.V = tempVar instanceof TransValue ? (TransValue)tempVar : null;
			tf.add(tempVar2);
		}
		TransBoundFacet tempVar3 = new TransBoundFacet();
		tempVar3.TF = tf;
		return tempVar3;
	}

	private static class TransBoundOr implements TransAbstractBound
	{
		public ArrayList<TransAbstractBound> Bnds;
		public final abstractbound bound()
		{
			boundOr o = new boundOr(null);
			o.List = new ArrayList<abstractbound>();
			for (TransAbstractBound f : Bnds)
			{
				o.List.add(f.bound());
			}
			return o;
		}
	}

	public final Object Visit(BoundOr e)
	{
		ArrayList<TransAbstractBound> bnds = new ArrayList<TransAbstractBound>();
		for (AbstractBound b : e.List)
		{
			Object tempVar = b.accept(this);
			bnds.add(tempVar instanceof TransAbstractBound ? (TransAbstractBound)tempVar : null);
		}
		TransBoundOr tempVar2 = new TransBoundOr();
		tempVar2.Bnds = bnds;
		return tempVar2;
	}

	private static class TransBoundAnd implements TransAbstractBound
	{
		public ArrayList<TransAbstractBound> Bnds;
		public final abstractbound bound()
		{
			boundAnd o = new boundAnd(null);
			o.List = new ArrayList<abstractbound>();
			for (TransAbstractBound f : Bnds)
			{
				o.List.add(f.bound());
			}
			return o;
		}
	}

	public final Object Visit(BoundAnd e)
	{
		ArrayList<TransAbstractBound> bnds = new ArrayList<TransAbstractBound>();
		for (AbstractBound b : e.List)
		{
			Object tempVar = b.accept(this);
			bnds.add(tempVar instanceof TransAbstractBound ? (TransAbstractBound)tempVar : null);
		}
		TransBoundAnd tempVar2 = new TransBoundAnd();
		tempVar2.Bnds = bnds;
		return tempVar2;
	}

	private static class TransBoundNot implements TransAbstractBound
	{
		public TransAbstractBound Bnd;
		public final abstractbound bound()
		{
			boundNot tempVar = new boundNot(null);
			tempVar.bnd = Bnd.bound();
			return tempVar;
		}
	}
	public final Object Visit(BoundNot e)
	{
		Object tempVar = e.B.accept(this);
		TransBoundNot tempVar2 = new TransBoundNot();
		tempVar2.Bnd = tempVar instanceof TransAbstractBound ? (TransAbstractBound)tempVar : null;
		return tempVar2;
	}

	private static class TransBoundVal implements TransAbstractBound
	{
		public String Kind;
		public TransValue V;
		public final CNL.EN.abstractbound bound()
		{
			cognipy.cnl.en.facet f = V.facet(Kind);
			boundVal tempVar = new boundVal(null);
			tempVar.Cmp = f.Cmp;
			tempVar.V = f.V;
			return tempVar;
		}
	}

	public final Object Visit(BoundVal e)
	{
		Object tempVar = e.V.accept(this);
		TransBoundVal tempVar2 = new TransBoundVal();
		tempVar2.Kind = e.Kind;
		tempVar2.V = tempVar instanceof TransValue ? (TransValue)tempVar : null;
		return tempVar2;
	}

	public final Object Visit(cognipy.cnl.dl.TotalBound e)
	{
		TransTotalBound tempVar = new TransTotalBound();
		tempVar.V = e.V;
		return tempVar;
	}

	public final Object Visit(DTBound e)
	{
		TransDTBound tempVar = new TransDTBound(this);
		tempVar.name = e.name;
		return tempVar;
	}

	public final Object Visit(cognipy.cnl.dl.TopBound e)
	{
		return new TransTopBound();
	}

	private static class TransAtomic extends TransNode
	{
		private Transform _me;
		public TransAtomic(Transform me)
		{
			_me = me;
		}
		public String id;
		@Override
		public CNL.EN.nosubject nosubject()
		{
			return new CNL.EN.subjectNo(null, new CNL.EN.singleName(null, _me.FromDL(id, false)));
		}
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEvery(null, new CNL.EN.singleName(null, _me.FromDL(id, false)));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectA(null, new CNL.EN.singleName(null, isPlural ? _me.FromDL(id, CNL.EN.endict.WordKind.PluralFormNoun, false) : _me.FromDL(id, false))));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectA(null, new CNL.EN.singleName(null, isPlural ? (new Transform()).FromDL(id, CNL.EN.endict.WordKind.PluralForm, false) : (new Transform()).FromDL(id, false))));
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleName(null, isPlural ? _me.FromDL(id, CNL.EN.endict.WordKind.PluralFormNoun, false) : _me.FromDL(id, false));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
		@Override
		public CNL.EN.role role(boolean isPlural, boolean isModal, Boolean isInverse)
		{
			if (isInverse != null && isInverse.booleanValue())
			{
				return new CNL.EN.role(null, _me.FromDL(id, CNL.EN.endict.WordKind.SimplePast, false), true);
			}
			else
			{
				return new CNL.EN.role(null, _me.FromDL(id, isPlural ? CNL.EN.endict.WordKind.PluralFormVerb : (isModal ? CNL.EN.endict.WordKind.NormalForm : CNL.EN.endict.WordKind.PastParticiple), false), false);
			}
		}
	}
	public final Object Visit(cognipy.cnl.dl.Atomic e)
	{
		TransAtomic tempVar = new TransAtomic(this);
		tempVar.id = e.id;
		return tempVar;
	}

	private static class TransTop extends TransNode
	{
		@Override
		public CNL.EN.nosubject nosubject()
		{
			return new CNL.EN.subjectNothing(null);
		}
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null);
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectSomething(null));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectSomething(null));
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThing(null);
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectSomething(null);
		}
		@Override
		public CNL.EN.role role(boolean isPlural, boolean isModal, Boolean isInverse)
		{
			return new CNL.EN.role(null, ENNameingConvention.TOPROLENAME, isInverse != null ? isInverse : false);
		}
	}
	public final Object Visit(cognipy.cnl.dl.Top e)
	{
		return new TransTop();
	}

	private static class TransBottom extends TransNode
	{
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectNothing(null));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectNothing(null));
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectNothing(null))))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectNothing(null);
		}
		@Override
		public CNL.EN.role role(boolean isPlural, boolean isModal, Boolean isInverse)
		{
			return new CNL.EN.role(null, ENNameingConvention.BOTTOMROLENAME, isInverse != null ? isInverse : false);
		}
	}
	public final Object Visit(cognipy.cnl.dl.Bottom e)
	{
		return new TransBottom();
	}

	private static class TransRoleInversion extends TransNode
	{
		public TransNode R;
		@Override
		public CNL.EN.role role(boolean isPlural, boolean isModal, Boolean isInverse)
		{
			Boolean val = null;
			if (isInverse != null)
			{
				val = !isInverse.booleanValue();
			}
			return R.role(isPlural, isModal, val);
		}
	}
	public final Object Visit(cognipy.cnl.dl.RoleInversion e)
	{
		Object o = e.R.accept(this);
		Assert(o instanceof TransNode);
		TransRoleInversion tempVar = new TransRoleInversion();
		tempVar.R = o instanceof TransNode ? (TransNode)o : null;
		return tempVar;
	}

	private static class TransInstanceSingle extends TransNode
	{
		public TransInstance Instance;
		@Override
		public CNL.EN.subject subject()
		{
			Object o = Instance.instance(false, false);
			if (o instanceof CNL.EN.instanceThe)
			{
				return new CNL.EN.subjectThe(null, (o instanceof CNL.EN.instanceThe ? (CNL.EN.instanceThe)o : null).only, (o instanceof CNL.EN.instanceThe ? (CNL.EN.instanceThe)o : null).s);
			}
			else if (o instanceof CNL.EN.instanceBigName)
			{
				String n = (o instanceof CNL.EN.instanceBigName ? (CNL.EN.instanceBigName)o : null).name;
				if (n.startsWith("_"))
				{
					return new CNL.EN.subjectBigName(null, n.substring(1), false);
				}
				else
				{
					return new CNL.EN.subjectBigName(null, n, true);
				}
			}
			Assert(false);
			return null;
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			cognipy.cnl.en.instance o = Instance.instance(isPlural, isModal);
			if (o != null)
			{
				return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectInstance(null, o));
			}
			Assert(false);
			return null;
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    var o = Instance.instance(isPlural, isModal);
		//    if (o != null)
		//        return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectInstance(null, o));
		//    Assert(false);
		//    return null;
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, new CNL.EN.objectRoleExpr1(null, false, oobject(isPlural, isModal))))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			cognipy.cnl.en.instance o = Instance.instance(isPlural, isModal);
			if (o != null)
			{
				return new CNL.EN.oobjectInstance(null, o);
			}
			Assert(false);
			return null;
		}
	}
	private static class TransInstanceSet extends TransNode
	{
		public ArrayList<TransInstance> Instances = new ArrayList<TransInstance>();
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEvery(null, single(false, false));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectA(null, single(false, false)));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false,
		//        new CNL.EN.oobjectA(null, single(false, false)));
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			Assert(Instances.size() > 1);

			ArrayList<CNL.EN.instance> insts = new ArrayList<CNL.EN.instance>();

			for (TransInstance i : Instances)
			{
				insts.add(i.instance(false, false));
			}

			CNL.EN.instanceList tempVar = new CNL.EN.instanceList(null);
			tempVar.insts = insts;
			return new CNL.EN.singleOneOf(null, tempVar);
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.InstanceSet e)
	{
		if (e.Instances.isEmpty())
		{
			return new TransBottom();
		}
		else if (e.Instances.size() == 1)
		{
			Object tempVar = e.Instances.get(0).accept(this);
			TransInstanceSingle tempVar2 = new TransInstanceSingle();
			tempVar2.Instance = (tempVar instanceof TransInstance ? (TransInstance)tempVar : null);
			return tempVar2;
		}
		else
		{
			TransInstanceSet ret = new TransInstanceSet();
			for (Instance I : e.Instances)
			{
				Object inner = I.accept(this);
				Assert(inner instanceof TransInstance);
				ret.Instances.add(inner instanceof TransInstance ? (TransInstance)inner : null);
			}
			return ret;
		}
	}

	public final Object Visit(cognipy.cnl.dl.ValueSet e)
	{
		if (e.Values.size() == 1)
		{
			Object tempVar = e.Values.get(0).accept(this);
			TransBoundFacet tempVar2 = new TransBoundFacet();
			TransFacet tempVar3 = new TransFacet();
			tempVar3.Kind = "=";
			tempVar3.V = tempVar instanceof TransValue ? (TransValue)tempVar : null;
			tempVar2.TF = new ArrayList<TransFacet>(Arrays.asList(tempVar3));
			return tempVar2;
		}
		else
		{
			TransDataSetBound ret = new TransDataSetBound();
			ret.Values = e.Values;
			return ret;
		}
	}

	private static class TransConceptOr extends TransNode
	{
		private Transform _me;
		public TransConceptOr(Transform me)
		{
			_me = me;
		}
		public ArrayList<TransNode> Exprs = new ArrayList<TransNode>();
		private TransAtomic findFirstAtom()
		{
			for (TransNode C : Exprs)
			{
				if (C instanceof TransAtomic)
				{
					return C instanceof TransAtomic ? (TransAtomic)C : null;
				}
			}
			return null;
		}

		@Override
		public CNL.EN.orloop makeorloop(boolean isPlural, boolean isModal, TransAtomic atom)
		{
			CNL.EN.orloop orloop = new CNL.EN.orloop(null);
			orloop.exprs = new ArrayList<CNL.EN.andloop>();
			for (TransNode C : Exprs)
			{
				if (C != atom)
				{
					CNL.EN.andloop andloop = new CNL.EN.andloop(null);
					andloop.exprs = new ArrayList<CNL.EN.objectRoleExpr>();
					andloop.exprs.add(C.objectRoleExpr(isPlural, isModal));
					orloop.exprs.add(andloop);
				}
			}
			return orloop;
		}

		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, makeorloop(false, false, null)));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.objectRoleExpr1(null, false, new CNL.EN.oobjectSomethingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null))));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, new CNL.EN.oobjectSomethingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null))));
		//}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			if (Exprs.size() == 1)
			{
				cognipy.cnl.en.Transform.TransAtomic atom = findFirstAtom();
				if (atom == null)
				{
					return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
				}
				else
				{
					return new CNL.EN.singleNameThat(null, _me.FromDL(atom.id, false), new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, atom)));
				}
			}
			else
			{
				return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
			}
		}
	}
	public final Object Visit(cognipy.cnl.dl.ConceptOr e)
	{
		TransConceptOr ret = new TransConceptOr(this);
		for (Node C : e.Exprs)
		{
			Object inner = C.accept(this);
			Assert(inner instanceof TransNode);
			ret.Exprs.add(inner instanceof TransNode ? (TransNode)inner : null);
		}
		return ret;
	}

	private static class TransConceptAnd extends TransNode
	{
		private Transform _me;
		public TransConceptAnd(Transform me)
		{
			_me = me;
		}

		public ArrayList<TransNode> Exprs = new ArrayList<TransNode>();
		private TransAtomic findFirstAtom()
		{
			for (TransNode C : Exprs)
			{
				if (C instanceof TransAtomic)
				{
					return C instanceof TransAtomic ? (TransAtomic)C : null;
				}
			}
			return null;
		}
		@Override
		public CNL.EN.orloop makeorloop(boolean isPlural, boolean isModal, TransAtomic atom)
		{
			CNL.EN.andloop andloop = new CNL.EN.andloop(null);
			andloop.exprs = new ArrayList<CNL.EN.objectRoleExpr>();
			for (TransNode C : Exprs)
			{
				if (C != atom)
				{
					andloop.exprs.add(C.objectRoleExpr(isPlural, isModal));
				}
			}
			return new CNL.EN.orloop(null, andloop);
		}

		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, makeorloop(false, false, null)));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.objectRoleExpr1(null, false, oobject(isPlural, isModal));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, oobject(isPlural, isModal));
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			cognipy.cnl.en.Transform.TransAtomic atom = findFirstAtom();
			if (atom == null)
			{
				return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, null)));
			}
			else
			{
				return new CNL.EN.singleNameThat(null, _me.FromDL(atom.id, false), new CNL.EN.thatOrLoop(null, makeorloop(isPlural, isModal, atom)));
			}
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.ConceptAnd e)
	{
		TransConceptAnd ret = new TransConceptAnd(this);
		for (Node C : e.Exprs)
		{
			Object inner = C.accept(this);
			Assert(inner instanceof TransNode);
			ret.Exprs.add(inner instanceof TransNode ? (TransNode)inner : null);
		}
		return ret;
	}

	private static class TransConceptNot extends TransNode
	{
		private Transform _me;
		public TransConceptNot(Transform me)
		{
			_me = me;
		}
		public TransNode C;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			if (C instanceof TransInstanceSingle)
			{
				TransInstanceSingle ic = C instanceof TransInstanceSingle ? (TransInstanceSingle)C : null;
				if (ic.Instance instanceof TransNamedInstance)
				{
					String n = (ic.Instance instanceof TransNamedInstance ? (TransNamedInstance)ic.Instance : null).id;
					instanceBigName i = n.startsWith("_") ? new instanceBigName(null, _me.FromDL(n.substring(1), true), false) : new instanceBigName(null, _me.FromDL(n, true), true);
					return new CNL.EN.objectRoleExpr1(null, true, new CNL.EN.oobjectInstance(null, i));

				}
			}
			else if (C instanceof TransSomeRestriction)
			{
				TransSomeRestriction ic2 = C instanceof TransSomeRestriction ? (TransSomeRestriction)C : null;
				if (ic2.C instanceof TransInstanceSingle)
				{
					TransInstanceSingle ic = ic2.C instanceof TransInstanceSingle ? (TransInstanceSingle)ic2.C : null;
					if (ic.Instance instanceof TransNamedInstance)
					{
						String n = (ic.Instance instanceof TransNamedInstance ? (TransNamedInstance)ic.Instance : null).id;
						instanceBigName i = n.startsWith("_") ? new instanceBigName(null, _me.FromDL(n.substring(1), true), false) : new instanceBigName(null, _me.FromDL(n, true), true);
						return new CNL.EN.objectRoleExpr2(null, true, new CNL.EN.oobjectInstance(null, i), ic2.R.role(false, true, false));
					}
				}
			}
			return new CNL.EN.objectRoleExpr1(null, true, new CNL.EN.oobjectA(null, C.single(isPlural, isModal)));
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    if (C is TransInstanceSingle)
		//    {
		//        var ic = C as TransInstanceSingle;
		//        if (ic.Instance is TransNamedInstance)
		//        {
		//            var n = (ic.Instance as TransNamedInstance).id;
		//            var i = n.StartsWith("_") ?
		//                new instanceBigName(null, (new Transform()).FromDL(n.Substring(1), true), false)
		//               : new instanceBigName(null, (new Transform()).FromDL(n, true), true);
		//            return new CNL.EN.defObjectRoleExpr1(null, true, new CNL.EN.oobjectInstance(null, i));
		//        }
		//    }
		//    else if (C is TransSomeRestriction)
		//    {
		//        Assert(false);
		//    }
		//    return new CNL.EN.defObjectRoleExpr1(null, true, new CNL.EN.oobjectA(null, C.single(isPlural, isModal)));
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, new CNL.EN.objectRoleExpr1(null, true, new CNL.EN.oobjectA(null, C.single(isPlural, isModal)))))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.ConceptNot e)
	{
		Object o = e.C.accept(this);
		Assert(o instanceof TransNode);
		TransConceptNot tempVar = new TransConceptNot(this);
		tempVar.C = o instanceof TransNode ? (TransNode)o : null;
		return tempVar;
	}

	private static class TransOnlyRestriction extends TransNode
	{
		public TransNode R;
		public TransNode C;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectOnly(null, C.single(true, false)), RN);
			return ore;
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, oobject(isPlural, isModal));
		//}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
	}
	public final Object Visit(cognipy.cnl.dl.OnlyRestriction e)
	{
		Object r = e.R.accept(this);
		Object c = e.C.accept(this);
		Assert(r instanceof TransNode);
		Assert(c instanceof TransNode);
		TransOnlyRestriction tempVar = new TransOnlyRestriction();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		tempVar.C = c instanceof TransNode ? (TransNode)c : null;
		return tempVar;
	}

	private static class TransSomeRestriction extends TransNode
	{
		public TransNode R;
		public TransNode C;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			if ((C instanceof TransConceptNot) || (C instanceof TransConceptOr) || (C instanceof TransConceptAnd))
			{
				ore = new CNL.EN.objectRoleExpr3(null, new thatOrLoop(null, C.makeorloop(isPlural, false)), RN);
				return ore;
			}
			else
			{
				ore = new CNL.EN.objectRoleExpr2(null, false, C instanceof TransTop ? null : C.oobject(isPlural, false), RN);
				return ore;
			}
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    return new CNL.EN.defObjectRoleExpr1(null, false, oobject(isPlural, isModal));
		//}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectSomethingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
	}
	public final Object Visit(cognipy.cnl.dl.SomeRestriction e)
	{
		Object r = e.R.accept(this);
		Object c = e.C.accept(this);
		Assert(r instanceof TransNode);
		Assert(c instanceof TransNode);
		TransSomeRestriction tempVar = new TransSomeRestriction();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		tempVar.C = c instanceof TransNode ? (TransNode)c : null;
		return tempVar;
	}

	private static class TransOnlyValueRestriction extends TransNode
	{
		public TransNode R;
		public TransAbstractBound B;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectOnlyBnd(null, B.bound()), RN);
			return ore;
		}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.OnlyValueRestriction e)
	{
		Object r = e.R.accept(this);
		Object b = e.B.accept(this);
		Assert(r instanceof TransNode);
		Assert(b instanceof TransAbstractBound);
		TransOnlyValueRestriction tempVar = new TransOnlyValueRestriction();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		tempVar.B = b instanceof TransAbstractBound ? (TransAbstractBound)b : null;
		return tempVar;
	}

	private static class TransSomeValueRestriction extends TransNode
	{
		public TransNode R;
		public TransAbstractBound B;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectBnd(null, B.bound()), RN);
			return ore;
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    Assert(false);
		//    return null;
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.SomeValueRestriction e)
	{
		Object r = e.R.accept(this);
		Object b = e.B.accept(this);
		Assert(r instanceof TransNode);
		Assert(b instanceof TransAbstractBound);
		TransSomeValueRestriction tempVar = new TransSomeValueRestriction();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		tempVar.B = b instanceof TransAbstractBound ? (TransAbstractBound)b : null;
		return tempVar;
	}

	private static class TransSelfReference extends TransNode
	{
		public TransNode R;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectSelf(null), RN);
			return ore;
		}
		//public override CNL.EN.defObjectRoleExpr defObjectRoleExpr(bool isPlural, bool isModal)
		//{
		//    Assert(false);
		//    return null;
		//}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.SelfReference e)
	{
		Object r = e.R.accept(this);
		Assert(r instanceof TransNode);
		TransSelfReference tempVar = new TransSelfReference();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		return tempVar;
	}

	private static class TransNumberRestriction extends TransNode
	{
		public String Kind;
		public String N;
		public TransNode R;
		public TransNode C;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectCmp(null, Kind, N, C.single(Long.parseLong(N) != 1, false)), RN);
			return ore;
		}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.NumberRestriction e)
	{
		Object r = e.R.accept(this);
		Object c = e.C.accept(this);
		Assert(r instanceof TransNode);
		Assert(c instanceof TransNode);
		TransNumberRestriction tempVar = new TransNumberRestriction();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		tempVar.C = c instanceof TransNode ? (TransNode)c : null;
		tempVar.Kind = e.Kind;
		tempVar.N = e.N;
		return tempVar;
	}

	private static class TransNumberValueRestriction extends TransNode
	{
		public String Kind;
		public String N;
		public TransNode R;
		public TransAbstractBound B;
		@Override
		public CNL.EN.subject subject()
		{
			return new CNL.EN.subjectEverything(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(false, false)))));
		}
		@Override
		public CNL.EN.objectRoleExpr objectRoleExpr(boolean isPlural, boolean isModal)
		{
			CNL.EN.objectRoleExpr ore = null;
			cognipy.cnl.en.role RN = R.role(isPlural, isModal, false);
			ore = new CNL.EN.objectRoleExpr2(null, false, new CNL.EN.oobjectCmpBnd(null, Kind, N, B.bound()), RN);
			return ore;
		}
		@Override
		public CNL.EN.single single(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.singleThingThat(null, new CNL.EN.thatOrLoop(null, new CNL.EN.orloop(null, new CNL.EN.andloop(null, objectRoleExpr(isPlural, isModal)))));
		}
		@Override
		public oobject oobject(boolean isPlural, boolean isModal)
		{
			return new CNL.EN.oobjectA(null, single(isPlural, isModal));
		}
	}
	public final Object Visit(cognipy.cnl.dl.NumberValueRestriction e)
	{
		Object r = e.R.accept(this);
		Object b = e.B.accept(this);
		Assert(r instanceof TransNode);
		Assert(b instanceof TransAbstractBound);
		TransNumberValueRestriction tempVar = new TransNumberValueRestriction();
		tempVar.R = r instanceof TransNode ? (TransNode)r : null;
		tempVar.B = b instanceof TransAbstractBound ? (TransAbstractBound)b : null;
		tempVar.Kind = e.Kind;
		tempVar.N = e.N;
		return tempVar;
	}


	public static void Assert(boolean b)
	{
		if (!b)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
			System.Diagnostics.Debugger.Break();
//#endif
			throw new IllegalStateException("Conversion Assertion Failed.");
		}
	}

	//////////////// SWRL DL //////////////////////////////////////////////////////////////

	private HashMap<String, String> mapped_dvars = new HashMap<String, String>();
	private HashMap<String, String> mapped_ivars = new HashMap<String, String>();

	private VisitingParam<Boolean> inResult = new VisitingParam<Boolean>(false);

	public final Object Visit(SwrlStatement e)
	{
		mapped_dvars.clear();
		mapped_ivars.clear();
		var2class.clear();
		definedVars.clear();
		class2var.clear();
		remappedIdx.clear();
		var2dataProp.clear();
		var2dataRange.clear();
		allVars.clear();
		identifiedVars.clear();

		{
			//checking
			Object tempVar = e.slp.accept(this);
			CNL.EN.clause slp = tempVar instanceof CNL.EN.clause ? (CNL.EN.clause)tempVar : null;

			CNL.EN.clause_result slc;
			try (inResult.set(true))
			{
				Object tempVar2 = e.slc.accept(this);
				slc = tempVar2 instanceof CNL.EN.clause_result ? (CNL.EN.clause_result)tempVar2 : null;
			}
		}

		{
			try (checkinSimplifier.set(false))
			{
				//solving
				CNL.EN.clause slp;
				Object tempVar3 = e.slp.accept(this);
				slp = tempVar3 instanceof CNL.EN.clause ? (CNL.EN.clause)tempVar3 : null;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var v : var2class.keySet())
				{
					if (!definedVars.contains(v))
					{
						slp.Conditions.add(new condition_exists(null, new objectr_nio(null, new notidentobject(null, FromDL(var2class.get(v), false)))));
					}
				}
				CNL.EN.clause_result slc;
				try (inResult.set(true))
				{
					Object tempVar4 = e.slc.accept(this);
					slc = tempVar4 instanceof CNL.EN.clause_result ? (CNL.EN.clause_result)tempVar4 : null;
				}

				CNL.EN.swrlrule tempVar5 = new CNL.EN.swrlrule(null);
				tempVar5.Predicate = slp;
				tempVar5.Result = slc;
				tempVar5.modality = resolveModality(e.modality);
				return tempVar5;
			}
		}
	}

	private abstract static class TransSwrlItemAtom
	{
		public abstract condition condition();
		public abstract condition_result condition_result();
	}

	private VisitingParam<Boolean> checkinSimplifier = new VisitingParam<Boolean>(true);

	public final Object Visit(SwrlItemList e)
	{
		if (!inResult.get())
		{
			CNL.EN.clause mc = new CNL.EN.clause(null);
			mc.Conditions = new ArrayList<condition>();
			for (int i = 0; i < e.list.size(); i++)
			{
				Object tempVar = e.list.get(i).accept(this);
				TransSwrlItemAtom el = tempVar instanceof TransSwrlItemAtom ? (TransSwrlItemAtom)tempVar : null;
				cognipy.cnl.en.condition cnd = el.condition();
				if (cnd != null)
				{
					mc.Conditions.add(cnd);
				}
			}
			return mc;
		}
		else
		{
			CNL.EN.clause_result mc = new CNL.EN.clause_result(null);
			mc.Conditions = new ArrayList<condition_result>();
			for (int i = 0; i < e.list.size(); i++)
			{
				Object tempVar2 = e.list.get(i).accept(this);
				TransSwrlItemAtom el = tempVar2 instanceof TransSwrlItemAtom ? (TransSwrlItemAtom)tempVar2 : null;
				cognipy.cnl.en.condition_result cnd = el.condition_result();
				if (cnd != null)
				{
					mc.Conditions.add(cnd);
				}
			}
			return mc;
		}
	}

	private HashMap<String, String> var2class = new HashMap<String, String>();
	private HashMap<String, HashSet<String>> class2var = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashMap<String, String>> remappedIdx = new HashMap<String, HashMap<String, String>>();

	private HashSet<String> definedVars = new HashSet<String>();

	private static class TransSwrlInstance extends TransSwrlItemAtom
	{
		private Transform me;

		public TransNode C;
		public TransSwrlIObject I;

		public TransSwrlInstance(Transform me)
		{
			this.me = me;
		}

		@Override
		public condition condition()
		{
			if (me.checkinSimplifier.get())
			{
				if (C instanceof TransAtomic && I instanceof TransSwrlIVar)
				{
					String cls = (C instanceof TransAtomic ? (TransAtomic)C : null).id;
					String ivar = (I instanceof TransSwrlIVar ? (TransSwrlIVar)I : null).VAR;
					if (!me.var2class.containsKey(ivar))
					{
						me.var2class.put(ivar, cls);
					}
					if (!me.class2var.containsKey(cls))
					{
						me.class2var.put(cls, new HashSet<String>());
					}
					me.class2var.get(cls).add(ivar);
				}
				condition_definition tempVar = new condition_definition(null);
				tempVar.objectClass = C.oobject(false, false);
				tempVar.objectA = I.objectr();
				return tempVar;
			}
			else
			{
				if (C instanceof TransAtomic && I instanceof TransSwrlIVar)
				{
					String cls = (C instanceof TransAtomic ? (TransAtomic)C : null).id;
					String ivar = (I instanceof TransSwrlIVar ? (TransSwrlIVar)I : null).VAR;
					if (me.var2class.get(ivar).equals(cls))
					{
						return null;
					}
				}
				condition_definition tempVar2 = new condition_definition(null);
				tempVar2.objectClass = C.oobject(false, false);
				tempVar2.objectA = I.objectr();
				return tempVar2;
			}
		}

		@Override
		public condition_result condition_result()
		{
			condition_result_definition tempVar = new condition_result_definition(null);
			tempVar.objectClass = C.oobject(false, false);
			tempVar.objectA = I.identobject();
			return tempVar;
		}
	}

	public final Object Visit(cognipy.cnl.dl.SwrlInstance e)
	{
		Object tempVar = e.C.accept(this);
		Object tempVar2 = e.I.accept(this);
		TransSwrlInstance tempVar3 = new TransSwrlInstance(this);
		tempVar3.C = tempVar instanceof TransNode ? (TransNode)tempVar : null;
		tempVar3.I = tempVar2 instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar2 : null;
		return tempVar3;
	}

	private static class TransSwrlRole extends TransSwrlItemAtom
	{
		public String R;
		public TransSwrlIObject I, J;
		private Transform _me;
		public TransSwrlRole(Transform me)
		{
			_me = me;
		}
		@Override
		public condition condition()
		{
			return new condition_role(null, I.objectr(), _me.FromDL(R, endict.WordKind.PastParticiple, false), J.objectr(), condition_kind.None);
		}

		@Override
		public condition_result condition_result()
		{
			return new condition_result_role(null, I.identobject(), _me.FromDL(R, endict.WordKind.PastParticiple, false), J.identobject(), condition_kind.None);
		}
	}

	public final Object Visit(cognipy.cnl.dl.SwrlRole e)
	{
		Object tempVar = e.I.accept(this);
		Object tempVar2 = e.J.accept(this);
		TransSwrlRole tempVar3 = new TransSwrlRole(this);
		tempVar3.I = tempVar instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar : null;
		tempVar3.R = e.R;
		tempVar3.J = tempVar2 instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar2 : null;
		return tempVar3;
	}

	private static class TransSwrlSameOrDifferent extends TransSwrlItemAtom
	{
		public TransSwrlIObject I, J;
		public boolean TrueForSame;

		@Override
		public condition condition()
		{
			return new condition_is(null, I.objectr(), J.objectr(), TrueForSame ? condition_kind.None : condition_kind.Not);
		}

		@Override
		public condition_result condition_result()
		{
			return new condition_result_is(null, I.identobject(), J.identobject(), TrueForSame ? condition_kind.None : condition_kind.Not);
		}
	}

	public final Object Visit(cognipy.cnl.dl.SwrlSameAs e)
	{
		Object tempVar = e.I.accept(this);
		Object tempVar2 = e.J.accept(this);
		TransSwrlSameOrDifferent tempVar3 = new TransSwrlSameOrDifferent();
		tempVar3.I = tempVar instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar : null;
		tempVar3.J = tempVar2 instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar2 : null;
		tempVar3.TrueForSame = true;
		return tempVar3;
	}

	public final Object Visit(SwrlDifferentFrom e)
	{
		Object tempVar = e.I.accept(this);
		Object tempVar2 = e.J.accept(this);
		TransSwrlSameOrDifferent tempVar3 = new TransSwrlSameOrDifferent();
		tempVar3.I = tempVar instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar : null;
		tempVar3.J = tempVar2 instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar2 : null;
		tempVar3.TrueForSame = false;
		return tempVar3;
	}

	private HashMap<String, ArrayList<Tuple<String, TransSwrlIObject>>> var2dataProp = new HashMap<String, ArrayList<Tuple<String, TransSwrlIObject>>>();
	private HashMap<String, ArrayList<TransAbstractBound>> var2dataRange = new HashMap<String, ArrayList<TransAbstractBound>>();

	public final boolean mergablePropAndRange(String var)
	{
		return var2dataProp.containsKey(var) && var2dataProp.get(var).size() == 1 && var2dataRange.containsKey(var) && var2dataRange.get(var).size() == 1;
	}

	public final condition_data_property_bound mergePropAndRange(String var)
	{
		return new condition_data_property_bound(null, var2dataProp.get(var).get(0).Item2.objectr(), var2dataProp.get(var).get(0).Item1, var2dataRange.get(var).get(0).bound());
	}

	private static class TransSwrlDataProperty extends TransSwrlItemAtom
	{
		private Transform me;
		public TransSwrlDataProperty(Transform me)
		{
			this.me = me;
		}
		public String R;
		public TransSwrlIObject IO;
		public TransSwrlDObject DO;

		@Override
		public condition condition()
		{
			if (DO.isVar())
			{
				if (me.checkinSimplifier.get())
				{
					String rR = me.FromDL(R, endict.WordKind.PastParticiple, false);
					condition_data_property r = new condition_data_property(null, IO.objectr(), rR, DO.getVar());
					if (!me.var2dataProp.containsKey(DO.getVar()))
					{
						me.var2dataProp.put(DO.getVar(), new ArrayList<Tuple<String, TransSwrlIObject>>());
					}
					me.var2dataProp.get(DO.getVar()).add(Tuple.Create(rR, IO));
					return r;
				}
				else
				{
					if (me.mergablePropAndRange(DO.getVar()))
					{
						return me.mergePropAndRange(DO.getVar());
					}
					else
					{
						return new condition_data_property(null, IO.objectr(), me.FromDL(R, endict.WordKind.PastParticiple, false), DO.getVar());
					}
				}
			}
			else
			{
				return new condition_data_property_bound(null, IO.objectr(), me.FromDL(R, endict.WordKind.PastParticiple, false), new boundFacets(null, new facetList(null, new facet(null, "=", DO.getVal()))));
			}
		}

		@Override
		public condition_result condition_result()
		{
			return new condition_result_data_property(null, IO.identobject(), me.FromDL(R, endict.WordKind.PastParticiple, false), DO.datavaler());
		}
	}

	public final Object Visit(SwrlDataProperty e)
	{
		Object tempVar = e.IO.accept(this);
		Object tempVar2 = e.DO.accept(this);
		TransSwrlDataProperty tempVar3 = new TransSwrlDataProperty(this);
		tempVar3.IO = tempVar instanceof TransSwrlIObject ? (TransSwrlIObject)tempVar : null;
		tempVar3.R = e.R;
		tempVar3.DO = tempVar2 instanceof TransSwrlDObject ? (TransSwrlDObject)tempVar2 : null;
		return tempVar3;
	}

	private static class TransSwrlDataRange extends TransSwrlItemAtom
	{
		private Transform me;
		public TransSwrlDataRange(Transform me)
		{
			this.me = me;
		}
		public TransAbstractBound B;
		public TransSwrlDObject DO;

		@Override
		public condition condition()
		{
			condition_data_bound r = new condition_data_bound(null, DO.datavaler(), B.bound());
			if (DO.isVar())
			{
				if (me.checkinSimplifier.get())
				{
					if (!me.var2dataRange.containsKey(DO.getVar()))
					{
						me.var2dataRange.put(DO.getVar(), new ArrayList<TransAbstractBound>());
					}
					me.var2dataRange.get(DO.getVar()).add(B);
				}
				else
				{
					if (me.mergablePropAndRange(DO.getVar()))
					{
						return null;
					}
					else
					{
						return r;
					}
				}
			}
			return r;
		}

		@Override
		public condition_result condition_result()
		{
			throw new IllegalStateException("No bound for such a case");
		}
	}

	public final Object Visit(SwrlDataRange e)
	{
		Object tempVar = e.DO.accept(this);
		Object tempVar2 = e.B.accept(this);
		TransSwrlDataRange tempVar3 = new TransSwrlDataRange(this);
		tempVar3.DO = tempVar instanceof TransSwrlDObject ? (TransSwrlDObject)tempVar : null;
		tempVar3.B = tempVar2 instanceof TransAbstractBound ? (TransAbstractBound)tempVar2 : null;
		return tempVar3;
	}

	private static class TransSwrlBuiltIn extends TransSwrlItemAtom
	{
		public ArrayList<ITransSwrlObject> Values;
		public String builtInName;

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

		private builtin getBuiltIn()
		{
			String btag = KeyWords.Me.GetTag(mapCode(builtInName));
			if (btag.equals("CMP") || btag.equals("EQ"))
			{
				return new builtin_cmp(null, Values.get(1).datavaler(), builtInName, Values.get(0).datavaler());
			}
			else if (builtInName.equals("plus") || builtInName.equals("times") || builtInName.equals("followed-by"))
			{
				ArrayList<datavaler> lst = new ArrayList<datavaler>();
				for (int i = 0; i < Values.size() - 1; i++)
				{
					lst.add(Values.get(i).datavaler());
				}
				return new builtin_list(null, lst, builtInName, Values.get(Values.size() - 1).datavaler());
			}
			else if (btag.equals("TRANSLATEDREPLACED"))
			{
				return new builtin_trans(null, builtInName, Values.get(0).datavaler(), Values.get(1).datavaler(), Values.get(2).datavaler(), Values.get(3).datavaler());
			}
			else if (btag.equals("ANNOTATION"))
			{
				return new builtin_annot(null, Values.get(0).objectr(), Values.get(1).datavaler(), Values.get(2).datavaler(), Values.get(3).datavaler());
			}
			else if (builtInName.equals("from") || builtInName.equals("before") || builtInName.equals("after"))
			{
				return new builtin_substr(null, Values.get(0).datavaler(), builtInName, Values.get(1).datavaler(), Values.get(2).datavaler());
			}
			else if (builtInName.equals("duration") || builtInName.equals("datetime"))
			{
				String t = Values.get(1).getVal().getVal();
				ArrayList<datavaler> lst = new ArrayList<datavaler>();
				for (int i = 2; i < Values.size(); i++)
				{
					lst.add(Values.get(i).datavaler());
				}

				if (builtInName.equals("duration"))
				{
					duration dur = null;
					if (t.equals("'M'"))
					{
						dur = new duration_m(null, lst.get(0), lst.get(1), lst.get(2), lst.get(3), lst.get(4), lst.get(5));
					}
					else
					{
						dur = new duration_w(null, lst.get(0), lst.get(1), lst.get(2), lst.get(3), lst.get(4), lst.get(5));
					}
					return new builtin_duration(null, dur, Values.get(0).datavaler());
				}
				else
				{
					datetime dtm = null;
					dtm = new datetime(null, lst.get(0), lst.get(1), lst.get(2), lst.get(3), lst.get(4), lst.get(5));
					return new builtin_datetime(null, dtm, Values.get(0).datavaler());
				}
			}
			else if (builtInName.equals("execute"))
			{
				String name = Values.get(0).getVal().getVal();
				ArrayList<iexevar> exevars = new ArrayList<iexevar>();
				for (int i = 1; i < Values.size() - 1; i++)
				{
					ITransSwrlObject el = Values.get(i);
					if ((el instanceof TransSwrlDVar) || (el instanceof TransSwrlDVal))
					{
						exevars.add(el.datavaler());
					}
					else if ((el instanceof TransSwrlIVar) || (el instanceof TransSwrlIVal))
					{
						exevars.add(el.identobject());
					}
					else
					{
						throw new IllegalStateException();
					}
				}
				exeargs tempVar = new exeargs(null);
				tempVar.exevars = exevars;
				return new builtin_exe(null, name, tempVar, Values.get(Values.size() - 1).datavaler());
			}
			else if (Values.size() == 3)
			{
				return new builtin_bin(null, Values.get(0).datavaler(), builtInName, Values.get(1).datavaler(), Values.get(2).datavaler());
			}
			else if (Values.size() == 2)
			{
				if (KeyWords.Me.GetTag(builtInName).equals("UNOP2"))
				{
					return new builtin_unary_free(null, Values.get(1).datavaler(), builtInName, Values.get(0).datavaler());
				}
				else if (builtInName.equals("alpha-representation-of"))
				{
					return new builtin_alpha(null, Values.get(0).objectr(), Values.get(1).datavaler());
				}
				else
				{
					return new builtin_unary_cmp(null, builtInName, Values.get(0).datavaler(), Values.get(1).datavaler());
				}
			}


			throw new UnsupportedOperationException();
		}

		@Override
		public condition condition()
		{
			return new condition_builtin(null, getBuiltIn());
		}

		@Override
		public condition_result condition_result()
		{
			return new condition_result_builtin(null, getBuiltIn());
		}
	}

	public final Object Visit(SwrlBuiltIn e)
	{
		Object tempVar = x.accept(this);
		TransSwrlBuiltIn tempVar2 = new TransSwrlBuiltIn();
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		tempVar2.Values = (from x in e.Values select (tempVar instanceof ITransSwrlObject ? (ITransSwrlObject)tempVar : null)).ToList();
		tempVar2.builtInName = e.builtInName;
		return tempVar2;
	}

	private interface ITransSwrlObject
	{
		identobject identobject();
		objectr objectr();
		datavaler datavaler();
		dataval getVal();
	}

	private abstract static class TransSwrlIObject implements ITransSwrlObject
	{
		public abstract objectr objectr();
		public abstract identobject identobject();

		public final datavaler datavaler()
		{
			throw new UnsupportedOperationException();
		}

		public final dataval getVal()
		{
			throw new UnsupportedOperationException();
		}

	}

	private abstract static class TransSwrlDObject implements ITransSwrlObject
	{
		public abstract datavaler datavaler();
		public abstract boolean isVar();
		public abstract dataval getVal();
		public abstract String getVar();

		public final objectr objectr()
		{
			throw new UnsupportedOperationException();
		}

		public final identobject identobject()
		{
			throw new UnsupportedOperationException();
		}
	}

	private static class TransSwrlDVal extends TransSwrlDObject
	{
		public TransValue Val;

		@Override
		public datavaler datavaler()
		{
			return new datavalval(null, Val.dataval());
		}

		@Override
		public boolean isVar()
		{
			return false;
		}

		@Override
		public dataval getVal()
		{
			return Val.dataval();
		}

		@Override
		public String getVar()
		{
			throw new IllegalStateException();
		}
	}

	public final Object Visit(SwrlDVal e)
	{
		Object tempVar = e.Val.accept(this);
		TransSwrlDVal tempVar2 = new TransSwrlDVal();
		tempVar2.Val = tempVar instanceof TransValue ? (TransValue)tempVar : null;
		return tempVar2;
	}

	private static class TransSwrlDVar extends TransSwrlDObject
	{
		public String VAR;

		@Override
		public datavaler datavaler()
		{
			return new datavalvar(null, VAR);
		}

		@Override
		public boolean isVar()
		{
			return true;
		}

		@Override
		public dataval getVal()
		{
			throw new IllegalStateException();
		}

		@Override
		public String getVar()
		{
			return VAR;
		}
	}

	private static class TransSwrlRoleAtom extends TransSwrlIObject
	{
		public String name;

		@Override
		public identobject identobject()
		{
			throw new UnsupportedOperationException();
		}

		@Override
		public objectr objectr()
		{
			throw new UnsupportedOperationException();
		}

	}

	private static class TransSwrlIVal extends TransSwrlIObject
	{
		private Transform _me;
		public TransSwrlIVal(Transform me)
		{
			_me = me;
		}
		public String I;

		@Override
		public objectr objectr()
		{
			return new objectr_io(null, identobject());
		}

		@Override
		public identobject identobject()
		{
			if (I.startsWith("_"))
			{
				return new identobject_inst(null, new instancer(null, _me.FromDL(I.substring(1), true), false));
			}
			else
			{
				return new identobject_inst(null, new instancer(null, _me.FromDL(I, true), true));
			}
		}
	}

	public final Object Visit(SwrlIVal e)
	{
		TransSwrlIVal tempVar = new TransSwrlIVal(this);
		tempVar.I = e.I;
		return tempVar;
	}

	private HashSet<Tuple<String, String>> identifiedVars = new HashSet<Tuple<String, String>>();
	private HashSet<String> allVars = new HashSet<String>();

	private static class TransSwrlIVar extends TransSwrlIObject
	{
		private Transform _me;
		public TransSwrlIVar(Transform me)
		{
			_me = me;
		}

		public String VAR;

		@Override
		public objectr objectr()
		{
			if (_me.checkinSimplifier.get())
			{
				_me.allVars.add(VAR);
				return new objectr_io(null, identobject());
			}
			else
			{
				_me.definedVars.add(VAR);
				String varidx = null;
				if (_me.var2class.containsKey(VAR))
				{
					if (!_me.preserveVarsNumbering.get())
					{
						if (!_me.remappedIdx.containsKey(_me.var2class.get(VAR)))
						{
							_me.remappedIdx.put(_me.var2class.get(VAR), new HashMap<String, String>());
						}

						if (!_me.remappedIdx.get(_me.var2class.get(VAR)).containsKey(VAR))
						{
							_me.remappedIdx.get(_me.var2class.get(VAR)).put(VAR, (String.valueOf(_me.remappedIdx.get(_me.var2class.get(VAR)).size()));
						}

						boolean addIdx = _me.class2var.containsKey(_me.var2class.get(VAR)) ? _me.class2var.get(_me.var2class.get(VAR)).size() > 1 : true;

						varidx = addIdx ? _me.remappedIdx.get(_me.var2class.get(VAR)).get(VAR) : null;
					}
					else
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var arr = VAR.split("[-]", -1);
						varidx = arr[arr.Length - 1];
						int iid;
						tangible.OutObject<Integer> tempOut_iid = new tangible.OutObject<Integer>();
						if (!tangible.TryParseHelper.tryParseInt(varidx, tempOut_iid))
						{
						iid = tempOut_iid.argValue;
							varidx = null;
						}
					else
					{
						iid = tempOut_iid.argValue;
					}
					}
					if (_me.identifiedVars.add(Tuple.Create(_me.var2class.get(VAR), VAR)))
					{
						return new objectr_nio(null, new notidentobject(null, _me.FromDL(_me.var2class.get(VAR), false), varidx));
					}
					else
					{
						return new objectr_io(null, new identobject_name(null, _me.FromDL(_me.var2class.get(VAR), false), varidx));
					}
				}
				else
				{
					if (!_me.preserveVarsNumbering.get())
					{
						if (!_me.remappedIdx.containsKey(""))
						{
							_me.remappedIdx.put("", new HashMap<String, String>());
						}

						if (!_me.remappedIdx.get("").containsKey(VAR))
						{
							_me.remappedIdx.get("").put(VAR, (String.valueOf(_me.remappedIdx.get("").size()));
						}

						boolean addIdx = _me.allVars.size() != _me.class2var.keySet().size() + 1;
						varidx = addIdx ? _me.remappedIdx.get("").get(VAR) : null;
					}
					else
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var arr = VAR.split("[-]", -1);
						varidx = arr[arr.Length - 1];
						int iid;
						tangible.OutObject<Integer> tempOut_iid2 = new tangible.OutObject<Integer>();
						if (!tangible.TryParseHelper.tryParseInt(varidx, tempOut_iid2))
						{
						iid = tempOut_iid2.argValue;
							varidx = null;
						}
					else
					{
						iid = tempOut_iid2.argValue;
					}
					}
					if (_me.identifiedVars.add(Tuple.Create("", VAR)))
					{
						return new objectr_nio(null, new notidentobject(null, null, varidx));
					}
					else
					{
						return new objectr_io(null, new identobject_name(null, null, varidx));
					}
				}
			}
		}

		@Override
		public identobject identobject()
		{
			if (_me.checkinSimplifier.get())
			{
				_me.allVars.add(VAR);
				return new identobject_name(null, null, VAR);
			}
			else
			{
				_me.definedVars.add(VAR);

				String varidx = null;
				if (_me.var2class.containsKey(VAR))
				{
					if (!_me.preserveVarsNumbering.get())
					{
						if (!_me.remappedIdx.containsKey(_me.var2class.get(VAR)))
						{
							_me.remappedIdx.put(_me.var2class.get(VAR), new HashMap<String, String>());
						}

						if (!_me.remappedIdx.get(_me.var2class.get(VAR)).containsKey(VAR))
						{
							_me.remappedIdx.get(_me.var2class.get(VAR)).put(VAR, (String.valueOf(_me.remappedIdx.get(_me.var2class.get(VAR)).size()));
						}

						boolean addIdx = _me.class2var.containsKey(_me.var2class.get(VAR)) ? _me.class2var.get(_me.var2class.get(VAR)).size() > 1 : true;
						varidx = addIdx ? _me.remappedIdx.get(_me.var2class.get(VAR)).get(VAR) : null;
					}
					else
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var arr = VAR.split("[-]", -1);
						varidx = arr[arr.Length - 1];
						int iid;
						tangible.OutObject<Integer> tempOut_iid = new tangible.OutObject<Integer>();
						if (!tangible.TryParseHelper.tryParseInt(varidx, tempOut_iid))
						{
						iid = tempOut_iid.argValue;
							varidx = null;
						}
					else
					{
						iid = tempOut_iid.argValue;
					}
					}
					return new identobject_name(null, _me.FromDL(_me.var2class.get(VAR), false), varidx);
				}
				else
				{
					if (!_me.preserveVarsNumbering.get())
					{
						if (!_me.remappedIdx.containsKey(""))
						{
							_me.remappedIdx.put("", new HashMap<String, String>());
						}

						if (!_me.remappedIdx.get("").containsKey(VAR))
						{
							_me.remappedIdx.get("").put(VAR, (String.valueOf(_me.remappedIdx.get("").size()));
						}

						boolean addIdx = _me.allVars.size() != _me.class2var.keySet().size() + 1;
						varidx = addIdx ? _me.remappedIdx.get("").get(VAR) : null;
					}
					else
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var arr = VAR.split("[-]", -1);
						varidx = arr[arr.Length - 1];
						int iid;
						tangible.OutObject<Integer> tempOut_iid2 = new tangible.OutObject<Integer>();
						if (!tangible.TryParseHelper.tryParseInt(varidx, tempOut_iid2))
						{
						iid = tempOut_iid2.argValue;
							varidx = null;
						}
					else
					{
						iid = tempOut_iid2.argValue;
					}
					}

					return new identobject_name(null, null, varidx);
				}
			}
		}
	}

	private VisitingParam<Boolean> preserveVarsNumbering = new VisitingParam<Boolean>(false);

	public final Object Visit(SwrlIVar e)
	{
		if (preserveVarsNumbering.get())
		{
			if (!mapped_ivars.containsKey(e.VAR))
			{
				mapped_ivars.put(e.VAR, e.VAR);
			}
		}
		else
		{
			if (!mapped_ivars.containsKey(e.VAR))
			{
				mapped_ivars.put(e.VAR, (String.valueOf(mapped_ivars.size()));
			}
		}
		TransSwrlIVar tempVar = new TransSwrlIVar(this);
		tempVar.VAR = mapped_ivars.get(e.VAR);
		return tempVar;
	}

	public final Object Visit(SwrlDVar e)
	{
		if (preserveVarsNumbering.get())
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var arr = e.VAR.split("[-]", -1);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var varidx = arr[arr.Length - 1];

			if (!mapped_dvars.containsKey(e.VAR))
			{
				mapped_dvars.put(e.VAR, varidx);
			}
		}
		else
		{
			if (!mapped_dvars.containsKey(e.VAR))
			{
				mapped_dvars.put(e.VAR, (String.valueOf(mapped_dvars.size()));
			}
		}
		TransSwrlDVar tempVar = new TransSwrlDVar();
		tempVar.VAR = mapped_dvars.get(e.VAR);
		return tempVar;
	}

	/////////// SWRL DL ///////////////////////////////////////


	public final Object Visit(SwrlIterate e)
	{
		mapped_dvars.clear();
		mapped_ivars.clear();
		var2class.clear();
		class2var.clear();
		remappedIdx.clear();
		definedVars.clear();
		var2dataProp.clear();
		var2dataRange.clear();
		allVars.clear();
		identifiedVars.clear();

		try (preserveVarsNumbering.set(true))
		{

			{
				//checking
				Object tempVar = e.slp.accept(this);
				CNL.EN.clause slp = tempVar instanceof CNL.EN.clause ? (CNL.EN.clause)tempVar : null;

				CNL.EN.clause_result slc;
				try (inResult.set(true))
				{
					Object tempVar2 = e.slc.accept(this);
					slc = tempVar2 instanceof CNL.EN.clause_result ? (CNL.EN.clause_result)tempVar2 : null;
				}
			}

			{
				try (checkinSimplifier.set(false))
				{
					//solving
					CNL.EN.clause slp;
					Object tempVar3 = e.slp.accept(this);
					slp = tempVar3 instanceof CNL.EN.clause ? (CNL.EN.clause)tempVar3 : null;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					for (var v : var2class.keySet())
					{
						if (!definedVars.contains(v))
						{
							slp.Conditions.add(new condition_exists(null, new objectr_nio(null, new notidentobject(null, FromDL(var2class.get(v), false)))));
						}
					}

					CNL.EN.clause_result slc;
					CNL.EN.exeargs args;
					try (inResult.set(true))
					{
						Object tempVar4 = e.vars.accept(this);
						args = tempVar4 instanceof CNL.EN.exeargs ? (CNL.EN.exeargs)tempVar4 : null;
						Object tempVar5 = e.slc.accept(this);
						slc = tempVar5 instanceof CNL.EN.clause_result ? (CNL.EN.clause_result)tempVar5 : null;
					}
					CNL.EN.swrlrulefor tempVar6 = new CNL.EN.swrlrulefor(null);
					tempVar6.Predicate = slp;
					tempVar6.Collection = args;
					tempVar6.Result = slc;
					return tempVar6;
				}
			}
		}
	}


	/////////// EXE DL ///////////////////////////////////////

	public final Object Visit(ExeStatement e)
	{
		mapped_dvars.clear();
		mapped_ivars.clear();
		var2class.clear();
		class2var.clear();
		remappedIdx.clear();
		definedVars.clear();
		var2dataProp.clear();
		var2dataRange.clear();
		allVars.clear();
		identifiedVars.clear();

		try (preserveVarsNumbering.set(true))
		{

			{
				//checking
				Object tempVar = e.slp.accept(this);
				CNL.EN.clause slp = tempVar instanceof CNL.EN.clause ? (CNL.EN.clause)tempVar : null;
			}

			{
				try (checkinSimplifier.set(false))
				{
					//solving
					CNL.EN.clause slp;
					Object tempVar2 = e.slp.accept(this);
					slp = tempVar2 instanceof CNL.EN.clause ? (CNL.EN.clause)tempVar2 : null;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					for (var v : var2class.keySet())
					{
						if (!definedVars.contains(v))
						{
							slp.Conditions.add(new condition_exists(null, new objectr_nio(null, new notidentobject(null, FromDL(var2class.get(v), false)))));
						}
					}
					CNL.EN.exeargs args;
					try (inResult.set(true))
					{
						Object tempVar3 = e.args.accept(this);
						args = tempVar3 instanceof CNL.EN.exeargs ? (CNL.EN.exeargs)tempVar3 : null;
					}

					CNL.EN.exerule tempVar4 = new CNL.EN.exerule(null);
					tempVar4.slp = slp;
					tempVar4.args = args;
					tempVar4.exe = e.exe;
					return tempVar4;
				}
			}
		}
	}

	public final Object Visit(SwrlVarList e)
	{
		CNL.EN.exeargs exeargs = new CNL.EN.exeargs(null);
		exeargs.exevars = new ArrayList<iexevar>();
		for (IExeVar el : e.list)
		{
			if (el instanceof SwrlIVar)
			{
				Object tempVar = (el instanceof SwrlIVar ? (SwrlIVar)el : null).accept(this);
				TransSwrlIVar x = tempVar instanceof TransSwrlIVar ? (TransSwrlIVar)tempVar : null;
				exeargs.exevars.add(x.identobject());
			}
			else
			{
				Object tempVar2 = (el instanceof SwrlDVar ? (SwrlDVar)el : null).accept(this);
				TransSwrlDVar x = tempVar2 instanceof TransSwrlDVar ? (TransSwrlDVar)tempVar2 : null;
				exeargs.exevars.add(x.datavaler());
			}
		}
		return exeargs;
	}

	/////////// EXE DL ///////////////////////////////////////



	public final Object Visit(CodeStatement e)
	{
		CNL.EN.code tempVar = new CNL.EN.code(null);
		tempVar.exe = e.exe;
		return tempVar;
	}
}