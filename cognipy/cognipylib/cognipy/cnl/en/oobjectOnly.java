package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectOnly+146
public class oobjectOnly extends oobjectRelated
{
	public oobjectOnly(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	public oobjectOnly(Parser yyp, single s_)
	{
		super(yyp);
	s = s_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectOnly";
	}
	@Override
	public int getYynumEndl()
	{
		return 146;
	}
}