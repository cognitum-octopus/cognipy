package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subjectNothing+126
public class subjectNothing extends nosubject
{
	public subjectNothing(Parser yyp)
	{
		super(yyp);
	}
	public that t = null;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "subjectNothing";
	}
	@Override
	public int getYynumEndl()
	{
		return 126;
	}
}