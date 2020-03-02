package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class unOp extends PartialSymbol
{
	public unOp(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "unOp";
	}
	@Override
	public int getYynumEndl()
	{
		return 359;
	}
}