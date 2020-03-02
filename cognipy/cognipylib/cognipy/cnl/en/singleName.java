package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+singleName+164
public class singleName extends single
{
	public singleName(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public singleName(Parser yyp, String name_)
	{
		super(yyp);
	name = name_;
	}


	@Override
	public String getYynameEndl()
	{
		return "singleName";
	}
	@Override
	public int getYynumEndl()
	{
		return 164;
	}
}