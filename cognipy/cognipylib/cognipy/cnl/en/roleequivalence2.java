package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+roleequivalence2+113
public class roleequivalence2 extends sentence
{
	public roleequivalence2(Parser yyp)
	{
		super(yyp);
	}
	public role r;
	public roleWithXY s;
	public roleequivalence2(Parser yyp, role r_, roleWithXY s_)
	{
		super(yyp);
	r = r_;
	s = s_;
	}

	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "roleequivalence2";
	}
	@Override
	public int getYynumEndl()
	{
		return 113;
	}
}