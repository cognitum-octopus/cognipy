package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+andComma+196
public class andComma extends PartialSymbol
{
	public andComma(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "andComma";
	}
	@Override
	public int getYynumEndl()
	{
		return 196;
	}
}