package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition+203
public class condition implements iaccept, PartialSymbol
{
	public condition(Parser yyp)
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
		return "condition";
	}
	@Override
	public int getYynumEndl()
	{
		return 203;
	}
}