package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_result+213
public class condition_result implements iaccept, PartialSymbol
{
	public condition_result(Parser yyp)
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
		return "condition_result";
	}
	@Override
	public int getYynumEndl()
	{
		return 213;
	}
}