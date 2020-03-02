package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+instance+135
public class instance implements iaccept, PartialSymbol
{
	public instance(Parser yyp)
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
		return "instance";
	}
	@Override
	public int getYynumEndl()
	{
		return 135;
	}
}