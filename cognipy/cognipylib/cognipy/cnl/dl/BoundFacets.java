package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+BoundFacets+101
public class BoundFacets extends AbstractBound
{
	public BoundFacets(Parser yyp)
	{
		super(yyp);
	}
	public FacetList FL;
	public BoundFacets(Parser yyp, FacetList fl)
	{
		super(yyp);
	FL = fl;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "BoundFacets";
	}
	@Override
	public int getYynumDl()
	{
		return 101;
	}
}