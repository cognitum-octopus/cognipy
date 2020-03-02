package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class binOp extends PartialSymbol
{
	public binOp(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "binOp";
	}
	@Override
	public int getYynumEndl()
	{
		return 349;
	}
}