package cognipy.ars;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class InvTransform implements OWLObjectVisitor
{
	private OWLOntologyManager owlManager;
	private OWLOntology _ontology = null;

	public HashMap<String, String> Pfx2ns = new HashMap<String, String>(Map.ofEntries(Map.entry("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"), Map.entry("rdfs", "http://www.w3.org/2000/01/rdf-schema#"), Map.entry("owl", "http://www.w3.org/2002/07/owl#"), Map.entry("dcterms", "http://purl.org/dc/terms/"), Map.entry("skos", "http://www.w3.org/2004/02/skos/core#")));

	public HashMap<String, String> Ns2pfx = new HashMap<String, String>(Map.ofEntries(Map.entry("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"), Map.entry("http://www.w3.org/2000/01/rdf-schema#", "rdfs"), Map.entry("http://www.w3.org/2002/07/owl#", "owl"), Map.entry("http://purl.org/dc/terms/", "dcterms"), Map.entry("http://www.w3.org/2004/02/skos/core#", "skos")));

	public String defaultNs;

	private cognipy.cnl.en.endict lex = CNL.EN.CNLFactory.lex;

	//public InvTransform(OWLOntologyManager owlManager) { this.owlManager = owlManager; }
	/** 
	 Constructor.
	 
	 @param owlManager
	 @param defaultOntology This is the default ontology that will be used each time (unless than for the Convert(OWLOntology) function.
	*/

	public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, String ontologyLocation, NameingConventionKind nck)
	{
		this(owlManager, defaultOntology, ontologyLocation, nck, null);
	}

	public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, String ontologyLocation)
	{
		this(owlManager, defaultOntology, ontologyLocation, NameingConventionKind.CamelCase, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, string ontologyLocation, NameingConventionKind nck = NameingConventionKind.CamelCase, Func<string, IEnumerable<string>> getForms = null)
	public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, String ontologyLocation, NameingConventionKind nck, tangible.Func1Param<String, java.lang.Iterable<String>> getForms)
	{
		if (getForms == null)
		{
			getForms = (String arg) -> new ArrayList<String>(Arrays.asList(word));
		}

		this.owlManager = owlManager;
		this._ontology = defaultOntology;

		if (owlManager.getOntologyFormat(defaultOntology) == null)
		{
			throw new RuntimeException("Cannot infer the Ontology format from the ontology. You should provide the ontology format directly.");
		}

		org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat nm = owlManager.getOntologyFormat(defaultOntology).asPrefixOWLOntologyFormat();
		if ((nm.getDefaultPrefix() == null || nm.getDefaultPrefix().equals("http://www.w3.org/2002/07/owl#")) && defaultOntology.getOntologyID().getOntologyIRI() != null)
		{
			nm.setDefaultPrefix(defaultOntology.getOntologyID().getOntologyIRI().toString());
		}
		else if (nm.getDefaultPrefix() == null && ontologyLocation != null)
		{
			nm.setDefaultPrefix(ontologyLocation);
		}

		setNameProvider(nm, CNL.EN.CNLFactory.lex);
		if (nck != NameingConventionKind.CamelCase)
		{
			cognipy.ars.IOwlNameingConvention namc = null;
			if (nck == NameingConventionKind.Smart)
			{
				throw new UnsupportedOperationException();
			}
			//                    namc = new Ontorion.ARS.OwlNameingConventionSmartImport(getForms);
			else
			{
				namc = new cognipy.ars.OwlNameingConventionUnderscore(nck == NameingConventionKind.Underscored ? '_' : '-', nck == NameingConventionKind.Underscored ? false : true);
			}
			setNameingConvention(namc);
		}
	}

	/** 
	 Zero constructor 
	*/
	public InvTransform()
	{
	}


	public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		this(owlManager, defaultOntology, namespaceManager, lex, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, PrefixOWLOntologyFormat namespaceManager, CogniPy.CNL.EN.endict lex, IOwlNameingConvention owlNameingConvention = null)
	public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex, IOwlNameingConvention owlNameingConvention)
	{
		this.owlManager = owlManager;
		this._ontology = defaultOntology;
		setNameProvider(namespaceManager, lex);
		if (owlNameingConvention != null)
		{
			setNameingConvention(owlNameingConvention);
		}
	}

	private void setNameProvider(PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		Map map = namespaceManager.getPrefixName2PrefixMap();
		Iterator keys = map.keySet().iterator();
		while (keys.hasNext())
		{
			String k = keys.next().toString();
			String v = map.get(k).toString();
			k = k.split("[:]", -1)[0];
			if (tangible.StringHelper.isNullOrEmpty(k))
			{
				defaultNs = v;
				continue;
			}
			if (!Pfx2ns.containsKey(k.replace(".", "$")))
			{
				Pfx2ns.put(k.replace(".", "$"), v);
				if (!Ns2pfx.containsKey(v))
				{
					Ns2pfx.put(v, k.replace(".", "$"));
				}
			}
		}

		this.lex = lex;
	}

	private IOwlNameingConvention owlNameingConventionCC = null;
	private IOwlNameingConvention owlNameingConvention = new OwlNameingConventionCamelCase();
	private void setNameingConvention(IOwlNameingConvention owlNameingConvention)
	{
		this.owlNameingConvention = owlNameingConvention;
		if (!(owlNameingConvention instanceof OwlNameingConventionCamelCase))
		{
			this.owlNameingConventionCC = new OwlNameingConventionCamelCase();
		}
	}

	private Object ret = null;

	public final cognipy.cnl.dl.Paragraph Convert(OWLOntology ontology)
	{
		declaredEntities = new TreeMap<String, CNL.DL.Statement>();
		iriKindCache = new HashMap<String, EntityKind>();
		OWLOntology defaultOnt = _ontology;
		_ontology = ontology;
		ret = null;
		ontology.accept(this);
		_ontology = defaultOnt;
		return ret instanceof cognipy.cnl.dl.Paragraph ? (cognipy.cnl.dl.Paragraph)ret : null;
	}

	public final CNL.DL.Statement Convert(OWLAxiom axiom)
	{
		declaredEntities = new TreeMap<String, CNL.DL.Statement>();
		iriKindCache = new HashMap<String, EntityKind>();
		ret = null;
		axiom.accept(this);
		// TODO ALESSANDRO here we should return a paragraph if some additional annotaton axioms where found!
		return ret instanceof CNL.DL.Statement ? (CNL.DL.Statement)ret : null;
	}

	public final CNL.DL.Node Convert(OWLClassExpression axiom)
	{
		declaredEntities = new TreeMap<String, CNL.DL.Statement>();
		iriKindCache = new HashMap<String, EntityKind>();
		ret = null;
		axiom.accept(this);
		return ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
	}

	public final void visit(OWLOntology ontology)
	{
		cognipy.cnl.dl.Paragraph par = new CNL.DL.Paragraph(null);
		par.Statements = new ArrayList<CNL.DL.Statement>();
		Iterator axioms = ontology.getAxioms().iterator();
		while (axioms.hasNext())
		{
			Object tempVar = axioms.next();
			OWLAxiom ax = tempVar instanceof OWLAxiom ? (OWLAxiom)tempVar : null;
			ret = null;
			ax.accept(this);
			CNL.DL.Statement toAdd = ret instanceof CNL.DL.Statement ? (CNL.DL.Statement)ret : null;
			if (toAdd != null)
			{
				par.Statements.add(toAdd);
			}
		}
		ArrayList<CNL.DL.Statement> decls = new ArrayList<CNL.DL.Statement>();
		for (Map.Entry<String, cognipy.cnl.dl.Statement> ax : declaredEntities.entrySet())
		{
			if (!usedEntities.contains(ax.getKey()))
			{
				decls.add(ax.getValue());
			}
		}
		par.Statements.addAll(0, decls);
		if (!_annotMan.GetAnnotationSubjects().isEmpty())
		{
			for (Map.Entry<ARS.EntityKind, ArrayList<DL.DLAnnotationAxiom>> ann : _annotMan.getDLAnnotationAxioms().entrySet())
			{
				par.Statements.addAll(ann.getValue());
			}
		}

		ret = par;
	}

	private static NamespaceUtil namespaceUtil = new NamespaceUtil();

	// used in TODL so arg will be a namespace (without <>) and we return a namespace with <>
	private String ns2pfx(String arg)
	{
		if (arg == null)
		{
			return "<" + defaultNs + ">";
		}

		if (!arg.endsWith("/") && !arg.endsWith("#") && !arg.contains("#"))
		{
			arg += "#";
		}

		if (Ns2pfx.containsKey(arg))
		{
			return Ns2pfx.get(arg);
		}
		else if (namespaceUtil.getNamespace2PrefixMap().containsKey(arg))
		{
			return namespaceUtil.getNamespace2PrefixMap().get(arg).toString();
		}
		else if (!arg.startsWith("<") && !arg.endsWith(">"))
		{
			return "<" + arg + ">";
		}
		else
		{
			return arg;
		}
	}

	// user in FROMDL so arg will be a DLprefix (with or without <>) and the return value should be without <>
	private String pfx2ns(String arg)
	{
		if (arg == null)
		{
			return defaultNs;
		}

		if (!Pfx2ns.containsKey(arg))
		{
			if (arg.startsWith("<") && arg.endsWith(">"))
			{
				String argg = arg.substring(1, 1 + arg.length() - 2);
				if (!argg.endsWith("/") && !argg.endsWith("#") && !argg.contains("#"))
				{
					argg += "#";
				}
				return argg;
			}
			else
			{
				return "http://unknown.prefix/" + arg + "#";
			}
		}
		else
		{
			return Pfx2ns.get(arg);
		}
	}

	public HashMap<Tuple<EntityKind, String>, String> UriMappings = new HashMap<Tuple<EntityKind, String>, String>();
	public HashMap<String, String> InvUriMappings = new HashMap<String, String>();

	private String ensureQuotation(IOwlNameingConvention conv, CNL.DL.DlName dl_id, EntityKind makeFor)
	{
		cognipy.ars.OwlName cc_owl = conv.FromDL(dl_id, lex, (string arg) -> pfx2ns(arg), makeFor);
		cognipy.cnl.dl.DlName.Parts p_dl = dl_id.Split();
		cognipy.cnl.dl.DlName dl_cc_id = conv.ToDL(cc_owl, lex, (string arg) -> ns2pfx(arg), makeFor);
		cognipy.cnl.dl.DlName.Parts p_cc_dl = dl_cc_id.Split();
		if (!p_cc_dl.name.equals(p_dl.name))
		{
			cognipy.cnl.dl.DlName.Parts p = dl_id.Split();
			p.quoted = true;
			return p.Combine().id;
		}
		else
		{
			return dl_cc_id.id;
		}
	}


	public final String renderEntity(OWLEntity entity, EntityKind makeFor)
	{
		return renderEntity(entity, makeFor, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string renderEntity(OWLEntity entity, EntityKind makeFor, bool useCamelCase = false)
	public final String renderEntity(OWLEntity entity, EntityKind makeFor, boolean useCamelCase)
	{
		OwlName tempVar = new OwlName();
		tempVar.iri = entity.getIRI();
		return renderEntity(tempVar, makeFor, useCamelCase);
	}


	public final String renderEntity(OwlName owlName, EntityKind makeFor)
	{
		return renderEntity(owlName, makeFor, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string renderEntity(OwlName owlName, EntityKind makeFor, bool useCamelCase = false)
	public final String renderEntity(OwlName owlName, EntityKind makeFor, boolean useCamelCase)
	{
		try
		{
			String convId;
			if (!useCamelCase && owlNameingConventionCC != null)
			{
				if (UriMappings.containsKey(Tuple.Create(makeFor, owlName.iri.toString())))
				{
					return UriMappings.get(Tuple.Create(makeFor, owlName.iri.toString()));
				}
				String id = ensureQuotation(owlNameingConventionCC, owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), makeFor), makeFor);
				String ccid = ensureQuotation(owlNameingConventionCC, owlNameingConventionCC.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), makeFor), makeFor);
				if (!id.equals(ccid))
				{
					if (InvUriMappings.containsKey(id))
					{
						return id;
					}
					UriMappings.put(Tuple.Create(makeFor, owlName.iri.toString()), id);
					InvUriMappings.put(id, owlName.iri.toString());
				}
				convId = id;
				return id;
			}
			else
			{
				return ensureQuotation(owlNameingConventionCC != null ? owlNameingConventionCC : owlNameingConvention, (owlNameingConventionCC != null ? owlNameingConventionCC : owlNameingConvention).ToDL(owlName, lex, (string arg) -> ns2pfx(arg), makeFor), makeFor);
			}

		}
		catch (java.lang.Exception e)
		{
		}
		return null;
	}


	public final String renderEntity(String uri, EntityKind makeFor)
	{
		return renderEntity(uri, makeFor, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string renderEntity(string uri, EntityKind makeFor, bool useCamelCase = false)
	public final String renderEntity(String uri, EntityKind makeFor, boolean useCamelCase)
	{
		OwlName tempVar = new OwlName();
		tempVar.iri = IRI.create(uri);
		return renderEntity(tempVar, makeFor, useCamelCase);
	}

	private TreeMap<String, cognipy.cnl.dl.Statement> declaredEntities = new TreeMap<String, CNL.DL.Statement>();
	private boolean useEntityDeclMode = false;
	private HashSet<String> usedEntities = new HashSet<String>();

	public final void visit(OWLDeclarationAxiom axiom)
	{
		ret = null;
		org.semanticweb.owlapi.model.OWLEntity ent = axiom.getEntity();
		if (ent instanceof OWLClass)
		{
			cognipy.cnl.dl.Subsumption stmt = new cognipy.cnl.dl.Subsumption(null);

			useEntityDeclMode = true;
			ent.accept(this);
			useEntityDeclMode = false;
			if (ret instanceof CNL.DL.Top || ret instanceof CNL.DL.Bottom)
			{
				return;
			}

			CNL.DL.Atomic atom = ret instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)ret : null;

			stmt.C = atom;
			stmt.D = new CNL.DL.Top(null);
			if (!declaredEntities.containsKey("C:" + atom.id))
			{
				declaredEntities.put("C:" + atom.id, stmt);
			}

			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
		}
		else if (ent instanceof OWLIndividual)
		{
			cognipy.cnl.dl.InstanceOf stmt = new cognipy.cnl.dl.InstanceOf(null);

			useEntityDeclMode = true;
			ent.accept(this);
			useEntityDeclMode = false;

			CNL.DL.NamedInstance atom = ret instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)ret : null;
			if (atom == null)
			{
				return;
			}

			stmt.I = atom;
			stmt.C = new CNL.DL.Top(null);
			if (!declaredEntities.containsKey("I:" + atom.name))
			{
				declaredEntities.put("I:" + atom.name, stmt);
			}

			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
		}
		else if (ent instanceof OWLObjectProperty)
		{
			cognipy.cnl.dl.RoleInclusion stmt = new cognipy.cnl.dl.RoleInclusion(null);

			useEntityDeclMode = true;
			ent.accept(this);
			useEntityDeclMode = false;
			if (ret instanceof CNL.DL.Top || ret instanceof CNL.DL.Bottom)
			{
				return;
			}

			CNL.DL.Atomic atom = ret instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)ret : null;

			stmt.C = atom;
			stmt.D = new CNL.DL.Top(null);
			if (!declaredEntities.containsKey("R:" + atom.id))
			{
				declaredEntities.put("R:" + atom.id, stmt);
			}

			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
		}
		else if (ent instanceof OWLDataProperty)
		{
			cognipy.cnl.dl.DataRoleInclusion stmt = new cognipy.cnl.dl.DataRoleInclusion(null);

			useEntityDeclMode = true;
			ent.accept(this);
			useEntityDeclMode = false;
			if (ret instanceof CNL.DL.Top || ret instanceof CNL.DL.Bottom)
			{
				return;
			}

			CNL.DL.Atomic atom = ret instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)ret : null;

			stmt.C = atom;
			stmt.D = new CNL.DL.Top(null);
			if (!declaredEntities.containsKey("D:" + atom.id))
			{
				declaredEntities.put("D:" + atom.id, stmt);
			}

			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
		}
		else if (ent instanceof OWLDatatype)
		{
			cognipy.cnl.dl.DataTypeDefinition stmt = new CNL.DL.DataTypeDefinition(null);
			useEntityDeclMode = true;
			ent.accept(this);
			useEntityDeclMode = false;
			if (!(ret instanceof CNL.DL.DTBound))
			{
				return;
			}

			CNL.DL.DTBound atom = (ret instanceof CNL.DL.DTBound ? (CNL.DL.DTBound)ret : null);
			stmt.name = atom.name;
			stmt.B = new CNL.DL.TopBound(null);

			if (!declaredEntities.containsKey("T:" + atom.name))
			{
				declaredEntities.put("T:" + atom.name, stmt);
			}

			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
		}
		//do nothing
	}

	public final void visit(OWLSubClassOfAxiom axiom)
	{
		if (axiom.getSubClass() instanceof OWLObjectOneOf)
		{
			if (((OWLObjectOneOf)axiom.getSubClass()).getIndividuals().size() == 1)
			{
				OWLIndividual ind = (OWLIndividual)((OWLObjectOneOf)axiom.getSubClass()).getIndividuals().toArray()[0];
				axiom.getSuperClass().accept(this);
				CNL.DL.InstanceOf stmt = new CNL.DL.InstanceOf(null, ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null, CNL.DL.Statement.Modality.IS);
				ind.accept(this);
				stmt.I = ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null;
				if (axiom.isAnnotated())
				{
					appendAnnotationsToManager(axiom, stmt);
				}
				ret = stmt;

				return;
			}
		}

		{
			cognipy.cnl.dl.Subsumption stmt = new cognipy.cnl.dl.Subsumption(null);
			axiom.getSubClass().accept(this);
			stmt.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
			axiom.getSuperClass().accept(this);
			stmt.D = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
			ret = stmt;
		}
	}

	public final void visit(OWLNegativeObjectPropertyAssertionAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}


	public final void visit(OWLReflexiveObjectPropertyAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}


	public final void visit(OWLDisjointClassesAxiom axiom)
	{
		Iterator classes = axiom.getClassExpressions().iterator();

		cognipy.cnl.dl.Disjoint stmt = new CNL.DL.Disjoint(null);
		stmt.Disjoints = new ArrayList<CNL.DL.Node>();
		while (classes.hasNext())
		{
			Object tempVar = classes.next();
			OWLClassExpression Do = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
			Do.accept(this);
			stmt.Disjoints.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLDataPropertyDomainAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLObjectPropertyDomainAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}


	public final void visit(OWLNegativeDataPropertyAssertionAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLDifferentIndividualsAxiom axiom)
	{
		CNL.DL.DifferentInstances expr = new CNL.DL.DifferentInstances(null);
		expr.Instances = new ArrayList<CNL.DL.Instance>();
		Iterator inds = axiom.getIndividuals().iterator();
		while (inds.hasNext())
		{
			Object ind = inds.next();
			Assert(ind instanceof OWLIndividual);
			(ind instanceof OWLIndividual ? (OWLIndividual)ind : null).accept((OWLIndividualVisitor)this);
			expr.Instances.add(ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null);
		}

		if (expr.Instances.size() < 2)
		{
			ret = null;
		}
		else
		{
			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, expr);
			}
			ret = expr;
		}
	}

	public final void visit(OWLObjectPropertyRangeAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLObjectPropertyAssertionAxiom axiom)
	{
		cognipy.cnl.dl.RelatedInstances stmt = new CNL.DL.RelatedInstances(null);
		org.semanticweb.owlapi.model.OWLIndividual i = axiom.getSubject();
		org.semanticweb.owlapi.model.OWLPropertyAssertionObject j = axiom.getObject();
		org.semanticweb.owlapi.model.OWLPropertyExpression prop = axiom.getProperty();
		(i instanceof OWLIndividual ? (OWLIndividual)i : null).accept((OWLIndividualVisitor)this);
		stmt.I = ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null;
		(j instanceof OWLIndividual ? (OWLIndividual)j : null).accept((OWLIndividualVisitor)this);
		stmt.J = ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null;
		(prop instanceof OWLPropertyExpression ? (OWLPropertyExpression)prop : null).accept(this);
		stmt.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLFunctionalObjectPropertyAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLSubObjectPropertyOfAxiom axiom)
	{
		cognipy.cnl.dl.RoleInclusion stmt = new cognipy.cnl.dl.RoleInclusion(null);
		axiom.getSubProperty().accept(this);
		stmt.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		axiom.getSuperProperty().accept(this);
		stmt.D = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLDataPropertyRangeAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLFunctionalDataPropertyAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLEquivalentDataPropertiesAxiom axiom)
	{
		Iterator roles = axiom.getProperties().iterator();

		cognipy.cnl.dl.DataRoleEquivalence stmt = new CNL.DL.DataRoleEquivalence(null);
		stmt.Equivalents = new ArrayList<CNL.DL.Node>();
		while (roles.hasNext())
		{
			Object tempVar = roles.next();
			OWLDataProperty Do = tempVar instanceof OWLDataProperty ? (OWLDataProperty)tempVar : null;
			Do.accept((OWLPropertyExpressionVisitor)this);
			stmt.Equivalents.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (stmt.Equivalents.size() < 2)
		{
			ret = null;
		}
		else
		{
			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
			ret = stmt;
		}
	}

	public final void visit(OWLClassAssertionAxiom axiom)
	{
		cognipy.cnl.dl.InstanceOf stmt = new CNL.DL.InstanceOf(null);
		axiom.getClassExpression().accept(this);
		stmt.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		axiom.getIndividual().accept(this);
		stmt.I = ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null;

		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLEquivalentClassesAxiom axiom)
	{

		Iterator classes = axiom.getClassExpressions().iterator();

		cognipy.cnl.dl.Equivalence stmt = new CNL.DL.Equivalence(null);
		stmt.Equivalents = new ArrayList<CNL.DL.Node>();
		while (classes.hasNext())
		{
			Object tempVar = classes.next();
			OWLClassExpression Do = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
			Do.accept(this);
			stmt.Equivalents.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (stmt.Equivalents.size() < 2)
		{
			ret = null;
		}
		else
		{
			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
			ret = stmt;
		}
	}

	public final void visit(OWLEquivalentObjectPropertiesAxiom axiom)
	{
		Iterator roles = axiom.getProperties().iterator();

		cognipy.cnl.dl.RoleEquivalence stmt = new CNL.DL.RoleEquivalence(null);
		stmt.Equivalents = new ArrayList<CNL.DL.Node>();
		while (roles.hasNext())
		{
			Object Do = roles.next();
			(Do instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)Do : null).accept((OWLPropertyExpressionVisitor)this);
			stmt.Equivalents.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (stmt.Equivalents.size() < 2)
		{
			ret = null;
		}
		else
		{
			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, stmt);
			}
			ret = stmt;
		}
	}

	public final void visit(OWLDataPropertyAssertionAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLTransitiveObjectPropertyAxiom axiom)
	{
		axiom.getProperty().accept(this);
		CNL.DL.Node role = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		CNL.DL.ComplexRoleInclusion expr = new CNL.DL.ComplexRoleInclusion(null);
		expr.RoleChain = new ArrayList<CNL.DL.Node>();
		expr.R = role;
		expr.RoleChain.add(role);
		expr.RoleChain.add(role);
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, expr);
		}
		ret = expr;
	}

	public final void visit(OWLIrreflexiveObjectPropertyAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLInverseFunctionalObjectPropertyAxiom axiom)
	{
		axiom.asOWLSubClassOfAxiom().accept(this);
	}

	public final void visit(OWLInverseObjectPropertiesAxiom axiom)
	{
		axiom.getFirstProperty().accept(this);
		CNL.DL.Node firstRole = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		axiom.getSecondProperty().accept(this);
		CNL.DL.Node secondRole = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		CNL.DL.RoleEquivalence expr = new CNL.DL.RoleEquivalence(null);
		expr.Equivalents = new ArrayList<CNL.DL.Node>();
		expr.Equivalents.add(firstRole);
		expr.Equivalents.add(new CNL.DL.RoleInversion(null, secondRole));
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, expr);
		}
		ret = expr;
	}

	public final void visit(OWLAsymmetricObjectPropertyAxiom axiom)
	{
		axiom.getProperty().accept(this);
		CNL.DL.Node role = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		CNL.DL.RoleDisjoint expr = new CNL.DL.RoleDisjoint(null);
		expr.Disjoints = new ArrayList<CNL.DL.Node>();
		expr.Disjoints.add(role);
		expr.Disjoints.add(new CNL.DL.RoleInversion(null, role));
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, expr);
		}
		ret = expr;
	}

	public final void visit(OWLSymmetricObjectPropertyAxiom axiom)
	{
		axiom.getProperty().accept(this);
		CNL.DL.Node role = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		CNL.DL.RoleEquivalence expr = new CNL.DL.RoleEquivalence(null);
		expr.Equivalents = new ArrayList<CNL.DL.Node>();
		expr.Equivalents.add(role);
		expr.Equivalents.add(new CNL.DL.RoleInversion(null, role));
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, expr);
		}
		ret = expr;
	}

	public final void visit(OWLClass desc)
	{
		if (desc.isOWLThing() || desc.getIRI().isThing())
		{
			ret = new CNL.DL.Top(null);
		}
		else if (desc.isOWLNothing() || desc.getIRI().isNothing())
		{
			ret = new CNL.DL.Bottom(null);
		}
		else
		{
			String id = renderEntity(desc, EntityKind.Concept);
			// annotations start
			appendAnnotationsToManager(desc);
			// annotations end
			ret = new CNL.DL.Atomic(null);
			ret.id = id;
			if (!useEntityDeclMode)
			{
				usedEntities.add("C:" + id);
			}
		}
	}

	public final void visit(OWLObjectIntersectionOf desc)
	{
		CNL.DL.ConceptAnd expr = new CNL.DL.ConceptAnd(null);
		expr.Exprs = new ArrayList<CNL.DL.Node>();

		Iterator classes = desc.getOperands().iterator();
		while (classes.hasNext())
		{
			Object tempVar = classes.next();
			OWLClassExpression Do = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
			Do.accept(this);
			CNL.DL.Node D = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
			expr.Exprs.add(D);
		}

		ret = expr;
	}

	public final void visit(OWLObjectUnionOf desc)
	{
		CNL.DL.ConceptOr expr = new CNL.DL.ConceptOr(null);
		expr.Exprs = new ArrayList<CNL.DL.Node>();

		Iterator classes = desc.getOperands().iterator();
		while (classes.hasNext())
		{
			Object tempVar = classes.next();
			OWLClassExpression Do = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
			Do.accept(this);
			CNL.DL.Node D = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
			expr.Exprs.add(D);
		}

		ret = expr;
	}

	public final void visit(OWLObjectComplementOf desc)
	{
		cognipy.cnl.dl.ConceptNot expr = new cognipy.cnl.dl.ConceptNot(null);

		desc.getOperand().accept(this);
		expr.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;

		ret = expr;
	}

	public final void visit(OWLObjectSomeValuesFrom desc)
	{
		CNL.DL.SomeRestriction expr = new CNL.DL.SomeRestriction(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getFiller().accept(this);
		expr.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLObjectAllValuesFrom desc)
	{
		CNL.DL.OnlyRestriction expr = new CNL.DL.OnlyRestriction(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getFiller().accept(this);
		expr.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLObjectHasValue desc)
	{
		CNL.DL.SomeRestriction expr = new CNL.DL.SomeRestriction(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		CNL.DL.InstanceSet instSet = new CNL.DL.InstanceSet(null);
		instSet.Instances = new ArrayList<CNL.DL.Instance>();
		Iterator inds = desc.getIndividualsInSignature().iterator();
		while (inds.hasNext())
		{
			Object ind = inds.next();
			Assert(ind instanceof OWLIndividual);
			(ind instanceof OWLIndividual ? (OWLIndividual)ind : null).accept((OWLIndividualVisitor)this);
			instSet.Instances.add(ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null);
		}
		expr.C = instSet;
		ret = expr;
	}

	public final void visit(OWLObjectMinCardinality desc)
	{
		CNL.DL.NumberRestriction expr = new CNL.DL.NumberRestriction(null);
		expr.Kind = "≥";
		expr.N = String.valueOf(desc.getCardinality());
		desc.getFiller().accept(this);
		expr.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLObjectExactCardinality desc)
	{
		CNL.DL.NumberRestriction expr = new CNL.DL.NumberRestriction(null);
		expr.Kind = "=";
		expr.N = String.valueOf(desc.getCardinality());
		desc.getFiller().accept(this);
		expr.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLObjectMaxCardinality desc)
	{
		CNL.DL.NumberRestriction expr = new CNL.DL.NumberRestriction(null);
		expr.Kind = "≤";
		expr.N = String.valueOf(desc.getCardinality());
		desc.getFiller().accept(this);
		expr.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLObjectHasSelf desc)
	{
		CNL.DL.SelfReference expr = new CNL.DL.SelfReference(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLObjectOneOf desc)
	{
		CNL.DL.InstanceSet expr = new CNL.DL.InstanceSet(null);
		expr.Instances = new ArrayList<CNL.DL.Instance>();
		Iterator insts = desc.getIndividuals().iterator();
		while (insts.hasNext())
		{
			Object inst = insts.next();
			Assert(inst instanceof OWLIndividual);
			(inst instanceof OWLIndividual ? (OWLIndividual)inst : null).accept((OWLIndividualVisitor)this);
			expr.Instances.add(ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null);
		}
		ret = expr;
	}

	public final void visit(OWLObjectProperty property)
	{
		if (property.isTopEntity())
		{
			ret = new CNL.DL.Top(null);
		}
		else if (property.isBottomEntity())
		{
			ret = new CNL.DL.Bottom(null);
		}
		else
		{
			// annotations start
			appendAnnotationsToManager(property);
			// annotations end   
			String id = renderEntity(property, EntityKind.Role);
			ret = new CNL.DL.Atomic(null);
			ret.id = id;
			if (!useEntityDeclMode)
			{
				usedEntities.add("R:" + id);
			}
		}
	}


	public final void visit(OWLObjectInverseOf property)
	{
		CNL.DL.RoleInversion expr = new CNL.DL.RoleInversion(null);
		property.getInverse().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLDataProperty property)
	{
		if (property.isTopEntity())
		{
			ret = new CNL.DL.Top(null);
		}
		else if (property.isBottomEntity())
		{
			ret = new CNL.DL.Bottom(null);
		}
		else
		{
			// annotations start
			appendAnnotationsToManager(property);
			// annotations end
			String id = renderEntity(property, EntityKind.DataRole);
			ret = new CNL.DL.Atomic(null);
			ret.id = id;
			if (!useEntityDeclMode)
			{
				usedEntities.add("D:" + id);
			}
		}
	}

	public final boolean isUnnamedIndividual(OWLIndividual individual)
	{
		if (individual instanceof OWLNamedIndividual)
		{
			String id = renderEntity((OWLNamedIndividual)individual, EntityKind.Instance);

			if (!useEntityDeclMode)
			{
				usedEntities.add("I:" + id);
			}

			return id.startsWith("[");
		}
		else
		{
			return true;
		}
	}

	public final void visit(OWLNamedIndividual individual)
	{
		String id = renderEntity(individual, EntityKind.Instance);
		//if (id.StartsWith("["))
		//{
		//    //    		int pos=id.indexOf(']');
		//    //  		string inner=id.substring(0, pos+1);
		//    //		try {
		//    //		ret = udli.complexInstance(inner, sroiq);
		//    //} catch (Exception e) {
		//    // TODO Auto-generated catch block
		//    //e.printStackTrace();
		//    //			}
		//}
		//else
		ret = new CNL.DL.NamedInstance(null);
		ret.name = id;
	}

	public final void visit(OWLHasKeyAxiom axiom)
	{
		cognipy.cnl.dl.HasKey stmt = new CNL.DL.HasKey(null);
		stmt.DataRoles = new ArrayList<CNL.DL.Node>();
		stmt.Roles = new ArrayList<CNL.DL.Node>();
		axiom.getClassExpression().accept(this);
		Assert(ret instanceof CNL.DL.Node);
		stmt.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;

		Iterator roles = axiom.getObjectPropertyExpressions().iterator();
		while (roles.hasNext())
		{
			Object tempVar = roles.next();
			OWLObjectPropertyExpression Do = tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null;
			Do.accept(this);
			stmt.Roles.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		Iterator dataroles = axiom.getDataPropertyExpressions().iterator();
		while (dataroles.hasNext())
		{
			Object tempVar2 = dataroles.next();
			OWLDataPropertyExpression Do = tempVar2 instanceof OWLDataPropertyExpression ? (OWLDataPropertyExpression)tempVar2 : null;
			Do.accept(this);
			stmt.DataRoles.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLDisjointUnionAxiom axiom)
	{
		cognipy.cnl.dl.DisjointUnion stmt = new CNL.DL.DisjointUnion(null);
		stmt.Union = new ArrayList<CNL.DL.Node>();
		axiom.getOWLClass().accept((OWLClassExpressionVisitor)this);
		Assert(ret instanceof CNL.DL.Atomic);
		stmt.name = (ret instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)ret : null).id;
		Iterator unions = axiom.getClassExpressions().iterator();
		while (unions.hasNext())
		{
			Object tempVar = unions.next();
			OWLClassExpression Do = tempVar instanceof OWLClassExpression ? (OWLClassExpression)tempVar : null;
			Do.accept(this);
			stmt.Union.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLDisjointDataPropertiesAxiom axiom)
	{
		Iterator roles = axiom.getProperties().iterator();

		cognipy.cnl.dl.DataRoleDisjoint stmt = new CNL.DL.DataRoleDisjoint(null);
		stmt.Disjoints = new ArrayList<CNL.DL.Node>();
		while (roles.hasNext())
		{
			Object tempVar = roles.next();
			OWLDataProperty Do = tempVar instanceof OWLDataProperty ? (OWLDataProperty)tempVar : null;
			Do.accept((OWLPropertyExpressionVisitor)this);
			stmt.Disjoints.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLDisjointObjectPropertiesAxiom axiom)
	{
		Iterator roles = axiom.getProperties().iterator();

		cognipy.cnl.dl.RoleDisjoint stmt = new CNL.DL.RoleDisjoint(null);
		stmt.Disjoints = new ArrayList<CNL.DL.Node>();
		while (roles.hasNext())
		{
			Object tempVar = roles.next();
			OWLObjectPropertyExpression Do = tempVar instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)tempVar : null;
			Do.accept((OWLPropertyExpressionVisitor)this);
			stmt.Disjoints.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLSubDataPropertyOfAxiom axiom)
	{
		cognipy.cnl.dl.DataRoleInclusion stmt = new cognipy.cnl.dl.DataRoleInclusion(null);
		axiom.getSubProperty().accept(this);
		stmt.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		axiom.getSuperProperty().accept(this);
		stmt.D = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLSubPropertyChainOfAxiom axiom)
	{
		cognipy.cnl.dl.ComplexRoleInclusion stmt = new cognipy.cnl.dl.ComplexRoleInclusion(null);
		stmt.RoleChain = new ArrayList<CNL.DL.Node>();
		Iterator chains = axiom.getPropertyChain().iterator();
		while (chains.hasNext())
		{
			Object r = chains.next();
			Assert(r instanceof OWLObjectPropertyExpression);
			(r instanceof OWLObjectPropertyExpression ? (OWLObjectPropertyExpression)r : null).accept((OWLPropertyExpressionVisitor)this);
			Assert(ret instanceof CNL.DL.Node);
			stmt.RoleChain.add(ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null);
		}

		axiom.getSuperProperty().accept(this);
		stmt.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, stmt);
		}
		ret = stmt;
	}

	public final void visit(OWLSameIndividualAxiom axiom)
	{
		CNL.DL.SameInstances expr = new CNL.DL.SameInstances(null);
		expr.Instances = new ArrayList<CNL.DL.Instance>();
		Iterator inds = axiom.getIndividuals().iterator();
		while (inds.hasNext())
		{
			Object ind = inds.next();
			Assert(ind instanceof OWLIndividual);
			(ind instanceof OWLIndividual ? (OWLIndividual)ind : null).accept((OWLIndividualVisitor)this);
			expr.Instances.add(ret instanceof CNL.DL.Instance ? (CNL.DL.Instance)ret : null);
		}
		if (expr.Instances.size() < 2)
		{
			ret = null;
		}
		else
		{
			if (axiom.isAnnotated())
			{
				appendAnnotationsToManager(axiom, expr);
			}
			ret = expr;
		}
	}

	public final void visit(OWLDataSomeValuesFrom desc)
	{
		CNL.DL.SomeValueRestriction expr = new CNL.DL.SomeValueRestriction(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getFiller().accept(this);
		expr.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		ret = expr;
	}

	public final void visit(OWLDataAllValuesFrom desc)
	{
		CNL.DL.OnlyValueRestriction expr = new CNL.DL.OnlyValueRestriction(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getFiller().accept(this);
		expr.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		ret = expr;
	}

	public final void visit(OWLDataHasValue desc)
	{
		CNL.DL.SomeValueRestriction expr = new CNL.DL.SomeValueRestriction(null);
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		desc.getValue().accept(this);
		expr.B = new CNL.DL.BoundVal(null, "=", ret instanceof CNL.DL.Value ? (CNL.DL.Value)ret : null);
		ret = expr;
	}

	public final void visit(OWLDataMinCardinality desc)
	{
		CNL.DL.NumberValueRestriction expr = new CNL.DL.NumberValueRestriction(null);
		expr.Kind = "≥";
		expr.N = String.valueOf(desc.getCardinality());
		desc.getFiller().accept(this);
		expr.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLDataExactCardinality desc)
	{
		CNL.DL.NumberValueRestriction expr = new CNL.DL.NumberValueRestriction(null);
		expr.Kind = "=";
		expr.N = String.valueOf(desc.getCardinality());
		desc.getFiller().accept(this);
		expr.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLDataMaxCardinality desc)
	{
		CNL.DL.NumberValueRestriction expr = new CNL.DL.NumberValueRestriction(null);
		expr.Kind = "≤";
		expr.N = String.valueOf(desc.getCardinality());
		desc.getFiller().accept(this);
		expr.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		desc.getProperty().accept(this);
		expr.R = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret = expr;
	}

	public final void visit(OWLDatatype node)
	{
		if (node.isTopDatatype())
		{
			ret = new CNL.DL.TopBound(null);
		}
		else if (node.isDouble() || node.isFloat())
		{
			ret = new CNL.DL.TotalBound(null);
			ret.V = new CNL.DL.Float(null, "3.14");
		}
		else if (node.isBuiltIn())
		{
			if (node.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || node.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP)
			{
				ret = new CNL.DL.TotalBound(null);
				ret.V = new CNL.DL.DateTimeVal(null, "2012-01-03");
			}
			else if (node.getBuiltInDatatype().isNumeric())
			{
				ret = new CNL.DL.TotalBound(null);
				ret.V = new CNL.DL.Number(null, "1");
			}
			else if (node.isBoolean())
			{
				ret = new CNL.DL.TotalBound(null);
				ret.V = new CNL.DL.Bool(null, "[1]");
			}
			else
			{
				ret = new CNL.DL.TotalBound(null);
				ret.V = new CNL.DL.String(null, "\'...\'");
			}
		}
		else
		{
			if (node.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#date") || node.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#time"))
			{
				ret = new CNL.DL.TotalBound(null);
				ret.V = new CNL.DL.DateTimeVal(null, "2012-01-03");
			}
			else
			{
				OwlName owlName = new OwlName();
				owlName.iri = node.getIRI();
				String id = owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), EntityKind.Concept).id;
				ret = new CNL.DL.DTBound(null);
				ret.name = id;
			}
		}
	}

	public final void visit(OWLDataComplementOf node)
	{
		ret = null;
		node.getDataRange().accept(this);
		ret = new CNL.DL.BoundNot(null);
		ret.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
	}

	public final void visit(OWLDataIntersectionOf node)
	{
		ArrayList<CNL.DL.AbstractBound> Bounds = new ArrayList<CNL.DL.AbstractBound>();
		ret = null;
		Iterator inds = node.getOperands().iterator();
		while (inds.hasNext())
		{
			Object ind = inds.next();
			Assert(ind instanceof OWLDataRange);
			(ind instanceof OWLDataRange ? (OWLDataRange)ind : null).accept(this);
			Bounds.add(ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null);
		}
		ret = new CNL.DL.BoundAnd(null);
		ret.List = Bounds;
	}

	public final void visit(OWLDataUnionOf node)
	{
		ArrayList<CNL.DL.AbstractBound> Bounds = new ArrayList<CNL.DL.AbstractBound>();
		ret = null;
		Iterator inds = node.getOperands().iterator();
		while (inds.hasNext())
		{
			Object ind = inds.next();
			Assert(ind instanceof OWLDataRange);
			(ind instanceof OWLDataRange ? (OWLDataRange)ind : null).accept(this);
			Bounds.add(ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null);
		}
		ret = new CNL.DL.BoundOr(null);
		ret.List = Bounds;
	}

	public final void visit(OWLDataOneOf node)
	{
		cognipy.cnl.dl.ValueSet set = new cognipy.cnl.dl.ValueSet(null);
		set.Values = new ArrayList<CNL.DL.Value>();
		Iterator vals = node.getValues().iterator();
		while (vals.hasNext())
		{
			Object tempVar = vals.next();
			OWLObject val = tempVar instanceof OWLObject ? (OWLObject)tempVar : null;
			val.accept(this);
			set.Values.add(ret instanceof cognipy.cnl.dl.Value ? (cognipy.cnl.dl.Value)ret : null);
		}
		ret = set;
	}

	public final void visit(OWLDatatypeRestriction node)
	{
		CNL.DL.FacetList FL = new CNL.DL.FacetList(null);
		FL.List = new ArrayList<CNL.DL.Facet>();
		Set set = node.getFacetRestrictions();
		Iterator fr = set.iterator();
		while (fr.hasNext())
		{
			Object tempVar = fr.next();
			OWLObject val = tempVar instanceof OWLObject ? (OWLObject)tempVar : null;
			ret = null;
			val.accept(this);
			FL.List.add(ret instanceof CNL.DL.Facet ? (CNL.DL.Facet)ret : null);
		}
		ret = new CNL.DL.BoundFacets(null);
		ret.FL = FL;
	}

	private static CNL.DL.Value getNumberOrFloatValFromLiteral(OWLLiteral owli)
	{
		int IntNumber;
		double DoubleNumber;
		if (owli.isInteger())
		{
			return new CNL.DL.Number(null, owli.getLiteral());
		}
		else if (owli.isFloat() || owli.isDouble())
		{
			return new CNL.DL.Float(null, String.valueOf(owli.parseDouble()));
		}
		else
		{
			tangible.OutObject<Integer> tempOut_IntNumber = new tangible.OutObject<Integer>();
			if (!owli.toString().contains("^^xsd:decimal") && tangible.TryParseHelper.tryParseInt(owli.getLiteral().replace("\"", ""), tempOut_IntNumber))
			{
			IntNumber = tempOut_IntNumber.argValue;
				return new CNL.DL.Number(null, owli.getLiteral());
			}
			else
			{
			IntNumber = tempOut_IntNumber.argValue;
				tangible.OutObject<Double> tempOut_DoubleNumber = new tangible.OutObject<Double>();
				if (!owli.toString().contains("^^xsd:decimal") && tangible.TryParseHelper.tryParseDouble(owli.getLiteral().replace("\"", ""), tempOut_DoubleNumber))
				{
				DoubleNumber = tempOut_DoubleNumber.argValue;
					return new CNL.DL.Number(null, owli.getLiteral());
				}
				else
				{
				DoubleNumber = tempOut_DoubleNumber.argValue;
					if (owli.toString().contains("^^xsd:decimal"))
					{
						throw new RuntimeException("Cannot import xsd:decimal as this datatype has not been implemented.");
					}
					else
					{
						Assert(false);
					}
				}
			}
		}
		return null;
	}

	private static CNL.DL.Value getNumberLiteral(OWLLiteral owli)
	{
		if (owli.isInteger())
		{
			return new CNL.DL.Number(null, owli.getLiteral());
		}
		else
		{
			Assert(false);
		}
		return null;
	}

	public final void visit(OWLFacetRestriction node)
	{
		OWLLiteral owli = node.getFacetValue();

		CNL.DL.Facet expr = new CNL.DL.Facet(null);
		org.semanticweb.owlapi.vocab.OWLFacet f = node.getFacet();
		int a1 = f.ordinal();
		String a2 = f.name().toUpperCase();
		switch (a2)
		{
			case "MIN_INCLUSIVE":
				expr.Kind = "≥";
				expr.V = getNumberOrFloatValFromLiteral(owli);
				break;
			case "MAX_INCLUSIVE":
				expr.Kind = "≤";
				expr.V = getNumberOrFloatValFromLiteral(owli);
				break;
			case "MIN_EXCLUSIVE":
				expr.Kind = ">";
				expr.V = getNumberOrFloatValFromLiteral(owli);
				break;
			case "MAX_EXCLUSIVE":
				expr.Kind = "<";
				expr.V = getNumberOrFloatValFromLiteral(owli);
				break;
			case "PATTERN":
				expr.Kind = "#";
				expr.V = new CNL.DL.String(null, "\'" + owli.getLiteral().replace("\'", "\'\'") + "\'");
				break;
			case "LENGTH":
				expr.Kind = "<->";
				expr.V = getNumberLiteral(owli);
				break;
			case "MIN_LENGTH":
				expr.Kind = "<-> ≥";
				expr.V = getNumberLiteral(owli);
				break;
			case "MAX_LENGTH":
				expr.Kind = "<-> ≤";
				expr.V = getNumberLiteral(owli);
				break;
			default:
				throw new UnsupportedOperationException("Facet " + a2 + " is not supported currently by FE");
		}

		ret = expr;
	}

	public final void visit(OWLDatatypeDefinitionAxiom axiom)
	{
		CNL.DL.DataTypeDefinition expr = new CNL.DL.DataTypeDefinition(null);

		OwlName owlName = new OwlName();
		owlName.iri = axiom.getDatatype().getIRI();
		expr.name = owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), EntityKind.Concept).id;

		if (!useEntityDeclMode)
		{
			usedEntities.add("T:" + expr.name);
		}

		axiom.getDataRange().accept(this);
		expr.B = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		if (axiom.isAnnotated())
		{
			appendAnnotationsToManager(axiom, expr);
		}
		ret = expr;
	}

	private void appendAnnotationsToManager(OWLClass desc)
	{
		if (_ontology != null)
		{
			Iterator it = desc.getAnnotations(_ontology).iterator();
			OwlName owlName = new OwlName();
			owlName.iri = desc.getIRI();
			appendAnnotationsToManager(owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), EntityKind.Concept).id, EntityKind.Concept, it);
			if (!iriKindCache.containsKey(desc.getIRI().toString()))
			{
				iriKindCache.put(desc.getIRI().toString(), EntityKind.Concept);
			}
		}
	}

	private void appendAnnotationsToManager(OWLProperty prop)
	{
		if (_ontology != null)
		{
			Iterator it = prop.getAnnotations(_ontology).iterator();
			OwlName owlName = new OwlName();
			owlName.iri = prop.getIRI();
			EntityKind kind = EntityKind.Role;
			if (prop instanceof OWLDataProperty)
			{
				kind = EntityKind.DataRole;
			}
			appendAnnotationsToManager(owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), kind).id, kind, it);
			if (!iriKindCache.containsKey(prop.getIRI().toString()))
			{
				iriKindCache.put(prop.getIRI().toString(), kind);
			}
		}
	}

	private void appendAnnotationsToManager(OWLAxiom axx, CNL.DL.Statement stmt)
	{
		if (_ontology != null)
		{
			Iterator it = axx.getAnnotations().iterator();
			cognipy.cnl.dl.Serializer ser = new cognipy.cnl.dl.Serializer(false);
			CNL.DL.Paragraph tempVar = new CNL.DL.Paragraph(null);
			tempVar.Statements = new ArrayList<CNL.DL.Statement>(Arrays.asList(stmt));
			String serializedStmt = ser.Serialize(tempVar);
			// we are here removing the quotes and other things that where added by the DL serializer. This is a bad practice as someone external to the serializer 
			// knows about its internal functioning but for the moment it should work....
			serializedStmt = serializedStmt.replace("\r\n", "");
			appendAnnotationsToManager(serializedStmt, EntityKind.Statement, it);
		}
	}

	private cognipy.cnl.AnnotationManager _annotMan = new cognipy.cnl.AnnotationManager();

	private void appendAnnotationsToManager(String subj, EntityKind kind, Iterator it)
	{
		if (_ontology != null)
		{
			while (it.hasNext())
			{
				OWLAnnotation annot = (OWLAnnotation)it.next();
				_annotMan.appendAnnotations(visitWithReturn(subj, kind, annot));
			}
		}
	}

	private CNL.DL.DLAnnotationAxiom visitWithReturn(String subj, EntityKind kind, OWLAnnotation annot)
	{
		OwlName owlName = new OwlName();
		owlName.iri = annot.getProperty().getIRI();

		String val = "";
		if (annot.getValue() instanceof IRI)
		{
			val = annot.getValue().toString();
		}
		else if (annot.getValue() instanceof OWLAnonymousIndividual)
		{
			val = annot.getValue().toString();
		}
		else
		{
			annot.getValue().accept(this);
			CNL.DL.Value valt = ret instanceof CNL.DL.Value ? (CNL.DL.Value)ret : null;
			val = valt.toString();
			val = System.Net.WebUtility.HtmlDecode(val);
		}

		String lang = "";
		if (annot.getValue() instanceof OWLLiteral)
		{
			Object tempVar = annot.getValue();
			OWLLiteral lit = tempVar instanceof OWLLiteral ? (OWLLiteral)tempVar : null;
			if (lit.hasLang())
			{
				lang = lit.getLang().toString();
			}
		}
		CNL.DL.DLAnnotationAxiom tempVar2 = new CNL.DL.DLAnnotationAxiom(null);
		tempVar2.setsubject(subj);
		tempVar2.setsubjKind(kind.toString());
		tempVar2.annotName = (new OwlNameingConventionCamelCase()).ToDL(owlName, lex, (string arg) -> ns2pfx(arg), EntityKind.Role).id;
		tempVar2.value = val;
		tempVar2.language = lang;
		return tempVar2;
	}

	public final void visit(OWLAnnotation node)
	{
	}

	private EntityKind getKindFromIri(String iri)
	{
		EntityKind subjKind = EntityKind.Concept;
		if (iriKindCache.containsKey(iri))
		{
			subjKind = iriKindCache.get(iri);
		}
		else
		{
			Iterator ontIt = _ontology.getSignature().iterator();
			while (ontIt.hasNext())
			{
				Object tempVar = ontIt.next();
				OWLEntity owlEnt = tempVar instanceof OWLEntity ? (OWLEntity)tempVar : null;
				if (owlEnt.getIRI().toString().equals(iri))
				{
					if (owlEnt.isOWLClass())
					{
						subjKind = EntityKind.Concept;
						break;
					}
					else if (owlEnt.isOWLDataProperty())
					{
						subjKind = EntityKind.DataRole;
						break;
					}
					else if (owlEnt.isOWLObjectProperty())
					{
						subjKind = EntityKind.Role;
						break;
					}
					else if (owlEnt.isOWLNamedIndividual())
					{
						subjKind = EntityKind.Instance;
						break;
					}
				}
			}
			iriKindCache.put(iri, subjKind);
		}

		return subjKind;
	}

	// cache of IRI,EntityKind --> useful when we need to understand what kind the subject of the annotation is.
	private HashMap<String, EntityKind> iriKindCache = new HashMap<String, EntityKind>();
	public final void visit(OWLAnnotationAssertionAxiom axiom)
	{
		if (_ontology != null)
		{
			Object tempVar = axiom.getSubject();
			IRI ir = tempVar instanceof IRI ? (IRI)tempVar : null;
			if (ir != null)
			{
				OwlName owlName = new OwlName();
				owlName.iri = ir;
				EntityKind subjKind = getKindFromIri(ir.toString());
				OWLAnnotation annot = axiom.getAnnotation();
				cognipy.cnl.dl.DLAnnotationAxiom dlSent = visitWithReturn(renderEntity(owlName, subjKind), subjKind, annot);
				if (axiom.isAnnotated())
				{
					appendAnnotationsToManager(axiom, dlSent);
				}
				ret = dlSent;
				// TODO ALESSANDRO this should be the way in which we search for the entity because in OWL there is the pruning so it is possible to have the same name for concept,role,...
				// in this case we should return more than one annotation! The problem is that there is no possibility to return more than one axiom at a time.
				//List<EntityKind> allEnt = new List<EntityKind>();
				//while (ontIt.hasNext())
				//{
				//    var owlEnt = ontIt.next() as OWLEntity;
				//    if (owlEnt.getIRI().toString() == ir.toString())
				//    {
				//        if (owlEnt.isOWLClass())
				//            allEnt.Add(EntityKind.Concept);
				//        else if (owlEnt.isOWLDataProperty())
				//            allEnt.Add(EntityKind.DataRole);
				//        else if (owlEnt.isOWLObjectProperty())
				//            allEnt.Add(EntityKind.Role);
				//        else if (owlEnt.isOWLNamedIndividual())
				//            allEnt.Add(EntityKind.Instance);
				//    }
				//}
			}
		}
	}

	public final void visit(OWLAnnotationPropertyDomainAxiom axiom)
	{
		//var annotForAx = getAnnotationsForAxiom(axiom);
		// this is a domain axiom for the annotation property
		// the domain can be either IRI either Literal
	}

	public final void visit(OWLAnnotationPropertyRangeAxiom axiom)
	{
		//var annotForAx = getAnnotationsForAxiom(axiom);
		// this is a range property axiom for the annotation property
		// the range can be IRI or Literal
	}

	public final void visit(OWLSubAnnotationPropertyOfAxiom axiom)
	{
		//var annotForAx = getAnnotationsForAxiom(axiom);
		// this axiom states the relation between two annotation properties.
	}

	public final void visit(OWLAnnotationProperty property)
	{
		// this is a generic class that has all the information about the annotation property (axioms, subjects,...)
		// the problem is that in the owlapi to get the annotations axioms you need the whole ontology!
		//property.getAnnotationAssertionAxioms(Ontology???)
	}

	public final void visit(OWLAnonymousIndividual individual)
	{
		ret = new CNL.DL.UnnamedInstance(null, false, new CNL.DL.Top(null));
	}

	public final void visit(OWLLiteral node)
	{
		org.semanticweb.owlapi.model.OWLDatatype dt = node.getDatatype();
		if (dt.isTopDatatype())
		{
			ret = new CNL.DL.String(null, node.getLiteral());
		}
		else if (dt.isBottomEntity())
		{
			ret = new CNL.DL.String(null, node.getLiteral());
		}
		else if (dt.isDouble() || dt.isFloat())
		{
			ret = new CNL.DL.Float(null, String.valueOf(node.parseDouble()));
		}
		else if (dt.isInteger())
		{
			ret = new CNL.DL.Number(null, node.getLiteral());
		}
		else if (dt.isString() || node.isRDFPlainLiteral())
		{
			ret = new CNL.DL.String(null, "\'" + node.getLiteral().replace("\'", "\'\'") + "\'");
		}
		else if (dt.isBoolean())
		{
			ret = new CNL.DL.Bool(null, (node.getLiteral().toLowerCase().trim().equals("true") || node.getLiteral().toLowerCase().trim().equals("1")) ? "[1]" : "[0]");
		}
		else if (dt.isBuiltIn())
		{
			if (dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP)
			{
				ret = new CNL.DL.DateTimeVal(null, node.getLiteral());
			}
			else if (dt.getBuiltInDatatype().isNumeric())
			{
				ret = new CNL.DL.Number(null, node.getLiteral());
			}
			else
			{
				ret = new CNL.DL.String(null, "\'" + (node.getLiteral() + ":" + dt.getBuiltInDatatype().toString()).replace("\'", "\'\'") + "\'");
			}
		}
		else if (dt.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#date") || dt.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#time"))
		{
			ret = new CNL.DL.DateTimeVal(null, node.getLiteral());
		}
		else
		{
			ret = new CNL.DL.String(null, "\'" + node.getLiteral().replace("\'", "\'\'") + "\'");
		}
	}

	public final void visit(IRI iri)
	{
		Assert(false);
	}

	/////////////// SWRL ///////////////////////////////////////////////////////

	public final void visit(SWRLRule rule)
	{
		cognipy.cnl.dl.SwrlStatement statement = new cognipy.cnl.dl.SwrlStatement(null);

		Iterator slpi = rule.getBody().iterator();
		statement.slp = new CNL.DL.SwrlItemList(null);
		statement.slp.list = new ArrayList<CNL.DL.SwrlItem>();
		while (slpi.hasNext())
		{
			Object tempVar = slpi.next();
			SWRLAtom atom = tempVar instanceof SWRLAtom ? (SWRLAtom)tempVar : null;
			ret = null;
			atom.accept(this);
			if (ret != null)
			{
				statement.slp.list.add(ret instanceof CNL.DL.SwrlItem ? (CNL.DL.SwrlItem)ret : null);
			}
		}

		Iterator slci = rule.getHead().iterator();
		statement.slc = new CNL.DL.SwrlItemList(null);
		statement.slc.list = new ArrayList<CNL.DL.SwrlItem>();
		while (slci.hasNext())
		{
			Object tempVar2 = slci.next();
			SWRLAtom atom = tempVar2 instanceof SWRLAtom ? (SWRLAtom)tempVar2 : null;
			ret = null;
			atom.accept(this);
			if (ret != null)
			{
				statement.slc.list.add(ret instanceof CNL.DL.SwrlItem ? (CNL.DL.SwrlItem)ret : null);
			}
		}

		if (rule.isAnnotated())
		{
			appendAnnotationsToManager(rule, statement);
		}
		ret = statement;
	}

	public final void visit(SWRLClassAtom node)
	{
		Iterator args = node.getAllArguments().iterator();
		cognipy.cnl.dl.SwrlIObject id_var = null;
		while (args.hasNext())
		{
			Assert(id_var == null);
			Object tempVar = args.next();
			SWRLIArgument arg1 = tempVar instanceof SWRLIArgument ? (SWRLIArgument)tempVar : null;
			ret = null;
			arg1.accept(this);
			if (ret instanceof cognipy.cnl.dl.SwrlIObject)
			{
				id_var = ret instanceof cognipy.cnl.dl.SwrlIObject ? (cognipy.cnl.dl.SwrlIObject)ret : null;
			}
			else if (ret instanceof String)
			{
				id_var = new cognipy.cnl.dl.SwrlIVal(null, ret instanceof String ? (String)ret : null);
			}
			else
			{
				Assert(false);
			}
		}

		ret = null;
		node.getPredicate().accept(this);
		ret = new cognipy.cnl.dl.SwrlInstance(null);
		ret.C = ret instanceof CNL.DL.Node ? (CNL.DL.Node)ret : null;
		ret.I = id_var;
	}

	public final void visit(SWRLObjectPropertyAtom node)
	{
		ArrayList<cognipy.cnl.dl.SwrlIObject> objs = new ArrayList<CNL.DL.SwrlIObject>();
		Iterator args = node.getAllArguments().iterator();
		while (args.hasNext())
		{
			Object tempVar = args.next();
			SWRLIArgument arg1 = tempVar instanceof SWRLIArgument ? (SWRLIArgument)tempVar : null;
			ret = null;
			arg1.accept(this);
			Assert(ret != null);
			if (ret instanceof cognipy.cnl.dl.SwrlIObject)
			{
				objs.add(ret instanceof cognipy.cnl.dl.SwrlIObject ? (cognipy.cnl.dl.SwrlIObject)ret : null);
			}
			else if (ret instanceof String)
			{
				objs.add(new cognipy.cnl.dl.SwrlIVal(null, ret instanceof String ? (String)ret : null));
			}
			else
			{
				Assert(false);
			}
		}
		Assert(objs.size() == 2);
		ret = null;
		node.getPredicate().accept(this);
		Object role = ret;
		if (ret instanceof cognipy.cnl.dl.RoleInversion)
		{
			ret = new CNL.DL.SwrlRole(null);
			ret.I = objs.get(1);
			ret.J = objs.get(0);
			ret.R = ((role instanceof cognipy.cnl.dl.RoleInversion ? (cognipy.cnl.dl.RoleInversion)role : null).R instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)(role instanceof cognipy.cnl.dl.RoleInversion ? (cognipy.cnl.dl.RoleInversion)role : null).R : null).id;
		}
		else
		{
			ret = new CNL.DL.SwrlRole(null);
			ret.I = objs.get(0);
			ret.J = objs.get(1);
			ret.R = (role instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)role : null).id;
		}
	}

	public final void visit(SWRLDataRangeAtom node)
	{
		Iterator args = node.getAllArguments().iterator();
		cognipy.cnl.dl.SwrlDObject id_var = null;
		while (args.hasNext())
		{
			Object tempVar = args.next();
			SWRLArgument arg1 = tempVar instanceof SWRLArgument ? (SWRLArgument)tempVar : null;
			ret = null;
			arg1.accept(this);
			if (ret instanceof cognipy.cnl.dl.SwrlBuiltIn)
			{
				id_var = ret instanceof cognipy.cnl.dl.SwrlDObject ? (cognipy.cnl.dl.SwrlDObject)ret : null;
			}
			else if (ret instanceof cognipy.cnl.dl.Value)
			{
				id_var = new cognipy.cnl.dl.SwrlDVal(null, ret instanceof cognipy.cnl.dl.Value ? (cognipy.cnl.dl.Value)ret : null);
			}
			else if (ret instanceof cognipy.cnl.dl.ISwrlVar)
			{
				id_var = new cognipy.cnl.dl.SwrlDVar(null, (ret instanceof cognipy.cnl.dl.ISwrlVar ? (cognipy.cnl.dl.ISwrlVar)ret : null).getVar());
			}

			Assert(id_var != null);
		}

		ret = null;
		node.getPredicate().accept(this);
		CNL.DL.AbstractBound na = ret instanceof CNL.DL.AbstractBound ? (CNL.DL.AbstractBound)ret : null;
		ret = new cognipy.cnl.dl.SwrlDataRange(null, na, id_var);
	}

	public final void visit(SWRLDataPropertyAtom node)
	{
		Iterator args = node.getAllArguments().iterator();
		ArrayList<Object> objs = new ArrayList<Object>();
		while (args.hasNext())
		{
			Object tempVar = args.next();
			SWRLArgument arg1 = tempVar instanceof SWRLArgument ? (SWRLArgument)tempVar : null;
			ret = null;
			arg1.accept(this);
			Assert(ret != null);
			if (ret instanceof CNL.DL.Value)
			{
				ret = new CNL.DL.SwrlDVal(null, ret instanceof CNL.DL.Value ? (CNL.DL.Value)ret : null);
			}
			else if (ret instanceof String)
			{
				ret = new CNL.DL.SwrlIVal(null);
				ret.I = ret instanceof String ? (String)ret : null;
			}
			objs.add(ret);
		}
		Assert(objs.size() == 2);
		ret = null;
		node.getPredicate().accept(this);

		CNL.DL.Atomic role = ret instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)ret : null;
		Assert(role != null);
		if (objs.get(1) instanceof CNL.DL.SwrlIVar)
		{
			objs.set(1, new CNL.DL.SwrlDVar(null, (objs.get(1) instanceof CNL.DL.SwrlIVar ? (CNL.DL.SwrlIVar)objs.get(1) : null).getVar()));
		}
		ret = new CNL.DL.SwrlDataProperty(null);
		ret.IO = objs.get(0) instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)objs.get(0) : null;
		ret.DO = objs.get(1) instanceof CNL.DL.SwrlDObject ? (CNL.DL.SwrlDObject)objs.get(1) : null;
		ret.R = (role instanceof cognipy.cnl.dl.Atomic ? (cognipy.cnl.dl.Atomic)role : null).id;
	}

	public final void visit(SWRLBuiltInAtom node)
	{
		ArrayList<CNL.DL.ISwrlObject> args = new ArrayList<CNL.DL.ISwrlObject>();
		int len = node.getArguments().size();
		for (int i = 1; i < len + 1; i++)
		{
			Object tempVar = node.getArguments().get(i % len);
			(tempVar instanceof SWRLArgument ? (SWRLArgument)tempVar : null).accept(this);
			Object Aobj = ret;
			CNL.DL.SwrlDObject A = null;
			if (Aobj instanceof CNL.DL.SwrlDObject)
			{
				A = Aobj instanceof CNL.DL.SwrlDObject ? (CNL.DL.SwrlDObject)Aobj : null;
			}
			else if (Aobj instanceof CNL.DL.SwrlIVar)
			{
				A = new CNL.DL.SwrlDVar(null, (Aobj instanceof CNL.DL.SwrlIVar ? (CNL.DL.SwrlIVar)Aobj : null).getVar());
			}
			else if (Aobj instanceof CNL.DL.Value)
			{
				A = new CNL.DL.SwrlDVal(null, Aobj instanceof CNL.DL.Value ? (CNL.DL.Value)Aobj : null);
			}
			else
			{
				throw new UnsupportedOperationException("Cannot interpret first argument for BuiltIn:<" + node.getPredicate().toURI().toString() + ">");
			}

			args.add(A);
		}


		String buildInName = null;

		org.semanticweb.owlapi.model.IRI pred = node.getPredicate();

		if (pred.compareTo(SWRLBuiltInsVocabulary.EQUAL.getIRI()) == 0)
		{
			buildInName = "=";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.NOT_EQUAL.getIRI()) == 0)
		{
			buildInName = "≠";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.LESS_THAN.getIRI()) == 0)
		{
			buildInName = "<";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.LESS_THAN_OR_EQUAL.getIRI()) == 0)
		{
			buildInName = "≤";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.GREATER_THAN.getIRI()) == 0)
		{
			buildInName = ">";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.GREATER_THAN_OR_EQUAL.getIRI()) == 0)
		{
			buildInName = "≥";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD.getIRI()) == 0)
		{
			buildInName = "plus";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT.getIRI()) == 0)
		{
			buildInName = "minus";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.MULTIPLY.getIRI()) == 0)
		{
			buildInName = "times";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.DIVIDE.getIRI()) == 0)
		{
			buildInName = "divided-by";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.INTEGER_DIVIDE.getIRI()) == 0)
		{
			buildInName = "integer-divided-by";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.MOD.getIRI()) == 0)
		{
			buildInName = "modulo";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.POW.getIRI()) == 0)
		{
			buildInName = "raised-to-the-power-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.UNARY_MINUS.getIRI()) == 0)
		{
			buildInName = "minus";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.UNARY_PLUS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ABS.getIRI()) == 0)
		{
			buildInName = "absolute-value-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.CEILING.getIRI()) == 0)
		{
			buildInName = "ceiling-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.FLOOR.getIRI()) == 0)
		{
			buildInName = "floor-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ROUND.getIRI()) == 0)
		{
			buildInName = "round-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ROUND_HALF_TO_EVEN.getIRI()) == 0)
		{
			buildInName = "rounded-with-the-precision-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SIN.getIRI()) == 0)
		{
			buildInName = "sine-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.COS.getIRI()) == 0)
		{
			buildInName = "cosine-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.TAN.getIRI()) == 0)
		{
			buildInName = "tangent-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.BOOLEAN_NOT.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.STRING_EQUALS_IGNORE_CASE.getIRI()) == 0)
		{
			buildInName = "case-ignored";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.STRING_CONCAT.getIRI()) == 0)
		{
			buildInName = "followed-by";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBSTRING.getIRI()) == 0)
		{
			buildInName = "from";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.STRING_LENGTH.getIRI()) == 0)
		{
			buildInName = "length-of";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.NORMALIZE_SPACE.getIRI()) == 0)
		{
			buildInName = "space-normalized";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.UPPER_CASE.getIRI()) == 0)
		{
			buildInName = "upper-cased";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.LOWER_CASE.getIRI()) == 0)
		{
			buildInName = "lower-cased";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.TRANSLATE.getIRI()) == 0)
		{
			buildInName = "translated";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.CONTAINS.getIRI()) == 0)
		{
			buildInName = "contains-string";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.CONTAINS_IGNORE_CASE.getIRI()) == 0)
		{
			buildInName = "contains-case-ignored-string";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.STARTS_WITH.getIRI()) == 0)
		{
			buildInName = "starts-with-string";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ENDS_WITH.getIRI()) == 0)
		{
			buildInName = "ends-with-string";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBSTRING_BEFORE.getIRI()) == 0)
		{
			buildInName = "before";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBSTRING_AFTER.getIRI()) == 0)
		{
			buildInName = "after";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.MATCHES.getIRI()) == 0)
		{
			buildInName = "matches-string";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.REPLACE.getIRI()) == 0)
		{
			buildInName = "replaced";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.TOKENIZE.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.YEAR_MONTH_DURATION.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.DAY_TIME_DURATION.getIRI()) == 0)
		{
			buildInName = "duration";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.DATE_TIME.getIRI()) == 0)
		{
			buildInName = "datetime";
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.DATE.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.TIME.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DATES.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_TIMES.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.RESOLVE_URI.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ANY_URI.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_YEAR_MONTH_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_YEAR_MONTH_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.MULTIPLY_YEAR_MONTH_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.DIVIDE_YEAR_MONTH_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.MULTIPLY_DAY_TIME_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.DIVIDE_DAY_TIME_DURATIONS.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATION_TO_DATE_TIME.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_YEAR_MONTH_DURATION_FROM_DATE_TIME.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATION_FROM_DATE_TIME.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_YEAR_MONTH_DURATION_TO_DATE.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATION_TO_DATE.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_YEAR_MONTH_DURATION_FROM_DATE.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATION_FROM_DATE.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATION_FROM_TIME.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATION_FROM_TIME.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DATE_TIMES_YIELDING_YEAR_MONTH_DURATION.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DATE_TIMES_YIELDING_DAY_TIME_DURATION.getIRI()) == 0)
		{
			buildInName = null;
		}
		else if (pred.compareTo(IRI.create("http://ontorion.com/swrlb#" + "soundsLike")) == 0)
		{
			buildInName = "sounds-like-string";
		}
		else if (pred.compareTo(IRI.create("http://ontorion.com/swrlb#" + "alphaRepresentationOf")) == 0)
		{
			buildInName = "alpha-representation-of";
		}
		else if (pred.compareTo(IRI.create("http://ontorion.com/swrlb#" + "annotation")) == 0)
		{
			buildInName = "annotation";
		}

		if (buildInName == null)
		{
			throw new UnsupportedOperationException("BuiltIn:<" + pred.toURI().toString() + "> is currently not supported");
		}

		ret = new CNL.DL.SwrlBuiltIn(null, buildInName, args);
	}

	public final void visit(SWRLVariable node)
	{
		OwlName owlName = new OwlName();
		owlName.iri = node.getIRI();
		ret = new cognipy.cnl.dl.SwrlIVar(null, owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), EntityKind.SWRLVariable).id);
	}

	public final void visit(SWRLIndividualArgument node)
	{
		OwlName owlName = new OwlName();
		owlName.iri = node.getIndividual().asOWLNamedIndividual().getIRI();
		ret = owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), EntityKind.Instance).id;
	}

	public final void visit(SWRLLiteralArgument node)
	{
		org.semanticweb.owlapi.model.OWLDatatype dt = node.getLiteral().getDatatype();
		if (dt.isTopDatatype())
		{
			ret = new CNL.DL.String(null, node.getLiteral().getLiteral());
		}
		else if (dt.isBottomEntity())
		{
			ret = new CNL.DL.String(null, node.getLiteral().getLiteral());
		}
		else if (dt.isDouble() || dt.isFloat())
		{
			ret = new CNL.DL.Float(null, node.getLiteral().getLiteral());
		}
		else if (dt.isInteger())
		{
			ret = new CNL.DL.Number(null, node.getLiteral().getLiteral());
		}
		else if (dt.isString() || node.getLiteral().isRDFPlainLiteral())
		{
			ret = new CNL.DL.String(null, "\'" + node.getLiteral().getLiteral().replace("\'", "\''") + "\'");
		}
		else if (dt.isBoolean())
		{
			ret = new CNL.DL.Bool(null, node.getLiteral().getLiteral());
		}
		else if (dt.isBuiltIn())
		{
			if (dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP)
			{
				ret = new CNL.DL.DateTimeVal(null, node.getLiteral().getLiteral().replace(' ', 'T'));
			}
			else if (dt.getBuiltInDatatype().isNumeric())
			{
				ret = new CNL.DL.Number(null, node.getLiteral().getLiteral());
			}
			else
			{
				ret = new CNL.DL.String(null, node.getLiteral().getLiteral());
			}
		}
		else if (dt.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#date") || dt.getIRI().toString().equals("http://www.w3.org/2001/XMLSchema#time"))
		{
			ret = new CNL.DL.DateTimeVal(null, node.getLiteral().getLiteral().replace(' ', 'T'));
		}
		else
		{
			ret = new CNL.DL.String(null, "\'" + node.getLiteral() + "\'");
		}
	}

	public final void visit(SWRLSameIndividualAtom node)
	{
		ret = null;
		node.getFirstArgument().accept(this);
		CNL.DL.SwrlIObject n1 = ret instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)ret : null;
		ret = null;
		node.getSecondArgument().accept(this);
		CNL.DL.SwrlIObject n2;
		if (ret instanceof String)
		{
			n2 = new CNL.DL.SwrlIVal(null);
			n2.I = ret instanceof String ? (String)ret : null;
		}
		else
		{
			n2 = ret instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)ret : null;
		}

		ret = new CNL.DL.SwrlSameAs(null);
		ret.I = n1;
		ret.J = n2;
	}

	public final void visit(SWRLDifferentIndividualsAtom node)
	{
		ret = null;
		node.getFirstArgument().accept(this);
		CNL.DL.SwrlIObject n1 = ret instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)ret : null;
		ret = null;
		node.getSecondArgument().accept(this);
		CNL.DL.SwrlIObject n2 = ret instanceof CNL.DL.SwrlIObject ? (CNL.DL.SwrlIObject)ret : null;

		ret = new CNL.DL.SwrlDifferentFrom(null);
		ret.I = n1;
		ret.J = n2;
	}

	/////////// SWRL ////////////////////////////////////////////


	public ArrayList<String> Imports = new ArrayList<String>();

	public final void visit(OWLImportsDeclaration axiom)
	{
		Imports.add(axiom.getIRI().toString());
	}

	public static void Assert(boolean b)
	{
		if (!b)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
			System.Diagnostics.Debugger.Break();
//#endif
			throw new UnsupportedOperationException("Conversion Assertion Failed. OWLAPI->DL");
		}
	}


}