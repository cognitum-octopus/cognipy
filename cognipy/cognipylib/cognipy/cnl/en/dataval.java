package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+dataval+175
public class dataval implements iaccept, PartialSymbol
{
	public dataval(Parser yyp)
	{
		super(yyp);
	}

	public Object accept(IVisitor v)
	{
		return null;
	}
	public String getVal()
	{
		return null;
	}

	@Override
	public String toString()
	{
		return getVal().substring(1, 1 + getVal().length() - 2).replace("\'\'", "\'");
	}

	private static System.Globalization.CultureInfo en_cult = new System.Globalization.CultureInfo("en-US");
	public final double ToDouble()
	{
		return Double.parseDouble(String.format(en_cult.NumberFormat, getVal()));
	}

	public final int ToInt()
	{
		return Integer.parseInt(getVal());
	}

	public final boolean ToBool()
	{
		return getVal().equals("true");
	}


	@Override
	public String getYynameEndl()
	{
		return "dataval";
	}
	@Override
	public int getYynumEndl()
	{
		return 175;
	}
}