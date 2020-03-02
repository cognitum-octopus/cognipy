package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Value+86
public class Value extends PartialSymbol implements IAccept
{

	public static Value FromObject(Object obj)
	{
		if (obj.getClass() == Integer.class)
		{
			CNL.DL.Number tempVar = new CNL.DL.Number(null);
			tempVar.val = obj.toString();
			return tempVar;
		}
		else if (obj.getClass() == String.class)
		{
			CNL.DL.String tempVar2 = new CNL.DL.String(null);
			tempVar2.val = "'" + obj.toString().replace("'", "''") + "'";
			return tempVar2;
		}
		else if (obj.getClass() == Double.class)
		{
			CNL.DL.Float tempVar3 = new CNL.DL.Float(null);
			tempVar3.val = obj.toString();
			return tempVar3;
		}
		else if (obj.getClass() == Boolean.class)
		{
			CNL.DL.Bool tempVar4 = new CNL.DL.Bool(null);
			tempVar4.val = ((Boolean)obj) ? "[1]" : "[0]";
			return tempVar4;
		}
		else if (obj.getClass() == DateTimeOffset.class)
		{
			//                Debugger.Break(); // lets check if it can be serialized this way
			CNL.DL.DateTimeVal tempVar5 = new CNL.DL.DateTimeVal(null);
			tempVar5.val = ((DateTimeOffset)obj).toString("s");
			return tempVar5;
		}
		else if (obj.getClass() == TimeSpan.class)
		{
			//                Debugger.Break(); // lets check if it can be serialized this way
			CNL.DL.Duration tempVar6 = new CNL.DL.Duration(null);
			tempVar6.val = System.Xml.XmlConvert.toString((TimeSpan)obj);
			return tempVar6;
		}
		throw new IllegalStateException();
	}

	public static Object ToObject(Value val)
	{
		if (val.getClass() == CNL.DL.Number.class)
		{
			return Integer.parseInt(val.getVal());
		}
		else if (val.getClass() == CNL.DL.String.class)
		{
			String v = val.getVal();
			return v.substring(1, 1 + v.length() - 2).replace("''", "'");
		}
		else if (val.getClass() == CNL.DL.Float.class)
		{
			return Double.parseDouble(String.format(en_cult, val.getVal()));
		}
		else if (val.getClass() == CNL.DL.Bool.class)
		{
			return val.getVal().equals("[1]");
		}
		else if (val.getClass() == CNL.DL.DateTimeVal.class)
		{
			return DateTimeOffset.Parse(val.getVal());
		}
		else if (val.getClass() == CNL.DL.Duration.class)
		{
			return System.Xml.XmlConvert.ToTimeSpan(val.getVal());
		}
		throw new IllegalStateException();
	}

	public static Value MakeFrom(String typeTag, String val)
	{
		switch (typeTag)
		{
			case "I":
				CNL.DL.Number tempVar = new CNL.DL.Number(null);
				tempVar.val = val;
				return tempVar;
			case "S":
				CNL.DL.String tempVar2 = new CNL.DL.String(null);
				tempVar2.val = val;
				return tempVar2;
			case "F":
				CNL.DL.Float tempVar3 = new CNL.DL.Float(null);
				tempVar3.val = val;
				return tempVar3;
			case "B":
				CNL.DL.Bool tempVar4 = new CNL.DL.Bool(null);
				tempVar4.val = val;
				return tempVar4;
			case "T":
				CNL.DL.DateTimeVal tempVar5 = new CNL.DL.DateTimeVal(null);
				tempVar5.val = val;
				return tempVar5;
			case "D":
				CNL.DL.Duration tempVar6 = new CNL.DL.Duration(null);
				tempVar6.val = val;
				return tempVar6;
			default:
				throw new IllegalStateException();
		}
	}



	public Value(Parser yyp)
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
	public String getTypeTag()
	{
		return null;
	}

	public final String ToStringExact()
	{
		return getVal();
	}

	@Override
	public String toString()
	{
		return getVal();
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
		return getVal().equals("[1]");
	}


	@Override
	public String getYynameDl()
	{
		return "Value";
	}
	@Override
	public int getYynumDl()
	{
		return 86;
	}
}