package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Bottom+114
public class Bottom extends Node
{
	public Bottom(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "Bottom";
	}
	@Override
	public int getYynumDl()
	{
		return 114;
	}
}