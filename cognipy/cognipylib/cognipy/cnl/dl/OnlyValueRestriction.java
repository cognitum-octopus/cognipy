package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+OnlyValueRestriction+124
public class OnlyValueRestriction extends Restriction
{
	public OnlyValueRestriction(Parser yyp)
	{
		super(yyp);
	}
	public AbstractBound B;
	public OnlyValueRestriction(Parser yyp, Node r, AbstractBound b)
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
		return "OnlyValueRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 124;
	}
}