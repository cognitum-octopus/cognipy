package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+ValueSet+106
public class ValueSet extends AbstractBound
{
	public ValueSet(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Value> Values;
	public ValueSet(Parser yyp, ValueList vl)
	{
		super(yyp);
	Values = vl.List;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "ValueSet";
	}
	@Override
	public int getYynumDl()
	{
		return 106;
	}
}