package cognipy.ars;

import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import cognipy.*;
import java.util.*;

public class OwlNameingConventionUnderscore implements IOwlNameingConvention
{

	private static String UPPERL = "[A-Z]";
	private static String LOWERL = "[a-z]";
	private static String DIGIT = "[0-9]";
	private static String NAME = "\\A(?<g>" + UPPERL + "?" + LOWERL + "+)(_((?<g>" + UPPERL + "?" + LOWERL + "+)|(?<g>" + DIGIT + "+)))*\\Z";
	private static String NAME2 = "\\A(?<g>" + UPPERL + "?" + LOWERL + "+)(\\-((?<g>" + UPPERL + "?" + LOWERL + "+)|(?<g>" + DIGIT + "+)))*\\Z";

	private static Regex aregexp = new Regex(NAME, RegexOptions.Compiled);
	private static Regex aregexp2 = new Regex(NAME2, RegexOptions.Compiled);
	private boolean bigConcept;

	public OwlNameingConventionUnderscore(char separator, boolean bigConcept)
	{
		bregexp = separator == '_' ? aregexp : aregexp2;
		this.bigConcept = bigConcept;
	}

	private Regex bregexp = null;

	public final cognipy.cnl.dl.DlName ToDL(OwlName owlname, CNL.EN.endict lex, tangible.Func1Param<String, String> ns2pfx, EntityKind madeFor)
	{
		cognipy.ars.OwlName.Parts parts = owlname.Split();
		String defaultNs = ns2pfx.invoke(null);
		if (tangible.StringHelper.isNullOrWhiteSpace(parts.ns))
		{
			parts.ns = defaultNs;
		}

		String pfx = null;
		if (!IRIParser.AreNamespacesEqual(defaultNs, parts.ns))
		{
			pfx = ns2pfx.invoke(parts.ns);
		}

		System.Text.RegularExpressions.Match mth = bregexp.Match(parts.name);
		System.Text.RegularExpressions.CaptureCollection captures = mth.Groups["g"].Captures;
		DlName.Parts dlp = new DlName.Parts();
		dlp.term = pfx;
		if (mth.Success && ((!bigConcept && (((madeFor == EntityKind.Instance) && Character.isUpperCase(parts.name.First())) || ((madeFor == EntityKind.Concept || madeFor == EntityKind.DataRole || madeFor == EntityKind.Role) && Character.isLowerCase(parts.name.First())))) || (bigConcept && (((madeFor == EntityKind.Instance) && Character.isUpperCase(parts.name.First())) || ((madeFor == EntityKind.Concept || madeFor == EntityKind.DataRole || madeFor == EntityKind.Role) && Character.isLowerCase(parts.name.First()))))))
		{
			dlp.quoted = false;
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			for (System.Text.RegularExpressions.Capture n : captures)
			{
				String ss = n.toString();
				if (first)
				{
					first = false;
					if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
					{
						String nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PastParticiple);
						sb.append(nn);
						continue;
					}
				}
				else
				{
					sb.append("-");
				}
				sb.append(madeFor == EntityKind.Instance ? Character.toUpperCase(ss.charAt(0)) : Character.toLowerCase(ss.charAt(0)));
				sb.append(ss.substring(1));
			}
			dlp.name = sb.toString();
		}
		else
		{
			dlp.quoted = true;
			dlp.name = parts.name;
		}

		return dlp.Combine();
	}


	public final OwlName FromDL(cognipy.cnl.dl.DlName dl, CNL.EN.endict lex, tangible.Func1Param<String, String> pfx2ns, EntityKind madeFor)
	{
		Debugger.Break();
		throw new UnsupportedOperationException();
	}
}