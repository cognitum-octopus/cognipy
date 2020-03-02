package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Bool+96
public class Bool extends Value
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
	public String getTypeTag()
	{
		return "B";
	}


	@Override
	public String getYynameDl()
	{
		return "Bool";
	}
	@Override
	public int getYynumDl()
	{
		return 96;
	}
}