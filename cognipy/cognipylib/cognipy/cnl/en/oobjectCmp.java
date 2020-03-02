package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectCmp+149
public class oobjectCmp extends oobjectCardinal
{
	public oobjectCmp(Parser yyp)
	{
		super(yyp);
	}
	public single s;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectCmp(Parser yyp, String Cmp_, String Cnt_, single s_)
	{
		super(yyp);
	s = s_;
	Cmp = Cmp_;
	Cnt = Cnt_;
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectCmp";
	}
	@Override
	public int getYynumEndl()
	{
		return 149;
	}
}