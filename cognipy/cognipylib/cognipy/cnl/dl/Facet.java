package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class Facet extends PartialSymbol implements IAccept
{
	public String Kind;
	public Value V;
	public Facet(Parser yyp)
	{
		super(yyp);
	}
	public Facet(Parser yyp, String k, Value v)
	{
		super(yyp);
	Kind = k;
	V = v;
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "Facet";
	}
	@Override
	public int getYynumDl()
	{
		return 270;
	}
}