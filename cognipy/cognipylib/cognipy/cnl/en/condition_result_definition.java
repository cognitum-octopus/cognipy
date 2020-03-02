package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_result_definition+215
public class condition_result_definition extends condition_result
{
	public identobject objectA;
	public oobject objectClass;
	public condition_result_definition(Parser yyp)
	{
		super(yyp);
	}
	public condition_result_definition(Parser yyp, identobject objectA, oobject objectClass)
	{
		super(yyp);
		this.objectA = objectA;
		this.objectClass = objectClass;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_result_definition";
	}
	@Override
	public int getYynumEndl()
	{
		return 215;
	}
}