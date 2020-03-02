package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+datavalvar+228
public class datavalvar extends datavaler
{
	public String num;
	public datavalvar(Parser yyp)
	{
		super(yyp);
	}
	public datavalvar(Parser yyp, String num_)
	{
		super(yyp);
	num = num_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "datavalvar";
	}
	@Override
	public int getYynumEndl()
	{
		return 228;
	}
}