package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_substr+240
public class builtin_substr extends builtin
{
	public datavaler result;
	public datavaler b;
	public datavaler c;
	public datavaler d;
	public String tpy;

	public builtin_substr(Parser yyp)
	{
		super(yyp);
	}
	public builtin_substr(Parser yyp, datavaler b, String tpy, datavaler c, datavaler result)
	{
		super(yyp);
		this.result = result;
		this.b = b;
		this.c = c;
		this.d = null;
		this.tpy = tpy;
	}
	public builtin_substr(Parser yyp, datavaler b, String tpy, datavaler c, datavaler d, datavaler result)
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
		return "builtin_substr";
	}
	@Override
	public int getYynumEndl()
	{
		return 240;
	}
}