package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_data_bound+210
public class condition_data_bound extends condition
{
	public String property_name;
	public datavaler d_object;
	public abstractbound bound;

	public condition_data_bound(Parser yyp)
	{
		super(yyp);
	}
	public condition_data_bound(Parser yyp, datavaler d_object, abstractbound bound)
	{
		super(yyp);
		this.d_object = d_object;
		this.bound = bound.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_data_bound";
	}
	@Override
	public int getYynumEndl()
	{
		return 210;
	}
}