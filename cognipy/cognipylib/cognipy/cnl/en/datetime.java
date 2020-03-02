package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class datetime implements iaccept, PartialSymbol
{
	protected static datavaler zeroD = new datavalval(null, new Number(null, "0"));

	public datavaler y;
	public datavaler M;
	public datavaler d;
	public datavaler h;
	public datavaler m;
	public datavaler s;

	public datetime(Parser yyp)
	{
		super(yyp);
	}

	public datetime(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h, datavaler m)
	{
		this(yyp, y, M, d, h, m, null);
	}

	public datetime(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h)
	{
		this(yyp, y, M, d, h, null, null);
	}

	public datetime(Parser yyp, datavaler y, datavaler M, datavaler d)
	{
		this(yyp, y, M, d, null, null, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public datetime(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h = null, datavaler m = null, datavaler s = null)
	public datetime(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h, datavaler m, datavaler s)
	{
		super(yyp);
		this.y = y != null ? y : zeroD;
		this.M = M != null ? M : zeroD;
		this.d = d != null ? d : zeroD;
		this.h = h != null ? h : zeroD;
		this.m = m != null ? m : zeroD;
		this.s = s != null ? s : zeroD;
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "datetime";
	}
	@Override
	public int getYynumEndl()
	{
		return 388;
	}
}