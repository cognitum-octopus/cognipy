package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+notidentobject+222
public class notidentobject implements iaccept, PartialSymbol
{
	public String name;
	public String num;
	public notidentobject(Parser yyp)
	{
		super(yyp);
	}

	public notidentobject(Parser yyp, String name_)
	{
		this(yyp, name_, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public notidentobject(Parser yyp, string name_, string num_ = null)
	public notidentobject(Parser yyp, String name_, String num_)
	{
		super(yyp);
	name = name_;
	num = num_;
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "notidentobject";
	}
	@Override
	public int getYynumEndl()
	{
		return 222;
	}
}