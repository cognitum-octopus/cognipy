package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectBnd+151
public class oobjectBnd extends oobjectRelated
{
	public oobjectBnd(Parser yyp)
	{
		super(yyp);
	}
	public abstractbound b;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectBnd(Parser yyp, abstractbound b_)
	{
		super(yyp);
	b = b_.me();
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectBnd";
	}
	@Override
	public int getYynumEndl()
	{
		return 151;
	}
}