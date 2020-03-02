package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public abstract class JenaNode
{
	private DLToOWLNameConv owlNC = null;

	public final String ToOwlName(String name, ARS.EntityKind whatFor)
	{
		return "<" + owlNC.getIRIFromId(name, whatFor).toString() + ">";
	}

	private String freeVarId;

	public JenaNode(DLToOWLNameConv owlNC, String freeVarId)
	{
		this.freeVarId = freeVarId;
		this.owlNC = owlNC;
	}
	public final String GetFreeVariableId()
	{
		return freeVarId;
	}
	public abstract String ToJenaRule();

	public final String ToCombinedBlock()
	{
		return ToJenaRule();
	}

	private static Regex DtmRg = new Regex("(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?))?", RegexOptions.Compiled);
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
				else if (c != '\"')
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