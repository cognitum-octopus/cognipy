package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+identobject_name+224
public class identobject_name extends identobject
{
	public String name;
	public String num;
	public identobject_name(Parser yyp)
	{
		super(yyp);
	}

	public identobject_name(Parser yyp, String name_)
	{
		this(yyp, name_, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public identobject_name(Parser yyp, string name_, string num_ = null)
	public identobject_name(Parser yyp, String name_, String num_)
	{
		super(yyp);
	name = name_;
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
		return "identobject_name";
	}
	@Override
	public int getYynumEndl()
	{
		return 224;
	}
}