package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectSomething+155
public class oobjectSomething extends oobject
{
	public oobjectSomething(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectSomething";
	}
	@Override
	public int getYynumEndl()
	{
		return 155;
	}
}