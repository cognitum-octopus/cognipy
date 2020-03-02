package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subjectThe+123
public class subjectThe extends subject
{
	public subjectThe(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	public boolean only;
	public subjectThe(Parser yyp, boolean only_, single s_)
	{
		super(yyp);
	s = s_;
	only = only_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "subjectThe";
	}
	@Override
	public int getYynumEndl()
	{
		return 123;
	}
}