package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_bin+237
public class builtin_bin extends builtin
{
	public datavaler result;
	public datavaler b;
	public datavaler d;
	public String tpy;

	public builtin_bin(Parser yyp)
	{
		super(yyp);
	}
	public builtin_bin(Parser yyp, datavaler b, String tpy, datavaler d, datavaler result)
	{
		super(yyp);
		this.result = result;
		this.b = b;
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
		return "builtin_bin";
	}
	@Override
	public int getYynumEndl()
	{
		return 237;
	}
}