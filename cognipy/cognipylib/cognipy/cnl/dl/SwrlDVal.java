package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlDVal+148
public class SwrlDVal extends SwrlDObject implements IExeVar
{
	public SwrlDVal(Parser yyp)
	{
		super(yyp);
	}
	public Value Val;
	public SwrlDVal(Parser yyp, Value Val)
	{
		super(yyp);
	this.Val = Val;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}

	public final boolean isVar()
	{
		return false;
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlDVal";
	}
	@Override
	public int getYynumDl()
	{
		return 148;
	}
}