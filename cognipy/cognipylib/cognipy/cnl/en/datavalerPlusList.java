package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+datavalerPlusList+247
public class datavalerPlusList implements iaccept, PartialSymbol
{
	public datavalerPlusList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<datavaler> vals;

	public datavalerPlusList(Parser yyp, datavaler i, datavaler j)
	{
		super(yyp);
	vals = new ArrayList<datavaler>();
	vals.add(i);
	vals.add(j);
	}
	public datavalerPlusList(Parser yyp, datavalerPlusList il, datavaler i)
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
		return "datavalerPlusList";
	}
	@Override
	public int getYynumEndl()
	{
		return 247;
	}
}