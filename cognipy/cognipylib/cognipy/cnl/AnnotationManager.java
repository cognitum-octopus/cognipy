package cognipy.cnl;

import cognipy.*;
import java.util.*;
import java.time.*;

public class AnnotationManager
{
	public AnnotationManager()
	{
	}

	private AnnotationManager(HashMap<String, ArrayList<W3CAnnotation>> _annotatedElements, HashMap<String, String> subjectType, HashMap<String, Tuple<String, String>> pfx2NsInLoadedAnnotations)
	{
		this._annotatedElements = _annotatedElements;
		this.subjectType = subjectType;
		this.pfx2NsInLoadedAnnotations = pfx2NsInLoadedAnnotations;
	}

	public static String ANNOTATION_START = "Annotations:";
	// regex to extract the w3c annotations. These are written in the Annotations: part.
	// this regex expect something like: annotatedConcept: annotationType value@language
	// value can be: a string ("'), a number, a date (written with -).
	private static String allSubjectQuotedStart = "(\"|{\"|The-\"|THE-\"|the-\"|_\")";
	private static String allPossibleSubjectEnd = "(\"|\"}|\")";
	private static String allPossibleReferenceEnding = "\\[[^\\s\\]]*\\]|:[^\\s]*";
	public static Regex w3cAnnotRg = new Regex("^\\s*(\\r\\n)?(?<annotated>((" + allSubjectQuotedStart + "?[^\"]*" + allPossibleSubjectEnd + ")(" + allPossibleReferenceEnding + ")?\\s|([\\S]*)))(?<annotatedKind>[^:]*):\\s*(?<type>\\S*)\\s+[\"']?(?<value>(((?<=[\"'])([^\"']|\\\\'|\\\\\")*(?=[\"'])))|([0-9-\\.,]*))[\"']?(@(?<language>[a-zA-Z-]*))?\\s*($|(?<dot>\\.))", RegexOptions.Compiled.getValue() | RegexOptions.Multiline.getValue());

	private HashMap<String, String> subjectType = new HashMap<String, String>();
	private HashMap<String, ArrayList<W3CAnnotation>> _annotatedElements = new HashMap<String, ArrayList<W3CAnnotation>>();

	// TODO [AnnotationManager]: This commented part can be used to standardize the DL sentences we are storing inside the annotationManager. 
	// the problem with this is that after standardizing it the string will not be in DL so we cannot parse it as a DL string.....
	// this is a problem because if someone is reading the content of the annotation manager from outside it will get incorrect DL string.
	// One way to go would be to keep two versions: a standardized one internally and the DL string. 
	// To check if a subject is contained in the manager, we should compare standardized strings
	// To return outside we should always use the DL string.
	/** /////////////////////// FROM CNL TOOLS //////////////////////////////////////
	*/
	//private Tools.Parser dlParser = new Ontorion.CNL.DL.dl();
	//private Ontorion.CNL.DL.Paragraph GetDLAst(string text, bool throwOnError = true)
	//{
	//    if (text.Trim() == "") return null;
	//    Tools.SYMBOL smb = dlParser.Parse(text);
	//    if (!(smb is Ontorion.CNL.DL.Paragraph))   // get null on syntax error
	//    {
	//        if (smb is Tools.error)
	//        {
	//            if (throwOnError)
	//                throw new ParseException((smb as Tools.error).ToString(), (smb as Tools.error).Line, (smb as Tools.error).Position, (smb as Tools.error).pos, text);
	//        }
	//        return null;
	//    }
	//    else
	//    {
	//        return smb as Ontorion.CNL.DL.Paragraph;
	//    }
	//}
	//////////////////////////// FROM CNL TOOLS END ///////////////////////////////////////// 

	//private string GetStandardizedDLString(string dlString)
	//{
	//    var par = GetDLAst(dlString,false);
	//    if (par == null)
	//        return dlString;
	//    var ser = new Ontorion.CNL.DL.StandardizedSerializer();
	//    return ser.Serialize(par);
	//}

	public final boolean ContainsAnnotationSubject(String annotSubj)
	{
		//if (_annotatedElements.ContainsKey(GetStandardizedDLString(annotSubj)))
		if (_annotatedElements.containsKey(annotSubj))
		{
			return true;
		}
		else if (_annotatedElements.containsKey(annotSubj.replace("\r\n", ""))) // maybe it was a Statement with \r\n? (internall not \r\n is kept)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/** 
	 Loads the W3CAnnotations found in the line. By default the annotationManager is using DL internally if the text you are giving is not in DL, use the inputTranslator argument
	 If the subject of an annotation is a Statement, then the Statement is expected to be written like: "statement where quote is ''"
	 
	 @param line
	 @param append Decides if the text should be appended to the annotationManager or not
	 @param inputTranslator Used internally to translate for CNL to DL# Use it if the input is not in DL# From ENCNL, the translator is: x => Ontorion#CNL#EN#ENNameingConvention#ToDL(new Ontorion.CNL.EN.EnName() { id = x }, Ontorion.CNL.EN.endict.WordKind.NormalForm).id 
	 @return 
	*/

	public final void loadW3CAnnotationsFromText(String line, boolean append)
	{
		loadW3CAnnotationsFromText(line, append, null);
	}

	public final void loadW3CAnnotationsFromText(String line)
	{
		loadW3CAnnotationsFromText(line, false, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void loadW3CAnnotationsFromText(string line, bool append = false, Func<string, string> inputTranslator = null)
	public final void loadW3CAnnotationsFromText(String line, boolean append, tangible.Func1Param<String, String> inputTranslator)
	{
		if (tangible.StringHelper.isNullOrEmpty(line) || tangible.StringHelper.isNullOrWhiteSpace(line) || !line.contains(":"))
		{
			return;
		}

		AnnotationManager localAnnotationManager = new AnnotationManager();

		int dp = 0;
		if (line.contains(ANNOTATION_START))
		{
			dp = line.indexOf(':') + 1;
		}

		boolean newAnnotationSubjects = false;
		System.Text.RegularExpressions.MatchCollection refs = w3cAnnotRg.Matches(line.substring(dp).trim());
		for (Match match : refs)
		{
			String annotated = match.Groups["annotated"].Value;
			String type = match.Groups["type"].Value;
			String val = match.Groups["value"].Value;
			String kind = match.Groups["annotatedKind"].Value;
			// if one of these values are null this means that something went wrong during parsing... someone changed it manually?
			// in this case we skip this line.
			if (tangible.StringHelper.isNullOrWhiteSpace(annotated) || tangible.StringHelper.isNullOrWhiteSpace(type) || tangible.StringHelper.isNullOrWhiteSpace(val) || tangible.StringHelper.isNullOrWhiteSpace(kind))
			{
				continue;
			}

			cognipy.ars.EntityKind res = ParseSubjectKind(kind);
			annotated = annotated.trim();
			type = type.trim();

			if (inputTranslator != null)
			{
				if (res != ARS.EntityKind.Statement && !CNLTools.isSurelyDLEntity(annotated, res))
				{
					annotated = inputTranslator.invoke(annotated);
				}

				if (!CNLTools.isSurelyDLEntity(type, ARS.EntityKind.Role))
				{
					type = inputTranslator.invoke(type);
				}
			}

			// if the subject is a statement, then the annotation manager keeps it internally as: statement with quotes inside (no quotes around!)
			if (res == ARS.EntityKind.Statement && annotated.startsWith("\"") && annotated.endsWith("\""))
			{
				annotated = annotated.substring(1, 1 + annotated.length() - 2).replace("\''", "\"");
			}
			else if (res == ARS.EntityKind.Statement && !annotated.startsWith("\"") && !annotated.endsWith("\"")) //statement should be quoted! If not, continue.
			{
				continue;
			}

			if (!localAnnotationManager.ContainsAnnotationSubject(annotated.trim()))
			{
				newAnnotationSubjects = true;
			}
			W3CAnnotation tempVar = new W3CAnnotation(true);
			tempVar.setType(type.trim());
			tempVar.setValue(val.trim());
			tempVar.setLanguage(match.Groups["language"].Value);
			localAnnotationManager.appendAnnotations(annotated.trim(), kind.trim(), new ArrayList<W3CAnnotation>(Arrays.asList( tempVar )));
		}

		if (newAnnotationSubjects && NewAnnotationSubject != null && FireNewSubjectEvent)
		{
			for (NewAnnotationSubjectHandler listener : NewAnnotationSubject.listeners())
			{
				listener.invoke(this);
			}
		}

		if (!append)
		{
			this.clearAnnotations();
		}

		appendAnnotations(localAnnotationManager);

		if (!append)
		{
			this.AssumeNotModifiedNow();
		}
	}

	/** 
	 Appends all annotations contained in annotExt into the currently loaded annotations.
	 
	 @param annotExt Dictionary&lt;subject,list of annotations&gt;
	*/
	private void appendAnnotations(HashMap<String, ArrayList<W3CAnnotation>> annotExt)
	{
		if (annotExt == null)
		{
			return;
		}

		for (Map.Entry<String, ArrayList<W3CAnnotation>> ann : annotExt.entrySet())
		{
			appendAnnotations(ann.getKey(), null, ann.getValue());
		}
	}
	/** 
	 Append all annotations contained in dlannot to the annotations currently loaded into this Annotation Manager
	 
	 @param subj subject of the annotations to load
	 @param subjType type of the subject (role, instance,...)
	 @param dlannot list of annotation to append.
	 @param recordChanges If true all annotations that are added will be recorded into the AddedAnnotations
	*/
	public final void appendAnnotations(String subj, String subjType, ArrayList<W3CAnnotation> dlannot)
	{
		if (dlannot != null && !dlannot.isEmpty())
		{
			if (!_annotatedElements.containsKey(subj))
			{
				//if (subjType == ARS.EntityKind.Statement.ToString())
				//    subj = GetStandardizedDLString(subj);
				_annotatedElements.put(subj, new ArrayList<W3CAnnotation>());
				if (subjectType != null)
				{
					subjectType.put(subj, subjType);
				}
				if (NewAnnotationSubject != null && FireNewSubjectEvent)
				{
					for (NewAnnotationSubjectHandler listener : NewAnnotationSubject.listeners())
					{
						listener.invoke(this);
					}
				}
			}
			for (W3CAnnotation ann : dlannot)
			{
				if (!_annotatedElements.get(subj).contains(ann))
				{
					if (!ann.getIsDL())
					{
						throw new RuntimeException("The annotation manager can be used only with DL annotations.");
					}
					W3CAnnotation tempVar = new W3CAnnotation(true);
					tempVar.setType(ann.getType());
					tempVar.setValue(ann.getValue());
					tempVar.setLanguage(ann.getLanguage());
					tempVar.setExternal(ann.getExternal());
					_annotatedElements.get(subj).add(tempVar);

					if (regxForPrefixes.IsMatch(ann.getType()))
					{
						String prefix = regxForPrefixes.Match(ann.getType()).toString();
						prefix = prefix.trim();
						if (wellKnownPrefixToNamespace.containsKey(prefix))
						{
							if (!pfx2NsInLoadedAnnotations.containsKey(prefix) && !prefix.startsWith("<") && !prefix.endsWith(">"))
							{
								pfx2NsInLoadedAnnotations.put(prefix, new Tuple<String, String>(wellKnownPrefixToNamespace.get(prefix), ""));
							}
						}
						else if (!prefix.startsWith("<") && !prefix.endsWith(">"))
						{
							unknownPrefixes.add(prefix);
						}
					}
				}
			}
			_isModified = true;
		}
	}

	/** 
	 To add annotations. ! if the type is a statement, the subject should NOT be quoted!
	 
	 @param subj
	 @param subjType
	 @param dlannot
	*/
	public final void Add(String subjExt, String subjType, ArrayList<W3CAnnotation> dlannot)
	{
		String subj = subjExt;
		if (ARS.EntityKind.Statement.toString().equals(subjType))
		{
			subj = subj.replace("\r\n", "").replace("''", "\"");
		}

		appendAnnotations(subj, subjType, dlannot);
	}

	/** 
	 Appends all the annotations present in the annotMan to the annotations currently loaded in this annotation Manager.
	 
	 @param annotMan
	*/
	public final void appendAnnotations(AnnotationManager annotMan)
	{
		if (annotMan == null)
		{
			return;
		}

		for (Map.Entry<String, String> ann : annotMan.GetAnnotationSubjects().entrySet())
		{
			appendAnnotations(ann.getKey(), ann.getValue(), annotMan.GetAnnotations(ann.getKey()));
		}
	}

	/** 
	 Appends a DL annotation axiom into the current annotations.
	 ! If the subject is a statement, it is expected without \r\n and NOT inside quotes!
	 
	 @param dLAnnotationAxiom
	*/
	public final void appendAnnotations(cognipy.cnl.dl.DLAnnotationAxiom dLAnnotationAxiom)
	{
		if (!tangible.StringHelper.isNullOrWhiteSpace(dLAnnotationAxiom.value) && !tangible.StringHelper.isNullOrWhiteSpace(dLAnnotationAxiom.annotName) && !tangible.StringHelper.isNullOrWhiteSpace(dLAnnotationAxiom.getSubject()))
		{
			cognipy.ars.EntityKind knd = ParseSubjectKind(dLAnnotationAxiom.getSubjKind());
			String subj = dLAnnotationAxiom.getSubject();
			W3CAnnotation tempVar = new W3CAnnotation(true);
			tempVar.setType(dLAnnotationAxiom.annotName);
			tempVar.setValue(dLAnnotationAxiom.value);
			tempVar.setLanguage(!tangible.StringHelper.isNullOrWhiteSpace(dLAnnotationAxiom.language) ? dLAnnotationAxiom.language : "");
			appendAnnotations(subj, dLAnnotationAxiom.getSubjKind(), new ArrayList<W3CAnnotation>(Arrays.asList( tempVar )));
		}
	}

	/** 
	 Returns a string in which all the annotations currently loaded are serialized.
	 
	 @return Serialized annotations.
	*/
	public final String SerializeAnnotations()
	{
		StringBuilder annots = new StringBuilder();
		if (_annotatedElements.size() != 0)
		{
			annots.append("Annotations:" + "\r\n");
			for (Map.Entry<String, ArrayList<W3CAnnotation>> annotEl : _annotatedElements.entrySet())
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var subj = annotEl.getKey();
				if (ARS.EntityKind.Statement.toString().equals(subjectType.get(annotEl.getKey())))
				{
					subj = "\"" + subj.Replace("\"", "''") + "\"";
				}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var annotContain : annotEl.getValue())
				{
					annots.append(subj + " " + subjectType.get(annotEl.getKey()) + ": " + annotContain.toString() + "\r\n");
				}
			}
			annots.append(".");
		}
		return annots.toString();
	}

	/** 
	 here we store the prefix to namespace that are generally used.
	 TODO ALESSANDRO the namespaces should be added automatically when importing from OWL! but right now this is not done....
	*/
	private HashMap<String, String> wellKnownPrefixToNamespace = new HashMap<String, String>(Map.ofEntries(Map.entry("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), Map.entry("rdfs", "http://www.w3.org/2000/01/rdf-schema#"), Map.entry("owl", "http://www.w3.org/2002/07/owl#"), Map.entry("dcterms", "http://purl.org/dc/terms/"), Map.entry("skos", "http://www.w3.org/2004/02/skos/core#")));

	private Regex regxForPrefixes = new Regex("(?<=\\[).*?(?=\\])");
	private HashMap<String, Tuple<String, String>> pfx2NsInLoadedAnnotations = new HashMap<String, Tuple<String, String>>();
	/** 
	 Returns the Pfx2Ns map created on the basis of the annotations loaded into the AnnotationManager.
	 !! Beware that only known annotations are loaded into the Pfx2Ns map.
	 
	 @return 
	*/
	public final HashMap<String, Tuple<String, String>> getPfx2NsDefinedInLoadedAnnotations()
	{
		return pfx2NsInLoadedAnnotations;
	}

	private ArrayList<String> unknownPrefixes = new ArrayList<String>();
	/** 
	 Returns the unknown prefixes found in the annotations
	 
	 @return 
	*/
	public final ArrayList<String> getUnknownPrefixesInAnnotations()
	{
		return unknownPrefixes;
	}

	public static ArrayList<String> SerializeAnnotations(String annotSubj, String subjType, ArrayList<W3CAnnotation> annotEl)
	{
		String subj = annotSubj;
		if (ARS.EntityKind.Statement.toString().equals(subjType))
		{
			subj = "\"" + subj.replace("\"", "''") + "\"";
		}

		ArrayList<String> allAnnot = new ArrayList<String>();
		for (W3CAnnotation ann : annotEl)
		{
			allAnnot.add(subj + " " + subjType + ": " + ann.toString());
		}
		return allAnnot;
	}

	public final void clearAnnotations()
	{
		_annotatedElements = new HashMap<String, ArrayList<W3CAnnotation>>();
		subjectType = new HashMap<String, String>();
		pfx2NsInLoadedAnnotations = new HashMap<String, Tuple<String, String>>();
		AssumeNotModifiedNow();
	}

	public final AnnotationManager Copy()
	{
		return new AnnotationManager(_annotatedElements, subjectType, pfx2NsInLoadedAnnotations);
	}

	/** 
	 Compare the annotations loaded in this annotation manager to the annotations loaded in the annotNew.
	 
	 @param annotNew Manager to which you want to compare the current annotations
	 @return An annotations manager diff structure which contains the difference between the two annotation managers
	*/
	public final AnnotationManagerDiff Compare(AnnotationManager annotNew)
	{
		AnnotationManagerDiff diffMan = new AnnotationManagerDiff();
		// get the difference between the keys of the two managers.
		HashSet<String> keysInRef = new HashSet<String>(this.GetAnnotationSubjects().Select(x -> x.Key).ToList());
		HashSet<String> keysInNew = new HashSet<String>(annotNew.GetAnnotationSubjects().Select(x -> x.Key).ToList());
		// all keys present in annotReference and not in annotNew should be added in removedAnnotations
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var keysInRefNotInNew = keysInRef.Except(keysInNew);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var annKey : keysInRefNotInNew)
		{
			diffMan.getRemovedAnnotations().put(new Tuple<String, String>(annKey, this.subjectType.get(annKey)), this.GetAnnotations(annKey));
		}
		// all keys present in annotNew and not in annotReference should be added in addedAnnotations
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var keysInNewNotInRef = keysInNew.Except(keysInRef);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var annKey : keysInNewNotInRef)
		{
			diffMan.getAddedAnnotations().put(new Tuple<String, String>(annKey, annotNew.subjectType.get(annKey)), annotNew.GetAnnotations(annKey));
		}

		// if they have both the same key --> check if each annotation is the same
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var keysInBoth = keysInRef.Intersect(keysInNew);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var annKey : keysInBoth)
		{
			HashSet<W3CAnnotation> annInRef = new HashSet<W3CAnnotation>(this.GetAnnotations(annKey));
			HashSet<W3CAnnotation> annInNew = new HashSet<W3CAnnotation>(annotNew.GetAnnotations(annKey));
			ArrayList<Object> annInRefNotInNew = annInRef.Except(annInNew, new W3CAnnotation(true)).ToList();
			if (!annInRefNotInNew.isEmpty())
			{
				diffMan.getRemovedAnnotations().put(new Tuple<String, String>(annKey, this.subjectType.get(annKey)), annInRefNotInNew);
			}
			ArrayList<Object> annInNewNotInRef = annInNew.Except(annInRef, new W3CAnnotation(true)).ToList();
			if (!annInNewNotInRef.isEmpty())
			{
				diffMan.getAddedAnnotations().put(new Tuple<String, String>(annKey, annotNew.subjectType.get(annKey)), annInNewNotInRef);
			}
		}

		return diffMan;
	}

	/** 
	 Update the annotation for subj by changing the old annotation with the new one. If the newW3CEl is null --> the old one is removed.
	 If oldW3CEl is null --> all annotations relative to subj are removed.
	 
	 @param subj
	 @param oldW3CEl
	 @param newW3CEl If null, only remove the oldOne
	 @return 
	*/

	public final boolean UpdateAnnotation(String subj, W3CAnnotation oldW3CEl)
	{
		return UpdateAnnotation(subj, oldW3CEl, null);
	}

	public final boolean UpdateAnnotation(String subj)
	{
		return UpdateAnnotation(subj, null, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public bool UpdateAnnotation(string subj, W3CAnnotation oldW3CEl = null, W3CAnnotation newW3CEl = null)
	public final boolean UpdateAnnotation(String subj, W3CAnnotation oldW3CEl, W3CAnnotation newW3CEl)
	{
		if (oldW3CEl != null && newW3CEl != null && oldW3CEl.equals(newW3CEl))
		{
			return false;
		}

		if (_annotatedElements.containsKey(subj))
		{
			if (oldW3CEl == null)
			{
				_annotatedElements.remove(subj);
				subjectType.remove(subj);
				_isModified = true;
				return true;
			}

			if (_annotatedElements.get(subj).contains(oldW3CEl))
			{
				_annotatedElements.get(subj).remove(oldW3CEl);
				if (newW3CEl != null)
				{
					_annotatedElements.get(subj).add(newW3CEl);
				}
				else if (_annotatedElements.get(subj).isEmpty())
				{
					_annotatedElements.remove(subj);
					subjectType.remove(subj);
				}
				_isModified = true;
				return true;
			}
		}
		return false;
	}

	//TODO ALESSANDRO this flag can be used to check if the annotation manager has been changed from the last save.
	// it should be reset when saving the cnl file and it should be checked when the window is closed.
	private boolean _isModified = false;
	/** 
	 True if the annotations have been modified from the last time that AssumeNotModifiedNow has been called.
	*/
	public final boolean getIsModified()
	{
		return _isModified;
	}
	public final void AssumeNotModifiedNow()
	{
		_isModified = false;
	}

	private void UpdatePossibleENCNLLabelsAndLang(tangible.Func1Param<String, String> inputTranslator)
	{
		possibleLabels = new ArrayList<String>();
		possibleLang = new ArrayList<String>();
		for (Map.Entry<String, ArrayList<W3CAnnotation>> annotEl : _annotatedElements.entrySet())
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var labl : annotEl.getValue())
			{
				if (!possibleLabels.contains(labl.Type))
				{
					possibleLabels.add(inputTranslator.invoke(labl.Type));
				}
				if (!possibleLang.contains(labl.Language))
				{
					possibleLang.add(labl.Language);
				}
			}
		}
	}

	/** 
	 event that is fired when a new annotation subject is added.
	*/
	public tangible.Event<NewAnnotationSubjectHandler> NewAnnotationSubject = new tangible.Event<NewAnnotationSubjectHandler>();

	private ArrayList<String> possibleLabels = null;
	/** 
	 searches in the currently loaded annotations all the possible types
	 
	 @param inputTranslator This is needed to translate the annotations (internally stored in DL) to the ouput language. From ENCNL, the translator is: x => Ontorion.CNL.EN.ENNameingConvention.ToDL(new Ontorion.CNL.EN.EnName() { id = x }, Ontorion.CNL.EN.endict.WordKind.NormalForm).id  
	 @return 
	*/
	public final ArrayList<String> getPossibleTypesENCNL(tangible.Func1Param<String, String> inputTranslator)
	{
		if (getIsModified() || possibleLabels == null)
		{
			UpdatePossibleENCNLLabelsAndLang(inputTranslator);
		}
		return possibleLabels;
	}

	private ArrayList<String> possibleLang = null;
	public boolean FireNewSubjectEvent = true;
	/** 
	 Searches in the currently loaded annotations all the possible languages.
	 
	 @param inputTranslator This is needed to translate the annotations (internally stored in DL) to the ouput language. From ENCNL, the translator is: x => Ontorion.CNL.EN.ENNameingConvention.ToDL(new Ontorion.CNL.EN.EnName() { id = x }, Ontorion.CNL.EN.endict.WordKind.NormalForm).id  
	 @return 
	*/
	public final ArrayList<String> getPossibleLang(tangible.Func1Param<String, String> inputTranslator)
	{
		if (getIsModified() || possibleLang == null)
		{
			UpdatePossibleENCNLLabelsAndLang(inputTranslator);
		}
		return possibleLang;
	}

	public final ArrayList<W3CAnnotation> GetAnnotations(String annotSubj)
	{
		//var subj = GetStandardizedDLString(annotSubj);
		String subj = annotSubj;
		if (_annotatedElements.containsKey(subj))
		{
			return _annotatedElements.get(subj);
		}
		else if (_annotatedElements.containsKey(subj.replace("\r\n", "")))
		{
			return _annotatedElements.get(subj.replace("\r\n", ""));
		}
		else
		{
			return null;
		}
	}

	/** 
	 Returns the dictionary with (subject,subjectType)
	 
	 @return 
	*/
	public final HashMap<String, String> GetAnnotationSubjects()
	{
		return new HashMap<String, String>(subjectType);
	}

	/** 
	 Returns all annotations contained in the manager as DLAnnotationAxiom.
	 
	 @param pfx2ns If not null, it will be used to transform each prefix found to the full namespace.
	 @return 
	*/

	public final java.util.HashMap<ARS.EntityKind, java.util.ArrayList<DL.DLAnnotationAxiom>> getDLAnnotationAxioms()
	{
		return getDLAnnotationAxioms(null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Dictionary<ARS.EntityKind, List<DL.DLAnnotationAxiom>> getDLAnnotationAxioms(Func<string, string> pfx2ns = null)
	public final HashMap<ARS.EntityKind, ArrayList<cognipy.cnl.dl.DLAnnotationAxiom>> getDLAnnotationAxioms(tangible.Func1Param<String, String> pfx2ns)
	{
		HashMap<ARS.EntityKind, ArrayList<cognipy.cnl.dl.DLAnnotationAxiom>> dlAnnotatedAxioms = new HashMap<ARS.EntityKind, ArrayList<cognipy.cnl.dl.DLAnnotationAxiom>>();
		for (Map.Entry<String, ArrayList<W3CAnnotation>> annotKv : _annotatedElements.entrySet())
		{
			cognipy.ars.EntityKind kind = ParseSubjectKind(subjectType.get(annotKv.getKey()));
			if (!dlAnnotatedAxioms.containsKey(kind))
			{
				dlAnnotatedAxioms.put(kind, new ArrayList<cognipy.cnl.dl.DLAnnotationAxiom>());
			}
			String dlSubj = CNLTools.DLToFullUri(annotKv.getKey(), kind, pfx2ns);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var annotEl : annotKv.getValue())
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var nameToUse = annotEl.Type;

				dlAnnotatedAxioms.get(kind).add(new cognipy.cnl.dl.DLAnnotationAxiom(null, dlSubj, subjectType.get(annotKv.getKey()), CNLTools.DLToFullUri(annotEl.Type, ARS.EntityKind.Role, pfx2ns), annotEl.Language, (String)annotEl.Value));
			}
		}
		return dlAnnotatedAxioms;
	}

	public static cognipy.ars.EntityKind ParseSubjectKind(String kind)
	{
		cognipy.ars.EntityKind result;
		tangible.OutObject<TEnum> tempOut_result = new tangible.OutObject<TEnum>();
		if (Enum.TryParse(kind, true, tempOut_result))
		{
		result = tempOut_result.argValue;
			return result;
		}
		else
		{
		result = tempOut_result.argValue;
			throw new RuntimeException("Could not parse " + kind + " to an EntityKind.");
		}
	}
}