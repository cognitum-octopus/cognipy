package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class CNLFactory implements ICNLFactory
{
//C# TO JAVA CONVERTER TODO TASK: C# to Java Converter cannot determine whether this System.IO.Stream is input or output:
	private static Stream FindResourceString(String shortName)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		var name = (from x in System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceNames() where x.endsWith("." + shortName) select x).First();
		return System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceStream(name);
	}

	public static cognipy.cnl.en.endict lex = new cognipy.cnl.en.endict(FindResourceString("en.dict"));

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [ThreadStatic] static Tools.Parser enParser = null;
	private static tools.Parser enParser = null;

	public final tools.Lexer getLexer()
	{
		if (enParser == null)
		{
			enParser = new cognipy.cnl.en.endl(new yyendl(), new ErrorHandler(false));
		}
		return enParser.m_lexer;
	}
	public final tools.Parser getParser()
	{
		if (enParser == null)
		{
			enParser = new cognipy.cnl.en.endl(new yyendl(), new ErrorHandler(false));
		}
		return enParser;
	}

	public final boolean isEOL(TOKEN tok)
	{
		return (tok instanceof cognipy.cnl.en.END) || (tok instanceof cognipy.cnl.en.COMMENT);
	}

	public final boolean IsAnnot(TOKEN tok)
	{
		return (tok instanceof cognipy.cnl.en.COMMENT);
	}

	public final boolean isParagraph(SYMBOL smb)
	{
		return (smb instanceof cognipy.cnl.en.paragraph);
	}

	private tangible.Func1Param<String, String> pfx2Ns = null;
	public final void setPfx2NsSource(tangible.Func1Param<String, String> pfx2Ns)
	{
		this.pfx2Ns = (String arg) -> pfx2Ns.invoke(arg);
	}


	public final DL.Paragraph InvConvert(SYMBOL smb, boolean useFullUri)
	{
		return InvConvert(smb, useFullUri, null);
	}

	public final DL.Paragraph InvConvert(SYMBOL smb)
	{
		return InvConvert(smb, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public DL.Paragraph InvConvert(SYMBOL smb, bool useFullUri = false, Func<string, string> pfx2nsEx = null)
	public final DL.Paragraph InvConvert(SYMBOL smb, boolean useFullUri, tangible.Func1Param<String, String> pfx2nsEx)
	{
		cognipy.cnl.en.InvTransform trans = new cognipy.cnl.en.InvTransform();
		return trans.Convert(smb instanceof cognipy.cnl.en.paragraph ? (cognipy.cnl.en.paragraph)smb : null, useFullUri, (pfx2nsEx != null) ? pfx2nsEx : pfx2Ns);
	}


	public final Object Convert(DL.Statement stmast, boolean usePrefixes)
	{
		return Convert(stmast, usePrefixes, null);
	}

	public final Object Convert(DL.Statement stmast)
	{
		return Convert(stmast, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public object Convert(DL.Statement stmast, bool usePrefixes = false, Func<string, string> ns2pfx = null)
	public final Object Convert(DL.Statement stmast, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfx)
	{
		cognipy.cnl.en.Transform trans = new cognipy.cnl.en.Transform();
		return trans.Convert(stmast, usePrefixes, ns2pfx);
	}


	public final Object Convert(DL.Paragraph para, boolean usePrefixes)
	{
		return Convert(para, usePrefixes, null);
	}

	public final Object Convert(DL.Paragraph para)
	{
		return Convert(para, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public object Convert(DL.Paragraph para, bool usePrefixes = false, Func<string, string> ns2pfx = null)
	public final Object Convert(DL.Paragraph para, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfx)
	{
		cognipy.cnl.en.Transform trans = new cognipy.cnl.en.Transform();
		return trans.Convert(para, usePrefixes, ns2pfx);
	}



	public final Object Convert(DL.IAccept nodeast, boolean usePrefixes)
	{
		return Convert(nodeast, usePrefixes, null);
	}

	public final Object Convert(DL.IAccept nodeast)
	{
		return Convert(nodeast, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public object Convert(DL.IAccept nodeast, bool usePrefixes = false, Func<string, string> ns2pfx = null)
	public final Object Convert(DL.IAccept nodeast, boolean usePrefixes, tangible.Func1Param<String, String> ns2pfx)
	{
		cognipy.cnl.en.Transform trans = new cognipy.cnl.en.Transform();
		return trans.Convert(nodeast, usePrefixes, ns2pfx);
	}

	public final String Serialize(Object enast, boolean serializeAnnotations, tangible.OutObject<AnnotationManager> annotMan, boolean templateMode)
	{
		annotMan.argValue = new AnnotationManager();
		String ret = null;
		if (enast instanceof java.lang.Iterable<Object>)
		{
			StringBuilder retTT = new StringBuilder();
			for (java.lang.Iterable<Object> e : enast instanceof java.lang.Iterable<Object> ? (java.lang.Iterable<Object>)enast : null)
			{
				AnnotationManager localManager = new AnnotationManager();
				tangible.OutObject<cognipy.cnl.AnnotationManager> tempOut_localManager = new tangible.OutObject<cognipy.cnl.AnnotationManager>();
				retTT.append(Serialize(e, serializeAnnotations, tempOut_localManager, templateMode) + " ");
			localManager = tempOut_localManager.argValue;
				annotMan.argValue.appendAnnotations(localManager);
			}
			return retTT.toString().trim();
		}
		else
		{
			cognipy.cnl.en.Serializer2 ser = new cognipy.cnl.en.Serializer2();
			ser.setSerializeAnnotations(serializeAnnotations);
			ser.setTemplateMode(templateMode);
			if (enast instanceof cognipy.cnl.en.paragraph)
			{
				ret = ser.Serialize(enast instanceof cognipy.cnl.en.paragraph ? (cognipy.cnl.en.paragraph)enast : null);
			}
			else if (enast instanceof cognipy.cnl.en.sentence)
			{
				ret = ser.Serialize(enast instanceof cognipy.cnl.en.sentence ? (cognipy.cnl.en.sentence)enast : null);
			}
			else if (enast instanceof cognipy.cnl.en.orloop)
			{
				ret = ser.Serialize(enast instanceof cognipy.cnl.en.orloop ? (cognipy.cnl.en.orloop)enast : null);
			}
			else if (enast instanceof cognipy.cnl.en.boundFacets)
			{
				ret = ser.Serialize(enast instanceof cognipy.cnl.en.boundFacets ? (cognipy.cnl.en.boundFacets)enast : null);
			}
			else if (enast instanceof cognipy.cnl.en.boundTop)
			{
				ret = ser.Serialize(enast instanceof cognipy.cnl.en.boundTop ? (cognipy.cnl.en.boundTop)enast : null);
			}
			else if (enast instanceof cognipy.cnl.en.boundTotal)
			{
				ret = ser.Serialize(enast instanceof cognipy.cnl.en.boundTotal ? (cognipy.cnl.en.boundTotal)enast : null);
			}
			else
			{
				throw new UnsupportedOperationException("Could not serialize. Not implemented.");
			}

			annotMan.argValue = ser.annotMan;

			return ret;
		}
	}

	private static String FromDL(String name, endict.WordKind kind, boolean bigName)
	{
		cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
		tempVar.id = name;
		return ENNameingConvention.FromDL(tempVar, kind, bigName).id;
	}
	public final java.lang.Iterable<String> Morphology(java.lang.Iterable<String> col, String str, String form, boolean bigName)
	{
		if (form.equals("NormalForm"))
		{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return from r in col where FromDL(r, endict.WordKind.NormalForm, false).startsWith(str) select FromDL(r, endict.WordKind.NormalForm, bigName);
		}
		else
		{
			cognipy.cnl.en.endict.WordKind k = cognipy.cnl.en.endict.WordKind.PastParticiple;
			if (form.equals("SimplePast"))
			{
				k = cognipy.cnl.en.endict.WordKind.SimplePast;
			}
			else if (form.equals("PluralFormNoun"))
			{
				k = cognipy.cnl.en.endict.WordKind.PluralFormNoun;
			}
			else if (form.equals("PluralFormVerb"))
			{
				k = cognipy.cnl.en.endict.WordKind.PluralFormVerb;
			}
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			java.lang.Iterable<String> q = from r in col where FromDL(r, k, bigName).startsWith(str) select FromDL(r, k, bigName);
			return q;
		}
	}

	public final String[] GetAllKeywords()
	{
		return KeyWords.Me.GetAllKeywords();
	}

	public final boolean KeywordTagExists(String kw)
	{
		return KeyWords.Me.keywordExist(kw);
	}

	public final boolean IsKeyword(String kw)
	{
		return KeyWords.Me.isKeyword(kw) || kw.equals("x") || kw.equals("y") || kw.equals("z") || kw.equals("X") || kw.equals("Y") || kw.equals("Z");
	}

	public final String GetKeyword(String kw)
	{
		return KeyWords.Me.Get(kw);
	}

	public final HashSet<String> GetAllMatchingKeywords(String kw)
	{
		return KeyWords.Me.GetAll(kw);
	}

	public final String GetKeywordTag(String wrd)
	{
		return KeyWords.Me.GetTag(wrd);
	}

	public final void FindMark(SYMBOL smb, String mark, tangible.OutObject<String> kind, tangible.OutObject<String> form)
	{
		cognipy.cnl.en.InvTransform trans = new InvTransform(mark);
		trans.Convert(smb instanceof cognipy.cnl.en.paragraph ? (cognipy.cnl.en.paragraph)smb : null);
		cognipy.cnl.en.InvTransform.EntityKind ckind = trans.GetMarkerKind();
		cognipy.cnl.en.endict.WordKind cform = trans.GetMarkerForm();
		switch (ckind)
		{
			case Concept:
				kind.argValue = "concept";
				break;
			case AnyRole:
				kind.argValue = "role";
				break;
			case DataRole:
				kind.argValue = "datarole";
				break;
			case DataType:
				kind.argValue = "datatype";
				break;
			case Instance:
				kind.argValue = "instance";
				break;
			default:
				throw new IllegalStateException();
		}
		form.argValue = cform == endict.WordKind.NormalForm ? "NormalForm" : (cform == endict.WordKind.PastParticiple ? "PastParticiple" : (cform == endict.WordKind.SimplePast ? "SimplePast" : (cform == endict.WordKind.PluralFormNoun ? "PluralFormNoun" : "PluralFormVerb")));
	}

	public final String GetDefaultTagValue(String prop)
	{
		String pfx = "";
		if (KeywordTagExists(prop))
		{
			pfx = GetKeyword(prop);
		}
		else if (prop.equals("NAME"))
		{
			pfx = "name";
		}
		else if (prop.equals("BIGNAME"))
		{
			pfx = "Bigname";
		}
		else if (prop.equals("VERYBIGNAME"))
		{
			pfx = "BIGNAME";
		}
		else if (prop.equals("DBL"))
		{
			pfx = "3.14";
		}
		else if (prop.equals("DBL"))
		{
			pfx = "3.14";
		}
		else if (prop.equals("DTM"))
		{
			pfx = "2012-02-16";
		}
		else if (prop.equals("DUR"))
		{
			pfx = "P1234DT12H35M30.234S";
		}
		else if (prop.equals("NAT"))
		{
			pfx = "1";
		}
		else if (prop.equals("WORDNUM"))
		{
			pfx = "one";
		}
		else if (prop.equals("BOL"))
		{
			pfx = "true";
		}
		else if (prop.equals("NUM"))
		{
			pfx = "-1";
		}
		else if (prop.equals("STR"))
		{
			pfx = "\'...\'";
		}
		else if (prop.equals("COMMA"))
		{
			pfx = ",";
		}
		else if (prop.equals("END"))
		{
			pfx = ".";
		}
		else if (prop.equals("CMP"))
		{
			pfx = "<=";
		}
		else if (prop.equals("EQ"))
		{
			pfx = "=";
		}
		else if (prop.equals("COMMENT"))
		{
			pfx = "-";
		}
		else if (prop.equals("CODE"))
		{
			pfx = "<? ?>";
		}
		else if (!prop.equals("EOF"))
		{
			assert false;
		}
		return pfx;
	}

	public final boolean TagIsName(String prop)
	{
		return prop.equals("NAME");
	}

	public final boolean TagIsDatatype(String prop)
	{
		return prop.equals("DBL") || prop.equals("NAT") || prop.equals("NUM") || prop.equals("STR") || prop.equals("DTM") || prop.equals("DUR");
	}

	public final String[] GetTagSuffixes()
	{
		return new String[] {"", "by"};
	}

	public final boolean TagIsInstanceName(String prop)
	{
		return prop.equals("BIGNAME") || prop.equals("VERYBIGNAME");
	}

	public final String GetSymbol(String prop)
	{
		if (prop.equals("BIGNAME"))
		{
			return "Bigname";
		}
		else if (prop.equals("VERYBIGNAME"))
		{
			return "BIGNAME";
		}
		else if (prop.equals("DBL"))
		{
			return "floating point number IEEE-754";
		}
		else if (prop.equals("NAT"))
		{
			return "natural number";
		}
		else if (prop.equals("WORDNUM"))
		{
			return "name of natural number";
		}
		else if (prop.equals("BOL"))
		{
			return "true or false";
		}
		else if (prop.equals("NUM"))
		{
			return "integer";
		}
		else if (prop.equals("STR"))
		{
			return "string";
		}
		else if (prop.equals("DTM"))
		{
			return "dateTime ISO-8601";
		}
		else if (prop.equals("DUR"))
		{
			return "duration ISO-8601";
		}
		else if (prop.equals("COMMA"))
		{
			return "Comma";
		}
		else if (prop.equals("COMMENT"))
		{
			return "Comment";
		}
		else if (prop.equals("CMP"))
		{
			return "Comparator";
		}
		else if (prop.equals("END"))
		{
			return "FullStop";
		}
		else if (KeywordTagExists(prop))
		{
			return "Keyword";
		}
		else
		{
			return null;
		}
	}

	public final String GetEOLTag()
	{
		return "END";
	}

	public final String GetKeywordTip(String kwtag)
	{
		if (kwtag.equals("EVERY"))
		{
			return "Every cat is an animal.\nEvery chair should have four legs.";
		}
		else if (kwtag.equals("EVERYTHING"))
		{
			return "Every-single-thing that has a cat is a cat-owner.";
		}
		else
		{
			return null;
		}
	}

	public final String GetTooltipDesc(Map.Entry<String, String> kv)
	{
		switch (kv.getKey())
		{
			case "role":
				if (kv.getValue().equals("PastParticiple"))
				{
					return "verb in past-participle (e.g.:loves, is-part-of)";
				}
				else if (kv.getValue().equals("PluralFormVerb"))
				{
					return "verb in  in plural-form (e.g.:love, are-part-of)";
				}
				else if (kv.getValue().equals("SimplePast"))
				{
					return "verb in simple-past  (e.g.:loved, had)";
				}
				else //"NormalForm"
				{
					return "verb in present-simple (e.g.: love, be-part-of)";
				}
			case "concept":
				if (kv.getValue().equals("PluralFormNoun"))
				{
					return "noun in plural-form (e.g.: cats, girls, big-ships, young-women)";
				}
				else //"NormalForm"
				{
					return "noun in singular-form (e.g.: cat, girl, big-ship, young-woman)";
				}
			case "BIGNAME":
				return "Globally avaliable proper name (e.g.: CERN, POLAND, EURO)";
			case "Bigname":
				return "Proper name (e.g.: Mary, John-Smith, Great-Canyon)";
			case "Comparator":
				return "Comparator (e.g.: =, <>, <, >, <=, >=)";
			case "FullStop":
				return "full stop sign (it is .)";
			case "Comma":
				return "comma sign (it is ,)";
			case "floating point number IEEE-754":
				return "floating point number (e.g.: 0.1, 3.14, 31.4e-1)";
			case "dateTime ISO-8601":
				return "date and time (e.g.: 2001-10-26, 2001-10-26T21:32:52.321)";
			case "duration ISO-8601":
				return "duration (e.g.: P3DT5H20M30.123S)";
			case "natural number":
				return "natural number (e.g.: 0,1,2,3)";
			case "name of natural number":
				return "name of natural number (e.g.: one, two,...)";
			case "true or false":
				return "true or false";
			case "integer":
				return "integer (e.g.:-3,-2,-1,0,1,2,3)";
			case "string":
				return "string (e.g.: 'a long time ago', 'the one I know', '爱')";
			case "Keyword":
				return "keyword";
			default:
				return "";
		}
	}

	public final boolean LoadSmallestSentenceCache(HashMap<String, String> cache)
	{
		InputStream stream = FindResourceString("ssc");
		try (InputStreamReader str = new InputStreamReader(stream))
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var ver = str.ReadLine();

			while (true)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var k = str.ReadLine();
				if (k == null)
				{
					return true;
				}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var v = str.ReadLine();
				if (v == null)
				{
					return true;
				}
				if (tangible.StringHelper.isNullOrEmpty(v))
				{
					v = null;
				}
				cache.put(k, v);
			}
		}
	}

	public final void SaveSmallestSentenceCache(HashMap<String, String> cache)
	{
		String pth = Paths.get((new File(Assembly.GetExecutingAssembly().Location)).getParent()).resolve("..\\..\\..\\..\\..\\Common\\CNL\\ssc").toString();
		FileOutputStream stream = new FileOutputStream(pth);
		try (OutputStreamWriter str = new OutputStreamWriter(stream))
		{
			Assembly assembly = Assembly.GetExecutingAssembly();
			String version = assembly.FullName.split("[,]", -1)[1];
			String fullversion = version.split("[=]", -1)[1];

			str.write(fullversion + System.lineSeparator());
			for (Map.Entry<String, String> kv : cache.entrySet())
			{
				str.write(String.valueOf(kv.getKey()) + System.lineSeparator());
				str.write(String.valueOf(kv.getValue()) + System.lineSeparator());
			}
		}
	}

	private boolean valcond(CNL.EN.condition cnd)
	{
		if (cnd instanceof condition_exists)
		{
			cognipy.cnl.en.objectr o = (cnd instanceof condition_exists ? (condition_exists)cnd : null).objectA;
			if (o instanceof objectr_io)
			{
				if ((o instanceof objectr_io ? (objectr_io)o : null).identobject instanceof identobject_inst)
				{
					return false;
				}
			}
		}
		return true;
	}

	private boolean validateSingleStmt(CNL.EN.sentence stmt)
	{
		if (stmt instanceof CNL.EN.swrlrule)
		{
			for (condition cnd : (stmt instanceof CNL.EN.swrlrule ? (CNL.EN.swrlrule)stmt : null).Predicate.Conditions)
			{
				if (!valcond(cnd))
				{
					return false;
				}
			}
		}
		if (stmt instanceof CNL.EN.exerule)
		{
			for (condition cnd : (stmt instanceof CNL.EN.exerule ? (CNL.EN.exerule)stmt : null).slp.Conditions)
			{
				if (!valcond(cnd))
				{
					return false;
				}
			}
		}
		return true;
	}

	public final boolean ValidateSafeness(Object ast)
	{
		if (ast instanceof CNL.EN.paragraph)
		{
			for (sentence stmt : (ast instanceof CNL.EN.paragraph ? (CNL.EN.paragraph)ast : null).sentences)
			{
				if (!validateSingleStmt(stmt))
				{
					return false;
				}
			}
		}
		else if (ast instanceof CNL.EN.sentence)
		{
			return validateSingleStmt(ast instanceof CNL.EN.sentence ? (CNL.EN.sentence)ast : null);
		}
		return true;
	}
}