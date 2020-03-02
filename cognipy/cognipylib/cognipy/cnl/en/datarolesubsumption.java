package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+datarolesubsumption+115
public class datarolesubsumption extends sentence
{
	public datarolesubsumption(Parser yyp)
	{
		super(yyp);
	}
	public role subRole;
	public role superRole;

	public datarolesubsumption(Parser yyp, role z_, role s_)
	{
		super(yyp);
	subRole = z_;
	superRole = s_;
	}

	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "datarolesubsumption";
	}
	@Override
	public int getYynumEndl()
	{
		return 115;
	}
}