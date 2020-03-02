package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectSelf+154
public class oobjectSelf extends oobjectRelated
{
	public oobjectSelf(Parser yyp)
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
		return "oobjectSelf";
	}
	@Override
	public int getYynumEndl()
	{
		return 154;
	}
}