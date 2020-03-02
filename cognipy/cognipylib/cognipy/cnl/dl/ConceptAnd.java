package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+ConceptAnd+119
public class ConceptAnd extends ConceptList
{
	public ConceptAnd(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public int priority()
	{
		return 3;
	}
	public ConceptAnd(Parser yyp, Node c, Node d)
	{
		super(yyp);
		if (c.me() instanceof ConceptAnd)
		{
			cognipy.cnl.dl.Node tempVar = c.me();
			Exprs = (tempVar instanceof ConceptAnd ? (ConceptAnd)tempVar : null).Exprs;
		}
		else
		{
			Exprs = new ArrayList<Node>(Arrays.asList(c.me()));
		}
		if (d.me() instanceof ConceptAnd)
		{
			if (Exprs == null)
			{
				Exprs = new ArrayList<Node>();
			}
			cognipy.cnl.dl.Node tempVar2 = d.me();
			Exprs.addAll((tempVar2 instanceof ConceptAnd ? (ConceptAnd)tempVar2 : null).Exprs);
		}
		else
		{
			if (Exprs == null)
			{
				Exprs = new ArrayList<Node>();
			}
			Exprs.add(d.me());
		}
	}


	@Override
	public String getYynameDl()
	{
		return "ConceptAnd";
	}
	@Override
	public int getYynumDl()
	{
		return 119;
	}
}