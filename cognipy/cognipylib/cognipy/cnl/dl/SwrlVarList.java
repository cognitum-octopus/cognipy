package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+SwrlVarList+151
public class SwrlVarList extends PartialSymbol implements IAccept
{
	public SwrlVarList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<IExeVar> list;

	public SwrlVarList(Parser yyp, ID ins, ID dat)
	{
		super(yyp);
		list = new ArrayList<IExeVar>();
		if (ins != null)
		{
			list.add(new SwrlIVar(yyp, ins));
		}
		if (dat != null)
		{
			list.add(new SwrlDVar(yyp, dat));
		}
	}

	public SwrlVarList(Parser yyp, ID ins, ID dat, SwrlVarList sl)
	{
		super(yyp);
		list = sl.list;
		if (ins != null)
		{
			list.add(new SwrlIVar(yyp, ins));
		}
		if (dat != null)
		{
			list.add(new SwrlDVar(yyp, dat));
		}
	}

	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlVarList";
	}
	@Override
	public int getYynumDl()
	{
		return 151;
	}
}