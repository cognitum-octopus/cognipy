package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_result_is+214
public class condition_result_is extends condition_result
{
	public identobject objectA, objectB;
	public condition_kind condition_kind = condition_kind.values()[0];

	public condition_result_is(Parser yyp)
	{
		super(yyp);
	}
	public condition_result_is(Parser yyp, identobject objectA, identobject objectB, condition_kind condition_kind)
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
		return "condition_result_is";
	}
	@Override
	public int getYynumEndl()
	{
		return 214;
	}
}