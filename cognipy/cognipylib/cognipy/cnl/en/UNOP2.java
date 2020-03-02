package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class unOp2 extends PartialSymbol
{
	public unOp2(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "unOp2";
	}
	@Override
	public int getYynumEndl()
	{
		return 372;
	}
}