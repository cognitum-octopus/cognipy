package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public abstract class SparqlNode
{
	private DLToOWLNameConv owlNC = null;

	public final String ToOwlName(String name, ARS.EntityKind whatFor)
	{
		return "<" + owlNC.getIRIFromId(name, whatFor).toString() + ">";
	}

	private String freeVarId;

	public SparqlNode(DLToOWLNameConv owlNC, String freeVarId)
	{
		this.freeVarId = freeVarId;
		this.owlNC = owlNC;
	}
	public final String GetFreeVariableId()
	{
		return freeVarId;
	}

	public final abstract String ToSparqlBody(boolean meanSuperConcept);
//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public abstract string ToSparqlBody(bool meanSuperConcept, bool instance = true);
	public abstract String ToSparqlBody(boolean meanSuperConcept, boolean instance);

	public String ToSparqlMinus(boolean meanSuperConcept)
	{
		return ToSparqlMinus(meanSuperConcept, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public virtual string ToSparqlMinus(bool meanSuperConcept, bool instance = true)
	public String ToSparqlMinus(boolean meanSuperConcept, boolean instance)
	{
		return "";
	}
	public String ToSparqlFilter()
	{
		return "";
	}

	public String ToSparqlFilter(boolean includeTopBot, boolean removeClass)
	{
		return "";
	}

	public boolean UseDistinct()
	{
		return false;
	}



	public final String ToCombinedBlock(boolean meanSuperConcept)
	{
		return ToCombinedBlock(meanSuperConcept, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ToCombinedBlock(bool meanSuperConcept, bool instance = true)
	public final String ToCombinedBlock(boolean meanSuperConcept, boolean instance)
	{
		String bod = ToSparqlBody(meanSuperConcept, instance);
		String flt = ToSparqlFilter();

		return bod + (tangible.StringHelper.isNullOrEmpty(flt) ? "" : "\r\nFILTER(" + flt + ")");
	}

	public final String ToCombinedBlock(boolean meanSuperConcept, boolean instance, boolean direct, boolean includeTopBot, boolean removeClass)
	{
		String bod = ToSparqlBody(meanSuperConcept, instance);
		String flt = ToSparqlFilter(includeTopBot, removeClass);

		String wholeBody = bod;
		if (direct)
		{
			String min = ToSparqlMinus(meanSuperConcept, instance);
			if (!tangible.StringHelper.isNullOrEmpty(min))
			{
				wholeBody = "{" + bod + "}\r\nMINUS{" + min + "}";
			}
		}
		return wholeBody + (tangible.StringHelper.isNullOrEmpty(flt) ? "" : "\r\nFILTER(" + flt + ")");
	}

	private static Regex DtmRg = new Regex("(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?)(Z|((\\+|\\-)[0-2][0-9]:[0-5][0-9]))?)?", RegexOptions.Compiled);
	private static String completeDTMVal(String val)
	{
		System.Text.RegularExpressions.Match m = DtmRg.Match(val);
		String dta = m.Groups["date"].Value;
		String tm = m.Groups["time"].Value;
		StringBuilder sb = new StringBuilder();
		sb.append(dta);
		if (tangible.StringHelper.isNullOrEmpty(tm))
		{
			sb.append("T00:00:00");
		}
		else
		{
			sb.append(tm);
		}
		if (tm.length() == "T00:00".length())
		{
			sb.append(":00");
		}
		return sb.toString();
	}

	private static String escapeString(String str)
	{
		StringBuilder ret = new StringBuilder();
		ret.append("""");
		for (char c : str)
		{
			switch (c)
			{
				case '\t':
					ret.append("\\t");
					break;
				case '\n':
					ret.append("\\n");
					break;
				case '\r':
					ret.append("\\r");
					break;
				case (char)0x0008:
					ret.append("\\b");
					break;
				case (char)0x000C:
					ret.append("\\f");
					break;
				case '\"':
					ret.append("\\\"");
					break;
				case '\'':
					ret.append("\\'");
					break;
				case '\\':
					ret.append("\\\\");
					break;
				default:
					ret.append(c);
					break;
			}
		}
		ret.append("""");
		return ret.toString();
	}

	private static String unescapeString(String str)
	{
		StringBuilder ret = new StringBuilder();
		boolean wasBS = false;
		if (str.length() == 0)
		{
			return str;
		}
		char begChar = str.charAt(0);
		if (begChar != '\'' && begChar != '\"')
		{
			return str;
		}

		for (char c : str)
		{
			if (wasBS)
			{
				switch (c)
				{
					case 't':
						ret.append('\t');
						break;
					case 'n':
						ret.append('\n');
						break;
					case 'r':
						ret.append('\r');
						break;
					case 'b':
						ret.append((char)0x0008);
						break;
					case 'f':
						ret.append((char)0x000C);
						break;
					case '\"':
						ret.append('\"');
						break;
					case '\'':
						ret.append('\'');
						break;
					case '\\':
						ret.append('\\');
						break;
					default:
						ret.append(c);
						break;
				}
				wasBS = false;
			}
			else
			{
				if (c == '\\')
				{
					wasBS = true;
				}
				else if (c != begChar)
				{
					ret.append(c);
				}
			}
		}
		return ret.toString();
	}

	public static String GetLiteralVal(cognipy.cnl.dl.Value v)
	{
		if (v instanceof CNL.DL.Bool)
		{
			return escapeString(v.ToBool() ? "true" : "false") + "^^xsd:boolean";
		}
		if (v instanceof CNL.DL.String)
		{
			return escapeString(v.toString()) + "^^xsd:string";
		}
		if (v instanceof CNL.DL.Float)
		{
			return escapeString(v.ToStringExact()) + "^^xsd:double";
		}
		if (v instanceof CNL.DL.Number)
		{
			return escapeString(v.ToStringExact()) + "^^xsd:integer";
		}
		if (v instanceof CNL.DL.DateTimeVal)
		{
			return escapeString(completeDTMVal(v.ToStringExact())) + "^^xsd:dateTime";
		}
		if (v instanceof CNL.DL.Duration)
		{
			return escapeString(v.ToStringExact()) + "^^xsd:duration";
		}

		return escapeString(v.toString());
	}

	private static System.Globalization.CultureInfo en_cult = new System.Globalization.CultureInfo("en-US");

	public static Object ToTypedValue(String uri)
	{
		int ttpos = uri.lastIndexOf('^');
		String ttag;
		String str;
		if (ttpos <= 0)
		{
			ttag = "string";
			str = unescapeString(uri);
		}
		else
		{
			int ttxpos = uri.indexOf('^');
			str = unescapeString(uri.substring(0, ttxpos));
			ttag = uri.substring(ttpos + 1).toLowerCase();
		}
		if (ttag.endsWith(">"))
		{
			ttag = ttag.substring(1, 1 + ttag.length() - 2);
		}
		if (ttag.endsWith("boolean"))
		{
			return (str.compareTo("true") == 0);
		}
		else if (ttag.endsWith("string"))
		{
			return str;
		}
		else if (ttag.endsWith("double"))
		{
			return Double.parseDouble(String.format(en_cult, str));
		}
		else if (ttag.endsWith("integer") || ttag.endsWith("int"))
		{
			return Integer.parseInt(str);
		}
		else if (ttag.endsWith("dateTime") || ttag.endsWith("datetime"))
		{
			return DateTimeOffset.Parse(str);
		}
		else
		{
			return null;
		}
	}
}