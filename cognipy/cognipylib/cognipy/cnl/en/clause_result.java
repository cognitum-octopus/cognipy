package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+clause_result+212
public class clause_result implements iaccept, PartialSymbol
{
	public ArrayList<condition_result> Conditions;

	public clause_result(Parser yyp)
	{
		super(yyp);
	}
	public clause_result(Parser yyp, condition_result condition_result)
	{
		super(yyp);
		Conditions = new ArrayList<condition_result>();
		Conditions.add(condition_result);
	}
	public clause_result(Parser yyp, clause_result clause_result, condition_result condition_result)
	{
		super(yyp);
		Conditions = clause_result.Conditions;
		Conditions.add(condition_result);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "clause_result";
	}
	@Override
	public int getYynumEndl()
	{
		return 212;
	}
}