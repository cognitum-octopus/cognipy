package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectInstance+144
public class oobjectInstance extends oobject
{
	public oobjectInstance(Parser yyp)
	{
		super(yyp);
	}
	public instance i;
	public oobjectInstance(Parser yyp, instance i_)
	{
		super(yyp);
	i = i_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectInstance";
	}
	@Override
	public int getYynumEndl()
	{
		return 144;
	}
}