package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_definition+206
public class condition_definition extends condition
{
	public objectr objectA;
	public oobject objectClass;

	public condition_definition(Parser yyp)
	{
		super(yyp);
	}
	public condition_definition(Parser yyp, objectr objectA, oobject objectClass)
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
		return "condition_definition";
	}
	@Override
	public int getYynumEndl()
	{
		return 206;
	}
}