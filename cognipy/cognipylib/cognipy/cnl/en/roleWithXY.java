package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+roleWithXY+161
public class roleWithXY implements iaccept, PartialSymbol
{
	public roleWithXY(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public boolean inverse = false;
	public roleWithXY(Parser yyp, String name_, boolean inverse_)
	{
		super(yyp);
	name = name_;
	inverse = inverse_;
	}

	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "roleWithXY";
	}
	@Override
	public int getYynumEndl()
	{
		return 161;
	}
}