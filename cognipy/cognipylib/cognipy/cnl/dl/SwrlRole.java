package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlRole+136
public class SwrlRole extends SwrlItem
{
	public SwrlRole(Parser yyp)
	{
		super(yyp);
	}
	public String R;
	public SwrlIObject I, J;
	public SwrlRole(Parser yyp, ID R, SwrlIObject I, SwrlIObject J)
	{
		super(yyp);
	this.R = R.getYytext();
	this.I = I;
	this.J = J;
	}
	public SwrlRole(Parser yyp, String R, SwrlIObject I, SwrlIObject J)
	{
		super(yyp);
	this.R = R;
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
		return "SwrlRole";
	}
	@Override
	public int getYynumDl()
	{
		return 136;
	}
}