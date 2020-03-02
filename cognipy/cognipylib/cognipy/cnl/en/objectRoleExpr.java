package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+objectRoleExpr+127
public class objectRoleExpr implements iaccept, PartialSymbol
{
	public objectRoleExpr(Parser yyp)
	{
		super(yyp);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "objectRoleExpr";
	}
	@Override
	public int getYynumEndl()
	{
		return 127;
	}
}