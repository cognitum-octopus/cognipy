package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+ValueList+99
public class ValueList extends PartialSymbol
{
	public ValueList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Value> List;
	public ValueList(Parser yyp, Value V)
	{
		super(yyp);
	List = new ArrayList<Value>();
	List.add(V);
	}
	public ValueList(Parser yyp, ValueList cl, Value V)
	{
		super(yyp);
	List = cl.List;
	List.add(V);
	}


	@Override
	public String getYynameDl()
	{
		return "ValueList";
	}
	@Override
	public int getYynumDl()
	{
		return 99;
	}
}