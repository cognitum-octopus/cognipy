package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+dataroledisjoint2+117
public class dataroledisjoint2 extends sentence
{
	public dataroledisjoint2(Parser yyp)
	{
		super(yyp);
	}
	public role r;
	public role s;
	public dataroledisjoint2(Parser yyp, role r_, role s_)
	{
		super(yyp);
	r = r_;
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
		return "dataroledisjoint2";
	}
	@Override
	public int getYynumEndl()
	{
		return 117;
	}
}