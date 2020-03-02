package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subject+119
public class subject implements iaccept, PartialSymbol
{
	public subject(Parser yyp)
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
		return "subject";
	}
	@Override
	public int getYynumEndl()
	{
		return 119;
	}
}