package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectOnlySomethingThat+159
public class oobjectOnlySomethingThat extends oobjectRelated
{
	public oobjectOnlySomethingThat(Parser yyp)
	{
		super(yyp);
	}
	public that t;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectOnlySomethingThat(Parser yyp, that t_)
	{
		super(yyp);
	t = t_;
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectOnlySomethingThat";
	}
	@Override
	public int getYynumEndl()
	{
		return 159;
	}
}