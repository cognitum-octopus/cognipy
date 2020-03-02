package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundVal+186
public class boundVal extends abstractbound
{
	public String Cmp;
	public dataval V;

	public boundVal(Parser yyp)
	{
		super(yyp);
	}
	public boundVal(Parser yyp, String Cmp_, dataval V_)
	{
		super(yyp);
	Cmp = Cmp_;
	V = V_;
	}
	@Override
	public boolean isStrict()
	{
		return Cmp.equals("=");
	}
	@Override
	public dataval getStrictVal()
	{
		return V;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}



	@Override
	public String getYynameEndl()
	{
		return "boundVal";
	}
	@Override
	public int getYynumEndl()
	{
		return 186;
	}
}