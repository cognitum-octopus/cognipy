package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+ConceptOr+118
public class ConceptOr extends ConceptList
{
	public ConceptOr(Parser yyp)
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
		return 2;
	}
	public ConceptOr(Parser yyp, Node c, Node d)
	{
		super(yyp);
		if (c.me() instanceof ConceptOr)
		{
			cognipy.cnl.dl.Node tempVar = c.me();
			Exprs = (tempVar instanceof ConceptOr ? (ConceptOr)tempVar : null).Exprs;
		}
		else
		{
			Exprs = new ArrayList<Node>(Arrays.asList(c.me()));
		}
		if (d.me() instanceof ConceptOr)
		{
			if (Exprs == null)
			{
				Exprs = new ArrayList<Node>();
			}
			cognipy.cnl.dl.Node tempVar2 = d.me();
			Exprs.addAll((tempVar2 instanceof ConceptOr ? (ConceptOr)tempVar2 : null).Exprs);
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
		return "ConceptOr";
	}
	@Override
	public int getYynumDl()
	{
		return 118;
	}
}