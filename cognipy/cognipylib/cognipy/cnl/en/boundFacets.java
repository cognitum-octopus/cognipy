package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundFacets+185
public class boundFacets extends abstractbound
{
	public boundFacets(Parser yyp)
	{
		super(yyp);
	}
	public facetList l;
	@Override
	public boolean isStrict()
	{
		return l.isStrict();
	}
	@Override
	public dataval getStrictVal()
	{
		return l.getStrictVal();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public boundFacets(Parser yyp, facetList l_)
	{
		super(yyp);
	l = l_;
	}


	@Override
	public String getYynameEndl()
	{
		return "boundFacets";
	}
	@Override
	public int getYynumEndl()
	{
		return 185;
	}
}