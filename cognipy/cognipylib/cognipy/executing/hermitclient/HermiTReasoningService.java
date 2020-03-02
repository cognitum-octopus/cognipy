package cognipy.executing.hermitclient;

import cognipy.ars.*;
import cognipy.cnl.*;
import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import cognipy.executing.hermit.*;
import cognipy.models.*;
import com.clarkparsia.owlapi.explanation.*;
import org.semanticweb.owlapi.apibinding.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.profiles.*;
import org.semanticweb.owlapi.reasoner.*;
import org.semanticweb.owlapi.reasoner.impl.*;
import org.semanticweb.owlapi.reasoner.structural.*;
import org.semanticweb.owlapi.util.*;
import cognipy.*;
import java.io.*;
import java.util.*;

public class HermiTReasoningService
{
	//IKVM bugfix
	private static com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl s = new com.sun.org.apache.xerces.internal.jaxp.SAXParserFactoryImpl();
	//IKVM bugfix

	public static class ProgressEventArgs extends tangible.EventArgs
	{
		private String msg;
		private int Value;
		public final int getValue()
		{
			return Value;
		}
		private void setValue(int value)
		{
			Value = value;
		}
		private int Maximum;
		public final int getMaximum()
		{
			return Maximum;
		}
		private void setMaximum(int value)
		{
			Maximum = value;
		}

		public ProgressEventArgs(String messageData, int value, int maximum)
		{
			msg = messageData;
			setValue(value);
			setMaximum(maximum);
		}
		public final String getMessage()
		{
			return msg;
		}
		public final void setMessage(String value)
		{
			msg = value;
		}
	}
	public tangible.EventHandler<ProgressEventArgs> Progress;

	private static class ProgressMonitor implements ReasonerProgressMonitor
	{
		private HermiTReasoningService me;
		public ProgressMonitor(HermiTReasoningService me)
		{
			this.me = me;
		}
		public final void reasonerTaskBusy()
		{
			me.fireReasonerTaskBusy();
		}
		public final void reasonerTaskProgressChanged(int i1, int i2)
		{
			me.fireReasonerTaskProgressChanged(i1, i2);
		}
		public final void reasonerTaskStarted(String str)
		{
			me.fireReasonerTaskStarted(str);
		}
		public final void reasonerTaskStopped()
		{
			me.fireReasonerTaskStopped();
		}
	}

	public static class DebugTraceEventArgs extends tangible.EventArgs
	{
		private String TraceMessage;
		public final String getTraceMessage()
		{
			return TraceMessage;
		}
		private void setTraceMessage(String value)
		{
			TraceMessage = value;
		}
		private HashMap<String, Tuple<String, Object>> Binding;
		public final HashMap<String, Tuple<String, Object>> getBinding()
		{
			return Binding;
		}
		private void setBinding(HashMap<String, Tuple<String, Object>> value)
		{
			Binding = value;
		}
		public DebugTraceEventArgs(String tm, HashMap<String, Tuple<String, Object>> bnd)
		{
			setTraceMessage(tm);
			setBinding(bnd);
		}
	}

	public tangible.EventHandler<DebugTraceEventArgs> DebugTrace;

	private void fireDebugTraceMessage(String msg, HashMap<String, Tuple<String, Object>> bnd)
	{
		if (DebugTrace != null)
		{
			DebugTrace.invoke(this, new DebugTraceEventArgs(msg, bnd));
		}
	}

	private void fireReasonerTaskBusy()
	{
		if (Progress != null)
		{
			Progress.invoke(this, new ProgressEventArgs("Busy", 0, 0));
		}
	}

	private void fireReasonerTaskProgressChanged(int i1, int i2)
	{
		if (Progress != null)
		{
			Progress.invoke(this, new ProgressEventArgs("Progress: " + (i2 != 0 ? (i1 * 100 / i2).toString() : "0") + "%", i1, i2));
		}
	}

	private void fireReasonerTaskStarted(String str)
	{
		if (Progress != null)
		{
			Progress.invoke(this, new ProgressEventArgs("Task " + str, 0, 0));
		}
	}

	private void fireReasonerTaskStopped()
	{
		if (Progress != null)
		{
			Progress.invoke(this, new ProgressEventArgs("Stopped", 0, 0));
		}
	}

	private OWLOntologyManager manager;

	private String ontologyBase;
	private IRI ontologyIRI;
	private OWLOntology ontology;
	private OWLReasoner reasoner;
	private OWLReasoner structural_reasoner = null;
	private boolean materializing_reasner_supports_sroiq = true;

	public final SPARQL.Transform getSparqlTransform()
	{
		return sparqlTransform;
	}

	private cognipy.ars.Transform transform = new cognipy.ars.Transform();
	private cognipy.sparql.Transform sparqlTransform = new SPARQL.Transform();
	private cognipy.ars.InvTransform invtransform;
	private cognipy.cnl.dl.Paragraph sourceParagraph = null;
	private HashSet<org.apache.jena.graph.Triple> sourceTriplets = null;

	private cognipy.cnl.dl.Paragraph swrlRulesWIthBuiltInsParagraph = new cognipy.cnl.dl.Paragraph(null) {Statements = new ArrayList<Statement>()};

	private static String SerializeDoc(XmlDocument doc)
	{
		XmlWriterSettings settings = new XmlWriterSettings();
		settings.Indent = true;
		settings.IndentChars = "  ";
		settings.NewLineOnAttributes = false;
		settings.NamespaceHandling = NamespaceHandling.OmitDuplicates;
		settings.Encoding = Encoding.UTF8;
		settings.OmitXmlDeclaration = true;

		StringBuilder sb = new StringBuilder();
		sb.append("<?xml version = '1.0' encoding = 'UTF-8'?>" + "\r\n");
		XmlWriter writer = XmlWriter.Create(sb, settings);
		doc.Save(writer);
		return sb.toString();
	}


	public final String GetOWLXML(boolean includeImplicitValue)
	{
		return GetOWLXML(includeImplicitValue, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string GetOWLXML(bool includeImplicitValue, bool fresh = true)
	public final String GetOWLXML(boolean includeImplicitValue, boolean fresh)
	{
		{
			OWLOntologyManager manager;
			OWLOntology ontology;
			if (fresh)
			{
				manager = OWLManager.createOWLOntologyManager();
				ontology = manager.createOntology(ontologyIRI);
				manager.setOntologyFormat(ontology, owlxmlFormat);
				cognipy.ars.Transform.Axioms conv = transform.Convert(GetParagraph(includeImplicitValue));
				manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.axioms));
				manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.additions));
			}
			else
			{
				manager = this.manager;
				ontology = this.ontology;
			}

			XmlDocument XMLdoc = new XmlDocument();
			org.semanticweb.owlapi.io.StringDocumentTarget ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
			manager.saveOntology(ontology, owlxmlFormat, ontout);
			XMLdoc.LoadXml(ontout.toString());
			return SerializeDoc(XMLdoc);
		}
	}

	private String GetTurtle(boolean includeImplicitValue, boolean fresh)
	{
		{
			OWLOntologyManager manager = null;
			OWLOntology ontology = null;
			try
			{
				if (fresh)
				{
					manager = OWLManager.createOWLOntologyManager();
					ontology = manager.createOntology(ontologyIRI);
					manager.setOntologyFormat(ontology, turtleFormat);
					cognipy.ars.Transform.Axioms conv = transform.Convert(GetParagraph(includeImplicitValue));
					manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.axioms));
					manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.additions));
				}
				else
				{
					manager = this.manager;
					ontology = this.ontology;
				}

				org.semanticweb.owlapi.io.StringDocumentTarget ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
				manager.saveOntology(ontology, turtleFormat, ontout);
				return ontout.toString();
			}
			finally
			{
				manager.setOntologyFormat(ontology, owlxmlFormat);
			}
		}
	}

	private java.lang.Iterable<Tuple<Statement, String>> GetTurtleStream()
	{
		for (Statement stmt : GetParagraph(false).Statements)
		{
			if (stmt instanceof Annotation)
			{
				continue;
			}
			OWLOntologyManager manager = null;
			OWLOntology ontology = null;
			manager = OWLManager.createOWLOntologyManager();
			ontology = manager.createOntology(ontologyIRI);
			manager.setOntologyFormat(ontology, turtleFormat);
			cognipy.ars.Transform.Axioms conv = transform.Convert(new Paragraph(null, stmt));
			manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.axioms));
			manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.additions));

			org.semanticweb.owlapi.io.StringDocumentTarget ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
			manager.saveOntology(ontology, turtleFormat, ontout);
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
			yield return Tuple.<Statement, String>Create(stmt, ontout.toString());
		}
	}

	private org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat owlxmlFormat = null;
	private org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat turtleFormat = null;
	private ImpliKBVisitor impliKBVis = new ImpliKBVisitor();
	private cognipy.ars.IOwlNameingConvention namc;
	private HashMap<Tuple<EntityKind, String>, String> uriMappings;
	private HashMap<String, String> invUriMappings;

	public final boolean getContainsSWRLBuiltIns()
	{
		return !swrlRulesWIthBuiltInsParagraph.Statements.isEmpty();
	}

	public boolean exeRulesOn = false;
	public boolean debugModeOn = false;
	public boolean modalChecker = false;

	public OWLDataFactory df = null;
	public HermiTReasoningService(cognipy.cnl.dl.Paragraph ps, cognipy.cnl.dl.Paragraph impliAst, ReasoningMode rmode, cognipy.ars.IOwlNameingConvention namc, String ontologyBase, HashMap<Tuple<EntityKind, String>, String> uriMappings, HashMap<String, String> invUriMappings, HashMap<String, String> prefixes)
	{
		if (impliAst != null)
		{
			for (Statement s : impliAst.Statements)
			{
				ps.Statements.add(s);
			}
		}

		DLModSimplifier simli = new DLModSimplifier();
		Object tempVar = simli.Visit(ps);
		CNL.DL.Paragraph p = tempVar instanceof CNL.DL.Paragraph ? (CNL.DL.Paragraph)tempVar : null;

		SwrlBuiltinsExtractor swe = new SwrlBuiltinsExtractor();
		Tuple<Paragraph, Paragraph> split = swe.Split(java.util.regex.Pattern.quote(String.valueOf(p)), -1);
		sourceParagraph = split.Item1;
		sourceTriplets = new HashSet<org.apache.jena.graph.Triple>();
		swrlRulesWIthBuiltInsParagraph = split.Item2;
		if (impliAst != null)
		{
			impliKBVis.Import(impliAst);
		}
		this.namc = namc;
		this.uriMappings = uriMappings;
		this.invUriMappings = invUriMappings;

		manager = OWLManager.createOWLOntologyManager();
		if (!ontologyBase.endsWith("/") && !ontologyBase.endsWith("#") && !ontologyBase.contains("#"))
		{
			ontologyBase += "#";
		}

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var k : prefixes.keySet().ToList())
		{
			if (!prefixes.get(k).endsWith("/") && !prefixes.get(k).endsWith("#") && !prefixes.get(k).contains("#"))
			{
				prefixes.put(k, prefixes.get(k) + "#");
			}
		}

		this.ontologyBase = ontologyBase;
		ontologyIRI = IRI.create(ontologyBase);
		ontology = manager.createOntology(ontologyIRI);
		df = manager.getOWLDataFactory();

		owlxmlFormat = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
		turtleFormat = new org.coode.owlapi.turtle.TurtleOntologyFormat(); // new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();

		owlxmlFormat.setDefaultPrefix(ontologyBase);
		turtleFormat.setDefaultPrefix(ontologyBase);
		for (Map.Entry<String, String> kv : prefixes.entrySet())
		{
			owlxmlFormat.setPrefix(kv.getKey(), kv.getValue());
			turtleFormat.setPrefix(kv.getKey(), kv.getValue());
		}

		manager.setOntologyFormat(ontology, owlxmlFormat);

		transform.setOWLDataFactory(true, ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);
		sparqlTransform.setOWLDataFactory(ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);

		transform.setInvUriMappings(invUriMappings);
		sparqlTransform.setInvUriMappings(invUriMappings);

		cognipy.ars.Transform.Axioms conv = transform.Convert(sourceParagraph);
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.axioms));
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.additions));

		invtransform = new cognipy.ars.InvTransform(manager, ontology, owlxmlFormat, CNL.EN.CNLFactory.lex, namc);
		invtransform.InvUriMappings = invUriMappings;
		invtransform.UriMappings = uriMappings;

		try
		{
			if (rmode == ReasoningMode.STRUCTURAL)
			{
				reasoner = new StructuralReasoner(ontology, new SimpleConfiguration(new ProgressMonitor(this)), BufferingMode.BUFFERING);
			}
			else if (rmode == ReasoningMode.RL || rmode == ReasoningMode.SWRL)
			{
				return;
			}
			else
			{
				OWL2ELProfile elProfile = new OWL2ELProfile();
				OWLProfileReport report = elProfile.checkOntology(ontology);

				boolean inEl = true;
				Iterator it = report.getViolations().iterator();
				while (it.hasNext())
				{
					OWLProfileViolation v = (OWLProfileViolation)it.next();
					if (!(v instanceof UseOfUndeclaredClass) && !(v instanceof UseOfNonAbsoluteIRI) && !(v instanceof UseOfUndeclaredObjectProperty) && !(v instanceof UseOfUndeclaredDataProperty))
					{
						inEl = false;
						break;
					}
				}

				if (inEl)
				{
					materializing_reasner_supports_sroiq = false;
					structural_reasoner = new StructuralReasoner(ontology, new SimpleConfiguration(new ProgressMonitor(this)), BufferingMode.BUFFERING);
					SimpleConfiguration configuration = new SimpleConfiguration(new ProgressMonitor(this));
					OWLReasonerFactory rf = new org.semanticweb.elk.owlapi.ElkReasonerFactory();
					reasoner = rf.createReasoner(ontology, configuration);
				}
				else
				{
					org.semanticweb.HermiT.Configuration configuration = new org.semanticweb.HermiT.Configuration();
					configuration.reasonerProgressMonitor = new ProgressMonitor(this);
					configuration.throwInconsistentOntologyException = false;
					reasoner = new org.semanticweb.HermiT.Reasoner(configuration, ontology);
				}
			}
		}
		catch (RuntimeException ex)
		{
			throw new ReasoningServiceException(ex.getMessage().Replace("<" + ontologyIRI.toString() + "#", "'").Replace(">", "'") + ".");
		}
	}

	private HermiTReasoningService(HermiTReasoningService other, org.apache.jena.rdf.model.Model copyOfOntModel)
	{
		sourceParagraph = other.sourceParagraph;
		sourceTriplets = other.sourceTriplets;
		swrlRulesWIthBuiltInsParagraph = other.swrlRulesWIthBuiltInsParagraph;
		this.namc = other.namc;
		this.uriMappings = other.uriMappings;
		this.invUriMappings = other.invUriMappings;

		this.transform.setOWLDataFactory(true, other.ontologyBase, other.df, other.owlxmlFormat, CNL.EN.CNLFactory.lex);
		this.sparqlTransform.setOWLDataFactory(other.ontologyBase, other.df, other.owlxmlFormat, CNL.EN.CNLFactory.lex);

		this.transform.setInvUriMappings(other.invUriMappings);
		this.sparqlTransform.setInvUriMappings(other.invUriMappings);

		this.invtransform = new cognipy.ars.InvTransform(other.manager, other.ontology, other.owlxmlFormat, CNL.EN.CNLFactory.lex, other.namc);
		this.invtransform.InvUriMappings = other.invUriMappings;
		this.invtransform.UriMappings = other.uriMappings;

		manager = other.manager;
		this.ontologyBase = other.ontologyBase;
		ontologyIRI = other.ontologyIRI;
		ontology = other.ontology;

		this.reasoner = null; // other.reasoner;
		this.model = copyOfOntModel;
	}

	private org.apache.jena.reasoner.rulesys.GenericRuleReasoner bindedRsnr;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public final HermiTReasoningService Clone(dynamic TheAccessObject, dynamic Outer)
	{

		synchronized (this)
		{
			if (bindedRsnr == null)
			{
				((org.apache.jena.rdf.model.impl.InfModelImpl)model).prepare();
				bindedRsnr = (org.apache.jena.reasoner.rulesys.GenericRuleReasoner)((org.apache.jena.reasoner.rulesys.GenericRuleReasoner)rete_reasoner).bindSchema(model.getGraph());
				JenaRuleManager.BootstrapReasonerForAboxChanges(bindedRsnr, rete_reasoner, TheAccessObject, Outer);
			}


			org.apache.jena.rdf.model.InfModel newModel = org.apache.jena.rdf.model.ModelFactory.createInfModel(bindedRsnr, org.apache.jena.rdf.model.ModelFactory.createDefaultModel());
			return new HermiTReasoningService(this, newModel);
		}
	}

	public static CNL.DL.Paragraph SimplifyDL(CNL.DL.Paragraph paragraph)
	{
		DLModSimplifier simli = new DLModSimplifier();
		Object tempVar = simli.Visit(paragraph);
		cognipy.cnl.dl.Paragraph paragraphSimplified = tempVar instanceof cognipy.cnl.dl.Paragraph ? (cognipy.cnl.dl.Paragraph)tempVar : null;
		return paragraphSimplified;
	}


	public final String renderEntityFromUri(String uri)
	{
		return renderEntityFromUri(uri, ARS.EntityKind.Instance);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string renderEntityFromUri(string uri, ARS.EntityKind kind = ARS.EntityKind.Instance)
	public final String renderEntityFromUri(String uri, ARS.EntityKind kind)
	{
		return invtransform.renderEntity(uri, kind);
	}


	public final String renderUriFromEntity(String cnl)
	{
		return renderUriFromEntity(cnl, ARS.EntityKind.Instance);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string renderUriFromEntity(string cnl, ARS.EntityKind kind = ARS.EntityKind.Instance)
	public final String renderUriFromEntity(String cnl, ARS.EntityKind kind)
	{
		return transform.getIRIFromDL(cnl, kind).toString();
	}

	private static final boolean CALCULATE_DISJOINTS = false;

	private org.apache.jena.rdf.model.Model model = null;

	public static class SparqlRowset
	{
		private org.apache.jena.query.ResultSet results;
		private org.apache.jena.shared.PrefixMapping prefixMap;
		private org.apache.jena.rdf.model.Model model;
		private boolean detectTypesOfNodes;
		private String defaultKindOfNode;
		private String defaultNS;


		public SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results, String defaultNS, org.apache.jena.shared.PrefixMapping prefixMap, boolean detectTypesOfNodes)
		{
			this(model, results, defaultNS, prefixMap, detectTypesOfNodes, null);
		}

		public SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results, String defaultNS, org.apache.jena.shared.PrefixMapping prefixMap)
		{
			this(model, results, defaultNS, prefixMap, true, null);
		}

		public SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results, String defaultNS)
		{
			this(model, results, defaultNS, null, true, null);
		}

		public SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results)
		{
			this(model, results, null, null, true, null);
		}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: internal SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results, string defaultNS = null, org.apache.jena.shared.PrefixMapping prefixMap = null, bool detectTypesOfNodes = true, string defaultKindOfNode = null)
		public SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results, String defaultNS, org.apache.jena.shared.PrefixMapping prefixMap, boolean detectTypesOfNodes, String defaultKindOfNode)
		{
			this.results = results;
			this.prefixMap = prefixMap;
			this.defaultNS = defaultNS;
			this.model = model;
			this.detectTypesOfNodes = detectTypesOfNodes;
			this.defaultKindOfNode = defaultKindOfNode;
		}

		public final String GetTypeOfNode(org.apache.jena.rdf.model.Model context, org.apache.jena.graph.Node n)
		{
			org.apache.jena.graph.Graph infgraph = context.getGraph();

			boolean isInstance = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			boolean isRole = false;
			boolean isDataRole = false;
			if (!isInstance)
			{
				isRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode());
			}
			if (!isInstance && !isRole)
			{
				isDataRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());
			}

			boolean isConcept = (!isInstance && !isRole && !isDataRole);
			return (isInstance ? "instance" : (isConcept ? "concept" : (isRole ? "role" : "datarole")));
		}

		private ArrayList<Object> Convert(ArrayList<Object> value)
		{

			if (prefixMap == null)
			{
				return value;
			}

			Set keySet = prefixMap.getNsPrefixMap().keySet();

			Iterator it = keySet.iterator();

			Object tempVar = value.<String>OfType();
			java.lang.Iterable<Object> uris = tempVar instanceof java.lang.Iterable<Object> ? (java.lang.Iterable<Object>)tempVar : null;

			Object[] val = value.toArray(new Object[0]);

			while (it.hasNext())
			{

				String prefix = (String)it.next();

				for (int i = 0; i < val.Count(); i++)
				{
					if (uris.Contains(val[i]))
					{
						val[i] = ((String)val[i]).replace(prefixMap.getNsPrefixURI(prefix), (String)prefix + ":").replace(defaultNS, "");
					}
				}
			}

			return val.ToList();

		}


		public final ArrayList<String> GetCols()
		{
			ArrayList<String> res = new ArrayList<String>();
			List names = results.getResultVars();
			Iterator namesIter = names.iterator();
			while (namesIter.hasNext())
			{
				String varId = namesIter.next().toString();
				res.add(varId);
			}
			return res;
		}

		//            public static object CommonLock = new object();

		public final java.lang.Iterable<ArrayList<Object>> GetRows()
		{
			ArrayList<String> keys = GetCols();
			while (results.hasNext())
			{
				org.apache.jena.query.QuerySolution qbs = null;
				try
				{
					//                        lock (CommonLock)
					qbs = results.next();
				}
				catch (NoSuchElementException e)
				{
					if (qbs == null)
					{
						break;
					}
				}
				ArrayList<Object> row = new ArrayList<Object>();
				boolean blank = false;
				for (String key : keys)
				{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var val = qbs.get(key);
					if (val == null)
					{
						row.add(null);
					}
					else if (val instanceof org.apache.jena.rdf.model.Literal)
					{
						Object v = cognipy.sparql.SparqlNode.ToTypedValue((val instanceof org.apache.jena.rdf.model.Literal ? (org.apache.jena.rdf.model.Literal)val : null).toString());
						if (v == null)
						{
							v = JenaRuleManager.getObject(val.asNode());
						}
						row.add(v);
					}
					else if (val.asNode() != null && val.asNode().isBlank())
					{
						blank = true;
						break;
					}
					else
					{
						if (this.detectTypesOfNodes)
						{
							String type = GetTypeOfNode(model, val.asNode());
							cognipy.GraphEntity tempVar = new cognipy.GraphEntity();
							tempVar.setName(val.toString());
							tempVar.setKind(type);
							row.add(tempVar.clone());
						}
						else
						{
							cognipy.GraphEntity tempVar2 = new cognipy.GraphEntity();
							tempVar2.setName(val.toString());
							tempVar2.setKind(defaultKindOfNode);
							row.add(tempVar2.clone());
						}
					}
				}
				if (!blank)
				{
//C# TO JAVA CONVERTER TODO TASK: Java does not have an equivalent to the C# 'yield' keyword:
					yield return row;
				}

			}



		}
	}

	public final String GetOntologyID()
	{
		return ontology.getOntologyID().getOntologyIRI().toString();
	}


	public final SparqlRowset SparqlQuery(String queryString, java.util.HashMap<String, String> pfx2NsMap, boolean detectTypesOfNodes)
	{
		return SparqlQuery(queryString, pfx2NsMap, detectTypesOfNodes, null);
	}

	public final SparqlRowset SparqlQuery(String queryString, java.util.HashMap<String, String> pfx2NsMap)
	{
		return SparqlQuery(queryString, pfx2NsMap, true, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SparqlRowset SparqlQuery(string queryString, Dictionary<string, string> pfx2NsMap, bool detectTypesOfNodes = true, string defaultKindOfNode = null)
	public final SparqlRowset SparqlQuery(String queryString, HashMap<String, String> pfx2NsMap, boolean detectTypesOfNodes, String defaultKindOfNode)
	{
		fireReasonerTaskStarted("SPARQL query");
		fireReasonerTaskProgressChanged(0, 1);
		BuildModel();

		// Add the default namespace in the query

		org.apache.jena.shared.PrefixMapping prefixMap = new org.apache.jena.shared.impl.PrefixMappingImpl();

		//prefixMap.setNsPrefix("", ontology.getOntologyID().getOntologyIRI().toString());
		//prefixMap.setNsPrefix("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
		//prefixMap.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		//prefixMap.setNsPrefix("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
		//prefixMap.setNsPrefix("xml", "http://www.w3.org/XML/1998/namespace#");
		//prefixMap.setNsPrefix("owl", "http://www.w3.org/2002/07/owl#");
		String prefixes = "PREFIX : <" + ontology.getOntologyID().getOntologyIRI().toString() + ">\n";

		prefixes += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
		prefixes += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
		prefixes += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
		prefixes += "PREFIX xml: <http://www.w3.org/XML/1998/namespace#>\n";
		prefixes += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";

		if (pfx2NsMap != null)
		{
			for (Map.Entry<String, String> pfx2ns : pfx2NsMap.entrySet())
			{
				if (!queryString.contains("PREFIX " + pfx2ns.getKey() + ":") && !prefixes.contains("PREFIX " + pfx2ns.getKey() + ":"))
				{
					prefixes += "PREFIX " + pfx2ns.getKey() + ": <" + pfx2ns.getValue() + ">\n";
				}
			}
		}
		queryString = prefixes + queryString;
		org.apache.jena.query.Query query = org.apache.jena.query.QueryFactory.create(queryString);

		org.apache.jena.shared.PrefixMapping origPrefixMap = query.getPrefixMapping();

		Set keySet = prefixMap.getNsPrefixMap().keySet();

		Iterator it = keySet.iterator();

		org.apache.jena.shared.PrefixMapping newPrefixMap = new org.apache.jena.shared.impl.PrefixMappingImpl();

		// Add all well known prefixes, but replace those that are directly defined by the user

		while (it.hasNext())
		{

			String prefix = (String)it.next();

			if (origPrefixMap.getNsPrefixURI(prefix) != null)
			{
				newPrefixMap.setNsPrefix(prefix, origPrefixMap.getNsPrefixURI(prefix));
			}

			else
			{
				newPrefixMap.setNsPrefix(prefix, prefixMap.getNsPrefixURI(prefix));
			}
		}

		// Now add those that are left from origPrefix
		keySet = origPrefixMap.getNsPrefixMap().keySet();

		it = keySet.iterator();

		while (it.hasNext())
		{
			String prefix = (String)it.next();

			if (newPrefixMap.getNsPrefixURI(prefix) == null)
			{
				newPrefixMap.setNsPrefix(prefix, origPrefixMap.getNsPrefixURI(prefix));
			}
		}

		query.setPrefixMapping(newPrefixMap);

		org.apache.jena.query.QueryExecution qe = org.apache.jena.query.QueryExecutionFactory.create(query, model);
		//            lock (SparqlRowset.CommonLock)
		{
			org.apache.jena.query.ResultSet results = qe.execSelect();
			return new SparqlRowset(model, results, GetOntologyID(), newPrefixMap, detectTypesOfNodes, defaultKindOfNode);
		}
	}


	private static class MyListener implements org.apache.jena.rdf.model.ModelChangedListener
	{
		private tangible.Action1Param<org.apache.jena.rdf.model.Statement> call;
		public MyListener(tangible.Action1Param<org.apache.jena.rdf.model.Statement> call)
		{
			this.call = (org.apache.jena.rdf.model.Statement obj) -> call.invoke(obj);
		}
		public final void addedStatement(org.apache.jena.rdf.model.Statement s)
		{
			call.invoke(s);
		}

		public final void addedStatements(org.apache.jena.rdf.model.Model m)
		{
		}
		public final void addedStatements(org.apache.jena.rdf.model.StmtIterator si)
		{
		}
		public final void addedStatements(ArrayList l)
		{
		}
		public final void addedStatements(org.apache.jena.rdf.model.Statement[] sarr)
		{
		}
		public final void notifyEvent(org.apache.jena.rdf.model.Model m, Object obj)
		{
		}
		public final void removedStatement(org.apache.jena.rdf.model.Statement s)
		{
		}
		public final void removedStatements(org.apache.jena.rdf.model.Model m)
		{
		}
		public final void removedStatements(org.apache.jena.rdf.model.StmtIterator si)
		{
		}
		public final void removedStatements(ArrayList l)
		{
		}
		public final void removedStatements(org.apache.jena.rdf.model.Statement[] sarr)
		{
		}
	}

	private HashMap<String, CNL.DL.Statement> blankStmts = null;

	private void addBlankStmt(String id, Statement stmt)
	{
		if (!blankStmts.containsKey(id))
		{
			blankStmts.put(id, stmt);
		}
	}

	private void BuildModel()
	{

		if (model != null)
		{
			return;
		}

		model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();

		if (true)
		{
			blankStmts = new HashMap<String, Statement>();
			CNL.DL.Statement curstmt = null;
			MyListener lst = new MyListener((stmt) ->
			{
					if (stmt.getObject().isAnon())
					{
						addBlankStmt(stmt.getObject().asNode().getBlankNodeId().toString(), curstmt);
					}
					if (stmt.getPredicate().isAnon())
					{
						addBlankStmt(stmt.getPredicate().asNode().getBlankNodeId().toString(), curstmt);
					}
					if (stmt.getSubject().isAnon())
					{
						addBlankStmt(stmt.getSubject().asNode().getBlankNodeId().toString(), curstmt);
					}
			});
			model.register(lst);
			for (Tuple<Statement, String> turt : GetTurtleStream())
			{
				curstmt = turt.Item1;
				model.read(new StringReader(turt.Item2), "", "TURTLE");
			}
			model.unregister(lst);
		}
		else
		{
			model.read(new StringReader(GetTurtle(true, false)), "", "TURTLE");
		}
		for (Tuple<String, String, Object> trip : AdditionalTriplets)
		{
			org.apache.jena.rdf.model.RDFNode node = model.getRDFNode(JenaRuleManager.getLiteral(trip.Item3));
			model.add(model.getResource(transform.getIRIFromDL(trip.Item1, EntityKind.Instance).toString()), model.getProperty(transform.getIRIFromDL(trip.Item2, EntityKind.DataRole).toString()), node);

		}

		AdditionalTriplets.clear();
	}

	private CNL.DL.Paragraph inferfedAbox = null;

	public final Value getValFromJenaLiteral(org.apache.jena.graph.impl.LiteralLabel node)
	{
		Object val = node.getValue();
		if (val instanceof java.lang.Double || val instanceof java.lang.Float || val instanceof Double || val instanceof Float)
		{
			return new CNL.DL.Float(null, val.toString());
		}
		else if (val instanceof java.lang.Number || val instanceof java.lang.Integer || val instanceof java.lang.Long || val instanceof Integer || val instanceof Long)
		{
			return new CNL.DL.Number(null, val.toString());
		}
		else if (val instanceof java.lang.String || val instanceof String)
		{
			return new CNL.DL.String(null, "\'" + val.toString().replace("\'", "\'\'") + "\'");
		}
		else if (val instanceof java.lang.Boolean || val instanceof Boolean)
		{
			return new CNL.DL.Bool(null, val.toString().equals("true") ? "[1]" : "[0]");
		}
		else if (val instanceof org.apache.jena.datatypes.xsd.XSDDateTime)
		{
			return new CNL.DL.DateTimeVal(null, val.toString());
		}
		else if (val instanceof org.apache.jena.datatypes.xsd.XSDDuration)
		{
			return new CNL.DL.Duration(null, val.toString());
		}

		throw new UnsupportedOperationException("Unimplemented datatype");
	}

	private void InvalidateSyncOntologyToModel()
	{
		inferfedAbox = null;
	}

	public final void SyncOntologyToModel()
	{
		if (model == null)
		{
			return;
		}

		if (inferfedAbox != null)
		{
			return;
		}

		inferfedAbox = new Paragraph(null);
		inferfedAbox.Statements = new ArrayList<Statement>();

		org.apache.jena.graph.Graph dedGraph = ((org.apache.jena.reasoner.InfGraph)((org.apache.jena.rdf.model.InfModel)model).getGraph()).getDeductionsGraph();

		org.apache.jena.util.iterator.ExtendedIterator triplets = dedGraph.find(null, null, null);

		while (triplets.hasNext())
		{
			AddTripleStmt((org.apache.jena.graph.Triple)triplets.next(), inferfedAbox);
		}

	}

	public final void AddTripleStmt(org.apache.jena.graph.Triple stmt, Paragraph inferfedAbox)
	{
		org.apache.jena.graph.Node s = stmt.getSubject();
		org.apache.jena.graph.Node v = stmt.getPredicate();
		org.apache.jena.graph.Node o = stmt.getObject();
		if (s.isBlank() || v.isBlank() || o.isBlank())
		{
			return;
		}

		if (s.isURI() && v.isURI())
		{
			if (s.getURI().equals(org.apache.jena.vocabulary.OWL.Nothing.getURI()))
			{
				return;
			}

			if (v.getURI().equals(org.apache.jena.vocabulary.RDF.type.getURI()))
			{
				if (o.isURI())
				{
					if (o.getURI().equals(org.apache.jena.vocabulary.OWL.DatatypeProperty.getURI()))
					{
						//role declaration (top data role)
						CNL.DL.DataRoleInclusion st = new CNL.DL.DataRoleInclusion(null);
						st.C = new CNL.DL.Atomic(null);
						st.C.id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.DataRole);
						st.D = new CNL.DL.Top(null);
						inferfedAbox.Statements.add(st);
					}
					else if (o.getURI().equals(org.apache.jena.vocabulary.OWL.ObjectProperty.getURI()))
					{
						//role declaration (top object role)
						CNL.DL.RoleInclusion st = new CNL.DL.RoleInclusion(null);
						st.C = new CNL.DL.Atomic(null);
						st.C.id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Role);
						st.D = new CNL.DL.Top(null);
						inferfedAbox.Statements.add(st);
					}
					else if (o.getURI().equals(org.apache.jena.vocabulary.OWL2.NamedIndividual.getURI()))
					{
						//class declaration (top concept)
						String un = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
						CNL.DL.InstanceOf st = new CNL.DL.InstanceOf(null);
						st.I = new CNL.DL.NamedInstance(null);
						st.I.name = un;
						st.C = new CNL.DL.Top(null);
						inferfedAbox.Statements.add(st);
					}
					else if (o.getURI().equals(org.apache.jena.vocabulary.OWL.Class.getURI()))
					{
						//class declaration (top concept)
						String un = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept);
						CNL.DL.Subsumption st = new CNL.DL.Subsumption(null);
						st.C = new CNL.DL.Atomic(null);
						st.C.id = un;
						st.D = new CNL.DL.Top(null);
						inferfedAbox.Statements.add(st);
					}
					else //instance type
					{
						if (!o.getURI().equals(org.apache.jena.vocabulary.OWL.Thing.getURI()))
						{
							String un = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
							CNL.DL.InstanceOf st = new CNL.DL.InstanceOf(null);
							st.I = new CNL.DL.NamedInstance(null);
							st.I.name = un;
							st.C = new CNL.DL.Atomic(null);
							st.C.id = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Concept);
							inferfedAbox.Statements.add(st);
						}
					}
				}
				else
				{
					//unknown stuff
					return;
				}
			}
			else if (v.getURI().equals(org.apache.jena.vocabulary.RDFS.subClassOf.getURI()))
			{
				if (o.isURI())
				{
					if (!s.getURI().equals(o.getURI()))
					{
						if (o.getURI().equals(org.apache.jena.vocabulary.OWL.Thing.getURI()))
						{
							CNL.DL.Subsumption st = new CNL.DL.Subsumption(null);
							st.C = new CNL.DL.Atomic(null);
							st.C.id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept);
							st.D = new CNL.DL.Top(null);
							inferfedAbox.Statements.add(st);
						}
						else
						{ //subclassof
							CNL.DL.Subsumption st = new CNL.DL.Subsumption(null);
							st.C = new CNL.DL.Atomic(null);
							st.C.id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept);
							st.D = new CNL.DL.Atomic(null);
							st.D.id = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Concept);
							inferfedAbox.Statements.add(st);
						}
					}
				}
			}
			else if (v.getURI().equals(org.apache.jena.vocabulary.OWL.equivalentClass.getURI()))
			{
				if (o.isURI())
				{
					if (!s.getURI().equals(o.getURI()))
					{ //equivalent
						CNL.DL.Equivalence st = new CNL.DL.Equivalence(null);
						CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
						tempVar.id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept);
						CNL.DL.Atomic tempVar2 = new CNL.DL.Atomic(null);
						tempVar2.id = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Concept);
						st.Equivalents = new ArrayList<CNL.DL.Node>(Arrays.asList(tempVar, tempVar2));
						inferfedAbox.Statements.add(st);
					}
				}
			}
			else if (v.getURI().equals(org.apache.jena.vocabulary.OWL.sameAs.getURI()))
			{
				if (o.isURI())
				{
					if (!s.getURI().equals(o.getURI()))
					{
						//same as
						CNL.DL.SameInstances st = new CNL.DL.SameInstances(null);
						CNL.DL.NamedInstance tempVar3 = new CNL.DL.NamedInstance(null);
						tempVar3.name = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
						CNL.DL.NamedInstance tempVar4 = new CNL.DL.NamedInstance(null);
						tempVar4.name = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Instance);
						st.Instances = new ArrayList<Instance>(Arrays.asList(tempVar3, tempVar4));
						inferfedAbox.Statements.add(st);
					}
				}
				else
				{
					//unknown stuff
					return;
				}
			}
			else if (!v.getURI().startsWith("http://www.w3.org/"))
			{
				if (o.isURI())
				{
					CNL.DL.RelatedInstances st = new CNL.DL.RelatedInstances(null);
					st.I = new CNL.DL.NamedInstance(null);
					st.I.name = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
					st.R = new CNL.DL.Atomic(null);
					st.R.id = invtransform.renderEntity(v.getURI(), ARS.EntityKind.Role);
					st.J = new CNL.DL.NamedInstance(null);
					st.J.name = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Instance);
					inferfedAbox.Statements.add(st);
				}
				else if (o.isLiteral())
				{
					CNL.DL.InstanceValue st = new CNL.DL.InstanceValue(null);
					st.I = new CNL.DL.NamedInstance(null);
					st.I.name = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
					st.R = new CNL.DL.Atomic(null);
					st.R.id = invtransform.renderEntity(v.getURI(), ARS.EntityKind.Role);
					st.V = getValFromJenaLiteral(o.getLiteral());
					inferfedAbox.Statements.add(st);
				}
			}
		}
	}

	public final CNL.DL.Paragraph GetMaterializedRLParagrah(boolean includeImplicitKnowledge)
	{
		SyncOntologyToModel();

		CNL.DL.Paragraph toRet = new CNL.DL.Paragraph(null);
		toRet.Statements = new ArrayList<CNL.DL.Statement>();

		if (inferfedAbox != null)
		{

			cognipy.cnl.dl.Paragraph para = invtransform.Convert(ontology);

			for (Statement s : inferfedAbox.Statements)
			{
				if (includeImplicitKnowledge || !impliKBVis.IsEntailed(s))
				{
					toRet.Statements.add(s);
				}
			}
		}
		return toRet;
	}


	public final CNL.DL.Paragraph GetParagraph(boolean includeImplicitKnowledge)
	{
		return GetParagraph(includeImplicitKnowledge, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public CNL.DL.Paragraph GetParagraph(bool includeImplicitKnowledge, bool includeMaterialized = true)
	public final CNL.DL.Paragraph GetParagraph(boolean includeImplicitKnowledge, boolean includeMaterialized)
	{
		ArrayList<CNL.DL.Statement> stmts = new ArrayList<Statement>();
		stmts.addAll(sourceParagraph.Statements);

		{ //Add asserted statements
			Paragraph pr1 = new Paragraph(null);
			pr1.Statements = new ArrayList<Statement>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var stmt : sourceTriplets)
			{
				AddTripleStmt(stmt, pr1);
			}
			stmts.addAll(pr1.Statements);
		}

		stmts.addAll(swrlRulesWIthBuiltInsParagraph.Statements);
		if (includeImplicitKnowledge)
		{
			if (includeMaterialized)
			{
				SyncOntologyToModel();
				if (inferfedAbox != null)
				{
					HashSet<String> aboxAlready = new HashSet<String>();
					cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(false);
					//                        DLModSimplifier simli = new DLModSimplifier();
					//                        var p = simli.Visit(toRet) as CNL.DL.Paragraph;

					for (CNL.DL.Statement s : stmts)
					{
						if (IsABox(s))
						{
							aboxAlready.add(ser.Serialize(s));
						}
					}
					for (Statement s : inferfedAbox.Statements)
					{
						if (includeImplicitKnowledge || !impliKBVis.IsEntailed(s))
						{
							if (aboxAlready.add(ser.Serialize(s)))
							{
								stmts.add(s);
							}
						}
					}
				}
			}
		}
		CNL.DL.Paragraph tempVar = new CNL.DL.Paragraph(null);
		tempVar.Statements = stmts;
		return tempVar;
	}


	public final java.lang.Iterable<ConstraintResult> GetAllConstraints()
	{
		ArrayList<ConstraintResult> results = new ArrayList<ConstraintResult>();

		for (Statement s : swrlRulesWIthBuiltInsParagraph.Statements)
		{
			if (s.modality != Statement.Modality.IS)
			{
				Subsumption subsumptn = ((Subsumption)s);
				ConstraintResult result = new ConstraintResult();
				if (results.Any(cr = ((Atomic)subsumptn.C).id.equals(> cr.Concept)))
				{
					result = results.stream().filter(cr = ((Atomic)subsumptn.C).id.equals(> cr.Concept)).findFirst();
				}


				result.setConcept(((Atomic)subsumptn.C).id);
				if (result.getRelations() == null)
				{
					result.setRelations(new HashMap<Statement.Modality, ArrayList<String>>());
				}
				if (result.getThirdElement() == null)
				{
					result.setThirdElement(new HashMap<Statement.Modality, ArrayList<String>>());
				}

				if (!result.getRelations().containsKey(subsumptn.modality))
				{
					result.getRelations().put(s.modality, new ArrayList<String>());
				}
				if (!result.getThirdElement().containsKey(subsumptn.modality))
				{
					result.getThirdElement().put(s.modality, new ArrayList<String>());
				}

				if (subsumptn.D instanceof SomeRestriction)
				{
					result.getRelations().get(s.modality).add(((Atomic)((SomeRestriction)subsumptn.D).R).id);
					result.getThirdElement().get(s.modality).add(((Atomic)((SomeRestriction)subsumptn.D).C).id);
				}
				else if (subsumptn.D instanceof SomeValueRestriction)
				{
					result.getRelations().get(s.modality).add(((Atomic)((SomeValueRestriction)subsumptn.D).R).id);
					String type = "";
					switch (((TotalBound)((SomeValueRestriction)subsumptn.D).B).V.getTypeTag())
					{
						case "I":
							type = "integer";
							break;
						case "S":
							type = "string";
							break;
						case "F":
							type = "real";
							break;
						case "B":
							type = "boolean";
							break;
						case "T":
							type = "datetime";
							break;
						case "D":
							type = "duration";
							break;
						default:
							throw new UnsupportedOperationException();
					}

					result.getThirdElement().get(s.modality).add("(some " + type + " value)");
				}

				if (!results.Any(cr = ((Atomic)subsumptn.C).id.equals(> cr.Concept)))
				{
					results.add(result);
				}
			}
		}

		return results;
	}

	private boolean isMaterializedTbox = false;
	private boolean isMaterializedAbox = false;


	public final boolean Materialization(ReasoningMode TBox, ReasoningMode ABox, boolean isAboxInsertOnly)
	{
		return Materialization(TBox, ABox, isAboxInsertOnly, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public bool Materialization(ReasoningMode TBox, ReasoningMode ABox, bool isAboxInsertOnly, bool modalChecker = false)
	public final boolean Materialization(ReasoningMode TBox, ReasoningMode ABox, boolean isAboxInsertOnly, boolean modalChecker)
	{
		if (isMaterializedAbox)
		{
			return false;
		}

		if (isMaterializedTbox && TBox != ReasoningMode.NONE)
		{
			return false;
		}

		if (TBox == ReasoningMode.SROIQ && ABox == ReasoningMode.SROIQ)
		{
			MaterializeSROIQ(MatMode.Both, isAboxInsertOnly);
			isMaterializedTbox = true;
			isMaterializedAbox = true;
		}
		else if (TBox == ReasoningMode.RL && ABox == ReasoningMode.RL)
		{
			MaterializeRL(MatMode.Both, true, true, modalChecker, false);
			isMaterializedTbox = true;
			isMaterializedAbox = true;
		}
		else
		{
			if (TBox == ReasoningMode.SROIQ)
			{
				MaterializeSROIQ(MatMode.Tbox, isAboxInsertOnly);
				isMaterializedTbox = true;
			}
			if (TBox == ReasoningMode.RL)
			{
				isMaterializedTbox = true;
				MaterializeRL(MatMode.Tbox, true, true, modalChecker, false);
			}
			if (ABox == ReasoningMode.SROIQ)
			{
				MaterializeSROIQ(MatMode.Abox, isAboxInsertOnly);
				isMaterializedAbox = true;
			}
			if (ABox == ReasoningMode.RL)
			{
				isMaterializedAbox = true;
				MaterializeRL(MatMode.Abox, true, true, modalChecker, false);
			}
			if (ABox == ReasoningMode.SWRL)
			{
				isMaterializedAbox = true;
				MaterializeRL(MatMode.SWRLOnly, true, true, modalChecker, true);
			}
		}
		return true;
	}

//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public dynamic TheAccessObject;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	public dynamic Outer;

	public String WD;

	private HashMap<String, Statement> id2stmt = new HashMap<String, Statement>();


	private void MaterializeRL(MatMode mode, boolean extended, boolean sameAs, boolean modalChecker)
	{
		MaterializeRL(mode, extended, sameAs, modalChecker, false);
	}

	private void MaterializeRL(MatMode mode, boolean extended, boolean sameAs)
	{
		MaterializeRL(mode, extended, sameAs, false, false);
	}

	private void MaterializeRL(MatMode mode, boolean extended)
	{
		MaterializeRL(mode, extended, true, false, false);
	}

	private void MaterializeRL(MatMode mode)
	{
		MaterializeRL(mode, true, true, false, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: private void MaterializeRL(MatMode mode, bool extended = true, bool sameAs = true, bool modalChecker = false, bool swrlOnly = false)
	private void MaterializeRL(MatMode mode, boolean extended, boolean sameAs, boolean modalChecker, boolean swrlOnly)
	{
		fireReasonerTaskStarted("RL Materialization");
		fireReasonerTaskProgressChanged(0, 2);
		JenaRuleManager.Setup();
		BuildModel();

		org.apache.jena.rdf.model.Model src_model = model;

		List rules = JenaRuleManager.GetGeneralRules(mode, extended, sameAs, debugModeOn);

		cognipy.cnl.dl.Paragraph para = GetParagraph(true, false);


//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if USE_DYNAMICRULES
		CalcDynamicDepths cdd = new CalcDynamicDepths();
		cdd.Visit(para);

		rules.addAll(JenaRuleManager.GetDynamicRules(mode, cdd.IntersectionDepth, cdd.UnionDepth,cdd.HasKeyDepth));
//#endif
		tangible.Action2Param<String, HashMap<String, Tuple<String, Object>>> DebugAction = (String arg1, HashMap<String, Tuple<String, Object>> arg2) ->
		{
				 fireDebugTraceMessage(s, d);
		};
		HashMap<Integer, Tuple<String, ArrayList<IExeVar>>> ExeRules = new HashMap<Integer, Tuple<String, ArrayList<IExeVar>>>();
		HashMap<Integer, SwrlIterate> SwrlIterators = new HashMap<Integer, SwrlIterate>();
		boolean wdSet = false;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var stmt : para.Statements.Union(swrlRulesWIthBuiltInsParagraph.Statements))
		{
			if (stmt instanceof ComplexRoleInclusion || stmt instanceof Subsumption || stmt instanceof HasKey || (mode != MatMode.Tbox && (stmt instanceof SwrlStatement || stmt instanceof ExeStatement || stmt instanceof SwrlIterate) || (stmt.modality != Statement.Modality.IS)))
			{
				GenerateJenaRules gen = new GenerateJenaRules(model, modalChecker, mode != MatMode.Tbox, debugModeOn, exeRulesOn, debugModeOn, swrlOnly);
				gen.setOWLDataFactory(ontologyBase, owlxmlFormat, CNL.EN.CNLFactory.lex);
				gen.setId2stmt(id2stmt);
				Paragraph tempVar = new Paragraph(null);
				tempVar.Statements = new ArrayList<Statement>(Arrays.asList(stmt));
				String scr = gen.Generate(tempVar);
				rules.addAll(JenaRuleManager.GetRule(scr));
				for (Map.Entry<Integer, Tuple<String, ArrayList<IExeVar>>> r : gen.TheRules.entrySet())
				{
					ExeRules.put(r.getKey(), r.getValue());
				}
				for (Map.Entry<Integer, SwrlIterate> r : gen.TheIterators.entrySet())
				{
					SwrlIterators.put(r.getKey(), r.getValue());
				}

			}
			else if (stmt instanceof CodeStatement)
			{
				if (!wdSet)
				{
					if (WD != null)
					{
						Outer.Evaluate("setwd(\'" + WD.replace('\\', '/') + "')");
					}
					wdSet = true;
				}
				String code = (stmt instanceof CodeStatement ? (CodeStatement)stmt : null).exe;
				code = code.substring(2, 2 + code.length() - 4);
				code = code.replace("??>", "?>");
				Outer.Evaluate(code);
			}
		}

		SwrlIterateProc sproc = new SwrlIterateProc(model, swrlOnly);
		sproc.setOWLDataFactory(ontologyBase, owlxmlFormat, CNL.EN.CNLFactory.lex);

		rete_reasoner = JenaRuleManager.CreateReasoner(rules, DebugAction, ExeRules, SwrlIterators, TheAccessObject, Outer, invtransform, sproc);

		model = org.apache.jena.rdf.model.ModelFactory.createInfModel(rete_reasoner, src_model);
		((org.apache.jena.rdf.model.InfModel)model).setDerivationLogging(true);

		fireReasonerTaskProgressChanged(1, 2);
		fireReasonerTaskProgressChanged(2, 2);
		fireReasonerTaskStopped();

	}
	/**
	 * Convert an (assumed well formed) RDF list to a java list of Nodes
	 * @param root the root node of the list
	 * @param context the graph containing the list assertions
	 */
	public static ArrayList<org.apache.jena.graph.Node> convertList(org.apache.jena.graph.Node root, org.apache.jena.graph.Graph context)
	{
		return convertList(root, context, new ArrayList<org.apache.jena.graph.Node>());
	}

	/**
	 * Convert an (assumed well formed) RDF list to a java list of Nodes
	 */
	private static ArrayList<org.apache.jena.graph.Node> convertList(org.apache.jena.graph.Node node, org.apache.jena.graph.Graph context, ArrayList<org.apache.jena.graph.Node> sofar)
	{
		if (node == null || node.equals(org.apache.jena.vocabulary.RDF.nil.asNode()))
		{
			return sofar;
		}
		org.apache.jena.graph.Node next = org.apache.jena.reasoner.rulesys.Util.getPropValue(node, org.apache.jena.vocabulary.RDF.first.asNode(), context);
		if (next != null)
		{
			sofar.add(next);
			return convertList(org.apache.jena.reasoner.rulesys.Util.getPropValue(node, org.apache.jena.vocabulary.RDF.rest.asNode(), context), context, sofar);
		}
		else
		{
			return sofar;
		}
	}

	private String triple2Cnl(org.apache.jena.graph.Triple tri, org.apache.jena.reasoner.InfGraph infGraph)
	{
		if (tri.getMatchSubject().isBlank() || tri.getMatchObject().isBlank() || tri.getMatchPredicate().isBlank())
		{
			return null;
		}

		Statement stmt = null;
		if (tri.getMatchPredicate().getURI().equals(org.apache.jena.vocabulary.RDF.type.getURI()))
		{
			String sn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Instance);
			String cn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Concept);
			stmt = new InstanceOf(null);
			stmt.C = new CNL.DL.Atomic(null);
			stmt.C.id = cn;
			stmt.I = new CNL.DL.NamedInstance(null);
			stmt.I.name = sn;
		}
		else if (tri.getMatchPredicate().getURI().equals(org.apache.jena.vocabulary.RDFS.subPropertyOf.getURI()))
		{
			boolean isDataRole = infGraph.contains(tri.getMatchObject(), org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());

			if (isDataRole)
			{
				String cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.DataRole);
				String dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.DataRole);
				stmt = new DataRoleInclusion(null);
				stmt.C = new CNL.DL.Atomic(null);
				stmt.C.id = cn;
				stmt.D = new CNL.DL.Atomic(null);
				stmt.D.id = dn;
			}
			else
			{
				String cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Role);
				String dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Role);
				stmt = new RoleInclusion(null);
				stmt.C = new CNL.DL.Atomic(null);
				stmt.C.id = cn;
				stmt.D = new CNL.DL.Atomic(null);
				stmt.D.id = dn;
			}
		}
		else if (tri.getMatchPredicate().getURI().equals(org.apache.jena.vocabulary.RDFS.subClassOf.getURI()))
		{
			if (tri.getMatchObject().getURI().equals(org.apache.jena.vocabulary.OWL.Thing.getURI()))
			{
				String cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Concept);
				stmt = new Subsumption(null);
				stmt.C = new CNL.DL.Atomic(null);
				stmt.C.id = cn;
				stmt.D = new CNL.DL.Top(null);
			}
			else
			{
				String cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Concept);
				String dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Concept);
				stmt = new Subsumption(null);
				stmt.C = new CNL.DL.Atomic(null);
				stmt.C.id = cn;
				stmt.D = new CNL.DL.Atomic(null);
				stmt.D.id = dn;
			}
		}
		else if (tri.getMatchPredicate().getURI().equals(org.apache.jena.vocabulary.OWL.equivalentClass.getURI()))
		{
			String cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Concept);
			String dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Concept);
			stmt = new Equivalence(null);
			CNL.DL.Atomic tempVar = new CNL.DL.Atomic(null);
			tempVar.id = cn;
			CNL.DL.Atomic tempVar2 = new CNL.DL.Atomic(null);
			tempVar2.id = dn;
			stmt.Equivalents = new ArrayList<CNL.DL.Node>(Arrays.asList(tempVar, tempVar2));
		}
		else if (tri.getMatchPredicate().getURI().equals(org.apache.jena.vocabulary.OWL.sameAs.getURI()))
		{
			String i = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Instance);
			String j = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Instance);
			stmt = new SameInstances(null);
			CNL.DL.NamedInstance tempVar3 = new CNL.DL.NamedInstance(null);
			tempVar3.name = i;
			CNL.DL.NamedInstance tempVar4 = new CNL.DL.NamedInstance(null);
			tempVar4.name = j;
			stmt.Instances = new ArrayList<CNL.DL.Instance>(Arrays.asList(tempVar3, tempVar4));
		}
		else
		{
			String sn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Instance);
			String pn = renderEntityFromUri(tri.getMatchPredicate().getURI(), EntityKind.Role);
			if (tri.getMatchObject().isLiteral())
			{
				cognipy.cnl.dl.Value on = getValFromJenaLiteral(tri.getMatchObject().getLiteral());
				stmt = new InstanceValue(null);
				stmt.V = on;
				stmt.R = new CNL.DL.Atomic(null);
				stmt.R.id = pn;
				stmt.I = new CNL.DL.NamedInstance(null);
				stmt.I.name = sn;
			}
			else
			{
				String on = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Instance);
				stmt = new RelatedInstances(null);
				stmt.R = new CNL.DL.Atomic(null);
				stmt.R.id = pn;
				stmt.I = new CNL.DL.NamedInstance(null);
				stmt.I.name = sn;
				stmt.J = new CNL.DL.NamedInstance(null);
				stmt.J.name = on;
			}
		}

		if (stmt != null)
		{
			return TheAccessObject.ToCNL(stmt);
		}
		else
		{
			return null;
		}
	}

	private String blankToCnl(org.apache.jena.graph.Node blk)
	{
		if (blankStmts.containsKey(blk.getBlankNodeId().toString()))
		{
			return TheAccessObject.ToCNL(blankStmts.get(blk.getBlankNodeId().toString()));
		}
		else
		{
			return blk.getBlankNodeId().toString();
		}
	}

	private String JESC(String s)
	{
		return JavaScriptStringEncode(s, true);
	}

	public static String JavaScriptStringEncode(String value)
	{
		return JavaScriptStringEncode(value, false);
	}

	public static String JavaScriptStringEncode(String value, boolean addDoubleQuotes)
	{
		if (tangible.StringHelper.isNullOrEmpty(value))
		{
			return addDoubleQuotes ? "\"\"" : "";
		}

		int len = value.length();
		boolean needEncode = false;
		char c;
		for (int i = 0; i < len; i++)
		{
			c = value.charAt(i);

			if (c >= 0 && c <= 31 || c == 34 || c == 39 || c == 60 || c == 62 || c == 92)
			{
				needEncode = true;
				break;
			}
		}

		if (!needEncode)
		{
			return addDoubleQuotes ? "\"" + value + "\"" : value;
		}

		StringBuilder sb = new StringBuilder();
		if (addDoubleQuotes)
		{
			sb.append('"');
		}

		for (int i = 0; i < len; i++)
		{
			c = value.charAt(i);
			if (c >= 0 && c <= 7 || c == 11 || c >= 14 && c <= 31 || c == 39 || c == 60 || c == 62)
			{
				sb.append(String.format("\\u%1$.4x", (int)c));
			}
			else
			{
				switch ((int)c)
				{
					case 8:
						sb.append("\\b");
						break;

					case 9:
						sb.append("\\t");
						break;

					case 10:
						sb.append("\\n");
						break;

					case 12:
						sb.append("\\f");
						break;

					case 13:
						sb.append("\\r");
						break;

					case 34:
						sb.append("\\\"");
						break;

					case 92:
						sb.append("\\\\");
						break;

					default:
						sb.append(c);
						break;
				}
			}
		}

		if (addDoubleQuotes)
		{
			sb.append('"');
		}

		return sb.toString();
	}

	private String JVAL(Object o)
	{
		if (o instanceof String)
		{
			return JESC(o.toString());
		}
		else
		{
			return o.toString();
		}
	}

	protected final Tuple<ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>>, HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>>> GetReasoningInfoDetails()
	{
		return Tuple.Create(JenaRuleManager.GetOntologyErrors(rete_reasoner), JenaRuleManager.GetModalValidationResult(rete_reasoner));
	}

	protected final void printErrors(TextWriter strWriter, ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>> errors)
	{
		strWriter.WriteLine(JESC("errors") + ":[");
		boolean addComma = false;
		for (Tuple<String, String, ArrayList<Tuple<Object, String>>> error : errors)
		{
			if (addComma)
			{
				strWriter.Write(',');
			}
			else
			{
				addComma = true;
			}
			String title = error.Item1;
			String content = error.Item2;
			ArrayList<Tuple<Object, String>> vals = error.Item3;
			strWriter.WriteLine("{" + JESC("title") + ":" + JESC(title) + ",");
			strWriter.WriteLine(JESC("content") + ":" + JESC(content) + ",");
			strWriter.WriteLine(JESC("vals") + ":{");
			boolean addComma2 = false;
			for (Tuple<Object, String> val : vals)
			{
				if (addComma2)
				{
					strWriter.Write(',');
				}
				else
				{
					addComma2 = true;
				}
				if (val.Item2.equals("value"))
				{
					strWriter.WriteLine(JESC(val.Item2) + ":" + JVAL(val.Item1));
				}
				else
				{
					strWriter.WriteLine(JESC(val.Item2) + ":" + JESC(val.Item1.toString()));
				}
			}
			strWriter.WriteLine("}");
			strWriter.WriteLine("}");
		}
		strWriter.WriteLine("]");
	}

	protected final void printModals(TextWriter strWriter, HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> modals)
	{
		strWriter.WriteLine(JESC("modals") + ":{");
		boolean addComma = false;
		for (Map.Entry<String, ArrayList<LinkedDictionary<String, JenaValue>>> kv : modals.entrySet())
		{
			if (addComma)
			{
				strWriter.Write(',');
			}
			else
			{
				addComma = true;
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var key = kv.getKey();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var content = kv.getValue();
			strWriter.WriteLine(JESC(key) + ":[");
			boolean addComma2 = false;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var cc : content)
			{
				if (addComma2)
				{
					strWriter.Write(',');
				}
				else
				{
					addComma2 = true;
				}
				strWriter.WriteLine("{");
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				for (var kv2 : cc)
				{
					if (kv2.Value.IsInstance)
					{
						strWriter.WriteLine(JESC(kv2.Key) + ":{\"instance\":" + JESC(kv2.Value.Value.toString()) + "}");
					}
					else
					{
						strWriter.WriteLine(JESC(kv2.Key) + ":{\"value\":" + JVAL(kv2.Value.Value) + "}");
					}
				}
				strWriter.WriteLine("}");
			}
			strWriter.WriteLine("]");
		}
		strWriter.WriteLine("}");
	}

	public final String GetReasoningInfo()
	{
		StringWriter strWriter = new StringWriter();

		Tuple<ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>>, HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>>> status = GetReasoningInfoDetails();
		ArrayList<Tuple<String, String, ArrayList<Tuple<Object, String>>>> errors = status.Item1;
		HashMap<String, ArrayList<LinkedDictionary<String, JenaValue>>> modals = status.Item2;
		if (errors.isEmpty() && modals.isEmpty())
		{
			return "";
		}

		strWriter.Write("{");
		if (!errors.isEmpty())
		{
			printErrors(strWriter, errors);
			if (!modals.isEmpty())
			{
				strWriter.Write(",");
			}
		}

		if (!modals.isEmpty())
		{
			printModals(strWriter, modals);
		}

		strWriter.Write("}");
		return strWriter.toString();

	}


	protected final void printTrace(TextWriter strWriter, org.apache.jena.reasoner.InfGraph infGraph, org.apache.jena.reasoner.rulesys.RuleDerivation me, int indent, HashSet<org.apache.jena.reasoner.rulesys.RuleDerivation> seen, HashSet<String> alreadyPrinted)
	{
		strWriter.Write(tangible.StringHelper.repeatChar(' ', indent) + "{");

		if (id2stmt.containsKey(me.getRule().getName()))
		{
			strWriter.WriteLine(JESC("rule") + ":" + JESC(TheAccessObject.ToCNL(id2stmt.get(me.getRule().getName()))) + ",");
		}

		String tr = triple2Cnl(me.getConclusion(), infGraph);
		if (tr != null)
		{
			strWriter.Write(JESC("concluded") + ":" + JESC(tr) + ",");
		}

		strWriter.WriteLine(JESC("by") + ":[");
		int margin = indent + 4;
		boolean addComma = false;
		for (int i = 0; i < me.getMatches().size(); i++)
		{

			org.apache.jena.graph.Triple match = (org.apache.jena.graph.Triple)me.getMatches().get(i);

			Iterator derivations = infGraph.getDerivation(match);
			if (derivations == null || !derivations.hasNext())
			{
				if (addComma)
				{
					strWriter.WriteLine(",");
				}
				strWriter.Write(tangible.StringHelper.repeatChar(' ', margin) + "{");
				if (match == null)
				{
					// A primitive
					org.apache.jena.reasoner.rulesys.ClauseEntry term = me.getRule().getBodyElement(i);
					if (term instanceof org.apache.jena.reasoner.rulesys.Functor)
					{
						strWriter.Write(JESC("func") + ":" + JESC(((org.apache.jena.reasoner.rulesys.Functor)term).getName() + "()"));
					}
					else
					{
						strWriter.Write(JESC("builtin") + ":" + JESC("<not-implemented-yet>"));
					}
				}
				else
				{
					if (match.getMatchSubject().isBlank())
					{
						strWriter.Write(JESC("expr") + ":" + JESC(blankToCnl(match.getMatchSubject())));
					}
					else if (match.getMatchPredicate().isBlank())
					{
						strWriter.Write(JESC("expr") + ":" + JESC(blankToCnl(match.getMatchPredicate())));
					}
					else if (match.getMatchObject().isBlank())
					{
						strWriter.Write(JESC("expr") + ":" + JESC(blankToCnl(match.getMatchObject())));
					}
					else
					{
						strWriter.Write(JESC("expr") + ":" + JESC(triple2Cnl(match, infGraph)));
					}
				}
				strWriter.Write("}");
				addComma = true;
			}
			else
			{
				if (addComma)
				{
					strWriter.WriteLine(",");
				}
				org.apache.jena.reasoner.rulesys.RuleDerivation derivation = (org.apache.jena.reasoner.rulesys.RuleDerivation)derivations.next();
				if (seen.contains(derivation))
				{
					strWriter.Write(tangible.StringHelper.repeatChar(' ', margin));
					strWriter.WriteLine(JESC("alreadyShown") + ":" + JESC(triple2Cnl(match, infGraph)));
				}
				else
				{
					seen.add(derivation);
					printTrace(strWriter, infGraph, derivation, margin, seen, alreadyPrinted);
				}
				addComma = true;
			}
		}
		strWriter.WriteLine("]}");
	}


	private String GetDeriv(org.apache.jena.util.iterator.ExtendedIterator iter1)
	{
		StringWriter wr = new StringWriter();
		while (iter1.hasNext())
		{
			org.apache.jena.graph.Triple stmt = (org.apache.jena.graph.Triple)iter1.next();
			Iterator iter2 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).getDerivation(stmt);
			while (iter2.hasNext())
			{
				org.apache.jena.reasoner.rulesys.RuleDerivation deriv = (org.apache.jena.reasoner.rulesys.RuleDerivation)iter2.next();
				printTrace(wr, ((org.apache.jena.reasoner.InfGraph)model.getGraph()), deriv, 0, new HashSet<org.apache.jena.reasoner.rulesys.RuleDerivation>(), new HashSet<String>());
			}
		}
		return wr.toString();
	}

	public final String GetIsADerivation(String C, String A)
	{
		org.apache.jena.rdf.model.Resource cls = model.getResource(transform.getIRIFromDL(C, EntityKind.Concept).toString());
		org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
		org.apache.jena.util.iterator.ExtendedIterator iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), org.apache.jena.vocabulary.RDF.Nodes.type, cls.asNode());
		return GetDeriv(iter1);
	}

	public final String GetObjectPropertyDerivation(String A, String p, String B)
	{
		org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
		org.apache.jena.rdf.model.Property reln = model.getProperty(transform.getIRIFromDL(p, EntityKind.Role).toString());
		org.apache.jena.rdf.model.Resource jnst = model.getResource(transform.getIRIFromDL(B, EntityKind.Instance).toString());
		org.apache.jena.util.iterator.ExtendedIterator iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), reln.asNode(), jnst.asNode());
		return GetDeriv(iter1);
	}

	public final String GetSameAsDerivation(String A, String B)
	{
		org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
		org.apache.jena.rdf.model.Resource jnst = model.getResource(transform.getIRIFromDL(B, EntityKind.Instance).toString());
		org.apache.jena.util.iterator.ExtendedIterator iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), org.apache.jena.vocabulary.OWL.sameAs.asNode(), jnst.asNode());
		return GetDeriv(iter1);
	}

	public final String GetDifferentThanDerivation(String A, String B)
	{
		org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
		org.apache.jena.rdf.model.Resource jnst = model.getResource(transform.getIRIFromDL(B, EntityKind.Instance).toString());
		org.apache.jena.util.iterator.ExtendedIterator iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), org.apache.jena.vocabulary.OWL.differentFrom.asNode(), jnst.asNode());
		return GetDeriv(iter1);
	}

	public final String GetDataPropertyDerivation(String A, String p, Value V)
	{
		org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
		org.apache.jena.rdf.model.Property reln = model.getProperty(transform.getIRIFromDL(p, EntityKind.Role).toString());
		org.apache.jena.rdf.model.Resource dat = getLiteralVal(V).asResource();
		org.apache.jena.util.iterator.ExtendedIterator iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), reln.asNode(), dat.asNode());
		return GetDeriv(iter1);
	}

	public final String Why(CNL.DL.Paragraph para)
	{
		DLModSimplifier simli = new DLModSimplifier();
		Object tempVar = simli.Visit(para);
		para = tempVar instanceof CNL.DL.Paragraph ? (CNL.DL.Paragraph)tempVar : null;


		if (para.Statements.Any((s) -> !IsABox(s) && !(s instanceof CNL.DL.Annotation)))
		{
			throw new UnsupportedOperationException();
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			for (Statement s : para.Statements)
			{
				if (s instanceof CNL.DL.InstanceOf)
				{
					CNL.DL.InstanceOf stmt = s instanceof CNL.DL.InstanceOf ? (CNL.DL.InstanceOf)s : null;
					String str = GetIsADerivation((stmt.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)stmt.C : null).id, (stmt.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.I : null).name);
					sb.append(str + "\r\n");
				}
				else if (s instanceof CNL.DL.RelatedInstances)
				{
					CNL.DL.RelatedInstances stmt = s instanceof CNL.DL.RelatedInstances ? (CNL.DL.RelatedInstances)s : null;
					String str = GetObjectPropertyDerivation((stmt.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.I : null).name, (stmt.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)stmt.R : null).id, (stmt.J instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.J : null).name);
					sb.append(str + "\r\n");
				}
				else if (s instanceof CNL.DL.InstanceValue)
				{
					CNL.DL.InstanceValue stmt = s instanceof CNL.DL.InstanceValue ? (CNL.DL.InstanceValue)s : null;
					String str = GetDataPropertyDerivation((stmt.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.I : null).name, (stmt.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)stmt.R : null).id, stmt.V);
					sb.append(str + "\r\n");
				}
				else if (s instanceof CNL.DL.SameInstances)
				{
					CNL.DL.SameInstances stmt = s instanceof CNL.DL.SameInstances ? (CNL.DL.SameInstances)s : null;
					if (stmt.Instances.size() == 2)
					{
						String str = GetSameAsDerivation((stmt.Instances.get(0) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(0) : null).name, (stmt.Instances.get(1) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(1) : null).name);
						sb.append(str + "\r\n");
					}
					else
					{
						throw new UnsupportedOperationException();
					}
				}
				else if (s instanceof CNL.DL.DifferentInstances)
				{
					CNL.DL.DifferentInstances stmt = s instanceof CNL.DL.DifferentInstances ? (CNL.DL.DifferentInstances)s : null;
					if (stmt.Instances.size() == 2)
					{
						String str = GetDifferentThanDerivation((stmt.Instances.get(0) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(0) : null).name, (stmt.Instances.get(1) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(1) : null).name);
						sb.append(str + "\r\n");
					}
					else
					{
						throw new UnsupportedOperationException();
					}
				}
			}
			return sb.toString();
		}
	}


//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to the C# 'dynamic' keyword:
	private dynamic rete_reasoner;

	public final boolean StatementIsValidInRLPlus(Statement statement)
	{
		GenerateJenaRules gen = new GenerateJenaRules(model, modalChecker, true, debugModeOn, exeRulesOn, debugModeOn);
		gen.setOWLDataFactory(ontologyBase, owlxmlFormat, CNL.EN.CNLFactory.lex);
		return gen.Validate(statement);
	}

	private cognipy.cnl.dl.Serializer dlserializer = new cognipy.cnl.dl.Serializer();

	private void MaterializeSROIQ(MatMode mode, boolean isAboxInsertOnly)
	{
		if (!getIsConsistent())
		{
			throw new InconsistentOntologyException();
		}

		model = null;

		CNL.DL.Paragraph toRet = new CNL.DL.Paragraph(null);
		toRet.Statements = new ArrayList<CNL.DL.Statement>();

		dlserializer.Serialize(this.sourceParagraph);

		if (mode != MatMode.Tbox)
		{
			ArrayList<ArrayList<String>> insts = GetInstancesOf(new CNL.DL.Top(null), false);

			if (!isAboxInsertOnly)
			{
				//calculate same-as instances
				for (ArrayList<String> ins : insts)
				{
					for (int i = 0; i < ins.size(); i++)
					{
						if (ins.size() > 1)
						{
							CNL.DL.InstanceList tempVar = new CNL.DL.InstanceList(null);
							CNL.DL.NamedInstance tempVar2 = new CNL.DL.NamedInstance(null);
							tempVar2.name = x;
							CNL.DL.NamedInstance tempVar3 = new CNL.DL.NamedInstance(null);
							tempVar3.name = x;
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
							tempVar.List = (from x in ins select tempVar2 instanceof CNL.DL.Instance ? (CNL.DL.Instance)tempVar3 : null).ToList();
							toRet.Statements.add(new CNL.DL.SameInstances(null, tempVar, CNL.DL.Statement.Modality.IS));
						}
					}
				}

				if (CALCULATE_DISJOINTS)
				{
					//calculate different instances
					for (int i = 0; i < insts.size() - 1; i++)
					{
						for (int j = i + 1; j < insts.size(); j++)
						{
							CNL.DL.NamedInstance tempVar4 = new CNL.DL.NamedInstance(null);
							tempVar4.name = insts.get(i).get(0);
							CNL.DL.NamedInstance tempVar5 = new CNL.DL.NamedInstance(null);
							tempVar5.name = insts.get(j).get(0);
							if (!IsSatisfable(new CNL.DL.ConceptAnd(null, new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, tempVar4)), new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, tempVar5)))))
							{
								CNL.DL.InstanceList tempVar6 = new CNL.DL.InstanceList(null);
								CNL.DL.NamedInstance tempVar7 = new CNL.DL.NamedInstance(null);
								tempVar7.name = insts.get(i).get(0);
								CNL.DL.NamedInstance tempVar8 = new CNL.DL.NamedInstance(null);
								tempVar8.name = insts.get(j).get(0);
								tempVar6.List = new ArrayList<CNL.DL.Instance>(Arrays.asList(tempVar7, tempVar8));
								toRet.Statements.add(new CNL.DL.DifferentInstances(null, tempVar6, CNL.DL.Statement.Modality.IS));
							}
						}
					}
				}
			}

			{ //calculate obj relations

				CNL.DL.Atomic tempVar9 = new CNL.DL.Atomic(null);
				tempVar9.id = "enumeration";
				ArrayList<ArrayList<String>> enums = GetInstancesOf(tempVar9, false);
				ArrayList<ArrayList<String>> nonEnums = new ArrayList<ArrayList<String>>();

				for (ArrayList<String> i : insts)
				{
					boolean found = false;
					for (ArrayList<String> e : enums)
					{
						if (e.Intersect(i).Count() > 0)
						{
							found = true;
							break;
						}
					}
					if (!found)
					{
						nonEnums.add(i);
					}
				}

				ArrayList<ArrayList<String>> rels = GetSubObjectPropertiesOf(new CNL.DL.Top(null), false, false);
				for (ArrayList<String> rel : rels)
				{
					for (ArrayList<String> Is : nonEnums)
					{
						for (ArrayList<String> Js : insts)
						{
							SetInstance(isAboxInsertOnly, toRet, Is, Js, rel);
						}
					}
				}

				if (!isAboxInsertOnly)
				{
					for (ArrayList<String> rel : rels)
					{
						for (ArrayList<String> Is : enums)
						{
							for (ArrayList<String> Js : enums)
							{
								SetInstance(isAboxInsertOnly, toRet, Is, Js, rel);
							}
						}
					}
				}

				//dependent attributesrs from SWRL rules: the key:the given attrbute, value - all that are dependent on it
				HashMap<String, HashSet<String>> depAttrs = dlserializer.GetDependentAttrs();

				//explicitly specified instance-attribute-values
				ArrayList<CNL.DL.InstanceValue> explicitIV = dlserializer.GetInstanceValues();
				for (CNL.DL.InstanceValue e : explicitIV)
				{
					if (isAboxInsertOnly)
					{
						boolean isEnum = false;
						for (ArrayList<String> i : enums)
						{
							for (String ii : i)
							{
								if (e.I instanceof CNL.DL.NamedInstance && ((e.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)e.I : null).name.equals(ii)))
								{
								isEnum = true;
								break;
								}
							}
							if (isEnum)
							{
								break;
							}
						}
						if (isEnum)
						{
							continue;
						}
					}
					SetDataPropsAndPropagetToDependentAttrs(isAboxInsertOnly, GetDataProps(depAttrs, (e.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.R : null).id), isAboxInsertOnly ? nonEnums : insts, toRet, e.V);
				}

				if (!isAboxInsertOnly)
				{
					HashSet<Tuple<String, String, String>> dataVals = dlserializer.GetDataValues();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					for (var prts : dataVals)
					{
						CNL.DL.Value val = CNL.DL.Value.MakeFrom(prts.Item1, prts.Item3);
						ArrayList<ArrayList<String>> dataprops = GetDataProps(depAttrs, prts.Item2);
						SetDataPropsAndPropagetToDependentAttrs(isAboxInsertOnly, dataprops, insts, toRet, val);
					}
				}
			}
		}

		List gens = new ArrayList();
		List gens_struct = new ArrayList();

		if (!isAboxInsertOnly)
		{
			//calculate class assertions
			gens.add(new InferredClassAssertionAxiomGenerator());
			if (materializing_reasner_supports_sroiq)
			{
				gens.add(new InferredPropertyAssertionGeneratorNoTopProps(materializing_reasner_supports_sroiq));
			}
			if (structural_reasoner != null && !materializing_reasner_supports_sroiq)
			{
				gens_struct.add(new InferredClassAssertionAxiomGenerator());
			}
		}

		//calculate structurals
		if (structural_reasoner != null && !materializing_reasner_supports_sroiq && mode != MatMode.Abox)
		{
			gens_struct.add(new InferredSubClassAxiomGenerator());
			gens_struct.add(new InferredEquivalentClassAxiomGenerator());
		}

		if (structural_reasoner != null && !materializing_reasner_supports_sroiq && mode != MatMode.Abox && !isAboxInsertOnly)
		{
			//    calculate disjoint classes
			if (CALCULATE_DISJOINTS)
			{
				gens_struct.add(new InferredDisjointClassesAxiomGenerator());
			}
			gens_struct.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
			gens_struct.add(new InferredDataPropertyCharacteristicAxiomGenerator());
			gens_struct.add(new InferredEquivalentDataPropertiesAxiomGenerator());
			gens_struct.add(new InferredEquivalentObjectPropertyAxiomGenerator());
			gens_struct.add(new InferredSubDataPropertyAxiomGenerator());
			gens_struct.add(new InferredSubObjectPropertyAxiomGenerator());
		}

		//calculate subclass of
		if (mode != MatMode.Abox)
		{
			gens.add(new InferredSubClassAxiomGenerator());
			gens.add(new InferredEquivalentClassAxiomGenerator());
		}

		if (mode != MatMode.Abox && !isAboxInsertOnly)
		{
			if (materializing_reasner_supports_sroiq)
			{
				//calculate disjoint classes
				if (CALCULATE_DISJOINTS)
				{
					gens.add(new InferredDisjointClassesAxiomGenerator());
				}
				gens.add(new InferredObjectPropertyCharacteristicAxiomGenerator());
				gens.add(new InferredDataPropertyCharacteristicAxiomGenerator());
				gens.add(new InferredEquivalentDataPropertiesAxiomGenerator());
				gens.add(new InferredEquivalentObjectPropertyAxiomGenerator());
				gens.add(new InferredInverseObjectPropertiesAxiomGenerator());
				gens.add(new InferredSubDataPropertyAxiomGenerator());
				gens.add(new InferredSubObjectPropertyAxiomGenerator());
			}
		}

		if (structural_reasoner != null && gens_struct.size() > 0)
		{
			InferredOntologyGenerator iog = new InferredOntologyGenerator(structural_reasoner, gens_struct);
			iog.fillOntology(manager, ontology);
		}

		if (gens.size() > 0)
		{
			InferredOntologyGenerator iog = new InferredOntologyGenerator(reasoner, gens);
			iog.fillOntology(manager, ontology);
		}

		cognipy.ars.Transform.Axioms conv = transform.Convert(toRet);
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.axioms));
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.additions));
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.hotfixes));

		cognipy.ars.Transform.Axioms adds = transform.Convert(swrlRulesWIthBuiltInsParagraph);
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(adds.hotfixes));

	}

	private ArrayList<Tuple<String, String, Object>> AdditionalTriplets = new ArrayList<Tuple<String, String, Object>>();

	public final void SetValue(String instance, String datarole, Object val)
	{
		AdditionalTriplets.add(Tuple.Create(instance, datarole, val));
	}

	private void SetInstance(boolean isAboxInsertOnly, CNL.DL.Paragraph toRet, java.lang.Iterable<String> Is, java.lang.Iterable<String> Js, java.lang.Iterable<String> Rs)
	{
		CNL.DL.RelatedInstances stmt = new CNL.DL.RelatedInstances(null);
		stmt.I = new CNL.DL.NamedInstance(null);
		stmt.I.name = Is.First();
		stmt.R = new CNL.DL.Atomic(null);
		stmt.R.id = Rs.First();
		stmt.J = new CNL.DL.NamedInstance(null);
		stmt.J.name = Js.First();
		stmt.modality = CNL.DL.Statement.Modality.IS;
		if (impliKBVis.IsEntailed(stmt) || IsEntailed(stmt))
		{
			for (String ii : Is)
			{
				for (String jj : Js)
				{
					for (String rr : Rs)
					{
						CNL.DL.RelatedInstances s = new CNL.DL.RelatedInstances(null);
						s.I = new CNL.DL.NamedInstance(null);
						s.I.name = ii;
						s.R = new CNL.DL.Atomic(null);
						s.R.id = rr;
						s.J = new CNL.DL.NamedInstance(null);
						s.J.name = jj;
						s.modality = CNL.DL.Statement.Modality.IS;
						if (!isAboxInsertOnly || !impliKBVis.IsEntailed(s))
						{
							toRet.Statements.add(s);
						}
					}
				}
			}
		}
	}

	private void SetDataPropsAndPropagetToDependentAttrs(boolean isAboxInsertOnly, ArrayList<ArrayList<String>> dataprops, ArrayList<ArrayList<String>> insts, CNL.DL.Paragraph toRet, CNL.DL.Value val)
	{
		for (ArrayList<String> dpl : dataprops)
		{
			CNL.DL.Atomic attrib = new CNL.DL.Atomic(null);
			attrib.id = dpl.get(0);
			for (int i = 0; i < insts.size(); i++)
			{
				CNL.DL.InstanceValue stmt = new CNL.DL.InstanceValue(null);
				stmt.I = new CNL.DL.NamedInstance(null);
				stmt.I.name = insts.get(i).get(0);
				stmt.R = new CNL.DL.Atomic(null);
				stmt.R.id = dpl.get(0);
				stmt.V = val;
				stmt.modality = CNL.DL.Statement.Modality.IS;
				if (impliKBVis.IsEntailed(stmt) || IsEntailed(stmt))
				{
					for (String ii : insts.get(i))
					{
						for (String rr : dpl)
						{
							CNL.DL.InstanceValue s = new CNL.DL.InstanceValue(null);
							s.I = new CNL.DL.NamedInstance(null);
							s.I.name = ii;
							s.R = new CNL.DL.Atomic(null);
							s.R.id = rr;
							s.V = val;
							s.modality = CNL.DL.Statement.Modality.IS;
							if (!isAboxInsertOnly || !impliKBVis.IsEntailed(s))
							{
								toRet.Statements.add(s);
							}
						}
					}
				}
			}
		}
	}

	private ArrayList<ArrayList<String>> GetDataProps(HashMap<String, HashSet<String>> depAttrs, String attr)
	{
		ArrayList<ArrayList<String>> dataprops = new ArrayList<ArrayList<String>>();

		//here we keep all crawled attributes
		HashSet<String> allProps = new HashSet<String>();

		//here we have the 
		Stack<String> newProps = new Stack<String>();
		newProps.push(attr);

		while (newProps.size() > 0)
		{
			while (newProps.size() > 0)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var topP = newProps.pop();

				CNL.DL.Atomic attrib3 = new CNL.DL.Atomic(null);
				attrib3.id = topP;

				ArrayList<String> eqv = GetEquivalentDataPropertiesOf(attrib3, false);
				eqv.add(0, attrib3.id);

				ArrayList<ArrayList<String>> supers = GetSuperDataPropertiesOf(attrib3, false, false);
				for (ArrayList<String> sl : supers)
				{
					for (String s : sl)
					{
						eqv.add(s);
					}
				}

				for (String s : eqv)
				{
					allProps.add(s);
				}

				dataprops.add(eqv);
			}

			for (ArrayList<String> dpl : dataprops)
			{
				for (String dp : dpl)
				{
					if (depAttrs.containsKey(dp))
					{
						for (String p : depAttrs.get(dp))
						{
							if (!allProps.contains(p))
							{
								newProps.push(p);
							}
						}
					}
				}
			}
		}
		return dataprops;
	}


	private String GetShortForm(OWLEntity entity)
	{
		return entity.getIRI().toString();
	}

	private static class BlindFactory extends org.semanticweb.HermiT.Reasoner.ReasonerFactory
	{
		@Override
		protected OWLReasoner createHermiTOWLReasoner(org.semanticweb.HermiT.Configuration configuration, OWLOntology ontology)
		{
			configuration.throwInconsistentOntologyException = false;
			return new org.semanticweb.HermiT.Reasoner(configuration, ontology);
		}
	}

	private static class ExplanationsProgressMonitor implements com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor
	{
		public ArrayList<cognipy.cnl.dl.Paragraph> aExplanations = new ArrayList<cognipy.cnl.dl.Paragraph>();
		public tangible.Action1Param<cognipy.cnl.dl.Paragraph> foundExplanationAction;
		public tangible.Func0Param<Boolean> isCancelledFunc;

		private cognipy.ars.InvTransform invtransform;
		public ExplanationsProgressMonitor(cognipy.ars.InvTransform invtransform)
		{
			this.invtransform = invtransform;
		}

		public final void foundAllExplanations()
		{

		}

		public final void foundExplanation(Set explanation)
		{
			cognipy.cnl.dl.Paragraph explanat = new cognipy.cnl.dl.Paragraph(null);
			explanat.Statements = new ArrayList<cognipy.cnl.dl.Statement>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var causingAxiom : explanation.toArray())
			{
				explanat.Statements.add(invtransform.Convert(causingAxiom instanceof OWLAxiom ? (OWLAxiom)causingAxiom : null));
			}
			foundExplanationAction.invoke(explanat);
			aExplanations.add(explanat);
		}

		public final boolean isCancelled()
		{
			return isCancelledFunc.invoke();
		}
	}

	public final ArrayList<cognipy.cnl.dl.Paragraph> GetExplanations(tangible.Action1Param<cognipy.cnl.dl.Paragraph> foundExplanationAction, tangible.Func0Param<Boolean> isCancelledFunc)
	{
		rationals.Automaton a = new rationals.Automaton();

		org.semanticweb.owlapi.model.OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		org.semanticweb.owlapi.model.OWLOntology ontology = manager.createOntology(ontologyIRI);
		cognipy.ars.Transform.Axioms conv = transform.Convert(sourceParagraph);
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.axioms));
		manager.addAxioms(ontology, cognipy.ars.Transform.GetJavaAxiomSet(conv.additions));

		BlindFactory bf = new BlindFactory();


		ExplanationsProgressMonitor explProgMon = new ExplanationsProgressMonitor(invtransform);
		explProgMon.foundExplanationAction = (cognipy.cnl.dl.Paragraph obj) -> foundExplanationAction.invoke(obj);
		explProgMon.isCancelledFunc = () -> isCancelledFunc.invoke();

		DefaultExplanationGenerator multExplanator = new DefaultExplanationGenerator(manager, bf, ontology, explProgMon);

		Set explanations = multExplanator.getExplanations(manager.getOWLDataFactory().getOWLThing());

		if (explanations.isEmpty())
		{
			cognipy.cnl.dl.Paragraph explanat = new cognipy.cnl.dl.Paragraph(null);
			explanat.Statements = new ArrayList<cognipy.cnl.dl.Statement>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var causingAxiom : (ontology.getAxioms() instanceof Set ? (Set)ontology.getAxioms() : null).toArray())
			{
				explanat.Statements.add(invtransform.Convert(causingAxiom instanceof OWLAxiom ? (OWLAxiom)causingAxiom : null));
			}
			foundExplanationAction.invoke(explanat);
			explProgMon.aExplanations.add(explanat);
		}

		return explProgMon.aExplanations;
	}

	//public List<Ontorion.CNL.DL.Paragraph> ResolveInconsistency()
	//{
	//    if (!reasoner.isConsistent())
	//    {
	//        return GetExplanations();
	//    }
	//    return null;
	//}

	public final boolean getIsConsistent()
	{
		return reasoner.isConsistent();
	}


	public final java.util.ArrayList<java.util.ArrayList<String>> GetObjectPropertyRanges(cognipy.cnl.dl.Node role)
	{
		return GetObjectPropertyRanges(role, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetObjectPropertyRanges(CogniPy.CNL.DL.Node role, bool includeTopBot = false)
	public final ArrayList<ArrayList<String>> GetObjectPropertyRanges(cognipy.cnl.dl.Node role, boolean includeTopBot)
	{
		return GetObjectDomainRange(reasoner.getObjectPropertyRanges, role, includeTopBot);
	}


	public final java.util.ArrayList<java.util.ArrayList<String>> GetObjectPropertyDomains(cognipy.cnl.dl.Node role)
	{
		return GetObjectPropertyDomains(role, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetObjectPropertyDomains(CogniPy.CNL.DL.Node role, bool includeTopBot = false)
	public final ArrayList<ArrayList<String>> GetObjectPropertyDomains(cognipy.cnl.dl.Node role, boolean includeTopBot)
	{
		return GetObjectDomainRange(reasoner.getObjectPropertyDomains, role, includeTopBot);
	}

	private ArrayList<ArrayList<String>> GetObjectDomainRange(tangible.Func2Param<OWLObjectPropertyExpression, Boolean, NodeSet> func, cognipy.cnl.dl.Node role, boolean includeTopBot)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var ret = func.invoke(transform.GetObjectProperty(role), false);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object tempVar = it.next();
			OWLClassNode cls = tempVar instanceof OWLClassNode ? (OWLClassNode)tempVar : null;
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLClassExpression nod = tempVar2 instanceof OWLClassExpression ? (OWLClassExpression)tempVar2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else if (nod.isClassExpressionLiteral())
				{
					y.add(invtransform.renderEntity(nod instanceof OWLClass ? (OWLClass)nod : null, ARS.EntityKind.Concept));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}

		return x;
	}



	public final java.util.ArrayList<java.util.ArrayList<String>> GetDataPropertyDomains(cognipy.cnl.dl.Node role)
	{
		return GetDataPropertyDomains(role, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetDataPropertyDomains(CogniPy.CNL.DL.Node role, bool includeTopBot = false)
	public final ArrayList<ArrayList<String>> GetDataPropertyDomains(cognipy.cnl.dl.Node role, boolean includeTopBot)
	{
		return GetDataDomain(reasoner.getDataPropertyDomains, role, includeTopBot);
	}

	private ArrayList<ArrayList<String>> GetDataDomain(tangible.Func2Param<OWLDataProperty, Boolean, NodeSet> func, cognipy.cnl.dl.Node role, boolean includeTopBot)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var ret = func.invoke(transform.GetDataProperty(role), false);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object tempVar = it.next();
			OWLClassNode cls = tempVar instanceof OWLClassNode ? (OWLClassNode)tempVar : null;
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLClassExpression nod = tempVar2 instanceof OWLClassExpression ? (OWLClassExpression)tempVar2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else if (nod.isClassExpressionLiteral())
				{
					y.add(invtransform.renderEntity(nod instanceof OWLClass ? (OWLClass)nod : null, ARS.EntityKind.Concept));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}

		return x;
	}



	public final ArrayList<ArrayList<String>> GetInstancesOf(cognipy.cnl.dl.Node e, boolean direct)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		org.semanticweb.owlapi.reasoner.NodeSet ret = reasoner.getInstances(transform.Convert(e).Key, direct);
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			Object tempVar = it.next();
			OWLNamedIndividualNode ind = tempVar instanceof OWLNamedIndividualNode ? (OWLNamedIndividualNode)tempVar : null;
			ArrayList<String> y = new ArrayList<String>();
			Set s2 = ind.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLNamedIndividual nod = tempVar2 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar2 : null;
				y.add(invtransform.renderEntity(nod, ARS.EntityKind.Instance));
			}
			x.add(y);
		}

		return x;
	}

	public final ArrayList<String> GetDataPropertyValues(String instance)
	{
		ArrayList<String> res = new ArrayList<String>();
		OWLNamedIndividual individual = transform.GetNamedIndividual(instance);
		Map x = individual.getDataPropertyValues(ontology);
		for (Iterator it = x.keySet().iterator(); it.hasNext();)
		{
			Object tempVar = it.next();
			Map.Entry ind = tempVar instanceof Map.Entry ? (Map.Entry)tempVar : null; //OWLDataPropertyExpression;
			res.add(invtransform.renderEntity(ind instanceof OWLClass ? (OWLClass)ind : null, ARS.EntityKind.DataRole));
		}
		return res;
	}


	public final ArrayList<ArrayList<String>> GetRelatedInstances(String instance, cognipy.cnl.dl.Node r)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		org.semanticweb.owlapi.reasoner.NodeSet ret = reasoner.getObjectPropertyValues(transform.GetNamedIndividual(instance), transform.GetObjectProperty(r));
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			Object tempVar = it.next();
			OWLNamedIndividualNode ind = tempVar instanceof OWLNamedIndividualNode ? (OWLNamedIndividualNode)tempVar : null;
			ArrayList<String> y = new ArrayList<String>();
			Set s2 = ind.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLNamedIndividual nod = tempVar2 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar2 : null;
				y.add(invtransform.renderEntity(nod, ARS.EntityKind.Instance));
			}
			x.add(y);
		}

		return x;
	}

	public final ArrayList<String> GetDataPropertyValues(String instance, cognipy.cnl.dl.Node r)
	{
		ArrayList<String> x = new ArrayList<String>();
		Set ret = getSupprtingReasoner().getDataPropertyValues(transform.GetNamedIndividual(instance), transform.GetDataProperty(r));
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			Object tempVar = it.next();
			org.semanticweb.owlapi.model.OWLLiteral y = (tempVar instanceof org.semanticweb.owlapi.model.OWLLiteral ? (org.semanticweb.owlapi.model.OWLLiteral)tempVar : null);
			if (y != null)
			{
				x.add(y.getLiteral());
			}
			//x.Add(y);
		}

		return x;
	}


	public final java.util.ArrayList<java.util.ArrayList<String>> GetSubConcepts(cognipy.cnl.dl.Node e, boolean direct)
	{
		return GetSubConcepts(e, direct, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetSubConcepts(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
	public final ArrayList<ArrayList<String>> GetSubConcepts(cognipy.cnl.dl.Node e, boolean direct, boolean includeTopBot)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		org.semanticweb.owlapi.reasoner.NodeSet ret = reasoner.getSubClasses(transform.Convert(e).Key, direct);
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object tempVar = it.next();
			OWLClassNode cls = tempVar instanceof OWLClassNode ? (OWLClassNode)tempVar : null;
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLClassExpression nod = tempVar2 instanceof OWLClassExpression ? (OWLClassExpression)tempVar2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else if (nod.isClassExpressionLiteral())
				{
					y.add(invtransform.renderEntity(nod instanceof OWLClass ? (OWLClass)nod : null, ARS.EntityKind.Concept));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}
		return x;
	}

	private OWLReasoner getSupprtingReasoner()
	{
		if (structural_reasoner != null && !materializing_reasner_supports_sroiq)
		{
			return structural_reasoner;
		}
		else
		{
			return this.reasoner;
		}
	}


	public final java.util.ArrayList<java.util.ArrayList<String>> GetSubObjectPropertiesOf(cognipy.cnl.dl.Node e, boolean direct)
	{
		return GetSubObjectPropertiesOf(e, direct, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetSubObjectPropertiesOf(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
	public final ArrayList<ArrayList<String>> GetSubObjectPropertiesOf(cognipy.cnl.dl.Node e, boolean direct, boolean includeTopBot)
	{

		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		org.semanticweb.owlapi.reasoner.NodeSet ret = getSupprtingReasoner().getSubObjectProperties(transform.GetObjectProperty(e), direct);
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object clsc = it.next();
			OWLObjectPropertyNode cls = clsc instanceof OWLObjectPropertyNode ? (OWLObjectPropertyNode)clsc : null;
			if (cls == null)
			{
				continue;
			}
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object nod2 = it2.next();
				OWLObjectPropertyExpression nod = nod2 instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)nod2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else if (nod2 instanceof OWLObjectProperty)
				{
					y.add(invtransform.renderEntity(nod2 instanceof OWLObjectProperty ? (OWLObjectProperty)nod2 : null, ARS.EntityKind.Role));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}
		return x;
	}


	public final java.util.ArrayList<java.util.ArrayList<String>> GetSubDataPropertiesOf(cognipy.cnl.dl.Node e, boolean direct)
	{
		return GetSubDataPropertiesOf(e, direct, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetSubDataPropertiesOf(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
	public final ArrayList<ArrayList<String>> GetSubDataPropertiesOf(cognipy.cnl.dl.Node e, boolean direct, boolean includeTopBot)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		org.semanticweb.owlapi.reasoner.NodeSet ret = getSupprtingReasoner().getSubDataProperties(transform.GetDataProperty(e), direct);
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object tempVar = it.next();
			OWLDataPropertyNode cls = tempVar instanceof OWLDataPropertyNode ? (OWLDataPropertyNode)tempVar : null;
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLDataProperty nod = tempVar2 instanceof OWLDataProperty ? (OWLDataProperty)tempVar2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else
				{
					y.add(invtransform.renderEntity(nod, ARS.EntityKind.DataRole));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}
		return x;
	}


	public final java.util.ArrayList<java.util.ArrayList<String>> GetSuperDataPropertiesOf(cognipy.cnl.dl.Node e, boolean direct)
	{
		return GetSuperDataPropertiesOf(e, direct, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetSuperDataPropertiesOf(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
	public final ArrayList<ArrayList<String>> GetSuperDataPropertiesOf(cognipy.cnl.dl.Node e, boolean direct, boolean includeTopBot)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		org.semanticweb.owlapi.reasoner.NodeSet ret = getSupprtingReasoner().getSuperDataProperties(transform.GetDataProperty(e), direct);
		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object tempVar = it.next();
			OWLDataPropertyNode cls = tempVar instanceof OWLDataPropertyNode ? (OWLDataPropertyNode)tempVar : null;
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLDataProperty nod = tempVar2 instanceof OWLDataProperty ? (OWLDataProperty)tempVar2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else
				{
					y.add(invtransform.renderEntity(nod, ARS.EntityKind.DataRole));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}
		return x;
	}


	public final boolean IsTrue(cognipy.cnl.dl.Paragraph stmt)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var ecls = transform.Convert(stmt).axioms.get(0).axiom;
		return reasoner.isEntailed(ecls);
	}


	public final java.util.ArrayList<String> GetEquivalentConcepts(cognipy.cnl.dl.Node e)
	{
		return GetEquivalentConcepts(e, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<string> GetEquivalentConcepts(CogniPy.CNL.DL.Node e, bool includeTopBot = true)
	public final ArrayList<String> GetEquivalentConcepts(cognipy.cnl.dl.Node e, boolean includeTopBot)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var ecls = transform.Convert(e).Key;
		String thisNode = "";
		if (ecls instanceof OWLClass)
		{
			thisNode = invtransform.renderEntity(ecls instanceof OWLClass ? (OWLClass)ecls : null, ARS.EntityKind.Concept);
		}

		Object tempVar = reasoner.getEquivalentClasses(ecls);
		OWLClassNode ret = tempVar instanceof OWLClassNode ? (OWLClassNode)tempVar : null;
		ArrayList<String> y = new ArrayList<String>();
		for (Iterator it2 = ret.iterator(); it2.hasNext();)
		{
			Object tempVar2 = it2.next();
			OWLClassExpression nod = tempVar2 instanceof OWLClassExpression ? (OWLClassExpression)tempVar2 : null;
			if (nod.isBottomEntity())
			{
				if (includeTopBot)
				{
					y.add("⊥");
				}
			}
			else if (nod.isTopEntity())
			{
				if (includeTopBot)
				{
					y.add("⊤");
				}
			}
			else if (nod.isClassExpressionLiteral())
			{
				String entitStr = invtransform.renderEntity(nod instanceof OWLClass ? (OWLClass)nod : null, ARS.EntityKind.Concept);
				if (entitStr.compareTo(thisNode) != 0)
				{
					y.add(entitStr);
				}
			}
		}
		return y;
	}


	public final java.util.ArrayList<String> GetEquivalentDataPropertiesOf(cognipy.cnl.dl.Node e)
	{
		return GetEquivalentDataPropertiesOf(e, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<string> GetEquivalentDataPropertiesOf(CogniPy.CNL.DL.Node e, bool includeTopBot = true)
	public final ArrayList<String> GetEquivalentDataPropertiesOf(cognipy.cnl.dl.Node e, boolean includeTopBot)
	{
		OWLDataProperty ecls = transform.GetDataProperty(e);
		String thisNode = invtransform.renderEntity(ecls, ARS.EntityKind.DataRole);

		ArrayList<String> y = new ArrayList<String>();
		Set ret = reasoner.getEquivalentDataProperties(ecls).getEntities();
		for (Iterator it2 = ret.iterator(); it2.hasNext();)
		{
			Object tempVar = it2.next();
			OWLDataProperty nod = tempVar instanceof OWLDataProperty ? (OWLDataProperty)tempVar : null;
			if (nod.isBottomEntity())
			{
				if (includeTopBot)
				{
					y.add("⊥");
				}
			}
			else if (nod.isTopEntity())
			{
				if (includeTopBot)
				{
					y.add("⊤");
				}
			}
			else
			{
				String entitStr = invtransform.renderEntity(nod, ARS.EntityKind.DataRole);
				if (entitStr.compareTo(thisNode) != 0)
				{
					y.add(entitStr);
				}
			}
		}
		return y;
	}



	public final java.util.ArrayList<java.util.ArrayList<String>> GetSuperConcepts(cognipy.cnl.dl.Node e, boolean direct)
	{
		return GetSuperConcepts(e, direct, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public List<List<string>> GetSuperConcepts(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
	public final ArrayList<ArrayList<String>> GetSuperConcepts(cognipy.cnl.dl.Node e, boolean direct, boolean includeTopBot)
	{
		ArrayList<ArrayList<String>> x = new ArrayList<ArrayList<String>>();
		NodeSet ret;
		if (e instanceof InstanceSet)
		{
			ret = reasoner.getTypes(transform.GetNamedIndividual(((NamedInstance)((InstanceSet)e).Instances.get(0)).name), direct);
		}
		else
		{
			ret = reasoner.getSuperClasses(transform.Convert(e).Key, direct);
		}

		for (Iterator it = ret.iterator(); it.hasNext();)
		{
			ArrayList<String> y = new ArrayList<String>();

			Object tempVar = it.next();
			OWLClassNode cls = tempVar instanceof OWLClassNode ? (OWLClassNode)tempVar : null;
			Set s2 = cls.getEntities();
			for (Iterator it2 = s2.iterator(); it2.hasNext();)
			{
				Object tempVar2 = it2.next();
				OWLClassExpression nod = tempVar2 instanceof OWLClassExpression ? (OWLClassExpression)tempVar2 : null;
				if (nod.isBottomEntity())
				{
					if (includeTopBot)
					{
						y.add("⊥");
					}
				}
				else if (nod.isTopEntity())
				{
					if (includeTopBot)
					{
						y.add("⊤");
					}
				}
				else if (nod.isClassExpressionLiteral())
				{
					y.add(invtransform.renderEntity(nod instanceof OWLClass ? (OWLClass)nod : null, ARS.EntityKind.Concept));
				}
			}
			if (!y.isEmpty())
			{
				x.add(y);
			}
		}
		return x;
	}

	public final boolean IsSatisfable(cognipy.cnl.dl.Node C)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var expr = transform.Convert(C).Key;
		return reasoner.isSatisfiable(expr);
	}

	public final boolean IsEntailed(cognipy.cnl.dl.Statement S)
	{
		cognipy.ars.Transform.Axioms stat = transform.Convert(new CNL.DL.Paragraph(null, S));
		HashSet axioms = new HashSet();
		for (AxiomOrComment ax : stat.axioms)
		{
			axioms.add(ax.axiom);
		}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		for (var ax : stat.additions)
		{
			axioms.add(ax);
		}
		return reasoner.isEntailed(axioms);
	}

	private boolean SimpleModalCheck(cognipy.cnl.dl.Node C, cognipy.cnl.dl.Node D, tangible.OutObject<ArrayList<ArrayList<String>>> okOnInstances, tangible.OutObject<ArrayList<ArrayList<String>>> errsOnInstances)
	{
		errsOnInstances.argValue = new ArrayList<ArrayList<String>>();
		okOnInstances.argValue = new ArrayList<ArrayList<String>>();

		org.semanticweb.owlapi.reasoner.NodeSet Ainst = reasoner.getInstances(transform.Convert(C).Key, false);
		Iterator Aiter = Ainst.iterator();

		while (Aiter.hasNext())
		{
			Object tempVar = Aiter.next();
			org.semanticweb.owlapi.reasoner.Node a = tempVar instanceof org.semanticweb.owlapi.reasoner.Node ? (org.semanticweb.owlapi.reasoner.Node)tempVar : null;
			Iterator aiter = a.iterator();
			if (aiter.hasNext())
			{
				Object tempVar2 = aiter.next();
				OWLNamedIndividual i = tempVar2 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar2 : null;
				if (i.getIRI().toString().endsWith("_uUu_"))
				{
					continue;
				}

				org.semanticweb.owlapi.model.OWLObjectOneOf oneof = manager.getOWLDataFactory().getOWLObjectOneOf(i);

				Set clss2 = new HashSet();
				clss2.add(oneof);
				clss2.add(manager.getOWLDataFactory().getOWLObjectComplementOf(transform.Convert(D).Key));
				OWLClassExpression expr2 = manager.getOWLDataFactory().getOWLObjectIntersectionOf(clss2);
				boolean isError = reasoner.isSatisfiable(expr2);
				ArrayList<String> erri = new ArrayList<String>();

				Iterator a2iter = a.iterator();
				while (a2iter.hasNext())
				{
					Object tempVar3 = a2iter.next();
					OWLNamedIndividual i2 = tempVar3 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar3 : null;
					CNL.DL.DlName ins = new CNL.DL.DlName();
					ins.id = invtransform.renderEntity(i2, ARS.EntityKind.Instance);
					erri.add(ins.id);
				}
				if (isError)
				{
					errsOnInstances.argValue.add(erri);
				}
				else
				{
					okOnInstances.argValue.add(erri);
				}
			}
		}
		return errsOnInstances.argValue.isEmpty();
	}

	public enum ModalityCheckResult
	{
		Ok,
		Error,
		Warning,
		Hint;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static ModalityCheckResult forValue(int value)
		{
			return values()[value];
		}
	}

	public final ModalityCheckResult CheckModality(cognipy.cnl.dl.Statement stmt, tangible.OutObject<ArrayList<ArrayList<String>>> okOnInstances, tangible.OutObject<ArrayList<ArrayList<String>>> errsOnInstances)
	{
		errsOnInstances.argValue = null;

		ModalityCheckResult onError = ModalityCheckResult.Ok;
		boolean negate = false;
		switch (stmt.modality)
		{
			case MUST:
				onError = ModalityCheckResult.Error;
				break;
			case SHOULD:
				onError = ModalityCheckResult.Warning;
				break;
			case CAN:
				onError = ModalityCheckResult.Hint;
				break;
			case CANNOT:
				onError = ModalityCheckResult.Error;
				negate = true;
				break;
			case SHOULDNOT:
				onError = ModalityCheckResult.Warning;
				negate = true;
				break;
			case MUSTNOT:
				onError = ModalityCheckResult.Hint;
				negate = true;
				break;
		}

		if (stmt instanceof cognipy.cnl.dl.Subsumption)
		{
			if (!negate)
			{
				return SimpleModalCheck((stmt instanceof cognipy.cnl.dl.Subsumption ? (cognipy.cnl.dl.Subsumption)stmt : null).C, (stmt instanceof cognipy.cnl.dl.Subsumption ? (cognipy.cnl.dl.Subsumption)stmt : null).D, okOnInstances, errsOnInstances) ? ModalityCheckResult.Ok : onError;
			}
			else
			{
				return SimpleModalCheck((stmt instanceof cognipy.cnl.dl.Subsumption ? (cognipy.cnl.dl.Subsumption)stmt : null).C, (stmt instanceof cognipy.cnl.dl.Subsumption ? (cognipy.cnl.dl.Subsumption)stmt : null).D, errsOnInstances, okOnInstances) ? onError : ModalityCheckResult.Ok;
			}
		}
		else if (stmt instanceof cognipy.cnl.dl.Equivalence)
		{
			ArrayList<ArrayList<String>> errsOnInstances2;
			ArrayList<ArrayList<String>> okOnInstances2;
			boolean c1;
			boolean c2;
			if (!negate)
			{
				c1 = SimpleModalCheck((stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(0), (stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(1), okOnInstances, errsOnInstances);
				tangible.OutObject<ArrayList<ArrayList<String>>> tempOut_okOnInstances2 = new tangible.OutObject<ArrayList<ArrayList<String>>>();
				tangible.OutObject<ArrayList<ArrayList<String>>> tempOut_errsOnInstances2 = new tangible.OutObject<ArrayList<ArrayList<String>>>();
				c2 = SimpleModalCheck((stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(1), (stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(0), tempOut_okOnInstances2, tempOut_errsOnInstances2);
			errsOnInstances2 = tempOut_errsOnInstances2.argValue;
			okOnInstances2 = tempOut_okOnInstances2.argValue;
			}
			else
			{
				c1 = !SimpleModalCheck((stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(0), (stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(1), errsOnInstances, okOnInstances);
				tangible.OutObject<ArrayList<ArrayList<String>>> tempOut_errsOnInstances22 = new tangible.OutObject<ArrayList<ArrayList<String>>>();
				tangible.OutObject<ArrayList<ArrayList<String>>> tempOut_okOnInstances22 = new tangible.OutObject<ArrayList<ArrayList<String>>>();
				c2 = !SimpleModalCheck((stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(1), (stmt instanceof cognipy.cnl.dl.Equivalence ? (cognipy.cnl.dl.Equivalence)stmt : null).Equivalents.get(0), tempOut_errsOnInstances22, tempOut_okOnInstances22);
			okOnInstances2 = tempOut_okOnInstances22.argValue;
			errsOnInstances2 = tempOut_errsOnInstances22.argValue;
			}
			if (c1 && c2)
			{
				return ModalityCheckResult.Ok;
			}
			else
			{
				errsOnInstances.argValue.addAll(errsOnInstances2);
				okOnInstances.argValue.addAll(okOnInstances2);
				return onError;
			}
		}
		else
		{
			throw new UnsupportedOperationException();
		}
	}

	private boolean IsABox(CNL.DL.Statement stmt)
	{
		return ((stmt instanceof CNL.DL.InstanceOf) && (((stmt instanceof CNL.DL.InstanceOf ? (CNL.DL.InstanceOf)stmt : null).C instanceof CNL.DL.Atomic) || ((stmt instanceof CNL.DL.InstanceOf ? (CNL.DL.InstanceOf)stmt : null).C instanceof CNL.DL.Top))) || (stmt instanceof CNL.DL.RelatedInstances) || (stmt instanceof CNL.DL.InstanceValue) || (stmt instanceof CNL.DL.SameInstances) || (stmt instanceof CNL.DL.DifferentInstances);
	}

	private void AddIfNotExistsRemoveIfExists(boolean isAdd, org.apache.jena.rdf.model.Model model2, org.apache.jena.rdf.model.Resource s, org.apache.jena.rdf.model.Property v, org.apache.jena.rdf.model.RDFNode o)
	{
		org.apache.jena.rdf.model.Statement t = model2.createStatement(s, v, o);
		if (isAdd)
		{
			if (!model2.contains(t))
			{
				model2.add(t);
				sourceTriplets.add(t.asTriple());
			}
		}
		else
		{
			if (model2.contains(t))
			{
				model2.remove(t);
				sourceTriplets.remove(t.asTriple());
			}
		}
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

	private org.apache.jena.rdf.model.Literal getLiteralVal(Value v)
	{
		if (model == null)
		{
			return null;
		}

		if (v instanceof CNL.DL.Bool)
		{
			return model.createTypedLiteral(v.ToBool(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDboolean);
		}
		if (v instanceof CNL.DL.String)
		{
			return model.createTypedLiteral(v.toString(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
		}
		if (v instanceof CNL.DL.Float)
		{
			return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdouble);
		}
		if (v instanceof CNL.DL.Number)
		{
			return model.createTypedLiteral(new java.lang.Integer(v.ToInt()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger);
		}
		if (v instanceof CNL.DL.DateTimeVal)
		{
			return model.createTypedLiteral(completeDTMVal(v.ToStringExact()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdateTime);
		}
		if (v instanceof CNL.DL.Duration)
		{
			return model.createTypedLiteral(v.ToStringExact(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDduration);
		}

		return model.createTypedLiteral(v.toString()); //TODO xsd:date i inne typy
	}

	public final void MergeWith(HermiTReasoningService x)
	{
		model = org.apache.jena.rdf.model.ModelFactory.createUnion(model, x.model);
	}

	public final ArrayList<String> GetSubConceptsOfFromModelFast(CNL.DL.Atomic a)
	{
		BuildModel();
		String ths = transform.getIRIFromDL(a.id, EntityKind.Concept).toString();
		org.apache.jena.rdf.model.Resource cls = model.getResource(ths);
		org.apache.jena.util.iterator.ExtendedIterator triples = model.getGraph().find(null, org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), cls.asNode());

		ArrayList<String> ret = new ArrayList<String>();
		while (triples.hasNext())
		{
			org.apache.jena.graph.Triple trp = (org.apache.jena.graph.Triple)triples.next();
			org.apache.jena.graph.Node sub = trp.getSubject();
			if (sub.isBlank())
			{
				continue;
			}

			String vra = sub.toString();
			if (!ths.equals(vra) && !org.apache.jena.vocabulary.OWL.Thing.toString().equals(vra) && !org.apache.jena.vocabulary.OWL.Nothing.toString().equals(vra) && !org.apache.jena.vocabulary.OWL2.NamedIndividual.toString().equals(vra))
			{
				ret.add(vra);
			}
		}
		return ret;
	}

	public final ArrayList<String> GetSuperConceptsOfFromModelFast(CNL.DL.Node a)
	{
		BuildModel();
		org.apache.jena.util.iterator.ExtendedIterator triples;
		String ths;
		if (a instanceof CNL.DL.Atomic)
		{
			ths = transform.getIRIFromDL((a instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)a : null).id, EntityKind.Concept).toString();
			org.apache.jena.rdf.model.Resource cls = model.getResource(ths);
			triples = model.getGraph().find(cls.asNode(), org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), null);
		}
		else if (a instanceof CNL.DL.InstanceSet)
		{
			ths = transform.getIRIFromDL(((a instanceof CNL.DL.InstanceSet ? (CNL.DL.InstanceSet)a : null).Instances.get(0) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)(a instanceof CNL.DL.InstanceSet ? (CNL.DL.InstanceSet)a : null).Instances.get(0) : null).name, EntityKind.Instance).toString();
			org.apache.jena.rdf.model.Resource i = model.getResource(ths);
			triples = model.getGraph().find(i.asNode(), org.apache.jena.vocabulary.RDF.type.asNode(), null);
		}
		else
		{
			throw new IllegalStateException();
		}

		ArrayList<String> ret = new ArrayList<String>();
		while (triples.hasNext())
		{
			org.apache.jena.graph.Triple trp = (org.apache.jena.graph.Triple)triples.next();
			org.apache.jena.graph.Node obj = trp.getObject();
			if (obj.isBlank())
			{
				continue;
			}
			String vra = obj.toString();
			if (!ths.equals(vra) && !org.apache.jena.vocabulary.OWL.Thing.toString().equals(vra) && !org.apache.jena.vocabulary.OWL.Nothing.toString().equals(vra) && !org.apache.jena.vocabulary.OWL2.NamedIndividual.toString().equals(vra))
			{
				ret.add(vra);
			}
		}
		return ret;
	}

	public final ArrayList<String> GetInstancesOfFromModelFast(CNL.DL.Node a)
	{
		BuildModel();
		org.apache.jena.util.iterator.ExtendedIterator triples;
		String me = "";
		if (a instanceof CNL.DL.Atomic)
		{
			org.apache.jena.rdf.model.Resource cls = model.getResource(transform.getIRIFromDL((a instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)a : null).id, EntityKind.Concept).toString());
			triples = model.getGraph().find(null, org.apache.jena.vocabulary.RDF.type.asNode(), cls.asNode());
		}
		else if (a instanceof CNL.DL.InstanceSet)
		{
			org.apache.jena.rdf.model.Resource i = model.getResource(transform.getIRIFromDL(((a instanceof CNL.DL.InstanceSet ? (CNL.DL.InstanceSet)a : null).Instances.get(0) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)(a instanceof CNL.DL.InstanceSet ? (CNL.DL.InstanceSet)a : null).Instances.get(0) : null).name, EntityKind.Instance).toString());
			me = i.toString();
			triples = model.getGraph().find(null, org.apache.jena.vocabulary.OWL.sameAs.asNode(), i.asNode());
		}
		else
		{
			throw new IllegalStateException();
		}

		ArrayList<String> ret = new ArrayList<String>();
		while (triples.hasNext())
		{
			org.apache.jena.graph.Triple trp = (org.apache.jena.graph.Triple)triples.next();
			org.apache.jena.graph.Node sub = trp.getSubject();
			if (sub.isBlank())
			{
				continue;
			}
			String vra = sub.toString();
			ret.add(vra);
		}
		return ret;
	}

	public final ArrayList<Tuple<String, Object>> GetInstancesOfFromModelFastURI(Object a)
	{
		BuildModel();
		org.apache.jena.util.iterator.ExtendedIterator triples;
		String me = "";
		Object meU = null;
		if (a instanceof CNL.DL.Atomic)
		{
			org.apache.jena.rdf.model.Resource cls = model.getResource(transform.getIRIFromDL((a instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)a : null).id, EntityKind.Concept).toString());
			triples = model.getGraph().find(null, org.apache.jena.vocabulary.RDF.type.asNode(), cls.asNode());
		}
		else if (a instanceof CNL.DL.NamedInstance)
		{
			org.apache.jena.rdf.model.Resource i = model.getResource(transform.getIRIFromDL((a instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)a : null).name, EntityKind.Instance).toString());
			me = i.toString();
			meU = i;
			triples = model.getGraph().find(null, org.apache.jena.vocabulary.OWL.sameAs.asNode(), i.asNode());
		}
		else if (a instanceof CNL.DL.InstanceSet)
		{
			CNL.DL.InstanceSet set = a instanceof CNL.DL.InstanceSet ? (CNL.DL.InstanceSet)a : null;
			if (set.Instances.size() == 1)
			{
				return GetInstancesOfFromModelFastURI(set.Instances.get(0));
			}
			else
			{
				ArrayList<Tuple<String, Object>> ret2 = new ArrayList<Tuple<String, Object>>();
				for (Instance i : set.Instances)
				{
					ret2.addAll(GetInstancesOfFromModelFastURI(i));
				}
				return ret2;
			}
		}
		else
		{
			throw new IllegalStateException();
		}

		ArrayList<Tuple<String, Object>> ret = new ArrayList<Tuple<String, Object>>();
		while (triples.hasNext())
		{
			org.apache.jena.graph.Triple trp = (org.apache.jena.graph.Triple)triples.next();
			org.apache.jena.graph.Node sub = trp.getSubject();
			if (sub.isBlank())
			{
				continue;
			}
			String vra = sub.toString();
			ret.add(Tuple.Create(vra, (Object)sub));
		}
		return ret;
	}

	public final ArrayList<Tuple<Boolean, String, Object>> GetAllPriopertiesFastFromURI(Object nod)
	{
		org.apache.jena.util.iterator.ExtendedIterator triples;
		org.apache.jena.graph.Node node = nod instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)nod : null;
		org.apache.jena.graph.Graph G = model.getGraph();
		triples = G.find(node, null, null);

		ArrayList<Tuple<Boolean, String, Object>> ret = new ArrayList<Tuple<Boolean, String, Object>>();
		while (triples.hasNext())
		{
			org.apache.jena.graph.Triple trp = (org.apache.jena.graph.Triple)triples.next();
			org.apache.jena.graph.Node prp = trp.getPredicate();
			org.apache.jena.graph.Node val = trp.getObject();
			if (G.contains(prp, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL.ObjectProperty.asNode()))
			{
				ret.add(Tuple.Create(true, prp.toString(), (Object)val.toString()));
			}
			else if (G.contains(prp, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL.DatatypeProperty.asNode()))
			{
				if (val.isLiteral())
				{
					Object v = cognipy.sparql.SparqlNode.ToTypedValue(val.toString());
					if (v == null)
					{
						v = JenaRuleManager.getObject(val);
					}
					ret.add(Tuple.Create(false, prp.toString(), v));
				}
			}
		}
		return ret;
	}


	public final void AddRemoveKnowledge(CNL.DL.Paragraph para, boolean isAdd)
	{
		AddRemoveKnowledge(para, isAdd, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public void AddRemoveKnowledge(CNL.DL.Paragraph para, bool isAdd, bool swrlOnly = false)
	public final void AddRemoveKnowledge(CNL.DL.Paragraph para, boolean isAdd, boolean swrlOnly)
	{
		BuildModel();
		InvalidateSyncOntologyToModel();
		DLModSimplifier simli = new DLModSimplifier();
		Object tempVar = simli.Visit(para);
		para = tempVar instanceof CNL.DL.Paragraph ? (CNL.DL.Paragraph)tempVar : null;


		if (para.Statements.Any((s) -> !IsABox(s) && !(s instanceof CNL.DL.Annotation)))
		{
			throw new UnsupportedOperationException("Only A-Box can be modified.");
		}
		else
		{
			if (!isAdd)
			{
				synchronized (sourceParagraph)
				{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
					HashSet<String> toRemoveStmts = new HashSet<String>(from s in para.Statements select Splitting.DLToys.MakeExpressionFromStatement(s));
					tangible.ListHelper.removeAll(sourceParagraph.Statements, (a) -> toRemoveStmts.contains(Splitting.DLToys.MakeExpressionFromStatement(a)));
				}
			}
			for (Statement s : para.Statements)
			{
				if (s instanceof CNL.DL.InstanceOf)
				{
					CNL.DL.InstanceOf stmt = s instanceof CNL.DL.InstanceOf ? (CNL.DL.InstanceOf)s : null;
					org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL((stmt.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.I : null).name, EntityKind.Instance).toString());

					org.apache.jena.rdf.model.Resource clsn;
					if (stmt.C instanceof CNL.DL.Top)
					{
						clsn = org.apache.jena.vocabulary.OWL2.Thing;
					}
					else
					{
						clsn = model.getResource(transform.getIRIFromDL((stmt.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)stmt.C : null).id, EntityKind.Concept).toString());
					}

					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, clsn);
					if (isAdd && !swrlOnly)
					{
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
						AddIfNotExistsRemoveIfExists(isAdd, model, clsn, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Class);
					}
				}
				else if (s instanceof CNL.DL.RelatedInstances)
				{
					CNL.DL.RelatedInstances stmt = s instanceof CNL.DL.RelatedInstances ? (CNL.DL.RelatedInstances)s : null;
					org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL((stmt.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.I : null).name, EntityKind.Instance).toString());
					org.apache.jena.rdf.model.Property reln = model.getProperty(transform.getIRIFromDL((stmt.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)stmt.R : null).id, EntityKind.Role).toString());
					org.apache.jena.rdf.model.Resource jnst = model.getResource(transform.getIRIFromDL((stmt.J instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.J : null).name, EntityKind.Instance).toString());
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, reln, jnst);
					if (isAdd && !swrlOnly)
					{
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
						AddIfNotExistsRemoveIfExists(isAdd, model, jnst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
						AddIfNotExistsRemoveIfExists(isAdd, model, jnst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
						AddIfNotExistsRemoveIfExists(isAdd, model, reln, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.ObjectProperty);
					}
				}
				else if (s instanceof CNL.DL.InstanceValue)
				{
					CNL.DL.InstanceValue stmt = s instanceof CNL.DL.InstanceValue ? (CNL.DL.InstanceValue)s : null;
					org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL((stmt.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.I : null).name, EntityKind.Instance).toString());
					org.apache.jena.rdf.model.Property reln = model.getProperty(transform.getIRIFromDL((stmt.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)stmt.R : null).id, EntityKind.DataRole).toString());
					org.apache.jena.rdf.model.RDFNode node = model.getRDFNode(getLiteralVal(stmt.V).asNode());
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, reln, node);
					if (isAdd && !swrlOnly)
					{
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
						AddIfNotExistsRemoveIfExists(isAdd, model, reln, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty);
					}
				}
				else if (s instanceof CNL.DL.SameInstances)
				{
					CNL.DL.SameInstances stmt = s instanceof CNL.DL.SameInstances ? (CNL.DL.SameInstances)s : null;
					if (stmt.Instances.size() == 2)
					{
						org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL((stmt.Instances.get(0) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(0) : null).name, EntityKind.Instance).toString());
						org.apache.jena.rdf.model.Resource inst2 = model.getResource(transform.getIRIFromDL((stmt.Instances.get(1) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(1) : null).name, EntityKind.Instance).toString());
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.OWL.sameAs, inst2);
						if (isAdd && !swrlOnly)
						{
							AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
							AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
							AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
							AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
						}
					}
					else
					{
						throw new UnsupportedOperationException();
					}
				}
				else if (s instanceof CNL.DL.DifferentInstances)
				{
					CNL.DL.DifferentInstances stmt = s instanceof CNL.DL.DifferentInstances ? (CNL.DL.DifferentInstances)s : null;
					if (stmt.Instances.size() == 2)
					{
						org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL((stmt.Instances.get(0) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(0) : null).name, EntityKind.Instance).toString());
						org.apache.jena.rdf.model.Resource inst2 = model.getResource(transform.getIRIFromDL((stmt.Instances.get(1) instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)stmt.Instances.get(1) : null).name, EntityKind.Instance).toString());
						AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.OWL.differentFrom, inst2);
						if (isAdd && !swrlOnly)
						{
							AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
							AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
							AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
							AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
						}
					}
					else
					{
						throw new UnsupportedOperationException();
					}
				}
			}
		}
	}


	//public void Reset(CNL.DL.Paragraph para, bool structural)
	//{
	//    SwrlBuiltinsExtractor swe = new SwrlBuiltinsExtractor();
	//    var split = swe.Split(para);
	//    sourceParagraph.Statements.AddRange(split.Item1.Statements);
	//    swrlRulesWIthBuiltInsParagraph.Statements.AddRange(split.Item2.Statements);


	//    manager = OWLManager.createOWLOntologyManager();
	//    var df = manager.getOWLDataFactory();

	//    manager.setOntologyFormat(ontology, owlxmlFormat);

	//    transform.setOWLDataFactory(true, ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);
	//    sparqlTransform.setOWLDataFactory(ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);

	//    var conv = transform.Convert(sourceParagraph);
	//    manager.addAxioms(ontology, Ontorion.ARS.Transform.GetJavaAxiomSet(conv.axioms));
	//    manager.addAxioms(ontology, Ontorion.ARS.Transform.GetJavaAxiomSet(conv.additions));

	//    invtransform = new Ontorion.ARS.InvTransform(manager, ontology, owlxmlFormat, CNL.EN.CNLFactory.lex, namc);
	//    invtransform.InvUriMappings = invUriMappings;
	//    invtransform.UriMappings = uriMappings;

	//    try
	//    {
	//        if (structural)
	//            reasoner = new StructuralReasoner(ontology, new SimpleConfiguration(new ProgressMonitor(this)), BufferingMode.BUFFERING);
	//        else
	//        {
	//            OWL2ELProfile elProfile = new OWL2ELProfile();
	//            OWLProfileReport report = elProfile.checkOntology(ontology);

	//            bool inEl = true;
	//            java.util.Iterator it = report.getViolations().iterator();
	//            while (it.hasNext())
	//            {
	//                var v = (OWLProfileViolation)it.next();
	//                if (!(v is UseOfUndeclaredClass) && !(v is UseOfNonAbsoluteIRI) && !(v is UseOfUndeclaredObjectProperty) && !(v is UseOfUndeclaredDataProperty))
	//                {
	//                    inEl = false;
	//                    break;
	//                }
	//            }

	//            if (inEl)
	//            {
	//                materializing_reasner_supports_sroiq = false;
	//                structural_reasoner = new StructuralReasoner(ontology, new SimpleConfiguration(new ProgressMonitor(this)), BufferingMode.BUFFERING);
	//                var configuration = new SimpleConfiguration(new ProgressMonitor(this));
	//                OWLReasonerFactory rf = new org.semanticweb.elk.owlapi.ElkReasonerFactory();
	//                reasoner = rf.createReasoner(ontology, configuration);
	//            }
	//            else
	//            {
	//                var configuration = new org.semanticweb.HermiT.Configuration();
	//                configuration.reasonerProgressMonitor = new ProgressMonitor(this);
	//                configuration.throwInconsistentOntologyException = false;
	//                reasoner = new org.semanticweb.HermiT.Reasoner(configuration, ontology);
	//            }
	//        }
	//    }
	//    catch (Exception ex)
	//    {
	//        throw new ReasoningServiceException(ex.Message.Replace("<" + ontologyIRI.toString() + "#", "'").Replace(">", "'") + ".");
	//    }
	//}


	public final void RemoveRdfInstance(org.apache.jena.rdf.model.Model model2, org.apache.jena.rdf.model.Resource s)
	{
		model2.removeAll(s, null, null);
		model2.removeAll(null, null, s);
		String sur = s.getURI();
		sourceTriplets.RemoveWhere((t) = sur.equals(> t.getObject().getURI()) || sur.equals(t.getSubject().getURI()));
	}

	public final void RemoveInstance(String name)
	{
		BuildModel();
		InvalidateSyncOntologyToModel();
		org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(name, EntityKind.Instance).toString());
		RemoveRdfInstance(model, inst);
	}

	public final void AddRemoveAssertions(java.lang.Iterable<Tuple<String, String, String, Object>> assertions, boolean isAdd, boolean swrlOnly)
	{
		BuildModel();
		InvalidateSyncOntologyToModel();

		for (Tuple<String, String, String, Object> a : assertions)
		{
			if (a.Item1.equals("type"))
			{
				org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
				org.apache.jena.rdf.model.Resource clsn = model.getResource(transform.getIRIFromDL(a.Item4.toString(), EntityKind.Concept).toString());
				AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, clsn);
				if (isAdd && !swrlOnly)
				{
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
					AddIfNotExistsRemoveIfExists(isAdd, model, clsn, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Class);
				}
			}
			else if (a.Item1.equals("==") || a.Item1.equals("=") || a.Item1.equals("!=") || a.Item1.equals("<>"))
			{
				org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
				org.apache.jena.rdf.model.Resource inst2 = model.getResource(transform.getIRIFromDL(a.Item4.toString(), EntityKind.Instance).toString());
				AddIfNotExistsRemoveIfExists(isAdd, model, inst, a.Item1.startsWith("=") ? org.apache.jena.vocabulary.OWL.sameAs : org.apache.jena.vocabulary.OWL.differentFrom, inst2);
				if (isAdd && !swrlOnly)
				{
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
					AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
					AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
				}
			}
			else if (a.Item1.equals("R"))
			{
				org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
				org.apache.jena.rdf.model.Property reln = model.getProperty(transform.getIRIFromDL(a.Item3, EntityKind.Role).toString());
				org.apache.jena.rdf.model.Resource jnst = model.getResource(transform.getIRIFromDL(a.Item4.toString(), EntityKind.Instance).toString());
				AddIfNotExistsRemoveIfExists(isAdd, model, inst, reln, jnst);
				if (isAdd && !swrlOnly)
				{
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
					AddIfNotExistsRemoveIfExists(isAdd, model, jnst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
					AddIfNotExistsRemoveIfExists(isAdd, model, jnst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
					AddIfNotExistsRemoveIfExists(isAdd, model, reln, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.ObjectProperty);
				}
			}
			else if (a.Item1.equals("D"))
			{
				org.apache.jena.rdf.model.Resource inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
				org.apache.jena.rdf.model.Property reln = model.getProperty(transform.getIRIFromDL(a.Item3, EntityKind.DataRole).toString());
				org.apache.jena.rdf.model.RDFNode node = model.getRDFNode(getLiteralVal(CNL.DL.Value.FromObject(a.Item4)).asNode());
				AddIfNotExistsRemoveIfExists(isAdd, model, inst, reln, node);
				if (isAdd && !swrlOnly)
				{
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
					AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
					AddIfNotExistsRemoveIfExists(isAdd, model, reln, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty);
				}
			}
			else
			{
				throw new IllegalStateException("unknow type of assertion");
			}
		}
	}

}