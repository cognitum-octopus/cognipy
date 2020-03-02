package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DTBound+104
public class DTBound extends AbstractBound
{
	public DTBound(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public DTBound(Parser yyp, ID ID)
	{
		super(yyp);
	name = ID.getYytext();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "DTBound";
	}
	@Override
	public int getYynumDl()
	{
		return 104;
	}
}