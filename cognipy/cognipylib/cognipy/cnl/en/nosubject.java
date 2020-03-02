package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+nosubject+124
public class nosubject implements iaccept, PartialSymbol
{
	public nosubject(Parser yyp)
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
		return "nosubject";
	}
	@Override
	public int getYynumEndl()
	{
		return 124;
	}
}