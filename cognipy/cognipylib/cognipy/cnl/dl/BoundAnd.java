package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+BoundAnd+109
public class BoundAnd extends AbstractBound
{
	@Override
	public int priority()
	{
		return 3;
	}
	public ArrayList<AbstractBound> List;
	public BoundAnd(Parser yyp)
	{
		super(yyp);
	}
	public BoundAnd(Parser yyp, AbstractBound c, AbstractBound d)
	{
		super(yyp);
		if (c.me() instanceof BoundAnd)
		{
			cognipy.cnl.dl.AbstractBound tempVar = c.me();
			List = (tempVar instanceof BoundAnd ? (BoundAnd)tempVar : null).List;
		}
		else
		{
			List = new ArrayList<AbstractBound>(Arrays.asList(c.me()));
		}
		if (d.me() instanceof BoundAnd)
		{
			if (List == null)
			{
				List = new ArrayList<AbstractBound>();
			}
			cognipy.cnl.dl.AbstractBound tempVar2 = d.me();
			List.addAll((tempVar2 instanceof BoundAnd ? (BoundAnd)tempVar2 : null).List);
		}
		else
		{
			if (List == null)
			{
				List = new ArrayList<AbstractBound>();
			}
			List.add(d.me());
		}
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "BoundAnd";
	}
	@Override
	public int getYynumDl()
	{
		return 109;
	}
}