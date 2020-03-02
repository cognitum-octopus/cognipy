package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_builtin+211
public class condition_builtin extends condition
{
	public builtin bi;
	public condition_builtin(Parser yyp)
	{
		super(yyp);
	}
	public condition_builtin(Parser yyp, builtin bi)
	{
		super(yyp);
		this.bi = bi;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_builtin";
	}
	@Override
	public int getYynumEndl()
	{
		return 211;
	}
}