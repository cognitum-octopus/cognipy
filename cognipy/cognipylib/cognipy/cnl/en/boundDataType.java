package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundDataType+191
public class boundDataType extends abstractbound
{
	public boundDataType(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public boundDataType(Parser yyp, String name)
	{
		super(yyp);
	this.name = name;
	}


	@Override
	public String getYynameEndl()
	{
		return "boundDataType";
	}
	@Override
	public int getYynumEndl()
	{
		return 191;
	}
}