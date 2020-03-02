package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SelfReference+126
public class SelfReference extends Restriction
{
	public SelfReference(Parser yyp)
	{
		super(yyp);
	}
	public SelfReference(Parser yyp, Node r)
	{
		super(yyp);
	R = r.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SelfReference";
	}
	@Override
	public int getYynumDl()
	{
		return 126;
	}
}