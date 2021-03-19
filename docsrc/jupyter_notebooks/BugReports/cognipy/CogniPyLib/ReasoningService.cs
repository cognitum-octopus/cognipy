using CogniPy.ARS;
using CogniPy.CNL;
using CogniPy.CNL.DL;
using CogniPy.Configuration;
using CogniPy.Executing.HermiT;
using CogniPy.models;
using com.clarkparsia.owlapi.explanation;
using java.util;
using org.semanticweb.owlapi.apibinding;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.profiles;
using org.semanticweb.owlapi.reasoner;
using org.semanticweb.owlapi.reasoner.impl;
using org.semanticweb.owlapi.reasoner.structural;
using org.semanticweb.owlapi.util;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Xml;

namespace CogniPy.Executing.HermiTClient
{
    public enum ReasoningMode { SROIQ, RL, SWRL, STRUCTURAL, NONE }

    public class ReasoningServiceException : Exception
    {
        public ReasoningServiceException(string message) : base(message) { }
    }
    public class HermiTReasoningService
    {
        //IKVM bugfix
        static com.sun.org.apache.xerces.@internal.jaxp.SAXParserFactoryImpl s = new com.sun.org.apache.xerces.@internal.jaxp.SAXParserFactoryImpl();
        //IKVM bugfix

        public class ProgressEventArgs : EventArgs
        {
            private string msg;
            public int Value { get; private set; }
            public int Maximum { get; private set; }

            public ProgressEventArgs(string messageData, int value, int maximum)
            {
                msg = messageData;
                Value = value;
                Maximum = maximum;
            }
            public string Message
            {
                get { return msg; }
                set { msg = value; }
            }
        }
        public EventHandler<ProgressEventArgs> Progress;

        class ProgressMonitor : ReasonerProgressMonitor
        {
            HermiTReasoningService me;
            public ProgressMonitor(HermiTReasoningService me) { this.me = me; }
            public void reasonerTaskBusy()
            {
                me.fireReasonerTaskBusy();
            }
            public void reasonerTaskProgressChanged(int i1, int i2)
            {
                me.fireReasonerTaskProgressChanged(i1, i2);
            }
            public void reasonerTaskStarted(string str)
            {
                me.fireReasonerTaskStarted(str);
            }
            public void reasonerTaskStopped()
            {
                me.fireReasonerTaskStopped();
            }
        }

        public class DebugTraceEventArgs : EventArgs
        {
            public string TraceMessage { get; private set; }
            public Dictionary<string, Tuple<string, object>> Binding { get; private set; }
            public DebugTraceEventArgs(string tm, Dictionary<string, Tuple<string, object>> bnd)
            {
                TraceMessage = tm;
                Binding = bnd;
            }
        }

        public EventHandler<DebugTraceEventArgs> DebugTrace;

        void fireDebugTraceMessage(string msg, Dictionary<string, Tuple<string, object>> bnd)
        {
            if (DebugTrace != null)
                DebugTrace(this, new DebugTraceEventArgs(msg, bnd));
        }

        void fireReasonerTaskBusy()
        {
            if (Progress != null)
                Progress(this, new ProgressEventArgs("Busy", 0, 0));
        }

        void fireReasonerTaskProgressChanged(int i1, int i2)
        {
            if (Progress != null)
                Progress(this, new ProgressEventArgs("Progress: " + (i2 != 0 ? (i1 * 100 / i2).ToString() : "0") + "%", i1, i2));
        }

        void fireReasonerTaskStarted(string str)
        {
            if (Progress != null)
                Progress(this, new ProgressEventArgs("Task " + str, 0, 0));
        }

        void fireReasonerTaskStopped()
        {
            if (Progress != null)
                Progress(this, new ProgressEventArgs("Stopped", 0, 0));
        }

        OWLOntologyManager manager;

        string ontologyBase;
        IRI ontologyIRI;
        OWLOntology ontology;
        OWLReasoner reasoner;
        OWLReasoner structural_reasoner = null;
        bool materializing_reasner_supports_sroiq = true;

        public SPARQL.Transform SparqlTransform { get { return sparqlTransform; } }

        CogniPy.ARS.Transform transform = new CogniPy.ARS.Transform();
        CogniPy.SPARQL.Transform sparqlTransform = new SPARQL.Transform();
        CogniPy.ARS.InvTransform invtransform;
        CogniPy.CNL.DL.Paragraph sourceParagraph = null;
        HashSet<org.apache.jena.graph.Triple> sourceTriplets = null;

        CogniPy.CNL.DL.Paragraph swrlRulesWIthBuiltInsParagraph = new CogniPy.CNL.DL.Paragraph(null) { Statements = new List<Statement>() };

        private static string SerializeDoc(XmlDocument doc)
        {
            XmlWriterSettings settings = new XmlWriterSettings();
            settings.Indent = true;
            settings.IndentChars = "  ";
            settings.NewLineOnAttributes = false;
            settings.NamespaceHandling = NamespaceHandling.OmitDuplicates;
            settings.Encoding = Encoding.UTF8;
            settings.OmitXmlDeclaration = true;

            StringBuilder sb = new StringBuilder();
            sb.AppendLine("<?xml version = '1.0' encoding = 'UTF-8'?>");
            XmlWriter writer = XmlWriter.Create(sb, settings);
            doc.Save(writer);
            return sb.ToString();
        }

        public string GetOWLXML(bool includeImplicitValue, AnnotationManager annotMan)
        {
            XmlDocument XMLdoc = new XmlDocument();
            string ontout = OWLConverter.GetOWLXML(GetParagraph(includeImplicitValue), false, "", invUriMappings, annotMan, this.ontologyBase);
            XMLdoc.LoadXml(ontout);
            return SerializeDoc(XMLdoc);
        }

        public string GetOWLXML2(bool includeImplicitValue, bool fresh = true)
        {
            {
                OWLOntologyManager manager;
                OWLOntology ontology;
                if (fresh)
                {
                    manager = OWLManager.createOWLOntologyManager();
                    ontology = manager.createOntology(ontologyIRI);
                    manager.setOntologyFormat(ontology, owlxmlFormat);
                    var conv = transform.Convert(GetParagraph(includeImplicitValue));
                    manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.axioms));
                    manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.additions));
                }
                else
                {
                    manager = this.manager;
                    ontology = this.ontology;
                }

                XmlDocument XMLdoc = new XmlDocument();
                var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
                manager.saveOntology(ontology, owlxmlFormat, ontout);
                XMLdoc.LoadXml(ontout.toString());
                return SerializeDoc(XMLdoc);
            }
        }

        private string GetTurtle(bool includeImplicitValue, bool fresh)
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
                        var conv = transform.Convert(GetParagraph(includeImplicitValue));
                        manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.axioms));
                        manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.additions));
                    }
                    else
                    {
                        manager = this.manager;
                        ontology = this.ontology;
                    }

                    var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
                    manager.saveOntology(ontology, turtleFormat, ontout);
                    return ontout.toString();
                }
                finally
                {
                    manager.setOntologyFormat(ontology, owlxmlFormat);
                }
            }
        }

        private IEnumerable<Tuple<Statement, string>> GetTurtleStream()
        {
            foreach (var stmt in GetParagraph(false).Statements)
            {
                if (stmt is Annotation)
                    continue;
                OWLOntologyManager manager = null;
                OWLOntology ontology = null;
                manager = OWLManager.createOWLOntologyManager();
                ontology = manager.createOntology(ontologyIRI);
                manager.setOntologyFormat(ontology, turtleFormat);
                var conv = transform.Convert(new Paragraph(null, stmt));
                manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.axioms));
                manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.additions));

                var ontout = new org.semanticweb.owlapi.io.StringDocumentTarget();
                manager.saveOntology(ontology, turtleFormat, ontout);
                yield return Tuple.Create<Statement, string>(stmt, ontout.toString());
            }
        }

        private org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat owlxmlFormat = null;
        private org.semanticweb.owlapi.vocab.PrefixOWLOntologyFormat turtleFormat = null;
        ImpliKBVisitor impliKBVis = new ImpliKBVisitor();
        CogniPy.ARS.IOwlNameingConvention namc;
        Dictionary<Tuple<EntityKind, string>, string> uriMappings;
        Dictionary<string, string> invUriMappings;

        public bool ContainsSWRLBuiltIns { get { return swrlRulesWIthBuiltInsParagraph.Statements.Count > 0; } }

        public bool exeRulesOn = false;
        public bool debugModeOn = false;
        public bool modalChecker = false;

        public OWLDataFactory df = null;
        public HermiTReasoningService(CogniPy.CNL.DL.Paragraph ps, CogniPy.CNL.DL.Paragraph impliAst, ReasoningMode rmode,

            CogniPy.ARS.IOwlNameingConvention namc, string ontologyBase,
            Dictionary<Tuple<EntityKind, string>, string> uriMappings, Dictionary<string, string> invUriMappings, Dictionary<string, string> prefixes)
        {
            if (impliAst != null)
                foreach (var s in impliAst.Statements)
                    ps.Statements.Add(s);

            DLModSimplifier simli = new DLModSimplifier();
            var p = simli.Visit(ps) as CNL.DL.Paragraph;

            SwrlBuiltinsExtractor swe = new SwrlBuiltinsExtractor();
            var split = swe.Split(p);
            sourceParagraph = split.Item1;
            sourceTriplets = new HashSet<org.apache.jena.graph.Triple>();
            swrlRulesWIthBuiltInsParagraph = split.Item2;
            if (impliAst != null)
                impliKBVis.Import(impliAst);
            this.namc = namc;
            this.uriMappings = uriMappings;
            this.invUriMappings = invUriMappings;

            manager = OWLManager.createOWLOntologyManager();
            if (!ontologyBase.EndsWith("/") && !ontologyBase.EndsWith("#") && !ontologyBase.Contains("#"))
                ontologyBase += "#";

            foreach (var k in prefixes.Keys.ToList())
                if (!prefixes[k].EndsWith("/") && !prefixes[k].EndsWith("#") && !prefixes[k].Contains("#"))
                    prefixes[k] += "#";

            this.ontologyBase = ontologyBase;
            ontologyIRI = IRI.create(ontologyBase);
            ontology = manager.createOntology(ontologyIRI);
            df = manager.getOWLDataFactory();

            owlxmlFormat = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
            turtleFormat = new org.coode.owlapi.turtle.TurtleOntologyFormat();// new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();

            owlxmlFormat.setDefaultPrefix(ontologyBase);
            turtleFormat.setDefaultPrefix(ontologyBase);
            foreach (var kv in prefixes)
            {
                owlxmlFormat.setPrefix(kv.Key, kv.Value);
                turtleFormat.setPrefix(kv.Key, kv.Value);
            }

            manager.setOntologyFormat(ontology, owlxmlFormat);

            transform.setOWLDataFactory(true, ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);
            sparqlTransform.setOWLDataFactory(ontologyBase, df, owlxmlFormat, CNL.EN.CNLFactory.lex);

            transform.InvUriMappings = invUriMappings;
            sparqlTransform.InvUriMappings = invUriMappings;

            var conv = transform.Convert(sourceParagraph);
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.axioms));
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.additions));

            invtransform = new CogniPy.ARS.InvTransform(manager, ontology, owlxmlFormat, CNL.EN.CNLFactory.lex, namc);
            invtransform.InvUriMappings = invUriMappings;
            invtransform.UriMappings = uriMappings;

            try
            {
                if (rmode == ReasoningMode.STRUCTURAL)
                    reasoner = new StructuralReasoner(ontology, new SimpleConfiguration(new ProgressMonitor(this)), BufferingMode.BUFFERING);
                else if (rmode == ReasoningMode.RL || rmode == ReasoningMode.SWRL)
                    return;
                else
                {
                    OWL2ELProfile elProfile = new OWL2ELProfile();
                    OWLProfileReport report = elProfile.checkOntology(ontology);

                    bool inEl = true;
                    java.util.Iterator it = report.getViolations().iterator();
                    while (it.hasNext())
                    {
                        var v = (OWLProfileViolation)it.next();
                        if (!(v is UseOfUndeclaredClass) && !(v is UseOfNonAbsoluteIRI) && !(v is UseOfUndeclaredObjectProperty) && !(v is UseOfUndeclaredDataProperty))
                        {
                            inEl = false;
                            break;
                        }
                    }

                    if (inEl)
                    {
                        materializing_reasner_supports_sroiq = false;
                        structural_reasoner = new StructuralReasoner(ontology, new SimpleConfiguration(new ProgressMonitor(this)), BufferingMode.BUFFERING);
                        var configuration = new SimpleConfiguration(new ProgressMonitor(this));
                        OWLReasonerFactory rf = new org.semanticweb.elk.owlapi.ElkReasonerFactory();
                        reasoner = rf.createReasoner(ontology, configuration);
                    }
                    else
                    {
                        var configuration = new org.semanticweb.HermiT.Configuration();
                        configuration.reasonerProgressMonitor = new ProgressMonitor(this);
                        configuration.throwInconsistentOntologyException = false;
                        reasoner = new org.semanticweb.HermiT.Reasoner(configuration, ontology);
                    }
                }
            }
            catch (Exception ex)
            {
                throw new ReasoningServiceException(ex.Message.Replace("<" + ontologyIRI.toString() + "#", "'").Replace(">", "'") + ".");
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

            this.transform.InvUriMappings = other.invUriMappings;
            this.sparqlTransform.InvUriMappings = other.invUriMappings;

            this.invtransform = new CogniPy.ARS.InvTransform(other.manager, other.ontology, other.owlxmlFormat, CNL.EN.CNLFactory.lex, other.namc);
            this.invtransform.InvUriMappings = other.invUriMappings;
            this.invtransform.UriMappings = other.uriMappings;

            manager = other.manager;
            this.ontologyBase = other.ontologyBase;
            ontologyIRI = other.ontologyIRI;
            ontology = other.ontology;

            this.reasoner = null;// other.reasoner;
            this.model = copyOfOntModel;
        }

        private org.apache.jena.reasoner.rulesys.GenericRuleReasoner bindedRsnr;
        public HermiTReasoningService Clone(dynamic TheAccessObject, dynamic Outer)
        {

            lock (this)
            {
                if (bindedRsnr == null)
                {
                    ((org.apache.jena.rdf.model.impl.InfModelImpl)model).prepare();
                    bindedRsnr = (org.apache.jena.reasoner.rulesys.GenericRuleReasoner)((org.apache.jena.reasoner.rulesys.GenericRuleReasoner)rete_reasoner).bindSchema(model.getGraph());
                    JenaRuleManager.BootstrapReasonerForAboxChanges(bindedRsnr, rete_reasoner, TheAccessObject, Outer);
                }


                var newModel = org.apache.jena.rdf.model.ModelFactory.createInfModel(bindedRsnr, org.apache.jena.rdf.model.ModelFactory.createDefaultModel());
                return new HermiTReasoningService(this, newModel);
            }
        }

        public static CNL.DL.Paragraph SimplifyDL(CNL.DL.Paragraph paragraph)
        {
            DLModSimplifier simli = new DLModSimplifier();
            var paragraphSimplified = simli.Visit(paragraph) as CogniPy.CNL.DL.Paragraph;
            return paragraphSimplified;
        }

        public string renderEntityFromUri(string uri, ARS.EntityKind kind = ARS.EntityKind.Instance)
        {
            return invtransform.renderEntity(uri, kind);
        }

        public string renderUriFromEntity(string cnl, ARS.EntityKind kind = ARS.EntityKind.Instance)
        {
            return transform.getIRIFromDL(cnl, kind).toString();
        }

        const bool CALCULATE_DISJOINTS = false;

        org.apache.jena.rdf.model.Model model = null;

        public class SparqlRowset
        {
            org.apache.jena.query.ResultSet results;
            org.apache.jena.shared.PrefixMapping prefixMap;
            org.apache.jena.rdf.model.Model model;
            bool detectTypesOfNodes;
            string defaultKindOfNode;
            string defaultNS;

            internal SparqlRowset(org.apache.jena.rdf.model.Model model, org.apache.jena.query.ResultSet results, string defaultNS = null, org.apache.jena.shared.PrefixMapping prefixMap = null, bool detectTypesOfNodes = true, string defaultKindOfNode = null)
            {
                this.results = results;
                this.prefixMap = prefixMap;
                this.defaultNS = defaultNS;
                this.model = model;
                this.detectTypesOfNodes = detectTypesOfNodes;
                this.defaultKindOfNode = defaultKindOfNode;
            }

            public string GetTypeOfNode(org.apache.jena.rdf.model.Model context, org.apache.jena.graph.Node n)
            {
                var infgraph = context.getGraph();

                bool isInstance = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                bool isRole = false;
                bool isDataRole = false;
                if (!isInstance)
                    isRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode());
                if (!isInstance && !isRole)
                    isDataRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());

                bool isConcept = (!isInstance && !isRole && !isDataRole);
                return (isInstance ? "instance" : (isConcept ? "concept" : (isRole ? "role" : "datarole")));
            }

            private List<object> Convert(List<object> value)
            {

                if (prefixMap == null) return value;

                var keySet = prefixMap.getNsPrefixMap().keySet();

                var it = keySet.iterator();

                var uris = value.OfType<string>() as IEnumerable<object>;

                var val = value.ToArray();

                while (it.hasNext())
                {

                    var prefix = (string)it.next();

                    for (int i = 0; i < val.Count(); i++)
                    {
                        if (uris.Contains(val[i]))
                            val[i] = ((string)val[i]).Replace(prefixMap.getNsPrefixURI(prefix), (string)prefix + ":").Replace(defaultNS, "");
                    }
                }

                return val.ToList();

            }


            public List<string> GetCols()
            {
                List<string> res = new List<string>();
                var names = results.getResultVars();
                var namesIter = names.iterator();
                while (namesIter.hasNext())
                {
                    var varId = namesIter.next().ToString();
                    res.Add(varId);
                }
                return res;
            }

            //            public static object CommonLock = new object();

            public IEnumerable<List<object>> GetRows()
            {
                var keys = GetCols();
                while (results.hasNext())
                {
                    org.apache.jena.query.QuerySolution qbs = null;
                    try
                    {
                        //                        lock (CommonLock)
                        qbs = results.next();
                    }
                    catch (java.util.NoSuchElementException)
                    {
                        if (qbs == null)
                            break;
                    }
                    List<object> row = new List<object>();
                    bool blank = false;
                    foreach (var key in keys)
                    {
                        var val = qbs.get(key);
                        if (val == null)
                            row.Add(null);
                        else if (val is org.apache.jena.rdf.model.Literal)
                        {
                            var v = CogniPy.SPARQL.SparqlNode.ToTypedValue((val as org.apache.jena.rdf.model.Literal).ToString());
                            if (v == null)
                                v = JenaRuleManager.getObject(val.asNode());
                            row.Add(v);
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
                                var type = GetTypeOfNode(model, val.asNode());
                                row.Add(new CogniPy.GraphEntity() { Name = val.toString(), Kind = type });
                            }
                            else
                                row.Add(new CogniPy.GraphEntity() { Name = val.toString(), Kind = defaultKindOfNode });
                        }
                    }
                    if (!blank)
                        yield return row;

                }



            }
        }

        public string GetOntologyID()
        {
            return ontology.getOntologyID().getOntologyIRI().toString();
        }

        public SparqlRowset SparqlQuery(string queryString, Dictionary<string, string> pfx2NsMap, bool detectTypesOfNodes = true, string defaultKindOfNode = null)
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
            var prefixes = "PREFIX : <" + ontology.getOntologyID().getOntologyIRI().toString() + ">\n";

            prefixes += "PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>\n";
            prefixes += "PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>\n";
            prefixes += "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n";
            prefixes += "PREFIX xml: <http://www.w3.org/XML/1998/namespace#>\n";
            prefixes += "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n";

            if (pfx2NsMap != null)
            {
                foreach (var pfx2ns in pfx2NsMap)
                {
                    if (!queryString.Contains("PREFIX " + pfx2ns.Key + ":") && !prefixes.Contains("PREFIX " + pfx2ns.Key + ":"))
                        prefixes += "PREFIX " + pfx2ns.Key + ": <" + pfx2ns.Value + ">\n";
                }
            }
            queryString = prefixes + queryString;
            org.apache.jena.query.Query query = org.apache.jena.query.QueryFactory.create(queryString);

            var origPrefixMap = query.getPrefixMapping();

            var keySet = prefixMap.getNsPrefixMap().keySet();

            var it = keySet.iterator();

            org.apache.jena.shared.PrefixMapping newPrefixMap = new org.apache.jena.shared.impl.PrefixMappingImpl();

            // Add all well known prefixes, but replace those that are directly defined by the user

            while (it.hasNext())
            {

                var prefix = (string)it.next();

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
                var prefix = (string)it.next();

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


        class MyListener : org.apache.jena.rdf.model.ModelChangedListener
        {
            Action<org.apache.jena.rdf.model.Statement> call;
            public MyListener(Action<org.apache.jena.rdf.model.Statement> call)
            {
                this.call = call;
            }
            public void addedStatement(org.apache.jena.rdf.model.Statement s)
            {
                call(s);
            }

            public void addedStatements(org.apache.jena.rdf.model.Model m) { }
            public void addedStatements(org.apache.jena.rdf.model.StmtIterator si) { }
            public void addedStatements(List l) { }
            public void addedStatements(org.apache.jena.rdf.model.Statement[] sarr) { }
            public void notifyEvent(org.apache.jena.rdf.model.Model m, object obj) { }
            public void removedStatement(org.apache.jena.rdf.model.Statement s) { }
            public void removedStatements(org.apache.jena.rdf.model.Model m) { }
            public void removedStatements(org.apache.jena.rdf.model.StmtIterator si) { }
            public void removedStatements(List l) { }
            public void removedStatements(org.apache.jena.rdf.model.Statement[] sarr) { }
        }

        Dictionary<string, CNL.DL.Statement> blankStmts = null;

        private void addBlankStmt(string id, Statement stmt)
        {
            if (!blankStmts.ContainsKey(id))
                blankStmts.Add(id, stmt);
        }

        private void BuildModel()
        {

            if (model != null)
                return;

            model = org.apache.jena.rdf.model.ModelFactory.createDefaultModel();

            if (true)
            {
                blankStmts = new Dictionary<string, Statement>();
                CNL.DL.Statement curstmt = null;
                MyListener lst = new MyListener((stmt) =>
                {
                    if (stmt.getObject().isAnon())
                        addBlankStmt(stmt.getObject().asNode().getBlankNodeId().toString(), curstmt);
                    if (stmt.getPredicate().isAnon())
                        addBlankStmt(stmt.getPredicate().asNode().getBlankNodeId().toString(), curstmt);
                    if (stmt.getSubject().isAnon())
                        addBlankStmt(stmt.getSubject().asNode().getBlankNodeId().toString(), curstmt);
                });
                model.register(lst);
                foreach (var turt in GetTurtleStream())
                {
                    curstmt = turt.Item1;
                    model.read(new java.io.StringReader(turt.Item2), "", "TURTLE");
                }
                model.unregister(lst);
            }
            else
            {
                model.read(new java.io.StringReader(GetTurtle(true, false)), "", "TURTLE");
            }
            foreach (var trip in AdditionalTriplets)
            {
                var node = model.getRDFNode(JenaRuleManager.getLiteral(trip.Item3));
                model.add(
                    model.getResource(transform.getIRIFromDL(trip.Item1, EntityKind.Instance).toString()),
                    model.getProperty(transform.getIRIFromDL(trip.Item2, EntityKind.DataRole).toString()), node);

            }

            AdditionalTriplets.Clear();
        }

        CNL.DL.Paragraph inferfedAbox = null;

        public Value getValFromJenaLiteral(org.apache.jena.graph.impl.LiteralLabel node)
        {
            var val = node.getValue();
            if (val is java.lang.Double || val is java.lang.Float || val is double || val is float)
                return new CNL.DL.Float(null, val.ToString());
            else if (val is java.lang.Number || val is java.lang.Integer || val is java.lang.Long || val is int || val is long)
                return new CNL.DL.Number(null, val.ToString());
            else if (val is java.lang.String || val is string)
                return new CNL.DL.String(null, "\'" + val.ToString().Replace("\'", "\'\'") + "\'");
            else if (val is java.lang.Boolean || val is bool)
                return new CNL.DL.Bool(null, val.ToString() == "true" ? "[1]" : "[0]");
            else if (val is org.apache.jena.datatypes.xsd.XSDDateTime)
                return new CNL.DL.DateTimeVal(null, val.ToString());
            else if (val is org.apache.jena.datatypes.xsd.XSDDuration)
                return new CNL.DL.Duration(null, val.ToString());

            throw new NotImplementedException("Unimplemented datatype");
        }

        private void InvalidateSyncOntologyToModel()
        {
            inferfedAbox = null;
        }

        public void SyncOntologyToModel()
        {
            if (model == null)
                return;

            if (inferfedAbox != null)
                return;

            inferfedAbox = new Paragraph(null) { Statements = new List<Statement>() };

            var dedGraph = ((org.apache.jena.reasoner.InfGraph)((org.apache.jena.rdf.model.InfModel)model).getGraph()).getDeductionsGraph();

            var triplets = dedGraph.find(null, null, null);

            while (triplets.hasNext())
                AddTripleStmt((org.apache.jena.graph.Triple)triplets.next(), inferfedAbox);

        }

        public void AddTripleStmt(org.apache.jena.graph.Triple stmt, Paragraph inferfedAbox)
        {
            var s = stmt.getSubject();
            var v = stmt.getPredicate();
            var o = stmt.getObject();
            if (s.isBlank() || v.isBlank() || o.isBlank())
                return;

            if (s.isURI() && v.isURI())
            {
                if (s.getURI() == org.apache.jena.vocabulary.OWL.Nothing.getURI())
                    return;

                if (v.getURI() == org.apache.jena.vocabulary.RDF.type.getURI())
                {
                    if (o.isURI())
                    {
                        if (o.getURI() == org.apache.jena.vocabulary.OWL.DatatypeProperty.getURI())
                        {
                            //role declaration (top data role)
                            var st = new CNL.DL.DataRoleInclusion(null)
                            {
                                C = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.DataRole) },
                                D = new CNL.DL.Top(null)
                            };
                            inferfedAbox.Statements.Add(st);
                        }
                        else if (o.getURI() == org.apache.jena.vocabulary.OWL.ObjectProperty.getURI())
                        {
                            //role declaration (top object role)
                            var st = new CNL.DL.RoleInclusion(null)
                            {
                                C = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Role) },
                                D = new CNL.DL.Top(null)
                            };
                            inferfedAbox.Statements.Add(st);
                        }
                        else if (o.getURI() == org.apache.jena.vocabulary.OWL2.NamedIndividual.getURI())
                        {
                            //class declaration (top concept)
                            var un = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
                            var st = new CNL.DL.InstanceOf(null)
                            {
                                I = new CNL.DL.NamedInstance(null) { name = un },
                                C = new CNL.DL.Top(null)
                            };
                            inferfedAbox.Statements.Add(st);
                        }
                        else if (o.getURI() == org.apache.jena.vocabulary.OWL.Class.getURI())
                        {
                            //class declaration (top concept)
                            var un = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept);
                            var st = new CNL.DL.Subsumption(null)
                            {
                                C = new CNL.DL.Atomic(null) { id = un },
                                D = new CNL.DL.Top(null)
                            };
                            inferfedAbox.Statements.Add(st);
                        }
                        else //instance type
                        {
                            if (o.getURI() != org.apache.jena.vocabulary.OWL.Thing.getURI())
                            {
                                var un = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance);
                                var st = new CNL.DL.InstanceOf(null)
                                {
                                    I = new CNL.DL.NamedInstance(null) { name = un },
                                    C = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Concept) }
                                };
                                inferfedAbox.Statements.Add(st);
                            }
                        }
                    }
                    else
                    {
                        //unknown stuff
                        return;
                    }
                }
                else if (v.getURI() == org.apache.jena.vocabulary.RDFS.subClassOf.getURI())
                {
                    if (o.isURI())
                    {
                        if (s.getURI() != o.getURI())
                        {
                            if (o.getURI() == org.apache.jena.vocabulary.OWL.Thing.getURI())
                            {
                                var st = new CNL.DL.Subsumption(null)
                                {
                                    C = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept) },
                                    D = new CNL.DL.Top(null)
                                };
                                inferfedAbox.Statements.Add(st);
                            }
                            else
                            { //subclassof
                                var st = new CNL.DL.Subsumption(null)
                                {
                                    C = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept) },
                                    D = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Concept) },
                                };
                                inferfedAbox.Statements.Add(st);
                            }
                        }
                    }
                }
                else if (v.getURI() == org.apache.jena.vocabulary.OWL.equivalentClass.getURI())
                {
                    if (o.isURI())
                    {
                        if (s.getURI() != o.getURI())
                        { //equivalent
                            var st = new CNL.DL.Equivalence(null)
                            {
                                Equivalents = new List<CNL.DL.Node>()
                                    {
                                        new CNL.DL.Atomic(null) { id = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Concept) },
                                        new CNL.DL.Atomic(null) { id = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Concept) }
                                    }
                            };
                            inferfedAbox.Statements.Add(st);
                        }
                    }
                }
                else if (v.getURI() == org.apache.jena.vocabulary.OWL.sameAs.getURI())
                {
                    if (o.isURI())
                    {
                        if (s.getURI() != o.getURI())
                        {
                            //same as
                            var st = new CNL.DL.SameInstances(null)
                            {
                                Instances = new List<Instance>(){
                                    new CNL.DL.NamedInstance(null){name =
                                        invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance)} ,
                                    new CNL.DL.NamedInstance(null){name =
                                        invtransform.renderEntity(o.getURI(), ARS.EntityKind.Instance)}
                                }
                            };
                            inferfedAbox.Statements.Add(st);
                        }
                    }
                    else
                    {
                        //unknown stuff
                        return;
                    }
                }
                else if (!v.getURI().StartsWith("http://www.w3.org/"))
                {
                    if (o.isURI())
                    {
                        var st = new CNL.DL.RelatedInstances(null)
                        {
                            I = new CNL.DL.NamedInstance(null) { name = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance) },
                            R = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(v.getURI(), ARS.EntityKind.Role) },
                            J = new CNL.DL.NamedInstance(null) { name = invtransform.renderEntity(o.getURI(), ARS.EntityKind.Instance) }
                        };
                        inferfedAbox.Statements.Add(st);
                    }
                    else if (o.isLiteral())
                    {
                        var st = new CNL.DL.InstanceValue(null)
                        {
                            I = new CNL.DL.NamedInstance(null) { name = invtransform.renderEntity(s.getURI(), ARS.EntityKind.Instance) },
                            R = new CNL.DL.Atomic(null) { id = invtransform.renderEntity(v.getURI(), ARS.EntityKind.Role) },
                            V = getValFromJenaLiteral(o.getLiteral())
                        };
                        inferfedAbox.Statements.Add(st);
                    }
                }
            }
        }

        public CNL.DL.Paragraph GetMaterializedRLParagrah(bool includeImplicitKnowledge)
        {
            SyncOntologyToModel();

            var toRet = new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() };

            if (inferfedAbox != null)
            {

                var para = invtransform.Convert(ontology);

                foreach (var s in inferfedAbox.Statements)
                {
                    if (includeImplicitKnowledge || !impliKBVis.IsEntailed(s))
                        toRet.Statements.Add(s);
                }
            }
            return toRet;
        }

        public CNL.DL.Paragraph GetParagraph(bool includeImplicitKnowledge, bool includeMaterialized = true)
        {
            List<CNL.DL.Statement> stmts = new List<Statement>();
            stmts.AddRange(sourceParagraph.Statements);

            {//Add asserted statements
                Paragraph pr1 = new Paragraph(null) { Statements = new List<Statement>() };
                foreach (var stmt in sourceTriplets)
                    AddTripleStmt(stmt, pr1);
                stmts.AddRange(pr1.Statements);
            }

            stmts.AddRange(swrlRulesWIthBuiltInsParagraph.Statements);
            if (includeImplicitKnowledge)
            {
                if (includeMaterialized)
                {
                    SyncOntologyToModel();
                    if (inferfedAbox != null)
                    {
                        HashSet<string> aboxAlready = new HashSet<string>();
                        var ser = new CogniPy.CNL.DL.Serializer(false);
                        //                        DLModSimplifier simli = new DLModSimplifier();
                        //                        var p = simli.Visit(toRet) as CNL.DL.Paragraph;

                        foreach (var s in stmts)
                        {
                            if (IsABox(s))
                                aboxAlready.Add(ser.Serialize(s));
                        }
                        foreach (var s in inferfedAbox.Statements)
                        {
                            if (includeImplicitKnowledge || !impliKBVis.IsEntailed(s))
                                if (aboxAlready.Add(ser.Serialize(s)))
                                    stmts.Add(s);
                        }
                    }
                }
            }
            return new CNL.DL.Paragraph(null) { Statements = stmts };
        }


        public IEnumerable<ConstraintResult> GetAllConstraints()
        {
            var results = new List<ConstraintResult>();

            foreach (var s in swrlRulesWIthBuiltInsParagraph.Statements)
            {
                if (s.modality != Statement.Modality.IS)
                {
                    var subsumptn = ((Subsumption)s);
                    ConstraintResult result = new ConstraintResult();
                    if (results.Any(cr => cr.Concept == ((Atomic)subsumptn.C).id))
                        result = results.Where(cr => cr.Concept == ((Atomic)subsumptn.C).id).First();


                    result.Concept = ((Atomic)subsumptn.C).id;
                    if (result.Relations == null)
                        result.Relations = new Dictionary<Statement.Modality, List<string>>();
                    if (result.ThirdElement == null)
                        result.ThirdElement = new Dictionary<Statement.Modality, List<string>>();

                    if (!result.Relations.ContainsKey(subsumptn.modality))
                        result.Relations.Add(s.modality, new List<string>());
                    if (!result.ThirdElement.ContainsKey(subsumptn.modality))
                        result.ThirdElement.Add(s.modality, new List<string>());

                    if (subsumptn.D is SomeRestriction)
                    {
                        result.Relations[s.modality].Add(((Atomic)((SomeRestriction)subsumptn.D).R).id);
                        result.ThirdElement[s.modality].Add(((Atomic)((SomeRestriction)subsumptn.D).C).id);
                    }
                    else if (subsumptn.D is SomeValueRestriction)
                    {
                        result.Relations[s.modality].Add(((Atomic)((SomeValueRestriction)subsumptn.D).R).id);
                        var type = string.Empty;
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
                                throw new NotImplementedException();
                        }

                        result.ThirdElement[s.modality].Add("(some " + type + " value)");
                    }

                    if (!results.Any(cr => cr.Concept == ((Atomic)subsumptn.C).id))
                        results.Add(result);
                }
            }

            return results;
        }

        bool isMaterializedTbox = false;
        bool isMaterializedAbox = false;

        public bool Materialization(ReasoningMode TBox, ReasoningMode ABox, bool isAboxInsertOnly, bool modalChecker = false)
        {
            if (isMaterializedAbox)
                return false;

            if (isMaterializedTbox && TBox != ReasoningMode.NONE)
                return false;

            if (TBox == ReasoningMode.SROIQ && ABox == ReasoningMode.SROIQ)
            {
                MaterializeSROIQ(MatMode.Both, isAboxInsertOnly);
                isMaterializedTbox = true;
                isMaterializedAbox = true;
            }
            else if (TBox == ReasoningMode.RL && ABox == ReasoningMode.RL)
            {
                MaterializeRL(MatMode.Both, modalChecker: modalChecker);
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
                    MaterializeRL(MatMode.Tbox, modalChecker: modalChecker);
                }
                if (ABox == ReasoningMode.SROIQ)
                {
                    MaterializeSROIQ(MatMode.Abox, isAboxInsertOnly);
                    isMaterializedAbox = true;
                }
                if (ABox == ReasoningMode.RL)
                {
                    isMaterializedAbox = true;
                    MaterializeRL(MatMode.Abox, modalChecker: modalChecker);
                }
                if (ABox == ReasoningMode.SWRL)
                {
                    isMaterializedAbox = true;
                    MaterializeRL(MatMode.SWRLOnly, swrlOnly: true, modalChecker: modalChecker);
                }
            }
            return true;
        }

        public dynamic TheAccessObject;
        public dynamic Outer;

        public string WD;

        Dictionary<string, Statement> id2stmt = new Dictionary<string, Statement>();

        private void MaterializeRL(MatMode mode, bool extended = true, bool sameAs = true, bool modalChecker = false, bool swrlOnly = false)
        {
            fireReasonerTaskStarted("RL Materialization");
            fireReasonerTaskProgressChanged(0, 2);
            JenaRuleManager.Setup();
            BuildModel();

            var src_model = model;

            var rules = JenaRuleManager.GetGeneralRules(mode, extended, sameAs, debugModeOn);

            var para = GetParagraph(true, false);


#if USE_DYNAMICRULES
            CalcDynamicDepths cdd = new CalcDynamicDepths();
            cdd.Visit(para);

            rules.addAll(JenaRuleManager.GetDynamicRules(mode, cdd.IntersectionDepth, cdd.UnionDepth,cdd.HasKeyDepth));
#endif
            var DebugAction = new Action<string, Dictionary<string, Tuple<string, object>>>((s, d) =>
             {
                 fireDebugTraceMessage(s, d);
             });
            var ExeRules = new Dictionary<int, Tuple<string, List<IExeVar>>>();
            var SwrlIterators = new Dictionary<int, SwrlIterate>();
            bool wdSet = false;
            foreach (var stmt in para.Statements.Union(swrlRulesWIthBuiltInsParagraph.Statements))
            {
                if (stmt is ComplexRoleInclusion || stmt is Subsumption || stmt is HasKey || (mode != MatMode.Tbox && (stmt is SwrlStatement || stmt is ExeStatement || stmt is SwrlIterate) || (stmt.modality != Statement.Modality.IS)))
                {
                    var gen = new GenerateJenaRules(model, modalChecker, mode != MatMode.Tbox, debugModeOn, exeRulesOn, debugModeOn, swrlOnly);
                    gen.setOWLDataFactory(ontologyBase, owlxmlFormat, CNL.EN.CNLFactory.lex);
                    gen.setId2stmt(id2stmt);
                    var scr = gen.Generate(new Paragraph(null) { Statements = new List<Statement>() { stmt } });
                    rules.addAll(JenaRuleManager.GetRule(scr));
                    foreach (var r in gen.TheRules)
                        ExeRules.Add(r.Key, r.Value);
                    foreach (var r in gen.TheIterators)
                        SwrlIterators.Add(r.Key, r.Value);

                }
                else if (stmt is CodeStatement)
                {
                    if (!wdSet)
                    {
                        if (WD != null)
                        {
                            Outer.Evaluate("setwd(\'" + WD.Replace('\\', '/') + "')");
                        }
                        wdSet = true;
                    }
                    var code = (stmt as CodeStatement).exe;
                    code = code.Substring(2, code.Length - 4);
                    code = code.Replace("??>", "?>");
                    Outer.Evaluate(code);
                }
            }

            var sproc = new SwrlIterateProc(model, swrlOnly);
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
        public static List<org.apache.jena.graph.Node> convertList(org.apache.jena.graph.Node root, org.apache.jena.graph.Graph context)
        {
            return convertList(root, context, new List<org.apache.jena.graph.Node>());
        }

        /**
         * Convert an (assumed well formed) RDF list to a java list of Nodes
         */
        private static List<org.apache.jena.graph.Node> convertList(org.apache.jena.graph.Node node, org.apache.jena.graph.Graph context, List<org.apache.jena.graph.Node> sofar)
        {
            if (node == null || node.equals(org.apache.jena.vocabulary.RDF.nil.asNode())) return sofar;
            org.apache.jena.graph.Node next = org.apache.jena.reasoner.rulesys.Util.getPropValue(node, org.apache.jena.vocabulary.RDF.first.asNode(), context);
            if (next != null)
            {
                sofar.Add(next);
                return convertList(org.apache.jena.reasoner.rulesys.Util.getPropValue(node, org.apache.jena.vocabulary.RDF.rest.asNode(), context), context, sofar);
            }
            else
            {
                return sofar;
            }
        }

        private string triple2Cnl(org.apache.jena.graph.Triple tri, org.apache.jena.reasoner.InfGraph infGraph)
        {
            if (tri.getMatchSubject().isBlank() || tri.getMatchObject().isBlank() || tri.getMatchPredicate().isBlank())
                return null;

            Statement stmt = null;
            if (tri.getMatchPredicate().getURI() == org.apache.jena.vocabulary.RDF.type.getURI())
            {
                var sn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Instance);
                var cn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Concept);
                stmt = new InstanceOf(null) { C = new CNL.DL.Atomic(null) { id = cn }, I = new CNL.DL.NamedInstance(null) { name = sn } };
            }
            else if (tri.getMatchPredicate().getURI() == org.apache.jena.vocabulary.RDFS.subPropertyOf.getURI())
            {
                var isDataRole = infGraph.contains(tri.getMatchObject(), org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());

                if (isDataRole)
                {
                    var cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.DataRole);
                    var dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.DataRole);
                    stmt = new DataRoleInclusion(null) { C = new CNL.DL.Atomic(null) { id = cn }, D = new CNL.DL.Atomic(null) { id = dn } };
                }
                else
                {
                    var cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Role);
                    var dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Role);
                    stmt = new RoleInclusion(null) { C = new CNL.DL.Atomic(null) { id = cn }, D = new CNL.DL.Atomic(null) { id = dn } };
                }
            }
            else if (tri.getMatchPredicate().getURI() == org.apache.jena.vocabulary.RDFS.subClassOf.getURI())
            {
                if (tri.getMatchObject().getURI() == org.apache.jena.vocabulary.OWL.Thing.getURI())
                {
                    var cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Concept);
                    stmt = new Subsumption(null) { C = new CNL.DL.Atomic(null) { id = cn }, D = new CNL.DL.Top(null) };
                }
                else
                {
                    var cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Concept);
                    var dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Concept);
                    stmt = new Subsumption(null) { C = new CNL.DL.Atomic(null) { id = cn }, D = new CNL.DL.Atomic(null) { id = dn } };
                }
            }
            else if (tri.getMatchPredicate().getURI() == org.apache.jena.vocabulary.OWL.equivalentClass.getURI())
            {
                var cn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Concept);
                var dn = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Concept);
                stmt = new Equivalence(null)
                {
                    Equivalents = new List<CNL.DL.Node>()
                                    { new CNL.DL.Atomic(null) { id = cn }, new CNL.DL.Atomic(null) { id = dn } }
                };
            }
            else if (tri.getMatchPredicate().getURI() == org.apache.jena.vocabulary.OWL.sameAs.getURI())
            {
                var i = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Instance);
                var j = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Instance);
                stmt = new SameInstances(null)
                {
                    Instances = new List<CNL.DL.Instance>()
                    {
                        new CNL.DL.NamedInstance(null){name = i }, new CNL.DL.NamedInstance(null){name = j }
                    }
                };
            }
            else
            {
                var sn = renderEntityFromUri(tri.getMatchSubject().getURI(), EntityKind.Instance);
                var pn = renderEntityFromUri(tri.getMatchPredicate().getURI(), EntityKind.Role);
                if (tri.getMatchObject().isLiteral())
                {
                    var on = getValFromJenaLiteral(tri.getMatchObject().getLiteral());
                    stmt = new InstanceValue(null) { V = on, R = new CNL.DL.Atomic(null) { id = pn }, I = new CNL.DL.NamedInstance(null) { name = sn } };
                }
                else
                {
                    var on = renderEntityFromUri(tri.getMatchObject().getURI(), EntityKind.Instance);
                    stmt = new RelatedInstances(null) { R = new CNL.DL.Atomic(null) { id = pn }, I = new CNL.DL.NamedInstance(null) { name = sn }, J = new CNL.DL.NamedInstance(null) { name = on } };
                }
            }

            if (stmt != null)
                return TheAccessObject.ToCNL(stmt);
            else
                return null;
        }

        private string blankToCnl(org.apache.jena.graph.Node blk)
        {
            if (blankStmts.ContainsKey(blk.getBlankNodeId().toString()))
                return TheAccessObject.ToCNL(blankStmts[blk.getBlankNodeId().toString()]);
            else
                return blk.getBlankNodeId().toString();
        }

        private string JESC(string s)
        {
            return JavaScriptStringEncode(s, true);
        }

        public static string JavaScriptStringEncode(string value)
        {
            return JavaScriptStringEncode(value, false);
        }

        public static string JavaScriptStringEncode(string value, bool addDoubleQuotes)
        {
            if (System.String.IsNullOrEmpty(value))
                return addDoubleQuotes ? "\"\"" : System.String.Empty;

            int len = value.Length;
            bool needEncode = false;
            char c;
            for (int i = 0; i < len; i++)
            {
                c = value[i];

                if (c >= 0 && c <= 31 || c == 34 || c == 39 || c == 60 || c == 62 || c == 92)
                {
                    needEncode = true;
                    break;
                }
            }

            if (!needEncode)
                return addDoubleQuotes ? "\"" + value + "\"" : value;

            var sb = new StringBuilder();
            if (addDoubleQuotes)
                sb.Append('"');

            for (int i = 0; i < len; i++)
            {
                c = value[i];
                if (c >= 0 && c <= 7 || c == 11 || c >= 14 && c <= 31 || c == 39 || c == 60 || c == 62)
                    sb.AppendFormat("\\u{0:x4}", (int)c);
                else switch ((int)c)
                    {
                        case 8:
                            sb.Append("\\b");
                            break;

                        case 9:
                            sb.Append("\\t");
                            break;

                        case 10:
                            sb.Append("\\n");
                            break;

                        case 12:
                            sb.Append("\\f");
                            break;

                        case 13:
                            sb.Append("\\r");
                            break;

                        case 34:
                            sb.Append("\\\"");
                            break;

                        case 92:
                            sb.Append("\\\\");
                            break;

                        default:
                            sb.Append(c);
                            break;
                    }
            }

            if (addDoubleQuotes)
                sb.Append('"');

            return sb.ToString();
        }

        private string JVAL(object o)
        {
            if (o is string)
                return JESC(o.ToString());
            else
                return o.ToString();
        }

        protected Tuple<List<Tuple<string, string, List<Tuple<object, string>>>>, Dictionary<string, List<LinkedDictionary<string, JenaValue>>>> GetReasoningInfoDetails()
        {
            return Tuple.Create(JenaRuleManager.GetOntologyErrors(rete_reasoner), JenaRuleManager.GetModalValidationResult(rete_reasoner));
        }

        protected void printErrors(TextWriter strWriter, List<Tuple<string, string, List<Tuple<object, string>>>> errors)
        {
            strWriter.WriteLine(JESC("errors") + ":[");
            bool addComma = false;
            foreach (var error in errors)
            {
                if (addComma)
                    strWriter.Write(',');
                else
                    addComma = true;
                var title = error.Item1;
                var content = error.Item2;
                var vals = error.Item3;
                strWriter.WriteLine("{" + JESC("title") + ":" + JESC(title) + ",");
                strWriter.WriteLine(JESC("content") + ":" + JESC(content) + ",");
                strWriter.WriteLine(JESC("vals") + ":{");
                bool addComma2 = false;
                foreach (var val in vals)
                {
                    if (addComma2)
                        strWriter.Write(',');
                    else
                        addComma2 = true;
                    if (val.Item2 == "value")
                        strWriter.WriteLine(JESC(val.Item2) + ":" + JVAL(val.Item1));
                    else
                        strWriter.WriteLine(JESC(val.Item2) + ":" + JESC(val.Item1.ToString()));
                }
                strWriter.WriteLine("}");
                strWriter.WriteLine("}");
            }
            strWriter.WriteLine("]");
        }

        protected void printModals(TextWriter strWriter, Dictionary<string, List<LinkedDictionary<string, JenaValue>>> modals)
        {
            strWriter.WriteLine(JESC("modals") + ":{");
            bool addComma = false;
            foreach (var kv in modals)
            {
                if (addComma)
                    strWriter.Write(',');
                else
                    addComma = true;
                var key = kv.Key;
                var content = kv.Value;
                strWriter.WriteLine(JESC(key) + ":[");
                bool addComma2 = false;
                foreach (var cc in content)
                {
                    if (addComma2)
                        strWriter.Write(',');
                    else
                        addComma2 = true;
                    strWriter.WriteLine("{");
                    foreach (var kv2 in cc)
                    {
                        if (kv2.Value.IsInstance)
                            strWriter.WriteLine(JESC(kv2.Key) + ":{\"instance\":" + JESC(kv2.Value.Value.ToString()) + "}");
                        else
                            strWriter.WriteLine(JESC(kv2.Key) + ":{\"value\":" + JVAL(kv2.Value.Value) + "}");
                    }
                    strWriter.WriteLine("}");
                }
                strWriter.WriteLine("]");
            }
            strWriter.WriteLine("}");
        }

        public string GetReasoningInfo()
        {
            StringWriter strWriter = new StringWriter();

            var status = GetReasoningInfoDetails();
            var errors = status.Item1;
            var modals = status.Item2;
            if (errors.Count == 0 && modals.Count == 0)
                return "";

            strWriter.Write("{");
            if (errors.Count > 0)
            {
                printErrors(strWriter, errors);
                if (modals.Count > 0)
                    strWriter.Write(",");
            }

            if (modals.Count > 0)
                printModals(strWriter, modals);

            strWriter.Write("}");
            return strWriter.ToString();

        }


        protected void printTrace(TextWriter strWriter, org.apache.jena.reasoner.InfGraph infGraph, org.apache.jena.reasoner.rulesys.RuleDerivation me, int indent, HashSet<org.apache.jena.reasoner.rulesys.RuleDerivation> seen, HashSet<string> alreadyPrinted)
        {
            strWriter.Write(new string(' ', indent) + "{");

            if (id2stmt.ContainsKey(me.getRule().getName()))
                strWriter.WriteLine(JESC("rule") + ":" + JESC(TheAccessObject.ToCNL(id2stmt[me.getRule().getName()])) + ",");

            var tr = triple2Cnl(me.getConclusion(), infGraph);
            if (tr != null)
                strWriter.Write(JESC("concluded") + ":" + JESC(tr) + ",");

            strWriter.WriteLine(JESC("by") + ":[");
            int margin = indent + 4;
            bool addComma = false;
            for (int i = 0; i < me.getMatches().size(); i++)
            {

                var match = (org.apache.jena.graph.Triple)me.getMatches().get(i);

                var derivations = infGraph.getDerivation(match);
                if (derivations == null || !derivations.hasNext())
                {
                    if (addComma)
                        strWriter.WriteLine(",");
                    strWriter.Write(new string(' ', margin) + "{");
                    if (match == null)
                    {
                        // A primitive
                        var term = me.getRule().getBodyElement(i);
                        if (term is org.apache.jena.reasoner.rulesys.Functor)
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
                            strWriter.Write(JESC("expr") + ":" + JESC(blankToCnl(match.getMatchSubject())));
                        else if (match.getMatchPredicate().isBlank())
                            strWriter.Write(JESC("expr") + ":" + JESC(blankToCnl(match.getMatchPredicate())));
                        else if (match.getMatchObject().isBlank())
                            strWriter.Write(JESC("expr") + ":" + JESC(blankToCnl(match.getMatchObject())));
                        else
                            strWriter.Write(JESC("expr") + ":" + JESC(triple2Cnl(match, infGraph)));
                    }
                    strWriter.Write("}");
                    addComma = true;
                }
                else
                {
                    if (addComma)
                        strWriter.WriteLine(",");
                    var derivation = (org.apache.jena.reasoner.rulesys.RuleDerivation)derivations.next();
                    if (seen.Contains(derivation))
                    {
                        strWriter.Write(new string(' ', margin));
                        strWriter.WriteLine(JESC("alreadyShown") + ":" + JESC(triple2Cnl(match, infGraph)));
                    }
                    else
                    {
                        seen.Add(derivation);
                        printTrace(strWriter, infGraph, derivation, margin, seen, alreadyPrinted);
                    }
                    addComma = true;
                }
            }
            strWriter.WriteLine("]}");
        }


        private string GetDeriv(org.apache.jena.util.iterator.ExtendedIterator iter1)
        {
            StringWriter wr = new StringWriter();
            while (iter1.hasNext())
            {
                var stmt = (org.apache.jena.graph.Triple)iter1.next();
                var iter2 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).getDerivation(stmt);
                while (iter2.hasNext())
                {
                    var deriv = (org.apache.jena.reasoner.rulesys.RuleDerivation)iter2.next();
                    printTrace(wr, ((org.apache.jena.reasoner.InfGraph)model.getGraph()), deriv, 0, new HashSet<org.apache.jena.reasoner.rulesys.RuleDerivation>(), new HashSet<string>());
                }
            }
            return wr.ToString();
        }

        public string GetIsADerivation(string C, string A)
        {
            var cls = model.getResource(transform.getIRIFromDL(C, EntityKind.Concept).toString());
            var inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
            var iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), org.apache.jena.vocabulary.RDF.Nodes.type, cls.asNode());
            return GetDeriv(iter1);
        }

        public string GetObjectPropertyDerivation(string A, string p, string B)
        {
            var inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
            var reln = model.getProperty(transform.getIRIFromDL(p, EntityKind.Role).toString());
            var jnst = model.getResource(transform.getIRIFromDL(B, EntityKind.Instance).toString());
            var iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), reln.asNode(), jnst.asNode());
            return GetDeriv(iter1);
        }

        public string GetSameAsDerivation(string A, string B)
        {
            var inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
            var jnst = model.getResource(transform.getIRIFromDL(B, EntityKind.Instance).toString());
            var iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), org.apache.jena.vocabulary.OWL.sameAs.asNode(), jnst.asNode());
            return GetDeriv(iter1);
        }

        public string GetDifferentThanDerivation(string A, string B)
        {
            var inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
            var jnst = model.getResource(transform.getIRIFromDL(B, EntityKind.Instance).toString());
            var iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), org.apache.jena.vocabulary.OWL.differentFrom.asNode(), jnst.asNode());
            return GetDeriv(iter1);
        }

        public string GetDataPropertyDerivation(string A, string p, Value V)
        {
            var inst = model.getResource(transform.getIRIFromDL(A, EntityKind.Instance).toString());
            var reln = model.getProperty(transform.getIRIFromDL(p, EntityKind.Role).toString());
            var dat = getLiteralVal(V).asResource();
            var iter1 = ((org.apache.jena.reasoner.InfGraph)model.getGraph()).find(inst.asNode(), reln.asNode(), dat.asNode());
            return GetDeriv(iter1);
        }

        public string Why(CNL.DL.Paragraph para)
        {
            DLModSimplifier simli = new DLModSimplifier();
            para = simli.Visit(para) as CNL.DL.Paragraph;


            if (para.Statements.Any((s) => !IsABox(s) && !(s is CNL.DL.Annotation)))
            {
                throw new NotImplementedException();
            }
            else
            {
                StringBuilder sb = new StringBuilder();
                foreach (var s in para.Statements)
                {
                    if (s is CNL.DL.InstanceOf)
                    {
                        var stmt = s as CNL.DL.InstanceOf;
                        var str = GetIsADerivation((stmt.C as CNL.DL.Atomic).id, (stmt.I as CNL.DL.NamedInstance).name);
                        sb.AppendLine(str);
                    }
                    else if (s is CNL.DL.RelatedInstances)
                    {
                        var stmt = s as CNL.DL.RelatedInstances;
                        var str = GetObjectPropertyDerivation((stmt.I as CNL.DL.NamedInstance).name, (stmt.R as CNL.DL.Atomic).id, (stmt.J as CNL.DL.NamedInstance).name);
                        sb.AppendLine(str);
                    }
                    else if (s is CNL.DL.InstanceValue)
                    {
                        var stmt = s as CNL.DL.InstanceValue;
                        var str = GetDataPropertyDerivation((stmt.I as CNL.DL.NamedInstance).name, (stmt.R as CNL.DL.Atomic).id, stmt.V);
                        sb.AppendLine(str);
                    }
                    else if (s is CNL.DL.SameInstances)
                    {
                        var stmt = s as CNL.DL.SameInstances;
                        if (stmt.Instances.Count == 2)
                        {
                            var str = GetSameAsDerivation((stmt.Instances[0] as CNL.DL.NamedInstance).name, (stmt.Instances[1] as CNL.DL.NamedInstance).name);
                            sb.AppendLine(str);
                        }
                        else
                            throw new NotImplementedException();
                    }
                    else if (s is CNL.DL.DifferentInstances)
                    {
                        var stmt = s as CNL.DL.DifferentInstances;
                        if (stmt.Instances.Count == 2)
                        {
                            var str = GetDifferentThanDerivation((stmt.Instances[0] as CNL.DL.NamedInstance).name, (stmt.Instances[1] as CNL.DL.NamedInstance).name);
                            sb.AppendLine(str);
                        }
                        else
                            throw new NotImplementedException();
                    }
                }
                return sb.ToString();
            }
        }


        dynamic rete_reasoner;

        public bool StatementIsValidInRLPlus(Statement statement)
        {
            var gen = new GenerateJenaRules(model, modalChecker, true, debugModeOn, exeRulesOn, debugModeOn);
            gen.setOWLDataFactory(ontologyBase, owlxmlFormat, CNL.EN.CNLFactory.lex);
            return gen.Validate(statement);
        }

        private CogniPy.CNL.DL.Serializer dlserializer = new CogniPy.CNL.DL.Serializer();

        private void MaterializeSROIQ(MatMode mode, bool isAboxInsertOnly)
        {
            if (!IsConsistent)
                throw new InconsistentOntologyException();

            model = null;

            var toRet = new CNL.DL.Paragraph(null) { Statements = new List<CNL.DL.Statement>() };

            dlserializer.Serialize(this.sourceParagraph);

            if (mode != MatMode.Tbox)
            {
                var insts = GetInstancesOf(new CNL.DL.Top(null), false);

                if (!isAboxInsertOnly)
                {
                    //calculate same-as instances
                    foreach (var ins in insts)
                    {
                        for (int i = 0; i < ins.Count; i++)
                            if (ins.Count > 1)
                                toRet.Statements.Add(new CNL.DL.SameInstances(null, new CNL.DL.InstanceList(null) { List = (from x in ins select new CNL.DL.NamedInstance(null) { name = x } as CNL.DL.Instance).ToList() }, CNL.DL.Statement.Modality.IS));
                    }

                    if (CALCULATE_DISJOINTS)
                    {
                        //calculate different instances
                        for (int i = 0; i < insts.Count - 1; i++)
                        {
                            for (int j = i + 1; j < insts.Count; j++)
                            {
                                if (!IsSatisfable(new CNL.DL.ConceptAnd(null,
                                    new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, new CNL.DL.NamedInstance(null) { name = insts[i].First() })),
                                    new CNL.DL.InstanceSet(null, new CNL.DL.InstanceList(null, new CNL.DL.NamedInstance(null) { name = insts[j].First() })))))
                                {
                                    toRet.Statements.Add(new CNL.DL.DifferentInstances(null,
                                        new CNL.DL.InstanceList(null)
                                        {
                                            List = new List<CNL.DL.Instance>(){
                                        new CNL.DL.NamedInstance(null) { name = insts[i].First() },
                                        new CNL.DL.NamedInstance(null) { name = insts[j].First() }}
                                        }, CNL.DL.Statement.Modality.IS));
                                }
                            }
                        }
                    }
                }

                {//calculate obj relations

                    var enums = GetInstancesOf(new CNL.DL.Atomic(null) { id = "enumeration" }, false);
                    var nonEnums = new List<List<string>>();

                    foreach (var i in insts)
                    {
                        bool found = false;
                        foreach (var e in enums)
                        {
                            if (e.Intersect(i).Count() > 0)
                            {
                                found = true;
                                break;
                            }
                        }
                        if (!found)
                            nonEnums.Add(i);
                    }

                    var rels = GetSubObjectPropertiesOf(new CNL.DL.Top(null), false, false);
                    foreach (var rel in rels)
                        foreach (var Is in nonEnums)
                            foreach (var Js in insts)
                                SetInstance(isAboxInsertOnly, toRet, Is, Js, rel);

                    if (!isAboxInsertOnly)
                        foreach (var rel in rels)
                            foreach (var Is in enums)
                                foreach (var Js in enums)
                                    SetInstance(isAboxInsertOnly, toRet, Is, Js, rel);

                    //dependent attributesrs from SWRL rules: the key:the given attrbute, value - all that are dependent on it
                    var depAttrs = dlserializer.GetDependentAttrs();

                    //explicitly specified instance-attribute-values
                    var explicitIV = dlserializer.GetInstanceValues();
                    foreach (var e in explicitIV)
                    {
                        if (isAboxInsertOnly)
                        {
                            bool isEnum = false;
                            foreach (var i in enums)
                            {
                                foreach (var ii in i)
                                    if (e.I is CNL.DL.NamedInstance && ((e.I as CNL.DL.NamedInstance).name == ii)) { isEnum = true; break; }
                                if (isEnum) break;
                            }
                            if (isEnum) continue;
                        }
                        SetDataPropsAndPropagetToDependentAttrs(isAboxInsertOnly, GetDataProps(depAttrs, (e.R as CNL.DL.Atomic).id), isAboxInsertOnly ? nonEnums : insts, toRet, e.V);
                    }

                    if (!isAboxInsertOnly)
                    {
                        var dataVals = dlserializer.GetDataValues();

                        foreach (var prts in dataVals)
                        {
                            CNL.DL.Value val = CNL.DL.Value.MakeFrom(prts.Item1, prts.Item3);
                            List<List<string>> dataprops = GetDataProps(depAttrs, prts.Item2);
                            SetDataPropsAndPropagetToDependentAttrs(isAboxInsertOnly, dataprops, insts, toRet, val);
                        }
                    }
                }
            }

            java.util.List gens = new java.util.ArrayList();
            java.util.List gens_struct = new java.util.ArrayList();

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
                    gens_struct.add(new InferredDisjointClassesAxiomGenerator());
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
                        gens.add(new InferredDisjointClassesAxiomGenerator());
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

            var conv = transform.Convert(toRet);
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.axioms));
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.additions));
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.hotfixes));

            var adds = transform.Convert(swrlRulesWIthBuiltInsParagraph);
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(adds.hotfixes));

        }

        List<Tuple<string, string, object>> AdditionalTriplets = new List<Tuple<string, string, object>>();

        public void SetValue(string instance, string datarole, object val)
        {
            AdditionalTriplets.Add(Tuple.Create(instance, datarole, val));
        }

        private void SetInstance(bool isAboxInsertOnly, CNL.DL.Paragraph toRet, IEnumerable<string> Is, IEnumerable<string> Js, IEnumerable<string> Rs)
        {
            var stmt = new CNL.DL.RelatedInstances(null) { I = new CNL.DL.NamedInstance(null) { name = Is.First() }, R = new CNL.DL.Atomic(null) { id = Rs.First() }, J = new CNL.DL.NamedInstance(null) { name = Js.First() }, modality = CNL.DL.Statement.Modality.IS };
            if (impliKBVis.IsEntailed(stmt) || IsEntailed(stmt))
            {
                foreach (var ii in Is)
                    foreach (var jj in Js)
                        foreach (var rr in Rs)
                        {
                            var s = new CNL.DL.RelatedInstances(null) { I = new CNL.DL.NamedInstance(null) { name = ii }, R = new CNL.DL.Atomic(null) { id = rr }, J = new CNL.DL.NamedInstance(null) { name = jj }, modality = CNL.DL.Statement.Modality.IS };
                            if (!isAboxInsertOnly || !impliKBVis.IsEntailed(s))
                                toRet.Statements.Add(s);
                        }
            }
        }

        private void SetDataPropsAndPropagetToDependentAttrs(bool isAboxInsertOnly, List<List<string>> dataprops, List<List<string>> insts, CNL.DL.Paragraph toRet, CNL.DL.Value val)
        {
            foreach (var dpl in dataprops)
            {
                var attrib = new CNL.DL.Atomic(null) { id = dpl.First() };
                for (int i = 0; i < insts.Count; i++)
                {
                    var stmt = new CNL.DL.InstanceValue(null) { I = new CNL.DL.NamedInstance(null) { name = insts[i].First() }, R = new CNL.DL.Atomic(null) { id = dpl.First() }, V = val, modality = CNL.DL.Statement.Modality.IS };
                    if (impliKBVis.IsEntailed(stmt) || IsEntailed(stmt))
                    {
                        foreach (var ii in insts[i])
                            foreach (var rr in dpl)
                            {
                                var s = new CNL.DL.InstanceValue(null) { I = new CNL.DL.NamedInstance(null) { name = ii }, R = new CNL.DL.Atomic(null) { id = rr }, V = val, modality = CNL.DL.Statement.Modality.IS };
                                if (!isAboxInsertOnly || !impliKBVis.IsEntailed(s))
                                    toRet.Statements.Add(s);
                            }
                    }
                }
            }
        }

        private List<List<string>> GetDataProps(Dictionary<string, HashSet<string>> depAttrs, string attr)
        {
            List<List<string>> dataprops = new List<List<string>>();

            //here we keep all crawled attributes
            var allProps = new HashSet<string>();

            //here we have the 
            var newProps = new Stack<string>();
            newProps.Push(attr);

            while (newProps.Count > 0)
            {
                while (newProps.Count > 0)
                {
                    var topP = newProps.Pop();

                    var attrib3 = new CNL.DL.Atomic(null) { id = topP };

                    var eqv = GetEquivalentDataPropertiesOf(attrib3, false);
                    eqv.Insert(0, attrib3.id);

                    var supers = GetSuperDataPropertiesOf(attrib3, false, false);
                    foreach (var sl in supers)
                        foreach (var s in sl)
                            eqv.Add(s);

                    foreach (var s in eqv)
                        allProps.Add(s);

                    dataprops.Add(eqv);
                }

                foreach (var dpl in dataprops)
                {
                    foreach (var dp in dpl)
                    {
                        if (depAttrs.ContainsKey(dp))
                        {
                            foreach (var p in depAttrs[dp])
                                if (!allProps.Contains(p))
                                    newProps.Push(p);
                        }
                    }
                }
            }
            return dataprops;
        }


        private string GetShortForm(OWLEntity entity)
        {
            return entity.getIRI().toString();
        }

        class BlindFactory : org.semanticweb.HermiT.Reasoner.ReasonerFactory
        {
            protected override OWLReasoner createHermiTOWLReasoner(org.semanticweb.HermiT.Configuration configuration, OWLOntology ontology)
            {
                configuration.throwInconsistentOntologyException = false;
                return new org.semanticweb.HermiT.Reasoner(configuration, ontology);
            }
        }

        class ExplanationsProgressMonitor : com.clarkparsia.owlapi.explanation.util.ExplanationProgressMonitor
        {
            public List<CogniPy.CNL.DL.Paragraph> aExplanations = new List<CogniPy.CNL.DL.Paragraph>();
            public Action<CogniPy.CNL.DL.Paragraph> foundExplanationAction;
            public Func<bool> isCancelledFunc;

            CogniPy.ARS.InvTransform invtransform;
            public ExplanationsProgressMonitor(CogniPy.ARS.InvTransform invtransform)
            {
                this.invtransform = invtransform;
            }

            public void foundAllExplanations()
            {

            }

            public void foundExplanation(java.util.Set explanation)
            {
                CogniPy.CNL.DL.Paragraph explanat = new CogniPy.CNL.DL.Paragraph(null) { Statements = new List<CogniPy.CNL.DL.Statement>() };
                foreach (var causingAxiom in explanation.toArray())
                {
                    explanat.Statements.Add(invtransform.Convert(causingAxiom as OWLAxiom));
                }
                foundExplanationAction(explanat);
                aExplanations.Add(explanat);
            }

            public bool isCancelled()
            {
                return isCancelledFunc();
            }
        }

        public List<CogniPy.CNL.DL.Paragraph> GetExplanations(Action<CogniPy.CNL.DL.Paragraph> foundExplanationAction, Func<bool> isCancelledFunc)
        {
            rationals.Automaton a = new rationals.Automaton();

            var manager = OWLManager.createOWLOntologyManager();
            var ontology = manager.createOntology(ontologyIRI);
            var conv = transform.Convert(sourceParagraph);
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.axioms));
            manager.addAxioms(ontology, CogniPy.ARS.Transform.GetJavaAxiomSet(conv.additions));

            var bf = new BlindFactory();


            var explProgMon = new ExplanationsProgressMonitor(invtransform);
            explProgMon.foundExplanationAction = foundExplanationAction;
            explProgMon.isCancelledFunc = isCancelledFunc;

            var multExplanator = new DefaultExplanationGenerator(manager, bf, ontology, explProgMon);

            var explanations = multExplanator.getExplanations(manager.getOWLDataFactory().getOWLThing());

            if (explanations.isEmpty())
            {
                CogniPy.CNL.DL.Paragraph explanat = new CogniPy.CNL.DL.Paragraph(null) { Statements = new List<CogniPy.CNL.DL.Statement>() };
                foreach (var causingAxiom in (ontology.getAxioms() as java.util.Set).toArray())
                {
                    explanat.Statements.Add(invtransform.Convert(causingAxiom as OWLAxiom));
                }
                foundExplanationAction(explanat);
                explProgMon.aExplanations.Add(explanat);
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

        public bool IsConsistent
        {
            get
            {
                return reasoner.isConsistent();
            }
        }

        public List<List<string>> GetObjectPropertyRanges(CogniPy.CNL.DL.Node role, bool includeTopBot = false)
        {
            return GetObjectDomainRange(reasoner.getObjectPropertyRanges, role, includeTopBot);
        }

        public List<List<string>> GetObjectPropertyDomains(CogniPy.CNL.DL.Node role, bool includeTopBot = false)
        {
            return GetObjectDomainRange(reasoner.getObjectPropertyDomains, role, includeTopBot);
        }

        private List<List<string>> GetObjectDomainRange(Func<OWLObjectPropertyExpression, bool, NodeSet> func, CogniPy.CNL.DL.Node role, bool includeTopBot)
        {
            List<List<string>> x = new List<List<string>>();
            var ret = func(transform.GetObjectProperty(role), false);
            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                OWLClassNode cls = it.next() as OWLClassNode;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLClassExpression;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else if (nod.isClassExpressionLiteral())
                    {
                        y.Add(invtransform.renderEntity(nod as OWLClass, ARS.EntityKind.Concept));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }

            return x;
        }


        public List<List<string>> GetDataPropertyDomains(CogniPy.CNL.DL.Node role, bool includeTopBot = false)
        {
            return GetDataDomain(reasoner.getDataPropertyDomains, role, includeTopBot);
        }

        private List<List<string>> GetDataDomain(Func<OWLDataProperty, bool, NodeSet> func, CogniPy.CNL.DL.Node role, bool includeTopBot)
        {
            List<List<string>> x = new List<List<string>>();
            var ret = func(transform.GetDataProperty(role), false);
            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                OWLClassNode cls = it.next() as OWLClassNode;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLClassExpression;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else if (nod.isClassExpressionLiteral())
                    {
                        y.Add(invtransform.renderEntity(nod as OWLClass, ARS.EntityKind.Concept));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }

            return x;
        }



        public List<List<string>> GetInstancesOf(CogniPy.CNL.DL.Node e, bool direct)
        {
            List<List<string>> x = new List<List<string>>();
            var ret = reasoner.getInstances(transform.Convert(e).Key, direct);
            for (var it = ret.iterator(); it.hasNext();)
            {
                OWLNamedIndividualNode ind = it.next() as OWLNamedIndividualNode;
                List<string> y = new List<string>();
                var s2 = ind.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLNamedIndividual;
                    y.Add(invtransform.renderEntity(nod, ARS.EntityKind.Instance));
                }
                x.Add(y);
            }

            return x;
        }

        public List<string> GetDataPropertyValues(string instance)
        {
            var res = new List<string>();
            OWLNamedIndividual individual = transform.GetNamedIndividual(instance);
            var x = individual.getDataPropertyValues(ontology);
            for (var it = x.keySet().iterator(); it.hasNext();)
            {
                var ind = it.next() as Map.Entry;//OWLDataPropertyExpression;
                res.Add(invtransform.renderEntity(ind as OWLClass, ARS.EntityKind.DataRole));
            }
            return res;
        }


        public List<List<string>> GetRelatedInstances(string instance, CogniPy.CNL.DL.Node r)
        {
            var x = new List<List<string>>();
            var ret = reasoner.getObjectPropertyValues(transform.GetNamedIndividual(instance), transform.GetObjectProperty(r));
            for (var it = ret.iterator(); it.hasNext();)
            {
                var ind = it.next() as OWLNamedIndividualNode;
                var y = new List<string>();
                var s2 = ind.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLNamedIndividual;
                    y.Add(invtransform.renderEntity(nod, ARS.EntityKind.Instance));
                }
                x.Add(y);
            }

            return x;
        }

        public List<string> GetDataPropertyValues(string instance, CogniPy.CNL.DL.Node r)
        {
            var x = new List<string>();
            var ret = getSupprtingReasoner().getDataPropertyValues(transform.GetNamedIndividual(instance), transform.GetDataProperty(r));
            for (var it = ret.iterator(); it.hasNext();)
            {
                var y = (it.next() as org.semanticweb.owlapi.model.OWLLiteral);
                if (y != null)
                {
                    x.Add(y.getLiteral());
                }
                //x.Add(y);
            }

            return x;
        }

        public List<List<string>> GetSubConcepts(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
        {
            List<List<string>> x = new List<List<string>>();
            var ret = reasoner.getSubClasses(transform.Convert(e).Key, direct);
            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                OWLClassNode cls = it.next() as OWLClassNode;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLClassExpression;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else if (nod.isClassExpressionLiteral())
                    {
                        y.Add(invtransform.renderEntity(nod as OWLClass, ARS.EntityKind.Concept));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }
            return x;
        }

        OWLReasoner getSupprtingReasoner()
        {
            if (structural_reasoner != null && !materializing_reasner_supports_sroiq)
                return structural_reasoner;
            else
                return this.reasoner;
        }

        public List<List<string>> GetSubObjectPropertiesOf(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
        {

            List<List<string>> x = new List<List<string>>();
            var ret = getSupprtingReasoner().getSubObjectProperties(transform.GetObjectProperty(e), direct);
            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                var clsc = it.next();
                var cls = clsc as OWLObjectPropertyNode;
                if (cls == null)
                    continue;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod2 = it2.next();
                    var nod = nod2 as OWLObjectPropertyExpression;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else if (nod2 is OWLObjectProperty)
                    {
                        y.Add(invtransform.renderEntity(nod2 as OWLObjectProperty, ARS.EntityKind.Role));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }
            return x;
        }

        public List<List<string>> GetSubDataPropertiesOf(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
        {
            List<List<string>> x = new List<List<string>>();
            var ret = getSupprtingReasoner().getSubDataProperties(transform.GetDataProperty(e), direct);
            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                var cls = it.next() as OWLDataPropertyNode;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLDataProperty;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else
                    {
                        y.Add(invtransform.renderEntity(nod, ARS.EntityKind.DataRole));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }
            return x;
        }

        public List<List<string>> GetSuperDataPropertiesOf(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
        {
            List<List<string>> x = new List<List<string>>();
            var ret = getSupprtingReasoner().getSuperDataProperties(transform.GetDataProperty(e), direct);
            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                var cls = it.next() as OWLDataPropertyNode;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLDataProperty;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else
                    {
                        y.Add(invtransform.renderEntity(nod, ARS.EntityKind.DataRole));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }
            return x;
        }


        public bool IsTrue(CogniPy.CNL.DL.Paragraph stmt)
        {
            var ecls = transform.Convert(stmt).axioms.First().axiom;
            return reasoner.isEntailed(ecls);
        }

        public List<string> GetEquivalentConcepts(CogniPy.CNL.DL.Node e, bool includeTopBot = true)
        {
            var ecls = transform.Convert(e).Key;
            string thisNode = "";
            if (ecls is OWLClass)
                thisNode = invtransform.renderEntity(ecls as OWLClass, ARS.EntityKind.Concept);

            OWLClassNode ret = reasoner.getEquivalentClasses(ecls) as OWLClassNode;
            List<string> y = new List<string>();
            for (var it2 = ret.iterator(); it2.hasNext();)
            {
                var nod = it2.next() as OWLClassExpression;
                if (nod.isBottomEntity())
                {
                    if (includeTopBot)
                        y.Add("⊥");
                }
                else if (nod.isTopEntity())
                {
                    if (includeTopBot)
                        y.Add("⊤");
                }
                else if (nod.isClassExpressionLiteral())
                {
                    var entitStr = invtransform.renderEntity(nod as OWLClass, ARS.EntityKind.Concept);
                    if (entitStr.CompareTo(thisNode) != 0)
                        y.Add(entitStr);
                }
            }
            return y;
        }

        public List<string> GetEquivalentDataPropertiesOf(CogniPy.CNL.DL.Node e, bool includeTopBot = true)
        {
            var ecls = transform.GetDataProperty(e);
            string thisNode = invtransform.renderEntity(ecls, ARS.EntityKind.DataRole);

            List<string> y = new List<string>();
            var ret = reasoner.getEquivalentDataProperties(ecls).getEntities();
            for (var it2 = ret.iterator(); it2.hasNext();)
            {
                var nod = it2.next() as OWLDataProperty;
                if (nod.isBottomEntity())
                {
                    if (includeTopBot)
                        y.Add("⊥");
                }
                else if (nod.isTopEntity())
                {
                    if (includeTopBot)
                        y.Add("⊤");
                }
                else
                {
                    var entitStr = invtransform.renderEntity(nod, ARS.EntityKind.DataRole);
                    if (entitStr.CompareTo(thisNode) != 0)
                        y.Add(entitStr);
                }
            }
            return y;
        }


        public List<List<string>> GetSuperConcepts(CogniPy.CNL.DL.Node e, bool direct, bool includeTopBot = true)
        {
            List<List<string>> x = new List<List<string>>();
            NodeSet ret;
            if (e is InstanceSet)
                ret = reasoner.getTypes(transform.GetNamedIndividual(((NamedInstance)((InstanceSet)e).Instances[0]).name), direct);
            else
                ret = reasoner.getSuperClasses(transform.Convert(e).Key, direct);

            for (var it = ret.iterator(); it.hasNext();)
            {
                List<string> y = new List<string>();

                OWLClassNode cls = it.next() as OWLClassNode;
                var s2 = cls.getEntities();
                for (var it2 = s2.iterator(); it2.hasNext();)
                {
                    var nod = it2.next() as OWLClassExpression;
                    if (nod.isBottomEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊥");
                    }
                    else if (nod.isTopEntity())
                    {
                        if (includeTopBot)
                            y.Add("⊤");
                    }
                    else if (nod.isClassExpressionLiteral())
                    {
                        y.Add(invtransform.renderEntity(nod as OWLClass, ARS.EntityKind.Concept));
                    }
                }
                if (y.Count > 0)
                    x.Add(y);
            }
            return x;
        }

        public bool IsSatisfable(CogniPy.CNL.DL.Node C)
        {
            var expr = transform.Convert(C).Key;
            return reasoner.isSatisfiable(expr);
        }

        public bool IsEntailed(CogniPy.CNL.DL.Statement S)
        {
            var stat = transform.Convert(new CNL.DL.Paragraph(null, S));
            java.util.HashSet axioms = new java.util.HashSet();
            foreach (var ax in stat.axioms)
                axioms.add(ax.axiom);
            foreach (var ax in stat.additions)
                axioms.add(ax);
            return reasoner.isEntailed(axioms);
        }

        private bool SimpleModalCheck(CogniPy.CNL.DL.Node C, CogniPy.CNL.DL.Node D, out List<List<string>> okOnInstances, out List<List<string>> errsOnInstances)
        {
            errsOnInstances = new List<List<string>>();
            okOnInstances = new List<List<string>>();

            var Ainst = reasoner.getInstances(transform.Convert(C).Key, false);
            var Aiter = Ainst.iterator();

            while (Aiter.hasNext())
            {
                var a = Aiter.next() as org.semanticweb.owlapi.reasoner.Node;
                var aiter = a.iterator();
                if (aiter.hasNext())
                {
                    var i = aiter.next() as OWLNamedIndividual;
                    if (i.getIRI().toString().EndsWith("_uUu_"))
                        continue;

                    var oneof = manager.getOWLDataFactory().getOWLObjectOneOf(i);

                    java.util.Set clss2 = new java.util.HashSet();
                    clss2.add(oneof);
                    clss2.add(manager.getOWLDataFactory().getOWLObjectComplementOf(transform.Convert(D).Key));
                    OWLClassExpression expr2 = manager.getOWLDataFactory().getOWLObjectIntersectionOf(clss2);
                    bool isError = reasoner.isSatisfiable(expr2);
                    var erri = new List<string>();

                    var a2iter = a.iterator();
                    while (a2iter.hasNext())
                    {
                        var i2 = a2iter.next() as OWLNamedIndividual;
                        var ins = new CNL.DL.DlName() { id = invtransform.renderEntity(i2, ARS.EntityKind.Instance) };
                        erri.Add(ins.id);
                    }
                    if (isError)
                        errsOnInstances.Add(erri);
                    else
                        okOnInstances.Add(erri);
                }
            }
            return errsOnInstances.Count == 0;
        }

        public enum ModalityCheckResult { Ok, Error, Warning, Hint }

        public ModalityCheckResult CheckModality(CogniPy.CNL.DL.Statement stmt, out List<List<string>> okOnInstances, out List<List<string>> errsOnInstances)
        {
            errsOnInstances = null;

            ModalityCheckResult onError = ModalityCheckResult.Ok;
            bool negate = false;
            switch (stmt.modality)
            {
                case CNL.DL.Statement.Modality.MUST:
                    onError = ModalityCheckResult.Error; break;
                case CNL.DL.Statement.Modality.SHOULD:
                    onError = ModalityCheckResult.Warning; break;
                case CNL.DL.Statement.Modality.CAN:
                    onError = ModalityCheckResult.Hint; break;
                case CNL.DL.Statement.Modality.CANNOT:
                    onError = ModalityCheckResult.Error; negate = true; break;
                case CNL.DL.Statement.Modality.SHOULDNOT:
                    onError = ModalityCheckResult.Warning; negate = true; break;
                case CNL.DL.Statement.Modality.MUSTNOT:
                    onError = ModalityCheckResult.Hint; negate = true; break;
            }

            if (stmt is CogniPy.CNL.DL.Subsumption)
            {
                if (!negate)
                    return SimpleModalCheck((stmt as CogniPy.CNL.DL.Subsumption).C, (stmt as CogniPy.CNL.DL.Subsumption).D, out okOnInstances, out errsOnInstances) ? ModalityCheckResult.Ok : onError;
                else
                    return SimpleModalCheck((stmt as CogniPy.CNL.DL.Subsumption).C, (stmt as CogniPy.CNL.DL.Subsumption).D, out errsOnInstances, out okOnInstances) ? onError : ModalityCheckResult.Ok;
            }
            else if (stmt is CogniPy.CNL.DL.Equivalence)
            {
                List<List<string>> errsOnInstances2;
                List<List<string>> okOnInstances2;
                bool c1;
                bool c2;
                if (!negate)
                {
                    c1 = SimpleModalCheck((stmt as CogniPy.CNL.DL.Equivalence).Equivalents[0], (stmt as CogniPy.CNL.DL.Equivalence).Equivalents[1], out okOnInstances, out errsOnInstances);
                    c2 = SimpleModalCheck((stmt as CogniPy.CNL.DL.Equivalence).Equivalents[1], (stmt as CogniPy.CNL.DL.Equivalence).Equivalents[0], out okOnInstances2, out errsOnInstances2);
                }
                else
                {
                    c1 = !SimpleModalCheck((stmt as CogniPy.CNL.DL.Equivalence).Equivalents[0], (stmt as CogniPy.CNL.DL.Equivalence).Equivalents[1], out errsOnInstances, out okOnInstances);
                    c2 = !SimpleModalCheck((stmt as CogniPy.CNL.DL.Equivalence).Equivalents[1], (stmt as CogniPy.CNL.DL.Equivalence).Equivalents[0], out errsOnInstances2, out okOnInstances2);
                }
                if (c1 && c2)
                    return ModalityCheckResult.Ok;
                else
                {
                    errsOnInstances.AddRange(errsOnInstances2);
                    okOnInstances.AddRange(okOnInstances2);
                    return onError;
                }
            }
            else
            {
                throw new NotImplementedException();
            }
        }

        private bool IsABox(CNL.DL.Statement stmt)
        {
            return ((stmt is CNL.DL.InstanceOf) && (((stmt as CNL.DL.InstanceOf).C is CNL.DL.Atomic) || ((stmt as CNL.DL.InstanceOf).C is CNL.DL.Top))) || (stmt is CNL.DL.RelatedInstances) || (stmt is CNL.DL.InstanceValue) || (stmt is CNL.DL.SameInstances) || (stmt is CNL.DL.DifferentInstances);
        }

        void AddIfNotExistsRemoveIfExists(bool isAdd, org.apache.jena.rdf.model.Model model2, org.apache.jena.rdf.model.Resource s, org.apache.jena.rdf.model.Property v, org.apache.jena.rdf.model.RDFNode o)
        {
            var t = model2.createStatement(s, v, o);
            if (isAdd)
            {
                if (!model2.contains(t))
                {
                    model2.add(t);
                    sourceTriplets.Add(t.asTriple());
                }
            }
            else
            {
                if (model2.contains(t))
                {
                    model2.remove(t);
                    sourceTriplets.Remove(t.asTriple());
                }
            }
        }


        static Regex DtmRg = new Regex(@"(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?))?", RegexOptions.Compiled);
        static string completeDTMVal(string val)
        {
            var m = DtmRg.Match(val);
            var dta = m.Groups["date"].Value;
            var tm = m.Groups["time"].Value;
            StringBuilder sb = new StringBuilder();
            sb.Append(dta);
            if (string.IsNullOrEmpty(tm))
                sb.Append("T00:00:00");
            else
                sb.Append(tm);
            if (tm.Length == "T00:00".Length)
                sb.Append(":00");
            return sb.ToString();
        }

        org.apache.jena.rdf.model.Literal getLiteralVal(Value v)
        {
            if (model == null) return null;

            if (v is CNL.DL.Bool) return model.createTypedLiteral(v.ToBool(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDboolean);
            if (v is CNL.DL.String) return model.createTypedLiteral(v.ToString(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
            if (v is CNL.DL.Float) return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdouble);
            if (v is CNL.DL.Number) return model.createTypedLiteral(new java.lang.Integer(v.ToInt()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger);
            if (v is CNL.DL.DateTimeVal) return model.createTypedLiteral(completeDTMVal(v.ToStringExact()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdateTime);
            if (v is CNL.DL.Duration) return model.createTypedLiteral(v.ToStringExact(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDduration);

            return model.createTypedLiteral(v.ToString()); //TODO xsd:date i inne typy
        }

        public void MergeWith(HermiTReasoningService x)
        {
            model = org.apache.jena.rdf.model.ModelFactory.createUnion(model, x.model);
        }

        public List<string> GetSubConceptsOfFromModelFast(CNL.DL.Atomic a)
        {
            BuildModel();
            string ths = transform.getIRIFromDL(a.id, EntityKind.Concept).toString();
            var cls = model.getResource(ths);
            var triples = model.getGraph().find(null, org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), cls.asNode());

            List<string> ret = new List<string>();
            while (triples.hasNext())
            {
                var trp = (org.apache.jena.graph.Triple)triples.next();
                var sub = trp.getSubject();
                if (sub.isBlank())
                    continue;

                var vra = sub.toString();
                if (vra != ths && vra != org.apache.jena.vocabulary.OWL.Thing.toString() && vra != org.apache.jena.vocabulary.OWL.Nothing.toString() && vra != org.apache.jena.vocabulary.OWL2.NamedIndividual.toString())
                    ret.Add(vra);
            }
            return ret;
        }

        public List<string> GetSuperConceptsOfFromModelFast(CNL.DL.Node a)
        {
            BuildModel();
            org.apache.jena.util.iterator.ExtendedIterator triples;
            string ths;
            if (a is CNL.DL.Atomic)
            {
                ths = transform.getIRIFromDL((a as CNL.DL.Atomic).id, EntityKind.Concept).toString();
                var cls = model.getResource(ths);
                triples = model.getGraph().find(cls.asNode(), org.apache.jena.vocabulary.RDFS.subClassOf.asNode(), null);
            }
            else if (a is CNL.DL.InstanceSet)
            {
                ths = transform.getIRIFromDL(((a as CNL.DL.InstanceSet).Instances[0] as CNL.DL.NamedInstance).name, EntityKind.Instance).toString();
                var i = model.getResource(ths);
                triples = model.getGraph().find(i.asNode(), org.apache.jena.vocabulary.RDF.type.asNode(), null);
            }
            else
                throw new InvalidOperationException();

            List<string> ret = new List<string>();
            while (triples.hasNext())
            {
                var trp = (org.apache.jena.graph.Triple)triples.next();
                var obj = trp.getObject();
                if (obj.isBlank())
                    continue;
                var vra = obj.toString();
                if (vra != ths && vra != org.apache.jena.vocabulary.OWL.Thing.toString() && vra != org.apache.jena.vocabulary.OWL.Nothing.toString() && vra != org.apache.jena.vocabulary.OWL2.NamedIndividual.toString())
                    ret.Add(vra);
            }
            return ret;
        }

        public List<string> GetInstancesOfFromModelFast(CNL.DL.Node a)
        {
            BuildModel();
            org.apache.jena.util.iterator.ExtendedIterator triples;
            string me = "";
            if (a is CNL.DL.Atomic)
            {
                var cls = model.getResource(transform.getIRIFromDL((a as CNL.DL.Atomic).id, EntityKind.Concept).toString());
                triples = model.getGraph().find(null, org.apache.jena.vocabulary.RDF.type.asNode(), cls.asNode());
            }
            else if (a is CNL.DL.InstanceSet)
            {
                var i = model.getResource(transform.getIRIFromDL(((a as CNL.DL.InstanceSet).Instances[0] as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
                me = i.toString();
                triples = model.getGraph().find(null, org.apache.jena.vocabulary.OWL.sameAs.asNode(), i.asNode());
            }
            else
                throw new InvalidOperationException();

            List<string> ret = new List<string>();
            while (triples.hasNext())
            {
                var trp = (org.apache.jena.graph.Triple)triples.next();
                var sub = trp.getSubject();
                if (sub.isBlank())
                    continue;
                var vra = sub.toString();
                ret.Add(vra);
            }
            return ret;
        }

        public List<Tuple<string, object>> GetInstancesOfFromModelFastURI(object a)
        {
            BuildModel();
            org.apache.jena.util.iterator.ExtendedIterator triples;
            string me = "";
            object meU = null;
            if (a is CNL.DL.Atomic)
            {
                var cls = model.getResource(transform.getIRIFromDL((a as CNL.DL.Atomic).id, EntityKind.Concept).toString());
                triples = model.getGraph().find(null, org.apache.jena.vocabulary.RDF.type.asNode(), cls.asNode());
            }
            else if (a is CNL.DL.NamedInstance)
            {
                var i = model.getResource(transform.getIRIFromDL((a as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
                me = i.toString();
                meU = i;
                triples = model.getGraph().find(null, org.apache.jena.vocabulary.OWL.sameAs.asNode(), i.asNode());
            }
            else if (a is CNL.DL.InstanceSet)
            {
                var set = a as CNL.DL.InstanceSet;
                if (set.Instances.Count == 1)
                    return GetInstancesOfFromModelFastURI(set.Instances[0]);
                else
                {
                    var ret2 = new List<Tuple<string, object>>();
                    foreach (var i in set.Instances)
                        ret2.AddRange(GetInstancesOfFromModelFastURI(i));
                    return ret2;
                }
            }
            else
                throw new InvalidOperationException();

            var ret = new List<Tuple<string, object>>();
            while (triples.hasNext())
            {
                var trp = (org.apache.jena.graph.Triple)triples.next();
                var sub = trp.getSubject();
                if (sub.isBlank())
                    continue;
                var vra = sub.toString();
                ret.Add(Tuple.Create(vra, (object)sub));
            }
            return ret;
        }

        public List<Tuple<bool, string, object>> GetAllPriopertiesFastFromURI(object nod)
        {
            org.apache.jena.util.iterator.ExtendedIterator triples;
            var node = nod as org.apache.jena.graph.Node;
            var G = model.getGraph();
            triples = G.find(node, null, null);

            var ret = new List<Tuple<bool, string, object>>();
            while (triples.hasNext())
            {
                var trp = (org.apache.jena.graph.Triple)triples.next();
                var prp = trp.getPredicate();
                var val = trp.getObject();
                if (G.contains(prp, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL.ObjectProperty.asNode()))
                {
                    ret.Add(Tuple.Create(true, prp.toString(), (object)val.toString()));
                }
                else if (G.contains(prp, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL.DatatypeProperty.asNode()))
                {
                    if (val.isLiteral())
                    {
                        var v = CogniPy.SPARQL.SparqlNode.ToTypedValue(val.ToString());
                        if (v == null)
                            v = JenaRuleManager.getObject(val);
                        ret.Add(Tuple.Create(false, prp.toString(), v));
                    }
                }
            }
            return ret;
        }

        public void AddRemoveKnowledge(CNL.DL.Paragraph para, bool isAdd, bool swrlOnly = false)
        {
            BuildModel();
            InvalidateSyncOntologyToModel();
            DLModSimplifier simli = new DLModSimplifier();
            para = simli.Visit(para) as CNL.DL.Paragraph;


            if (para.Statements.Any((s) => !IsABox(s) && !(s is CNL.DL.Annotation)))
            {
                throw new NotImplementedException("Only A-Box can be modified.");
            }
            else
            {
                if (!isAdd)
                {
                    lock (sourceParagraph)
                    {
                        HashSet<string> toRemoveStmts = new HashSet<string>(from s in para.Statements select Splitting.DLToys.MakeExpressionFromStatement(s));
                        sourceParagraph.Statements.RemoveAll((a) => toRemoveStmts.Contains(Splitting.DLToys.MakeExpressionFromStatement(a)));
                    }
                }
                foreach (var s in para.Statements)
                {
                    if (s is CNL.DL.InstanceOf)
                    {
                        var stmt = s as CNL.DL.InstanceOf;
                        var inst = model.getResource(transform.getIRIFromDL((stmt.I as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());

                        org.apache.jena.rdf.model.Resource clsn;
                        if (stmt.C is CNL.DL.Top)
                            clsn = org.apache.jena.vocabulary.OWL2.Thing;
                        else
                            clsn = model.getResource(transform.getIRIFromDL((stmt.C as CNL.DL.Atomic).id, EntityKind.Concept).toString());

                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, clsn);
                        if (isAdd && !swrlOnly)
                        {
                            AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
                            AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
                            AddIfNotExistsRemoveIfExists(isAdd, model, clsn, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Class);
                        }
                    }
                    else if (s is CNL.DL.RelatedInstances)
                    {
                        var stmt = s as CNL.DL.RelatedInstances;
                        var inst = model.getResource(transform.getIRIFromDL((stmt.I as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
                        var reln = model.getProperty(transform.getIRIFromDL((stmt.R as CNL.DL.Atomic).id, EntityKind.Role).toString());
                        var jnst = model.getResource(transform.getIRIFromDL((stmt.J as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
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
                    else if (s is CNL.DL.InstanceValue)
                    {
                        var stmt = s as CNL.DL.InstanceValue;
                        var inst = model.getResource(transform.getIRIFromDL((stmt.I as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
                        var reln = model.getProperty(transform.getIRIFromDL((stmt.R as CNL.DL.Atomic).id, EntityKind.DataRole).toString());
                        var node = model.getRDFNode(getLiteralVal(stmt.V).asNode());
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, reln, node);
                        if (isAdd && !swrlOnly)
                        {
                            AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
                            AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
                            AddIfNotExistsRemoveIfExists(isAdd, model, reln, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty);
                        }
                    }
                    else if (s is CNL.DL.SameInstances)
                    {
                        var stmt = s as CNL.DL.SameInstances;
                        if (stmt.Instances.Count == 2)
                        {
                            var inst = model.getResource(transform.getIRIFromDL((stmt.Instances[0] as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
                            var inst2 = model.getResource(transform.getIRIFromDL((stmt.Instances[1] as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
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
                            throw new NotImplementedException();
                    }
                    else if (s is CNL.DL.DifferentInstances)
                    {
                        var stmt = s as CNL.DL.DifferentInstances;
                        if (stmt.Instances.Count == 2)
                        {
                            var inst = model.getResource(transform.getIRIFromDL((stmt.Instances[0] as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
                            var inst2 = model.getResource(transform.getIRIFromDL((stmt.Instances[1] as CNL.DL.NamedInstance).name, EntityKind.Instance).toString());
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
                            throw new NotImplementedException();
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


        public void RemoveRdfInstance(org.apache.jena.rdf.model.Model model2, org.apache.jena.rdf.model.Resource s)
        {
            model2.removeAll(s, null, null);
            model2.removeAll(null, null, s);
            var sur = s.getURI();
            sourceTriplets.RemoveWhere((t) => t.getObject().getURI() == sur || t.getSubject().getURI() == sur);
        }

        public void RemoveInstance(string name)
        {
            BuildModel();
            InvalidateSyncOntologyToModel();
            var inst = model.getResource(transform.getIRIFromDL(name, EntityKind.Instance).toString());
            RemoveRdfInstance(model, inst);
        }

        public void AddRemoveAssertions(IEnumerable<Tuple<string, string, string, object>> assertions, bool isAdd, bool swrlOnly)
        {
            BuildModel();
            InvalidateSyncOntologyToModel();

            foreach (var a in assertions)
                if (a.Item1 == "type")
                {
                    var inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
                    var clsn = model.getResource(transform.getIRIFromDL(a.Item4.ToString(), EntityKind.Concept).toString());
                    AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, clsn);
                    if (isAdd && !swrlOnly)
                    {
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
                        AddIfNotExistsRemoveIfExists(isAdd, model, clsn, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Class);
                    }
                }
                else if (a.Item1 == "==" || a.Item1 == "=" || a.Item1 == "!=" || a.Item1 == "<>")
                {
                    var inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
                    var inst2 = model.getResource(transform.getIRIFromDL(a.Item4.ToString(), EntityKind.Instance).toString());
                    AddIfNotExistsRemoveIfExists(isAdd, model, inst, a.Item1.StartsWith("=") ? org.apache.jena.vocabulary.OWL.sameAs : org.apache.jena.vocabulary.OWL.differentFrom, inst2);
                    if (isAdd && !swrlOnly)
                    {
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst2, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
                    }
                }
                else if (a.Item1 == "R")
                {
                    var inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
                    var reln = model.getProperty(transform.getIRIFromDL(a.Item3, EntityKind.Role).toString());
                    var jnst = model.getResource(transform.getIRIFromDL(a.Item4.ToString(), EntityKind.Instance).toString());
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
                else if (a.Item1 == "D")
                {
                    var inst = model.getResource(transform.getIRIFromDL(a.Item2, EntityKind.Instance).toString());
                    var reln = model.getProperty(transform.getIRIFromDL(a.Item3, EntityKind.DataRole).toString());
                    var node = model.getRDFNode(getLiteralVal(CNL.DL.Value.FromObject(a.Item4)).asNode());
                    AddIfNotExistsRemoveIfExists(isAdd, model, inst, reln, node);
                    if (isAdd && !swrlOnly)
                    {
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.Thing);
                        AddIfNotExistsRemoveIfExists(isAdd, model, inst, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.NamedIndividual);
                        AddIfNotExistsRemoveIfExists(isAdd, model, reln, org.apache.jena.vocabulary.RDF.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty);
                    }
                }
                else
                    throw new InvalidOperationException("unknow type of assertion");
        }

    }
}
