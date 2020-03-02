package cognipy;

import cognipy.ars.*;
import cognipy.cnl.*;
import cognipy.owl.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class ReferenceManager
{
	private tangible.Func1Param<String, java.lang.Iterable<String>> getForms = null;
	public final void setForms(tangible.Func1Param<String, java.lang.Iterable<String>> forms)
	{
		getForms = (String arg) -> forms.invoke(arg);
	}
	public boolean OWLReferencesProblem = false;

	//not thread safe
	private String curentlyLoadingOnt;
	protected Lazy<OWLOntologyManager> owlManager = null;
	public final OWLOntologyManager getOwlManager()
	{
		return owlManager.Value;
	}

	public ReferenceManager(tangible.Func1Param<String, java.lang.Iterable<String>> getForms)
	{
		this.getForms = (String arg) -> getForms.invoke(arg);
		CreateOWLManager();
	}

	private void CreateOWLManager()
	{
		owlManager = new Lazy<OWLOntologyManager>(() ->
		{
				org.semanticweb.owlapi.model.OWLOntologyManager oman = OWLManager.createOWLOntologyManager();
				MissingImportListenerImpl mil = new MissingImportListenerImpl(this);
				oman.setSilentMissingImportsHandling(true);
				oman.addMissingImportListener(mil);
				oman.setOntologyLoaderConfiguration(config);
				return oman;
		});
	}

	private static class MissingImportListenerImpl implements MissingImportListener
	{
		private ReferenceManager workspace;
		public MissingImportListenerImpl(ReferenceManager workspace)
		{
			this.workspace = workspace;
		}

		public final void importMissing(MissingImportEvent mie)
		{
			workspace.NewError = true;

			if (!workspace.BrokenImports.containsKey(workspace.curentlyLoadingOnt))
			{
				workspace.BrokenImports.put(workspace.curentlyLoadingOnt, new HashSet<String>());
			}

			String importedOntology = mie.getImportedOntologyURI().toURI().toString();

			workspace.BrokenImports.get(workspace.curentlyLoadingOnt).add(importedOntology);

			RuntimeException creationException = (RuntimeException)mie.getCreationException();

			if (!workspace.ErrorsOnImports.containsKey(importedOntology))
			{
				workspace.ErrorsOnImports.put(importedOntology, new HashSet<String>());
			}

			if (!workspace.ExceptionsOnImports.containsKey(importedOntology))
			{
				workspace.ExceptionsOnImports.put(importedOntology, new HashSet<RuntimeException>());
			}

			if (workspace.ErrorsOnImports.containsKey(importedOntology) && !workspace.ErrorsOnImports.get(importedOntology).contains(creationException.getMessage()))
			{
				workspace.ErrorsOnImports.get(importedOntology).add(creationException.getMessage());
				workspace.ExceptionsOnImports.get(importedOntology).add(creationException);
			}
		}
	}

	public static class VirtualSiteIRIMapper implements OWLOntologyIRIMapper
	{
		public String currDir;

		private ReferenceManager workspace;
		public VirtualSiteIRIMapper(ReferenceManager workspace)
		{
			this.workspace = workspace;
		}

		protected final String getFileName(String url, String extension)
		{
			if (url.endsWith("/") || url.endsWith("#"))
			{
				String tmp = tangible.StringHelper.remove(url, url.length() - 1, 1);
				return tmp + extension;
			}
			else
			{
				return url;
			}
		}


		protected final String getFilePath(String ontoStr, String dir)
		{
			return getFilePath(ontoStr, dir, ".rdf");
		}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: protected string getFilePath(string ontoStr, string dir, string extension = ".rdf")
		protected final String getFilePath(String ontoStr, String dir, String extension)
		{
			if (tangible.StringHelper.isNullOrEmpty(dir))
			{
				return null;
			}

			String fn = (new File(ontoStr)).getName();
			if (ontoStr.endsWith("/") || ontoStr.endsWith("#")) // the ontologyIRI was not a filePath. Try to remove the last / and add .rdf....
			{
				fn = (new File(getFileName(ontoStr, extension))).getName();
			}

			String gfn = Paths.get(dir).resolve(fn).toString();

			return gfn;
		}

		/**
		 Checks the file exists or not.
		
		 The URL of the remote file.
		 True : If the file exits, False if file not exists
		*/
		private boolean RemoteFileExists(String url)
		{
			try
			{
				//Creating the HttpWebRequest
				WebRequest request = HttpWebRequest.Create(url);
				//Setting the Request method HEAD to make it fast.
				request.Method = "HEAD";
				request.Timeout = 5000;
				//Getting the Web Response.
				try
				{
					try (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
					{
						return (response.StatusCode == HttpStatusCode.OK);
					}
				}
				catch (WebException ex)
				{
					// this code is add for the case in which a proxy with authentication is added but the proxy do not support HEAD requests. We can in this case try with a GET request to make it work.
					System.Net.WebResponse tempVar = ex.Response;
					if ((tempVar instanceof HttpWebResponse ? (HttpWebResponse)tempVar : null).StatusCode == HttpStatusCode.ProxyAuthenticationRequired)
					{
						request = HttpWebRequest.Create(url);
						request.Method = "GET";
						request.Timeout = 10000;
						try (HttpWebResponse response = (HttpWebResponse)request.GetResponse())
						{
							return (response.StatusCode == HttpStatusCode.OK);
						}
					}
					else
					{
						return false;
					}
				}
			}
			catch (java.lang.Exception e)
			{
				return false;
			}
		}



		protected final boolean IsLocalFile(IRI ontologyIRI, tangible.OutObject<IRI> fileIRI)
		{
			return IsLocalFile(ontologyIRI, fileIRI, ".rdf");
		}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: protected bool IsLocalFile(IRI ontologyIRI, out IRI fileIRI, string extension = ".rdf")
		protected final boolean IsLocalFile(IRI ontologyIRI, tangible.OutObject<IRI> fileIRI, String extension)
		{
			boolean localFile = false;
			fileIRI.argValue = null;
			String ontoStr = OWLPathUriTools.IRI2Path(ontologyIRI);

			String gfn = getFilePath(ontoStr, currDir, extension);
			if (!(new File(gfn)).isFile())
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var dirs = (new File(currDir)).list(File::isDirectory);
				for (String dir : dirs)
				{
					gfn = getFilePath(ontoStr, dir, extension);
					if ((new File(gfn)).isFile())
					{
						localFile = true;
						break;
					}
				}
			}
			else
			{
				localFile = true;
			}

			fileIRI.argValue = IRI.create(UriFromFilePath(gfn));
			return localFile;
		}

		protected final boolean IsRemoteFile(IRI ontologyIRI, tangible.OutObject<IRI> remoteIRI)
		{
			remoteIRI.argValue = null;
			// not local --> try remotely
			if (RemoteFileExists(ontologyIRI.toString()))
			{
				return true;
			}
			else if (RemoteFileExists(getFileName(ontologyIRI.toString(), ".rdf"))) // remotely not found --> try remotely adding .rdf to the name.
			{
				remoteIRI.argValue = IRI.create(getFileName(ontologyIRI.toString(), ".rdf"));
				return true;
			}
			else
			{
				return false;
			}
		}

		/** 
		 Function called by the ontology manager when an ontology is loading. If the function returns null, it means: use the ontologyIRI
		 This functions searches (in order):
		   * locally (starting from the current folder (= folder of the file we are importing, = folder of the current file where the reference is written)
			   - using the name as given
			   - using the name after removing the last / and adding .rdf
		   * remotely
			   - using the url as it is
			   - using the url after removing the last / and adding .rdf
		 
		 @param ontologyIRI
		 @return 
		*/
		public final IRI getDocumentIRI(IRI ontologyIRI)
		{
			IRI actualIRI = null;
			// currDir is empty when the ontology we are importing is not local.
			// in this case we also have to check if the file is present at ontologyIRI or if we need to change the IRI where it is located
			tangible.OutObject<IRI> tempOut_actualIRI = new tangible.OutObject<IRI>();
			if (tangible.StringHelper.isNullOrEmpty(currDir) && !IsRemoteFile(ontologyIRI, tempOut_actualIRI))
			{
			actualIRI = tempOut_actualIRI.argValue;
				return null;
			}
			else
			{
			actualIRI = tempOut_actualIRI.argValue;
				if (tangible.StringHelper.isNullOrEmpty(currDir)) // if currDir was null and we are here, it means the file was found in the remoteFile function --> use actualIRI.
				{
					return actualIRI;
				}
			}

			// prefer local
			tangible.OutObject<IRI> tempOut_actualIRI2 = new tangible.OutObject<IRI>();
			if (IsLocalFile(ontologyIRI, tempOut_actualIRI2))
			{
			actualIRI = tempOut_actualIRI2.argValue;
				return actualIRI;
			}
			else
			{
			actualIRI = tempOut_actualIRI2.argValue;
				tangible.OutObject<IRI> tempOut_actualIRI3 = new tangible.OutObject<IRI>();
				if (IsLocalFile(ontologyIRI, tempOut_actualIRI3, ".owl"))
				{
				actualIRI = tempOut_actualIRI3.argValue;
					return actualIRI;
				}
				else
				{
				actualIRI = tempOut_actualIRI3.argValue;
					tangible.OutObject<IRI> tempOut_actualIRI4 = new tangible.OutObject<IRI>();
					if (IsRemoteFile(ontologyIRI, tempOut_actualIRI4))
					{
					actualIRI = tempOut_actualIRI4.argValue;
						return actualIRI;
					}
					else
					{
					actualIRI = tempOut_actualIRI4.argValue;
						return null;
					}
				}
			}
		}
	}

	private HashMap<String, HashSet<RuntimeException>> ExceptionsOnImports = new HashMap<String, HashSet<RuntimeException>>();
	private HashMap<String, ReferenceTags> LoadedOntologies = new HashMap<String, ReferenceTags>();
	public HashMap<String, cognipy.cnl.dl.Paragraph> LoadedDlAsts = new HashMap<String, cognipy.cnl.dl.Paragraph>();
	private HashMap<String, HashSet<String>> BrokenImports = new HashMap<String, HashSet<String>>();
	private HashMap<String, HashSet<String>> ErrorsOnImports = new HashMap<String, HashSet<String>>();
	private ArrayList<ParseException> SyntaxErrors = new ArrayList<ParseException>();

	public final java.lang.Iterable<Tuple<String, Boolean>> GetReferencedOntologies(String onto, boolean cnl)
	{
		for (Map.Entry<String, ReferenceTags> x : LoadedOntologies.entrySet())
		{
			if (cnl ? (onto.equals(x.getValue().FENamespace)) : (onto.equals(x.getValue().ontologyLocation)))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var r : x.getValue().referencedTags)
				{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
					yield return Tuple.Create(r.ontologyLocation == null ? r.FENamespace : r.ontologyLocation, r.ontologyLocation == null);
				}
			}
		}
	}

	public static class ReferenceTags
	{
		public HashSet<String> concepts = new HashSet<String>();
		public HashSet<String> roles = new HashSet<String>();
		public HashSet<String> dataroles = new HashSet<String>();
		public HashSet<String> instances = new HashSet<String>();
		public HashSet<String> datatypes = new HashSet<String>();
		public String baseNamespace = null;
		public String FENamespace = null;
		public String ontologyLocation = null;
		public boolean IsDirectlyReferencedTag = true;
		// parameter telling us if the reference was uploaded correctly (if there was some error while uploading it will be false)
		public boolean fullyUploaded = true;
		// if the ast content is null --> this reference tag was not imported
		public final boolean getIsImported()
		{
			if (dlAst.Statements == null)
			{
				return false;
			}

			return true;
		}


		public ArrayList<ReferenceTags> referencedTags = new ArrayList<ReferenceTags>();
		public HashMap<Tuple<EntityKind, String>, String> uriMapping = new HashMap<Tuple<EntityKind, String>, String>();
		public HashMap<String, String> invUriMapping = new HashMap<String, String>();
		public cognipy.cnl.dl.Paragraph dlAst = new cognipy.cnl.dl.Paragraph(null);
	}

	public final ArrayList<String> getAnnotationForOntology(String ontologyIri)
	{
		ArrayList<String> allAnnot = new ArrayList<String>();
		Iterator ontIt = owlManager.Value.getOntologies().iterator();
		while (ontIt.hasNext())
		{
			OWLOntology onto = (OWLOntology)ontIt.next();

			IRI currIRI = onto.getOntologyID().getOntologyIRI();

			if (currIRI != null && currIRI.toString().equals(ontologyIri))
			{
				Iterator annotIt = onto.getAnnotations().iterator();
				while (annotIt.hasNext())
				{
					OWLAnnotation annot = (OWLAnnotation)annotIt.next();
					String val = "";
					if (annot.getValue() instanceof OWLLiteral)
					{
						OWLLiteral owlLit = (OWLLiteral)annot.getValue();
						val = owlLit.getLiteral().replace("\'", "\''");
					}
					else if (annot.getValue() instanceof IRI)
					{
						IRI ir = (IRI)annot.getValue();
						val = ir.toString();
					}


					String result = "'" + annot.getProperty().getIRI() + "'::: '" + val + "'";
					allAnnot.add(result);
				}
				break;
			}
		}

		return allAnnot;
	}

	public final ArrayList<cognipy.cnl.W3CAnnotation> getAnnotationForEntity(cognipy.cnl.dl.DlName dlName, cognipy.ars.EntityKind kind, tangible.Func1Param<String, String> pfx2ns)
	{
		ArrayList<cognipy.cnl.W3CAnnotation> allAnnot = new ArrayList<cognipy.cnl.W3CAnnotation>();

		ArrayList<OWLOntology> loadedOntologies = new ArrayList<OWLOntology>();
		Iterator ontIt = owlManager.Value.getOntologies().iterator();
		while (ontIt.hasNext())
		{
			loadedOntologies.add((OWLOntology)ontIt.next());
		}

		if (loadedOntologies.isEmpty())
		{
			return allAnnot;
		}

		cognipy.ars.OwlNameingConventionCamelCase owlNameing = new cognipy.ars.OwlNameingConventionCamelCase();
		cognipy.ars.OwlName owlName = owlNameing.FromDL(dlName, CNL.EN.CNLFactory.lex, pfx2ns, kind);

		for (OWLOntology ont : loadedOntologies)
		{
			////////////////////////////////////////
			if (owlManager.Value.getOntologyFormat(ont) == null)
			{
				continue;
			}
			cognipy.ars.InvTransform invtransform = new cognipy.ars.InvTransform(owlManager.Value, ont, null);
			////////////////////////////////////////

			Iterator signIt = ont.getEntitiesInSignature(owlName.iri).iterator();
			if (!signIt.hasNext())
			{
				String newIri = owlName.iri.toString().replace("#", "");
				signIt = ont.getEntitiesInSignature(org.semanticweb.owlapi.model.IRI.create(newIri)).iterator();
			}

			while (signIt.hasNext())
			{
				OWLEntity ent = (OWLEntity)signIt.next();

				Iterator annotIt = ent.getAnnotationAssertionAxioms(ont).iterator();
				while (annotIt.hasNext())
				{
					OWLAnnotationAssertionAxiom annot = (OWLAnnotationAssertionAxiom)annotIt.next();
					cognipy.cnl.dl.DLAnnotationAxiom dlNameAnnot = (cognipy.cnl.dl.DLAnnotationAxiom)invtransform.Convert(annot);
					W3CAnnotation tempVar = new W3CAnnotation(true);
					tempVar.setExternal(true);
					tempVar.setLanguage(dlNameAnnot.language);
					tempVar.setType(dlNameAnnot.annotName);
					tempVar.setValue(dlNameAnnot.value);
					allAnnot.add(tempVar);
				}
			}
		}

		return allAnnot;
	}

	public static String UriFromFilePath(String pth)
	{
		return (new File(pth)).toURI().toString();
	}

	public final java.lang.Iterable<RuntimeException> GetExceptionsOnImports(String uri)
	{
		String pathUri = null;
		try
		{
			if (Paths.get(uri).getParent() == null)
			{
				if ((new File(uri)).isFile())
				{
					pathUri = UriFromFilePath(uri);
				}
			}
		}
		catch (java.lang.Exception e)
		{
		}

		if (ExceptionsOnImports.containsKey(uri))
		{
			return ExceptionsOnImports.get(uri);
		}
		else if (!tangible.StringHelper.isNullOrEmpty(pathUri) && ErrorsOnImports.containsKey(pathUri))
		{
			return ExceptionsOnImports.get(pathUri);
		}
		else
		{
			return new ArrayList<RuntimeException>();
		}
	}

	public final java.lang.Iterable<ParseException> GetSyntaxErrors()
	{
		return SyntaxErrors;
	}

	public final java.lang.Iterable<String> GetErrorsOnImports(String uri)
	{
		String pathUri = null;
		try
		{
			if (Paths.get(uri).getParent() == null)
			{
				if ((new File(uri)).isFile())
				{
					pathUri = UriFromFilePath(uri);
				}
			}
		}
		catch (java.lang.Exception e)
		{
		}

		if (ErrorsOnImports.containsKey(uri))
		{
			return ErrorsOnImports.get(uri);
		}
		else if (!tangible.StringHelper.isNullOrEmpty(pathUri) && ErrorsOnImports.containsKey(pathUri))
		{
			return ErrorsOnImports.get(pathUri);
		}
		else
		{
			return new ArrayList<String>();
		}
	}

	public boolean NewError = false;

	// decides what should be done when a reference inside an owl document is not present.
	public enum OWLMissingOntologyReferencesStrategy
	{
		Throw_Exception(0),
		Retry(1);

		public static final int SIZE = java.lang.Integer.SIZE;

		private int intValue;
		private static java.util.HashMap<Integer, OWLMissingOntologyReferencesStrategy> mappings;
		private static java.util.HashMap<Integer, OWLMissingOntologyReferencesStrategy> getMappings()
		{
			if (mappings == null)
			{
				synchronized (OWLMissingOntologyReferencesStrategy.class)
				{
					if (mappings == null)
					{
						mappings = new java.util.HashMap<Integer, OWLMissingOntologyReferencesStrategy>();
					}
				}
			}
			return mappings;
		}

		private OWLMissingOntologyReferencesStrategy(int value)
		{
			intValue = value;
			getMappings().put(value, this);
		}

		public int getValue()
		{
			return intValue;
		}

		public static OWLMissingOntologyReferencesStrategy forValue(int value)
		{
			return getMappings().get(value);
		}
	}

	public XmlDocument ontodoc;

	/** 
	 dictionary with <prefix,namespace> for all (imported and non-imported) the references inside the ontology.
	*/
	public TreeMap<String, String> AllReferences = new TreeMap<String, String>();
	/** 
	 dictionary with <namespace,location> of the direct imports in the ontology.
	*/
	public HashMap<String, String> DirectImports = new HashMap<String, String>();

	public String DefaultNamespace;
	private String _currentFilePath = null;
	public final void setCurrentFilePath(String value)
	{
		_currentFilePath = value;
	}

	// can defaultPfx be null??
	public enum WhatToLoad
	{
		FromUri,
		CnlFromString,
		OwlRdfFromString;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static WhatToLoad forValue(int value)
		{
			return values()[value];
		}
	}

	private ArrayList<String> recursivelyLoadedOntologies = new ArrayList<String>();


	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx, boolean convertToAst, boolean insertDependentAsts, OWLMissingOntologyReferencesStrategy missingReferencesStrategy, boolean loadAnnotations, boolean useDefaultNamespaceAsFullUri)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, defaultPfx, convertToAst, insertDependentAsts, missingReferencesStrategy, loadAnnotations, useDefaultNamespaceAsFullUri, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx, boolean convertToAst, boolean insertDependentAsts, OWLMissingOntologyReferencesStrategy missingReferencesStrategy, boolean loadAnnotations)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, defaultPfx, convertToAst, insertDependentAsts, missingReferencesStrategy, loadAnnotations, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx, boolean convertToAst, boolean insertDependentAsts, OWLMissingOntologyReferencesStrategy missingReferencesStrategy)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, defaultPfx, convertToAst, insertDependentAsts, missingReferencesStrategy, true, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx, boolean convertToAst, boolean insertDependentAsts)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, defaultPfx, convertToAst, insertDependentAsts, OWLMissingOntologyReferencesStrategy.Throw_Exception, true, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx, boolean convertToAst)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, defaultPfx, convertToAst, true, OWLMissingOntologyReferencesStrategy.Throw_Exception, true, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, defaultPfx, true, true, OWLMissingOntologyReferencesStrategy.Throw_Exception, true, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, nck, null, true, true, OWLMissingOntologyReferencesStrategy.Throw_Exception, true, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast, String rootontology)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, rootontology, cognipy.ars.NameingConventionKind.CamelCase, null, true, true, OWLMissingOntologyReferencesStrategy.Throw_Exception, true, false, true);
	}

	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<java.util.HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<CNL.DL.Paragraph> dlast)
	{
		return LoadOntology(whatToLoad, tools, source, brokenImports, tags, dlast, null, cognipy.ars.NameingConventionKind.CamelCase, null, true, true, OWLMissingOntologyReferencesStrategy.Throw_Exception, true, false, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public bool LoadOntology(WhatToLoad whatToLoad, CNLTools tools, string source, out HashSet<string> brokenImports, out ReferenceTags tags, out CNL.DL.Paragraph dlast, string rootontology = null, CogniPy.ARS.NameingConventionKind nck = CogniPy.ARS.NameingConventionKind.CamelCase, string defaultPfx = null, bool convertToAst = true, bool insertDependentAsts = true, OWLMissingOntologyReferencesStrategy missingReferencesStrategy = OWLMissingOntologyReferencesStrategy.Throw_Exception, bool loadAnnotations = true, bool useDefaultNamespaceAsFullUri = false, bool isFirstLevel = true)
	public final boolean LoadOntology(WhatToLoad whatToLoad, CNLTools tools, String source, tangible.OutObject<HashSet<String>> brokenImports, tangible.OutObject<ReferenceTags> tags, tangible.OutObject<cognipy.cnl.dl.Paragraph> dlast, String rootontology, cognipy.ars.NameingConventionKind nck, String defaultPfx, boolean convertToAst, boolean insertDependentAsts, OWLMissingOntologyReferencesStrategy missingReferencesStrategy, boolean loadAnnotations, boolean useDefaultNamespaceAsFullUri, boolean isFirstLevel)
	{

	RETRY:
		if (rootontology == null)
		{
			recursivelyLoadedOntologies = new ArrayList<String>();
			recursivelyLoadedOntologies.add(source);
		}
		NewError = false;
		OWLReferencesProblem = false;

		String oldBaseNamespace = null;
		tags.argValue = null;
		brokenImports.argValue = null;
		dlast.argValue = null;
		if (defaultPfx != null && (defaultPfx.endsWith("#") || defaultPfx.endsWith("/")))
		{
			defaultPfx.replace("#", "").replace("/", "");
		}

		boolean isendl = (whatToLoad == WhatToLoad.CnlFromString);
		boolean fromString = (whatToLoad != WhatToLoad.FromUri);
		if (!fromString)
		{
			try
			{
				if (System.IO.Path.GetExtension(source).equals(".encnl"))
				{
					isendl = true;
				}
			}
			catch (RuntimeException e)
			{
				if (!ErrorsOnImports.containsKey(source))
				{
					ErrorsOnImports.put(source, new HashSet<String>());
				}
				ErrorsOnImports.get(rootontology != null ? rootontology : source).add(e.getMessage());
				if (!ExceptionsOnImports.containsKey(source))
				{
					ExceptionsOnImports.put(source, new HashSet<RuntimeException>());
				}
				ExceptionsOnImports.get(rootontology != null ? rootontology : source).add(e);
				return false;
			}
			if (LoadedOntologies.containsKey(source) && !LoadedOntologies.get(source).FENamespace.equals(defaultPfx)) // the ontology uri is the same but the user changed the FENamespace!
			{
				oldBaseNamespace = LoadedOntologies.get(source).baseNamespace;
				this.ForgetOntology(source, false); // remove the ontology from the loadedOntology list but do not remove them from the owlManager so that it will be faster to check them again
			}
			else
			{
				if (LoadedOntologies.containsKey(source))
				{
					LoadedOntologies.remove(source);
					LoadedDlAsts.remove(source);
				}
				oldBaseNamespace = source;
			}
		}

		if (isendl) // encnl reference
		{
			dlast.argValue = null;

			try
			{
				if (!fromString && LoadedOntologies.containsKey(source)) // check that the ontology was not already loaded.
				{
					if (!BrokenImports.containsKey(source))
					{
						tags.argValue = LoadedOntologies.get(source);
						dlast.argValue = LoadedDlAsts.get(source);
						return true;
					}
					else
					{
						brokenImports.argValue = BrokenImports.get(source);
						return false;
					}
				}

				String txtToParse;
				if (!fromString)
				{
					txtToParse = Files.readString(source);
				}
				else
				{
					txtToParse = source;
				}
				ArrayList<Object> sentences = tools.splitSentences(txtToParse).ToList();

				tags.argValue = new ReferenceTags();

				dlast.argValue = new cognipy.cnl.dl.Paragraph(null);
				dlast.argValue.Statements = new ArrayList<cognipy.cnl.dl.Statement>();
				String cnlDefaultNamespace = "http://www.cognitum.eu/onto";
				for (Object sentence : sentences)
				{
					if (sentence.isAnnotation) //[T1-FE2-579] //&& loadAnnotations )
					{
						int dp = sentence.line.indexOf(':');
						String x = sentence.line.substring(0, dp);
						if (x.trim().equals("Namespace"))
						{
							String ontologyIri = sentence.line.substring(dp + 1).trim();
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
							cnlDefaultNamespace = ontologyIri;
						}
						else if (x.trim().equals("References")) // found another reference in the file --> load it.
						{
							MatchCollection refs = CNLTools.ParseReferences(sentence.line); //.Substring(dp)
							for (Match match : refs)
							{
								String onto, pfx, ns;
								tangible.OutObject<String> tempOut_pfx = new tangible.OutObject<String>();
								tangible.OutObject<String> tempOut_onto = new tangible.OutObject<String>();
								tangible.OutObject<String> tempOut_ns = new tangible.OutObject<String>();
								CNLTools.GetReferencePieces(match, tempOut_pfx, tempOut_onto, tempOut_ns);
								{
							ns = tempOut_ns.argValue;
							onto = tempOut_onto.argValue;
							pfx = tempOut_pfx.argValue;
									ReferenceTags innertags;
									HashSet<String> innerbrokenImports;
									cognipy.cnl.dl.Paragraph innerdlast;

									if ((tangible.StringHelper.isNullOrWhiteSpace(onto) || !loadAnnotations) && !AllReferences.containsKey(pfx)) // shortcut for load annotations
									{
										// case in which there is no location --> it is not an import.
										if (tangible.StringHelper.isNullOrEmpty(ns))
										{
											if (!tangible.StringHelper.isNullOrEmpty(onto))
											{
												AllReferences.put(pfx, onto);
											}
											else
											{
												AllReferences.put(pfx, "http://unknown.prefix/" + pfx);
											}
										}
										else
										{
											AllReferences.put(pfx, ns);
										}

										continue;
									}

									if (tangible.StringHelper.isNullOrWhiteSpace(onto))
									{
										continue;
									}

									if (!(new Uri(onto, UriKind.RelativeOrAbsolute)).IsAbsoluteUri)
									{
										String filePathLoc = (!fromString) ? source : _currentFilePath;
										onto = (new File(Paths.get((new File(filePathLoc)).getParent()).resolve(onto).toString())).getAbsolutePath();
									}

									if (_currentFilePath.equals(onto)) // the file we need to analyze next is the same as the current file. Skip
									{
										continue;
									}

									if (!recursivelyLoadedOntologies.contains(onto))
									{
										recursivelyLoadedOntologies.add(onto);
										boolean loaded = false;
										tangible.OutObject<HashSet<String>> tempOut_innerbrokenImports = new tangible.OutObject<HashSet<String>>();
										tangible.OutObject<cognipy.ReferenceManager.ReferenceTags> tempOut_innertags = new tangible.OutObject<cognipy.ReferenceManager.ReferenceTags>();
										tangible.OutObject<cognipy.cnl.dl.Paragraph> tempOut_innerdlast = new tangible.OutObject<cognipy.cnl.dl.Paragraph>();
										if (LoadOntology(WhatToLoad.FromUri, tools, onto, tempOut_innerbrokenImports, tempOut_innertags, tempOut_innerdlast, source, nck, pfx, convertToAst, insertDependentAsts, missingReferencesStrategy, true, false, false))
										{
										innerdlast = tempOut_innerdlast.argValue;
										innertags = tempOut_innertags.argValue;
										innerbrokenImports = tempOut_innerbrokenImports.argValue;
											loaded = true;
											if (!DirectImports.containsKey(innertags.baseNamespace))
											{
												DirectImports.put(innertags.baseNamespace, onto);
												if (!AllReferences.containsKey(pfx))
												{
													AllReferences.put(pfx, innertags.baseNamespace);
												}
											}
											tags.argValue.concepts.UnionWith(innertags.concepts);
											tags.argValue.roles.UnionWith(innertags.roles);
											tags.argValue.dataroles.UnionWith(innertags.dataroles);
											tags.argValue.instances.UnionWith(innertags.instances);
											tags.argValue.datatypes.UnionWith(innertags.datatypes);
											tags.argValue.referencedTags.add(innertags);
											if (insertDependentAsts && innerdlast != null)
											{
												dlast.argValue.Statements.addAll(innerdlast.Statements);
											}
										}
										else
										{
										innerdlast = tempOut_innerdlast.argValue;
										innertags = tempOut_innertags.argValue;
										innerbrokenImports = tempOut_innerbrokenImports.argValue;
											if (brokenImports.argValue != null)
											{
												brokenImports.argValue.UnionWith(innerbrokenImports);
											}
											else
											{
												brokenImports.argValue = innerbrokenImports;
											}
										}

										if (!AllReferences.containsKey(pfx))
										{
											if (!loaded && !tangible.StringHelper.isNullOrWhiteSpace(ns))
											{
												AllReferences.put(pfx, ns);
											}
											else if (!loaded)
											{
												AllReferences.put(pfx, onto);
											}
										}
									}
								}
							}
						}
						cognipy.cnl.dl.Paragraph ast = tools.GetEN2DLAst(sentence.line, false);
						if (ast != null)
						{
							dlast.argValue.Statements.addAll(ast.Statements);
						}
					}
					else
					{
						try
						{
							try
							{
								cognipy.cnl.dl.Paragraph ast = tools.GetEN2DLAst(sentence.line, true);

								if (!tangible.StringHelper.isNullOrEmpty(defaultPfx))
								{
									cognipy.cnl.dl.SetDefaultPfxVisitor defPfxVis = new cognipy.cnl.dl.SetDefaultPfxVisitor(defaultPfx);
									defPfxVis.Visit(ast);
								}

								dlast.argValue.Statements.addAll(ast.Statements);

								HashSet<Tuple<cognipy.ars.EntityKind, String>> sign = tools.GetDLAstSignature(ast);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
								for (var smb : sign)
								{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
									var inam = smb.Item2;
									if (smb.Item1 == ARS.EntityKind.Instance)
									{
										cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
										tempVar.id = inam;
										cognipy.cnl.en.EnName.Parts en = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar, true).Split();
										tags.argValue.instances.add(inam); //(en.Combine().id);
									}
									else
									{
										cognipy.cnl.dl.DlName tempVar2 = new cognipy.cnl.dl.DlName();
										tempVar2.id = inam;
										cognipy.cnl.en.EnName.Parts en = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar2, false).Split();
										if (smb.Item1 == ARS.EntityKind.Role)
										{
											tags.argValue.roles.add(inam); //(en.Combine().id);
										}
										else if (smb.Item1 == ARS.EntityKind.DataRole)
										{
											tags.argValue.dataroles.add(inam); //(en.Combine().id);
										}
										else if (smb.Item1 == ARS.EntityKind.Concept)
										{
											tags.argValue.concepts.add(inam); //(en.Combine().id);
										}
										else if (smb.Item1 == ARS.EntityKind.DataType)
										{
											tags.argValue.datatypes.add(inam); //(en.Combine().id);
										}
									}
								}
							}
							catch (ParseException ex)
							{
								SyntaxErrors.add(ex);
								continue;
							}
						}
						catch (RuntimeException ex)
						{
							Debug.WriteLine("Krr22" + ex.getMessage());
						}
					}

					DefaultNamespace = cnlDefaultNamespace;
				}
				if (dlast.argValue == null || dlast.argValue.Statements.isEmpty())
				{
					throw new RuntimeException("File is empty or all cnl sentences are incorrect.");
				}
				if (SyntaxErrors.size() == 1)
				{
					throw SyntaxErrors.get(0);
				}
				else if (!SyntaxErrors.isEmpty())
				{
					throw new AggregateException(SyntaxErrors);
				}

				if (tangible.StringHelper.isNullOrEmpty(DefaultNamespace))
				{
					DefaultNamespace = "http://www.ontorion.com/ontologies/Ontology" + UUID.NewGuid().toString("N");
				}

				tags.argValue.baseNamespace = DefaultNamespace;
				if (!tangible.StringHelper.isNullOrEmpty(defaultPfx))
				{
					tags.argValue.FENamespace = defaultPfx;
				}
				LoadedOntologies.put(source, tags.argValue);
				LoadedDlAsts.put(source, dlast.argValue);
				tags.argValue.dlAst = dlast.argValue;
				return true;
			}
			catch (RuntimeException e)
			{

				if (!BrokenImports.containsKey(rootontology != null ? rootontology : source))
				{
					BrokenImports.put(rootontology != null ? rootontology : source, new HashSet<String>());
				}
				BrokenImports.get(rootontology != null ? rootontology : source).add(source);
				if (!ErrorsOnImports.containsKey(rootontology != null ? rootontology : source))
				{
					ErrorsOnImports.put(rootontology != null ? rootontology : source, new HashSet<String>());
					ExceptionsOnImports.put(rootontology != null ? rootontology : source, new HashSet<RuntimeException>());
				}

				if (ErrorsOnImports.containsKey(rootontology != null ? rootontology : source) && !ErrorsOnImports.get(rootontology != null ? rootontology : source).contains(e.getMessage()))
				{
					ErrorsOnImports.get(rootontology != null ? rootontology : source).add(e.getMessage());
					ExceptionsOnImports.get(rootontology != null ? rootontology : source).add(e);
				}

				brokenImports.argValue = BrokenImports.get(source);
				return false;
			}
		}
		else // owl reference
		{
			try
			{
				try
				{
					// check if the input uri is local...
					String sourcePath = source.replace("file:/", ""); // remove the file:/ (this is there is it was already recognized as a file)
					sourcePath = Uri.UnescapeDataString(sourcePath);
					if (!fromString)
					{
						VirtualSiteIRIMapper mapper = null;
						if (Paths.get(sourcePath).getParent() == null && (new File(sourcePath)).isFile())
						{
							mapper = new VirtualSiteIRIMapper(this); // add a local iri mapper
							mapper.currDir = (new File(sourcePath)).getParent();
							source = UriFromFilePath(sourcePath);
						}
						else
						{
							if (!tangible.StringHelper.isNullOrEmpty(_currentFilePath) && Paths.get(sourcePath).getParent() == null && (new File(sourcePath)).isFile())
							{
								mapper = new VirtualSiteIRIMapper(this); // add a local iri mapper
								mapper.currDir = (new File(_currentFilePath)).getParent();
							}
							else
							{
								mapper = new VirtualSiteIRIMapper(this); // add a local iri mapper
								mapper.currDir = null;
							}
						}
						owlManager.Value.addIRIMapper(mapper);
					}
				}
				catch (java.lang.Exception e)
				{
				}

				if (!fromString && LoadedOntologies.containsKey(source))
				{
					if (!BrokenImports.containsKey(source) || missingReferencesStrategy == OWLMissingOntologyReferencesStrategy.Retry)
					{
						tags.argValue = LoadedOntologies.get(source);
						dlast.argValue = LoadedDlAsts.get(source);
						if (BrokenImports.containsKey(source))
						{
							brokenImports.argValue = BrokenImports.get(source);
							OWLReferencesProblem = true;
						}
						return true;
					}
					else
					{
						brokenImports.argValue = BrokenImports.get(source);
						return false;
					}
				}

				curentlyLoadingOnt = fromString ? "" : source;

				OWLOntology ontology = null;

				try
				{
					// http://ehc.ac/p/owlapi/mailman/message/28914114/

					boolean containsOntology = false;
					for (org.semanticweb.owlapi.model.OWLOntology owlOntology : owlManager.Value.getOntologies().toArray().ToList())
					{
						org.semanticweb.owlapi.model.IRI iri = owlOntology.getOntologyID().getVersionIRI();
						if (iri != null)
						{
							if (iri.compareTo(OWLPathUriTools.Path2IRI(source)) == 0 && !fromString)
							{
								ontology = owlOntology;
								containsOntology = true;
								break;
							}
						}
					}

					if (!fromString && convertToAst && !containsOntology)
					{
						ontology = owlManager.Value.loadOntology(OWLPathUriTools.Path2IRI(source));
					}

					/*
					 * some boolean algebra to prove that the previous if-elseif-else structure is equivalent to this.
					 *    a => fromString 
					 *    b => convertToAST
					 *    c => containsOntology
					 *    
					 *    Previously we had if (~a . b . ~c) {A} else if (~a . c) {B} else {C} 
					 *    
					 *    Reaching  A and B is clearly equivalent however C is not obvious. To prove it is equivalent we treat the previous else case as the complement of the union of A and B: 
					 *    
					 *    ~ ((~a . b . ~c) + (~a . c)) <=> ~(~a . (c + b . ~c)) <=> 
					 *    
					 *    ~(~a . (b + c)) <=> 
					 *
					 *     [ a          +       ~b          .       ~c      ]                                    
					 *      fromString || !containsOntology && !convertToAst
					 *      
					 *   The case C is now the else if below
					 */
					else if (fromString || !containsOntology && !convertToAst)
					{
						if (fromString)
						{
							org.semanticweb.owlapi.io.StringDocumentSource ontin = new org.semanticweb.owlapi.io.StringDocumentSource(source);
							ontology = owlManager.Value.loadOntologyFromOntologyDocument(ontin);
						}
						else
						{
							ontodoc = new XmlDocument();
							ontodoc.Load(source);
							org.semanticweb.owlapi.io.StringDocumentSource ontin = new org.semanticweb.owlapi.io.StringDocumentSource(ontodoc.OuterXml);
							ontology = owlManager.Value.loadOntologyFromOntologyDocument(ontin);
						}
					}

					if (NewError && missingReferencesStrategy != OWLMissingOntologyReferencesStrategy.Retry)
					{
						owlManager.Value.removeOntology(ontology);
						brokenImports.argValue = BrokenImports.get(source);
						return false;
					}
					else if (NewError && missingReferencesStrategy == OWLMissingOntologyReferencesStrategy.Retry)
					{
						brokenImports.argValue = BrokenImports.get(source);
						OWLReferencesProblem = true;
					}

					if (convertToAst)
					{
						cognipy.ars.InvTransform invtransform = new cognipy.ars.InvTransform(owlManager.Value, ontology, source, nck, getForms);

						dlast.argValue = SetOWLOntologyTagsAndAst(ontology, invtransform, source, defaultPfx, tags, useDefaultNamespaceAsFullUri);

						if (!tangible.StringHelper.isNullOrWhiteSpace(invtransform.defaultNs))
						{
							DefaultNamespace = invtransform.defaultNs;
						}

						HashMap<TKey, TValue>.KeyCollection refs = invtransform.Pfx2ns.keySet();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						for (var refi : refs)
						{
							if (!tangible.StringHelper.isNullOrEmpty(refi))
							{
								AllReferences.put(refi, invtransform.Pfx2ns.get(refi));
							}
						}

						if (isFirstLevel)
						{
							DirectImports = new HashMap<String, String>();
						}

						Iterator imports = ontology.getImportsDeclarations().iterator();
						while (imports.hasNext()) // there are direct imports....
						{
							// is it right not to add a direct import in DirectImports if reff is null?? 
							// This could be because no default namespace is defined inside but still there is this import....
							Object tempVar3 = imports.next();
							OWLImportsDeclaration impp = tempVar3 instanceof OWLImportsDeclaration ? (OWLImportsDeclaration)tempVar3 : null;
							OWLOntology impOnt = owlManager.Value.getImportedOntology(impp);

							String reff = impp.getIRI().toString();

							String impSource = reff;

							if (impOnt != null) // if imported ontology if different from null --> we have imported it, let's get the DocumentIRI (location) and OntoriogyID (namespace)
							{
								if (impOnt.getOntologyID().getOntologyIRI() != null)
								{

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
									var refKey = AllReferences.FirstOrDefault(reference -> reference.Value.Contains(reff)).Key;

									// replace the reff in AllReferences.
									reff = impOnt.getOntologyID().getOntologyIRI().toString();

									if (!reff.endsWith("/") && !reff.endsWith("#") && !reff.contains("#"))
									{
										reff += "#";
									}


									if (!tangible.StringHelper.isNullOrEmpty(refKey))
									{
										if (AllReferences.containsKey(refKey))
										{
											AllReferences.put(refKey, reff);
										}
									}

									IRI importedIRI = owlManager.Value.getOntologyDocumentIRI(impOnt);
									if (importedIRI != null)
									{
										// [a]
										impSource = importedIRI.toString();
									}
								}
							}

							if (!reff.endsWith("/") && !reff.endsWith("#") && !reff.contains("#"))
							{
								reff += "#";
							}

							// At this point impSource should be updated with the appropriate namespace so, all if-else if belowe would be wasteless
							// and should be replaced by  the single sentence:  
							if (!tangible.StringHelper.isNullOrEmpty(reff))
							{
								DirectImports.put(reff, impSource);
							}
							if (tangible.StringHelper.isNullOrEmpty(AllReferences.FirstOrDefault(reference -> reference.Value.Contains(reff)).Key) && tangible.StringHelper.isNullOrEmpty(AllReferences.FirstOrDefault(reference -> reference.Value.Contains(reff + "#")).Key) && tangible.StringHelper.isNullOrEmpty(AllReferences.FirstOrDefault(reference -> reference.Value.Contains(reff + "/")).Key))
							{
								String pfx = null;
								if (reff.split("[/]", -1) != null)
								{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
									var end = reff.split("[/]", -1).Last();
									pfx = end.Replace("#", "").Replace("/", "") + UUID.NewGuid();
								}
								else
								{
									pfx = reff + "pfx";
								}
								// this will solve a problem that appeared when a referenced ontology was using the same pfx used in the another referenced ontology or in the main ontology.
								// nevertheless it means that AllReferences will not contain all references. Maybe we should change the AllReferences to a <string,List<string>> dictionary?
								if (!AllReferences.containsKey(pfx))
								{
									AllReferences.put(pfx, reff);
								}
							}

							// if no error was found for the imported ontology, add the referencedTags and ast to the memory.
							if (!ErrorsOnImports.containsKey(reff) || !ErrorsOnImports.containsKey(reff.replace("#", "")))
							{
								String impPrefix = AllReferences.FirstOrDefault(x = reff.equals(> x.Value)).Key;
								ReferenceTags impTags;
								cognipy.cnl.dl.Paragraph impAst = null;

								// This case is reached for imports as http://purl.org/dc/elements/1.1/ which for some reason is detected as importable (no import error in the owlapi) but never gets imported
								if (impOnt != null)
								{
									invtransform = new cognipy.ars.InvTransform(owlManager.Value, impOnt, impSource, nck, getForms);
									tangible.OutObject<cognipy.ReferenceManager.ReferenceTags> tempOut_impTags = new tangible.OutObject<cognipy.ReferenceManager.ReferenceTags>();
									impAst = SetOWLOntologyTagsAndAst(impOnt, invtransform, impSource, impPrefix, tempOut_impTags, useDefaultNamespaceAsFullUri);
								impTags = tempOut_impTags.argValue;
									impTags.ontologyLocation = impSource;
									tags.argValue.referencedTags.add(impTags);
								}

								if (insertDependentAsts && impAst != null)
								{
									dlast.argValue.Statements.addAll(impAst.Statements);
								}
							}
						}
					}

					// BUG FIX START --- FE2-190 (http://ehc.ac/p/owlapi/mailman/message/28914114/)
					org.semanticweb.owlapi.model.IRI logIRI = ((org.semanticweb.owlapi.model.OWLOntology)ontology).getOntologyID().getOntologyIRI();
					if (logIRI == null)
					{
						String iri = source;
						if (!source.endsWith("/") && !source.endsWith("#") && !source.contains("#"))
						{
							iri += "#";
						}
						logIRI = IRI.create(iri); // if the ontology doesn't contain an IRI give the same as the source
					}

					org.semanticweb.owlapi.model.OWLOntologyID newID = new org.semanticweb.owlapi.model.OWLOntologyID(logIRI, OWLPathUriTools.Path2IRI(source));

					owlManager.Value.applyChange(new org.semanticweb.owlapi.model.SetOntologyID((org.semanticweb.owlapi.model.OWLOntology)ontology, newID));
					// BUG FIX END --- FE2-190 

					//owlManager.Value.removeOntology(ontology);
					return true;
				}
				catch (OWLOntologyAlreadyExistsException e2)
				{
					CreateOWLManager();
					ForgetOntology(source, false);
//C# TO JAVA CONVERTER TODO TASK: There is no 'goto' in Java:
					goto RETRY;
				}
				catch (RuntimeException ex)
				{
					if (ontology != null)
					{
						owlManager.Value.removeOntology(ontology);
					}
					if (BrokenImports.containsKey(source))
					{
						brokenImports.argValue = BrokenImports.get(source);
					}
					if (!ErrorsOnImports.containsKey(rootontology != null ? rootontology : source))
					{
						ErrorsOnImports.put(rootontology != null ? rootontology : source, new HashSet<String>());
						ExceptionsOnImports.put(rootontology != null ? rootontology : source, new HashSet<RuntimeException>());
					}

					if (ErrorsOnImports.containsKey(rootontology != null ? rootontology : source) && !ErrorsOnImports.get(rootontology != null ? rootontology : source).contains(ex.getMessage()))
					{
						ErrorsOnImports.get(rootontology != null ? rootontology : source).add(ex.getMessage());
						ExceptionsOnImports.get(rootontology != null ? rootontology : source).add(ex);
					}

					return false;
				}
			}
			catch (RuntimeException e)
			{
				if (BrokenImports.containsKey(source))
				{
					brokenImports.argValue = BrokenImports.get(source);
				}
				if (!ErrorsOnImports.containsKey(rootontology != null ? rootontology : source))
				{
					ErrorsOnImports.put(rootontology != null ? rootontology : source, new HashSet<String>());
					ExceptionsOnImports.put(rootontology != null ? rootontology : source, new HashSet<RuntimeException>());
				}

				if (ErrorsOnImports.containsKey(rootontology != null ? rootontology : source) && !ErrorsOnImports.get(rootontology != null ? rootontology : source).contains(e.getMessage()))
				{
					ErrorsOnImports.get(rootontology != null ? rootontology : source).add(e.getMessage());
					ExceptionsOnImports.get(rootontology != null ? rootontology : source).add(e);
				}
			}
		}
		return false;
	}


	public final CNL.DL.Paragraph SetOWLOntologyTagsAndAst(OWLOntology ontology, cognipy.ars.InvTransform invtransform, String source, String defaultPfx, tangible.OutObject<ReferenceTags> tags)
	{
		return SetOWLOntologyTagsAndAst(ontology, invtransform, source, defaultPfx, tags, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: internal CNL.DL.Paragraph SetOWLOntologyTagsAndAst(OWLOntology ontology, CogniPy.ARS.InvTransform invtransform, string source, string defaultPfx, out ReferenceTags tags, bool useDefaultNamespaceAsFullUri = false)
	public final cognipy.cnl.dl.Paragraph SetOWLOntologyTagsAndAst(OWLOntology ontology, cognipy.ars.InvTransform invtransform, String source, String defaultPfx, tangible.OutObject<ReferenceTags> tags, boolean useDefaultNamespaceAsFullUri)
	{
		tags.argValue = new ReferenceTags();
		cognipy.cnl.dl.Paragraph dlast = invtransform.Convert(ontology);
		tags.argValue.uriMapping = invtransform.UriMappings;
		tags.argValue.invUriMapping = invtransform.InvUriMappings;

		if (ontology.getOntologyID().getOntologyIRI() != null)
		{
			tags.argValue.baseNamespace = ontology.getOntologyID().getOntologyIRI().toString().split("[#]", -1).First();
		}
		else
		{
			tags.argValue.baseNamespace = source;
		}

		if (!tags.argValue.baseNamespace.endsWith("/") && !tags.argValue.baseNamespace.endsWith("#") && !tags.argValue.baseNamespace.contains("#"))
		{
			tags.argValue.baseNamespace += "#";
		}

		if (!tangible.StringHelper.isNullOrEmpty(defaultPfx))
		{
			tags.argValue.FENamespace = defaultPfx;
		}
		else // case in which there are no prefixes defined for this element.
		{
			String[] baseNamespEl = tags.argValue.baseNamespace.replace("#", "").split("[/]", -1);
			if (baseNamespEl.Count() > 0)
			{
				tags.argValue.FENamespace = baseNamespEl[baseNamespEl.Count() - 1];
			}
		}

		if (!tangible.StringHelper.isNullOrEmpty(defaultPfx))
		{
			cognipy.cnl.dl.SetDefaultPfxVisitor defPfxVis = new cognipy.cnl.dl.SetDefaultPfxVisitor(defaultPfx);
			defPfxVis.Visit(dlast);
		}
		else if (useDefaultNamespaceAsFullUri && !tangible.StringHelper.isNullOrWhiteSpace(tags.argValue.baseNamespace))
		{
			cognipy.cnl.dl.SetDefaultPfxVisitor defPfxVis = new cognipy.cnl.dl.SetDefaultPfxVisitor(null, tags.argValue.baseNamespace);
			defPfxVis.Visit(dlast);
		}

		cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(false);
		ser.Serialize(dlast);
		HashSet<Tuple<EntityKind, String>> sign = ser.GetTaggedSignature();


//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var smb : sign)
		{
			if (smb.Item1 == ARS.EntityKind.Instance)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var inam = smb.Item2;
				if (!tangible.StringHelper.isNullOrWhiteSpace(inam) && !inam.StartsWith("["))
				{
					if (inam.StartsWith("_"))
					{
						inam = inam.Substring(1);
					}
					tags.argValue.instances.add(inam);
				}
			}
			if (smb.Item1 == ARS.EntityKind.Role && !tangible.StringHelper.isNullOrWhiteSpace(smb.Item2))
			{
				tags.argValue.roles.add(smb.Item2);
			}
			if (smb.Item1 == ARS.EntityKind.DataRole && !tangible.StringHelper.isNullOrWhiteSpace(smb.Item2))
			{
				tags.argValue.dataroles.add(smb.Item2);
			}
			if (smb.Item1 == ARS.EntityKind.Concept && !tangible.StringHelper.isNullOrWhiteSpace(smb.Item2))
			{
				tags.argValue.concepts.add(smb.Item2);
			}
			if (smb.Item1 == ARS.EntityKind.DataType && !tangible.StringHelper.isNullOrWhiteSpace(smb.Item2))
			{
				tags.argValue.datatypes.add(smb.Item2);
			}
		}

		if (!LoadedOntologies.containsKey(source))
		{
			LoadedOntologies.put(source, tags.argValue);
			LoadedDlAsts.put(source, dlast);
		}

		tags.argValue.dlAst = dlast;

		return dlast;
	}

	private boolean isForgetAllCommand = false;
	public final void ForgetAllOntologies()
	{
		isForgetAllCommand = true;
		CreateOWLManager();


		Object[] k = LoadedOntologies.keySet().ToArray();
		for (Object uri : k)
		{
			ForgetOntology(uri, false);
		}

		Object[] k2 = BrokenImports.keySet().ToArray();
		for (Object uri : k2)
		{
			ForgetOntology(uri, false);
		}

		ExceptionsOnImports = new HashMap<String, HashSet<RuntimeException>>();
		BrokenImports = new HashMap<String, HashSet<String>>();
		ErrorsOnImports = new HashMap<String, HashSet<String>>();
	}

	// If forgetOntology = true --> also remove the ontology from the owlManager (--> the only way I found is by creating again the owl manager)

	public final void ForgetOntology(String uri)
	{
		ForgetOntology(uri, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void ForgetOntology(string uri, bool resetOntologyManager = true)
	public final void ForgetOntology(String uri, boolean resetOntologyManager) // remove all informations about this ontology in the refence manager.
	{
		boolean isendl = false;
		try
		{
			if (System.IO.Path.GetExtension(uri).equals(".encnl"))
			{
				isendl = true;
			}
		}
		catch (java.lang.Exception e)
		{
			return;
		}


		if (!isendl) // for owl/rdf/....
		{
			if (!isForgetAllCommand && resetOntologyManager)
			{
				if (owlManager.Value.getOntology(IRI.create(uri)) != null)
				{
					CreateOWLManager(); // !! this will erase ALL ontologies previously loaded.... it could not be what we expect when we are cleaning only one ontology!
				}
				// The problem is that using  owlManager.Value.removeOntology(owlManager.Value.getOntology(IRI.create(uri))); is not removing the referenced ontologies errors
				// --> if we do not do this, the next time we will load the ontology the missing import listener is not called again....
			}

			try
			{
				if (Paths.get(uri).getParent() == null)
				{
					if ((new File(uri)).isFile())
					{
						uri = UriFromFilePath(uri);
					}
				}
			}
			catch (java.lang.Exception e2)
			{
			}
		}

		if (LoadedOntologies.containsKey(uri)) // remove the ontology if loaded
		{
			//if (owlManager.Value.getOntology(IRI.create(uri)) != null)
			//    owlManager.Value.removeOntology(owlManager.Value.getOntology(IRI.create(uri)));
			LoadedOntologies.remove(uri);
			LoadedDlAsts.remove(uri);
		}

		if (BrokenImports.containsKey(uri)) // forget all import problem
		{
			for (String relOnt : BrokenImports.get(uri))
			{
				if (ExceptionsOnImports.containsKey(relOnt))
				{ // remove exceptions
					ExceptionsOnImports.remove(relOnt);
				}

				if (ErrorsOnImports.containsKey(relOnt)) // remove errors
				{
					ErrorsOnImports.remove(relOnt);
				}
			}
			BrokenImports.remove(uri);
		}

		if (ExceptionsOnImports.containsKey(uri))
		{
			ExceptionsOnImports.remove(uri);
		}

		if (ErrorsOnImports.containsKey(uri))
		{
			ErrorsOnImports.remove(uri);
		}
	}

	private OWLOntologyLoaderConfiguration config = new OWLOntologyLoaderConfiguration();
	public final void setConnectionProxy(String host, int port)
	{
		setConnectionProxy(host, port, null, null);
	}

	public final void setConnectionProxy(String host, int port, String userName, String password)
	{
		if (!tangible.StringHelper.isNullOrWhiteSpace(host))
		{
			java.net.SocketAddress addr = new java.net.InetSocketAddress(host, port);
			java.net.Proxy proxy = new java.net.Proxy(java.net.Proxy.Type.HTTP, addr);
			if (!tangible.StringHelper.isNullOrWhiteSpace(userName) && !tangible.StringHelper.isNullOrWhiteSpace(password))
			{
				config = config.setProxy(proxy, userName, password);
			}
			else
			{
				config = config.setProxy(proxy);
			}
		}
		else
		{
			config = config.setProxy(null);
		}
		if (owlManager.IsValueCreated)
		{
			owlManager.Value.setOntologyLoaderConfiguration(config);
		}
	}
}