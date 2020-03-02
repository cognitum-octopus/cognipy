package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectSomethingThat+158
public class oobjectSomethingThat extends oobject
{
	public oobjectSomethingThat(Parser yyp)
	{
		super(yyp);
	}
	public that t;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectSomethingThat(Parser yyp, that t_)
	{
		super(yyp);
	t = t_;
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectSomethingThat";
	}
	@Override
	public int getYynumEndl()
	{
		return 158;
	}
}