package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+NamedInstance+82
public class NamedInstance extends Instance
{
	public NamedInstance(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public NamedInstance(Parser yyp, ID i)
	{
		super(yyp);
	name = i.getYytext();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public String toString()
	{
		return name;
	}


	@Override
	public String getYynameDl()
	{
		return "NamedInstance";
	}
	@Override
	public int getYynumDl()
	{
		return 82;
	}
}