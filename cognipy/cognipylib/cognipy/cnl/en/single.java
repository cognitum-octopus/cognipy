package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+single+163
public class single implements iaccept, PartialSymbol
{
	public single(Parser yyp)
	{
		super(yyp);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "single";
	}
	@Override
	public int getYynumEndl()
	{
		return 163;
	}
}