package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundIdent+192
public class boundIdent extends abstractbound
{
	public abstractbound bnd;
	public boundIdent(Parser yyp)
	{
		super(yyp);
	}
	public boundIdent(Parser yyp, abstractbound bnd)
	{
		super(yyp);
	this.bnd = bnd.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		throw new IllegalStateException();
	}


	@Override
	public String getYynameEndl()
	{
		return "boundIdent";
	}
	@Override
	public int getYynumEndl()
	{
		return 192;
	}
}