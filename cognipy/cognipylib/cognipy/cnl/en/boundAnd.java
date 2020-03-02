package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+boundAnd+193
public class boundAnd extends abstractbound
{
	public ArrayList<abstractbound> List;
	@Override
	public int priority()
	{
		return 3;
	}
	public boundAnd(Parser yyp)
	{
		super(yyp);
	}
	public boundAnd(Parser yyp, abstractbound c, abstractbound d)
	{
		super(yyp);
		if (c.me() instanceof boundAnd)
		{
			ound tempVar = c.me();
			List = (tempVar instanceof boundAnd ? (boundAnd)tempVar : null).List;
		}
		else
		{
			List = new ArrayList<abstractbound>(Arrays.asList(c.me()));
		}
		if (d.me() instanceof boundAnd)
		{
			if (List == null)
			{
				List = new ArrayList<abstractbound>();
			}
			ound tempVar2 = d.me();
			List.addAll((tempVar2 instanceof boundAnd ? (boundAnd)tempVar2 : null).List);
		}
		else
		{
			if (List == null)
			{
				List = new ArrayList<abstractbound>();
			}
			List.add(d.me());
		}
	}
	@Override
	public boolean isStrict()
	{
		return List.size() == 1 && List.get(0).isStrict();
	}
	@Override
	public dataval getStrictVal()
	{
		if (List.size() == 1)
		{
			return List.get(0).getStrictVal();
		}
		throw new IllegalStateException();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "boundAnd";
	}
	@Override
	public int getYynumEndl()
	{
		return 193;
	}
}