using System;
using System.Collections.Generic;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.vocab;
using org.coode.xml;
using org.semanticweb.owlapi.util;
//using Ontorion.OWL;

namespace Ontorion.ARS
{
    public enum NameingConventionKind
    {
        Smart = 0,
        CamelCase = 1,
        Dashed = 2,
        Underscored = 3,
    }

    public class InvTransform : OWLObjectVisitor
    {
        OWLOntologyManager owlManager;
        OWLOntology _ontology=null;

        public Dictionary<string, string> Pfx2ns = new Dictionary<string, string>(){
            {"rdf", @"http://www.w3.org/1999/02/22-rdf-syntax-ns#"},
            {"rdfs", @"http://www.w3.org/2000/01/rdf-schema#"},
            {"owl", @"http://www.w3.org/2002/07/owl#"},
            {"dcterms", @"http://purl.org/dc/terms/"},
            {"skos", @"http://www.w3.org/2004/02/skos/core#"}
        };

        public Dictionary<string, string> Ns2pfx = new Dictionary<string, string>(){
            {@"http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf" },
            {@"http://www.w3.org/2000/01/rdf-schema#", "rdfs"},
            {@"http://www.w3.org/2002/07/owl#", "owl"},
            {@"http://purl.org/dc/terms/", "dcterms"},
            {@"http://www.w3.org/2004/02/skos/core#", "skos"}
        };

        public string defaultNs;

        Ontorion.CNL.EN.endict lex = CNL.EN.CNLFactory.lex;

        //public InvTransform(OWLOntologyManager owlManager) { this.owlManager = owlManager; }
        /// <summary>
        /// Constructor.
        /// </summary>
        /// <param name="owlManager"></param>
        /// <param name="defaultOntology">This is the default ontology that will be used each time (unless than for the Convert(OWLOntology) function.</param>
        public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, string ontologyLocation, NameingConventionKind nck = NameingConventionKind.CamelCase,Func<string,IEnumerable<string>> getForms=null) 
        {
            if (getForms == null)
                getForms = new Func<string, IEnumerable<string>>((word) => new List<string>() { word });

            this.owlManager = owlManager; 
            this._ontology = defaultOntology;

            if (owlManager.getOntologyFormat(defaultOntology) == null)
                throw new Exception("Cannot infer the Ontology format from the ontology. You should provide the ontology format directly.");

            var nm = owlManager.getOntologyFormat(defaultOntology).asPrefixOWLOntologyFormat();
            if ((nm.getDefaultPrefix() == null || nm.getDefaultPrefix() == "http://www.w3.org/2002/07/owl#") && defaultOntology.getOntologyID().getOntologyIRI() != null)
                nm.setDefaultPrefix(defaultOntology.getOntologyID().getOntologyIRI().toString());
            else if (nm.getDefaultPrefix() == null && ontologyLocation != null)
                nm.setDefaultPrefix(ontologyLocation);

            setNameProvider(nm, CNL.EN.CNLFactory.lex);
            if (nck != NameingConventionKind.CamelCase)
            {
                Ontorion.ARS.IOwlNameingConvention namc = null;
                if (nck == NameingConventionKind.Smart)
                    throw new NotImplementedException();
//                    namc = new Ontorion.ARS.OwlNameingConventionSmartImport(getForms);
                else
                    namc = new Ontorion.ARS.OwlNameingConventionUnderscore(nck == NameingConventionKind.Underscored ? '_' : '-', nck == NameingConventionKind.Underscored ? false : true);
                setNameingConvention(namc);
            }
        }

		/// <summary>
        /// Zero constructor 
        /// </summary>
        public InvTransform() { }

        public InvTransform(OWLOntologyManager owlManager, OWLOntology defaultOntology, PrefixOWLOntologyFormat namespaceManager, Ontorion.CNL.EN.endict lex, IOwlNameingConvention owlNameingConvention = null)
        {
            this.owlManager = owlManager;
            this._ontology = defaultOntology;
            setNameProvider(namespaceManager, lex);
            if(owlNameingConvention != null)
                setNameingConvention(owlNameingConvention);
        }

        private void setNameProvider(PrefixOWLOntologyFormat namespaceManager, Ontorion.CNL.EN.endict lex)
        {
            var map = namespaceManager.getPrefixName2PrefixMap();
            var keys = map.keySet().iterator();
            while (keys.hasNext())
            {
                var k = keys.next().ToString();
                var v = map.get(k).ToString();
                k = k.Split(':')[0];
                if (string.IsNullOrEmpty(k))
                {
                    defaultNs = v;
                    continue;
                }
                if (!Pfx2ns.ContainsKey(k.Replace(".","$")))
                {
                    Pfx2ns.Add(k.Replace(".","$"), v);
                    if (!Ns2pfx.ContainsKey(v))
                        Ns2pfx.Add(v, k.Replace(".","$"));
                }
            }
           
            this.lex = lex;
        }

        IOwlNameingConvention owlNameingConventionCC = null;
        IOwlNameingConvention owlNameingConvention = new OwlNameingConventionCamelCase();
        private void setNameingConvention(IOwlNameingConvention owlNameingConvention)
        {
            this.owlNameingConvention = owlNameingConvention;
            if (!(owlNameingConvention is OwlNameingConventionCamelCase))
                this.owlNameingConventionCC = new OwlNameingConventionCamelCase();
        }
        
        private object ret = null;

        public Ontorion.CNL.DL.Paragraph Convert(OWLOntology ontology)
        {
            declaredEntities = new SortedDictionary<string, CNL.DL.Statement>();
            iriKindCache = new Dictionary<string, EntityKind>();
            var defaultOnt = _ontology;
            _ontology = ontology;
            ret = null;
            ontology.accept(this);
            _ontology = defaultOnt;
            return ret as Ontorion.CNL.DL.Paragraph;
        }

        public CNL.DL.Statement Convert(OWLAxiom axiom)
        {
            declaredEntities = new SortedDictionary<string, CNL.DL.Statement>();
            iriKindCache = new Dictionary<string, EntityKind>();
            ret = null;
            axiom.accept(this);
            // TODO ALESSANDRO here we should return a paragraph if some additional annotaton axioms where found!
            return ret as CNL.DL.Statement;
        }

        public CNL.DL.Node Convert(OWLClassExpression axiom)
        {
            declaredEntities = new SortedDictionary<string, CNL.DL.Statement>();
            iriKindCache = new Dictionary<string, EntityKind>();
            ret = null;
            axiom.accept(this);
            return ret as CNL.DL.Node;
        }

        public void visit(OWLOntology ontology)
        {
            Ontorion.CNL.DL.Paragraph par = new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() };
            var axioms = ontology.getAxioms().iterator();
            while (axioms.hasNext())
            {
                var ax = axioms.next() as OWLAxiom;
                ret = null;
                ax.accept(this);
                var toAdd = ret as CNL.DL.Statement;
                if (toAdd != null)
                {
                    par.Statements.Add(toAdd);
                }
            }
            List<CNL.DL.Statement> decls = new List<CNL.DL.Statement>();
            foreach (var ax in declaredEntities)
            {
                if (!usedEntities.Contains(ax.Key))
                    decls.Add(ax.Value);
            }
            par.Statements.InsertRange(0, decls);
            if (_annotMan.GetAnnotationSubjects().Count > 0)
            {
                foreach(var ann in _annotMan.getDLAnnotationAxioms())
                    par.Statements.AddRange(ann.Value);
            }
            
            ret = par;
        }

        private static NamespaceUtil namespaceUtil = new NamespaceUtil();

        // used in TODL so arg will be a namespace (without <>) and we return a namespace with <>
        string ns2pfx(string arg)
        {
            if (arg == null)
                return "<"+defaultNs+">";

            if (!arg.EndsWith("/") && !arg.EndsWith("#") && !arg.Contains("#"))
                arg +="#";

            if (Ns2pfx.ContainsKey(arg))
                return Ns2pfx[arg];
            else if (namespaceUtil.getNamespace2PrefixMap().containsKey(arg))
                return namespaceUtil.getNamespace2PrefixMap().get(arg).ToString();
            else if (!arg.StartsWith("<") && !arg.EndsWith(">"))
                return "<" + arg + ">";
            else
                return arg;
        }

        // user in FROMDL so arg will be a DLprefix (with or without <>) and the return value should be without <>
        string pfx2ns(string arg)
        {
            if (arg == null)
                return defaultNs;

            if (!Pfx2ns.ContainsKey(arg))
            {
                if (arg.StartsWith("<") && arg.EndsWith(">"))
                {
                    var argg = arg.Substring(1, arg.Length - 2);
                    if (!argg.EndsWith("/") && !argg.EndsWith("#") && !argg.Contains("#"))
                        argg += "#";
                    return argg;
                }
                else
                    return "http://unknown.prefix/" + arg + "#";
            }
            else
                return Pfx2ns[arg];
        }

        public Dictionary<Tuple<EntityKind, string>, string> UriMappings = new Dictionary<Tuple<EntityKind, string>, string>();
        public Dictionary<string, string> InvUriMappings = new Dictionary<string, string>();

        string ensureQuotation(IOwlNameingConvention conv, CNL.DL.DlName dl_id, EntityKind makeFor)
        {
            var cc_owl = conv.FromDL(dl_id, lex, pfx2ns, makeFor);
            var p_dl = dl_id.Split();
            var dl_cc_id = conv.ToDL(cc_owl, lex, ns2pfx, makeFor);
            var p_cc_dl = dl_cc_id.Split();
            if (p_cc_dl.name != p_dl.name)
            {
                var p = dl_id.Split();
                p.quoted = true;
                return p.Combine().id;
            }
            else
                return dl_cc_id.id;
        }

        public string renderEntity(OWLEntity entity, EntityKind makeFor, bool useCamelCase = false)
        {
            return renderEntity(new OwlName() { iri = entity.getIRI() }, makeFor, useCamelCase);
        }

        public string renderEntity(OwlName owlName, EntityKind makeFor, bool useCamelCase = false)
        {
            try
            {
                string convId;
                if (!useCamelCase && owlNameingConventionCC != null)
                {
                    if (UriMappings.ContainsKey(Tuple.Create(makeFor,owlName.iri.toString())))
                        return UriMappings[Tuple.Create(makeFor,owlName.iri.toString())];
                    var id = ensureQuotation(owlNameingConventionCC, owlNameingConvention.ToDL(owlName, lex, ns2pfx, makeFor), makeFor);
                    var ccid = ensureQuotation(owlNameingConventionCC, owlNameingConventionCC.ToDL(owlName, lex, ns2pfx, makeFor), makeFor);
                    if (ccid != id)
                    {
                        if (InvUriMappings.ContainsKey(id))
                            return id;
                        UriMappings[Tuple.Create(makeFor,owlName.iri.toString())] = id;
                        InvUriMappings[id] = owlName.iri.toString();
                    }
                    convId = id;
                    return id;
                }
                else
                {
                    return ensureQuotation(owlNameingConventionCC ?? owlNameingConvention, (owlNameingConventionCC ?? owlNameingConvention).ToDL(owlName, lex, ns2pfx, makeFor), makeFor);
                }

            }
            catch
            {
            }
            return null;
        }

        public string renderEntity(string uri, EntityKind makeFor, bool useCamelCase=false)
        {
            return renderEntity(new OwlName() { iri = IRI.create(uri) }, makeFor,useCamelCase);
        }

        SortedDictionary<string, Ontorion.CNL.DL.Statement> declaredEntities = new SortedDictionary<string, CNL.DL.Statement>();
        bool useEntityDeclMode = false;
        HashSet<string> usedEntities = new HashSet<string>();

        public void visit(OWLDeclarationAxiom axiom)
        {
            ret = null;
            var ent = axiom.getEntity();
            if (ent is OWLClass)
            {
                Ontorion.CNL.DL.Subsumption stmt = new Ontorion.CNL.DL.Subsumption(null);

                useEntityDeclMode = true;
                ent.accept(this);
                useEntityDeclMode = false;
                if (ret is CNL.DL.Top || ret is CNL.DL.Bottom)
                    return;

                var atom = ret as CNL.DL.Atomic;

                stmt.C = atom;
                stmt.D = new CNL.DL.Top(null);
                if(!declaredEntities.ContainsKey("C:" + atom.id))
                    declaredEntities.Add("C:" + atom.id, stmt);

                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
            }
            else if (ent is OWLIndividual)
            {
                Ontorion.CNL.DL.InstanceOf stmt = new Ontorion.CNL.DL.InstanceOf(null);

                useEntityDeclMode = true;
                ent.accept(this);
                useEntityDeclMode = false;

                var atom = ret as CNL.DL.NamedInstance;
                if (atom == null)
                    return;

                stmt.I = atom;
                stmt.C = new CNL.DL.Top(null);
                if(!declaredEntities.ContainsKey("I:" + atom.name))
                    declaredEntities.Add("I:" + atom.name, stmt);

                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
            }
            else if (ent is OWLObjectProperty)
            {
                Ontorion.CNL.DL.RoleInclusion stmt = new Ontorion.CNL.DL.RoleInclusion(null);

                useEntityDeclMode = true;
                ent.accept(this);
                useEntityDeclMode = false;
                if (ret is CNL.DL.Top || ret is CNL.DL.Bottom)
                    return;

                var atom = ret as CNL.DL.Atomic;

                stmt.C = atom;
                stmt.D = new CNL.DL.Top(null);
                if (!declaredEntities.ContainsKey("R:" + atom.id))
                    declaredEntities.Add("R:" + atom.id, stmt);

                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
            }
            else if (ent is OWLDataProperty)
            {
                Ontorion.CNL.DL.DataRoleInclusion stmt = new Ontorion.CNL.DL.DataRoleInclusion(null);

                useEntityDeclMode = true;
                ent.accept(this);
                useEntityDeclMode = false;
                if (ret is CNL.DL.Top || ret is CNL.DL.Bottom)
                    return;

                var atom = ret as CNL.DL.Atomic;

                stmt.C = atom;
                stmt.D = new CNL.DL.Top(null);
                if(!declaredEntities.ContainsKey("D:" + atom.id))
                    declaredEntities.Add("D:" + atom.id, stmt);

                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
            }
            else if (ent is OWLDatatype)
            {
                Ontorion.CNL.DL.DataTypeDefinition stmt = new CNL.DL.DataTypeDefinition(null);
                useEntityDeclMode = true;
                ent.accept(this);
                useEntityDeclMode = false;
                if (!(ret is CNL.DL.DTBound))
                    return;

                var atom = (ret as CNL.DL.DTBound);
                stmt.name = atom.name;
                stmt.B = new CNL.DL.TopBound(null);

                if (!declaredEntities.ContainsKey("T:" + atom.name))
                    declaredEntities.Add("T:" + atom.name, stmt);

                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
            }
            //do nothing
        }

        public void visit(OWLSubClassOfAxiom axiom)
        {
            if (axiom.getSubClass() is OWLObjectOneOf)
            {
                if (((OWLObjectOneOf)axiom.getSubClass()).getIndividuals().size() == 1)
                {
                    OWLIndividual ind = (OWLIndividual)((OWLObjectOneOf)axiom.getSubClass()).getIndividuals().toArray()[0];
                    axiom.getSuperClass().accept(this);
                    var stmt = new CNL.DL.InstanceOf(null, ret as CNL.DL.Node, CNL.DL.Statement.Modality.IS);
                    ind.accept(this);
                    stmt.I = ret as CNL.DL.Instance;
                    if (axiom.isAnnotated())
                        appendAnnotationsToManager(axiom, stmt);
                    ret = stmt;

                    return;
                }
            }

            {
                Ontorion.CNL.DL.Subsumption stmt = new Ontorion.CNL.DL.Subsumption(null);
                axiom.getSubClass().accept(this); stmt.C = ret as CNL.DL.Node;
                axiom.getSuperClass().accept(this); stmt.D = ret as CNL.DL.Node;
                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
                ret = stmt;
            }
        }

        public void visit(OWLNegativeObjectPropertyAssertionAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }


        public void visit(OWLReflexiveObjectPropertyAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }


        public void visit(OWLDisjointClassesAxiom axiom)
        {
            var classes = axiom.getClassExpressions().iterator();

            Ontorion.CNL.DL.Disjoint stmt = new CNL.DL.Disjoint(null) { Disjoints = new List<CNL.DL.Node>() };
            while (classes.hasNext())
            {
                var Do = classes.next() as OWLClassExpression;
                Do.accept(this);
                stmt.Disjoints.Add(ret as CNL.DL.Node);
            }
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLDataPropertyDomainAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLObjectPropertyDomainAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }


        public void visit(OWLNegativeDataPropertyAssertionAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLDifferentIndividualsAxiom axiom)
        {
            var expr = new CNL.DL.DifferentInstances(null) { Instances = new List<CNL.DL.Instance>() };
            var inds = axiom.getIndividuals().iterator();
            while (inds.hasNext())
            {
                var ind = inds.next();
                Assert(ind is OWLIndividual);
                (ind as OWLIndividual).accept((OWLIndividualVisitor)this);
                expr.Instances.Add(ret as CNL.DL.Instance);
            }

            if (expr.Instances.Count < 2)
                ret = null;
            else
            {
                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, expr);
                ret = expr;
            }
        }

        public void visit(OWLObjectPropertyRangeAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLObjectPropertyAssertionAxiom axiom)
        {
            Ontorion.CNL.DL.RelatedInstances stmt = new CNL.DL.RelatedInstances(null);
            var i = axiom.getSubject();
            var j = axiom.getObject();
            var prop = axiom.getProperty();
            (i as OWLIndividual).accept((OWLIndividualVisitor)this);
            stmt.I = ret as CNL.DL.Instance;
            (j as OWLIndividual).accept((OWLIndividualVisitor)this);
            stmt.J = ret as CNL.DL.Instance;
            (prop as OWLPropertyExpression).accept(this);
            stmt.R = ret as CNL.DL.Node;
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLFunctionalObjectPropertyAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLSubObjectPropertyOfAxiom axiom)
        {
            Ontorion.CNL.DL.RoleInclusion stmt = new Ontorion.CNL.DL.RoleInclusion(null);
            axiom.getSubProperty().accept(this); stmt.C = ret as CNL.DL.Node;
            axiom.getSuperProperty().accept(this); stmt.D = ret as CNL.DL.Node;
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLDataPropertyRangeAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLFunctionalDataPropertyAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLEquivalentDataPropertiesAxiom axiom)
        {
            var roles = axiom.getProperties().iterator();

            Ontorion.CNL.DL.DataRoleEquivalence stmt = new CNL.DL.DataRoleEquivalence(null) { Equivalents = new List<CNL.DL.Node>() };
            while (roles.hasNext())
            {
                var Do = roles.next() as OWLDataProperty;
                Do.accept((OWLPropertyExpressionVisitor)this);
                stmt.Equivalents.Add(ret as CNL.DL.Node);
            }
            if (stmt.Equivalents.Count < 2)
                ret = null;
            else
            {
                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
                ret = stmt;
            }
        }

        public void visit(OWLClassAssertionAxiom axiom)
        {
            Ontorion.CNL.DL.InstanceOf stmt = new CNL.DL.InstanceOf(null);
            axiom.getClassExpression().accept(this); stmt.C = ret as CNL.DL.Node;
            axiom.getIndividual().accept(this); stmt.I = ret as CNL.DL.Instance;

            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLEquivalentClassesAxiom axiom)
        {

            var classes = axiom.getClassExpressions().iterator();

            Ontorion.CNL.DL.Equivalence stmt = new CNL.DL.Equivalence(null) { Equivalents = new List<CNL.DL.Node>() };
            while (classes.hasNext())
            {
                var Do = classes.next() as OWLClassExpression;
                Do.accept(this);
                stmt.Equivalents.Add(ret as CNL.DL.Node);
            }
            if (stmt.Equivalents.Count < 2)
                ret = null;
            else
            {
                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
                ret = stmt;
            }
        }

        public void visit(OWLEquivalentObjectPropertiesAxiom axiom)
        {
            var roles = axiom.getProperties().iterator();

            Ontorion.CNL.DL.RoleEquivalence stmt = new CNL.DL.RoleEquivalence(null) { Equivalents = new List<CNL.DL.Node>() };
            while (roles.hasNext())
            {
                var Do = roles.next();
                (Do as OWLObjectPropertyExpression).accept((OWLPropertyExpressionVisitor)this);
                stmt.Equivalents.Add(ret as CNL.DL.Node);
            }
            if (stmt.Equivalents.Count < 2)
                ret = null;
            else
            {
                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, stmt);
                ret = stmt;
            }
        }

        public void visit(OWLDataPropertyAssertionAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLTransitiveObjectPropertyAxiom axiom)
        {
            axiom.getProperty().accept(this);
            var role = ret as CNL.DL.Node;
            var expr = new CNL.DL.ComplexRoleInclusion(null) { RoleChain = new List<CNL.DL.Node>(), R = role };
            expr.RoleChain.Add(role);
            expr.RoleChain.Add(role);
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, expr);
            ret = expr;
        }

        public void visit(OWLIrreflexiveObjectPropertyAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLInverseFunctionalObjectPropertyAxiom axiom)
        {
            axiom.asOWLSubClassOfAxiom().accept(this);
        }

        public void visit(OWLInverseObjectPropertiesAxiom axiom)
        {
            axiom.getFirstProperty().accept(this);
            var firstRole = ret as CNL.DL.Node;
            axiom.getSecondProperty().accept(this);
            var secondRole = ret as CNL.DL.Node;
            var expr = new CNL.DL.RoleEquivalence(null) { Equivalents = new List<CNL.DL.Node>() };
            expr.Equivalents.Add(firstRole);
            expr.Equivalents.Add(new CNL.DL.RoleInversion(null, secondRole));
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, expr);
            ret = expr;
        }

        public void visit(OWLAsymmetricObjectPropertyAxiom axiom)
        {
            axiom.getProperty().accept(this);
            var role = ret as CNL.DL.Node;
            var expr = new CNL.DL.RoleDisjoint(null) { Disjoints = new List<CNL.DL.Node>() };
            expr.Disjoints.Add(role);
            expr.Disjoints.Add(new CNL.DL.RoleInversion(null, role));
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, expr);
            ret = expr;
        }

        public void visit(OWLSymmetricObjectPropertyAxiom axiom)
        {
            axiom.getProperty().accept(this);
            var role = ret as CNL.DL.Node;
            var expr = new CNL.DL.RoleEquivalence(null) { Equivalents = new List<CNL.DL.Node>() };
            expr.Equivalents.Add(role);
            expr.Equivalents.Add(new CNL.DL.RoleInversion(null, role));
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, expr);
            ret = expr;
        }

        public void visit(OWLClass desc)
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
                string id = renderEntity(desc, EntityKind.Concept);
                // annotations start
                appendAnnotationsToManager(desc);
                // annotations end
                ret = new CNL.DL.Atomic(null) { id = id };
                if (!useEntityDeclMode)
                    usedEntities.Add("C:" + id);
            }
        }

        public void visit(OWLObjectIntersectionOf desc)
        {
            var expr = new CNL.DL.ConceptAnd(null) { Exprs = new List<CNL.DL.Node>() };

            var classes = desc.getOperands().iterator();
            while (classes.hasNext())
            {
                var Do = classes.next() as OWLClassExpression;
                Do.accept(this); var D = ret as CNL.DL.Node;
                expr.Exprs.Add(D);
            }

            ret = expr;
        }

        public void visit(OWLObjectUnionOf desc)
        {
            var expr = new CNL.DL.ConceptOr(null) { Exprs = new List<CNL.DL.Node>() };

            var classes = desc.getOperands().iterator();
            while (classes.hasNext())
            {
                var Do = classes.next() as OWLClassExpression;
                Do.accept(this); var D = ret as CNL.DL.Node;
                expr.Exprs.Add(D);
            }

            ret = expr;
        }

        public void visit(OWLObjectComplementOf desc)
        {
            var expr = new Ontorion.CNL.DL.ConceptNot(null);

            desc.getOperand().accept(this); expr.C = ret as CNL.DL.Node;

            ret = expr;
        }

        public void visit(OWLObjectSomeValuesFrom desc)
        {
            var expr = new CNL.DL.SomeRestriction(null);
            desc.getProperty().accept(this); expr.R = ret as CNL.DL.Node;
            desc.getFiller().accept(this); expr.C = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLObjectAllValuesFrom desc)
        {
            var expr = new CNL.DL.OnlyRestriction(null);
            desc.getProperty().accept(this); expr.R = ret as CNL.DL.Node;
            desc.getFiller().accept(this); expr.C = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLObjectHasValue desc)
        {
            var expr = new CNL.DL.SomeRestriction(null);
            desc.getProperty().accept(this); expr.R = ret as CNL.DL.Node;
            var instSet = new CNL.DL.InstanceSet(null) { Instances = new List<CNL.DL.Instance>() };
            var inds = desc.getIndividualsInSignature().iterator();
            while (inds.hasNext())
            {
                var ind = inds.next();
                Assert(ind is OWLIndividual);
                (ind as OWLIndividual).accept((OWLIndividualVisitor)this);
                instSet.Instances.Add(ret as CNL.DL.Instance);
            }
            expr.C = instSet;
            ret = expr;
        }

        public void visit(OWLObjectMinCardinality desc)
        {
            var expr = new CNL.DL.NumberRestriction(null);
            expr.Kind = "≥";
            expr.N = desc.getCardinality().ToString();
            desc.getFiller().accept(this);
            expr.C = ret as CNL.DL.Node;
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLObjectExactCardinality desc)
        {
            var expr = new CNL.DL.NumberRestriction(null);
            expr.Kind = "=";
            expr.N = desc.getCardinality().ToString();
            desc.getFiller().accept(this);
            expr.C = ret as CNL.DL.Node;
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLObjectMaxCardinality desc)
        {
            var expr = new CNL.DL.NumberRestriction(null);
            expr.Kind = "≤";
            expr.N = desc.getCardinality().ToString();
            desc.getFiller().accept(this);
            expr.C = ret as CNL.DL.Node;
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLObjectHasSelf desc)
        {
            var expr = new CNL.DL.SelfReference(null);
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLObjectOneOf desc)
        {
            var expr = new CNL.DL.InstanceSet(null) { Instances = new List<CNL.DL.Instance>() };
            var insts = desc.getIndividuals().iterator();
            while (insts.hasNext())
            {
                var inst = insts.next();
                Assert(inst is OWLIndividual);
                (inst as OWLIndividual).accept((OWLIndividualVisitor)this);
                expr.Instances.Add(ret as CNL.DL.Instance);
            }
            ret = expr;
        }

        public void visit(OWLObjectProperty property)
        {
            if (property.isTopEntity())
                ret = new CNL.DL.Top(null);
            else if (property.isBottomEntity())
                ret = new CNL.DL.Bottom(null);
            else
            {
                // annotations start
                appendAnnotationsToManager(property);
                // annotations end   
                var id = renderEntity(property, EntityKind.Role);
                ret = new CNL.DL.Atomic(null) { id = id};
                if (!useEntityDeclMode)
                    usedEntities.Add("R:" + id);
            }
        }


        public void visit(OWLObjectInverseOf property)
        {
            var expr = new CNL.DL.RoleInversion(null);
            property.getInverse().accept(this); expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLDataProperty property)
        {
            if (property.isTopEntity())
                ret = new CNL.DL.Top(null);
            else if (property.isBottomEntity())
                ret = new CNL.DL.Bottom(null);
            else
            {
                // annotations start
                appendAnnotationsToManager(property);
                // annotations end
                var id = renderEntity(property, EntityKind.DataRole);
                ret = new CNL.DL.Atomic(null) { id = id};
                if (!useEntityDeclMode)
                    usedEntities.Add("D:" + id);
            }
        }

        public bool isUnnamedIndividual(OWLIndividual individual)
        {
            if (individual is OWLNamedIndividual)
            {
                string id = renderEntity((OWLNamedIndividual)individual, EntityKind.Instance);

                if (!useEntityDeclMode)
                    usedEntities.Add("I:" + id);

                return id.StartsWith("[");
            }
            else
                return true;
        }

        public void visit(OWLNamedIndividual individual)
        {
            string id = renderEntity(individual, EntityKind.Instance);
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
            ret = new CNL.DL.NamedInstance(null) { name = id };
        }

        public void visit(OWLHasKeyAxiom axiom)
        {
            Ontorion.CNL.DL.HasKey stmt = new CNL.DL.HasKey(null) { DataRoles = new List<CNL.DL.Node>(), Roles = new List<CNL.DL.Node>() };
            axiom.getClassExpression().accept(this);
            Assert(ret is CNL.DL.Node);
            stmt.C = ret as CNL.DL.Node;

            var roles = axiom.getObjectPropertyExpressions().iterator();
            while (roles.hasNext())
            {
                var Do = roles.next() as OWLObjectPropertyExpression;
                Do.accept(this);
                stmt.Roles.Add(ret as CNL.DL.Node);
            }
            var dataroles = axiom.getDataPropertyExpressions().iterator();
            while (dataroles.hasNext())
            {
                var Do = dataroles.next() as OWLDataPropertyExpression;
                Do.accept(this);
                stmt.DataRoles.Add(ret as CNL.DL.Node);
            }
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLDisjointUnionAxiom axiom)
        {
            Ontorion.CNL.DL.DisjointUnion stmt = new CNL.DL.DisjointUnion(null) { Union = new List<CNL.DL.Node>() };
            axiom.getOWLClass().accept((OWLClassExpressionVisitor) this);
            Assert(ret is CNL.DL.Atomic);
            stmt.name = (ret as CNL.DL.Atomic).id;
            var unions = axiom.getClassExpressions().iterator();
            while (unions.hasNext())
            {
                var Do = unions.next() as OWLClassExpression;
                Do.accept(this);
                stmt.Union.Add(ret as CNL.DL.Node);
            }
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLDisjointDataPropertiesAxiom axiom)
        {
            var roles = axiom.getProperties().iterator();

            Ontorion.CNL.DL.DataRoleDisjoint stmt = new CNL.DL.DataRoleDisjoint(null) { Disjoints = new List<CNL.DL.Node>() };
            while (roles.hasNext())
            {
                var Do = roles.next() as OWLDataProperty;
                Do.accept((OWLPropertyExpressionVisitor)this);
                stmt.Disjoints.Add(ret as CNL.DL.Node);
            }
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLDisjointObjectPropertiesAxiom axiom)
        {
            var roles = axiom.getProperties().iterator();

            Ontorion.CNL.DL.RoleDisjoint stmt = new CNL.DL.RoleDisjoint(null) { Disjoints = new List<CNL.DL.Node>() };
            while (roles.hasNext())
            {
                var Do = roles.next() as OWLObjectPropertyExpression;
                Do.accept((OWLPropertyExpressionVisitor)this);
                stmt.Disjoints.Add(ret as CNL.DL.Node);
            }
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLSubDataPropertyOfAxiom axiom)
        {
            Ontorion.CNL.DL.DataRoleInclusion stmt = new Ontorion.CNL.DL.DataRoleInclusion(null);
            axiom.getSubProperty().accept(this); stmt.C = ret as CNL.DL.Node;
            axiom.getSuperProperty().accept(this); stmt.D = ret as CNL.DL.Node;
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLSubPropertyChainOfAxiom axiom)
        {
            Ontorion.CNL.DL.ComplexRoleInclusion stmt = new Ontorion.CNL.DL.ComplexRoleInclusion(null) { RoleChain = new List<CNL.DL.Node>() };
            var chains = axiom.getPropertyChain().iterator();
            while (chains.hasNext())
            {
                var r = chains.next();
                Assert(r is OWLObjectPropertyExpression);
                (r as OWLObjectPropertyExpression).accept((OWLPropertyExpressionVisitor)this);
                Assert(ret is CNL.DL.Node);
                stmt.RoleChain.Add(ret as CNL.DL.Node);
            }

            axiom.getSuperProperty().accept(this); stmt.R = ret as CNL.DL.Node;
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, stmt);
            ret = stmt;
        }

        public void visit(OWLSameIndividualAxiom axiom)
        {
            var expr = new CNL.DL.SameInstances(null) { Instances = new List<CNL.DL.Instance>() };
            var inds = axiom.getIndividuals().iterator();
            while (inds.hasNext())
            {
                var ind = inds.next();
                Assert(ind is OWLIndividual);
                (ind as OWLIndividual).accept((OWLIndividualVisitor)this);
                expr.Instances.Add(ret as CNL.DL.Instance);
            }
            if (expr.Instances.Count < 2)
                ret = null;
            else
            {
                if (axiom.isAnnotated())
                    appendAnnotationsToManager(axiom, expr);
                ret = expr;
            }
        }

        public void visit(OWLDataSomeValuesFrom desc)
        {
            var expr = new CNL.DL.SomeValueRestriction(null);
            desc.getProperty().accept(this); expr.R = ret as CNL.DL.Node;
            desc.getFiller().accept(this); expr.B = ret as CNL.DL.AbstractBound;
            ret = expr;
        }

        public void visit(OWLDataAllValuesFrom desc)
        {
            var expr = new CNL.DL.OnlyValueRestriction(null);
            desc.getProperty().accept(this); expr.R = ret as CNL.DL.Node;
            desc.getFiller().accept(this); expr.B = ret as CNL.DL.AbstractBound;
            ret = expr;
        }

        public void visit(OWLDataHasValue desc)
        {
            var expr = new CNL.DL.SomeValueRestriction(null);
            desc.getProperty().accept(this); expr.R = ret as CNL.DL.Node;
            desc.getValue().accept(this); expr.B = new CNL.DL.BoundVal(null, "=", ret as CNL.DL.Value);
            ret = expr;
        }

        public void visit(OWLDataMinCardinality desc)
        {
            var expr = new CNL.DL.NumberValueRestriction(null);
            expr.Kind = "≥";
            expr.N = desc.getCardinality().ToString();
            desc.getFiller().accept(this);
            expr.B = ret as CNL.DL.AbstractBound;
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLDataExactCardinality desc)
        {
            var expr = new CNL.DL.NumberValueRestriction(null);
            expr.Kind = "=";
            expr.N = desc.getCardinality().ToString();
            desc.getFiller().accept(this);
            expr.B = ret as CNL.DL.AbstractBound;
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLDataMaxCardinality desc)
        {
            var expr = new CNL.DL.NumberValueRestriction(null);
            expr.Kind = "≤";
            expr.N = desc.getCardinality().ToString();
            desc.getFiller().accept(this);
            expr.B = ret as CNL.DL.AbstractBound;
            desc.getProperty().accept(this);
            expr.R = ret as CNL.DL.Node;
            ret = expr;
        }

        public void visit(OWLDatatype node)
        {
            if (node.isTopDatatype())
                ret = new CNL.DL.TopBound(null);
            else if (node.isDouble() || node.isFloat())
                ret = new CNL.DL.TotalBound(null) { V = new CNL.DL.Float(null, "3.14") };
            else if (node.isBuiltIn())
            {
                if (node.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || node.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP)
                    ret = new CNL.DL.TotalBound(null) { V = new CNL.DL.DateTimeVal(null, "2012-01-03") };
                else if (node.getBuiltInDatatype().isNumeric())
                    ret = new CNL.DL.TotalBound(null) { V = new CNL.DL.Number(null, "1") };
                else if (node.isBoolean())
                    ret = new CNL.DL.TotalBound(null) { V = new CNL.DL.Bool(null, "[1]") };
                else
                    ret = new CNL.DL.TotalBound(null) { V = new CNL.DL.String(null, "\'...\'") };
            }
            else
            {
                if (    node.getIRI().toString() == "http://www.w3.org/2001/XMLSchema#date"
                   ||   node.getIRI().toString() == "http://www.w3.org/2001/XMLSchema#time")
                {
                    ret = new CNL.DL.TotalBound(null) { V = new CNL.DL.DateTimeVal(null, "2012-01-03") };
                }
                else
                {
                    var owlName = new OwlName() { iri = node.getIRI() };
                    var id = owlNameingConvention.ToDL(owlName, lex, ns2pfx, EntityKind.Concept).id;
                    ret = new CNL.DL.DTBound(null) { name = id };
                }
            }
        }

        public void visit(OWLDataComplementOf node)
        {
            ret = null;
            node.getDataRange().accept(this);
            ret = new CNL.DL.BoundNot(null) { B = ret as CNL.DL.AbstractBound };
        }

        public void visit(OWLDataIntersectionOf node)
        {
            var Bounds = new List<CNL.DL.AbstractBound>();
            ret = null;
            var inds = node.getOperands().iterator();
            while (inds.hasNext())
            {
                var ind = inds.next();
                Assert(ind is OWLDataRange);
                (ind as OWLDataRange).accept(this);
                Bounds.Add(ret as CNL.DL.AbstractBound);
            }
            ret = new CNL.DL.BoundAnd(null) { List = Bounds };
        }

        public void visit(OWLDataUnionOf node)
        {
            var Bounds = new List<CNL.DL.AbstractBound>();
            ret = null;
            var inds = node.getOperands().iterator();
            while (inds.hasNext())
            {
                var ind = inds.next();
                Assert(ind is OWLDataRange);
                (ind as OWLDataRange).accept(this);
                Bounds.Add(ret as CNL.DL.AbstractBound);
            }
            ret = new CNL.DL.BoundOr(null) { List = Bounds };
        }

        public void visit(OWLDataOneOf node)
        {
            var set = new Ontorion.CNL.DL.ValueSet(null) { Values = new List<CNL.DL.Value>() };
            var vals = node.getValues().iterator();
            while (vals.hasNext())
            {
                var val = vals.next() as OWLObject;
                val.accept(this);
                set.Values.Add(ret as Ontorion.CNL.DL.Value);
            }
            ret = set;
        }

        public void visit(OWLDatatypeRestriction node)
        {
            CNL.DL.FacetList FL = new CNL.DL.FacetList(null) { List = new List<CNL.DL.Facet>() };
            var set = node.getFacetRestrictions();
            var fr = set.iterator();
            while (fr.hasNext())
            {
                var val = fr.next() as OWLObject;
                ret = null;               
                val.accept(this);
                FL.List.Add(ret as CNL.DL.Facet);
            }
            ret = new CNL.DL.BoundFacets(null) { FL = FL };
        }

        static CNL.DL.Value getNumberOrFloatValFromLiteral(OWLLiteral owli)
        {
            int IntNumber;
            double DoubleNumber;
            if (owli.isInteger())
                return new CNL.DL.Number(null, owli.getLiteral());
            else if (owli.isFloat() || owli.isDouble())
                return new CNL.DL.Float(null, owli.parseDouble().ToString());
            else if (!owli.ToString().Contains("^^xsd:decimal") && Int32.TryParse(owli.getLiteral().Replace("\"", ""), out IntNumber))
                return new CNL.DL.Number(null, owli.getLiteral());
            else if (!owli.ToString().Contains("^^xsd:decimal") && Double.TryParse(owli.getLiteral().Replace("\"", ""), out DoubleNumber))
                return new CNL.DL.Number(null, owli.getLiteral());
            else if (owli.ToString().Contains("^^xsd:decimal"))
                throw new Exception("Cannot import xsd:decimal as this datatype has not been implemented.");
            else
                Assert(false);
            return null;
        }

        static CNL.DL.Value getNumberLiteral(OWLLiteral owli)
        {
            if (owli.isInteger())
                return new CNL.DL.Number(null, owli.getLiteral());
            else
                Assert(false);
            return null;
        }

        public void visit(OWLFacetRestriction node)
        {
            OWLLiteral owli = node.getFacetValue();

            var expr = new CNL.DL.Facet(null);
            var f = node.getFacet();
            var a1 = f.ordinal();
            var a2 = f.name().ToUpper();
            switch (a2)
            {
                case "MIN_INCLUSIVE": expr.Kind = "≥";
                    expr.V = getNumberOrFloatValFromLiteral(owli);
                    break;
                case "MAX_INCLUSIVE": expr.Kind = "≤";
                    expr.V = getNumberOrFloatValFromLiteral(owli);
                    break;
                case "MIN_EXCLUSIVE": expr.Kind = ">";
                    expr.V = getNumberOrFloatValFromLiteral(owli);
                    break;
                case "MAX_EXCLUSIVE": expr.Kind = "<";
                    expr.V = getNumberOrFloatValFromLiteral(owli);
                    break;
                case "PATTERN": expr.Kind = "#";
                    expr.V = new CNL.DL.String(null, "\'" + owli.getLiteral().Replace("\'", "\'\'") + "\'");
                    break;
                case "LENGTH": expr.Kind = "<->";
                    expr.V = getNumberLiteral(owli);
                    break;
                case "MIN_LENGTH": expr.Kind = "<-> ≥";
                    expr.V = getNumberLiteral(owli);
                    break;
                case "MAX_LENGTH": expr.Kind = "<-> ≤";
                    expr.V = getNumberLiteral(owli);
                    break;
                default:
                    throw new NotImplementedException("Facet " + a2 + " is not supported currently by FE");
            }

            ret = expr;
        }

        public void visit(OWLDatatypeDefinitionAxiom axiom)
        {
            var expr = new CNL.DL.DataTypeDefinition(null);
            
            var owlName = new OwlName() { iri = axiom.getDatatype().getIRI() };
            expr.name=owlNameingConvention.ToDL(owlName, lex, ns2pfx, EntityKind.Concept).id;
            
            if (!useEntityDeclMode)
                usedEntities.Add("T:" + expr.name);
            
            axiom.getDataRange().accept(this);
            expr.B = ret as CNL.DL.AbstractBound;
            if (axiom.isAnnotated())
                appendAnnotationsToManager(axiom, expr);
            ret = expr;
        }

        private void appendAnnotationsToManager(OWLClass desc)
        {
            if (_ontology != null)
            {
                var it = desc.getAnnotations(_ontology).iterator();
                var owlName = new OwlName() { iri = desc.getIRI() };
                appendAnnotationsToManager(owlNameingConvention.ToDL(owlName, lex, ns2pfx, EntityKind.Concept).id,EntityKind.Concept, it);
                if(!iriKindCache.ContainsKey(desc.getIRI().toString()))
                    iriKindCache.Add(desc.getIRI().toString(), EntityKind.Concept);
            }
        }

        private void appendAnnotationsToManager(OWLProperty prop)
        {
            if (_ontology != null)
            {
                var it = prop.getAnnotations(_ontology).iterator();
                var owlName = new OwlName() { iri = prop.getIRI() };
                EntityKind kind = EntityKind.Role;
                if(prop is OWLDataProperty)
                    kind = EntityKind.DataRole;
                appendAnnotationsToManager(owlNameingConvention.ToDL(owlName, lex, ns2pfx, kind).id,kind,it);
                if (!iriKindCache.ContainsKey(prop.getIRI().toString()))
                    iriKindCache.Add(prop.getIRI().toString(), kind);
            }
        }

        private void appendAnnotationsToManager(OWLAxiom axx, CNL.DL.Statement stmt)
        {
            if (_ontology != null)
            {
                var it = axx.getAnnotations().iterator();
                var ser = new Ontorion.CNL.DL.Serializer(false);
                var serializedStmt = ser.Serialize(new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() { stmt } });
                // we are here removing the quotes and other things that where added by the DL serializer. This is a bad practice as someone external to the serializer 
                // knows about its internal functioning but for the moment it should work....
                serializedStmt = serializedStmt.Replace("\r\n", "");
                appendAnnotationsToManager(serializedStmt, EntityKind.Statement, it);
            }
        }

        private Ontorion.CNL.AnnotationManager _annotMan = new Ontorion.CNL.AnnotationManager();

        private void appendAnnotationsToManager(string subj,EntityKind kind, java.util.Iterator it)
        {
            if (_ontology != null)
            {
                while (it.hasNext())
                {
                    var annot = (OWLAnnotation)it.next();
                    _annotMan.appendAnnotations(visitWithReturn(subj,kind,annot));
                }
            }
        }

        private CNL.DL.DLAnnotationAxiom visitWithReturn(string subj, EntityKind kind, OWLAnnotation annot)
        {
            var owlName = new OwlName() { iri = annot.getProperty().getIRI() };

            string val="";
            if (annot.getValue() is IRI)
            {
                val = annot.getValue().ToString();
            }
            else if (annot.getValue() is OWLAnonymousIndividual)
            {
                val = annot.getValue().ToString();
            }
            else
            {
                annot.getValue().accept(this);
                var valt = ret as CNL.DL.Value;
                val = valt.ToString();
                val = System.Net.WebUtility.HtmlDecode(val);
            }

            var lang = "";
            if (annot.getValue() is OWLLiteral)
            {
                var lit = annot.getValue() as OWLLiteral;
                if (lit.hasLang())
                {
                    lang = lit.getLang().ToString();
                }
            }
            return new CNL.DL.DLAnnotationAxiom(null) { subject = subj, subjKind = kind.ToString(), annotName = (new OwlNameingConventionCamelCase()).ToDL(owlName, lex, ns2pfx, EntityKind.Role).id, value = val, language = lang };
        }

        public void visit(OWLAnnotation node)
        {
        }

        private EntityKind getKindFromIri(string iri)
        {
            EntityKind subjKind = EntityKind.Concept;
            if (iriKindCache.ContainsKey(iri))
            {
                subjKind = iriKindCache[iri];
            }
            else
            {
                var ontIt = _ontology.getSignature().iterator();
                while (ontIt.hasNext())
                {
                    var owlEnt = ontIt.next() as OWLEntity;
                    if (owlEnt.getIRI().toString() == iri)
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
                iriKindCache.Add(iri, subjKind);
            }

            return subjKind;
        }

        // cache of IRI,EntityKind --> useful when we need to understand what kind the subject of the annotation is.
        private Dictionary<string, EntityKind> iriKindCache = new Dictionary<string, EntityKind>();
        public void visit(OWLAnnotationAssertionAxiom axiom)
        {
            if (_ontology != null)
            {
                var ir = axiom.getSubject() as IRI;
                if (ir != null)
                {
                    var owlName = new OwlName() { iri = ir };
                    EntityKind subjKind = getKindFromIri(ir.toString());
                    OWLAnnotation annot = axiom.getAnnotation();
                    var dlSent = visitWithReturn(renderEntity(owlName, subjKind), subjKind, annot);
                    if (axiom.isAnnotated())
                        appendAnnotationsToManager(axiom,dlSent);
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

        public void visit(OWLAnnotationPropertyDomainAxiom axiom)
        {
            //var annotForAx = getAnnotationsForAxiom(axiom);
            // this is a domain axiom for the annotation property
            // the domain can be either IRI either Literal
        }

        public void visit(OWLAnnotationPropertyRangeAxiom axiom)
        {
            //var annotForAx = getAnnotationsForAxiom(axiom);
            // this is a range property axiom for the annotation property
            // the range can be IRI or Literal
        }

        public void visit(OWLSubAnnotationPropertyOfAxiom axiom)
        {
            //var annotForAx = getAnnotationsForAxiom(axiom);
            // this axiom states the relation between two annotation properties.
        }

        public void visit(OWLAnnotationProperty property)
        {
            // this is a generic class that has all the information about the annotation property (axioms, subjects,...)
            // the problem is that in the owlapi to get the annotations axioms you need the whole ontology!
            //property.getAnnotationAssertionAxioms(Ontology???)
        }

        public void visit(OWLAnonymousIndividual individual)
        {
            ret = new CNL.DL.UnnamedInstance(null, false, new CNL.DL.Top(null));
        }

        public void visit(OWLLiteral node)
        {
            var dt = node.getDatatype();
            if (dt.isTopDatatype())
                ret = new CNL.DL.String(null, node.getLiteral());
            else if (dt.isBottomEntity())
                ret = new CNL.DL.String(null, node.getLiteral());
            else if (dt.isDouble() || dt.isFloat())
                ret = new CNL.DL.Float(null, node.parseDouble().ToString());
            else if (dt.isInteger())
                ret = new CNL.DL.Number(null, node.getLiteral());
            else if (dt.isString() || node.isRDFPlainLiteral())
                ret = new CNL.DL.String(null, "\'" + node.getLiteral().Replace("\'", "\'\'") + "\'");
            else if (dt.isBoolean())
                ret = new CNL.DL.Bool(null, (node.getLiteral().ToLower().Trim() == "true" || node.getLiteral().ToLower().Trim() == "1") ? "[1]" : "[0]");
            else if (dt.isBuiltIn())
            {
                if (dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP)
                    ret = new CNL.DL.DateTimeVal(null, node.getLiteral());
                else if (dt.getBuiltInDatatype().isNumeric())
                    ret = new CNL.DL.Number(null, node.getLiteral());
                else
                    ret = new CNL.DL.String(null, "\'" + (node.getLiteral() + ":" + dt.getBuiltInDatatype().toString()).Replace("\'", "\'\'") + "\'");
            }
            else if (dt.getIRI().toString() == "http://www.w3.org/2001/XMLSchema#date"
                    || dt.getIRI().toString() == "http://www.w3.org/2001/XMLSchema#time")
            {
                ret = new CNL.DL.DateTimeVal(null, node.getLiteral());
            }
            else
                ret = new CNL.DL.String(null, "\'" + node.getLiteral().Replace("\'", "\'\'") + "\'");
        }

        public void visit(IRI iri)
        {
            Assert(false);
        }

        /////////////// SWRL ///////////////////////////////////////////////////////

        public void visit(SWRLRule rule)
        {
            Ontorion.CNL.DL.SwrlStatement statement = new Ontorion.CNL.DL.SwrlStatement(null);

            var slpi = rule.getBody().iterator();            
            statement.slp = new CNL.DL.SwrlItemList(null)
            {
                list = new List<CNL.DL.SwrlItem>()                 
            }; 
            while(slpi.hasNext())
            {
                var atom = slpi.next() as SWRLAtom;
                ret = null;
                atom.accept(this);
                if (ret != null)
                    statement.slp.list.Add(ret as CNL.DL.SwrlItem);
            }

            var slci = rule.getHead().iterator();    
            statement.slc = new CNL.DL.SwrlItemList(null)
            {
                list = new List<CNL.DL.SwrlItem>()
            };
            while (slci.hasNext())
            {
                var atom = slci.next() as SWRLAtom;
                ret = null;
                atom.accept(this);
                if (ret != null)
                    statement.slc.list.Add(ret as CNL.DL.SwrlItem);
            }

            if (rule.isAnnotated())
                appendAnnotationsToManager(rule, statement);
            ret = statement;           
        }

        public void visit(SWRLClassAtom node)
        {
            var args = node.getAllArguments().iterator();
            Ontorion.CNL.DL.SwrlIObject id_var = null;
            while (args.hasNext())
            {
                Assert(id_var == null);
                var arg1 = args.next() as SWRLIArgument;
                ret = null;
                arg1.accept(this);
                if (ret is Ontorion.CNL.DL.SwrlIObject)
                    id_var = ret as Ontorion.CNL.DL.SwrlIObject;
                else if (ret is string)
                    id_var = new Ontorion.CNL.DL.SwrlIVal(null, ret as string);
                else
                    Assert(false);
            }

            ret = null;
            node.getPredicate().accept(this);
            ret = new Ontorion.CNL.DL.SwrlInstance(null)
            {
                C = ret as CNL.DL.Node,
                I = id_var
            };
        }

        public void visit(SWRLObjectPropertyAtom node)
        {
            List<Ontorion.CNL.DL.SwrlIObject> objs = new List<CNL.DL.SwrlIObject>();
            var args = node.getAllArguments().iterator();
            while (args.hasNext())
            {
                var arg1 = args.next() as SWRLIArgument;
                ret = null;
                arg1.accept(this);
                Assert(ret != null);
                if (ret is Ontorion.CNL.DL.SwrlIObject)
                    objs.Add(ret as Ontorion.CNL.DL.SwrlIObject);
                else if (ret is string)
                    objs.Add(new Ontorion.CNL.DL.SwrlIVal(null, ret as string));
                else
                    Assert(false);
            }
            Assert(objs.Count == 2);
            ret = null;
            node.getPredicate().accept(this);
            var role = ret;
            if (ret is Ontorion.CNL.DL.RoleInversion)
                ret = new CNL.DL.SwrlRole(null) { I = objs[1], J = objs[0], R = ((role as Ontorion.CNL.DL.RoleInversion).R as Ontorion.CNL.DL.Atomic).id };
            else
                ret = new CNL.DL.SwrlRole(null) { I = objs[0], J = objs[1], R = (role as Ontorion.CNL.DL.Atomic).id };
        }

        public void visit(SWRLDataRangeAtom node)
        {
            var args = node.getAllArguments().iterator();
            Ontorion.CNL.DL.SwrlDObject id_var = null;
            while (args.hasNext())
            {
                var arg1 = args.next() as SWRLArgument;
                ret = null;
                arg1.accept(this);
                if(ret is Ontorion.CNL.DL.SwrlBuiltIn)
                    id_var = ret as Ontorion.CNL.DL.SwrlDObject;
                else if(ret is Ontorion.CNL.DL.Value)
                    id_var = new Ontorion.CNL.DL.SwrlDVal(null, ret as Ontorion.CNL.DL.Value);
                else if(ret is Ontorion.CNL.DL.ISwrlVar)
                    id_var = new Ontorion.CNL.DL.SwrlDVar(null, (ret as Ontorion.CNL.DL.ISwrlVar).getVar());

                Assert(id_var != null);
            }

            ret = null;
            node.getPredicate().accept(this);
            CNL.DL.AbstractBound na = ret as CNL.DL.AbstractBound;
            ret = new Ontorion.CNL.DL.SwrlDataRange(null, na, id_var);
        }

        public void visit(SWRLDataPropertyAtom node)
        {
            var args = node.getAllArguments().iterator();
            List<object> objs = new List<object>();
            while (args.hasNext())
            {
                var arg1 = args.next() as SWRLArgument;
                ret = null;
                arg1.accept(this);
                Assert(ret != null);
                if (ret is CNL.DL.Value)
                    ret = new CNL.DL.SwrlDVal(null, ret as CNL.DL.Value);
                else if (ret is string)
                    ret = new CNL.DL.SwrlIVal(null) { I = ret as string };
                objs.Add(ret);
            }
            Assert(objs.Count == 2);
            ret = null;
            node.getPredicate().accept(this);

            var role = ret as CNL.DL.Atomic;
            Assert(role != null);
            if (objs[1] is CNL.DL.SwrlIVar)
                objs[1] = new CNL.DL.SwrlDVar(null, (objs[1] as CNL.DL.SwrlIVar).getVar());
            ret = new CNL.DL.SwrlDataProperty(null) { IO = objs[0] as CNL.DL.SwrlIObject, DO = objs[1] as CNL.DL.SwrlDObject, R = (role as Ontorion.CNL.DL.Atomic).id };
        }

        public void visit(SWRLBuiltInAtom node)
        {
            List<CNL.DL.ISwrlObject> args = new List<CNL.DL.ISwrlObject>();
            int len = node.getArguments().size();
            for (int i = 1; i < len + 1; i++)
            {
                (node.getArguments().get(i % len) as SWRLArgument).accept(this);
                var Aobj = ret;
                CNL.DL.SwrlDObject A = null;
                if (Aobj is CNL.DL.SwrlDObject)
                    A = Aobj as CNL.DL.SwrlDObject;
                else if (Aobj is CNL.DL.SwrlIVar)
                    A = new CNL.DL.SwrlDVar(null, (Aobj as CNL.DL.SwrlIVar).getVar());
                else if (Aobj is CNL.DL.Value)
                    A = new CNL.DL.SwrlDVal(null, Aobj as CNL.DL.Value);
                else
                    throw new NotImplementedException("Cannot interpret first argument for BuiltIn:<" + node.getPredicate().toURI().toString() + ">");

                args.Add(A);
            }

            
            string buildInName = null;

            var pred = node.getPredicate();

            if (pred.compareTo(SWRLBuiltInsVocabulary.EQUAL.getIRI()) == 0)
                buildInName = "=";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.NOT_EQUAL.getIRI()) == 0)
                buildInName = "≠";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.LESS_THAN.getIRI()) == 0)
                buildInName = "<";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.LESS_THAN_OR_EQUAL.getIRI()) == 0)
                buildInName = "≤";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.GREATER_THAN.getIRI()) == 0)
                buildInName = ">";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.GREATER_THAN_OR_EQUAL.getIRI()) == 0)
                buildInName = "≥";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD.getIRI()) == 0)
                buildInName = "plus";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT.getIRI()) == 0)
                buildInName = "minus";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.MULTIPLY.getIRI()) == 0)
                buildInName = "times";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.DIVIDE.getIRI()) == 0)
                buildInName = "divided-by";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.INTEGER_DIVIDE.getIRI()) == 0)
                buildInName = "integer-divided-by";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.MOD.getIRI()) == 0)
                buildInName = "modulo";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.POW.getIRI()) == 0)
                buildInName = "raised-to-the-power-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.UNARY_MINUS.getIRI()) == 0)
                buildInName = "minus";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.UNARY_PLUS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ABS.getIRI()) == 0)
                buildInName = "absolute-value-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.CEILING.getIRI()) == 0)
                buildInName = "ceiling-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.FLOOR.getIRI()) == 0)
                buildInName = "floor-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ROUND.getIRI()) == 0)
                buildInName = "round-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ROUND_HALF_TO_EVEN.getIRI()) == 0)
                buildInName = "rounded-with-the-precision-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SIN.getIRI()) == 0)
                buildInName = "sine-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.COS.getIRI()) == 0)
                buildInName = "cosine-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.TAN.getIRI()) == 0)
                buildInName = "tangent-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.BOOLEAN_NOT.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.STRING_EQUALS_IGNORE_CASE.getIRI()) == 0)
                buildInName = "case-ignored";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.STRING_CONCAT.getIRI()) == 0)
                buildInName = "followed-by";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBSTRING.getIRI()) == 0)
                buildInName = "from";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.STRING_LENGTH.getIRI()) == 0)
                buildInName = "length-of";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.NORMALIZE_SPACE.getIRI()) == 0)
                buildInName = "space-normalized";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.UPPER_CASE.getIRI()) == 0)
                buildInName = "upper-cased";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.LOWER_CASE.getIRI()) == 0)
                buildInName = "lower-cased";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.TRANSLATE.getIRI()) == 0)
                buildInName = "translated";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.CONTAINS.getIRI()) == 0)
                buildInName = "contains-string";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.CONTAINS_IGNORE_CASE.getIRI()) == 0)
                buildInName = "contains-case-ignored-string";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.STARTS_WITH.getIRI()) == 0)
                buildInName = "starts-with-string";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ENDS_WITH.getIRI()) == 0)
                buildInName = "ends-with-string";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBSTRING_BEFORE.getIRI()) == 0)
                buildInName = "before";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBSTRING_AFTER.getIRI()) == 0)
                buildInName = "after";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.MATCHES.getIRI()) == 0)
                buildInName = "matches-string";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.REPLACE.getIRI()) == 0)
                buildInName = "replaced";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.TOKENIZE.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.YEAR_MONTH_DURATION.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.DAY_TIME_DURATION.getIRI()) == 0)
                buildInName = "duration";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.DATE_TIME.getIRI()) == 0)
                buildInName = "datetime";
            else if (pred.compareTo(SWRLBuiltInsVocabulary.DATE.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.TIME.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DATES.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_TIMES.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.RESOLVE_URI.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ANY_URI.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_YEAR_MONTH_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_YEAR_MONTH_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.MULTIPLY_YEAR_MONTH_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.DIVIDE_YEAR_MONTH_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.MULTIPLY_DAY_TIME_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.DIVIDE_DAY_TIME_DURATIONS.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATION_TO_DATE_TIME.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_YEAR_MONTH_DURATION_FROM_DATE_TIME.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATION_FROM_DATE_TIME.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_YEAR_MONTH_DURATION_TO_DATE.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATION_TO_DATE.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_YEAR_MONTH_DURATION_FROM_DATE.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATION_FROM_DATE.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.ADD_DAY_TIME_DURATION_FROM_TIME.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DAY_TIME_DURATION_FROM_TIME.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DATE_TIMES_YIELDING_YEAR_MONTH_DURATION.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(SWRLBuiltInsVocabulary.SUBTRACT_DATE_TIMES_YIELDING_DAY_TIME_DURATION.getIRI()) == 0)
                buildInName = null;
            else if (pred.compareTo(IRI.create("http://ontorion.com/swrlb#" + "soundsLike")) == 0)
                buildInName = "sounds-like-string";
            else if (pred.compareTo(IRI.create("http://ontorion.com/swrlb#" + "alphaRepresentationOf")) == 0)
                buildInName = "alpha-representation-of";
            else if (pred.compareTo(IRI.create("http://ontorion.com/swrlb#" + "annotation")) == 0)
                buildInName = "annotation";

            if (buildInName==null)
                throw new NotImplementedException("BuiltIn:<" + pred.toURI().toString() + "> is currently not supported");

            ret = new CNL.DL.SwrlBuiltIn(null, buildInName, args);
        }

        public void visit(SWRLVariable node)
        {
            var owlName = new OwlName() { iri = node.getIRI() };
            ret = new Ontorion.CNL.DL.SwrlIVar(null, owlNameingConvention.ToDL(owlName, lex, ns2pfx, EntityKind.SWRLVariable).id);
        }

        public void visit(SWRLIndividualArgument node)
        {
            var owlName = new OwlName() { iri = node.getIndividual().asOWLNamedIndividual().getIRI() };
            ret = owlNameingConvention.ToDL(owlName, lex, ns2pfx, EntityKind.Instance).id;
        }

        public void visit(SWRLLiteralArgument node)
        {
            var dt = node.getLiteral().getDatatype();
            if (dt.isTopDatatype())
                ret = new CNL.DL.String(null, node.getLiteral().getLiteral());
            else if (dt.isBottomEntity())
                ret = new CNL.DL.String(null, node.getLiteral().getLiteral());
            else if (dt.isDouble() || dt.isFloat())
                ret = new CNL.DL.Float(null, node.getLiteral().getLiteral());
            else if (dt.isInteger())
                ret = new CNL.DL.Number(null, node.getLiteral().getLiteral());
            else if (dt.isString() || node.getLiteral().isRDFPlainLiteral())
                ret = new CNL.DL.String(null, "\'" + node.getLiteral().getLiteral().Replace("\'", "\''") + "\'");
            else if (dt.isBoolean())
                ret = new CNL.DL.Bool(null, node.getLiteral().getLiteral());
            else if (dt.isBuiltIn())
            {
                if (dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME || dt.getBuiltInDatatype() == OWL2Datatype.XSD_DATE_TIME_STAMP)
                    ret = new CNL.DL.DateTimeVal(null, node.getLiteral().getLiteral().Replace(' ', 'T'));
                else if (dt.getBuiltInDatatype().isNumeric())
                    ret = new CNL.DL.Number(null, node.getLiteral().getLiteral());
                else
                    ret = new CNL.DL.String(null, node.getLiteral().getLiteral());
            }
            else if (dt.getIRI().toString() == "http://www.w3.org/2001/XMLSchema#date"
                    || dt.getIRI().toString() == "http://www.w3.org/2001/XMLSchema#time")
            {
                ret = new CNL.DL.DateTimeVal(null, node.getLiteral().getLiteral().Replace(' ', 'T'));
            }
            else
                ret = new CNL.DL.String(null, "\'" + node.getLiteral() + "\'");
        }

        public void visit(SWRLSameIndividualAtom node)
        {
            ret = null;
            node.getFirstArgument().accept(this);
            CNL.DL.SwrlIObject n1 = ret as CNL.DL.SwrlIObject;
            ret = null;
            node.getSecondArgument().accept(this);
            CNL.DL.SwrlIObject n2;
            if (ret is string)
                n2 = new CNL.DL.SwrlIVal(null) { I = ret as string };
            else
                n2 = ret as CNL.DL.SwrlIObject;

            ret = new CNL.DL.SwrlSameAs(null)
                {
                    I = n1,
                    J = n2
                };
        }

        public void visit(SWRLDifferentIndividualsAtom node)
        {
            ret = null;
            node.getFirstArgument().accept(this);
            CNL.DL.SwrlIObject n1 = ret as CNL.DL.SwrlIObject;
            ret = null;
            node.getSecondArgument().accept(this);
            CNL.DL.SwrlIObject n2 = ret as CNL.DL.SwrlIObject;

            ret = new CNL.DL.SwrlDifferentFrom(null)
            {
                I = n1 ,
                J = n2 
            };
        }

        /////////// SWRL ////////////////////////////////////////////


        public List<string> Imports = new List<string>();

        public void visit(OWLImportsDeclaration axiom)
        {
            Imports.Add(axiom.getIRI().toString());
        }

        public static void Assert(bool b)
        {
            if (!b)
            {
#if DEBUG
                System.Diagnostics.Debugger.Break();
#endif
                throw new NotImplementedException("Conversion Assertion Failed. OWLAPI->DL");
            }
        }


    }
}
