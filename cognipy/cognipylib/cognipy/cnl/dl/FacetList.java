package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class FacetList extends PartialSymbol implements IAccept
{
	public ArrayList<Facet> List;
	public FacetList(Parser yyp)
	{
		super(yyp);
	}
	public FacetList(Parser yyp, Facet f)
	{
		super(yyp);
	List = new ArrayList<Facet>(Arrays.asList(f));
	}
	public FacetList(Parser yyp, Facet f1, Facet f2)
	{
		super(yyp);
	List = new ArrayList<Facet>(Arrays.asList(f1, f2));
	}
	public FacetList(Parser yyp, FacetList l, Facet f)
	{
		super(yyp);
	List = l.List;
	List.add(f);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "FacetList";
	}
	@Override
	public int getYynumDl()
	{
		return 279;
	}
}