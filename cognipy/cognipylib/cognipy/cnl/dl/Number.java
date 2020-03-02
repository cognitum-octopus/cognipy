package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Number+93
public class Number extends Value
{
	public Number(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public Number(Parser yyp, String v)
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
		return "I";
	}


	@Override
	public String getYynameDl()
	{
		return "Number";
	}
	@Override
	public int getYynumDl()
	{
		return 93;
	}
}