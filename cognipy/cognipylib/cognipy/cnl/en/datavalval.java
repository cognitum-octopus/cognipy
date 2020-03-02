package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+datavalval+229
public class datavalval extends datavaler
{
	public dataval dv;
	public datavalval(Parser yyp)
	{
		super(yyp);
	}
	public datavalval(Parser yyp, dataval dv)
	{
		super(yyp);
	this.dv = dv;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "datavalval";
	}
	@Override
	public int getYynumEndl()
	{
		return 229;
	}
}