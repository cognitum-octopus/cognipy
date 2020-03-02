package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Number+176
public class Number extends dataval
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
	public String getYynameEndl()
	{
		return "Number";
	}
	@Override
	public int getYynumEndl()
	{
		return 176;
	}
}