package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+exclusiveunion+111
public class exclusiveunion extends sentence
{
	public String name;
	public ArrayList<objectRoleExpr> objectRoleExprs;
	public exclusiveunion(Parser yyp)
	{
		super(yyp);
	}
	public exclusiveunion(Parser yyp, String name_, orObjectRoleExprChain z_, String modality_)
	{
		super(yyp);
		modality = modality_;
		name = name_;
		objectRoleExprs = z_.objectRoleExprs;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "exclusiveunion";
	}
	@Override
	public int getYynumEndl()
	{
		return 111;
	}
}