package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+clause+202
public class clause implements iaccept, PartialSymbol
{
	public ArrayList<condition> Conditions;

	public clause(Parser yyp)
	{
		super(yyp);
	}
	public clause(Parser yyp, condition condition)
	{
		super(yyp);
		Conditions = new ArrayList<condition>();
		Conditions.add(condition);
	}
	public clause(Parser yyp, clause clause, condition condition)
	{
		super(yyp);
		Conditions = clause.Conditions;
		Conditions.add(condition);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "clause";
	}
	@Override
	public int getYynumEndl()
	{
		return 202;
	}
}