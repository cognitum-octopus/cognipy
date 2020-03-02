package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+datavalList+187
public class datavalList implements iaccept, PartialSymbol
{
	public datavalList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<dataval> vals;

	public datavalList(Parser yyp, dataval i, dataval j)
	{
		super(yyp);
	vals = new ArrayList<dataval>();
	vals.add(i);
	vals.add(j);
	}
	public datavalList(Parser yyp, datavalList il, dataval i)
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
		return "datavalList";
	}
	@Override
	public int getYynumEndl()
	{
		return 187;
	}
}