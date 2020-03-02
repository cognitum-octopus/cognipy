package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+singleThing+165
public class singleThing extends single
{
	public singleThing(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "singleThing";
	}
	@Override
	public int getYynumEndl()
	{
		return 165;
	}
}