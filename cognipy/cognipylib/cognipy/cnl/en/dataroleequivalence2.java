package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+dataroleequivalence2+116
public class dataroleequivalence2 extends sentence
{
	public dataroleequivalence2(Parser yyp)
	{
		super(yyp);
	}
	public role r;
	public role s;
	public dataroleequivalence2(Parser yyp, role r_, role s_)
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
		return "dataroleequivalence2";
	}
	@Override
	public int getYynumEndl()
	{
		return 116;
	}
}