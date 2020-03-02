package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+datavalerTimesList+248
public class datavalerTimesList implements iaccept, PartialSymbol
{
	public datavalerTimesList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<datavaler> vals;

	public datavalerTimesList(Parser yyp, datavaler i, datavaler j)
	{
		super(yyp);
	vals = new ArrayList<datavaler>();
	vals.add(i);
	vals.add(j);
	}
	public datavalerTimesList(Parser yyp, datavalerTimesList il, datavaler i)
	{
		super(yyp);
	vals = il.vals;
	vals.add(i);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "datavalerTimesList";
	}
	@Override
	public int getYynumEndl()
	{
		return 248;
	}
}