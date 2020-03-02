package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subjectEverything+121
public class subjectEverything extends subject
{
	public subjectEverything(Parser yyp)
	{
		super(yyp);
	}
	public that t = null;
	public subjectEverything(Parser yyp, that t_)
	{
		super(yyp);
	t = t_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "subjectEverything";
	}
	@Override
	public int getYynumEndl()
	{
		return 121;
	}
}