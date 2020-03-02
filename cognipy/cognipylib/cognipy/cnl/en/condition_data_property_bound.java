package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_data_property_bound+209
public class condition_data_property_bound extends condition
{
	public String property_name;
	public objectr objectA;
	public abstractbound bnd;

	public condition_data_property_bound(Parser yyp)
	{
		super(yyp);
	}
	public condition_data_property_bound(Parser yyp, objectr objectA, String property_name, abstractbound bnd)
	{
		super(yyp);
		this.objectA = objectA;
		this.property_name = property_name;
		this.bnd = bnd.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_data_property_bound";
	}
	@Override
	public int getYynumEndl()
	{
		return 209;
	}
}