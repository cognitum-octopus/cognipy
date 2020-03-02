package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectA+143
public class oobjectA extends oobject
{
	public oobjectA(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	public oobjectA(Parser yyp, single s_)
	{
		super(yyp);
	s = s_;
	}
	public oobjectA(Parser yyp, String name)
	{
		super(yyp);
	s = new singleName(null, name);
	}
	public oobjectA(Parser yyp, String name, that t)
	{
		super(yyp);
	s = new singleNameThat(null, name, t);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectA";
	}
	@Override
	public int getYynumEndl()
	{
		return 143;
	}
}