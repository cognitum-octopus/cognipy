package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectCmpBnd+153
public class oobjectCmpBnd extends oobjectCardinal
{
	public oobjectCmpBnd(Parser yyp)
	{
		super(yyp);
	}
	public abstractbound b;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectCmpBnd(Parser yyp, String Cmp_, String Cnt_, abstractbound b_)
	{
		super(yyp);
	Cnt = Cnt_;
	Cmp = Cmp_;
	b = b_.me();
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectCmpBnd";
	}
	@Override
	public int getYynumEndl()
	{
		return 153;
	}
}