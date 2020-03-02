package cognipy.ars;

import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import cognipy.*;
import java.util.*;

//public class OwlNameingConventionSmartImport : IOwlNameingConvention
//{
//    static string UPPERL = @"[A-Z]";
//    static string LOWERL = @"[a-z]";
//    static string DIGIT = @"[0-9]";
//    static string NAME = "\\A((?<g>" + UPPERL + "?" + LOWERL + @"*)|(?<g>" + DIGIT + "+)|_|\\-|\\.)*\\Z";
//    static string BIGNAME = "\\A((?<g>" + UPPERL + "+" + @")|(?<g>" + DIGIT + "+)|_|\\-|\\.)*\\Z";


//    static Regex normregexp = new Regex(NAME, RegexOptions.Compiled);
//    static Regex bigregexp = new Regex(BIGNAME, RegexOptions.Compiled);

//    Func<string, IEnumerable<string>> getForms = null;

//    public OwlNameingConventionSmartImport(Func<string, IEnumerable<string>> getForms)
//    {
//        this.getForms = getForms;
//    }

//    static HashSet<string> pfxes = new HashSet<string>(new string[] { "sub", "super", "full" });

//    static WordNetClasses.WN wordnet = new WordNetClasses.WN();

//    private bool isVerb(CNL.EN.endict lex, string word, IEnumerable<string> forms)
//    {
//        foreach (var frm in forms)
//        {
//            bool b = false;
//            Wnlib.SearchSet bobj2 = null;
//            var list = new System.Collections.ArrayList();
//            lock(wordnet)
//                wordnet.OverviewFor(frm, "verb", ref b, ref bobj2, list);
//            foreach (var e in list)
//            {
//                Wnlib.Search s = e as Wnlib.Search;
//                var pp = lex.toN_Simple(s.word.ToLower(), CNL.EN.endict.WordKind.PastParticiple);
//                var sp = lex.toN_Simple(s.word.ToLower(), CNL.EN.endict.WordKind.SimplePast);
//                if (word.ToLower() == pp.ToLower() || word.ToLower() == sp.ToLower())
//                    return true;
//            }
//        }
//        return false;
//    }

//    private bool isPluralNoun(CNL.EN.endict lex, string word, IEnumerable<string> forms)
//    {
//        foreach (var frm in forms)
//        {
//            bool b = false;
//            Wnlib.SearchSet bobj2 = null;
//            var list = new System.Collections.ArrayList();
//            lock (wordnet)
//                wordnet.OverviewFor(frm, "noun", ref b, ref bobj2, list);
//            foreach (var e in list)
//            {
//                Wnlib.Search s = e as Wnlib.Search;
//                var spp = lex.toDL_Simple(s.word.ToLower(), CNL.EN.endict.WordKind.PluralFormNoun);
//                var sp = lex.toN_Simple(spp, CNL.EN.endict.WordKind.NormalForm);
//                if (word.ToLower() != sp.ToLower())
//                    return true;
//            }
//        }
//        return false;
//    }

//    private bool isPOS(string word, IEnumerable<string> forms, string pos, bool equalToBaseForm)
//    {
//        foreach (var frm in forms)
//        {
//            bool b = false;
//            Wnlib.SearchSet bobj2 = null;
//            var list = new System.Collections.ArrayList();
//            lock (wordnet)
//                wordnet.OverviewFor(frm, pos, ref b, ref bobj2, list);
//            foreach (var e in list)
//            {
//                if (equalToBaseForm)
//                {
//                    Wnlib.Search s = e as Wnlib.Search;
//                    if (word.ToLower() == s.word.ToLower())
//                        return true;
//                }
//                else
//                    return true;
//            }
//        }
//        return false;
//    }

//    public DlName ToDL(OwlName owlname, CNL.EN.endict lex, Func<string, string> ns2pfx, EntityKind madeFor)
//    {
//        var parts = owlname.Split();
//        string defaultNs = ns2pfx(null);
//        if (string.IsNullOrWhiteSpace(parts.ns))
//            parts.ns = defaultNs;

//        if (parts.name == null)
//            return new DlName() { id = null };

//        string pfx = null;

//        if(!IRIParser.AreNamespacesEqual(defaultNs,parts.ns))
//            pfx = ns2pfx(parts.ns);

//        var name_part = parts.name;
//        var sufix = "";
//        if (madeFor == EntityKind.SWRLVariable)
//        {
//            var a = name_part.Split('_');
//            if (a.Length == 2)
//            {
//                name_part = a.First();
//                sufix = a.Last();
//            }
//        }

//        List<string> grps = new List<string>();
//        var dlp = new DlName.Parts() { term = pfx };

//        Match mth = null;

//        if (bigregexp.IsMatch(name_part))
//            mth = bigregexp.Match(name_part);
//        else
//            mth = normregexp.Match(name_part);

//        if (mth.Success)
//        {
//            var grps2 = mth.Groups["g"].Captures;
//            foreach (var g in grps2)
//                if (g.ToString().Length > 0)
//                    grps.Add(g.ToString().ToLower());
//        }
//        else
//        {
//            grps = name_part.ToLower().Split('-', '_', '.').ToList();
//        }

//        dlp.quoted = false;

//        StringBuilder sb = new StringBuilder();
//        var first = true;
//        bool addOf = false;
//        foreach (var ss in grps)
//        {
//            if (first)
//            {
//                first = false;
//                if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
//                {
//                    var word = grps.First();
//                    var forms = getForms(word);
//                    if (!isVerb(lex, word, forms))
//                    {
//                        if (grps.Count() == 1 && (isPOS(word, forms, "adj", false) || isPOS(word, forms, "adv", false)) && !isPOS(word, forms, "noun", true))
//                            sb.Append("be-" + ss);
//                        else if (grps.Last() == "of")
//                            sb.Append("be-" + ss);
//                        else
//                            sb.Append("have-" + ss);
//                    }
//                    else
//                    {
//                        var spp = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.SimplePast);
//                        var sp = lex.toN_Simple(spp, CNL.EN.endict.WordKind.SimplePast);
//                        if (pfxes.Contains(ss))
//                        {
//                            sb.Append("be-" + ss);
//                            addOf = true;
//                        }
//                        else if (sp == ss)
//                        {
//                            sb.Append("be-" + ss);
//                        }
//                        else
//                        {
//                            var nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PastParticiple);
//                            sb.Append(nn);
//                        }
//                    }
//                    continue;
//                }
//                else if (madeFor == EntityKind.Concept)
//                {
//                    var word = grps.First();
//                    var forms = getForms(word);
//                    if (isPluralNoun(lex, word, forms))
//                    {
//                        var nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PluralFormNoun);
//                        if (nn.ToLower() != word.ToLower() && nn.Length > 2)
//                        {
//                            sb.Append(char.ToLower(nn[0]));
//                            sb.Append(nn.Substring(1));
//                            continue;
//                        }
//                    }
//                }
//            }
//            else
//                sb.Append("-");
//            if (ss.Length == 0)
//            {
//                sb.Append(madeFor == EntityKind.Instance ? "U" : "u");
//                sb.Append("nknown");
//            }
//            else
//            {
//                sb.Append(madeFor == EntityKind.Instance ? char.ToUpper(ss[0]) : char.ToLower(ss[0]));
//                sb.Append(ss.Substring(1));
//            }
//        }
//        if (addOf)
//        {
//            if (!sb.ToString().ToLower().EndsWith("-of"))
//                sb.Append("-of");
//        }
//        dlp.name = sb.ToString() + (string.IsNullOrEmpty(sufix)?"":"_" + sufix);
//        dlp.local = ((madeFor== EntityKind.Instance) && !parts.global);
//        return dlp.Combine();
//    }

//    public OwlName FromDL(DlName dl, CNL.EN.endict lex, Func<string, string> pfx2ns, EntityKind madeFor)
//    {
//        return new OwlNameingConventionCamelCase().FromDL(dl, lex, pfx2ns, madeFor);
//    }
//}

public class OwlNameingConventionCamelCase implements IOwlNameingConvention
{

	private static String UPPERL = "[A-Z]";
	private static String LOWERL = "[a-z]";
	private static String DIGIT = "[0-9]";
	private static String DIGIT_D = "((\\-)?" + DIGIT + "+)";
	private static String NAME = "\\A(?<g>" + LOWERL + "+)?((?<g>" + UPPERL + LOWERL + "*)|(?<g>" + DIGIT + DIGIT_D + "*))*\\b" + "(?<u>\\.(" + UPPERL + "|" + LOWERL + "|" + DIGIT + ")+)?\\Z";
	private static String exoticUTFForUnderscore = IRIParser.getFirstCharForInternalUse();
	private static String VBIGNAME = "\\A(?<g>" + UPPERL + UPPERL + "+)(" + exoticUTFForUnderscore + "((?<g>" + UPPERL + "+)|(?<g>" + DIGIT + "+)))*\\b" + "(?<u>\\.(" + UPPERL + "|" + LOWERL + "|" + DIGIT + ")+)?\\Z";

	private static Regex regexp = new Regex(NAME, RegexOptions.Compiled);
	private static Regex vbregexp = new Regex(VBIGNAME, RegexOptions.Compiled);

	/** 
	 Translate an OWLname to a CNL (DL) name
	 
	 @param owlname the OWL name
	 @param lex
	 @param ns2pfx
	 @param madeFor the type of this entity (Concept, Instance, Role,...)
	 @return 
	*/
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

		DlName.Parts dlp = new DlName.Parts();
		dlp.term = pfx;

		if (parts.name.startsWith("⊤"))
		{
			dlp.name = "⊤";
			if (parts.name.contains("."))
			{
				dlp.name = "[" + dlp.name + "]";
			}
			dlp.quoted = false;
		}
		else
		{
			String name_part = parts.name;
			boolean isVBigName = parts.global;
			String sufix = "";
			if (madeFor == EntityKind.SWRLVariable)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var a = name_part.split("[_]", -1);
				if (a.Length == 2)
				{
					name_part = a.First();
					sufix = a.Last();
				}
			}
			Match mth = null;
			if (!isVBigName)
			{
				mth = regexp.Match(name_part);
			}
			Match mth2 = null;
			if (isVBigName || !mth.Success)
			{
				mth2 = vbregexp.Match(name_part);
			}
			if (!name_part.equals("") && ((mth != null && mth.Success && (((madeFor == EntityKind.SWRLVariable || madeFor == EntityKind.Instance) && Character.isUpperCase(name_part.First())) || ((madeFor == EntityKind.Concept || madeFor == EntityKind.DataRole || madeFor == EntityKind.SWRLVariable || madeFor == EntityKind.Role) && Character.isLowerCase(name_part.First())))) || (mth2 != null && mth2.Success && madeFor == EntityKind.Instance)))
			{
				System.Text.RegularExpressions.CaptureCollection grps = (mth != null && mth.Success) ? mth.Groups["g"].Captures : mth2.Groups["g"].Captures;
				System.Text.RegularExpressions.CaptureCollection urps = (mth != null && mth.Success) ? mth.Groups["u"].Captures : mth2.Groups["u"].Captures;
				dlp.quoted = false;
				StringBuilder sb = new StringBuilder();
				boolean first = true;
				boolean success = true;
				for (System.Text.RegularExpressions.Capture n : grps)
				{
					String ss = n.toString();
					if (first)
					{
						first = false;
						if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
						{
							String nn = lex.toDL_Simple(ss, CNL.EN.endict.WordKind.PastParticiple);
							if (!lex.toN_Simple(nn, CNL.EN.endict.WordKind.PastParticiple).equals(ss))
							{
								success = false;
								dlp.quoted = true;
								dlp.name = parts.name + (tangible.StringHelper.isNullOrEmpty(sufix) ? "" : "_" + sufix);
								break;
							}
							sb.append(nn);
							continue;
						}
					}
					else
					{
						sb.append("-");
					}
					sb.append(madeFor == EntityKind.Instance && !(urps.Count > 0) ? Character.toUpperCase(ss.charAt(0)) : Character.toLowerCase(ss.charAt(0)));
					sb.append(ss.substring(1));
				}
				if (success)
				{
					dlp.name = sb.toString() + (tangible.StringHelper.isNullOrEmpty(sufix) ? "" : "_" + sufix);
					if (urps.Count > 0)
					{
						dlp.name = "[" + dlp.name + "]";
					}
				}
			}
			else
			{
				dlp.quoted = true;
				dlp.name = parts.name + (tangible.StringHelper.isNullOrEmpty(sufix) ? "" : "_" + sufix);
			}
		}

		if (madeFor == EntityKind.Instance)
		{
			dlp.name = IRIParser.decodeIRI(dlp.name);
		}

		dlp.local = ((madeFor == EntityKind.Instance) && !parts.global);

		return dlp.Combine();
	}

	/** 
	 Translate a CNL(DL) name to a OWL name
	 
	 @param dl the CNL name
	 @param lex
	 @param pfx2ns
	 @param madeFor the type of this entity (Concept, Instance, Role,...)
	 @return 
	*/
	public final OwlName FromDL(cognipy.cnl.dl.DlName dl, CNL.EN.endict lex, tangible.Func1Param<String, String> pfx2ns, EntityKind madeFor)
	{
		OwlName.Parts owlParts = new OwlName.Parts();
		cognipy.cnl.dl.DlName.Parts dlParts = dl.Split();
		if (dlParts.term == null)
		{
			owlParts.ns = pfx2ns.invoke(null); // OWLOntologyXMLNamespaceManager namespaceManager namespaceManager.getDefaultNamespace();
		}
		else
		{
			owlParts.ns = pfx2ns.invoke(dlParts.term); //namespaceManager.getNamespaceForPrefix(dlParts.term);
			if (owlParts.ns == null)
			{
				owlParts.ns = "http://ontorion.com/unknown.owl/" + dlParts.term + "#";
			}
		}
		if (!dlParts.quoted)
		{
			String name_part = dlParts.name;
			String sufix = "";
			if (madeFor == EntityKind.SWRLVariable)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var a = name_part.split("[_]", -1);
				if (a.Length == 2)
				{
					name_part = a.First();
					sufix = a.Last();
				}
			}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var pp = name_part.split("[-]", -1);
			StringBuilder sb = new StringBuilder();
			boolean first = true;
			String oldpp = "";
			boolean wereDigits = false;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var p : pp)
			{
				if (first)
				{
					first = false;
					if (madeFor == EntityKind.Role || madeFor == EntityKind.DataRole)
					{
						String nn = lex.toN_Simple(p, CNL.EN.endict.WordKind.PastParticiple);
						sb.append(nn);
					}
					else
					{
						sb.append((madeFor == EntityKind.Instance || (madeFor == EntityKind.SWRLVariable)) ? Character.toUpperCase(p[0]) : Character.toLowerCase(p[0]));
						sb.append(p.Substring(1));
					}
				}
				else
				{
					if (oldpp.length() > 1 && Character.isLetter(oldpp.charAt(0)) && Character.toUpperCase(oldpp.Last()) == oldpp.Last() && Character.isLetter(p[0]))
					{
						sb.append(exoticUTFForUnderscore);
					}
					if (Character.isDigit(p[0]))
					{
						if (wereDigits)
						{
							sb.append("-");
						}
						sb.append(p);
						wereDigits = true;
					}
					else
					{
						sb.append(Character.toUpperCase(p[0]));
						sb.append(p.Substring(1));
						wereDigits = false;
					}
				}
				oldpp = p;
			}
			owlParts.name = sb.toString() + (tangible.StringHelper.isNullOrEmpty(sufix) ? "" : "_" + sufix);
		}
		else
		{
			owlParts.name = dlParts.name;
		}

		owlParts.global = (madeFor == EntityKind.Instance && !dlParts.local);

		return owlParts.Combine();
	}
}