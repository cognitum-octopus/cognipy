package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+builtin_list+236
public class builtin_list extends builtin
{
	public datavaler result;
	public ArrayList<datavaler> vals;
	public String tpy;

	public builtin_list(Parser yyp)
	{
		super(yyp);
	}
	public builtin_list(Parser yyp, ArrayList<datavaler> dl, String tpy, datavaler result)
	{
		super(yyp);
		this.result = result;
		this.vals = dl;
		this.tpy = tpy;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "builtin_list";
	}
	@Override
	public int getYynumEndl()
	{
		return 236;
	}
}