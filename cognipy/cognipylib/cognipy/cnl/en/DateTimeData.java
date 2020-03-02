package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DateTimeData+180
public class DateTimeData extends dataval
{
	public DateTimeData(Parser yyp)
	{
		super(yyp);
	}
	public String val;
	public DateTimeData(Parser yyp, String v)
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
	public String getYynameEndl()
	{
		return "DateTimeData";
	}
	@Override
	public int getYynumEndl()
	{
		return 180;
	}
}