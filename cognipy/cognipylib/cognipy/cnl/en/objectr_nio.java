package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectr_nio+220
public class objectr_nio extends objectr
{
	public notidentobject notidentobject;
	public objectr_nio(Parser yyp)
	{
		super(yyp);
	}
	public objectr_nio(Parser yyp, notidentobject notidentobject)
	{
		super(yyp);
	this.notidentobject = notidentobject;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "objectr_nio";
	}
	@Override
	public int getYynumEndl()
	{
		return 220;
	}
}