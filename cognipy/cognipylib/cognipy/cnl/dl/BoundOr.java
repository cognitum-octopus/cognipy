package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+BoundOr+110
public class BoundOr extends AbstractBound
{
	@Override
	public int priority()
	{
		return 2;
	}
	public ArrayList<AbstractBound> List;
	public BoundOr(Parser yyp)
	{
		super(yyp);
	}
	public BoundOr(Parser yyp, AbstractBound c, AbstractBound d)
	{
		super(yyp);
		if (c.me() instanceof BoundOr)
		{
			cognipy.cnl.dl.AbstractBound tempVar = c.me();
			List = (tempVar instanceof BoundOr ? (BoundOr)tempVar : null).List;
		}
		else
		{
			List = new ArrayList<AbstractBound>(Arrays.asList(c.me()));
		}
		if (d.me() instanceof BoundOr)
		{
			if (List == null)
			{
				List = new ArrayList<AbstractBound>();
			}
			cognipy.cnl.dl.AbstractBound tempVar2 = d.me();
			List.addAll((tempVar2 instanceof BoundOr ? (BoundOr)tempVar2 : null).List);
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
		return "BoundOr";
	}
	@Override
	public int getYynumDl()
	{
		return 110;
	}
}