package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+haskey+118
public class haskey extends sentence
{
	public haskey(Parser yyp)
	{
		super(yyp);
	}
	public objectRoleExpr s;
	public ArrayList<role> roles;
	public ArrayList<role> dataroles;

	public haskey(Parser yyp, objectRoleExpr s_, andanyrolechain x_)
	{
		super(yyp);
		s = s_;
		roles = x_.chain;
		dataroles = x_.datachain;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}



	@Override
	public String getYynameEndl()
	{
		return "haskey";
	}
	@Override
	public int getYynumEndl()
	{
		return 118;
	}
}