package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Atomic+112
public class Atomic extends Node
{
	public String id;
	public Atomic(Parser yyp)
	{
		super(yyp);
	}
	public Atomic(Parser yyp, ID A)
	{
		super(yyp);
	id = A.getYytext();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "Atomic";
	}
	@Override
	public int getYynumDl()
	{
		return 112;
	}
}