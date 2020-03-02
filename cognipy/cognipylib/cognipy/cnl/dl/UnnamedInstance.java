package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+UnnamedInstance+83
public class UnnamedInstance extends Instance
{
	public UnnamedInstance(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public boolean Only;
	public UnnamedInstance(Parser yyp, boolean only, Node c)
	{
		super(yyp);
	C = c.me();
	Only = only;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public String toString()
	{
		throw new IllegalStateException();
	}


	@Override
	public String getYynameDl()
	{
		return "UnnamedInstance";
	}
	@Override
	public int getYynumDl()
	{
		return 83;
	}
}