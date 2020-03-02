package cognipy.cnl;

import cognipy.cnl.dl.*;
import cognipy.*;
import java.util.*;

public class DLModSimplifier extends GenericVisitor
{
	private NamedInstance getSingleNamgedInstance(Node C)
	{
		if (C instanceof InstanceSet)
		{
			if ((C instanceof InstanceSet ? (InstanceSet)C : null).Instances.size() == 1)
			{
				if ((C instanceof InstanceSet ? (InstanceSet)C : null).Instances.get(0) instanceof NamedInstance)
				{
					return (C instanceof InstanceSet ? (InstanceSet)C : null).Instances.get(0) instanceof NamedInstance ? (NamedInstance)(C instanceof InstanceSet ? (InstanceSet)C : null).Instances.get(0) : null;
				}
			}
		}
		return null;
	}

	private Value getSingleEqualValue(AbstractBound C)
	{
		if (C instanceof ValueSet)
		{
			if ((C instanceof ValueSet ? (ValueSet)C : null).Values.size() == 1)
			{
				return (C instanceof ValueSet ? (ValueSet)C : null).Values.get(0) instanceof Value ? (Value)(C instanceof ValueSet ? (ValueSet)C : null).Values.get(0) : null;
			}
		}
		else if (C instanceof BoundFacets)
		{
			if ((C instanceof BoundFacets ? (BoundFacets)C : null).FL.List.size() == 1 && (C instanceof BoundFacets ? (BoundFacets)C : null).FL.List.get(0).Kind.equals("="))
			{
				return (C instanceof BoundFacets ? (BoundFacets)C : null).FL.List.get(0).V;
			}
		}
		else if (C instanceof BoundVal)
		{
			if ((C instanceof BoundVal ? (BoundVal)C : null).Kind.equals("="))
			{
				return (C instanceof BoundVal ? (BoundVal)C : null).V;
			}
		}
		else if (C instanceof BoundOr)
		{
			if ((C instanceof BoundOr ? (BoundOr)C : null).List.size() == 1)
			{
				return getSingleEqualValue((C instanceof BoundOr ? (BoundOr)C : null).List.get(0));
			}
		}
		else if (C instanceof BoundAnd)
		{
			if ((C instanceof BoundAnd ? (BoundAnd)C : null).List.size() == 1)
			{
				return getSingleEqualValue((C instanceof BoundAnd ? (BoundAnd)C : null).List.get(0));
			}
		}
		return null;
	}

	@Override
	public Object Visit(Paragraph e)
	{
		ArrayList<Statement> newStmt = new ArrayList<Statement>();
		for (Statement x : e.Statements)
		{
			Object o = x.accept(this);
			if (o instanceof Statement)
			{
				newStmt.add(o instanceof Statement ? (Statement)o : null);
			}
			if (o instanceof java.lang.Iterable<Statement>)
			{
				newStmt.addAll(o instanceof java.lang.Iterable<Statement> ? (java.lang.Iterable<Statement>)o : null);
			}
		}
		Paragraph tempVar = new Paragraph(null);
		tempVar.Statements = newStmt;
		return tempVar;
	}

	private Object ConvertToAbox(NamedInstance iC, Statement.Modality modality, Node D)
	{
		if (D instanceof SomeRestriction)
		{
			cognipy.cnl.dl.NamedInstance iS = getSingleNamgedInstance((D instanceof SomeRestriction ? (SomeRestriction)D : null).C);
			if (iS != null)
			{
				cognipy.cnl.dl.Node r = (D instanceof SomeRestriction ? (SomeRestriction)D : null).R;

				boolean inv = false;
				while (r instanceof RoleInversion)
				{
					r = (r instanceof RoleInversion ? (RoleInversion)r : null).R;
					inv = !inv;
				}
				if (!inv)
				{
					RelatedInstances tempVar = new RelatedInstances(null);
					tempVar.R = r;
					tempVar.I = iC;
					tempVar.J = iS;
					tempVar.modality = modality;
					return tempVar;
				}
				else
				{
					RelatedInstances tempVar2 = new RelatedInstances(null);
					tempVar2.R = r;
					tempVar2.J = iC;
					tempVar2.I = iS;
					tempVar2.modality = modality;
					return tempVar2;
				}
			}
		}
		else if (D instanceof ConceptAnd)
		{
			ArrayList<Statement> ret = new ArrayList<Statement>();

			for (Node E : (D instanceof ConceptAnd ? (ConceptAnd)D : null).Exprs)
			{
				Object r = ConvertToAbox(iC, modality, E);
				if (r == null)
				{
					return null;
				}
				else if (r instanceof ArrayList<Statement>)
				{
					ret.addAll(r instanceof ArrayList<Statement> ? (ArrayList<Statement>)r : null);
				}
				else if (r instanceof Statement)
				{
					ret.add(r instanceof Statement ? (Statement)r : null);
				}
				else
				{
					throw new IllegalStateException();
				}
			}
			return ret;
		}
		else if (D instanceof SomeValueRestriction)
		{
			cognipy.cnl.dl.Value iV = getSingleEqualValue((D instanceof SomeValueRestriction ? (SomeValueRestriction)D : null).B);
			if (iV != null)
			{
				InstanceValue tempVar3 = new InstanceValue(null);
				tempVar3.R = (D instanceof SomeValueRestriction ? (SomeValueRestriction)D : null).R;
				tempVar3.I = iC;
				tempVar3.V = iV;
				tempVar3.modality = modality;
				return tempVar3;
			}
		}

		InstanceOf tempVar4 = new InstanceOf(null);
		tempVar4.I = new NamedInstance(null);
		tempVar4.I.name = iC.name;
		tempVar4.C = D;
		tempVar4.modality = modality;
		return tempVar4;
	}

	@Override
	public Object Visit(Subsumption e)
	{
		cognipy.cnl.dl.NamedInstance iC = getSingleNamgedInstance(e.C);
		cognipy.cnl.dl.NamedInstance iD = getSingleNamgedInstance(e.D);
		if (iC != null)
		{
			if (iD != null)
			{
				InstanceList list = new InstanceList(null);
				list.List = new ArrayList<Instance>(Arrays.asList(new Instance[] {iC, iD}));
				return new SameInstances(null, list, e.modality);
			}
			else
			{
				return ConvertToAbox(iC, e.modality, e.D);
			}
		}
		return e;
	}

	@Override
	public Object Visit(RelatedInstances e)
	{
		cognipy.cnl.dl.Node r = e.R;

		boolean inv = false;
		while (r instanceof RoleInversion)
		{
			r = (r instanceof RoleInversion ? (RoleInversion)r : null).R;
			inv = !inv;
		}
		if (!inv)
		{
			return e;
		}
		else
		{
			RelatedInstances tempVar = new RelatedInstances(null);
			tempVar.R = r;
			tempVar.J = e.I;
			tempVar.I = e.J;
			tempVar.modality = e.modality;
			return tempVar;
		}
	}

}