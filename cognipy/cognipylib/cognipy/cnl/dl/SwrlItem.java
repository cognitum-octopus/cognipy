package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlItem+134
public class SwrlItem extends PartialSymbol implements IAccept
{
	public SwrlItem(Parser yyp)
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
		return "SwrlItem";
	}
	@Override
	public int getYynumDl()
	{
		return 134;
	}
}