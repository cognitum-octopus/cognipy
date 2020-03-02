package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Identity+111
public class Identity extends Node
{
	public Identity(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public Identity(Parser yyp, Node c)
	{
		super(yyp);
	C = c;
	}
	@Override
	public Object accept(IVisitor v)
	{
		throw new IllegalStateException();
	}


	@Override
	public String getYynameDl()
	{
		return "Identity";
	}
	@Override
	public int getYynumDl()
	{
		return 111;
	}
}