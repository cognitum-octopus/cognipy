package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+String+94
public class String extends Value
{
	public String(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public String(Parser yyp, String v)
	{
		super(yyp);
	val = v;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	@Override
	public String getVal()
	{
		return val.toString();
	}
	@Override
	public String getTypeTag()
	{
		return "S";
	}
	@Override
	public String toString()
	{
		return getVal().substring(1, 1 + getVal().length() - 2).replace("\'\'", "\'");
	}


	@Override
	public String getYynameDl()
	{
		return "String";
	}
	@Override
	public int getYynumDl()
	{
		return 94;
	}
}