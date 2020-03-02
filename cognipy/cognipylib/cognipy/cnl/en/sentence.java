package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+sentence+103
public class sentence implements iaccept, PartialSymbol
{
	public sentence(Parser yyp)
	{
		super(yyp);
	}
	public String modality;
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "sentence";
	}
	@Override
	public int getYynumEndl()
	{
		return 103;
	}
}