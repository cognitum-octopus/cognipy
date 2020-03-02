package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+notRoleWithXY+162
public class notRoleWithXY implements iaccept, PartialSymbol
{
	public notRoleWithXY(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public boolean inverse = false;
	public notRoleWithXY(Parser yyp, String name_, boolean inverse_)
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
		return "notRoleWithXY";
	}
	@Override
	public int getYynumEndl()
	{
		return 162;
	}
}