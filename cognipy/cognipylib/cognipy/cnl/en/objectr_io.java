package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectr_io+221
public class objectr_io extends objectr
{
	public identobject identobject;
	public objectr_io(Parser yyp)
	{
		super(yyp);
	}
	public objectr_io(Parser yyp, identobject identobject)
	{
		super(yyp);
	this.identobject = identobject;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "objectr_io";
	}
	@Override
	public int getYynumEndl()
	{
		return 221;
	}
}