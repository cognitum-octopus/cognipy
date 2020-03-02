package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_datetime+243
public class builtin_datetime extends builtin
{
	public datavaler a;
	public datetime d;

	public builtin_datetime(Parser yyp)
	{
		super(yyp);
	}
	public builtin_datetime(Parser yyp, datetime d, datavaler a)
	{
		super(yyp);
		this.a = a;
		this.d = d;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_datetime";
	}
	@Override
	public int getYynumEndl()
	{
		return 243;
	}
}