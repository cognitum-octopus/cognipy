package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+InstanceSet+116
public class InstanceSet extends Expression
{
	public InstanceSet(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Instance> Instances;
	public InstanceSet(Parser yyp, InstanceList il)
	{
		super(yyp);
	Instances = il.List;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "InstanceSet";
	}
	@Override
	public int getYynumDl()
	{
		return 116;
	}
}