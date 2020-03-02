package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+BoundNot+108
public class BoundNot extends AbstractBound
{
	@Override
	public int priority()
	{
		return 5;
	}
	public AbstractBound B;
	public BoundNot(Parser yyp)
	{
		super(yyp);
	}
	public BoundNot(Parser yyp, AbstractBound B)
	{
		super(yyp);
	this.B = B.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "BoundNot";
	}
	@Override
	public int getYynumDl()
	{
		return 108;
	}
}