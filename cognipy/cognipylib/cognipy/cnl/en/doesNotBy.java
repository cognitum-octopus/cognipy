package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+doesNotBy+200
public class doesNotBy extends PartialSymbol
{
	public doesNotBy(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "doesNotBy";
	}
	@Override
	public int getYynumEndl()
	{
		return 200;
	}
}