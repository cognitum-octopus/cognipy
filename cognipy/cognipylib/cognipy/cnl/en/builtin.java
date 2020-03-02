package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+builtin+234
public class builtin implements iaccept, PartialSymbol
{
	public builtin(Parser yyp)
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
		return "builtin";
	}
	@Override
	public int getYynumEndl()
	{
		return 234;
	}
}