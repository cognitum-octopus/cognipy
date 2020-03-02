package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_result_builtin+218
public class condition_result_builtin extends condition_result
{
	public builtin bi;
	public condition_result_builtin(Parser yyp)
	{
		super(yyp);
	}
	public condition_result_builtin(Parser yyp, builtin bi)
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
		return "condition_result_builtin";
	}
	@Override
	public int getYynumEndl()
	{
		return 218;
	}
}