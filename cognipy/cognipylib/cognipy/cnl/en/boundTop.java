package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundTop+189
public class boundTop extends abstractbound
{
	public boundTop(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "boundTop";
	}
	@Override
	public int getYynumEndl()
	{
		return 189;
	}
}