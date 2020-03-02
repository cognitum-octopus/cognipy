package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Float+95
public class Float extends Value
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
	public String getTypeTag()
	{
		return "F";
	}


	@Override
	public String getYynameDl()
	{
		return "Float";
	}
	@Override
	public int getYynumDl()
	{
		return 95;
	}
}