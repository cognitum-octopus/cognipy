package cognipy;

import cognipy.ars.*;
import cognipy.cnl.*;
import cognipy.executing.hermitclient.*;
import cognipy.models.*;
import cognipy.splitting.*;
import java.util.*;

public class CogniPySvr implements ICogniPySvr
{

	private cognipy.cnl.dl.Paragraph paragraph;
	private HermiTReasoningService _reasoner = null;
	private HermiTReasoningService getReasoner()
	{
		if (_reasoner == null)
		{
			throw new IllegalStateException("The reasoner is not initialized. You need to call Load... before calling this method.");
		}
		else
		{
			return _reasoner;
		}
	}
	private void setReasoner(HermiTReasoningService value)
	{
		_reasoner = value;
	}

	private cognipy.ReferenceManager.ReferenceTags tags = new cognipy.ReferenceManager.ReferenceTags();
	private CNLTools tools = null;

	private HashMap<String, String> AllReferences;
	//        static SpellFactory engine = null;
	//        static object engineGuard = new object();
	//        static bool engineLoaded = false;

	private boolean debugModeOn = false;
	private boolean modalChecker = false;
	private boolean alreadyMaterialized = false;
	private boolean PassParamsInCNL;
	public final boolean getPassParamsInCNL()
	{
		return PassParamsInCNL;
	}
	public final void setPassParamsInCNL(boolean value)
	{
		PassParamsInCNL = value;
	}
	private String ontologyBase;
	private IOwlNameingConvention namc = new OwlNameingConventionCamelCase();
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public dynamic Outer;
	public boolean SWRLOnly = false;

	private HashSet<String> objectroles = new HashSet<String>();
	private HashSet<String> dataroles = new HashSet<String>();
	private HashSet<String> instances = new HashSet<String>();
	private HashSet<String> concepts = new HashSet<String>();
	private HashSet<String> datatypes = new HashSet<String>();
	private HashMap<String, HashMap<String, ArrayList<AnnotationResult>>> annotations = new HashMap<String, HashMap<String, ArrayList<AnnotationResult>>>();

	private static String KWDBEG = "\\b(?<!(\\-|[A-z]|[0-9]))";
	private static String KWDEND = "(?!\\-)\\b";
	private static String KWDTHE = "(the-\".*\"\\s*)";
	private static Regex kwds = null;

	private static DLToOWLNameConv pfxman = new DLToOWLNameConv();

	static
	{
		{
		//remove log4j appender warning
			org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
			org.apache.log4j.ConsoleAppender app = new org.apache.log4j.ConsoleAppender(new org.apache.log4j.PatternLayout(org.apache.log4j.PatternLayout.DEFAULT_CONVERSION_PATTERN));
			app.setThreshold(org.apache.log4j.Priority.FATAL);
			root.addAppender(app);
		}

	}

	public CogniPySvr()
	{
		java.lang.Class tpy = cognipy.cnl.en.CNLFactory.class;
		CNLTools.RegisterCNLFactory("en", tpy);
		AllReferences = new HashMap<String, String>();
		tools = new CNLTools("en");

		//lock (engineGuard)
		//{
		//    if (engineLoaded)
		//        return;
		//    LanguageConfig enConfig = new LanguageConfig();
		//    enConfig.LanguageCode = "en";
		//    enConfig.Processors = 1;
		//    enConfig.HunspellAffFile = "en_US.aff";
		//    enConfig.HunspellDictFile = "en_US.dic";
		//    enConfig.HunspellKey = "";
		//    enConfig.HyphenDictFile = "hyph_en_US.dic";
		//    //enConfig. MyThesIdxFile = "th_en_US_new.idx";
		//    enConfig.MyThesDatFile = "th_en_US_new.dat";
		//    Hunspell.NativeDllPath = "";
		//    engine = new SpellFactory(enConfig);
		//    engineLoaded = true;
		//}
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		kwds = new Regex(KWDBEG + "(" + tangible.StringHelper.join("|", from k in tools.GetAllKeywords() select Regex.Escape(k)) + "|" + KWDTHE + ")" + KWDEND + "|\\.|,|\\(|\\)|\\'", System.Text.RegularExpressions.RegexOptions.IgnoreCase.getValue() | RegexOptions.ExplicitCapture.getValue());

	}

	//static IEnumerable<string> GetForms(string word)
	//{
	//    HashSet<string> ret = new HashSet<string>();
	//    lock (engineGuard)
	//    {
	//        return engine.Stem(word);
	//    }
	//}

	public final String GetStatementId(String cnlStatement)
	{
		cognipy.cnl.dl.Paragraph dlAst = tools.GetEN2DLAst(cnlStatement);
		return tools.SerializeDLAst(dlAst).replace("\r\n", "");
	}

	//      bool traceOn = true;

	public final void LoadCnl(String filename, boolean loadAnnotations, boolean materialize)
	{
		LoadCnl(filename, loadAnnotations, materialize, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void LoadCnl(string filename, bool loadAnnotations, bool materialize, bool modalChecker = false)
	public final void LoadCnl(String filename, boolean loadAnnotations, boolean materialize, boolean modalChecker)
	{
		//            System.Diagnostics.Trace.WriteIf(this.traceOn, string.Format("onto.LoadCnl(\'{0}\',{1},{2})",Path.GetFullPath( filename).Replace("\\","/"),loadAnnotations,materialize) );
		LoadCnl(filename, null, loadAnnotations, materialize, modalChecker);
	}


	public final void LoadRdf(String uri, boolean loadAnnotations, boolean materialize)
	{
		LoadRdf(uri, loadAnnotations, materialize, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void LoadRdf(string uri, bool loadAnnotations, bool materialize, bool modalChecker = false)
	public final void LoadRdf(String uri, boolean loadAnnotations, boolean materialize, boolean modalChecker)
	{
		LoadRdf(uri, null, loadAnnotations, materialize, modalChecker);
	}



	public final void LoadRdfFromString(String rdf, boolean loadAnnotations, boolean materialize)
	{
		LoadRdfFromString(rdf, loadAnnotations, materialize, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void LoadRdfFromString(string rdf, bool loadAnnotations, bool materialize, bool modalChecker = false)
	public final void LoadRdfFromString(String rdf, boolean loadAnnotations, boolean materialize, boolean modalChecker)
	{
		LoadRdfFromString(rdf, null, loadAnnotations, materialize, modalChecker);
	}


	public final void LoadCnlFromString(String cnl, boolean loadAnnotations, boolean materialize)
	{
		LoadCnlFromString(cnl, loadAnnotations, materialize, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void LoadCnlFromString(string cnl, bool loadAnnotations, bool materialize, bool modalChecker = false)
	public final void LoadCnlFromString(String cnl, boolean loadAnnotations, boolean materialize, boolean modalChecker)
	{
		LoadCnlFromString(cnl, null, loadAnnotations, materialize, modalChecker);
	}

	public final String CnlFromUri(String uri, String type)
	{
		String n = getReasoner().renderEntityFromUri(uri, (type == null || type.equals("instance")) ? cognipy.ars.EntityKind.Instance : (type.equals("concept") ? cognipy.ars.EntityKind.Concept : cognipy.ars.EntityKind.Role));
		if (n == null)
		{
			return "";
		}

		cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
		tempVar.id = n;
		cognipy.cnl.en.EnName enN = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar, (type == null || type.equals("instance")) ? cognipy.cnl.en.endict.WordKind.NormalForm : (type.equals("concept") ? cognipy.cnl.en.endict.WordKind.NormalForm : cognipy.cnl.en.endict.WordKind.PastParticiple), type.equals("instance"));
		return enN.id;
	}

	public final String UriFromCnl(String cnl, String type)
	{
		cognipy.cnl.en.EnName tempVar = new cognipy.cnl.en.EnName();
		tempVar.id = cnl;
		String dl = cognipy.cnl.en.ENNameingConvention.ToDL(tempVar, (type == null || type.equals("instance")) ? cognipy.cnl.en.endict.WordKind.NormalForm : (type.equals("concept") ? cognipy.cnl.en.endict.WordKind.NormalForm : cognipy.cnl.en.endict.WordKind.PastParticiple)).id;

		String n = getReasoner().renderUriFromEntity(dl, (type == null || type.equals("instance")) ? cognipy.ars.EntityKind.Instance : (type.equals("concept") ? cognipy.ars.EntityKind.Concept : cognipy.ars.EntityKind.Role));
		if (n == null)
		{
			return "";
		}

		return n;
	}


	public final void SetValue(String instance, String datarole, Object val)
	{
		getReasoner().SetValue(instance, datarole, val);
	}


	private void Load(ReferenceManager.WhatToLoad whatToLoad, String contentToLoad, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize)
	{
		Load(whatToLoad, contentToLoad, impliAst, loadAnns, materialize, false);
	}

	private void Load(ReferenceManager.WhatToLoad whatToLoad, String contentToLoad, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns)
	{
		Load(whatToLoad, contentToLoad, impliAst, loadAnns, false, false);
	}

	private void Load(ReferenceManager.WhatToLoad whatToLoad, String contentToLoad, cognipy.cnl.dl.Paragraph impliAst)
	{
		Load(whatToLoad, contentToLoad, impliAst, false, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private void Load(ReferenceManager.WhatToLoad whatToLoad, string contentToLoad, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
	private void Load(ReferenceManager.WhatToLoad whatToLoad, String contentToLoad, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize, boolean modalChecker)
	{
		ReferenceManager rm = new ReferenceManager(null);
		HashSet<String> brokenImports;
		if (whatToLoad == ReferenceManager.WhatToLoad.FromUri)
		{
			rm.setCurrentFilePath(contentToLoad);
		}

		tangible.OutObject<HashSet<String>> tempOut_brokenImports = new tangible.OutObject<HashSet<String>>();
		tangible.OutObject<cognipy.ReferenceManager.ReferenceTags> tempOut_tags = new tangible.OutObject<cognipy.ReferenceManager.ReferenceTags>();
		tangible.OutObject<cognipy.cnl.dl.Paragraph> tempOut_paragraph = new tangible.OutObject<cognipy.cnl.dl.Paragraph>();
		if (!rm.LoadOntology(whatToLoad, tools, contentToLoad, tempOut_brokenImports, tempOut_tags, tempOut_paragraph, null, cognipy.ars.NameingConventionKind.Smart, null, true, true, OWLMissingOntologyReferencesStrategy.Throw_Exception, loadAnns, false, true))
		{
		paragraph = tempOut_paragraph.argValue;
		tags = tempOut_tags.argValue;
		brokenImports = tempOut_brokenImports.argValue;
			java.lang.Iterable<RuntimeException> excepts = rm.GetExceptionsOnImports(contentToLoad);
			if (excepts.size() > 0)
			{
				throw excepts.First();
			}
			else
			{
				throw new IllegalStateException("Unknown error during import");
			}
		}
	else
	{
		paragraph = tempOut_paragraph.argValue;
		tags = tempOut_tags.argValue;
		brokenImports = tempOut_brokenImports.argValue;
	}

		this.ontologyBase = rm.DefaultNamespace;
		HashMap<Object, Object> pfx2nss = rm.AllReferences.ToDictionary(i -> i.Key, i -> i.Value);
		AllReferences = pfx2nss;
		ontologyBase = ontologyBase != null ? ontologyBase : "http://ontorion.com/unknown.owl";
		rm.DefaultNamespace = ontologyBase;

		HermiTReasoningService r = new HermiTReasoningService(paragraph, impliAst, ReasoningMode.RL, namc, ontologyBase, uriMapping(), invUriMapping(), pfx2nss);
		this.modalChecker = modalChecker;
		r.TheAccessObject = this;
		r.Outer = Outer;
		setReasoner(r);

		if (materialize)
		{
			Materialize();
		}

		if (loadAnns)
		{
			LoadAnnotations(paragraph);
		}
	}



	private void LoadAnnotations(cognipy.cnl.dl.Paragraph paragraph)
	{

		cognipy.cnl.AnnotationManager annotManToTranslate = new AnnotationManager();

		for (Statement stmt : paragraph.Statements)
		{
			if (stmt instanceof cognipy.cnl.dl.Annotation)
			{
				cognipy.cnl.dl.Annotation ann = stmt instanceof cognipy.cnl.dl.Annotation ? (cognipy.cnl.dl.Annotation)stmt : null;
				String cnlSent = tools.GetENDLFromAst(ann, true);

				if (cnlSent.startsWith(cognipy.cnl.AnnotationManager.ANNOTATION_START))
				{
					cognipy.cnl.AnnotationManager annotMan = new cognipy.cnl.AnnotationManager();
					cognipy.cnl.en.EnName tempVar = new cognipy.cnl.en.EnName();
					tempVar.id = x;
					annotMan.loadW3CAnnotationsFromText(cnlSent, false, x -> cognipy.cnl.en.ENNameingConvention.ToDL(tempVar, cognipy.cnl.en.endict.WordKind.NormalForm).id);

					annotManToTranslate.appendAnnotations(annotMan);

					// add the prefix to ns manager the prefixes/namespace map derived from the annotations
					for (Map.Entry<String, Tuple<String, String>> pfx2nsIn : annotMan.getPfx2NsDefinedInLoadedAnnotations().entrySet())
					{
					}

					// add the prefixes that where not recognized by the namespace manager.
					for (String pfx : annotMan.getUnknownPrefixesInAnnotations())
					{

					}
				}

			}
		}
		for (Map.Entry<String, String> subj : annotManToTranslate.GetAnnotationSubjects().entrySet())
		{
			HashMap<String, ArrayList<AnnotationResult>> annotsEl = new HashMap<String, ArrayList<AnnotationResult>>();
			String subjn = EN(subj.getKey(), subj.getValue().equals("Instance"));
			for (W3CAnnotation w3ann : annotManToTranslate.GetAnnotations(subj.getKey()))
			{
				AnnotationResult annot = new AnnotationResult();
				annot.setSubject(subjn);
				annot.setSubjectType(subj.getValue());
				annot.setLanguage(w3ann.getLanguage());
				annot.setProperty(EN(w3ann.getType(), false));
				annot.setValue(w3ann.getValue());
				if (!annotsEl.containsKey(annot.getProperty()))
				{
					annotsEl.put(annot.getProperty(), new ArrayList<AnnotationResult>());
				}
				annotsEl.get(annot.getProperty()).add(annot);
			}
			if (!annotations.containsKey(subjn))
			{
				annotations.put(subjn, annotsEl);
			}
			else
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var k : annotations.keySet())
				{
					if (annotations.get(subjn).containsKey(k))
					{
						annotations.get(subjn).get(k).addAll(annotsEl.get(k));
					}
					else
					{
						annotations.get(subjn).put(k, annotsEl.get(k));
					}
				}
			}
		}
	}


	private void LoadRdf(String uri, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize)
	{
		LoadRdf(uri, impliAst, loadAnns, materialize, false);
	}

	private void LoadRdf(String uri, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns)
	{
		LoadRdf(uri, impliAst, loadAnns, false, false);
	}

	private void LoadRdf(String uri, cognipy.cnl.dl.Paragraph impliAst)
	{
		LoadRdf(uri, impliAst, false, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void LoadRdf(string uri, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
	private void LoadRdf(String uri, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize, boolean modalChecker)
	{
		Load(ReferenceManager.WhatToLoad.FromUri, uri, impliAst, loadAnns, materialize, modalChecker);
	}


	private void LoadRdfFromString(String rdf, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize)
	{
		LoadRdfFromString(rdf, impliAst, loadAnns, materialize, false);
	}

	private void LoadRdfFromString(String rdf, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns)
	{
		LoadRdfFromString(rdf, impliAst, loadAnns, false, false);
	}

	private void LoadRdfFromString(String rdf, cognipy.cnl.dl.Paragraph impliAst)
	{
		LoadRdfFromString(rdf, impliAst, false, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void LoadRdfFromString(string rdf, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
	private void LoadRdfFromString(String rdf, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize, boolean modalChecker)
	{
		Load(ReferenceManager.WhatToLoad.OwlRdfFromString, rdf, impliAst, loadAnns, materialize, modalChecker);
	}


	private void LoadCnl(String filename, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize)
	{
		LoadCnl(filename, impliAst, loadAnns, materialize, false);
	}

	private void LoadCnl(String filename, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns)
	{
		LoadCnl(filename, impliAst, loadAnns, false, false);
	}

	private void LoadCnl(String filename, cognipy.cnl.dl.Paragraph impliAst)
	{
		LoadCnl(filename, impliAst, false, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void LoadCnl(string filename, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns = false, bool materialize = false, bool modalChecker = false)
	private void LoadCnl(String filename, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize, boolean modalChecker)
	{
		Load(ReferenceManager.WhatToLoad.FromUri, filename, impliAst, loadAnns, materialize, modalChecker);
	}


	private void LoadCnlFromString(String cnl, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize)
	{
		LoadCnlFromString(cnl, impliAst, loadAnns, materialize, false);
	}

	private void LoadCnlFromString(String cnl, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns)
	{
		LoadCnlFromString(cnl, impliAst, loadAnns, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: void LoadCnlFromString(string cnl, CogniPy.CNL.DL.Paragraph impliAst, bool loadAnns, bool materialize = false, bool modalChecker = false)
	private void LoadCnlFromString(String cnl, cognipy.cnl.dl.Paragraph impliAst, boolean loadAnns, boolean materialize, boolean modalChecker)
	{
		Load(ReferenceManager.WhatToLoad.CnlFromString, cnl, impliAst, loadAnns, materialize, modalChecker);
	}

	public final void GetOwlUriMapping(tangible.RefObject<HashMap<Tuple<EntityKind, String>, String>> owlMapping, ReferenceManager.ReferenceTags tag)
	{
		for (Map.Entry<Tuple<EntityKind, String>, String> m : tag.uriMapping.entrySet())
		{
			if (!owlMapping.argValue.containsKey(m.getKey()))
			{
				owlMapping.argValue.put(m.getKey(), m.getValue());
			}
		}

		for (ReferenceTags t : tag.referencedTags)
		{
			GetOwlUriMapping(owlMapping, t);
		}
	}

	public final void GetOwlInvUriMapping(tangible.RefObject<HashMap<String, String>> owlMapping, ReferenceManager.ReferenceTags tag)
	{
		for (Map.Entry<String, String> m : tag.invUriMapping.entrySet())
		{
			if (!owlMapping.argValue.containsKey(m.getKey()))
			{
				owlMapping.argValue.put(m.getKey(), m.getValue());
			}
		}

		for (ReferenceTags t : tag.referencedTags)
		{
			GetOwlInvUriMapping(owlMapping, t);
		}
	}

	public final HashMap<String, String> invUriMapping()
	{
		HashMap<String, String> ret = new HashMap<String, String>();
		tangible.RefObject<HashMap<String, String>> tempRef_ret = new tangible.RefObject<HashMap<String, String>>(ret);
		GetOwlInvUriMapping(tempRef_ret, tags);
	ret = tempRef_ret.argValue;
		return ret;
	}

	public final HashMap<Tuple<EntityKind, String>, String> uriMapping()
	{
		HashMap<Tuple<EntityKind, String>, String> ret = new HashMap<Tuple<EntityKind, String>, String>();
		tangible.RefObject<HashMap<Tuple<EntityKind, String>, String>> tempRef_ret = new tangible.RefObject<HashMap<Tuple<EntityKind, String>, String>>(ret);
		GetOwlUriMapping(tempRef_ret, tags);
	ret = tempRef_ret.argValue;
		return ret;
	}

	public final String ToRDF(boolean includeImplicitKnowledge)
	{
		if (_reasoner == null)
		{
			return "";
		}
		return getReasoner().GetOWLXML(includeImplicitKnowledge);
	}

	public final void SetProperty(String prop, String name, Object val)
	{
		Outer.SetProperty(prop, name, val);
	}

	public final Object GetProperty(String prop, String name)
	{
		return Outer.GetProperty(prop, name);
	}

	public final String[] ListProperties(String prop)
	{
		return Outer.ListProperties(prop);
	}

	public final void ClearProperties(String prop)
	{
		Outer.ClearProperties(prop);
	}

	public final String ToCNL(boolean includeAnnotations)
	{
		return ToCNL(false, includeAnnotations);
	}

	public final String ToCNL(boolean includeImplicitKnowledge, boolean includeAnnotations)
	{
		if (_reasoner == null)
		{
			return "";
		}
		return tools.GetENDLFromAst(getReasoner().GetParagraph(includeImplicitKnowledge), includeAnnotations);
	}

	public final String ToCNL(cognipy.cnl.dl.Statement stmt)
	{
		return tools.GetENDLFromAst(stmt);
	}

	private boolean isConcept(String conc)
	{
		return conc.startsWith("a ") || conc.startsWith("an ") || Character.isLowerCase(conc.charAt(0));
	}

	public final Object GetAnnotationValue(String subj, String prop, String lang, String type)
	{
		if (annotations.containsKey(subj))
		{
			HashMap<String, ArrayList<AnnotationResult>> ann = annotations.get(subj);
			if (ann.containsKey(prop))
			{
				ArrayList<AnnotationResult> ap = ann[prop];
				for (AnnotationResult a : ap)
				{
					if ((lang == null || a.getLanguage().equals(lang)) && (type == null || a.getSubjectType().equals(type)))
					{
						return a.getValue();
					}
				}
			}
		}
		return null;
	}

	public final java.lang.Iterable<AnnotationResult> GetAnnotationsForSignature(java.lang.Iterable<String> cnlEntities)
	{
		ArrayList<AnnotationResult> res = new ArrayList<AnnotationResult>();

		for (String subj : cnlEntities)
		{
			if (annotations.containsKey(subj))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var v : annotations.get(subj).values())
				{
					res.addAll(v);
				}
			}
		}

		return res;
	}

	private HashMap<String, ConstraintResult> constraints = null;

	private void LoadConstrains()
	{
		if (constraints != null)
		{
			return;
		}

		java.lang.Iterable<ConstraintResult> allConstraints = getReasoner().GetAllConstraints();
		constraints = new HashMap<String, ConstraintResult>();
		for (ConstraintResult cc : allConstraints)
		{
			String nam = EN(cc.getConcept(), false);
			if (!constraints.containsKey(nam))
			{
				ConstraintResult tempVar = new ConstraintResult();
				tempVar.setConcept(nam);
				tempVar.setRelations(new HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>>());
				tempVar.setThirdElement(new HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>>());
				constraints.put(nam, tempVar);
			}

			ConstraintResult cr = constraints.get(nam);

			for (cognipy.cnl.dl.Statement.Modality mod : cc.getRelations().keySet())
			{
				if (cr.getRelations().containsKey(mod))
				{
					cr.getRelations().get(mod).addAll(cc.getRelations().get(mod).Select(x -> EN(x, true)));
				}
				else
				{
					cr.getRelations().put(mod, cc.getRelations().get(mod).Select(x -> EN(x, true)).ToList());
				}
			}


			for (cognipy.cnl.dl.Statement.Modality mod : cc.getThirdElement().keySet())
			{
				if (cr.getThirdElement().containsKey(mod))
				{
					cr.getThirdElement().get(mod).addAll(cc.getThirdElement().get(mod).Select(x -> x.startsWith("(some ") ? x : EN(x, true)).ToList());
				}
				else
				{
					cr.getThirdElement().put(mod, cc.getThirdElement().get(mod).Select(x -> x.startsWith("(some ") ? x : EN(x, true)).ToList());
				}
			}
		}
	}

	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> GetConstrainsForSubject(String concept)
	{
		ArrayList<ArrayList<Object>> ret = new ArrayList<ArrayList<Object>>();
		HashMap<String, ConstraintResult> cr = GetConstraints(new ArrayList<String>(Arrays.asList(concept)));
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var rex = cr.FirstOrDefault();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var rels = rex.Value.Relations;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var tep = rex.Value.ThirdElement;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var mod : rels.keySet())
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var r1 = rels[mod];
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var t1 = tep[mod];
			for (int i = 0; i < r1.Count; i++)
			{
				ret.add(new Object[] {mod.toString(), r1[i], t1[i]}.ToList());
			}
		}
		return Tuple.<ArrayList<String>, ArrayList<ArrayList<Object>>>Create(new String[] {"modality", "relation", "range"}.ToList(), ret);
	}

	public final HashMap<String, ConstraintResult> GetConstraints(ArrayList<String> descriptions)
	{


		//Get all needed concepts to check.
		HashMap<String, ArrayList<String>> conceptsToReturn = new HashMap<String, ArrayList<String>>();
		for (String desc : descriptions)
		{
			String name = desc;
			if (!conceptsToReturn.containsKey(name))
			{
				ArrayList<String> jenaConcepts = GetSuperConceptsOf(name, false);

				conceptsToReturn.put(name, jenaConcepts);
				if (isConcept(desc))
				{
					conceptsToReturn.get(name).add(name.replace("a ", "").replace("an ", ""));
				}

			}
		}

		LoadConstrains();
		HashMap<String, ConstraintResult> constraintsByDescription = new HashMap<String, ConstraintResult>();

		for (Map.Entry<String, ArrayList<String>> desc : conceptsToReturn.entrySet())
		{
			ConstraintResult cr = new ConstraintResult();
			cr.setConcept(null);
			cr.setRelations(new HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>>());
			cr.setThirdElement(new HashMap<cognipy.cnl.dl.Statement.Modality, ArrayList<String>>());

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var kn : desc.getValue())
			{
				if (!constraints.containsKey(kn))
				{
					continue;
				}

				ConstraintResult cc = constraints.get(kn);
				for (cognipy.cnl.dl.Statement.Modality mod : cc.getRelations().keySet())
				{
					if (cr.getRelations().containsKey(mod))
					{
						cr.getRelations().get(mod).addAll(cc.getRelations().get(mod).Select(x -> EN(x, true)));
					}
					else
					{
						cr.getRelations().put(mod, cc.getRelations().get(mod).Select(x -> EN(x, true)).ToList());
					}
				}


				for (cognipy.cnl.dl.Statement.Modality mod : cc.getThirdElement().keySet())
				{
					if (cr.getThirdElement().containsKey(mod))
					{
						cr.getThirdElement().get(mod).addAll(cc.getThirdElement().get(mod).Select(x -> x.startsWith("(some ") ? x : EN(x, true)).ToList());
					}
					else
					{
						cr.getThirdElement().put(mod, cc.getThirdElement().get(mod).Select(x -> x.startsWith("(some ") ? x : EN(x, true)).ToList());
					}
				}

			}
			if (cr.getRelations().size() != 0 || cr.getThirdElement().size() != 0)
			{
				constraintsByDescription.put(desc.getKey(), cr);
			}
		}

		return constraintsByDescription;
	}

	public final ArrayList<String> ToCNLList(boolean includeAnnotations)
	{
		return ToCNLList(false, true, includeAnnotations);
	}

	public final ArrayList<String> ToCNLList(boolean includeImplicitKnowledge, boolean removeTrivials, boolean includeAnnotations)
	{
		HashSet<String> cnlList = new HashSet<String>();
		if (_reasoner == null)
		{
			return cnlList.ToList();
		}
		ArrayList<cognipy.cnl.dl.Statement> stmts = new ArrayList<cognipy.cnl.dl.Statement>();
		ArrayList<cognipy.cnl.dl.Statement> annots = new ArrayList<cognipy.cnl.dl.Statement>();
		HashMap<HashSet<String>, cognipy.cnl.dl.Statement> trct = new HashMap<HashSet<String>, cognipy.cnl.dl.Statement>();
		for (Statement stmt : getReasoner().GetParagraph(includeImplicitKnowledge, includeImplicitKnowledge).Statements)
		{
			if (stmt instanceof cognipy.cnl.dl.Annotation)
			{
				if ((stmt instanceof cognipy.cnl.dl.Annotation ? (cognipy.cnl.dl.Annotation)stmt : null).txt.startsWith("%Annotations:"))
				{
					annots.add(stmt);
				}
				else
				{
					continue;
				}
			}

			cognipy.cnl.dl.Paragraph pr = new cognipy.cnl.dl.Paragraph(null);
			pr.Statements = new ArrayList<cognipy.cnl.dl.Statement>(Arrays.asList(stmt));

			if (!includeImplicitKnowledge || removeTrivials)
			{
				if ((stmt instanceof cognipy.cnl.dl.InstanceOf && (stmt instanceof cognipy.cnl.dl.InstanceOf ? (cognipy.cnl.dl.InstanceOf)stmt : null).C instanceof cognipy.cnl.dl.Top) || (stmt instanceof cognipy.cnl.dl.Subsumption && (stmt instanceof cognipy.cnl.dl.Subsumption ? (cognipy.cnl.dl.Subsumption)stmt : null).D instanceof cognipy.cnl.dl.Top) || (stmt instanceof cognipy.cnl.dl.RoleInclusion && (stmt instanceof cognipy.cnl.dl.RoleInclusion ? (cognipy.cnl.dl.RoleInclusion)stmt : null).D instanceof cognipy.cnl.dl.Top) || (stmt instanceof cognipy.cnl.dl.DataRoleInclusion && (stmt instanceof cognipy.cnl.dl.DataRoleInclusion ? (cognipy.cnl.dl.DataRoleInclusion)stmt : null).D instanceof cognipy.cnl.dl.Top))
				{
					HashSet<String> sg = DLToys.GetSignatureFromParagraph(pr);
					trct.put(sg, stmt);
					continue;
				}

				stmts.add(stmt);
			}

			String cnlSent = tools.GetENDLFromAst(pr, false);

			if (!tangible.StringHelper.isNullOrEmpty(cnlSent))
			{
				cnlList.add(cnlSent.replace("\r\n", ""));
			}
		}
		if (!includeImplicitKnowledge || removeTrivials)
		{
			cognipy.cnl.dl.Paragraph tempVar = new cognipy.cnl.dl.Paragraph(null);
			tempVar.Statements = stmts;
			HashSet<String> sign = DLToys.GetSignatureFromParagraph(tempVar);
			for (Map.Entry<HashSet<String>, cognipy.cnl.dl.Statement> kv : trct.entrySet())
			{
				if (sign.Intersect(kv.getKey()).Count() == 0)
				{
					String cnlSent = tools.GetENDLFromAst(kv.getValue());
					cnlList.add(cnlSent.replace("\r\n", ""));
				}
			}
		}

		if (includeAnnotations)
		{
			//We collect all annotations together in order to create single Annotations: block
			cognipy.cnl.dl.Paragraph pr = new cognipy.cnl.dl.Paragraph(null);
			pr.Statements = new ArrayList<cognipy.cnl.dl.Statement>(annots);
			String annotsCnlBlock = tools.GetENDLFromAst(pr, includeAnnotations);
			if (!tangible.StringHelper.isNullOrEmpty(annotsCnlBlock))
			{
				cnlList.add(annotsCnlBlock);
			}
		}

		return cnlList.ToList();
	}

	public final ArrayList<CogniPyStatement> ToCNLStatementList()
	{
		return ToCNLStatementList(false);
	}
	public final ArrayList<CogniPyStatement> ToCNLStatementList(boolean includeImplicitKnowledge)
	{
		DLENConverter dlEnConverter = new DLENConverter(tools, (ns) -> ns, (pfx) -> pfx, "");

		ArrayList<CogniPyStatement> cnlList = new ArrayList<CogniPyStatement>();
		if (_reasoner == null)
		{
			return cnlList;
		}
		for (Statement stmt : getReasoner().GetParagraph(includeImplicitKnowledge).Statements)
		{
			StatementType type;
			cognipy.cnl.dl.StatementAttr attr = (cognipy.cnl.dl.StatementAttr)stmt.getClass().GetCustomAttributes(cognipy.cnl.dl.StatementAttr.class, true).First();
			switch (attr.getType())
			{
				case cognipy.cnl.dl.StatementType.Concept:
					type = StatementType.Concept;
					break;
				case cognipy.cnl.dl.StatementType.Instance:
					type = StatementType.Instance;
					break;
				case cognipy.cnl.dl.StatementType.Role:
					type = StatementType.Role;
					break;
				case cognipy.cnl.dl.StatementType.Rule:
					type = StatementType.Rule;
					break;
				case cognipy.cnl.dl.StatementType.Annotation:
					type = StatementType.Annotation;
					continue; //skip the annotations.

				default:
					throw new IllegalArgumentException("Internal error while retrieving statements.");
			}
			if (stmt.modality != cognipy.cnl.dl.Statement.Modality.IS)
			{
				type = StatementType.Constraint;
			}

			HashSet<String> signature = DLToys.GetSignatureFromStatement(stmt);
			HashSet<String> concepts = new HashSet<String>();
			HashSet<String> roles = new HashSet<String>();
			HashSet<String> dataroles = new HashSet<String>();
			HashSet<String> instances = new HashSet<String>();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var element : signature)
			{
				if (element.StartsWith("C:"))
				{
					concepts.add(dlEnConverter.EN(element.Substring(2), false));
				}
				if (element.StartsWith("I:"))
				{
					instances.add(dlEnConverter.EN(element.Substring(2), true));
				}
				if (element.StartsWith("R:"))
				{
					roles.add(dlEnConverter.EN(element.Substring(2), false));
				}
				if (element.StartsWith("D:"))
				{
					dataroles.add(dlEnConverter.EN(element.Substring(2), false));
				}
			}

			String statementCNL = tools.GetENDLFromAst(stmt, false, (ns) -> ns);
			CogniPyStatement tempVar = new CogniPyStatement();
			tempVar.setCnlStatement(statementCNL);
			tempVar.setConcepts(concepts);
			tempVar.setInstances(instances);
			tempVar.setRoles(roles);
			tempVar.setDataRoles(dataroles);
			tempVar.setType(type);
			cnlList.add(tempVar);
		}

		return cnlList;
	}


	public final cognipy.cnl.dl.Paragraph GetParagrah()
	{
		return GetParagrah(true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CogniPy.CNL.DL.Paragraph GetParagrah(bool includeImplicitKnowledge = true)
	public final cognipy.cnl.dl.Paragraph GetParagrah(boolean includeImplicitKnowledge)
	{
		return getReasoner().GetParagraph(includeImplicitKnowledge);
	}

	public final String[] SelectInstancesSPARQLDetails(String cnl)
	{
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnl);
		HashMap<String, String> roleMapping;
		HashMap<String, String> attrMapping;
		String defaultInstance;
		tangible.OutObject<HashMap<String, String>> tempOut_roleMapping = new tangible.OutObject<HashMap<String, String>>();
		tangible.OutObject<HashMap<String, String>> tempOut_attrMapping = new tangible.OutObject<HashMap<String, String>>();
		tangible.OutObject<String> tempOut_defaultInstance = new tangible.OutObject<String>();
		String[] tempVar = getReasoner().getSparqlTransform().ConvertToGetInstancesOfDetails(node, null, null, tempOut_roleMapping, tempOut_attrMapping, tempOut_defaultInstance, true, false);
	defaultInstance = tempOut_defaultInstance.argValue;
	attrMapping = tempOut_attrMapping.argValue;
	roleMapping = tempOut_roleMapping.argValue;
	return tempVar;
	}

	public final String SelectTypesOfSPARQL(String cnl, boolean direct)
	{
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnl);
		HashMap<String, String> roleMapping;
		HashMap<String, String> attrMapping;
		String defaultInstance;
		tangible.OutObject<HashMap<String, String>> tempOut_roleMapping = new tangible.OutObject<HashMap<String, String>>();
		tangible.OutObject<HashMap<String, String>> tempOut_attrMapping = new tangible.OutObject<HashMap<String, String>>();
		tangible.OutObject<String> tempOut_defaultInstance = new tangible.OutObject<String>();
		String querySelect = getReasoner().getSparqlTransform().ConvertToGetTypesOf(node, null, null, tempOut_roleMapping, tempOut_attrMapping, tempOut_defaultInstance, 0, -1, true, direct);
	defaultInstance = tempOut_defaultInstance.argValue;
	attrMapping = tempOut_attrMapping.argValue;
	roleMapping = tempOut_roleMapping.argValue;
		return querySelect;
	}


	public final String SelectInstancesSPARQL(String cnl)
	{
		return SelectInstancesSPARQL(cnl, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string SelectInstancesSPARQL(string cnl, bool direct = false)
	public final String SelectInstancesSPARQL(String cnl, boolean direct)
	{
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnl);
		HashMap<String, String> roleMapping;
		HashMap<String, String> attrMapping;
		String defaultInstance;
		tangible.OutObject<HashMap<String, String>> tempOut_roleMapping = new tangible.OutObject<HashMap<String, String>>();
		tangible.OutObject<HashMap<String, String>> tempOut_attrMapping = new tangible.OutObject<HashMap<String, String>>();
		tangible.OutObject<String> tempOut_defaultInstance = new tangible.OutObject<String>();
		String querySelect = getReasoner().getSparqlTransform().ConvertToGetInstancesOf(node, null, null, tempOut_roleMapping, tempOut_attrMapping, tempOut_defaultInstance, 0, -1, true, direct);
	defaultInstance = tempOut_defaultInstance.argValue;
	attrMapping = tempOut_attrMapping.argValue;
	roleMapping = tempOut_roleMapping.argValue;
		return querySelect;
	}

	public final String SelectSubconceptsSPARQL(String cnl, boolean direct)
	{
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnl);
		String querySelect = getReasoner().getSparqlTransform().ConvertToGetSubconceptsOf(node, direct, false, 0, -1, true);
		return querySelect;
	}

	public final String SelectSuperconceptsSPARQL(String cnl, boolean direct)
	{
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnl);
		String querySelect = getReasoner().getSparqlTransform().ConvertToGetSuperconceptsOf(node, direct, false, 0, -1, true);
		return querySelect;
	}


	private void InvalidateMaterialization()
	{
		alreadyMaterialized = false;
	}

	public final java.lang.Iterable<InstanceDescription> DescribeInstancesByName(java.lang.Iterable<String> instances)
	{
		HashSet<String> instancesDeduped = new HashSet<String>(instances);
		ArrayList<InstanceDescription> result = new ArrayList<InstanceDescription>();
		HashMap<String, InstanceDescription> ret = DescribeInstances((instancesDeduped.size() > 1 ? "either " : "") + tangible.StringHelper.join(",", instancesDeduped));
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var inst : instancesDeduped)
		{
			if (ret.containsKey(inst))
			{
				result.add(ret.get(inst));
			}
		}
		return result;
	}

	public final HashMap<String, InstanceDescription> DescribeInstances(String query)
	{
		Materialize();
		HashMap<String, InstanceDescription> results = new HashMap<String, InstanceDescription>();
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(query);
		if (node instanceof cognipy.cnl.dl.Atomic || (node instanceof cognipy.cnl.dl.InstanceSet))
		{
			ArrayList<Tuple<String, Object>> l = this.getReasoner().GetInstancesOfFromModelFastURI(node);
			for (Tuple<String, Object> ins : l)
			{
				String instanceName = CnlFromUri(ins.Item1, "instance");
				if (!results.containsKey(instanceName))
				{
					InstanceDescription tempVar = new InstanceDescription();
					tempVar.setAttributeValues(new HashMap<String, java.lang.Iterable<Object>>());
					tempVar.setRelatedInstances(new HashMap<String, java.lang.Iterable<String>>());
					tempVar.setInstance(instanceName);
					results.put(instanceName, tempVar);
				}

				ArrayList<Tuple<Boolean, String, Object>> prps = this.getReasoner().GetAllPriopertiesFastFromURI(ins.Item2);
				for (Tuple<Boolean, String, Object> prp : prps)
				{
					String propName = CnlFromUri(prp.Item2, "role");
					if (prp.Item1)
					{
						if (!results.get(instanceName).getRelatedInstances().containsKey(propName))
						{
							results.get(instanceName).getRelatedInstances().put(propName, new HashSet<String>());
						}

						((HashSet<String>)results.get(instanceName).getRelatedInstances().get(propName)).add(CnlFromUri(prp.Item3.toString(), "instance"));
					}
					else
					{
						if (!results.get(instanceName).getAttributeValues().containsKey(propName))
						{
							results.get(instanceName).getAttributeValues().put(propName, new HashSet<Object>());
						}

						((HashSet<Object>)results.get(instanceName).getAttributeValues().get(propName)).add(prp.Item3);
					}
				}
			}
		}
		else
		{
			String[] qq = SelectInstancesSPARQLDetails(query);

			String totSparql = "SELECT " + qq[1] + " ?r ?d ?y {" + "{" + qq[1] + " rdf:type owl:NamedIndividual " + ". " + qq[2] + ". " + "OPTIONAL{ " + qq[1] + " ?r ?y. ?r rdf:type owl:ObjectProperty} " + "} UNION {" + qq[1] + " rdf:type owl:NamedIndividual " + ". " + qq[2] + ". " + "OPTIONAL{ " + qq[1] + " ?d ?y. ?d rdf:type owl:DatatypeProperty} " + "}";
			if (qq[3] != null)
			{
				totSparql = totSparql + ". FILTER(" + qq[3] + ")";
			}
			totSparql = totSparql + "}";

			{
				Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = SparqlQuery(totSparql, true, false);
				int x0idx = res.Item1.indexOf(qq[1].substring(1));
				int ridx = res.Item1.indexOf("r");
				int didx = res.Item1.indexOf("d");
				int yidx = res.Item1.indexOf("y");

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
				java.lang.Iterable<ArrayList<Object>> str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].toString(), "instance"), (x[ridx] != null) ? CnlFromUri(x[ridx].toString(), "role") : null, (x[didx] != null) ? CnlFromUri(x[didx].toString(), "role") : null, x[yidx]);
				for (ArrayList<Object> x : str)
				{
					if (!results.containsKey(x.Item1))
					{
						InstanceDescription tempVar2 = new InstanceDescription();
						tempVar2.setAttributeValues(new HashMap<String, java.lang.Iterable<Object>>());
						tempVar2.setRelatedInstances(new HashMap<String, java.lang.Iterable<String>>());
						tempVar2.setInstance(x.Item1);
						results.put(x.Item1, tempVar2);
					}

					if (x.Item2 != null)
					{
						if (!results.get(x.Item1).getRelatedInstances().containsKey(x.Item2))
						{
							results.get(x.Item1).getRelatedInstances().put(x.Item2, new HashSet<String>());
						}
						((HashSet<String>)results.get(x.Item1).getRelatedInstances().get(x.Item2)).add(CnlFromUri(x.Item4.toString(), "instance"));
					}

					if (x.Item3 != null)
					{
						if (!results.get(x.Item1).getAttributeValues().containsKey(x.Item3))
						{
							results.get(x.Item1).getAttributeValues().put(x.Item3, new HashSet<Object>());
						}
						((HashSet<Object>)results.get(x.Item1).getAttributeValues().get(x.Item3)).add(x.Item4);
					}
				}
			}

		}
		return results;
	}

	public static class RuleEntity
	{
		private String Name;
		public final String getName()
		{
			return Name;
		}
		public final void setName(String value)
		{
			Name = value;
		}
		private Object Value;
		public final Object getValue()
		{
			return Value;
		}
		public final void setValue(Object value)
		{
			Value = value;
		}
	}

	private tangible.EventHandler<HermiTReasoningService.DebugTraceEventArgs> _ruleDebugger = null;

	public final String CnlFromDLString(String dl)
	{
		return tools.GetENDLFromAst(tools.GetDLAst(dl));
	}

	private tangible.Action2Param<String, ArrayList<RuleEntity>> _debugListener;
	private tangible.Func2Param<String, ArrayList<RuleEntity>, Tuple<String, ArrayList<RuleEntity>>> _converter;
	public final void SetDebugListener(tangible.Action2Param<String, ArrayList<RuleEntity>> debugListener, tangible.Func2Param<String, ArrayList<RuleEntity>, Tuple<String, ArrayList<RuleEntity>>> converter)
	{
		_debugListener = (String arg1, ArrayList<RuleEntity> arg2) -> debugListener.invoke(arg1, arg2);
		_converter = (String arg1, ArrayList<RuleEntity> arg2) -> converter.invoke(arg1, arg2);
		debugModeOn = true;

		_ruleDebugger = (Object sender, HermiTReasoningService.DebugTraceEventArgs e) ->
		{
				if (!tea.TraceMessage.startsWith("#"))
				{
					String enR = CnlFromDLString(tea.TraceMessage);

					ArrayList<RuleEntity> entitiesPerRule = new ArrayList<RuleEntity>();
					for (var kv : tea.Binding)
					{
						String vn = kv.Key.substring(1);
						int lidx = vn.lastIndexOf('-');
						if (lidx >= 0)
						{
							String avn = vn.substring(0, lidx);
							int r;
							tangible.OutObject<Integer> tempOut_r = new tangible.OutObject<Integer>();
							if (tangible.TryParseHelper.tryParseInt(vn.substring(lidx + 1), tempOut_r))
							{
								r = tempOut_r.argValue;
								vn = avn + "(" + String.valueOf(r) + ")";
							}
							else
							{
								r = tempOut_r.argValue;
								vn = avn;
							}
						}

						RuleEntity tempVar = new RuleEntity();
						tempVar.setName(vn);
						tempVar.setValue(kv.Value.Item1 == null ? kv.Value.Item2 :this.EN(kv.Value.Item2.toString(), kv.Value.Item1.equals("Instance")));
						entitiesPerRule.add(tempVar);
					}
					_debugListener.invoke(enR, entitiesPerRule);
				}
				else
				{
					ArrayList<RuleEntity> entitiesPerRule = new ArrayList<RuleEntity>();
					for (var kv : tea.Binding)
					{
						RuleEntity tempVar2 = new RuleEntity();
						tempVar2.setName(kv.Key);
						tempVar2.setValue(kv.Value.Item1 == null ? kv.Value.Item2 :this.EN(kv.Value.Item2.toString(), kv.Value.Item1.equals("Instance")));
						entitiesPerRule.add(tempVar2);
					}
					var cnv = _converter.invoke(tea.TraceMessage, entitiesPerRule);
					_debugListener.invoke(cnv.Item1, cnv.Item2);
				}
		};
	}

	private void Materialize()
	{
		if (alreadyMaterialized)
		{
			return;
		}

		ReasoningMode TBox;
		ReasoningMode ABox;

		if (SWRLOnly)
		{
			TBox = ReasoningMode.SWRL;
			ABox = ReasoningMode.SWRL;
		}
		else
		{
			TBox = ReasoningMode.RL;
			ABox = ReasoningMode.RL;
		}
		getReasoner().debugModeOn = debugModeOn;
		getReasoner().exeRulesOn = true;
		if (debugModeOn)
		{
			getReasoner().DebugTrace += _ruleDebugger;
		}
		getReasoner().Materialization(TBox, ABox, false, modalChecker);
		alreadyMaterialized = true;
	}

	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> SparqlQuery(String query, boolean materialize, boolean asCnl)
	{
		Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = SparqlQueryInternal(query, materialize, true, null);
		if (asCnl)
		{
			res = TranslateQueryResultsIntoCnlInPlace(res);
		}
		return res;
	}


	public final Tuple<java.util.ArrayList<String>, java.util.ArrayList<java.util.ArrayList<Object>>> SparqlQueryInternal(String query, boolean materialize, boolean detectTypesOfNodes)
	{
		return SparqlQueryInternal(query, materialize, detectTypesOfNodes, null);
	}

	public final Tuple<java.util.ArrayList<String>, java.util.ArrayList<java.util.ArrayList<Object>>> SparqlQueryInternal(String query, boolean materialize)
	{
		return SparqlQueryInternal(query, materialize, true, null);
	}

	public final Tuple<java.util.ArrayList<String>, java.util.ArrayList<java.util.ArrayList<Object>>> SparqlQueryInternal(String query)
	{
		return SparqlQueryInternal(query, true, true, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Tuple<List<string>, List<List<object>>> SparqlQueryInternal(string query, bool materialize = true, bool detectTypesOfNodes = true, string defaultKindOfNode = null)
	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> SparqlQueryInternal(String query, boolean materialize, boolean detectTypesOfNodes, String defaultKindOfNode)
	{
		if (materialize)
		{
			Materialize();
		}
		cognipy.executing.hermitclient.HermiTReasoningService.SparqlRowset res = getReasoner().SparqlQuery(query, invUriMapping(), detectTypesOfNodes, defaultKindOfNode);
		ArrayList<String> cols = res.GetCols();
		ArrayList<Object> rows = res.GetRows().ToList();
		return Tuple.Create(cols, rows);
	}

	public final String GetReasoningInfo()
	{
		Materialize();
		return getReasoner().GetReasoningInfo();
	}


	public final Tuple<java.util.ArrayList<String>, java.util.ArrayList<java.util.ArrayList<Object>>> GetAnnotationsForSubject(String subj, String prop)
	{
		return GetAnnotationsForSubject(subj, prop, "");
	}

	public final Tuple<java.util.ArrayList<String>, java.util.ArrayList<java.util.ArrayList<Object>>> GetAnnotationsForSubject(String subj)
	{
		return GetAnnotationsForSubject(subj, "", "");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public Tuple<List<string>, List<List<object>>> GetAnnotationsForSubject(string subj, string prop = "", string lang = "")
	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> GetAnnotationsForSubject(String subj, String prop, String lang)
	{
		ArrayList<Object> cols = (new String[] {"subject", "subjectType", "property", "value", "language"}).ToList();
		ArrayList<ArrayList<Object>> res = new ArrayList<ArrayList<Object>>();

		if (annotations.containsKey(subj))
		{
			HashMap<String, ArrayList<AnnotationResult>> annots = annotations.get(subj);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var ann2 : annots.values())
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var ann : ann2)
				{
					if ((lang.equals("") || (lang.equals(ann.Language))) && (prop.equals("") || prop.equals(ann.Property)))
					{
						ArrayList<Object> rw = new ArrayList<Object>();
						rw.add(ann.Subject);
						rw.add(ann.SubjectType);
						rw.add(ann.Property);
						rw.add(ann.Value);
						rw.add(ann.Language);
						res.add(rw);
					}
				}
			}
		}

		return Tuple.Create(cols, res);
	}

	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> TranslateQueryResultsIntoCnlInPlace(Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> result)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var x : result.Item2)
		{
			for (int i = 0; i < x.Count; i++)
			{
				if (x[i] instanceof cognipy.GraphEntity)
				{
					x[i] = CnlFromUri(x[i].toString(), ((cognipy.GraphEntity)x[i]).getKind());
				}
			}
		}
		return result;
	}

	private final String[] toFilter = new String[] {"\"Thing\"[owl]", "\"NamedIndividual\"[owl]", "class[owl]"};

	public final ArrayList<String> GetSuperConceptsOf(String cnlName, boolean direct)
	{
		Materialize();
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnlName);
		if (!direct && (node instanceof cognipy.cnl.dl.Atomic || ((node instanceof cognipy.cnl.dl.InstanceSet) && (node instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)node : null).Instances.size() == 1 && (node instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)node : null).Instances.get(0) instanceof cognipy.cnl.dl.NamedInstance)))
		{
			ArrayList<String> l = this.getReasoner().GetSuperConceptsOfFromModelFast(node);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return (from x in l select CnlFromUri(x, "concept")).ToList();
		}
		else
		{
			String sparql;
			if ((node instanceof cognipy.cnl.dl.InstanceSet) && (node instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)node : null).Instances.size() == 1 && (node instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)node : null).Instances.get(0) instanceof cognipy.cnl.dl.NamedInstance)
			{
				sparql = SelectTypesOfSPARQL(cnlName, direct);
			}
			else
			{
				if (!(node instanceof cognipy.cnl.dl.Atomic) && !(node instanceof cognipy.cnl.dl.Top))
				{
					throw new UnsupportedOperationException("It works only for atomic concept names");
				}
				sparql = SelectSuperconceptsSPARQL(cnlName, direct);
			}
			return TranslateQueryResultsIntoCnlInPlace(SparqlQueryInternal(sparql, true, false, "concept")).Item2.SelectMany(x -> x.stream().filter(z -> !toFilter.Contains(z)).map(y -> (y instanceof String ? (String)y : null))).ToList();
		}
	}

	private ArrayList<String> r_GetSuperConcepts(String concept, boolean direct)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		ArrayList<Object> toRet = (from l in getReasoner().GetSuperConcepts(tools.GetEN2DLNode(concept), direct, false) select new ArrayList<String>(tools.Morphology(l, "", "NormalForm", false))).ToList();

		return toRet.SelectMany(sc -> sc).ToList();
	}

	public final ArrayList<String> GetSubConceptsOf(String cnlName, boolean direct)
	{
		Materialize();
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnlName);
		if (!direct && (node instanceof cognipy.cnl.dl.Atomic))
		{
			ArrayList<String> l = this.getReasoner().GetSubConceptsOfFromModelFast(node instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)node : null);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return (from x in l select CnlFromUri(x, "concept")).ToList();
		}
		else
		{
			if (!(node instanceof cognipy.cnl.dl.Atomic) && !(node instanceof cognipy.cnl.dl.Top))
			{
				throw new UnsupportedOperationException("It works only for atomic concept names");
			}
			String sparql;
			sparql = SelectSubconceptsSPARQL(cnlName, direct);
			return TranslateQueryResultsIntoCnlInPlace(SparqlQueryInternal(sparql, true, false, "concept")).Item2.SelectMany(x -> x.stream().filter(z -> !toFilter.Contains(z)).map(y -> (y instanceof String ? (String)y : null))).ToList();
		}
	}

	private ArrayList<ArrayList<String>> r_GetSubConceptsOf(String concept, boolean direct)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		return (from l in getReasoner().GetSubConcepts(tools.GetEN2DLNode(concept), direct, false) select new ArrayList<String>(tools.Morphology(l, "", "NormalForm", false))).ToList();
	}

	public final ArrayList<String> GetInstancesOf(String cnlName, boolean direct)
	{
		Materialize();
		cognipy.cnl.dl.Node node = tools.GetEN2DLNode(cnlName);
		if (!direct && (node instanceof cognipy.cnl.dl.Atomic || ((node instanceof cognipy.cnl.dl.InstanceSet) && (node instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)node : null).Instances.size() == 1 && (node instanceof cognipy.cnl.dl.InstanceSet ? (cognipy.cnl.dl.InstanceSet)node : null).Instances.get(0) instanceof cognipy.cnl.dl.NamedInstance)))
		{
			ArrayList<String> l = this.getReasoner().GetInstancesOfFromModelFast(node);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			return (from x in l select CnlFromUri(x, "instance")).ToList();
		}
		else
		{
			HashMap<String, String> roleMapping;
			HashMap<String, String> attrMapping;
			String defaultInstance;
			tangible.OutObject<HashMap<String, String>> tempOut_roleMapping = new tangible.OutObject<HashMap<String, String>>();
			tangible.OutObject<HashMap<String, String>> tempOut_attrMapping = new tangible.OutObject<HashMap<String, String>>();
			tangible.OutObject<String> tempOut_defaultInstance = new tangible.OutObject<String>();
			String sparql = getReasoner().getSparqlTransform().ConvertToGetInstancesOf(node, null, null, tempOut_roleMapping, tempOut_attrMapping, tempOut_defaultInstance, 0, -1, true, direct);
		defaultInstance = tempOut_defaultInstance.argValue;
		attrMapping = tempOut_attrMapping.argValue;
		roleMapping = tempOut_roleMapping.argValue;
			return TranslateQueryResultsIntoCnlInPlace(SparqlQueryInternal(sparql, true, false, "instance")).Item2.SelectMany(x -> x.stream().filter(z -> !toFilter.Contains(z)).map(y -> (y instanceof String ? (String)y : null))).ToList();
		}
	}

	private ArrayList<ArrayList<String>> r_GetInstancesOf(String concept, boolean direct)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		return (from l in getReasoner().GetInstancesOf(tools.GetEN2DLNode(concept), direct) select new ArrayList<String>(tools.Morphology(l, "", "NormalForm", true))).ToList();
	}

	private String[] r_GetEquivalentConceptsOf(String concept)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		return (from l in getReasoner().GetEquivalentConcepts(tools.GetEN2DLNode(concept), false) select tools.Morphology(new String[] {l}, "", "NormalForm", false).First()).ToArray();
	}

	///////////// AP

	public final String DLFromUri(String uri, String type)
	{
		String n = getReasoner().renderEntityFromUri(uri, type.equals("instance") ? cognipy.ars.EntityKind.Instance : (type.equals("concept") ? cognipy.ars.EntityKind.Concept : cognipy.ars.EntityKind.Role));
		if (n == null)
		{
			return "";
		}
		return n;
	}

	public final String InstanceDL(String en)
	{
		if (en.length() >= 2)
		{
			if (Character.isUpperCase(en.charAt(0)) && Character.isUpperCase(en.charAt(1)))
			{
				return en;
			}
		}
		return "_" + en;
	}

	public final String ID(String en)
	{
		return en.substring(1);
	}

	public final String EN(String en)
	{
		synchronized (tools)
		{
			return tools.GetDL(en, true);
		}
	}


	public final String EN(String dl, boolean bigName)
	{
		return EN(dl, bigName, cognipy.cnl.en.endict.WordKind.NormalForm);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string EN(string dl, bool bigName, CogniPy.CNL.EN.endict.WordKind wrdKnd = CogniPy.CNL.EN.endict.WordKind.NormalForm)
	public final String EN(String dl, boolean bigName, cognipy.cnl.en.endict.WordKind wrdKnd)
	{
		if (dl.equals("⊤"))
		{
			return "thing";
		}

		cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
		tempVar.id = dl;
		cognipy.cnl.en.EnName.Parts allParts = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar, wrdKnd, bigName).Split();
		if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && allParts.term.startsWith("<") && allParts.term.endsWith(">"))
		{
			String nss = allParts.term.substring(1, 1 + allParts.term.length() - 2);
			if (ontologyBase.equals(nss)) // remove if the namespace is the default one.
			{
				allParts.term = null;
			}
			else
			{
				String tterm = AllReferences.containsKey(nss) ? AllReferences.get(nss) : null;
				if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
				{
					allParts.term = tterm;
				}
			}
		}

		return allParts.Combine().id;
	}


	public final HashSet<String> SplitText(String stxt)
	{
		try
		{
			HashSet<String> newScript = new HashSet<String>();
			if (!stxt.trim().equals(""))
			{
				synchronized (tools)
				{
					Object tempVar = tools.GetENAst(stxt, true);
					cognipy.cnl.en.paragraph ast = tempVar instanceof cognipy.cnl.en.paragraph ? (cognipy.cnl.en.paragraph)tempVar : null;
					for (sentence stmt : ast.sentences)
					{
						newScript.add(tools.GetENFromAstSentence(stmt, true));
					}
				}
			}
			return newScript;
		}
		catch (java.lang.Exception e)
		{
			//if (showBox)
			//    MessageBox.Show(this, "There are errors in the knowledge. Please fix them.", "Errors!", MessageBoxButtons.OK, MessageBoxIcon.Error);
			return null;
		}
	}

	public final void SetProgress(double completed)
	{
		//  _ctrl.SetProgress(completed);
	}

	public final void WriteMessage(int priority, String message)
	{

	}

	private static class SimplePopulator implements cognipy.cnl.dl.Populator
	{
		private CogniPySvr _parent;
		public SimplePopulator(CogniPySvr _parent)
		{
			this._parent = _parent;
		}

		public final java.lang.Iterable<Map.Entry<String, String>> Populate(String sentenceBeginning, String str, ArrayList<String> forms, int max)
		{

			ArrayList<Map.Entry<String, String>> ret = new ArrayList<Map.Entry<String, String>>();

			for (String form : forms)
			{
				// ask the main window for the elements to add from the current window text (i=0) or from the referenced ontologies (i=1)....
				for (int i = 0; i <= 0; i++)
				{
					boolean external = i == 0;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var kv = form.split("[:]", -1);
					java.lang.Iterable<Map.Entry<String, String>> retD = _parent.Populate(false, kv[0], str, kv[1]);
					for (Map.Entry<String, String> d : retD) // add it in returned strings
					{
						ret.add(new Map.Entry<String, String>((external ? "e" : "i") + ":" + form, d));
					}
				}
			}

			return ret;
		}


	}


	private java.lang.Iterable<String> getConcepts()
	{
		if (concepts.isEmpty())
		{
			return new String[] {"<noun>"};
		}
		else
		{
			return concepts;
		}
	}

	private java.lang.Iterable<String> getAllRoles()
	{
		HashSet<String> ret = new HashSet<String>();
		if (objectroles.isEmpty())
		{
			ret.add("<verb>");
		}
		else
		{
			ret.UnionWith(objectroles);
		}
		if (dataroles.isEmpty())
		{
			ret.add("<attribute>");
		}
		else
		{
			ret.UnionWith(dataroles);
		}
		return ret;
	}

	private java.lang.Iterable<String> getDataRoles()
	{
		if (dataroles.isEmpty())
		{
			return new String[] {"<attribute>"};
		}
		else
		{
			return dataroles;
		}
	}

	private java.lang.Iterable<String> getDatatypes()
	{
		if (datatypes.isEmpty())
		{
			return new String[] {"<datatype>"};
		}
		else
		{
			return datatypes;
		}
	}

	private java.lang.Iterable<String> getInstances()
	{
		if (instances.isEmpty())
		{
			return new String[] {"<Proper-Name>"};
		}
		else
		{
			return instances;
		}
	}

	public final java.lang.Iterable<String> Populate(boolean LoadExternal, String What, String Start, String Form)
	{
		if (!LoadExternal)
		{
			switch (What)
			{
				case "datarole":
					return tools.Morphology(getDataRoles(), Start, Form, false);
				case "role":
					return tools.Morphology(getAllRoles(), Start, Form, false);
				case "concept":
					return tools.Morphology(getConcepts(), Start, Form, false);
				case "instance":
					return tools.Morphology(getInstances(), Start, Form, true);
				case "datatype":
					return tools.Morphology(getDatatypes(), Start, Form, false);
			}
		}
		else
		{
			switch (What)
			{
				//case "datarole": return tools.Morphology(getRefDataRoles(), Start, Form, false); 
				//case "role": return tools.Morphology(getAllRefRoles(), Start, Form, false); 
				//case "concept": return tools.Morphology(getRefConcepts(), Start, Form, false);
				//case "instance": return tools.Morphology(getRefInstances(), Start, Form, true);
				//case "datatype": return tools.Morphology(getRefDatatypes(), Start, Form, false); 
			}
		}
		throw new IllegalStateException();
	}

	public final String[] AutoComplete(String full)
	{
		HashSet<Tuple<cognipy.ars.EntityKind, String>> sign = tools.GetDLAstSignature(getReasoner().GetParagraph(false));
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var smb : sign)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var inam = smb.Item2;
			cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
			tempVar.id = inam;
			cognipy.cnl.en.EnName.Parts en = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar, true).Split();
			if (smb.Item1 == cognipy.ars.EntityKind.Instance)
			{
				instances.add(inam); //en.Combine().id);
			}
			else if (smb.Item1 == cognipy.ars.EntityKind.Role)
			{
				objectroles.add(inam); //(en.Combine().id);
			}
			else if (smb.Item1 == cognipy.ars.EntityKind.DataRole)
			{
				dataroles.add(inam); //(en.Combine().id);
			}
			else if (smb.Item1 == cognipy.ars.EntityKind.Concept)
			{
				concepts.add(inam); //(en.Combine().id);
			}
			else if (smb.Item1 == cognipy.ars.EntityKind.DataType)
			{
				datatypes.add(inam); //(en.Combine().id);
			}
		}

		ArrayList<Map.Entry<String, String>> symbols;
		tangible.OutObject<ArrayList<Map.Entry<String, String>>> tempOut_symbols = new tangible.OutObject<ArrayList<Map.Entry<String, String>>>();
		ArrayList<String> ret = tools.AutoComplete(new SimplePopulator(this), full, tempOut_symbols, Integer.MAX_VALUE);
	symbols = tempOut_symbols.argValue;
		if (ret == null)
		{
			Object[] insts = this.Populate(false, "instance", full, "").ToArray();
			if (insts.length == 0)
			{
				insts = new String[] {"<Proper-Name>"};
			}
			return new String[] {"Every", "Every-single-thing", "If", "No", "Nothing", "Something", "The", "X"}.Where(s -> s.toLowerCase().startsWith(full.toLowerCase())).Union(insts).ToArray();
		}
		else
		{
			return ret.toArray(new String[0]);
		}
	}

	public final String Highlight(String text)
	{
		return kwds.Replace(text, (System.Text.RegularExpressions.Match match) ->
		{
				return Character.isLetter(kw.Value.First()) ? "**" + kw.Value + "**" : kw.Value;
		}).replace("\r\n", "\r\n\r\n");
	}

	public final HashSet<String> KnowledgeSplit(String knowledge)
	{
		HashSet<String> ret = new HashSet<String>();
		HashSet<String> lines = SplitText(knowledge);

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var l : lines)
		{
			ret.add(l);
		}

		return ret;
	}



	private String toDL(String name, boolean isRole)
	{
		cognipy.cnl.en.EnName tempVar = new cognipy.cnl.en.EnName();
		tempVar.id = name;
		return cognipy.cnl.en.ENNameingConvention.ToDL(tempVar, isRole ? cognipy.cnl.en.endict.WordKind.PastParticiple : cognipy.cnl.en.endict.WordKind.NormalForm).id;
	}


	public final void MergeWith(CogniPySvr x)
	{
		MergeWith(x, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void MergeWith(CogniPySvr x, bool materialize = true)
	public final void MergeWith(CogniPySvr x, boolean materialize)
	{
		if (materialize)
		{
			Materialize();
			x.Materialize();
		}

		getReasoner().MergeWith(x.getReasoner());
	}

	private CogniPySvr(CogniPySvr other)
	{
		this.tags = other.tags;
		this.paragraph = other.paragraph;
		this.tools = new CNLTools("en");


		this.ontologyBase = other.ontologyBase;
		this.Outer = other.Outer;
		this.setReasoner(other.getReasoner().Clone(this, Outer));
		this.annotations = other.annotations;
		this.alreadyMaterialized = other.alreadyMaterialized;
	}

	public final CogniPySvr CloneForAboxChangesOnly()
	{
		Materialize();
		return new CogniPySvr(this);
	}

	public final String Kaka(String ka)
	{
		return "aaa";
	}

	private Object toVal(String v)
	{
		return cognipy.cnl.dl.Value.ToObject(cognipy.cnl.dl.Value.MakeFrom(v.substring(0, 1), v.substring(2)));
	}

	public final void RemoveInstance(String name)
	{
		Materialize();
		String dl = toDL(name, false);
		getReasoner().RemoveInstance(dl);
	}

	public final void KnowledgeInsert(String text, boolean loadAnnotations, boolean materialize)
	{
		if (materialize)
		{
			Materialize();
		}

		cognipy.cnl.dl.Paragraph para = tools.GetEN2DLAst(text);
		if (loadAnnotations)
		{
			LoadAnnotations(para);
		}

		getReasoner().AddRemoveKnowledge(para, true, SWRLOnly);
	}

	public final void KnowledgeDelete(String text, boolean materialize)
	{
		if (materialize)
		{
			Materialize();
		}
		getReasoner().AddRemoveKnowledge(tools.GetEN2DLAst(text), false, SWRLOnly);
	}

	public final String Why(String text, boolean materialize)
	{
		if (materialize)
		{
			Materialize();
		}
		return getReasoner().Why(tools.GetEN2DLAst(text));
	}

	private java.lang.Iterable<Tuple<String, String, String, Object>> ConvertAssertsToTuples(String[] asserts)
	{
		for (int i = 0; i < asserts.length; i += 4)
		{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return Tuple.<String, String, String, Object>Create(asserts[i], toDL(asserts[i + 1], false), !asserts[i + 2].equals("") ? toDL(asserts[i + 2], true) : "", asserts[i].equals("D") ? toVal(asserts[i + 3]) : toDL(asserts[i + 3], false));
		}
	}

	public final void AssertionsInsert(String[] asserts)
	{
		Materialize();
		getReasoner().AddRemoveAssertions(ConvertAssertsToTuples(asserts), true, SWRLOnly);
	}

	public final void AssertionsDelete(String[] asserts)
	{
		Materialize();
		getReasoner().AddRemoveAssertions(ConvertAssertsToTuples(asserts), false, SWRLOnly);
	}


	public static String GetVersionInfo()
	{
		return GetVersionInfo(null, 0, new HashSet<String>());
	}

	private static String GetVersionInfo(Assembly thisAsm, int n, HashSet<String> mark)
	{
		if (thisAsm == null)
		{
			thisAsm = Assembly.GetExecutingAssembly();
		}

		StringBuilder ret = new StringBuilder();
		ret.append(thisAsm.GetName().toString() + "\r\n");
		//ret.Append(thisAsm.GetName().Version.ToString());
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var dep : thisAsm.GetReferencedAssemblies())
		{
			try
			{
				if (!mark.contains(dep.FullName))
				{
					mark.add(dep.FullName);
					System.Reflection.Assembly asm = Assembly.Load(dep.FullName);
					if (n > 0)
					{
						ret.append(tangible.StringHelper.repeatChar(' ', n));
					}
					String str = GetVersionInfo(asm, n + 1, mark);

					if (!str.equals(""))
					{
						ret.append(str);
					}
				}
			}
			catch (java.lang.Exception e)
			{
			}
		}
		return ret.toString();
	}

	//RBinding

	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> SparqlQueryForInstancesWithDetails(String query)
	{
		HashMap<String, InstanceDescription> r = DescribeInstances(query);
		//calculate all colums
		HashMap<String, Integer> colDic = new HashMap<String, Integer>();
		ArrayList<String> cols = new ArrayList<String>();
		int idx = 0;
		colDic.put("Instance", idx++);
		cols.add("Instance");
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var instkv : r.values())
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var x : instkv.RelatedInstances.keySet())
			{
				if (!colDic.containsKey(x))
				{
					colDic.put(x, idx++);
					cols.add(x);
				}
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var x : instkv.AttributeValues.keySet())
			{
				if (!colDic.containsKey(x))
				{
					colDic.put(x, idx++);
					cols.add(x);
				}
			}
		}
		ArrayList<ArrayList<Object>> vals = new ArrayList<ArrayList<Object>>();
		Object[] pro = new Object[idx];
		for (Map.Entry<String, InstanceDescription> kv : r.entrySet())
		{
			ArrayList<Object> lst = new ArrayList<Object>(Arrays.asList(pro));
			lst.set(0, kv.getKey());
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var xy : kv.getValue().RelatedInstances)
			{
				Object[] v = xy.Value.ToArray();
				lst.set(colDic.get(xy.Key), v.length == 1 ? (Object)v[0] : (Object)v);
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var xy : kv.getValue().AttributeValues)
			{
				Object[] v = xy.Value.ToArray();
				lst.set(colDic.get(xy.Key), v.length == 1 ? (Object)v[0] : (Object)v);
			}
			vals.add(lst);
		}
		return Tuple.Create(cols, vals);
	}

	public final Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> SparqlQueryForInstancesWithDetails_o(String query)
	{
		String[] qq = SelectInstancesSPARQLDetails(query);
		String sparqlCom = "SELECT DISTINCT " + qq[1] + " ?r ?y {" + qq[2] + "." + qq[1] + " ?r ?y. " + qq[1] + " rdf:type owl:NamedIndividual. ?r rdf:type ";
		String sparqlInvCom = "SELECT DISTINCT " + qq[1] + " ?r ?y {" + qq[2] + ".?y ?r " + qq[1] + ". " + qq[1] + " rdf:type owl:NamedIndividual. ?r rdf:type ";
		String sparqlD = sparqlCom + "owl:DatatypeProperty}";
		String sparqlO = sparqlCom + "owl:ObjectProperty}";
		String sparqlInvO = sparqlInvCom + "owl:ObjectProperty}";

		HashMap<Tuple<String, String>, HashSet<Object>> fc = new HashMap<Tuple<String, String>, HashSet<Object>>();
		{
			Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = SparqlQueryInternal(sparqlO);
			int x0idx = res.Item1.indexOf(qq[1].substring(1));
			int ridx = res.Item1.indexOf("r");
			int yidx = res.Item1.indexOf("y");

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			java.lang.Iterable<ArrayList<Object>> str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].toString(), "instance"), CnlFromUri(x[ridx].toString(), "role"), CnlFromUri(x[yidx].toString(), "instance"));
			for (ArrayList<Object> x : str)
			{
				System.Tuple<T1, T2> k = Tuple.Create(x.Item1, x.Item2);
				if (!fc.containsKey(k))
				{
					fc.put(k, new HashSet<Object>());
				}
				fc.get(k).add(x.Item3);
			}
		}
		{
			Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = SparqlQueryInternal(sparqlInvO);
			int x0idx = res.Item1.indexOf(qq[1].substring(1));
			int ridx = res.Item1.indexOf("r");
			int yidx = res.Item1.indexOf("y");

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			java.lang.Iterable<ArrayList<Object>> str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].toString(), "instance"), CnlFromUri(x[ridx].toString(), "role"), CnlFromUri(x[yidx].toString(), "instance"));
			for (ArrayList<Object> x : str)
			{
				System.Tuple<T1, T2> k = Tuple.Create(x.Item1, x.Item2 + "^");
				if (!fc.containsKey(k))
				{
					fc.put(k, new HashSet<Object>());
				}
				fc.get(k).add(x.Item3);
			}
		}
		{
			Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = SparqlQueryInternal(sparqlD);
			int x0idx = res.Item1.indexOf(qq[1].substring(1));
			int ridx = res.Item1.indexOf("r");
			int yidx = res.Item1.indexOf("y");

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
			java.lang.Iterable<ArrayList<Object>> str = from x in res.Item2 select Tuple.Create(CnlFromUri(x[x0idx].toString(), "instance"), CnlFromUri(x[ridx].toString(), "datarole"), x[yidx]);
			for (ArrayList<Object> x : str)
			{
				System.Tuple<T1, T2> k = Tuple.Create(x.Item1, x.Item2);
				if (!fc.containsKey(k))
				{
					fc.put(k, new HashSet<Object>());
				}
				fc.get(k).add(x.Item3);
			}
		}

		int idx = 0;
		HashMap<String, Integer> colDic = new HashMap<String, Integer>();
		colDic.put("Instance", idx++);
		ArrayList<String> cols = new ArrayList<String>();
		cols.add("Instance");

		for (Map.Entry<Tuple<String, String>, HashSet<Object>> kv : fc.entrySet())
		{
			if (!colDic.containsKey(kv.getKey().Item2))
			{
				colDic.put(kv.getKey().Item2, idx++);
				cols.add(kv.getKey().Item2);
			}
		}

		HashMap<String, Object[]> rowsDic = new HashMap<String, Object[]>();

		for (Map.Entry<Tuple<String, String>, HashSet<Object>> kv : fc.entrySet())
		{
			Object[] lst = null;
			if (!rowsDic.containsKey(kv.getKey().Item1))
			{
				lst = new Object[idx + 1];
				lst[0] = kv.getKey().Item1;
				rowsDic.put(kv.getKey().Item1, lst);
			}
			else
			{
				lst = rowsDic.get(kv.getKey().Item1);
			}

			if (kv.getValue().Count > 1)
			{
				lst[colDic.get(kv.getKey().Item2)] = kv.getValue().ToArray();
			}
			else if (kv.getValue().Count == 1)
			{
				lst[colDic.get(kv.getKey().Item2)] = kv.getValue().First();
			}
		}

		{
			String sparqlInstances = SelectInstancesSPARQL(query);
			Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = SparqlQueryInternal(sparqlInstances);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var r : res.Item2)
			{
				String ins = CnlFromUri(r.First().toString(), "instance");
				if (!rowsDic.containsKey(ins))
				{
					Object[] lst = new Object[idx + 1];
					lst[0] = ins;
					rowsDic.put(ins, lst);
				}

			}
		}

		ArrayList<ArrayList<Object>> rows = new ArrayList<ArrayList<Object>>();
		for (Map.Entry<String, Object[]> r : rowsDic.entrySet())
		{
			rows.add(r.getValue().ToList());
		}


		return TranslateQueryResultsIntoCnlInPlace(Tuple.Create(cols, rows));
	}


}