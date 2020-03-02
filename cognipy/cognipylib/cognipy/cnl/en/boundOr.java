package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+boundOr+194
public class boundOr extends abstractbound
{
	public ArrayList<abstractbound> List;
	@Override
	public int priority()
	{
		return 2;
	}
	public boundOr(Parser yyp)
	{
		super(yyp);
	}
	public boundOr(Parser yyp, abstractbound c, abstractbound d)
	{
		super(yyp);
		if (c.me() instanceof boundOr)
		{
			ound tempVar = c.me();
			List = (tempVar instanceof boundOr ? (boundOr)tempVar : null).List;
		}
		else
		{
			List = new ArrayList<abstractbound>(Arrays.asList(c.me()));
		}
		if (d.me() instanceof boundOr)
		{
			if (List == null)
			{
				List = new ArrayList<abstractbound>();
			}
			ound tempVar2 = d.me();
			List.addAll((tempVar2 instanceof boundOr ? (boundOr)tempVar2 : null).List);
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
		return "boundOr";
	}
	@Override
	public int getYynumEndl()
	{
		return 194;
	}
}