package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class equalTo extends PartialSymbol
{
	public equalTo(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "equalTo";
	}
	@Override
	public int getYynumEndl()
	{
		return 336;
	}
}