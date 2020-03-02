package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+thatOrLoop+170
public class thatOrLoop extends that
{
	public thatOrLoop(Parser yyp)
	{
		super(yyp);
	}
	public orloop o;
	public thatOrLoop(Parser yyp, orloop o_)
	{
		super(yyp);
	o = o_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "thatOrLoop";
	}
	@Override
	public int getYynumEndl()
	{
		return 170;
	}
}