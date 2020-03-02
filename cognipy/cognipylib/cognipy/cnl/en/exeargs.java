package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+exeargs+232
public class exeargs implements iaccept, PartialSymbol
{
	public exeargs(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<iexevar> exevars;

	public exeargs(Parser yyp, iexevar s)
	{
		super(yyp);
	exevars = new ArrayList<iexevar>();
	exevars.add(s);
	}

	public exeargs(Parser yyp, exeargs z, iexevar r)
	{
		super(yyp);
	exevars = z.exevars;
	exevars.add(r);
	}
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "exeargs";
	}
	@Override
	public int getYynumEndl()
	{
		return 232;
	}
}