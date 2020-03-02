package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_unary_free+239
public class builtin_unary_free extends builtin
{
	public datavaler a;
	public datavaler b;
	public String tpy;

	public builtin_unary_free(Parser yyp)
	{
		super(yyp);
	}
	public builtin_unary_free(Parser yyp, datavaler a, String tpy, datavaler b)
	{
		super(yyp);
		this.a = a;
		this.b = b;
		this.tpy = tpy;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_unary_free";
	}
	@Override
	public int getYynumEndl()
	{
		return 239;
	}
}