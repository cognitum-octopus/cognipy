package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+word_number+174
public class word_number extends PartialSymbol
{
	public word_number(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "word_number";
	}
	@Override
	public int getYynumEndl()
	{
		return 174;
	}
}