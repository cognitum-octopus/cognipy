package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobject+142
public class oobject implements iaccept, PartialSymbol
{
	public oobject(Parser yyp)
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
		return "oobject";
	}
	@Override
	public int getYynumEndl()
	{
		return 142;
	}
}