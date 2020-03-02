package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+singleThingThat+167
public class singleThingThat extends single
{
	public singleThingThat(Parser yyp)
	{
		super(yyp);
	}
	public that t;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public singleThingThat(Parser yyp, that t_)
	{
		super(yyp);
	t = t_;
	}


	@Override
	public String getYynameEndl()
	{
		return "singleThingThat";
	}
	@Override
	public int getYynumEndl()
	{
		return 167;
	}
}