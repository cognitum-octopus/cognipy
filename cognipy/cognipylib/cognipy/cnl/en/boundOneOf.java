package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+boundOneOf+188
public class boundOneOf extends abstractbound
{
	public boundOneOf(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<dataval> vals;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public boundOneOf(Parser yyp, datavalList dl)
	{
		super(yyp);
	vals = dl.vals;
	}


	@Override
	public String getYynameEndl()
	{
		return "boundOneOf";
	}
	@Override
	public int getYynumEndl()
	{
		return 188;
	}
}