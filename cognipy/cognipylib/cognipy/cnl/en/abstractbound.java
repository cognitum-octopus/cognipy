package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+abstractbound+184
public class abstractbound implements iaccept, PartialSymbol
{
	public abstractbound(Parser yyp)
	{
		super(yyp);
	}
	public boolean isStrict()
	{
		return false;
	}
	public dataval getStrictVal()
	{
		throw new IllegalStateException();
	}
	public Object accept(IVisitor v)
	{
		return null;
	}
	public int priority()
	{
		return 0;
	}
	public final abstractbound me()
	{
		return this instanceof boundIdent ? (this instanceof boundIdent ? (boundIdent)this : null).bnd :this;
	}


	@Override
	public String getYynameEndl()
	{
		return "abstractbound";
	}
	@Override
	public int getYynumEndl()
	{
		return 184;
	}
}