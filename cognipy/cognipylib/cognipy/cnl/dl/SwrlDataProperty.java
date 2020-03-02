package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlDataProperty+139
public class SwrlDataProperty extends SwrlItem
{
	public SwrlDataProperty(Parser yyp)
	{
		super(yyp);
	}
	public String R;
	public SwrlIObject IO;
	public SwrlDObject DO;
	public SwrlDataProperty(Parser yyp, ID R, SwrlIObject IO, SwrlDObject DO)
	{
		super(yyp);
	this.R = R.getYytext();
	this.IO = IO;
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
		return "SwrlDataProperty";
	}
	@Override
	public int getYynumDl()
	{
		return 139;
	}
}