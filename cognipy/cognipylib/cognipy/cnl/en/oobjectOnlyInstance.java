package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectOnlyInstance+147
public class oobjectOnlyInstance extends oobjectRelated
{
	public oobjectOnlyInstance(Parser yyp)
	{
		super(yyp);
	}
	public instance i;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectOnlyInstance(Parser yyp, instance i_)
	{
		super(yyp);
	i = i_;
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectOnlyInstance";
	}
	@Override
	public int getYynumEndl()
	{
		return 147;
	}
}