package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+instancer+226
public class instancer implements iaccept, PartialSymbol
{
	public String name;
	public boolean very;
	public instancer(Parser yyp)
	{
		super(yyp);
	}
	public instancer(Parser yyp, String name_, boolean very_)
	{
		super(yyp);
	name = name_;
	very = very_;
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "instancer";
	}
	@Override
	public int getYynumEndl()
	{
		return 226;
	}
}