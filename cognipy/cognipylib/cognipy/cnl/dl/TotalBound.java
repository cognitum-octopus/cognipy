package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+TotalBound+103
public class TotalBound extends AbstractBound
{
	public TotalBound(Parser yyp)
	{
		super(yyp);
	}
	public Value V;
	public TotalBound(Parser yyp, Value v)
	{
		super(yyp);
	V = v;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "TotalBound";
	}
	@Override
	public int getYynumDl()
	{
		return 103;
	}
}