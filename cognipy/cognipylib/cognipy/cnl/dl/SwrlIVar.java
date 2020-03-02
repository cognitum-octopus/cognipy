package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlIVar+143
public class SwrlIVar extends SwrlIObject implements ISwrlVar, IExeVar
{
	public SwrlIVar(Parser yyp)
	{
		super(yyp);
	}
	public String VAR;
	public SwrlIVar(Parser yyp, ID VAR)
	{
		super(yyp);
	this.VAR = VAR.getYytext();
	}
	public SwrlIVar(Parser yyp, String VAR)
	{
		super(yyp);
	this.VAR = VAR;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}

	public final String getVar()
	{
		return VAR;
	}
	public final boolean isVar()
	{
		return true;
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlIVar";
	}
	@Override
	public int getYynumDl()
	{
		return 143;
	}
}