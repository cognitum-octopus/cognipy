package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+SwrlItemList+133
public class SwrlItemList extends PartialSymbol implements IAccept
{
	public SwrlItemList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<SwrlItem> list;
	public SwrlItemList(Parser yyp, SwrlItem sid_)
	{
		super(yyp);
		list = new ArrayList<SwrlItem>();
		list.add(sid_);
	}

	public SwrlItemList(Parser yyp, SwrlItemList sl_, SwrlItem sid_)
	{
		super(yyp);
		list = sl_.list;
		list.add(sid_);
	}

	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlItemList";
	}
	@Override
	public int getYynumDl()
	{
		return 133;
	}
}