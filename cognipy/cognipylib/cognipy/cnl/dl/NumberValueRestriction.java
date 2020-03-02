package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+NumberValueRestriction+129
public class NumberValueRestriction extends CardinalRestriction
{
	public NumberValueRestriction(Parser yyp)
	{
		super(yyp);
	}
	public AbstractBound B;
	public NumberValueRestriction(Parser yyp, String k, Node r, String n, AbstractBound b)
	{
		super(yyp);
	Kind = k;
	N = n;
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
		return "NumberValueRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 129;
	}
}