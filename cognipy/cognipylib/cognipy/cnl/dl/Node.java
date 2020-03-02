package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Node+64
public class Node extends PartialSymbol implements IAccept
{
	public Object accept(IVisitor v)
	{
		return null;
	}
	public int priority()
	{
		return 0;
	}
	public final Node me()
	{
		return this instanceof Identity ? (this instanceof Identity ? (Identity)this : null).C :this;
	}
	public Node(Parser yyp)
	{
		super(yyp);
	}


	@Override
	public String getYynameDl()
	{
		return "Node";
	}
	@Override
	public int getYynumDl()
	{
		return 64;
	}
}