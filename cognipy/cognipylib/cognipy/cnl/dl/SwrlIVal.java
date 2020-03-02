package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlIVal+144
public class SwrlIVal extends SwrlIObject implements IExeVar
{
	public SwrlIVal(Parser yyp)
	{
		super(yyp);
	}
	public String I;
	public SwrlIVal(Parser yyp, ID I)
	{
		super(yyp);
	this.I = I.getYytext();
	}
	public SwrlIVal(Parser yyp, String I)
	{
		super(yyp);
	this.I = I;
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
		return "SwrlIVal";
	}
	@Override
	public int getYynumDl()
	{
		return 144;
	}
}