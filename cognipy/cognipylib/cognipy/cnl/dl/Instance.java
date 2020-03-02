package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Instance+81
public class Instance extends PartialSymbol implements IAccept
{
	public Instance(Parser yyp)
	{
		super(yyp);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameDl()
	{
		return "Instance";
	}
	@Override
	public int getYynumDl()
	{
		return 81;
	}
}