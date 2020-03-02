package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Bool+179
public class Bool extends dataval
{
	public Bool(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public Bool(Parser yyp, String v)
	{
		super(yyp);
	val = v;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public String getVal()
	{
		return val.toString();
	}


	@Override
	public String getYynameEndl()
	{
		return "Bool";
	}
	@Override
	public int getYynumEndl()
	{
		return 179;
	}
}