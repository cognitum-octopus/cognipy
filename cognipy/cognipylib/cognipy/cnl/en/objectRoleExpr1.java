package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectRoleExpr1+128
public class objectRoleExpr1 extends objectRoleExpr
{
	public objectRoleExpr1(Parser yyp)
	{
		super(yyp);
	}
	public boolean Negated = false;
	public oobject s;
	public objectRoleExpr1(Parser yyp, boolean Negated_, oobject s_)
	{
		super(yyp);
	Negated = Negated_;
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
		return "objectRoleExpr1";
	}
	@Override
	public int getYynumEndl()
	{
		return 128;
	}
}