package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class CmpOrID extends PartialSymbol implements IAccept
{
	public CmpOrID(Parser yyp)
	{
		super(yyp);
	}

	public final Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameDl()
	{
		return "CmpOrID";
	}
	@Override
	public int getYynumDl()
	{
		return 214;
	}
}