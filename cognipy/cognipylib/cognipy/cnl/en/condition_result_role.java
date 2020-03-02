package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_result_role+216
public class condition_result_role extends condition_result
{
	public String role;
	public identobject objectA, objectB;
	public condition_kind condition_kind = condition_kind.values()[0];

	public condition_result_role(Parser yyp)
	{
		super(yyp);
	}
	public condition_result_role(Parser yyp, identobject objectA, String role, identobject objectB, condition_kind condition_kind)
	{
		super(yyp);
		this.condition_kind = condition_kind;
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
		return "condition_result_role";
	}
	@Override
	public int getYynumEndl()
	{
		return 216;
	}
}