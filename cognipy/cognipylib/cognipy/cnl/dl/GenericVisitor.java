package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class GenericVisitor implements cognipy.cnl.dl.IVisitor
{
	protected VisitingParam<String> isKindOf = new VisitingParam<String>("C");

	public Object Visit(cognipy.cnl.dl.Paragraph e)
	{
		ArrayList<Statement> newStmt = new ArrayList<Statement>();
		for (Statement x : e.Statements)
		{
			x.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.Subsumption e)
	{
		e.C.accept(this);
		e.D.accept(this);
		return e;
	}

	public Object Visit(cognipy.cnl.dl.Equivalence e)
	{
		for (int i = 0; i < e.Equivalents.size(); i++)
		{
			e.Equivalents.get(i).accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.Disjoint e)
	{
		for (int i = 0; i < e.Disjoints.size(); i++)
		{
			e.Disjoints.get(i).accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.DisjointUnion e)
	{
		for (int i = 0; i < e.Union.size(); i++)
		{
			e.Union.get(i).accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.HasKey e)
	{
		try (isKindOf.set("D"))
		{
			for (int i = 0; i < e.DataRoles.size(); i++)
			{
				e.DataRoles.get(i).accept(this);
			}
		}
		try (isKindOf.set("R"))
		{
			for (int i = 0; i < e.Roles.size(); i++)
			{
				e.Roles.get(i).accept(this);
			}
		}
		e.C.accept(this);
		return e;
	}

	public Object Visit(cognipy.cnl.dl.RoleInclusion e)
	{
		try (isKindOf.set("R"))
		{
			e.C.accept(this);
			e.D.accept(this);
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.RoleEquivalence e)
	{
		try (isKindOf.set("R"))
		{
			for (int i = 0; i < e.Equivalents.size(); i++)
			{
				e.Equivalents.get(i).accept(this);
			}
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.RoleDisjoint e)
	{
		try (isKindOf.set("R"))
		{
			for (int i = 0; i < e.Disjoints.size(); i++)
			{
				e.Disjoints.get(i).accept(this);
			}
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.DataRoleInclusion e)
	{
		try (isKindOf.set("D"))
		{
			e.C.accept(this);
			e.D.accept(this);
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.DataRoleEquivalence e)
	{
		try (isKindOf.set("D"))
		{
			for (int i = 0; i < e.Equivalents.size(); i++)
			{
				e.Equivalents.get(i).accept(this);
			}
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.DataRoleDisjoint e)
	{
		try (isKindOf.set("D"))
		{
			for (int i = 0; i < e.Disjoints.size(); i++)
			{
				e.Disjoints.get(i).accept(this);
			}
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.ComplexRoleInclusion e)
	{
		try (isKindOf.set("R"))
		{
			for (Node n : e.RoleChain)
			{
				n.accept(this);
			}
			e.R.accept(this);
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.InstanceOf e)
	{
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
		}
		e.I.accept(this);
		return e;
	}

	public Object Visit(cognipy.cnl.dl.RelatedInstances e)
	{
		try (isKindOf.set("R"))
		{
			e.R.accept(this);
		}
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	public Object Visit(cognipy.cnl.dl.NamedInstance e)
	{
		return e;
	}

	public Object Visit(cognipy.cnl.dl.UnnamedInstance e)
	{
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.InstanceValue e)
	{
		try (isKindOf.set("D"))
		{
			e.R.accept(this);
			return e;
		}
		e.I.accept(this);
		e.V.accept(this);
	}

	public Object Visit(cognipy.cnl.dl.SameInstances e)
	{
		for (Instance i : e.Instances)
		{
			i.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.DifferentInstances e)
	{
		for (Instance i : e.Instances)
		{
			i.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.Number e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.String e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.Float e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.Bool e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.TopBound e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.TotalBound e)
	{
		return e;
	}

	public Object Visit(cognipy.cnl.dl.Facet e)
	{
		return e.V.accept(this);
	}

	public Object Visit(cognipy.cnl.dl.FacetList e)
	{
		for (Facet F : e.List)
		{
			F.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.BoundFacets e)
	{
		return e.FL.accept(this);
	}


	public Object Visit(cognipy.cnl.dl.BoundOr e)
	{
		for (AbstractBound C : e.List)
		{
			C.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.BoundAnd e)
	{
		for (AbstractBound C : e.List)
		{
			C.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.BoundNot e)
	{
		e.B.accept(this);
		return e;
	}

	public Object Visit(cognipy.cnl.dl.BoundVal e)
	{
		return e.V.accept(this);
	}

	public Object Visit(cognipy.cnl.dl.ValueSet e)
	{
		for (Value val : e.Values)
		{
			val.accept(this);
		}
		return e;
	}

	public Object Visit(cognipy.cnl.dl.Atomic e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.Top e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.Bottom e)
	{
		return e;
	}
	public Object Visit(cognipy.cnl.dl.RoleInversion e)
	{
		try (isKindOf.set("R"))
		{
			e.R.accept(this);
			return e;
		}
	}
	public Object Visit(cognipy.cnl.dl.InstanceSet e)
	{
		try (isKindOf.set("I"))
		{
			for (Instance I : e.Instances)
			{
				I.accept(this);
			}
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.ConceptOr e)
	{
		try (isKindOf.set("C"))
		{
			for (Node C : e.Exprs)
			{
				C.accept(this);
			}
			return e;
		}
	}

	public Object Visit(cognipy.cnl.dl.ConceptAnd e)
	{
		try (isKindOf.set("C"))
		{
			for (Node C : e.Exprs)
			{
				C.accept(this);
			}
			return e;
		}
	}
	public Object Visit(cognipy.cnl.dl.ConceptNot e)
	{
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
			return e;
		}
	}
	public Object Visit(cognipy.cnl.dl.OnlyRestriction e)
	{
		try (isKindOf.set("R"))
		{
			e.R.accept(this);
		}
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
		}
		return e;
	}
	public Object Visit(cognipy.cnl.dl.SomeRestriction e)
	{
		try (isKindOf.set("R"))
		{
			e.R.accept(this);
		}
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
		}
		return e;
	}
	public Object Visit(cognipy.cnl.dl.OnlyValueRestriction e)
	{
		try (isKindOf.set("D"))
		{
			e.R.accept(this);
		}
		e.B.accept(this);
		return e;
	}
	public Object Visit(cognipy.cnl.dl.SomeValueRestriction e)
	{
		try (isKindOf.set("D"))
		{
			e.R.accept(this);
		}
		e.B.accept(this);
		return e;
	}
	public Object Visit(cognipy.cnl.dl.SelfReference e)
	{
		try (isKindOf.set("R"))
		{
			e.R.accept(this);
		}
		return e;
	}
	public Object Visit(cognipy.cnl.dl.NumberRestriction e)
	{
		try (isKindOf.set("R"))
		{
			e.R.accept(this);
		}
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
		}
		return e;
	}
	public Object Visit(cognipy.cnl.dl.NumberValueRestriction e)
	{
		try (isKindOf.set("D"))
		{
			e.R.accept(this);
		}
		e.B.accept(this);
		return e;
	}

	public Object Visit(Annotation e)
	{
		return e;
	}

	public Object Visit(DLAnnotationAxiom e)
	{
		return e;
	}

	public Object Visit(DateTimeVal e)
	{
		return e;
	}
	public Object Visit(Duration e)
	{
		return e;
	}

	public Object Visit(SwrlStatement e)
	{
		e.slp.accept(this);
		e.slc.accept(this);
		return e;
	}

	public Object Visit(SwrlIterate e)
	{
		e.slp.accept(this);
		e.slc.accept(this);
		e.vars.accept(this);
		return e;
	}

	public Object Visit(SwrlItemList e)
	{
		for (SwrlItem i : e.list)
		{
			i.accept(this);
		}
		return e;
	}

	public Object Visit(SwrlInstance e)
	{
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
		}
		e.I.accept(this);
		return e;
	}

	public Object Visit(SwrlRole e)
	{
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	public Object Visit(SwrlSameAs e)
	{
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	public Object Visit(SwrlDifferentFrom e)
	{
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	public Object Visit(SwrlDataProperty e)
	{
		e.IO.accept(this);
		e.DO.accept(this);
		return e;
	}

	public Object Visit(SwrlDataRange e)
	{
		e.B.accept(this);
		e.DO.accept(this);
		return e;
	}

	public Object Visit(SwrlBuiltIn e)
	{
		for (ISwrlObject v : e.Values)
		{
			v.accept(this);
		}
		return e;
	}

	public Object Visit(ExeStatement e)
	{
		e.slp.accept(this);
		e.args.accept(this);
		return e;
	}

	public Object Visit(SwrlVarList e)
	{
		for (IExeVar x : e.list)
		{
			x.accept(this);
		}
		return e;
	}


	public Object Visit(SwrlDVal e)
	{
		e.Val.accept(this);
		return e;
	}

	public Object Visit(SwrlDVar e)
	{
		return e;
	}

	public Object Visit(SwrlIVal e)
	{
		return e;
	}

	public Object Visit(SwrlIVar e)
	{
		return e;
	}

	public Object Visit(DataTypeDefinition e)
	{
		e.B.accept(this);
		return e;
	}

	public Object Visit(DTBound e)
	{
		return e;
	}

	public Object Visit(CodeStatement e)
	{
		return e;
	}

}