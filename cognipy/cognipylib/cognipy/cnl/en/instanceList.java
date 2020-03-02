package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+instanceList+138
public class instanceList implements iaccept, PartialSymbol
{
	public instanceList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<instance> insts;

	public instanceList(Parser yyp, instance i, instance j)
	{
		super(yyp);
	insts = new ArrayList<instance>();
	insts.add(i);
	insts.add(j);
	}
	public instanceList(Parser yyp, instanceList il, instance i)
	{
		super(yyp);
	insts = il.insts;
	insts.add(i);
	}
	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameEndl()
	{
		return "instanceList";
	}
	@Override
	public int getYynumEndl()
	{
		return 138;
	}
}