package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

////////////////// SWRL //////////////////////////////////////

// EXERULE
//%+exerule+230
public class exerule extends sentence
{
	public clause slp;
	public exeargs args;
	public String exe;

	public exerule(Parser yyp)
	{
		super(yyp);
	}
	public exerule(Parser yyp, clause slp_, exeargs args_, String exe_)
	{
		super(yyp);
		slp = slp_;
		args = args_;
		exe = exe_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "exerule";
	}
	@Override
	public int getYynumEndl()
	{
		return 230;
	}
}