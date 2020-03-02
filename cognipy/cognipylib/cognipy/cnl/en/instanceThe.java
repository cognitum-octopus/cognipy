package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+instanceThe+136
public class instanceThe extends instance
{
	public instanceThe(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	public boolean only;
	public instanceThe(Parser yyp, boolean only_, single s_)
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
		return "instanceThe";
	}
	@Override
	public int getYynumEndl()
	{
		return 136;
	}
}