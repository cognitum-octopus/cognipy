package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+instanceBigName+137
public class instanceBigName extends instance
{
	public instanceBigName(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public boolean very;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public instanceBigName(Parser yyp, String name_, boolean very_)
	{
		super(yyp);
	name = name_;
	very = very_;
	}


	@Override
	public String getYynameEndl()
	{
		return "instanceBigName";
	}
	@Override
	public int getYynumEndl()
	{
		return 137;
	}
}