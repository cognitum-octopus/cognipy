package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+roledisjoint2+114
public class roledisjoint2 extends sentence
{
	public roledisjoint2(Parser yyp)
	{
		super(yyp);
	}
	public role r;
	public notRoleWithXY s;
	public roledisjoint2(Parser yyp, role r_, notRoleWithXY s_)
	{
		super(yyp);
	r = r_;
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
		return "roledisjoint2";
	}
	@Override
	public int getYynumEndl()
	{
		return 114;
	}
}