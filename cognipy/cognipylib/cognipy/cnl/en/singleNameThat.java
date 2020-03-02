package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+singleNameThat+166
public class singleNameThat extends single
{
	public singleNameThat(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public that t;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public singleNameThat(Parser yyp, String name_, that t_)
	{
		super(yyp);
	name = name_;
	t = t_;
	}


	@Override
	public String getYynameEndl()
	{
		return "singleNameThat";
	}
	@Override
	public int getYynumEndl()
	{
		return 166;
	}
}