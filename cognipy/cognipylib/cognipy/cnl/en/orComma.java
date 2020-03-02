package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+orComma+197
public class orComma extends PartialSymbol
{
	public orComma(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "orComma";
	}
	@Override
	public int getYynumEndl()
	{
		return 197;
	}
}