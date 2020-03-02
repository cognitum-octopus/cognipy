package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+orloop+172
public class orloop implements iaccept, PartialSymbol
{
	public orloop(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<andloop> exprs;

	public orloop(Parser yyp, andloop a)
	{
		super(yyp);
	exprs = new ArrayList<andloop>();
	exprs.add(a);
	}
	public orloop(Parser yyp, orloop l, andloop a)
	{
		super(yyp);
	exprs = l.exprs;
	exprs.add(a);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "orloop";
	}
	@Override
	public int getYynumEndl()
	{
		return 172;
	}
}