package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+condition_result_data_property+217
public class condition_result_data_property extends condition_result
{
	public String property_name;
	public identobject objectA;
	public datavaler d_object;

	public condition_result_data_property(Parser yyp)
	{
		super(yyp);
	}
	public condition_result_data_property(Parser yyp, identobject objectA, String property_name, datavaler d_object)
	{
		super(yyp);
		this.objectA = objectA;
		this.property_name = property_name;
		this.d_object = d_object;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "condition_result_data_property";
	}
	@Override
	public int getYynumEndl()
	{
		return 217;
	}
}