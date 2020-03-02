package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+facet+182
public class facet implements iaccept, PartialSymbol
{
	public String Cmp;
	public dataval V;

	public facet(Parser yyp)
	{
		super(yyp);
	}
	public facet(Parser yyp, String Cmp_, dataval V_)
	{
		super(yyp);
	Cmp = Cmp_;
	V = V_;
	}
	public facet(Parser yyp, String Cmp_, String V_)
	{
		super(yyp);
		Cmp = Cmp_;
		if (Cmp.equals("#"))
		{
			V = new CNL.EN.StrData(null, V_);
		}
		else if (Cmp.startsWith("<->"))
		{
			V = new CNL.EN.Number(null, V_);
		}
		else
		{
			throw new IllegalStateException();
		}
	}

	public boolean isStrict()
	{
		return Cmp.equals("=");
	}
	public dataval getStrictVal()
	{
		return V;
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "facet";
	}
	@Override
	public int getYynumEndl()
	{
		return 182;
	}
}