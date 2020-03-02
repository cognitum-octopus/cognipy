package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+beAre+198
public class beAre extends PartialSymbol
{
	public beAre(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "beAre";
	}
	@Override
	public int getYynumEndl()
	{
		return 198;
	}
}