package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+orObjectRoleExprChain+140
public class orObjectRoleExprChain implements iaccept, PartialSymbol
{
	public orObjectRoleExprChain(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<objectRoleExpr> objectRoleExprs;

	public orObjectRoleExprChain(Parser yyp, objectRoleExpr r, objectRoleExpr s)
	{
		super(yyp);
	objectRoleExprs = new ArrayList<objectRoleExpr>();
	objectRoleExprs.add(r);
	objectRoleExprs.add(s);
	}
	public orObjectRoleExprChain(Parser yyp, orObjectRoleExprChain z, objectRoleExpr r)
	{
		super(yyp);
	objectRoleExprs = z.objectRoleExprs;
	objectRoleExprs.add(r);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "orObjectRoleExprChain";
	}
	@Override
	public int getYynumEndl()
	{
		return 140;
	}
}