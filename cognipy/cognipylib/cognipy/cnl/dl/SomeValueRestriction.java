package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SomeValueRestriction+125
public class SomeValueRestriction extends Restriction
{
	public SomeValueRestriction(Parser yyp)
	{
		super(yyp);
	}
	public AbstractBound B;
	public SomeValueRestriction(Parser yyp, Node r, AbstractBound b)
	{
		super(yyp);
	R = r.me();
	B = b.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SomeValueRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 125;
	}
}