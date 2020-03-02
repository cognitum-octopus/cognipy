package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlInstance+135
public class SwrlInstance extends SwrlItem
{
	public SwrlInstance(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public SwrlIObject I;
	public SwrlInstance(Parser yyp, Node C, SwrlIObject I)
	{
		super(yyp);
	this.C = C.me();
	this.I = I;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlInstance";
	}
	@Override
	public int getYynumDl()
	{
		return 135;
	}
}