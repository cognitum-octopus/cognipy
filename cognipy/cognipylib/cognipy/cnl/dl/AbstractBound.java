package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+AbstractBound+100
public class AbstractBound extends PartialSymbol implements IAccept
{
	public AbstractBound(Parser yyp)
	{
		super(yyp);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}
	public int priority()
	{
		return 0;
	}
	public final AbstractBound me()
	{
		return this instanceof IdentityBound ? (this instanceof IdentityBound ? (IdentityBound)this : null).B :this;
	}


	@Override
	public String getYynameDl()
	{
		return "AbstractBound";
	}
	@Override
	public int getYynumDl()
	{
		return 100;
	}
}