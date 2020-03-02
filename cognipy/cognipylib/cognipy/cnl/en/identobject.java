package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+identobject+223
public class identobject implements iexevar, PartialSymbol
{
	public identobject(Parser yyp)
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
		return "identobject";
	}
	@Override
	public int getYynumEndl()
	{
		return 223;
	}
}