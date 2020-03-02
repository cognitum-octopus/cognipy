package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subjectNo+125
public class subjectNo extends nosubject
{
	public subjectNo(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	public subjectNo(Parser yyp, single s_)
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
		return "subjectNo";
	}
	@Override
	public int getYynumEndl()
	{
		return 125;
	}
}