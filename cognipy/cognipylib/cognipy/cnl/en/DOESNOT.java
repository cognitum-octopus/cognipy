package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+doesNot+199
public class doesNot extends PartialSymbol
{
	public doesNot(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "doesNot";
	}
	@Override
	public int getYynumEndl()
	{
		return 199;
	}
}