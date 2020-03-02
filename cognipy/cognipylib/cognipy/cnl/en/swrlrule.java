package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

////////////////// SWRL //////////////////////////////////////

//%+swrlrule+201
public class swrlrule extends sentence
{
	public clause Predicate;
	public clause_result Result;

	public swrlrule(Parser yyp)
	{
		super(yyp);
	}
	public swrlrule(Parser yyp, clause predicate, clause_result result, String modality)
	{
		super(yyp);
		this.Predicate = predicate;
		this.Result = result;
		this.modality = modality;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "swrlrule";
	}
	@Override
	public int getYynumEndl()
	{
		return 201;
	}
}