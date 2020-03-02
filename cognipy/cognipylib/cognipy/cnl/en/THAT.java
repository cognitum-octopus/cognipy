package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+that+169
public class that implements iaccept, PartialSymbol
{
	public that(Parser yyp)
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
		return "that";
	}
	@Override
	public int getYynumEndl()
	{
		return 169;
	}
}