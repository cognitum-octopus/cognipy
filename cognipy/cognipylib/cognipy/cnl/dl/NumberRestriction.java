package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+NumberRestriction+128
public class NumberRestriction extends CardinalRestriction
{
	public NumberRestriction(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public NumberRestriction(Parser yyp, String k, Node r, String n, Node c)
	{
		super(yyp);
	Kind = k;
	N = n;
	R = r.me();
	C = c.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "NumberRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 128;
	}
}