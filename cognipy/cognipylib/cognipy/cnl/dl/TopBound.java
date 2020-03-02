package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+TopBound+105
public class TopBound extends AbstractBound
{
	public TopBound(Parser yyp)
	{
		super(yyp);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "TopBound";
	}
	@Override
	public int getYynumDl()
	{
		return 105;
	}
}