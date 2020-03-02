package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+facetList+183
public class facetList implements iaccept, PartialSymbol
{
	public ArrayList<facet> Facets;
	public facetList(Parser yyp)
	{
		super(yyp);
	}
	public facetList(Parser yyp, facet f)
	{
		super(yyp);
	Facets = new ArrayList<facet>(Arrays.asList(f));
	}
	public facetList(Parser yyp, facet f1, facet f2)
	{
		super(yyp);
	Facets = new ArrayList<facet>(Arrays.asList(f1, f2));
	}
	public facetList(Parser yyp, facetList l, facet f)
	{
		super(yyp);
	Facets = l.Facets;
	Facets.add(f);
	}
	public boolean isStrict()
	{
		return Facets.size() == 1 && Facets.get(0).isStrict();
	}
	public dataval getStrictVal()
	{
		if (Facets.size() == 1)
		{
			return Facets.get(0).getStrictVal();
		}
		throw new IllegalStateException();
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "facetList";
	}
	@Override
	public int getYynumEndl()
	{
		return 183;
	}
}