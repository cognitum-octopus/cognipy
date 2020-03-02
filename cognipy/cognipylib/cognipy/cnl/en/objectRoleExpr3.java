package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectRoleExpr3+130
public class objectRoleExpr3 extends objectRoleExpr
{
	public objectRoleExpr3(Parser yyp)
	{
		super(yyp);
	}
	public that t;
	public role r;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public objectRoleExpr3(Parser yyp, that t_, role r_)
	{
		super(yyp);
		t = t_;
		r = r_;
	}


	@Override
	public String getYynameEndl()
	{
		return "objectRoleExpr3";
	}
	@Override
	public int getYynumEndl()
	{
		return 130;
	}
}