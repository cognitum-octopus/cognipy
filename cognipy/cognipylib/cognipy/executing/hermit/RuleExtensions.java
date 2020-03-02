package cognipy.executing.hermit;

import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.*;
import cognipy.*;
import java.util.*;
import java.io.*;

public final class RuleExtensions
{
	public static Object getValFromJenaLiteral(Object val)
	{
		if (val instanceof java.lang.Double)
		{
			val = (val instanceof java.lang.Double ? (java.lang.Double)val : null).doubleValue();
		}
		else if (val instanceof java.lang.Float)
		{
			val = (val instanceof java.lang.Float ? (java.lang.Float)val : null).doubleValue();
		}
		else if (val instanceof Float)
		{
			val = (Double)val;
		}
		else if (val instanceof java.lang.Number)
		{
			val = (val instanceof java.lang.Number ? (java.lang.Number)val : null).intValue();
		}
		else if (val instanceof java.lang.Integer)
		{
			val = (val instanceof java.lang.Integer ? (java.lang.Integer)val : null).intValue();
		}
		else if (val instanceof java.lang.Long)
		{
			val = (val instanceof java.lang.Long ? (java.lang.Long)val : null).intValue();
		}
		else if (val instanceof Long)
		{
			val = (Integer)val;
		}
		else if (val instanceof java.lang.String)
		{
			val = val.toString();
		}
		else if (val instanceof java.lang.Boolean)
		{
			val = (val instanceof java.lang.Boolean ? (java.lang.Boolean)val : null).booleanValue();
		}
		else if (val instanceof org.apache.jena.datatypes.xsd.XSDDateTime)
		{
			val = DateTimeOffset.Parse((val instanceof org.apache.jena.datatypes.xsd.XSDDateTime ? (org.apache.jena.datatypes.xsd.XSDDateTime)val : null).toString());
		}
		else if (val.toString().endsWith(xsdDayTimeDuration))
		{
			val = System.Xml.XmlConvert.ToTimeSpan(val.toString().substring(0, val.toString().length() - xsdDayTimeDuration.length()));
		}

		return val;
	}

	private static final String xsdDayTimeDuration = "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration";

	public static boolean isSimpleJenaObject(Object o)
	{
		return isInteger(o) || isDouble(o) || o instanceof String || o instanceof Boolean || o instanceof DateTimeOffset || o instanceof TimeSpan;
	}

	public static boolean isDouble(Object val)
	{
		return (val instanceof Double || val instanceof Float);
	}
	public static boolean isInteger(Object v1)
	{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: return v1 is int || v1 is long || v1 is short || v1 is uint || v1 is ulong || v1 is ushort;
		return v1 instanceof Integer || v1 instanceof Long || v1 instanceof Short || v1 instanceof Integer || v1 instanceof Long || v1 instanceof Short;
	}
	public static String lex(org.apache.jena.graph.Node n, Builtin bi, RuleContext context)
	{
		if (n.isBlank())
		{
			return n.getBlankNodeLabel();
		}
		else if (n.isURI())
		{
			return n.getURI();
		}
		else if (n.isLiteral())
		{
			return n.getLiteralLexicalForm();
		}
		else
		{
			throw new BuiltinException(bi, context, "Illegal node type: " + n);
		}
	}
}