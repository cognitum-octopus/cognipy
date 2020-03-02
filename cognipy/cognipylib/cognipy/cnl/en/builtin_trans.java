package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_trans+241
public class builtin_trans extends builtin
{
	public datavaler result;
	public datavaler b;
	public datavaler c;
	public datavaler d;
	public String tpy;

	public builtin_trans(Parser yyp)
	{
		super(yyp);
	}
	public builtin_trans(Parser yyp, String tpy, datavaler b, datavaler c, datavaler d, datavaler result)
	{
		super(yyp);
		this.result = result;
		this.b = b;
		this.c = c;
		this.d = d;
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
		return "builtin_trans";
	}
	@Override
	public int getYynumEndl()
	{
		return 241;
	}
}