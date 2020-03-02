package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+duration_m+251
public class duration_m extends duration
{
	public datavaler y;
	public datavaler M;
	public datavaler d;
	public datavaler h;
	public datavaler m;
	public datavaler s;

	public duration_m(Parser yyp)
	{
		super(yyp);
	}

	public duration_m(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h, datavaler m)
	{
		this(yyp, y, M, d, h, m, null);
	}

	public duration_m(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h)
	{
		this(yyp, y, M, d, h, null, null);
	}

	public duration_m(Parser yyp, datavaler y, datavaler M, datavaler d)
	{
		this(yyp, y, M, d, null, null, null);
	}

	public duration_m(Parser yyp, datavaler y, datavaler M)
	{
		this(yyp, y, M, null, null, null, null);
	}

	public duration_m(Parser yyp, datavaler y)
	{
		this(yyp, y, null, null, null, null, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public duration_m(Parser yyp, datavaler y, datavaler M = null, datavaler d = null, datavaler h = null, datavaler m = null, datavaler s = null)
	public duration_m(Parser yyp, datavaler y, datavaler M, datavaler d, datavaler h, datavaler m, datavaler s)
	{
		super(yyp);
		this.y = y != null ? y : zeroD;
		this.M = M != null ? M : zeroD;
		this.d = d != null ? d : zeroD;
		this.h = h != null ? h : zeroD;
		this.m = m != null ? m : zeroD;
		this.s = s != null ? s : zeroD;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "duration_m";
	}
	@Override
	public int getYynumEndl()
	{
		return 251;
	}
}