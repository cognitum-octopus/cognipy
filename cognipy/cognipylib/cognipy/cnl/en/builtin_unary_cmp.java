package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_unary_cmp+238
public class builtin_unary_cmp extends builtin
{
	public datavaler result;
	public datavaler b;
	public String tpy;

	public builtin_unary_cmp(Parser yyp)
	{
		super(yyp);
	}
	public builtin_unary_cmp(Parser yyp, String tpy, datavaler b, datavaler result)
	{
		super(yyp);
		this.result = result;
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
		return "builtin_unary_cmp";
	}
	@Override
	public int getYynumEndl()
	{
		return 238;
	}
}