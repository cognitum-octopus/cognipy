package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+RoleInversion+115
public class RoleInversion extends Expression
{
	public RoleInversion(Parser yyp)
	{
		super(yyp);
	}
	public Node R;
	public RoleInversion(Parser yyp, Node r)
	{
		super(yyp);
		R = r.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public int priority()
	{
		return 5;
	}


	@Override
	public String getYynameDl()
	{
		return "RoleInversion";
	}
	@Override
	public int getYynumDl()
	{
		return 115;
	}
}