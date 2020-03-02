package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+swrlrulefor+233
public class swrlrulefor extends sentence
{
	public clause Predicate;
	public clause_result Result;
	public exeargs Collection;

	public swrlrulefor(Parser yyp)
	{
		super(yyp);
	}
	public swrlrulefor(Parser yyp, clause predicate, String vot, String n, datavaler col, clause_result result)
	{
		super(yyp);
		this.Predicate = predicate;
		this.Result = result;

		iexevar ev;
		if (vot.toLowerCase().equals("value"))
		{
			ev = new datavalvar(null, n);
		}
		else
		{
			ev = new identobject_name(null, null, n);
		}

		this.Collection = new exeargs(null);
		this.Collection.exevars = new ArrayList<iexevar>(Arrays.asList(ev, col));
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "swrlrulefor";
	}
	@Override
	public int getYynumEndl()
	{
		return 233;
	}
}