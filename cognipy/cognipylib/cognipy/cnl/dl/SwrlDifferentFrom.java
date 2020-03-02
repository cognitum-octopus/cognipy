package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlDifferentFrom+138
public class SwrlDifferentFrom extends SwrlItem
{
	public SwrlDifferentFrom(Parser yyp)
	{
		super(yyp);
	}
	public SwrlIObject I, J;
	public SwrlDifferentFrom(Parser yyp, SwrlIObject I, SwrlIObject J)
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
		return "SwrlDifferentFrom";
	}
	@Override
	public int getYynumDl()
	{
		return 138;
	}
}