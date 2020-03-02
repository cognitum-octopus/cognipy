package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+duration+250
public class duration implements iaccept, PartialSymbol
{
	public duration(Parser yyp)
	{
		super(yyp);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}
	protected static datavaler zeroD = new datavalval(null, new Number(null, "0"));


	@Override
	public String getYynameEndl()
	{
		return "duration";
	}
	@Override
	public int getYynumEndl()
	{
		return 250;
	}
}