package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_is+204
public class condition_is extends condition
{
	public objectr objectA, objectB;
	public condition_kind condition_kind = condition_kind.values()[0];

	public condition_is(Parser yyp)
	{
		super(yyp);
	}
	public condition_is(Parser yyp, objectr objectA, objectr objectB, condition_kind condition_kind)
	{
		super(yyp);
		this.condition_kind = condition_kind;
		this.objectA = objectA;
		this.objectB = objectB;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_is";
	}
	@Override
	public int getYynumEndl()
	{
		return 204;
	}
}