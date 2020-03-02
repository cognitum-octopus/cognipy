package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_duration+242
public class builtin_duration extends builtin
{
	public datavaler a;
	public duration d;

	public builtin_duration(Parser yyp)
	{
		super(yyp);
	}
	public builtin_duration(Parser yyp, duration d, datavaler a)
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
		return "builtin_duration";
	}
	@Override
	public int getYynumEndl()
	{
		return 242;
	}
}