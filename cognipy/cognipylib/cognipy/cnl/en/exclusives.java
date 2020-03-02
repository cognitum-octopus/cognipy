package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+exclusives+110
public class exclusives extends sentence
{
	public ArrayList<objectRoleExpr> objectRoleExprs;
	public exclusives(Parser yyp)
	{
		super(yyp);
	}
	public exclusives(Parser yyp, orObjectRoleExprChain z_, String modality_)
	{
		super(yyp);
		modality = modality_;
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
		return "exclusives";
	}
	@Override
	public int getYynumEndl()
	{
		return 110;
	}
}