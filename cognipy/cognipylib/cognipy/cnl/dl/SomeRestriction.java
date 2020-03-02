package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SomeRestriction+123
public class SomeRestriction extends Restriction
{
	public SomeRestriction(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public SomeRestriction(Parser yyp, Node r, Node c)
	{
		super(yyp);
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
		return "SomeRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 123;
	}
}