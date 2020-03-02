package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+identobject_inst+225
public class identobject_inst extends identobject
{
	public instancer i;
	public identobject_inst(Parser yyp)
	{
		super(yyp);
	}
	public identobject_inst(Parser yyp, instancer i_)
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
		return "identobject_inst";
	}
	@Override
	public int getYynumEndl()
	{
		return 225;
	}
}