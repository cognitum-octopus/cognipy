package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlDVar+147
public class SwrlDVar extends SwrlDObject implements ISwrlVar, IExeVar
{
	public SwrlDVar(Parser yyp)
	{
		super(yyp);
	}
	public String VAR;
	public SwrlDVar(Parser yyp, ID VAR)
	{
		super(yyp);
	this.VAR = VAR.getYytext();
	}
	public SwrlDVar(Parser yyp, String VAR)
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
		return "SwrlDVar";
	}
	@Override
	public int getYynumDl()
	{
		return 147;
	}
}