package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Float+178
public class Float extends dataval
{
	public Float(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public Float(Parser yyp, String v)
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
		return "Float";
	}
	@Override
	public int getYynumEndl()
	{
		return 178;
	}
}