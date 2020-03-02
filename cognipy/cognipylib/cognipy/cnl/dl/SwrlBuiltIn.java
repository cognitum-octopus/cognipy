package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+SwrlBuiltIn+140
public class SwrlBuiltIn extends SwrlItem
{
	public SwrlBuiltIn(Parser yyp)
	{
		super(yyp);
	}
	public SwrlBuiltIn(Parser yyp, String builtInName, SwrlObjectList DOL)
	{
		super(yyp);
	this.builtInName = builtInName;
	this.Values = DOL.Values;
	}
	public SwrlBuiltIn(Parser yyp, String builtInName, ArrayList<ISwrlObject> Values)
	{
		super(yyp);
	this.builtInName = builtInName;
	this.Values = Values;
	}
	public ArrayList<ISwrlObject> Values;
	public String builtInName = null;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlBuiltIn";
	}
	@Override
	public int getYynumDl()
	{
		return 140;
	}
}