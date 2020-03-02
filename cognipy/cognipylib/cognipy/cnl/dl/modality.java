package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+modality+66
public class modality extends PartialSymbol
{
	public Statement.Modality mod = Statement.Modality.values()[0];
	public modality(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameDl()
	{
		return "modality";
	}
	@Override
	public int getYynumDl()
	{
		return 66;
	}
}