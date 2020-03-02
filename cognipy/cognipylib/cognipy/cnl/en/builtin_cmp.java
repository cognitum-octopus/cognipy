package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_cmp+235
public class builtin_cmp extends builtin
{
	public datavaler a;
	public datavaler b;
	public String cmp;

	public builtin_cmp(Parser yyp)
	{
		super(yyp);
	}
	public builtin_cmp(Parser yyp, datavaler a, String cmp, datavaler b)
	{
		super(yyp);
		this.a = a;
		this.b = b;
		this.cmp = cmp;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_cmp";
	}
	@Override
	public int getYynumEndl()
	{
		return 235;
	}
}