package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subjectBigName+122
public class subjectBigName extends subject
{
	public subjectBigName(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public subjectBigName(Parser yyp, String name_, boolean very_)
	{
		super(yyp);
	name = name_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "subjectBigName";
	}
	@Override
	public int getYynumEndl()
	{
		return 122;
	}
}