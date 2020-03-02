package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+andloop+171
public class andloop implements iaccept, PartialSymbol
{
	public andloop(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<objectRoleExpr> exprs;

	public andloop(Parser yyp, objectRoleExpr o)
	{
		super(yyp);
	exprs = new ArrayList<objectRoleExpr>();
	exprs.add(o);
	}
	public andloop(Parser yyp, andloop l, objectRoleExpr o)
	{
		super(yyp);
	exprs = l.exprs;
	exprs.add(o);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "andloop";
	}
	@Override
	public int getYynumEndl()
	{
		return 171;
	}
}