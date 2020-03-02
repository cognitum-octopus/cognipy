package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+IdentityBound+107
public class IdentityBound extends AbstractBound
{
	public IdentityBound(Parser yyp)
	{
		super(yyp);
	}
	public AbstractBound B;
	public IdentityBound(Parser yyp, AbstractBound B)
	{
		super(yyp);
	this.B = B;
	}
	@Override
	public Object accept(IVisitor v)
	{
		throw new IllegalStateException();
	}


	@Override
	public String getYynameDl()
	{
		return "IdentityBound";
	}
	@Override
	public int getYynumDl()
	{
		return 107;
	}
}