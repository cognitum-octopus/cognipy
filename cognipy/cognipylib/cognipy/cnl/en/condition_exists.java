package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_exists+205
public class condition_exists extends condition
{
	public objectr objectA;

	public condition_exists(Parser yyp)
	{
		super(yyp);
	}
	public condition_exists(Parser yyp, objectr objectA)
	{
		super(yyp);
		this.objectA = objectA;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_exists";
	}
	@Override
	public int getYynumEndl()
	{
		return 205;
	}
}