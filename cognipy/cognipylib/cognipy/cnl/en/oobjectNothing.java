package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectNothing+156
public class oobjectNothing extends oobject
{
	public oobjectNothing(Parser yyp)
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
		return "oobjectNothing";
	}
	@Override
	public int getYynumEndl()
	{
		return 156;
	}
}