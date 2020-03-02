package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundNot+195
public class boundNot extends abstractbound
{
	@Override
	public int priority()
	{
		return 5;
	}
	public abstractbound bnd;
	public boundNot(Parser yyp)
	{
		super(yyp);
	}
	public boundNot(Parser yyp, abstractbound bnd)
	{
		super(yyp);
	this.bnd = bnd.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "boundNot";
	}
	@Override
	public int getYynumEndl()
	{
		return 195;
	}
}