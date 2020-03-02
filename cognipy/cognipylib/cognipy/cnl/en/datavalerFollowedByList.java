package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+datavalerFollowedByList+249
public class datavalerFollowedByList implements iaccept, PartialSymbol
{
	public datavalerFollowedByList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<datavaler> vals;

	public datavalerFollowedByList(Parser yyp, datavaler i, datavaler j)
	{
		super(yyp);
	vals = new ArrayList<datavaler>();
	vals.add(i);
	vals.add(j);
	}
	public datavalerFollowedByList(Parser yyp, datavalerFollowedByList il, datavaler i)
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
		return "datavalerFollowedByList";
	}
	@Override
	public int getYynumEndl()
	{
		return 249;
	}
}