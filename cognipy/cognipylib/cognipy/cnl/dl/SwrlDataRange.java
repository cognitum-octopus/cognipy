package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlDataRange+141
public class SwrlDataRange extends SwrlItem
{
	public SwrlDataRange(Parser yyp)
	{
		super(yyp);
	}
	public AbstractBound B;
	public SwrlDObject DO;
	public SwrlDataRange(Parser yyp, AbstractBound B, SwrlDObject DO)
	{
		super(yyp);
	this.B = B.me();
	this.DO = DO;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlDataRange";
	}
	@Override
	public int getYynumDl()
	{
		return 141;
	}
}