package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectr+219
public class objectr implements iaccept, PartialSymbol
{
	public objectr(Parser yyp)
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
		return "objectr";
	}
	@Override
	public int getYynumEndl()
	{
		return 219;
	}
}