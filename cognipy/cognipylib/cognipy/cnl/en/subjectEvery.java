package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subjectEvery+120
public class subjectEvery extends subject
{
	public subjectEvery(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	public subjectEvery(Parser yyp, single s_)
	{
		super(yyp);
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
		return "subjectEvery";
	}
	@Override
	public int getYynumEndl()
	{
		return 120;
	}
}