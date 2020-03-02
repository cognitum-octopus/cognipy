package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+BoundVal+102
public class BoundVal extends AbstractBound
{
	public String Kind;
	public Value V;
	public BoundVal(Parser yyp)
	{
		super(yyp);
	}
	public BoundVal(Parser yyp, String k, Value v)
	{
		super(yyp);
	Kind = k;
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
		return "BoundVal";
	}
	@Override
	public int getYynumDl()
	{
		return 102;
	}
}