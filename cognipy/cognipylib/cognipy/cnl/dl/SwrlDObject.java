package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlDObject+145
public class SwrlDObject extends PartialSymbol implements ISwrlObject, IAccept
{
	public SwrlDObject(Parser yyp)
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
		return "SwrlDObject";
	}
	@Override
	public int getYynumDl()
	{
		return 145;
	}
}