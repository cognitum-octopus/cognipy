package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+singleOneOf+168
public class singleOneOf extends single
{
	public singleOneOf(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<instance> insts;
	public singleOneOf(Parser yyp, instanceList il)
	{
		super(yyp);
	insts = il.insts;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "singleOneOf";
	}
	@Override
	public int getYynumEndl()
	{
		return 168;
	}
}