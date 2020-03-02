package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_role+207
public class condition_role extends condition
{
	public String role;
	public objectr objectA, objectB;
	public condition_kind condition_kind = condition_kind.values()[0];

	public condition_role(Parser yyp)
	{
		super(yyp);
	}
	public condition_role(Parser yyp, objectr objectA, String role, objectr objectB, condition_kind conditionKind)
	{
		super(yyp);
		this.condition_kind = conditionKind;
		this.role = role;
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
		return "condition_role";
	}
	@Override
	public int getYynumEndl()
	{
		return 207;
	}
}