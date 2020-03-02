package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_data_property+208
public class condition_data_property extends condition
{
	public String property_name;
	public objectr objectA;
	public datavaler d_object;

	public condition_data_property(Parser yyp)
	{
		super(yyp);
	}
	public condition_data_property(Parser yyp, objectr objectA, String property_name, String dvar)
	{
		super(yyp);
		this.objectA = objectA;
		this.property_name = property_name;
		this.d_object = new datavalvar(yyp, dvar);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_data_property";
	}
	@Override
	public int getYynumEndl()
	{
		return 208;
	}
}