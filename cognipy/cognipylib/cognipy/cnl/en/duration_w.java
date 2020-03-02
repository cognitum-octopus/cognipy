package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+duration_w+252
public class duration_w extends duration
{
	public datavaler y;
	public datavaler W;
	public datavaler d;
	public datavaler h;
	public datavaler m;
	public datavaler s;

	public duration_w(Parser yyp)
	{
		super(yyp);
	}

	public duration_w(Parser yyp, datavaler y, datavaler W, datavaler d, datavaler h, datavaler m)
	{
		this(yyp, y, W, d, h, m, null);
	}

	public duration_w(Parser yyp, datavaler y, datavaler W, datavaler d, datavaler h)
	{
		this(yyp, y, W, d, h, null, null);
	}

	public duration_w(Parser yyp, datavaler y, datavaler W, datavaler d)
	{
		this(yyp, y, W, d, null, null, null);
	}

	public duration_w(Parser yyp, datavaler y, datavaler W)
	{
		this(yyp, y, W, null, null, null, null);
	}

	public duration_w(Parser yyp, datavaler y)
	{
		this(yyp, y, null, null, null, null, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public duration_w(Parser yyp, datavaler y, datavaler W = null, datavaler d = null, datavaler h = null, datavaler m = null, datavaler s = null)
	public duration_w(Parser yyp, datavaler y, datavaler W, datavaler d, datavaler h, datavaler m, datavaler s)
	{
		super(yyp);
		this.y = y != null ? y : zeroD;
		this.W = W != null ? W : zeroD;
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
		return "duration_w";
	}
	@Override
	public int getYynumEndl()
	{
		return 252;
	}
}