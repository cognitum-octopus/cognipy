package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectOnlyBnd+152
public class oobjectOnlyBnd extends oobjectRelated
{
	public oobjectOnlyBnd(Parser yyp)
	{
		super(yyp);
	}
	public abstractbound b;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectOnlyBnd(Parser yyp, abstractbound b_)
	{
		super(yyp);
	b = b_.me();
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectOnlyBnd";
	}
	@Override
	public int getYynumEndl()
	{
		return 152;
	}
}