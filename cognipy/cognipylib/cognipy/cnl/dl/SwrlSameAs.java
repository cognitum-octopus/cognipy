package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlSameAs+137
public class SwrlSameAs extends SwrlItem
{
	public SwrlSameAs(Parser yyp)
	{
		super(yyp);
	}
	public SwrlIObject I, J;
	public SwrlSameAs(Parser yyp, SwrlIObject I, SwrlIObject J)
	{
		super(yyp);
	this.I = I;
	this.J = J;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlSameAs";
	}
	@Override
	public int getYynumDl()
	{
		return 137;
	}
}