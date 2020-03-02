package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+role+160
public class role implements iaccept, PartialSymbol
{
	public role(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public boolean inverse = false;
	public role(Parser yyp, String name_, boolean inverse_)
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
		return "role";
	}
	@Override
	public int getYynumEndl()
	{
		return 160;
	}
}