package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+SwrlObjectList+146
public class SwrlObjectList extends PartialSymbol implements IAccept
{
	public SwrlObjectList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<ISwrlObject> Values;

	public SwrlObjectList(Parser yyp, ISwrlObject a)
	{
		super(yyp);
		Values = new ArrayList<ISwrlObject>();
		Values.add(a);
	}

	public SwrlObjectList(Parser yyp, SwrlObjectList sl, ISwrlObject a)
	{
		super(yyp);
		Values = sl.Values;
		Values.add(a);
	}

	public Object accept(IVisitor v)
	{
		return null;
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlObjectList";
	}
	@Override
	public int getYynumDl()
	{
		return 146;
	}
}