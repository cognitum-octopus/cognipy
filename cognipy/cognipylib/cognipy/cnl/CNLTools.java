package cognipy.cnl;

import tools.*;
import cognipy.*;
import java.util.*;
import java.nio.file.*;

public class CNLTools
{
	private static HashMap<String, java.lang.Class> factories = new HashMap<String, java.lang.Class>();

	public static void RegisterCNLFactory(String name, java.lang.Class factory)
	{
		synchronized (factories)
		{
			name = name.toLowerCase(Locale.ROOT);
			factories.put(name, factory);
		}
	}

	public String currentLang = "";


	public CNLTools(String languageSymbol)
	{
		this(languageSymbol, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CNLTools(string languageSymbol, Func<string, string> pfx2Ns = null)
	public CNLTools(String languageSymbol, tangible.Func1Param<String, String> pfx2Ns)
	{
		java.lang.Class t_factory = null;
		languageSymbol = languageSymbol.toLowerCase(Locale.ROOT);
		synchronized (factories)
		{
			if (!factories.containsKey(languageSymbol))
			{
				throw new IllegalArgumentException(String.format("CNL not loaded for given language: %1$s", languageSymbol));
			}
			else
			{
				t_factory = factories.get(languageSymbol);
			}
		}
		java.lang.reflect.Constructor cnstr = t_factory.getConstructor(new java.lang.Class[] { });
		Object tempVar = cnstr.newInstance(new Object[] { });
		factory = tempVar instanceof ICNLFactory ? (ICNLFactory)tempVar : null;
		factory.setPfx2NsSource(pfx2Ns);
		currentLang = languageSymbol;
	}

	public static boolean isSurelyDLEntity(String entity, cognipy.ars.EntityKind entKind)
	{
		if (tangible.StringHelper.isNullOrWhiteSpace(entity))
		{
			return false;
		}

		// if it starts with _ or {" it is an instance and it is already in DL!
		if (entKind == cognipy.ars.EntityKind.Instance && (entity.startsWith("_") || entity.startsWith("{\"")))
		{
			return true;
		}

		cognipy.cnl.dl.DlName nn = new cognipy.cnl.dl.DlName();
		nn.id = entity;
		cognipy.cnl.dl.DlName.Parts sp = nn.Split();
		return !tangible.StringHelper.isNullOrWhiteSpace(sp.term);
	}

	public final String GetNamespaceFromNamespaceLine(String input)
	{
		int dp = input.indexOf(':');
		String x = input.substring(0, dp);
		String ontologyIri = input.substring(dp + 1).trim();
		if (ontologyIri.endsWith("."))
		{
			ontologyIri = ontologyIri.substring(0, ontologyIri.length() - 1);
		}
		if (ontologyIri.startsWith("\'") && ontologyIri.length() > 2)
		{
			ontologyIri = ontologyIri.substring(1, 1 + ontologyIri.length() - 2).replace("\'\'", "\'");
		}
		ontologyIri = ontologyIri.replace(" ", "");
		ontologyIri = ontologyIri.replace("\\", "/");
		if (Paths.get(ontologyIri).getParent() == null)
		{
			ontologyIri = "file:" + ontologyIri;
		}

		if (!ontologyIri.endsWith("/") && !ontologyIri.endsWith("#") && !ontologyIri.contains("#"))
		{
			ontologyIri += "#";
		}
		return ontologyIri;
	}

	public static String GetCanonicalNs(String ns)
	{
		if (!tangible.StringHelper.isNullOrWhiteSpace(ns) && !ns.endsWith("/") && !ns.endsWith("#") && !ns.contains("#"))
		{
			return ns + "#";
		}
		else
		{
			return ns;
		}
	}

	/** 
	 Checks if the two namespaces are different
	 NB: http://aaa/ and http://aaa# are the same namespace!
	 
	 @param ns1
	 @param ns2
	 @return 
	*/
	public static boolean AreNamespacesEqual(String ns1, String ns2)
	{
		if (tangible.StringHelper.isNullOrWhiteSpace(ns1) || tangible.StringHelper.isNullOrWhiteSpace(ns2))
		{
			return false;
		}

		if (ns1.equals(ns2))
		{
			return true;
		}

		// trying to understand here if the two namespaces differ only for the last character.
		if (ns1.endsWith("#") || ns1.endsWith("/"))
		{
			ns1 = tangible.StringHelper.remove(ns1, ns1.length() - 1);
		}
		if (ns2.endsWith("#") || ns2.endsWith("/"))
		{
			ns2 = tangible.StringHelper.remove(ns2, ns2.length() - 1);
		}
		if (ns1.equals(ns2))
		{
			return true;
		}

		return false;
	}


	public static String DLToFullUri(String entity, ARS.EntityKind entKind, Func<String, String> pfx2ns)
	{
		return DLToFullUri(entity, entKind, pfx2ns, null);
	}

	public static String DLToFullUri(String entity, ARS.EntityKind entKind)
	{
		return DLToFullUri(entity, entKind, null, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public static string DLToFullUri(string entity, ARS.EntityKind entKind, Func<string, string> pfx2ns = null, string defaultNs = null)
	public static String DLToFullUri(String entity, ARS.EntityKind entKind, tangible.Func1Param<String, String> pfx2ns, String defaultNs)
	{
		if (pfx2ns == null)
		{
			return entity;
		}

		if (entKind == ARS.EntityKind.Statement)
		{
			return entity;
		}
		else
		{
			cognipy.cnl.dl.DlName dlName = new cognipy.cnl.dl.DlName();
			dlName.id = entity;
			cognipy.cnl.dl.DlName.Parts allParts = dlName.Split();
			if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && !allParts.term.startsWith("<") && !allParts.term.endsWith(">"))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var tterm = pfx2ns.invoke(allParts.term);
				if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
				{
					allParts.term = "<" + tterm + ">";
				}
				else
				{
					throw new RuntimeException("No namespace found for prefix " + allParts.term + ". You need to define it before saving into Ontorion.");
				}
			}
			else if (tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && !tangible.StringHelper.isNullOrWhiteSpace(defaultNs)) // if a default namespace is given, add it to the entity if it does not have a namespace associated
			{
				allParts.term = "<" + defaultNs + ">";
			}
			else if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && allParts.term.startsWith("<") && allParts.term.endsWith(">"))
			{
				String tterm = CNLTools.GetCanonicalNs(allParts.term.substring(1, 1 + allParts.term.length() - 2)); // string without < and >
				allParts.term = "<" + tterm + ">";
			}
			dlName = allParts.Combine();

			return dlName.id;
		}
	}

	public final void setPfx2NsSource(tangible.Func1Param<String, String> pfx2Ns)
	{
		factory.setPfx2NsSource(pfx2Ns);
	}

	private ICNLFactory factory = null;

	public static MatchCollection ParseReferences(String str)
	{
		return CacheLine.refsRg.Matches(str);
	}

	private static String GetPathSafeString(String path)
	{
		String invalid = new String(Path.GetInvalidPathChars()) + "*";

		for (char c : invalid)
		{
			path = path.replace(String.valueOf(c), "");
		}

		return path;
	}

	public static void GetReferencePieces(Match match, tangible.OutObject<String> pfx, tangible.OutObject<String> onto, tangible.OutObject<String> ns)
	{
		System.Text.RegularExpressions.Group pfxT = match.Groups["pfx"];
		System.Text.RegularExpressions.Group nsT = match.Groups["ns"];
		System.Text.RegularExpressions.Group ontT = match.Groups["ont"];

		// we have to be sure that onto is path safe. Otherwise, everyWhere we use Path.GetFullPath(Path.Combine(....,onto)) will return an exception!
		onto.argValue = pfx.argValue = ns.argValue = null;
		if (ontT != null)
		{
			onto.argValue = GetPathSafeString(ontT.Value);
			if (onto.argValue.trim().startsWith("\'") && onto.argValue.length() > 2)
			{
				onto.argValue = onto.argValue.trim().substring(1, 1 + onto.argValue.length() - 2).replace("\'", "");
			}
		}
		pfx.argValue = match.Groups["pfx"].Value;
		ns.argValue = match.Groups["ns"].Value.trim();
		if (ns.argValue.trim().startsWith("\'") && ns.argValue.length() > 2)
		{
			ns.argValue = ns.argValue.trim().substring(1, 1 + ns.argValue.length() - 2).replace("\'", "").trim();
		}
		if (!tangible.StringHelper.isNullOrEmpty(ns.argValue))
		{
			ns.argValue = Regex.Replace(ns.argValue, "\\r", "");
			ns.argValue = Regex.Replace(ns.argValue, "\\n", "");
			ns.argValue = Regex.Replace(ns.argValue, "'", "");
			ns.argValue = cognipy.cnl.CNLTools.GetCanonicalNs(ns.argValue);
		}
	}

	public static class CacheLine
	{
		public final int getStart()
		{
			return val.start;
		}

		public final String getLine()
		{
			return val.getLine();
		}
		public final void setLine(String value)
		{
			val = val.Clone();
			val.setLine(value);
			isAnnot = null;
			isRef = null;
			isRule = null;
			isNs = null;
		}

		public final String getWsBefore()
		{
			return val.wsBefore;
		}

		public final String getText()
		{
			return val.text;
		}

		public final String getWsAfter()
		{
			return val.wsAfter;
		}



		public CacheLine(InternalCacheLine val)
		{
			this.val = val;
		}
		private InternalCacheLine val;
		private Boolean isAnnot = null;
		private Boolean isRule = null;
		private Boolean isRef = null;
		private Boolean isNs = null;


		private static String UPPERL = "[A-Z]";
		private static String LOWERL = "[a-z]";
		private static String LETTER = "(" + UPPERL + "|" + LOWERL + ")";
		private static String DIGIT = "[0-9]";
		private static String BIGNAME = UPPERL + LOWERL + "*(\\-(" + UPPERL + LOWERL + "*|" + DIGIT + "+))*";
		private static String NAME = LOWERL + "+(\\-(" + LOWERL + "+|" + DIGIT + "+))*";
		private static String STRING = "('([^']|''|'[^?=@])+')";
		private static String BSTRING = "(\"([^\"]|\"\")+\")";
		private static String ANNOT = "^(?<item>" + BIGNAME + "):([^|']|(\\.\\.)|" + STRING + "|" + BSTRING + ")+\\." + "$";
		private static Regex annot = new Regex(ANNOT, RegexOptions.Compiled);

		// regex to extract the references. Expecting something like : [prefix] namespace (ontology_location)

		public static Regex refsRg = new Regex("^([A-Z]{1}[a-z]+?:)?\\s*\\[(?<pfx>[^\\]]*)\\]\\s*(?<ns>[^\\(|\\[]*)(\\((?<ont>[^\\|\\[]*)\\))?\\s*($|(?<dot>\\.))", RegexOptions.Compiled.getValue() | RegexOptions.Multiline.getValue());

		private ArrayList<String> _pfx;

		public final ArrayList<String> getPfx()
		{
			if (getIsReference())
			{
				return _pfx;
			}
			else
			{
				return null;
			}
		}

		private ArrayList<String> _ont;

		public final ArrayList<String> getOnt()
		{
			if (getIsReference())
			{
				return _ont;
			}
			else
			{
				return null;
			}
		}

		private ArrayList<String> _ns;

		public final ArrayList<String> getNs()
		{
			if (getIsReference())
			{
				return _ns;
			}
			else
			{
				return null;
			}
		}

		// returns true if the cacheline is an annotation
		public final boolean getIsAnnotation()
		{
			if (isAnnot != null)
			{
				return isAnnot.booleanValue();
			}
			else
			{
				isAnnot = annot.IsMatch(val.text.trim());
				return isAnnot.booleanValue();
			}
		}

		// returns true if the cacheline is a rule code
		public final boolean getIsRuleCode()
		{
			if (isRule != null)
			{
				return isRule.booleanValue();
			}
			else
			{
				isRule = tangible.StringHelper.trimEnd(val.text).endsWith("?>.");
				return isRule.booleanValue();
			}
		}

		public final boolean getIsNamespace()
		{
			if (isNs == null && !this.getIsAnnotation())
			{
				isNs = false;
			}
			else if (isNs == null && this.getIsAnnotation())
			{
				int dp = val.getLine().indexOf(':');
				String x = val.getLine().substring(0, dp);
				isNs = (x.trim().equals("Namespace"));
			}
			return isNs.booleanValue();
		}

		// returns true if the cache line is a Reference (annotation + "Reference:...")
		public final boolean getIsReference()
		{
			if (isRef == null && !this.getIsAnnotation())
			{
				isRef = false;
			}
			else if (isRef == null && this.getIsAnnotation())
			{
				int dp = val.getLine().indexOf(':');
				String x = val.getLine().substring(0, dp);
				if (x.trim().equals("References"))
				{
					isRef = true;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var refs = refsRg.Matches(val.getLine().substring(dp));
					if (refs.Count > 0)
					{
						_ont = new ArrayList<String>();
						_pfx = new ArrayList<String>();
						_ns = new ArrayList<String>();
					}

					for (Match match : refs)
					{
						String onto, pfx, ns;
						tangible.OutObject<String> tempOut_pfx = new tangible.OutObject<String>();
						tangible.OutObject<String> tempOut_onto = new tangible.OutObject<String>();
						tangible.OutObject<String> tempOut_ns = new tangible.OutObject<String>();
						CNLTools.GetReferencePieces(match, tempOut_pfx, tempOut_onto, tempOut_ns);
					ns = tempOut_ns.argValue;
					onto = tempOut_onto.argValue;
					pfx = tempOut_pfx.argValue;
						_ont.add(onto);
						_pfx.add(pfx);
						_ns.add(ns);
					}
				}
				else
				{
					isRef = false;
				}
			}
			return isRef.booleanValue();
		}

		public static boolean CheckIfAnnotation(String txt)
		{
			return annot.IsMatch(txt);
		}

		public static boolean CheckIfAnnotation(String txt, tangible.OutObject<String> annotationType)
		{
			boolean mth = annot.Match(txt);
			if (mth.Success)
			{
				annotationType.argValue = mth.Groups["item"].Value;
			}
			else
			{
				annotationType.argValue = null;
			}

			return mth.Success;
		}
	}

	public static class InternalCacheLine
	{
		public int start;

		public final String getLine()
		{
			return wsBefore + text + wsAfter;
		}
		public final void setLine(String value)
		{
			tangible.OutObject<String> tempOut_wsBefore = new tangible.OutObject<String>();
			tangible.OutObject<String> tempOut_text = new tangible.OutObject<String>();
			tangible.OutObject<String> tempOut_wsAfter = new tangible.OutObject<String>();
			parseFormating(value, tempOut_wsBefore, tempOut_text, tempOut_wsAfter);
		wsAfter = tempOut_wsAfter.argValue;
		text = tempOut_text.argValue;
		wsBefore = tempOut_wsBefore.argValue;
		}

		public String wsBefore;
		public String text;
		public String wsAfter;



		public final InternalCacheLine Clone()
		{
			InternalCacheLine l = new InternalCacheLine();
			l.setLine(this.getLine());
			return l;
		}
	}

	private Object cacheLinesGuard = new Object();
	private ArrayList<InternalCacheLine> cacheLines = null;
	private String cachedTxt = null;

	private static void parseFormating(String str, tangible.OutObject<String> wsBefore, tangible.OutObject<String> text, tangible.OutObject<String> wsAfter)
	{
		text.argValue = tangible.StringHelper.trimStart(str);
		if (text.argValue.length() == 0)
		{
			wsAfter.argValue = "";
			wsBefore.argValue = "";
			return;
		}
		wsBefore.argValue = str.substring(0, str.length() - text.argValue.length());
		text.argValue = tangible.StringHelper.trimEnd(text.argValue);
		if (text.argValue.length() == 0)
		{
			wsAfter.argValue = "";
			return;
		}
		wsAfter.argValue = str.substring(text.argValue.length() + wsBefore.argValue.length());
	}



	public final java.lang.Iterable<String> SplitDLIntoLines(String txt)
	{
		cognipy.cnl.dl.Paragraph dlast = GetDLAst(txt, true);
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(false);

		for (Statement stmt : dlast.Statements)
		{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return ser.Serialize(stmt);
		}
	}

	public final java.lang.Iterable<String> SplitENIntoLines(String txt)
	{
		tools.Lexer lex = factory.getLexer();
		lex.Start(txt);
		int lastIdx = 0;
		int newIdx = 0;
		while (true)
		{
			while (true)
			{
				tools.TOKEN tok = lex.Next();
				if (tok == null)
				{
					if (lex.PeekChar() == 0)
					{
						if (lastIdx != newIdx)
						{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
							yield return txt.substring(lastIdx, newIdx);
						}
						else
						{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
							yield return txt.substring(lastIdx);
						}

//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
						yield break;
					}
					else
					{
						lex.GetChar();
						continue;
					}
				}

				if (factory.isEOL(tok))
				{
					newIdx = tok.pos + tok.getYytext().length();
					break;
				}
			}

//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return txt.substring(lastIdx, newIdx);
			lastIdx = newIdx;
		}
	}


	public final void LoadCache(String txt)
	{
		synchronized (cacheLinesGuard)
		{
			ArrayList<TOKEN> trace = new ArrayList<TOKEN>();
			if (cacheLines == null || !cachedTxt.equals(txt))
			{
				cachedTxt = txt;
				cacheLines = new ArrayList<InternalCacheLine>();
				if (factory == null)
				{
					return;
				}
				tools.Lexer lex = factory.getLexer();
				lex.Start(txt);
				int lastIdx = 0;
				int newIdx = 0;
				while (true)
				{
					while (true)
					{
						tools.TOKEN tok = lex.Next();
						trace.add(tok);
						if (tok == null)
						{
							if (lex.PeekChar() == 0)
							{
								if (lastIdx != newIdx)
								{
									InternalCacheLine tempVar = new InternalCacheLine();
									tempVar.start = lastIdx;
									tempVar.setline(txt.substring(lastIdx, newIdx));
									cacheLines.add(tempVar);
								}
								else
								{
									InternalCacheLine tempVar2 = new InternalCacheLine();
									tempVar2.start = lastIdx;
									tempVar2.setline(txt.substring(lastIdx));
									cacheLines.add(tempVar2);
								}

								return;
							}
							else
							{
								lex.GetChar();
								continue;
							}
						}

						if (factory.isEOL(tok))
						{
							newIdx = tok.pos + tok.getYytext().length();
							break;
						}
					}

					InternalCacheLine tempVar3 = new InternalCacheLine();
					tempVar3.start = lastIdx;
					tempVar3.setline(txt.substring(lastIdx, newIdx));
					cacheLines.add(tempVar3);
					lastIdx = newIdx;
				}
			}
		}
	}

	public final java.lang.Iterable<CacheLine> splitSentences(String txt)
	{
		synchronized (cacheLinesGuard)
		{
			LoadCache(txt);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return (from l in cacheLines select new CacheLine(l)).ToList();
		}
	}

	public final java.lang.Iterable<CacheLine> splitSentences(String txt, tangible.RefObject<Integer> start, tangible.RefObject<Integer> end)
	{
		synchronized (cacheLinesGuard)
		{
			LoadCache(txt);
			GetOverlappingFragment(txt, start, end);
			int strt = start.argValue, ed = end.argValue;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return (from l in cacheLines where l.start >= strt && l.start < ed select new CacheLine(l)).ToList();
		}
	}

	private static class GetOverlappingFragmentComparer implements Comparator<InternalCacheLine>
	{
		public final int compare(InternalCacheLine x, InternalCacheLine y)
		{
			return (new Integer(x.start)).compareTo(y.start);
		}
	}

	public final void GetOverlappingFragment(String txt, tangible.RefObject<Integer> start, tangible.RefObject<Integer> end)
	{
		synchronized (cacheLinesGuard)
		{
			LoadCache(txt);
			InternalCacheLine tempVar = new InternalCacheLine();
			tempVar.setline("");
			tempVar.start = start.argValue;
			int idx = cacheLines.BinarySearch(tempVar, new GetOverlappingFragmentComparer());
			if (idx >= 0)
			{
				start.argValue = cacheLines.get(idx).start;
			}
			else
			{
				idx = ~idx;
				if (idx < cacheLines.size())
				{
					if (idx > 0)
					{
						start.argValue = cacheLines.get(idx - 1).start;
					}
					else
					{
						start.argValue = 0;
					}
				}
				else
				{
					if (!cacheLines.isEmpty())
					{
						start.argValue = cacheLines.get(cacheLines.size() - 1).start;
					}
					else
					{
						start.argValue = 0;
					}
				}
			}

			InternalCacheLine tempVar2 = new InternalCacheLine();
			tempVar2.setline("");
			tempVar2.start = end.argValue;
			idx = cacheLines.BinarySearch(tempVar2, new GetOverlappingFragmentComparer());
			if (idx >= 0)
			{
				end.argValue = cacheLines.get(idx).start + cacheLines.get(idx).getLine().length();
			}
			else
			{
				idx = ~idx;
				if (idx < cacheLines.size())
				{
					if (idx > 0)
					{
						end.argValue = cacheLines.get(idx - 1).start + cacheLines.get(idx - 1).getLine().length();
					}
					else
					{
						end.argValue = 0;
					}
				}
				else
				{
					end.argValue = txt.length() - 1;
				}
			}
		}
	}


	public final cognipy.cnl.dl.Paragraph GetENDNL2DLForRoleBody(String text, tangible.OutObject<String> pattern, boolean throwOnError, boolean useFullUri)
	{
		return GetENDNL2DLForRoleBody(text, pattern, throwOnError, useFullUri, null);
	}

	public final cognipy.cnl.dl.Paragraph GetENDNL2DLForRoleBody(String text, tangible.OutObject<String> pattern, boolean throwOnError)
	{
		return GetENDNL2DLForRoleBody(text, pattern, throwOnError, false, null);
	}

	public final cognipy.cnl.dl.Paragraph GetENDNL2DLForRoleBody(String text, tangible.OutObject<String> pattern)
	{
		return GetENDNL2DLForRoleBody(text, pattern, true, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Paragraph GetENDNL2DLForRoleBody(string text, out string pattern, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2Ns = null)
	public final cognipy.cnl.dl.Paragraph GetENDNL2DLForRoleBody(String text, tangible.OutObject<String> pattern, boolean throwOnError, boolean useFullUri, tangible.Func1Param<String, String> pfx2Ns)
	{
		pattern.argValue = null;

		if (text.trim().equals(""))
		{
			return null;
		}
		String PRE = "If ";
		String POST = " then for the loooooked-for execute <? ?>.\r\n";
		tools.SYMBOL smb = factory.getParser().Parse(PRE + text + POST);
		if (!factory.isParagraph(smb)) // get null on syntax error
		{
			if (smb instanceof tools.error)
			{
				if (throwOnError)
				{
					throw new ParseException((smb instanceof tools.error ? (tools.error)smb : null).toString(), (smb instanceof tools.error ? (tools.error)smb : null).getLine(), (smb instanceof tools.error ? (tools.error)smb : null).getPosition(), (smb instanceof tools.error ? (tools.error)smb : null).pos, text);
				}
			}
			return null;
		}
		else
		{
			if (!factory.ValidateSafeness(smb))
			{
				if (throwOnError)
				{
					throw new IllegalStateException();
				}
				else
				{
					return null;
				}
			}
			pattern.argValue = this.GetENFromAstSentence(smb, false, true);
			String TF = "then for";
			int thenForPos = pattern.argValue.lastIndexOf(TF);
			pattern.argValue = pattern.argValue.substring(0, thenForPos - 1);
			pattern.argValue = pattern.argValue.substring(PRE.length());
			return factory.InvConvert(smb, useFullUri, pfx2Ns);
		}
	}


	public final cognipy.cnl.dl.Node GetEN2DLNode(String text, boolean throwOnError, boolean useFullUri)
	{
		return GetEN2DLNode(text, throwOnError, useFullUri, null);
	}

	public final cognipy.cnl.dl.Node GetEN2DLNode(String text, boolean throwOnError)
	{
		return GetEN2DLNode(text, throwOnError, false, null);
	}

	public final cognipy.cnl.dl.Node GetEN2DLNode(String text)
	{
		return GetEN2DLNode(text, true, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Node GetEN2DLNode(string text, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2Ns = null)
	public final cognipy.cnl.dl.Node GetEN2DLNode(String text, boolean throwOnError, boolean useFullUri, tangible.Func1Param<String, String> pfx2Ns)
	{
		if (text.trim().equals(""))
		{
			return null;
		}
		tools.SYMBOL smb = factory.getParser().Parse("Every loooooked-for is " + text + " .");
		if (!factory.isParagraph(smb)) // get null on syntax error
		{
			if (smb instanceof tools.error)
			{
				if (throwOnError)
				{
					throw new ParseException((smb instanceof tools.error ? (tools.error)smb : null).toString(), (smb instanceof tools.error ? (tools.error)smb : null).getLine(), (smb instanceof tools.error ? (tools.error)smb : null).getPosition(), (smb instanceof tools.error ? (tools.error)smb : null).pos, text);
				}
			}
			return null;
		}
		else
		{
			if (!factory.ValidateSafeness(smb))
			{
				if (throwOnError)
				{
					throw new IllegalStateException();
				}
				else
				{
					return null;
				}
			}
			cognipy.cnl.dl.Paragraph stmt = factory.InvConvert(smb, useFullUri, pfx2Ns);
			Object tempVar = stmt.Statements.get(0);
			return (tempVar instanceof cognipy.cnl.dl.Subsumption ? (cognipy.cnl.dl.Subsumption)tempVar : null).D;
		}
	}


	public final Object GetENAst(String text)
	{
		return GetENAst(text, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public object GetENAst(string text, bool throwOnError = true)
	public final Object GetENAst(String text, boolean throwOnError)
	{
		if (text.trim().equals(""))
		{
			return null;
		}
		tools.SYMBOL smb = factory.getParser().Parse(text);
		if (!factory.isParagraph(smb)) // get null on syntax error
		{
			if (smb instanceof tools.error)
			{
				if (throwOnError)
				{
					throw new ParseException((smb instanceof tools.error ? (tools.error)smb : null).toString(), (smb instanceof tools.error ? (tools.error)smb : null).getLine(), (smb instanceof tools.error ? (tools.error)smb : null).getPosition(), (smb instanceof tools.error ? (tools.error)smb : null).pos, text);
				}
			}
			return null;
		}
		else
		{
			if (!factory.ValidateSafeness(smb))
			{
				if (throwOnError)
				{
					throw new IllegalStateException();
				}
				else
				{
					return null;
				}
			}
			return smb;
		}
	}


	public final String GetENFromAstSentence(Object astSent, boolean serializeAnnotations)
	{
		return GetENFromAstSentence(astSent, serializeAnnotations, false);
	}

	public final String GetENFromAstSentence(Object astSent)
	{
		return GetENFromAstSentence(astSent, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string GetENFromAstSentence(object astSent, bool serializeAnnotations = false, bool templateMode = false)
	public final String GetENFromAstSentence(Object astSent, boolean serializeAnnotations, boolean templateMode)
	{
		tangible.OutObject<cognipy.cnl.AnnotationManager> tempOut__annotMan = new tangible.OutObject<cognipy.cnl.AnnotationManager>();
		String tempVar = factory.Serialize(astSent, serializeAnnotations, tempOut__annotMan, templateMode);
	_annotMan = tempOut__annotMan.argValue;
	return tempVar;
	}

	/** 
	 Translate a EN-CNL text to a DL ast
	 
	 @param text Input EN-CNL text
	 @param throwOnError
	 @return 
	*/

	public final cognipy.cnl.dl.Paragraph GetEN2DLAst(String text, boolean throwOnError, boolean useFullUri)
	{
		return GetEN2DLAst(text, throwOnError, useFullUri, null);
	}

	public final cognipy.cnl.dl.Paragraph GetEN2DLAst(String text, boolean throwOnError)
	{
		return GetEN2DLAst(text, throwOnError, false, null);
	}

	public final cognipy.cnl.dl.Paragraph GetEN2DLAst(String text)
	{
		return GetEN2DLAst(text, true, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Paragraph GetEN2DLAst(string text, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2ns = null)
	public final cognipy.cnl.dl.Paragraph GetEN2DLAst(String text, boolean throwOnError, boolean useFullUri, tangible.Func1Param<String, String> pfx2ns)
	{
		if (text.trim().equals(""))
		{
			return null;
		}
		tools.SYMBOL smb = factory.getParser().Parse(text);
		if (!factory.isParagraph(smb)) // get null on syntax error
		{
			if (smb instanceof tools.error)
			{
				if (throwOnError)
				{
					throw new ParseException((smb instanceof tools.error ? (tools.error)smb : null).toString(), (smb instanceof tools.error ? (tools.error)smb : null).getLine(), (smb instanceof tools.error ? (tools.error)smb : null).getPosition(), (smb instanceof tools.error ? (tools.error)smb : null).pos, text);
				}
			}
			return null;
		}
		else
		{
			if (!factory.ValidateSafeness(smb))
			{
				if (throwOnError)
				{
					throw new IllegalStateException();
				}
				else
				{
					return null;
				}
			}
			return factory.InvConvert(smb, useFullUri, pfx2ns);
		}
	}


	public final cognipy.cnl.dl.Paragraph GetDLAstFromEnAst(Object smb, boolean throwOnError, boolean useFullUri)
	{
		return GetDLAstFromEnAst(smb, throwOnError, useFullUri, null);
	}

	public final cognipy.cnl.dl.Paragraph GetDLAstFromEnAst(Object smb, boolean throwOnError)
	{
		return GetDLAstFromEnAst(smb, throwOnError, false, null);
	}

	public final cognipy.cnl.dl.Paragraph GetDLAstFromEnAst(Object smb)
	{
		return GetDLAstFromEnAst(smb, true, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Paragraph GetDLAstFromEnAst(object smb, bool throwOnError = true, bool useFullUri = false, Func<string, string> pfx2ns = null)
	public final cognipy.cnl.dl.Paragraph GetDLAstFromEnAst(Object smb, boolean throwOnError, boolean useFullUri, tangible.Func1Param<String, String> pfx2ns)
	{
		if (!factory.ValidateSafeness(smb))
		{
			if (throwOnError)
			{
				throw new IllegalStateException();
			}
			else
			{
				return null;
			}
		}
		return factory.InvConvert((tools.SYMBOL)smb, useFullUri, pfx2ns);
	}

	private static final Regex regxForPrefixes = new Regex("(?<=\\[).*?(?=\\])", RegexOptions.Compiled);
	/** 
	 Returns the prefixes found in the cnl string given
	 
	 @param cnlString
	 @return 
	*/
	public final ArrayList<String> getPrefixFromCNL(String cnlString)
	{
		ArrayList<String> allPrefixes = new ArrayList<String>();

		if (!regxForPrefixes.IsMatch(cnlString))
		{
			return allPrefixes;
		}

		tools.Lexer lex = factory.getLexer();
		lex.Start(cnlString);
		while (true)
		{
			while (true)
			{
				tools.TOKEN tok = lex.Next();
				if (tok == null)
				{
					if (lex.PeekChar() == 0)
					{
						return allPrefixes;
					}
					else
					{
						lex.GetChar();
						continue;
					}
				}

				if (factory.TagIsName(tok.getClass().getSimpleName()) || factory.TagIsInstanceName(tok.getClass().getSimpleName()))
				{
					if (regxForPrefixes.IsMatch(tok.getYytext()))
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						for (var prefMatch : regxForPrefixes.Matches(cnlString))
						{
							if (!allPrefixes.contains(prefMatch.toString()))
							{
								allPrefixes.add(prefMatch.toString());
							}
						}
					}
					break;
				}
			}
		}
	}


	public final java.util.HashMap<ARS.EntityKind, cognipy.cnl.dl.Paragraph> GetENAnnotations2DLAst(boolean useFullUri)
	{
		return GetENAnnotations2DLAst(useFullUri, null);
	}

	public final java.util.HashMap<ARS.EntityKind, cognipy.cnl.dl.Paragraph> GetENAnnotations2DLAst()
	{
		return GetENAnnotations2DLAst(false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Dictionary<ARS.EntityKind, CogniPy.CNL.DL.Paragraph> GetENAnnotations2DLAst(bool useFullUri = false, Func<string, string> pfx2ns = null)
	public final HashMap<ARS.EntityKind, cognipy.cnl.dl.Paragraph> GetENAnnotations2DLAst(boolean useFullUri, tangible.Func1Param<String, String> pfx2ns)
	{
		if (getAnnotMan() != null)
		{
			HashMap<ARS.EntityKind, cognipy.cnl.dl.Paragraph> res = new HashMap<ARS.EntityKind, cognipy.cnl.dl.Paragraph>();
			HashMap<ARS.EntityKind, ArrayList<cognipy.cnl.dl.DLAnnotationAxiom>> annAx = getAnnotMan().getDLAnnotationAxioms(pfx2ns);
			for (Map.Entry<ARS.EntityKind, ArrayList<cognipy.cnl.dl.DLAnnotationAxiom>> ann : annAx.entrySet())
			{
				if (!res.containsKey(ann.getKey()))
				{
					cognipy.cnl.dl.Paragraph tempVar = new cognipy.cnl.dl.Paragraph(null);
					tempVar.Statements = new ArrayList<cognipy.cnl.dl.Statement>();
					res.put(ann.getKey(), tempVar);
				}
				res.get(ann.getKey()).Statements.addAll(ann.getValue().Select(x -> x instanceof cognipy.cnl.dl.Statement ? (cognipy.cnl.dl.Statement)x : null).ToList());
			}
			return res;
		}
		else
		{
			return null;
		}
	}

	private tools.Parser dlParser = new cognipy.cnl.dl.dl();

	/** 
	 Convert a string representation of DL to a DL ast.
	 
	 @param text
	 @param throwOnError
	 @return 
	*/

	public final cognipy.cnl.dl.Paragraph GetDLAst(String text)
	{
		return GetDLAst(text, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Paragraph GetDLAst(string text, bool throwOnError = true)
	public final cognipy.cnl.dl.Paragraph GetDLAst(String text, boolean throwOnError)
	{
		if (text.trim().equals(""))
		{
			return null;
		}
		tools.SYMBOL smb = dlParser.Parse(text);
		if (!(smb instanceof cognipy.cnl.dl.Paragraph)) // get null on syntax error
		{
			if (smb instanceof tools.error)
			{
				if (throwOnError)
				{
					throw new ParseException((smb instanceof tools.error ? (tools.error)smb : null).toString(), (smb instanceof tools.error ? (tools.error)smb : null).getLine(), (smb instanceof tools.error ? (tools.error)smb : null).getPosition(), (smb instanceof tools.error ? (tools.error)smb : null).pos, text);
				}
			}
			return null;
		}
		else
		{
			return smb instanceof cognipy.cnl.dl.Paragraph ? (cognipy.cnl.dl.Paragraph)smb : null;
		}
	}


	public final String SerializeDLAst(cognipy.cnl.dl.Paragraph dlast)
	{
		return SerializeDLAst(dlast, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string SerializeDLAst(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
	public final String SerializeDLAst(cognipy.cnl.dl.Paragraph dlast, boolean simplifyBrackets)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(simplifyBrackets);
		return (ser.Serialize(dlast));
	}


	public final java.util.HashSet<Tuple<cognipy.ars.EntityKind, String>> GetDLAstSignature(cognipy.cnl.dl.Paragraph dlast)
	{
		return GetDLAstSignature(dlast, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public HashSet<Tuple<CogniPy.ARS.EntityKind, string>> GetDLAstSignature(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
	public final HashSet<Tuple<cognipy.ars.EntityKind, String>> GetDLAstSignature(cognipy.cnl.dl.Paragraph dlast, boolean simplifyBrackets)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(simplifyBrackets);
		ser.Serialize(dlast);
		return ser.GetTaggedSignature();
	}


	public final java.util.HashSet<Tuple<String, String, String>> GetDLAstDataSignature(cognipy.cnl.dl.Paragraph dlast)
	{
		return GetDLAstDataSignature(dlast, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public HashSet<Tuple<string, string, string>> GetDLAstDataSignature(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
	public final HashSet<Tuple<String, String, String>> GetDLAstDataSignature(cognipy.cnl.dl.Paragraph dlast, boolean simplifyBrackets)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(simplifyBrackets);
		ser.Serialize(dlast);
		return ser.GetDataValues();
	}


	public final Tuple<java.util.HashSet<Tuple<cognipy.ars.EntityKind, String>>, java.util.HashSet<Tuple<String, String, String>>> GetDLAstFullSignature(cognipy.cnl.dl.Paragraph dlast)
	{
		return GetDLAstFullSignature(dlast, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Tuple<HashSet<Tuple<CogniPy.ARS.EntityKind, string>>, HashSet<Tuple<string, string, string>>> GetDLAstFullSignature(CogniPy.CNL.DL.Paragraph dlast, bool simplifyBrackets = false)
	public final Tuple<HashSet<Tuple<cognipy.ars.EntityKind, String>>, HashSet<Tuple<String, String, String>>> GetDLAstFullSignature(cognipy.cnl.dl.Paragraph dlast, boolean simplifyBrackets)
	{
		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(simplifyBrackets);
		ser.Serialize(dlast);
		return Tuple.Create(ser.GetTaggedSignature(), ser.GetDataValues());
	}


	public final String GetDL(String text, boolean throwOnError)
	{
		return GetDL(text, throwOnError, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string GetDL(string text, bool throwOnError, bool simplifyBrackets = false)
	public final String GetDL(String text, boolean throwOnError, boolean simplifyBrackets)
	{
		cognipy.cnl.dl.Paragraph ast = GetEN2DLAst(text, throwOnError);
		return ast == null ? null : SerializeDLAst(ast, simplifyBrackets);
	}


	public final String GetENDLFromAst(cognipy.cnl.dl.IAccept nodeast, boolean serializeAnnotations)
	{
		return GetENDLFromAst(nodeast, serializeAnnotations, null);
	}

	public final String GetENDLFromAst(cognipy.cnl.dl.IAccept nodeast)
	{
		return GetENDLFromAst(nodeast, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string GetENDLFromAst(CogniPy.CNL.DL.IAccept nodeast, bool serializeAnnotations = false, Func<string, string> ns2pfx = null)
	public final String GetENDLFromAst(cognipy.cnl.dl.IAccept nodeast, boolean serializeAnnotations, tangible.Func1Param<String, String> ns2pfx)
	{
		Object enast = factory.Convert(nodeast, (ns2pfx == null) ? false : true, ns2pfx);
		tangible.OutObject<cognipy.cnl.AnnotationManager> tempOut__annotMan = new tangible.OutObject<cognipy.cnl.AnnotationManager>();
		String tempVar = factory.Serialize(enast, serializeAnnotations, tempOut__annotMan);
	_annotMan = tempOut__annotMan.argValue;
	return tempVar;
	}


	public final String GetENDLFromAst(cognipy.cnl.dl.Statement stmast, boolean serializeAnnotations)
	{
		return GetENDLFromAst(stmast, serializeAnnotations, null);
	}

	public final String GetENDLFromAst(cognipy.cnl.dl.Statement stmast)
	{
		return GetENDLFromAst(stmast, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string GetENDLFromAst(CogniPy.CNL.DL.Statement stmast, bool serializeAnnotations = false, Func<string, string> ns2pfx = null)
	public final String GetENDLFromAst(cognipy.cnl.dl.Statement stmast, boolean serializeAnnotations, tangible.Func1Param<String, String> ns2pfx)
	{
		Object enast = factory.Convert(stmast, (ns2pfx == null) ? false : true, ns2pfx);
		tangible.OutObject<cognipy.cnl.AnnotationManager> tempOut__annotMan = new tangible.OutObject<cognipy.cnl.AnnotationManager>();
		String tempVar = factory.Serialize(enast, serializeAnnotations, tempOut__annotMan);
	_annotMan = tempOut__annotMan.argValue;
	return tempVar;
	}


	public final String GetENDLFromAst(cognipy.cnl.dl.Paragraph dlast, boolean serializeAnnotations)
	{
		return GetENDLFromAst(dlast, serializeAnnotations, null);
	}

	public final String GetENDLFromAst(cognipy.cnl.dl.Paragraph dlast)
	{
		return GetENDLFromAst(dlast, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string GetENDLFromAst(CogniPy.CNL.DL.Paragraph dlast, bool serializeAnnotations = false, Func<string, string> ns2pfx = null)
	public final String GetENDLFromAst(cognipy.cnl.dl.Paragraph dlast, boolean serializeAnnotations, tangible.Func1Param<String, String> ns2pfx)
	{
		Object enast = factory.Convert(dlast, (ns2pfx == null) ? false : true, ns2pfx);
		tangible.OutObject<cognipy.cnl.AnnotationManager> tempOut__annotMan = new tangible.OutObject<cognipy.cnl.AnnotationManager>();
		String tempVar = factory.Serialize(enast, serializeAnnotations, tempOut__annotMan);
	_annotMan = tempOut__annotMan.argValue;
	return tempVar;
	}

	private AnnotationManager _annotMan;
	public final AnnotationManager getAnnotMan()
	{
		return _annotMan;
	}
	public final void setAnnotMan(AnnotationManager value)
	{
		_annotMan = value;
	}

	public final java.lang.Iterable<String> Morphology(java.lang.Iterable<String> col, String str, String form, boolean bigName)
	{
		return factory.Morphology(col, str, form, bigName);
	}

	public final String[] GetAllKeywords()
	{
		return factory.GetAllKeywords();
	}

	public final ArrayList<String> GetModalKeywords()
	{
		ArrayList<String> modalities = new ArrayList<String>();
		modalities.add(GetKeyword("MUST"));
		modalities.add(GetKeyword("SHOULD"));
		modalities.add(GetKeyword("CAN"));
		modalities.add(GetKeyword("CANNOT"));
		modalities.add(GetKeyword("SHOULDNOT"));
		modalities.add(GetKeyword("MUSTNOT"));

		return modalities;
	}

	public final boolean IsKeyword(String kw)
	{
		return factory.IsKeyword(kw);
	}

	public final boolean KeywordExists(String kw)
	{
		return factory.KeywordTagExists(kw);
	}

	public final String GetKeyword(String kw)
	{
		return factory.GetKeyword(kw);
	}

	public final String GetKeywordTag(String kw)
	{
		return factory.GetKeywordTag(kw);
	}

	private static String MARK = "mark-mark";
	private static Random rnd = new Random(0);

	public final Map.Entry<String, String> GetMorphologyAndTypeOfNextName(String begigning, String suffix)
	{
		String snt = BuildSmallestSentenceStartigWith(begigning, MARK, suffix);
		if (snt == null)
		{
			return new Map.Entry<String, String>(null, null);
		}
		tools.SYMBOL smb = factory.getParser().Parse(snt);
		if (!factory.isParagraph(smb)) // get null on syntax error
		{
			//                if (smb is Tools.error)
			//                  System.Diagnostics.Debug.Assert(false);
			return new Map.Entry<String, String>();
		}
		else
		{
			if (!factory.ValidateSafeness(smb))
			{
				return new Map.Entry<String, String>();
			}
			String kind;
			String form;
			tangible.OutObject<String> tempOut_kind = new tangible.OutObject<String>();
			tangible.OutObject<String> tempOut_form = new tangible.OutObject<String>();
			factory.FindMark(smb, MARK, tempOut_kind, tempOut_form);
		form = tempOut_form.argValue;
		kind = tempOut_kind.argValue;
			return new Map.Entry<String, String>(kind, form);
		}
	}

	private Regex alnumRegex = new Regex("\\A[\\w|\\d|\\-|\\\"|\\s]*\\Z", RegexOptions.IgnoreCase.getValue() | RegexOptions.Compiled.getValue());

	public final boolean TryGetTypeOfNextWord(String sentence, String beg, tangible.OutObject<HashSet<String>> whatToLoad, tangible.OutObject<HashSet<String>> keys, tangible.OutObject<ArrayList<Map.Entry<String, String>>> symbols)
	{
		keys.argValue = new HashSet<String>();
		whatToLoad.argValue = new HashSet<String>();
		symbols.argValue = new ArrayList<Map.Entry<String, String>>();

		ObjectList props = factory.getParser().CompletionProposals(sentence);

		if (props == null)
		{
			return false;
		}

		String[] suffixes = factory.GetTagSuffixes();

		for (String prop : props)
		{
			String pfx = factory.GetDefaultTagValue(prop);

			if (factory.getParser().TestParse(sentence + " " + pfx))
			{
				if (factory.TagIsDatatype(prop))
				{
					symbols.argValue.add(new Map.Entry<String, String>(factory.GetSymbol(prop), pfx));
					keys.argValue.add("<" + factory.GetSymbol(prop) + ">");
				}
				if (factory.TagIsName(prop))
				{
					if (alnumRegex.IsMatch(beg))
					{
						for (String suf : suffixes)
						{
							Map.Entry<String, String> mn = GetMorphologyAndTypeOfNextName(sentence, suf);
							if (mn.getKey() != null)
							{
								symbols.argValue.add(mn);
								whatToLoad.argValue.add(mn.getKey() + ":" + mn.getValue());
							}
						}
					}
				}
				else if (factory.TagIsInstanceName(prop))
				{
					if (alnumRegex.IsMatch(beg))
					{
						symbols.argValue.add(new Map.Entry<String, String>(factory.GetSymbol(prop), ""));
						whatToLoad.argValue.add("instance" + ":NormalForm");
					}
				}
				else
				{
					for (String suf : suffixes)
					{
						if (factory.KeywordTagExists(prop))
						{
							HashSet<String> kwds = factory.GetAllMatchingKeywords(prop);
							String smb = factory.GetSymbol(prop);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
							for (var kwd : kwds)
							{
								if (!beg.toLowerCase().equals(kwd) && kwd.StartsWith(beg.toLowerCase()))
								{
									if (IsCorrectKeyword(sentence, kwd, suf))
									{
										symbols.argValue.add(new Map.Entry<String, String>(smb, ""));
										keys.argValue.add(kwd);
									}
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

	public final ArrayList<String> AutoComplete(cognipy.cnl.dl.Populator populator, String full, tangible.OutObject<ArrayList<Map.Entry<String, String>>> symbols, int max)
	{
		symbols.argValue = new ArrayList<Map.Entry<String, String>>();
		Map.Entry<String, String> kv = GetSentenceWitoutLastWord(full);
		String sentence = kv.getKey();
		String beg = kv.getValue();

		if (IsKeyword(GetFirstWord(sentence)))
		{
			sentence = LoCase(sentence);
		}

		HashSet<String> names = new HashSet<String>();
		HashSet<String> bignames = new HashSet<String>();
		HashSet<String> extnames = new HashSet<String>();
		HashSet<String> extbignames = new HashSet<String>();

		HashSet<String> whatToLoad;
		HashSet<String> keys;

		tangible.OutObject<HashSet<String>> tempOut_whatToLoad = new tangible.OutObject<HashSet<String>>();
		tangible.OutObject<HashSet<String>> tempOut_keys = new tangible.OutObject<HashSet<String>>();
		if (!TryGetTypeOfNextWord(sentence, beg, tempOut_whatToLoad, tempOut_keys, symbols))
		{
		keys = tempOut_keys.argValue;
		whatToLoad = tempOut_whatToLoad.argValue;
			return null;
		}
	else
	{
		keys = tempOut_keys.argValue;
		whatToLoad = tempOut_whatToLoad.argValue;
	}

		if (populator != null)
		{
			// get the autcompleted elements from the populator (only for the whatToLoad elements (role,instance,...))
			java.lang.Iterable<Map.Entry<String, String>> pops = populator.Populate(sentence, beg, whatToLoad.ToList(), max);
			for (Map.Entry<String, String> kvx : pops) //populate the names list
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var x = kvx.getKey().Split("[:]", -1);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var v = kvx.getValue();
				if (!beg.equals(v))
				{
					if (x[1].equals("role") || x[1].equals("datarole") || x[1].equals("concept"))
					{
						if (x[0].equals("i"))
						{
							names.add(v);
						}
						else
						{
							extnames.add(v);
						}
					}
					else
					{
						if (x[0].equals("i"))
						{
							bignames.add(v);
						}
						else
						{
							extbignames.add(v);
						}
					}
				}
			}
		}

		boolean wasDot = false;
		ArrayList<String> possibleKeywords = new ArrayList<String>();
		for (String w : keys) // add the keywords
		{
			if (!possibleKeywords.contains(w))
			{
				if (w.contains(GetKeyword(factory.GetEOLTag())))
				{
					wasDot = true;
				}
				else
				{
					possibleKeywords.add(w);
				}
			}
		}
//C# TO JAVA CONVERTER TODO TASK: This version of the List.Sort method is not converted to Java:
		possibleKeywords.Sort();

		ArrayList<String> possibleNames = new ArrayList<String>();
		for (String w : names)
		{
			if (!possibleNames.contains(w))
			{
				possibleNames.add(w);
			}
		}
//C# TO JAVA CONVERTER TODO TASK: This version of the List.Sort method is not converted to Java:
		possibleNames.Sort();

		ArrayList<String> possibleBigames = new ArrayList<String>();
		for (String w : bignames)
		{
			if (!possibleBigames.contains(w))
			{
				possibleBigames.add(w);
			}
		}
//C# TO JAVA CONVERTER TODO TASK: This version of the List.Sort method is not converted to Java:
		possibleBigames.Sort();

		ArrayList<String> possibleWords = new ArrayList<String>();
		possibleWords.addAll(possibleKeywords);

		if (possibleWords.size() < max)
		{
			int delta = max - possibleWords.size();
			if (possibleNames.isEmpty() || possibleBigames.isEmpty())
			{
				possibleWords.addAll(possibleNames.Take(delta));
				possibleWords.addAll(possibleBigames.Take(delta));
				int tot = delta - (possibleNames.size() + possibleBigames.size());
				possibleWords.addAll(extnames.Take(tot / 2));
				possibleWords.addAll(extbignames.Take(tot / 2));
			}
			else if (possibleNames.size() + possibleBigames.size() <= delta)
			{
				possibleWords.addAll(possibleNames);
				possibleWords.addAll(possibleBigames);
				int tot = delta - (possibleNames.size() + possibleBigames.size());
				possibleWords.addAll(extnames.Take(tot / 2));
				possibleWords.addAll(extbignames.Take(tot / 2));
			}
			else
			{
				if (possibleNames.size() <= delta / 2)
				{
					possibleWords.addAll(possibleNames);
					possibleWords.addAll(possibleBigames.Take(delta - possibleNames.size()));
				}
				else if (possibleBigames.size() <= delta / 2)
				{
					possibleWords.addAll(possibleNames.Take(delta - possibleBigames.size()));
					possibleWords.addAll(possibleBigames);
				}
				else
				{
					possibleWords.addAll(possibleNames.Take(delta / 2));
					possibleWords.addAll(possibleBigames.Take(delta / 2));
				}
			}
		}
		if (wasDot)
		{
			possibleWords.add(GetKeyword(factory.GetEOLTag()));
		}
		return possibleWords;
	}

	/** 
	 This function has similar functionality that Autocomplete. 
	 The difference are:
		* no sorting of the result --> we are assuming that sorting is already done correctly by the populator
		* before proposals from the populator and then from the keywords
		* max used to constrain all autocompletition proposals.
	 
	 @param populator
	 @param full
	 @param symbols
	 @param max
	 @param wordsToSkip
	 @return 
	*/

	public final java.util.ArrayList<String> AutoComplete2(cognipy.cnl.dl.Populator populator, String full, tangible.OutObject<java.util.ArrayList<java.util.Map.Entry<String, String>>> symbols, int max)
	{
		return AutoComplete2(populator, full, symbols, max, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<string> AutoComplete2(CogniPy.CNL.DL.Populator populator, string full, out List<KeyValuePair<string, string>> symbols, int max, List<string> wordsToSkip = null)
	public final ArrayList<String> AutoComplete2(cognipy.cnl.dl.Populator populator, String full, tangible.OutObject<ArrayList<Map.Entry<String, String>>> symbols, int max, ArrayList<String> wordsToSkip)
	{
		if (wordsToSkip == null)
		{
			wordsToSkip = new ArrayList<String>();
		}

		symbols.argValue = new ArrayList<Map.Entry<String, String>>();
		Map.Entry<String, String> kv = GetSentenceWitoutLastWord(full);
		String sentence = kv.getKey();
		String beg = kv.getValue();

		if (IsKeyword(GetFirstWord(sentence)))
		{
			sentence = LoCase(sentence);
		}

		HashSet<String> names = new HashSet<String>();
		HashSet<String> bignames = new HashSet<String>();
		HashSet<String> extnames = new HashSet<String>();
		HashSet<String> extbignames = new HashSet<String>();

		HashSet<String> whatToLoad;
		HashSet<String> keys;

		tangible.OutObject<HashSet<String>> tempOut_whatToLoad = new tangible.OutObject<HashSet<String>>();
		tangible.OutObject<HashSet<String>> tempOut_keys = new tangible.OutObject<HashSet<String>>();
		if (!TryGetTypeOfNextWord(sentence, beg, tempOut_whatToLoad, tempOut_keys, symbols))
		{
		keys = tempOut_keys.argValue;
		whatToLoad = tempOut_whatToLoad.argValue;
			return new ArrayList<String>();
		}
	else
	{
		keys = tempOut_keys.argValue;
		whatToLoad = tempOut_whatToLoad.argValue;
	}

		if (populator != null)
		{
			// get the autcompleted elements from the populator (only for the whatToLoad elements (role,instance,...))
			java.lang.Iterable<Map.Entry<String, String>> pops = populator.Populate(sentence, beg, whatToLoad.ToList(), max);
			for (Map.Entry<String, String> kvx : pops) //populate the names list
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var x = kvx.getKey().Split("[:]", -1);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var v = kvx.getValue();
				if (!beg.equals(v))
				{
					if (x[1].equals("role") || x[1].equals("datarole") || x[1].equals("concept"))
					{
						if (x[0].equals("i"))
						{
							names.add(v);
						}
						else
						{
							extnames.add(v);
						}
					}
					else
					{
						if (x[0].equals("i"))
						{
							bignames.add(v);
						}
						else
						{
							extbignames.add(v);
						}
					}
				}
			}
		}

		boolean wasDot = false;
		ArrayList<String> possibleKeywords = new ArrayList<String>();
		for (String w : keys) // add the keywords
		{
			if (!possibleKeywords.contains(w) && !wordsToSkip.contains(w))
			{
				if (w.contains(GetKeyword(factory.GetEOLTag())))
				{
					wasDot = true;
				}
				else
				{
					possibleKeywords.add(w);
				}
			}
		}
//C# TO JAVA CONVERTER TODO TASK: This version of the List.Sort method is not converted to Java:
		possibleKeywords.Sort();

		ArrayList<String> possibleWords = new ArrayList<String>();

		for (String w : names)
		{
			if (!possibleWords.contains(w) && !wordsToSkip.contains(w))
			{
				possibleWords.add(w);
			}
		}

		for (String w : bignames)
		{
			if (!possibleWords.contains(w) && !wordsToSkip.contains(w))
			{
				possibleWords.add(w);
			}
		}

		possibleWords.addAll(possibleKeywords);

		if (wasDot)
		{
			possibleWords.add(GetKeyword(factory.GetEOLTag()));
		}

		return possibleWords.Take(max).ToList();
	}

	private String GetSentenceTemplate(String sentence)
	{
		tools.Lexer lex = factory.getLexer();
		lex.Start(sentence);
		StringBuilder templ = new StringBuilder();
		while (true)
		{
			tools.TOKEN tok = lex.Next();
			if (tok == null)
			{
				if (lex.PeekChar() == 0)
				{
					return templ.toString();
				}
				else
				{
					lex.GetChar();
					continue;
				}
			}

			if (!IsKeyword(tok.getYytext()) && !factory.TagIsDatatype(tok.getYyname()))
			{
				templ.append((Character.isLetter(tok.getYytext().charAt(0)) && (Character.toUpperCase(tok.getYytext().charAt(0)) == tok.getYytext().charAt(0))) ? "Bigname" : "name");
			}
			else
			{
				templ.append(tok.getYytext());
			}

			templ.append(" ");

			if (factory.isEOL(tok))
			{
				break;
			}
		}
		return templ.toString();
	}

	private static HashMap<String, String> smallestSentenceCache = new HashMap<String, String>();
	private boolean smallestSentenceCacheIsBuilding = false;

	private void BuildSmallestSentenceCacheForGenerator(tangible.Func0Param<String> generator)
	{
		for (int i = 0; i < 1; i++)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var sent = generator.invoke();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var wrds = sent.Replace("^", "").split(new char[] {' ', '.'}, StringSplitOptions.RemoveEmptyEntries);
			StringBuilder sb = new StringBuilder();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var w : wrds)
			{
				ArrayList<Map.Entry<String, String>> symbols;
				tangible.OutObject<ArrayList<Map.Entry<String, String>>> tempOut_symbols = new tangible.OutObject<ArrayList<Map.Entry<String, String>>>();
				ArrayList<String> cpls = AutoComplete(null, sb.toString() + " ", tempOut_symbols, 1);
			symbols = tempOut_symbols.argValue;
				sb.append((sb.length() != 0 ? " " : "") + w);
			}
		}
	}

	public final void BuildSmallestSentenceCache()
	{
		synchronized (smallestSentenceCache)
		{
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl3());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl4());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl5());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl6());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl7());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrl8());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrlWithBuiltins1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrlWithBuiltins2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrlWithUnaryBuiltinNamed("sine-of"));
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrlWithUnaryBuiltinNamed("ends-with-string"));
			BuildSmallestSentenceCacheForGenerator(() -> GenerateSwrlWithBinaryBuiltinNamed("raised-to-the-power-of"));

			BuildSmallestSentenceCacheForGenerator(() -> GenerateEvery1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEvery2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEvery3());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateAssert1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateAssert2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateAssertOnly());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateComplexRoleSubsumption(2));
			BuildSmallestSentenceCacheForGenerator(() -> GenerateComplexRoleSubsumption(3));
			BuildSmallestSentenceCacheForGenerator(() -> GenerateDisjoint1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateDisjoint2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateDisjointRoles());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEquiv1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEquiv2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEquivalentRoles());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEveryOnly());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEveryOnlyValue());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEverySingle1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEverySingle2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateEveryValue2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateNegativeAssert1());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateNegativeAssert2());
			BuildSmallestSentenceCacheForGenerator(() -> GenerateValueAssert1());
			for (String a : AdvancedSamples)
			{
				String tmp = a;
				BuildSmallestSentenceCacheForGenerator(() -> tmp);
			}
		}
	}

	public static String[] AdvancedSamples = new String[] {"Every man must be a cat.", "Every man must do-not love a pig.", "Anything either is a cat, is a dog, is a cat, is a lok or is a pig or-something-else.", "Something is a cat if-and-only-if-it-either is a dog, is a pig or is a cat that loves a mice.", "Nothing is a cat.", "The-one-and-only cat is a pig and is a lok.", "The cat loves.", "Every value-of kooo is something ((either 10 or 20) as-well-as different-from 20).", "Every X that is a cat is-unique-if X is loved by something and X lokes something and X is loved by equal-to something."};

	public final String ExampleSmallestSentenceStartigWith(String begining)
	{
		return BuildSmallestSentenceStartigWith(begining, "", "");
	}

	//public string ExampleSmallestSentenceStartigWith(string begining, string mark)
	//{
	//    return BuildSmallestSentenceStartigWith(begining, mark, "");
	//}

	//public SYMBOL CreateAuxiliaryTreeWithMarkForSentenceStartingWith(string begining, string mark)
	//{
	//    string snt = BuildSmallestSentenceStartigWith(begining, mark, "");

	//    if (snt == null)
	//        return null;
	//    string[] snts = snt.Split(' ');
	//    string[] beginings = begining.Split(' ');
	//    for (int i = 0; i < beginings.Length; i++)
	//    {
	//        snts[i] = beginings[i];
	//    }
	//    snt = string.Join(" ", snts);
	//    Tools.SYMBOL smb = factory.getParser().Parse(snt);
	//    if (!factory.isParagraph(smb))   // get null on syntax error
	//    {
	//        //                if (smb is Tools.error)
	//        //                  System.Diagnostics.Debug.Assert(false);
	//        return null;
	//    }
	//    else
	//    {
	//        return smb;
	//    }
	//}

	private String BuildSmallestSentenceStartigWith(String begining, String mrk, String suffix)
	{
		int valretryCnt = 0;
		int retryCnt = 0;
	retry:
		String sentenceX = GetSentenceWitoutLastWord(begining).getKey();

		String sentence = GetSentenceTemplate(sentenceX).trim();

		if (IsKeyword(GetFirstWord(sentence)))
		{
			sentence = LoCase(sentence);
		}

		if (!tangible.StringHelper.isNullOrEmpty(mrk))
		{
			sentence += " " + mrk;
		}

		if (!tangible.StringHelper.isNullOrEmpty(suffix))
		{
			sentence += " " + suffix;
		}

		synchronized (smallestSentenceCache)
		{
			if (smallestSentenceCache.isEmpty())
			{
				if (!factory.LoadSmallestSentenceCache(smallestSentenceCache))
				{
					if (!smallestSentenceCacheIsBuilding)
					{
						smallestSentenceCacheIsBuilding = true;
						BuildSmallestSentenceCache();
						factory.SaveSmallestSentenceCache(smallestSentenceCache);
						smallestSentenceCacheIsBuilding = false;
					}
				}
			}
		}
		synchronized (smallestSentenceCache)
		{
			if (smallestSentenceCache.containsKey(sentence))
			{
				return smallestSentenceCache.get(sentence);
			}

			if (!factory.getParser().TestParse(sentence))
			{
				smallestSentenceCache.put(sentence, null);
				return null;
			}
			tools.SYMBOL ast = factory.getParser().Parse(sentence);
			if (!factory.ValidateSafeness(ast))
			{
				smallestSentenceCache.put(sentence, null);
				return null;
			}
		}

		HashSet<String> sentencekey = new HashSet<String>();
		sentencekey.add(sentence);

		while (true)
		{
			if (sentence.length() - begining.length() > 300)
			{
				if (retryCnt < 5)
				{
					retryCnt++;
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
					goto retry;
				}
				else
				{
					return null;
				}
			}

			ObjectList props = factory.getParser().CompletionProposals(sentence);
			if (props == null)
			{
				return null;
			}

			HashMap<String, String> propset = new HashMap<String, String>();

			for (String p : props)
			{
				if (!KeywordExists(p))
				{
					propset.put(p, "");
				}
				else
				{
					if (!GetKeyword(p).equals(suffix))
					{
						propset.put(p, "");
					}
				}
			}

			ArrayList<Object> proparr = propset.keySet().ToList();
			if (propset.containsKey(factory.GetEOLTag()))
			{
				if (factory.getParser().TestParse(sentence + " " + GetKeyword(factory.GetEOLTag())))
				{
					if (factory.ValidateSafeness(factory.getParser().Parse(sentence + " " + GetKeyword(factory.GetEOLTag()))))
					{
						sentence += " " + GetKeyword(factory.GetEOLTag());
						break;
					}
					else
					{
						if (valretryCnt < 20)
						{
							valretryCnt++;
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
							goto retry;
						}
						else
						{
							sentence = null;
							break;
						}
					}
				}
			}

			if (proparr.isEmpty())
			{
				sentence += " " + suffix;
				sentencekey.add(sentence);
			}
			else
			{
				while (!proparr.isEmpty())
				{
					String prop = proparr.get(rnd.nextInt(proparr.size()));
					proparr.remove(prop);
					String pfx = factory.GetDefaultTagValue(prop);

					if (factory.getParser().TestParse(sentence + " " + pfx))
					{
						sentence += " " + pfx;
						sentencekey.add(sentence);
						break;
					}
				}
			}
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var key : sentencekey)
		{
			if (!smallestSentenceCache.containsKey(key))
			{
				smallestSentenceCache.put(key, sentence);
			}
		}

		return sentence;
	}

	public final String UpCase(String str)
	{
		if (str.length() > 0)
		{
			return Character.toUpperCase(str.charAt(0)) + str.substring(1, str.length());
		}
		else
		{
			return "";
		}
	}

	public final String LoCase(String str)
	{
		if (str.length() > 0)
		{
			return Character.toLowerCase(str.charAt(0)) + str.substring(1, str.length());
		}
		else
		{
			return "";
		}
	}

	public final String GetFirstWord(String sentence)
	{
		int i = 0;
		for (i = 0; i < sentence.length(); i++)
		{
			if (Character.isWhitespace(sentence.charAt(i)))
			{
				return sentence.substring(0, i);
			}
		}
		return sentence;
	}

	private boolean isPartOfWord(char c)
	{
		return Character.isLetterOrDigit(c) || c == '-' || c == '\"';
	}

	private boolean isSpecialSign(char c)
	{
		if (c == '\'' || c == '"')
		{
			return false;
		}
		else
		{
			return Character.IsPunctuation(c) && c != '-';
		}
	}

	private char[] sentent = new char[] {'(', ')', '.', '[', ']'};

	public final Map.Entry<String, String> GetSentenceWitoutLastWord(String sentence)
	{
		if (sentence.length() > 0)
		{
			if (sentent.Contains(sentence.charAt(sentence.length() - 1)))
			{
				return new Map.Entry<String, String>(sentence, "");
			}

			if (isSpecialSign(sentence.charAt(sentence.length() - 1)))
			{
				return new Map.Entry<String, String>(sentence.substring(0, sentence.length() - 1), tangible.StringHelper.substring(sentence, sentence.length() - 1, 1).trim());
			}
		}
		int i = 0;
		for (i = sentence.length() - 1; i >= 0; i--)
		{
			if (!isPartOfWord(sentence.charAt(i)))
			{
				return new Map.Entry<String, String>(sentence.substring(0, i + 1), sentence.substring(i, sentence.length()).trim());
			}
		}
		return new Map.Entry<String, String>("", sentence.trim());
	}

	/** 
	 This function is very similar to GetSentenceWithoutLastWord. 
	 There is a second one mainly because it was not recognizing correctly the last word when the word had [] inside.
	 
	 @param sentence
	 @return 
	*/
	public final Map.Entry<String, String> GetSentenceWitoutLastWord2(String sentence)
	{
		int i = 0;
		boolean inRef = false;
		for (i = sentence.length() - 1; i >= 0; i--)
		{
			if (sentence.charAt(i) == ']')
			{
				inRef = true;
			}

			if (!inRef && !isPartOfWord(sentence.charAt(i)))
			{
				return new Map.Entry<String, String>(sentence.substring(0, i + 1), sentence.substring(i, sentence.length()).trim());
			}

			if (sentence.charAt(i) == '[')
			{
				inRef = false;
			}
		}
		return new Map.Entry<String, String>("", sentence.trim());
	}

	private static Regex splitter = new Regex("(?<gr>([^\\[\\(\\)\\s\\.\\,]+(\\s*\\[[^\\]\\(\\)]+\\])?))+|(?<gr>[\\,\\.])", RegexOptions.IgnoreCase.getValue() | RegexOptions.Compiled.getValue());

	private MatchCollection splitToWords(String snt)
	{
		return splitter.Matches(snt);
	}

	private String dropAAn(String str)
	{
		if (str.toLowerCase().startsWith("if "))
		{
			return str.replace(" an name ", " a name ").replace(" an thing ", " a thing ");
		}
		else
		{
			return str.replace(" an name ", " name ").replace(" a name ", " name ");
		}
	}

	private boolean IsCorrectKeyword(String begigning, String kwd, String suf)
	{
		if (kwd.trim().startsWith("."))
		{
			return true;
		}

		boolean ok = false;
		String reformated = null;
		String snt = BuildSmallestSentenceStartigWith(begigning + kwd + " name", !suf.equals("") ? "name" : "", suf);
		if (snt != null)
		{
			tangible.OutObject<String> tempOut_reformated = new tangible.OutObject<String>();
			RewriteSentence(snt, tempOut_reformated);
		reformated = tempOut_reformated.argValue;
			ok = reformated != null;
		}
		if (ok)
		{
			MatchCollection refWrds = splitToWords(dropAAn(reformated));

			if (!Character.isLetter(kwd.charAt(0)))
			{
				MatchCollection begWrds = splitToWords(dropAAn(GetSentenceTemplate(begigning + kwd)));
				if (begWrds.Count < 1)
				{
					return true;
				}
				String kwd2 = LoCase(refWrds.get(begWrds.Count - 1).Value);
				String kwd3 = LoCase(begWrds.get(begWrds.Count - 1).Value);
				return ((kwd2.equals("or") && kwd3.equals(",")) || (kwd3.equals("or") && kwd2.equals(","))) || kwd2.equals(kwd3);
			}
			else
			{
				MatchCollection begWrds = splitToWords(dropAAn(GetSentenceTemplate(begigning)));
				String kwd2 = LoCase(refWrds.get(begWrds.Count).Value);
				return LoCase(kwd).equals(kwd2);
			}
		}
		else
		{
			String sentence = begigning + kwd;
			ObjectList props = factory.getParser().CompletionProposals(sentence);

			if (props != null)
			{
				String[] suffixes = factory.GetTagSuffixes();

				HashSet<String> whatToLoad = new HashSet<String>();

				for (String prop : props)
				{
					String pfx = factory.GetDefaultTagValue(prop);

					if (factory.getParser().TestParse(sentence + " " + pfx))
					{
						if (IsKeyword(prop))
						{
							String bb = BuildSmallestSentenceStartigWith(sentence + " " + pfx, "", "");
							return bb != null;
						}
					}
				}
			}
		}
		return false;
	}

	public final Object RewriteSentence(String str, tangible.OutObject<String> reformated)
	{
		if (IsKeyword(GetFirstWord(str)))
		{
			str = LoCase(str.trim());
		}

		try
		{
			tools.SYMBOL smb = factory.getParser().Parse(str);
			if (!factory.isParagraph(smb)) // get null on syntax error
			{
				reformated.argValue = str;
				return smb == null ? new tools.error(null) : smb;
			}
			else
			{
				if (!factory.ValidateSafeness(smb))
				{
					reformated.argValue = null;
					return new tools.error(null);
				}
				//TODO
				//var dl = factory.InvConvert(smb);
				//var en = GetENDLFromAst(dl);
				//reformated = UpCase(en.Trim());
				AnnotationManager annotManLoc;
				tangible.OutObject<cognipy.cnl.AnnotationManager> tempOut_annotManLoc = new tangible.OutObject<cognipy.cnl.AnnotationManager>();
				reformated.argValue = UpCase(factory.Serialize(smb, false, tempOut_annotManLoc).trim());
			annotManLoc = tempOut_annotManLoc.argValue;
				return null;
			}
		}
		catch (RuntimeException e)
		{
			reformated.argValue = str;
			return new tools.error(null);
		}
	}

	public final String GetTooltipDesc(Map.Entry<String, String> kv)
	{
		return factory.GetTooltipDesc(kv);
	}

	public final String GetKeywordTip(String ketag)
	{
		return factory.GetKeywordTip(ketag);
	}

	private static int lastNounIdx = 0;

	private String make_noun()
	{
		int r = rnd.nextInt(9);
		while (r == lastNounIdx)
		{
			r = rnd.nextInt(9);
		}
		lastNounIdx = r;
		switch (r)
		{
			case 0:
				return "cat";
			case 1:
				return "dog";
			case 2:
				return "monkey";
			case 3:
				return "giraffe";
			case 4:
				return "bird";
			case 5:
				return "fly";
			case 6:
				return "snake";
			case 7:
				return "elephant";
			default:
				return "mouse";
		}
	}

	private static int lastRoleIdx = 0;

	private String make_role()
	{
		int r = rnd.nextInt(6);
		while (r == lastRoleIdx)
		{
			r = rnd.nextInt(6);
		}
		lastRoleIdx = r;
		switch (r)
		{
			case 0:
				return "listen-to";
			case 1:
				return "love";
			case 2:
				return "like";
			case 3:
				return "dislike";
			case 4:
				return "eat";
			default:
				return "hate";
		}
	}

	private static int lastDataRoleIdx = 0;

	private String make_datarole()
	{
		int r = rnd.nextInt(6);
		while (r == lastDataRoleIdx)
		{
			r = rnd.nextInt(6);
		}
		lastDataRoleIdx = r;
		switch (r)
		{
			case 0:
				return "have-age";
			case 1:
				return "have-temperature";
			case 2:
				return "have-height";
			case 3:
				return "have-width";
			case 4:
				return "have-volume";
			default:
				return "have-speed";
		}
	}

	private static int lastBigNameIdx = 0;

	private String make_big_name()
	{
		int r = rnd.nextInt(10);
		while (r == lastBigNameIdx)
		{
			r = rnd.nextInt(10);
		}
		lastBigNameIdx = r;
		switch (r)
		{
			case 0:
				return "John";
			case 1:
				return "Mary";
			case 2:
				return "Mickey";
			case 3:
				return "Jerry";
			case 4:
				return "Leon";
			case 5:
				return "Paul";
			case 6:
				return "Caroline";
			case 7:
				return "Sylvia";
			case 8:
				return "Cloe";
			default:
				return "Rene";
		}
	}

	public final String GenerateAssert1()
	{
		cognipy.cnl.dl.NamedInstance tempVar2 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, DL.Statement.Modality.IS);
		tempVar2.name = make_big_name() + "^";
		cognipy.cnl.dl.NamedInstance tempVar3 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar3.name = make_big_name() + "^";
		cognipy.cnl.dl.InstanceSet(null, new cognipy.cnl.dl.InstanceList(null, tempVar2)), new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.InstanceSet(null, new cognipy.cnl.dl.InstanceList(null, tempVar3)), new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateValueAssert1()
	{
		cognipy.cnl.dl.NamedInstance tempVar2 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar.id = make_datarole();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, new cognipy.cnl.dl.InstanceSet(null, tempVar, new cognipy.cnl.dl.ValueSet(null, new cognipy.cnl.dl.ValueList(null, new cognipy.cnl.dl.Number(null, String.valueOf(rnd.nextInt(10)))))), DL.Statement.Modality.IS);
		tempVar2.name = make_big_name() + "^";
		cognipy.cnl.dl.NamedInstance tempVar3 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar3.name = make_big_name() + "^";
		cognipy.cnl.dl.InstanceList(null, tempVar3)), new cognipy.cnl.dl.SomeValueRestriction(null, new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.InstanceList(null, tempVar2)), new cognipy.cnl.dl.SomeValueRestriction(null, new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateAssert2()
	{
		cognipy.cnl.dl.NamedInstance tempVar2 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, new cognipy.cnl.dl.InstanceSet(null, tempVar, tempVar3), DL.Statement.Modality.IS);
		tempVar2.name = make_big_name() + "^";
		cognipy.cnl.dl.NamedInstance tempVar4 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar4.name = make_big_name() + "^";
		cognipy.cnl.dl.InstanceList(null, tempVar4)), new cognipy.cnl.dl.SomeRestriction(null, new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.InstanceList(null, tempVar2)), new cognipy.cnl.dl.SomeRestriction(null, new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateAssertOnly()
	{
		cognipy.cnl.dl.NamedInstance tempVar2 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, new cognipy.cnl.dl.InstanceSet(null, tempVar, tempVar3), DL.Statement.Modality.IS);
		tempVar2.name = make_big_name() + "^";
		cognipy.cnl.dl.NamedInstance tempVar4 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar4.name = make_big_name() + "^";
		cognipy.cnl.dl.InstanceList(null, tempVar4)), new cognipy.cnl.dl.OnlyRestriction(null, new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.InstanceList(null, tempVar2)), new cognipy.cnl.dl.OnlyRestriction(null, new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateNegativeAssert1()
	{
		cognipy.cnl.dl.NamedInstance tempVar2 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, new cognipy.cnl.dl.InstanceSet(null, tempVar), DL.Statement.Modality.IS);
		tempVar2.name = make_big_name() + "^";
		cognipy.cnl.dl.NamedInstance tempVar3 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar3.name = make_big_name() + "^";
		cognipy.cnl.dl.InstanceList(null, tempVar3)), new cognipy.cnl.dl.ConceptNot(null, new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.InstanceList(null, tempVar2)), new cognipy.cnl.dl.ConceptNot(null, new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateNegativeAssert2()
	{
		cognipy.cnl.dl.NamedInstance tempVar2 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, new cognipy.cnl.dl.InstanceSet(null, tempVar, new cognipy.cnl.dl.ConceptNot(null, tempVar3)), DL.Statement.Modality.IS);
		tempVar2.name = make_big_name() + "^";
		cognipy.cnl.dl.NamedInstance tempVar4 = new cognipy.cnl.dl.NamedInstance(null);
		tempVar4.name = make_big_name() + "^";
		cognipy.cnl.dl.InstanceList(null, tempVar4)), new cognipy.cnl.dl.SomeRestriction(null, new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.InstanceList(null, tempVar2)), new cognipy.cnl.dl.SomeRestriction(null, new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEvery1()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, tempVar2, DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEvery2()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.SomeRestriction(null, tempVar2, tempVar3), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEveryValue2()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_datarole();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.SomeValueRestriction(null, tempVar2, new cognipy.cnl.dl.BoundFacets(null, new cognipy.cnl.dl.FacetList(null, new cognipy.cnl.dl.Facet(null, "<", new cognipy.cnl.dl.Number(null, String.valueOf(rnd.nextInt(10))))))), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEveryOnly()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.OnlyRestriction(null, tempVar2, tempVar3), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEveryOnlyValue()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_datarole();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.OnlyValueRestriction(null, tempVar2, new cognipy.cnl.dl.BoundFacets(null, new cognipy.cnl.dl.FacetList(null, new cognipy.cnl.dl.Facet(null, "<", new cognipy.cnl.dl.Number(null, String.valueOf(rnd.nextInt(10))))))), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEvery3()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Atomic tempVar4 = new cognipy.cnl.dl.Atomic(null);
		tempVar4.id = make_role();
		cognipy.cnl.dl.Atomic tempVar5 = new cognipy.cnl.dl.Atomic(null);
		tempVar5.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.SomeRestriction(null, new cognipy.cnl.dl.RoleInversion(null, tempVar2), new cognipy.cnl.dl.ConceptAnd(null, tempVar3, new cognipy.cnl.dl.SomeRestriction(null, tempVar4, tempVar5))), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEquiv1()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_noun();
		cognipy.cnl.dl.Equivalence ex = new cognipy.cnl.dl.Equivalence(null, tempVar, tempVar2, DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEquiv2()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Equivalence ex = new cognipy.cnl.dl.Equivalence(null, tempVar, new cognipy.cnl.dl.SomeRestriction(null, tempVar2, tempVar3), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateDisjoint1()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.ConceptNot(null, tempVar2), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateDisjoint2()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, new cognipy.cnl.dl.ConceptNot(null, new cognipy.cnl.dl.SomeRestriction(null, tempVar2, tempVar3)), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEverySingle1()
	{
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, tempVar, DL.Statement.Modality.IS);
		tempVar2.id = make_noun();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_role() + "^";
		cognipy.cnl.dl.Atomic tempVar4 = new cognipy.cnl.dl.Atomic(null);
		tempVar4.id = make_role() + "^";
		cognipy.cnl.dl.Atomic tempVar5 = new cognipy.cnl.dl.Atomic(null);
		tempVar5.id = make_noun();
		cognipy.cnl.dl.SomeRestriction(null, tempVar3, tempVar2), new cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.SomeRestriction(null, tempVar4, tempVar5), new cognipy.cnl.dl.Atomic(null);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEverySingle2()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_role() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_noun();
		cognipy.cnl.dl.Atomic tempVar3 = new cognipy.cnl.dl.Atomic(null);
		tempVar3.id = make_role();
		cognipy.cnl.dl.Atomic tempVar4 = new cognipy.cnl.dl.Atomic(null);
		tempVar4.id = make_noun();
		cognipy.cnl.dl.Subsumption ex = new cognipy.cnl.dl.Subsumption(null, new cognipy.cnl.dl.SomeRestriction(null, tempVar, tempVar2), new cognipy.cnl.dl.SomeRestriction(null, tempVar3, tempVar4), DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateComplexRoleSubsumption(int n)
	{
		cognipy.cnl.dl.RoleChain chain = new cognipy.cnl.dl.RoleChain(null);
		chain.List = new ArrayList<cognipy.cnl.dl.Node>();
		for (int i = 0; i < n; i++)
		{
			cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
			tempVar.id = make_role() + (i == 0 ? "^" : "");
			chain.List.add(tempVar);
		}
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.ComplexRoleInclusion ex = new cognipy.cnl.dl.ComplexRoleInclusion(null, chain, tempVar2, DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateEquivalentRoles()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_role() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.RoleEquivalence ex = new cognipy.cnl.dl.RoleEquivalence(null, tempVar, tempVar2, DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateDisjointRoles()
	{
		cognipy.cnl.dl.Atomic tempVar = new cognipy.cnl.dl.Atomic(null);
		tempVar.id = make_role() + "^";
		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_role();
		cognipy.cnl.dl.RoleDisjoint ex = new cognipy.cnl.dl.RoleDisjoint(null, tempVar, tempVar2, DL.Statement.Modality.IS);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrlWithBuiltins1()
	{
		String t1 = make_noun();
		cognipy.cnl.dl.ID p1 = new cognipy.cnl.dl.ID(null);
		p1.setyytext(make_datarole());
		cognipy.cnl.dl.ID p2 = new cognipy.cnl.dl.ID(null);
		p2.setyytext(make_datarole());
		String t2 = make_noun();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext("val" + "_0");
		cognipy.cnl.dl.ID v3 = new cognipy.cnl.dl.ID(null);
		v3.setyytext(t2 + "_1");
		cognipy.cnl.dl.ID v4 = new cognipy.cnl.dl.ID(null);
		v4.setyytext("val" + "_1");
		cognipy.cnl.dl.ID rol = new cognipy.cnl.dl.ID(null);
		rol.setyytext(make_role() + "^");

		cognipy.cnl.dl.SwrlItemList tempVar3 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar3.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlDataProperty(null, p1, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v2)), new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v3)), new cognipy.cnl.dl.SwrlDataProperty(null, p2, new cognipy.cnl.dl.SwrlIVar(null, v3), new cognipy.cnl.dl.SwrlDVar(null, v4)), new cognipy.cnl.dl.SwrlBuiltIn(null, "<", new ArrayList<cognipy.cnl.dl.ISwrlObject>(Arrays.asList(new cognipy.cnl.dl.SwrlDVar(null, v2), new cognipy.cnl.dl.SwrlDVar(null, v4))))));
		cognipy.cnl.dl.SwrlItemList tempVar4 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar4.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlRole(null, rol, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlIVar(null, v3))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar3, tempVar4);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrlWithBuiltins2()
	{
		String t1 = make_noun();
		cognipy.cnl.dl.ID p1 = new cognipy.cnl.dl.ID(null);
		p1.setyytext(make_datarole());
		cognipy.cnl.dl.ID p2 = new cognipy.cnl.dl.ID(null);
		p2.setyytext(make_datarole());
		cognipy.cnl.dl.ID p3 = new cognipy.cnl.dl.ID(null);
		p3.setyytext(make_datarole());
		cognipy.cnl.dl.ID p4 = new cognipy.cnl.dl.ID(null);
		p4.setyytext(make_datarole() + "^");
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext("val" + "_0");
		cognipy.cnl.dl.ID v3 = new cognipy.cnl.dl.ID(null);
		v3.setyytext("val" + "_1");
		cognipy.cnl.dl.ID v4 = new cognipy.cnl.dl.ID(null);
		v4.setyytext("val" + "_2");
		cognipy.cnl.dl.ID v5 = new cognipy.cnl.dl.ID(null);
		v5.setyytext("val" + "_3");

		cognipy.cnl.dl.SwrlItemList tempVar2 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar2.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlDataProperty(null, p1, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v2)), new cognipy.cnl.dl.SwrlDataProperty(null, p2, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v3)), new cognipy.cnl.dl.SwrlDataProperty(null, p3, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v4)), new cognipy.cnl.dl.SwrlBuiltIn(null, "plus", new ArrayList<cognipy.cnl.dl.ISwrlObject>(Arrays.asList(new cognipy.cnl.dl.SwrlDVar(null, v2), new cognipy.cnl.dl.SwrlDVar(null, v3), new cognipy.cnl.dl.SwrlDVar(null, v4), new cognipy.cnl.dl.SwrlDVar(null, v5))))));
		cognipy.cnl.dl.SwrlItemList tempVar3 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar3.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlDataProperty(null, p4, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v5))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar2, tempVar3);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrlWithUnaryBuiltinNamed(String builtinname)
	{
		String t1 = make_noun();
		cognipy.cnl.dl.ID p1 = new cognipy.cnl.dl.ID(null);
		p1.setyytext(make_datarole());
		cognipy.cnl.dl.ID p4 = new cognipy.cnl.dl.ID(null);
		p4.setyytext(make_datarole() + "^");
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext("val" + "_0");
		cognipy.cnl.dl.ID v3 = new cognipy.cnl.dl.ID(null);
		v3.setyytext("val" + "_1");

		cognipy.cnl.dl.SwrlItemList tempVar2 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar2.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlDataProperty(null, p1, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v2)), new cognipy.cnl.dl.SwrlBuiltIn(null, builtinname, new ArrayList<cognipy.cnl.dl.ISwrlObject>(Arrays.asList(new cognipy.cnl.dl.SwrlDVar(null, v2), new cognipy.cnl.dl.SwrlDVar(null, v3))))));
		cognipy.cnl.dl.SwrlItemList tempVar3 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar3.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlDataProperty(null, p4, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v3))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar2, tempVar3);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrlWithBinaryBuiltinNamed(String builtinname)
	{
		String t1 = make_noun();
		cognipy.cnl.dl.ID p1 = new cognipy.cnl.dl.ID(null);
		p1.setyytext(make_datarole());
		cognipy.cnl.dl.ID p2 = new cognipy.cnl.dl.ID(null);
		p2.setyytext(make_datarole());
		cognipy.cnl.dl.ID p4 = new cognipy.cnl.dl.ID(null);
		p4.setyytext(make_datarole() + "^");
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext("val" + "_0");
		cognipy.cnl.dl.ID v3 = new cognipy.cnl.dl.ID(null);
		v3.setyytext("val" + "_1");
		cognipy.cnl.dl.ID v4 = new cognipy.cnl.dl.ID(null);
		v4.setyytext("val" + "_2");

		cognipy.cnl.dl.SwrlItemList tempVar2 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar2.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlDataProperty(null, p1, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v2)), new cognipy.cnl.dl.SwrlDataProperty(null, p2, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v3)), new cognipy.cnl.dl.SwrlBuiltIn(null, builtinname, new ArrayList<cognipy.cnl.dl.ISwrlObject>(Arrays.asList(new cognipy.cnl.dl.SwrlDVar(null, v2), new cognipy.cnl.dl.SwrlDVar(null, v3), new cognipy.cnl.dl.SwrlDVar(null, v4))))));
		cognipy.cnl.dl.SwrlItemList tempVar3 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar3.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlDataProperty(null, p4, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v4))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar2, tempVar3);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl8()
	{
		String t1 = make_noun();
		cognipy.cnl.dl.ID p1 = new cognipy.cnl.dl.ID(null);
		p1.setyytext(make_datarole() + "^");
		cognipy.cnl.dl.ID p2 = new cognipy.cnl.dl.ID(null);
		p2.setyytext(make_datarole());
		String t2 = make_noun();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext("val" + "_0");

		cognipy.cnl.dl.SwrlItemList tempVar2 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar2.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlDataProperty(null, p1, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v2))));
		cognipy.cnl.dl.SwrlItemList tempVar3 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar3.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlDataProperty(null, p2, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlDVar(null, v2))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar2, tempVar3);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl7()
	{
		String t0 = make_noun();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t0);
		cognipy.cnl.dl.Atomic c0 = new cognipy.cnl.dl.Atomic(null, tempVar);

		cognipy.cnl.dl.Atomic tempVar2 = new cognipy.cnl.dl.Atomic(null);
		tempVar2.id = make_datarole();
		cognipy.cnl.dl.OnlyValueRestriction c1 = new cognipy.cnl.dl.OnlyValueRestriction(null, tempVar2, new cognipy.cnl.dl.BoundFacets(null, new cognipy.cnl.dl.FacetList(null, new cognipy.cnl.dl.Facet(null, "<", new cognipy.cnl.dl.Number(null, String.valueOf(rnd.nextInt(10)))))));

		String t2 = make_noun() + "^";
		cognipy.cnl.dl.ID tempVar3 = new cognipy.cnl.dl.ID(null);
		tempVar3.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar3);

		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext("bnd_0");

		cognipy.cnl.dl.SwrlItemList tempVar4 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar4.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c0, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1))));
		cognipy.cnl.dl.SwrlItemList tempVar5 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar5.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v1))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar4, tempVar5);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl6()
	{
		String t1 = make_noun();
		String t2 = make_noun() + "^";
		String t3 = make_noun();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID tempVar3 = new cognipy.cnl.dl.ID(null);
		tempVar3.setyytext(t3);
		cognipy.cnl.dl.Atomic c3 = new cognipy.cnl.dl.Atomic(null, tempVar3);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");

		cognipy.cnl.dl.SwrlItemList tempVar4 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar4.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v1))));
		cognipy.cnl.dl.SwrlItemList tempVar5 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar5.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c3, new cognipy.cnl.dl.SwrlIVar(null, v1))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar4, tempVar5);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl5()
	{
		String t1 = make_noun();
		String t2 = make_noun() + "^";
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");

		cognipy.cnl.dl.SwrlItemList tempVar3 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar3.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1))));
		cognipy.cnl.dl.SwrlItemList tempVar4 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar4.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v1))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar3, tempVar4);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl4()
	{
		String t1 = make_noun();
		String t2 = make_noun();
		String r1 = make_role();
		String r2 = make_role();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext(t2 + "_0");
		cognipy.cnl.dl.ID tempVar3 = new cognipy.cnl.dl.ID(null);
		tempVar3.setyytext(r1 + "^");
		cognipy.cnl.dl.Atomic rol1 = new cognipy.cnl.dl.Atomic(null, tempVar3);
		cognipy.cnl.dl.ID tempVar4 = new cognipy.cnl.dl.ID(null);
		tempVar4.setyytext(r2);
		cognipy.cnl.dl.Atomic rol2 = new cognipy.cnl.dl.Atomic(null, tempVar4);

		cognipy.cnl.dl.SwrlItemList tempVar5 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar5.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v2)), new cognipy.cnl.dl.SwrlRole(null,rol1.id, new cognipy.cnl.dl.SwrlIVar(null, v1),new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlItemList tempVar6 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar6.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlRole(null,rol2.id, new cognipy.cnl.dl.SwrlIVar(null, v1),new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar5, tempVar6);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl3()
	{
		String t1 = make_noun();
		String r1 = make_role();
		String r2 = make_role();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_1");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext(t1 + "_2");
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(r1 + "^");
		cognipy.cnl.dl.Atomic rol1 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID tempVar3 = new cognipy.cnl.dl.ID(null);
		tempVar3.setyytext(r2);
		cognipy.cnl.dl.Atomic rol2 = new cognipy.cnl.dl.Atomic(null, tempVar3);

		cognipy.cnl.dl.SwrlItemList tempVar4 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar4.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v2)), new cognipy.cnl.dl.SwrlRole(null,rol1.id, new cognipy.cnl.dl.SwrlIVar(null, v1),new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlItemList tempVar5 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar5.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlRole(null,rol2.id, new cognipy.cnl.dl.SwrlIVar(null, v1),new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar4, tempVar5);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl2()
	{
		String t1 = make_noun();
		String t2 = make_noun();
		String t3 = make_noun();
		String r1 = make_role();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID tempVar3 = new cognipy.cnl.dl.ID(null);
		tempVar3.setyytext(t3);
		cognipy.cnl.dl.Atomic c3 = new cognipy.cnl.dl.Atomic(null, tempVar3);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext(t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext(t2 + "_0");
		cognipy.cnl.dl.ID tempVar4 = new cognipy.cnl.dl.ID(null);
		tempVar4.setyytext(r1 + "^");
		cognipy.cnl.dl.Atomic rol1 = new cognipy.cnl.dl.Atomic(null, tempVar4);

		cognipy.cnl.dl.SwrlItemList tempVar5 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar5.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v2)), new cognipy.cnl.dl.SwrlRole(null,rol1.id, new cognipy.cnl.dl.SwrlIVar(null, v1),new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlItemList tempVar6 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar6.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c3, new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar5, tempVar6);
		return GetENDLFromAst(ex);
	}

	public final String GenerateSwrl1()
	{
		String t1 = make_noun() + "^";
		String t2 = make_noun();
		String t3 = make_noun();
		cognipy.cnl.dl.ID tempVar = new cognipy.cnl.dl.ID(null);
		tempVar.setyytext(t1);
		cognipy.cnl.dl.Atomic c1 = new cognipy.cnl.dl.Atomic(null, tempVar);
		cognipy.cnl.dl.ID tempVar2 = new cognipy.cnl.dl.ID(null);
		tempVar2.setyytext(t2);
		cognipy.cnl.dl.Atomic c2 = new cognipy.cnl.dl.Atomic(null, tempVar2);
		cognipy.cnl.dl.ID tempVar3 = new cognipy.cnl.dl.ID(null);
		tempVar3.setyytext(t3);
		cognipy.cnl.dl.Atomic c3 = new cognipy.cnl.dl.Atomic(null, tempVar3);
		cognipy.cnl.dl.ID v1 = new cognipy.cnl.dl.ID(null);
		v1.setyytext("?" + t1 + "_0");
		cognipy.cnl.dl.ID v2 = new cognipy.cnl.dl.ID(null);
		v2.setyytext("?" + t2 + "_0");

		cognipy.cnl.dl.SwrlItemList tempVar4 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar4.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c1, new cognipy.cnl.dl.SwrlIVar(null, v1)), new cognipy.cnl.dl.SwrlInstance(null, c2, new cognipy.cnl.dl.SwrlIVar(null, v2)), new cognipy.cnl.dl.SwrlSameAs(null, new cognipy.cnl.dl.SwrlIVar(null, v1), new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlItemList tempVar5 = new cognipy.cnl.dl.SwrlItemList(null);
		tempVar5.list = new ArrayList<cognipy.cnl.dl.SwrlItem>(Arrays.asList(new cognipy.cnl.dl.SwrlInstance(null, c3, new cognipy.cnl.dl.SwrlIVar(null, v2))));
		cognipy.cnl.dl.SwrlStatement ex = new cognipy.cnl.dl.SwrlStatement(null, tempVar4, tempVar5);
		return GetENDLFromAst(ex);
	}
}