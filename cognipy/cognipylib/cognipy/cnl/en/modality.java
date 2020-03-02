package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+modality+131
public class modality extends PartialSymbol
{
	public modality(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameEndl()
	{
		return "modality";
	}
	@Override
	public int getYynumEndl()
	{
		return 131;
	}
}