package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlIObject+142
public class SwrlIObject extends PartialSymbol implements ISwrlObject, IAccept
{
	public SwrlIObject(Parser yyp)
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
		return "SwrlIObject";
	}
	@Override
	public int getYynumDl()
	{
		return 142;
	}
}