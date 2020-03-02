package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+datavaler+227
public class datavaler implements iexevar, PartialSymbol
{
	public datavaler(Parser yyp)
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
		return "datavaler";
	}
	@Override
	public int getYynumEndl()
	{
		return 227;
	}
}