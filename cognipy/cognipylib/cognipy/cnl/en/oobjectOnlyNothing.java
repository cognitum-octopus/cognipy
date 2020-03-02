package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectOnlyNothing+157
public class oobjectOnlyNothing extends oobjectRelated
{
	public oobjectOnlyNothing(Parser yyp)
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
		return "oobjectOnlyNothing";
	}
	@Override
	public int getYynumEndl()
	{
		return 157;
	}
}