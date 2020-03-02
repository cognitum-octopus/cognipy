package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class TransformToJenaRules extends cognipy.cnl.dl.GenericVisitor
{

	private DLToOWLNameConv owlNC;

	public final HashMap<String, String> getInvUriMappings()
	{
		return owlNC.InvUriMappings;
	}
	public final void setInvUriMappings(HashMap<String, String> value)
	{
		owlNC.InvUriMappings = value;
	}

	public final void setOWLDataFactory(DLToOWLNameConv owlNC)
	{
		this.owlNC = owlNC;
	}

	public final String ToOwlName(String name, ARS.EntityKind whatFor)
	{
		return "<" + owlNC.getIRIFromId(name, whatFor).toString() + ">";
	}

	public final DlName ToDL(String uri, ARS.EntityKind makeFor)
	{
		return owlNC.ToDL(uri, makeFor);
	}

	private int freeVarIdBase = 0;
	private VisitingParam<String> activeFreeVarId = new VisitingParam<String>(null);
	public final String newFreeVarId()
	{
		return "?x" + freeVarIdBase++.toString();
	}

	public final String ConvertToGetInstancesOf(CNL.DL.Node n)
	{
		freeVarIdBase = 0;
		String selectVars;
		String whereBlock;
		String lhs;

		try (activeFreeVarId.set(newFreeVarId()))
		{
			Object tempVar = n.accept(this);
			JenaNode sparqlNode = tempVar instanceof JenaNode ? (JenaNode)tempVar : null;
			lhs = sparqlNode.GetFreeVariableId();
			selectVars = sparqlNode.GetFreeVariableId();
			whereBlock = sparqlNode.ToCombinedBlock();
		}
		return whereBlock;
	}

	@Override
	public Object Visit(Top e)
	{
		if (isKindOf.get().equals("C"))
		{
			return new JenaInstanceOfDefinedClass(owlNC, activeFreeVarId.get(), null, newFreeVarId());
		}
		return null;
	}

	@Override
	public Object Visit(Atomic e)
	{
		if (isKindOf.get().equals("C"))
		{
			return new JenaInstanceOfDefinedClass(owlNC, activeFreeVarId.get(), e.id, null);
		}
		if (isKindOf.get().equals("R"))
		{
			return Tuple.Create(false, e.id);
		}
		else
		{
			return e.id;
		}
	}

	@Override
	public Object Visit(RoleInversion e)
	{
		try (isKindOf.set("R"))
		{
			Object tempVar = e.R.accept(this);
			Tuple<Boolean, String> r = tempVar instanceof Tuple<Boolean, String> ? (Tuple<Boolean, String>)tempVar : null;
			return Tuple.Create(!r.Item1, r.Item2);
		}
	}

	@Override
	public Object Visit(SomeRestriction e)
	{
		Tuple<Boolean, String> r;
		try (isKindOf.set("R"))
		{
			Object tempVar = e.R.accept(this);
			r = tempVar instanceof Tuple<Boolean, String> ? (Tuple<Boolean, String>)tempVar : null;
		}

		if (e.C instanceof InstanceSet)
		{
			InstanceSet instSet = e.C instanceof InstanceSet ? (InstanceSet)e.C : null;
			if (instSet.Instances.size() == 1)
			{
				Object tempVar2 = instSet.Instances.get(0);
				return new JenaRelatedToInstance(owlNC, activeFreeVarId.get(), (tempVar2 instanceof NamedInstance ? (NamedInstance)tempVar2 : null).name, r.Item2, r.Item1);
			}
			else
			{
				return null;
			}
		}
		else if (e.C instanceof Top)
		{
			return new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), newFreeVarId(), r.Item2, r.Item1, true);
		}
		else
		{
			JenaNode c;
			try (activeFreeVarId.set(newFreeVarId()))
			{
				Object tempVar3 = e.C.accept(this);
				c = tempVar3 instanceof JenaNode ? (JenaNode)tempVar3 : null;
			}

			JenaRelatedToVariable d = new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), c.GetFreeVariableId(), r.Item2, r.Item1);

			return new JenaAnd(owlNC, activeFreeVarId.get(), new ArrayList<JenaNode>(Arrays.asList( c, d )));
		}
	}

	@Override
	public Object Visit(SelfReference e)
	{
		Tuple<Boolean, String> r;
		try (isKindOf.set("R"))
		{
			Object tempVar = e.R.accept(this);
			r = tempVar instanceof Tuple<Boolean, String> ? (Tuple<Boolean, String>)tempVar : null;
		}

		return new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), activeFreeVarId.get(), r.Item2, r.Item1);
	}

	@Override
	public Object Visit(BoundAnd e)
	{
		Object tempVar = x.accept(this);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		ArrayList<Object> nodes = (from x in e.List select (tempVar instanceof JenaNode ? (JenaNode)tempVar : null)).ToList();

		return new JenaAnd(owlNC, activeFreeVarId.get(), nodes);
	}

	@Override
	public Object Visit(cognipy.cnl.dl.BoundFacets e)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		var nodes = (from x in e.FL.List select new JenaRelatedToValueFilter(owlNC, activeFreeVarId.get(), activeAttribute.get(), x.Kind, x.Kind.equals("=") ? null : newFreeVarId(), x.V)).<JenaNode>ToList();

		if (nodes.Count == 1)
		{
			return nodes.First();
		}
		else
		{
			return new JenaAnd(owlNC, activeFreeVarId.get(), nodes);
		}
	}

	@Override
	public Object Visit(cognipy.cnl.dl.FacetList e)
	{
		ArrayList<Tuple<String, CNL.DL.Value>> r = new ArrayList<Tuple<String, CNL.DL.Value>>();
		for (Facet F : e.List)
		{
			r.add(Tuple.Create(F.Kind, F.V));
		}
		return r;
	}

	@Override
	public Object Visit(BoundVal e)
	{
		return new JenaRelatedToValueFilter(owlNC, activeFreeVarId.get(), activeAttribute.get(), e.Kind, e.Kind.equals("=") ? null : newFreeVarId(), e.V);
	}

	private VisitingParam<String> activeAttribute = new VisitingParam<String>(null);

	@Override
	public Object Visit(SomeValueRestriction e)
	{
		String d;
		try (isKindOf.set("D"))
		{
			Object tempVar = e.R.accept(this);
			d = tempVar instanceof String ? (String)tempVar : null;
		}
		try (activeAttribute.set(d))
		{
			return e.B.accept(this);
		}
	}

	@Override
	public Object Visit(TopBound e)
	{
		return new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), newFreeVarId(), activeAttribute.get(), false, true);
	}

	private String getLiteralDatatypeString(Value v)
	{
		if (v instanceof CNL.DL.Bool)
		{
			return OWL2Datatype.XSD_BOOLEAN.getIRI().toString();
		}
		else if (v instanceof CNL.DL.String)
		{
			return OWL2Datatype.XSD_STRING.getIRI().toString();
		}
		else if (v instanceof CNL.DL.Float)
		{
			return OWL2Datatype.XSD_DOUBLE.getIRI().toString();
		}
		else if (v instanceof CNL.DL.Number)
		{
			return OWL2Datatype.XSD_INT.getIRI().toString();
		}
		else if (v instanceof CNL.DL.DateTimeVal)
		{
			return OWL2Datatype.XSD_DATE_TIME.getIRI().toString();
		}
		else if (v instanceof CNL.DL.Duration)
		{
			return "http://www.w3.org/2001/XMLSchema#dayTimeDuration";
		}
		else
		{
			return OWL2Datatype.RDFS_LITERAL.getIRI().toString();
		}
	}
	@Override
	public Object Visit(TotalBound e)
	{
		String nv = newFreeVarId();
		return new JenaAnd(owlNC, activeFreeVarId.get(), new ArrayList<JenaNode>(Arrays.asList( new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), nv, activeAttribute.get(), false, true), new JenaTotal(owlNC, nv, getLiteralDatatypeString(e.V)))));
	}

	@Override
	public Object Visit(ConceptAnd e)
	{
		try (isKindOf.set("C"))
		{
			ArrayList<JenaNode> nodes = new ArrayList<JenaNode>();
			for (Node expr : e.Exprs)
			{
				Object tempVar = expr.accept(this);
				nodes.add(tempVar instanceof JenaNode ? (JenaNode)tempVar : null);
			}
			return new JenaAnd(owlNC, activeFreeVarId.get(), nodes);
		}
	}

}