package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin_alpha+244
public class builtin_alpha extends builtin
{
	public objectr a;
	public datavaler b;
	public String cmp;

	public builtin_alpha(Parser yyp)
	{
		super(yyp);
	}
	public builtin_alpha(Parser yyp, objectr a, datavaler b)
	{
		super(yyp);
		this.a = a;
		this.b = b;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_alpha";
	}
	@Override
	public int getYynumEndl()
	{
		return 244;
	}
}