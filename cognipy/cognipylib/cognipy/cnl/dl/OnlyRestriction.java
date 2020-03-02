package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+OnlyRestriction+122
public class OnlyRestriction extends Restriction
{
	public OnlyRestriction(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public OnlyRestriction(Parser yyp, Node r, Node c)
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
		return "OnlyRestriction";
	}
	@Override
	public int getYynumDl()
	{
		return 122;
	}
}