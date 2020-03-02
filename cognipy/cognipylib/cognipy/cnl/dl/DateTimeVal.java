package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DateTimeVal+97
public class DateTimeVal extends Value
{
	public DateTimeVal(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public DateTimeVal(Parser yyp, String v)
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
		return "T";
	}


	@Override
	public String getYynameDl()
	{
		return "DateTimeVal";
	}
	@Override
	public int getYynumDl()
	{
		return 97;
	}
}