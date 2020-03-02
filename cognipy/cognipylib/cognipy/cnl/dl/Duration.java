package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Duration+98
public class Duration extends Value
{
	public Duration(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public Duration(Parser yyp, String v)
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
		return "T";
	}


	@Override
	public String getYynameDl()
	{
		return "Duration";
	}
	@Override
	public int getYynumDl()
	{
		return 98;
	}
}