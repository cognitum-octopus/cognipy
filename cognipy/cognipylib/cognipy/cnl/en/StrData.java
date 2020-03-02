package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+StrData+177
public class StrData extends dataval
{
	public StrData(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public StrData(Parser yyp, String v)
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
		return "StrData";
	}
	@Override
	public int getYynumEndl()
	{
		return 177;
	}
}