package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+annotation+104
public class annotation extends sentence
{
	public annotation(Parser yyp)
	{
		super(yyp);
	}
	public String txt;

	public annotation(Parser yyp, String txt_)
	{
		super(yyp);
	txt = txt_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "annotation";
	}
	@Override
	public int getYynumEndl()
	{
		return 104;
	}
}