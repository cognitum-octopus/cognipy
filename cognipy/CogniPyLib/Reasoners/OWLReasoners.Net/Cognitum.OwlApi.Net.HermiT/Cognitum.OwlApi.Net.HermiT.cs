using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Cognitum.OwlApi.Net.HermiT
{

    public class NetReasonerFactoryImpl : Cognitum.OwlApi.Net.NetReasonerFactory
    {
        public NetReasonerFactoryImpl()
        {
            //Assembly assReasoner = Assembly.LoadFrom(@"Reasoners\HermiT\hermit.dll");
        }

        private ReasonerProgressMonitor progrMonitor = new NullReasonerProgressMonitor();
        public void SetProgrMonitor(ReasonerProgressMonitor progrMonitorExt){
            progrMonitor = progrMonitorExt; 
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology)
        {
            var config = new org.semanticweb.HermiT.Configuration();
            config.reasonerProgressMonitor = progrMonitor;
            config.throwInconsistentOntologyException = false;

            return new org.semanticweb.HermiT.Reasoner(config, ontology);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
        {
            org.semanticweb.HermiT.Configuration configuration = new org.semanticweb.HermiT.Configuration();
            configuration.reasonerProgressMonitor = config.getProgressMonitor();
            configuration.throwInconsistentOntologyException = false;

            return new org.semanticweb.HermiT.Reasoner(configuration, ontology);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology)
        {
            var config = new org.semanticweb.HermiT.Configuration();
            config.reasonerProgressMonitor = progrMonitor;
            config.throwInconsistentOntologyException = false;

            return new org.semanticweb.HermiT.Reasoner(config, ontology);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology, org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration config)
        {
            org.semanticweb.HermiT.Configuration configuration = new org.semanticweb.HermiT.Configuration();
            configuration.reasonerProgressMonitor = config.getProgressMonitor();
            configuration.throwInconsistentOntologyException = false;

            return new org.semanticweb.HermiT.Reasoner(configuration, ontology);
        }

        public string getReasonerName()
        {
            OWLOntologyManager owlMan = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
            OWLOntology ont = owlMan.createOntology();
            OWLReasoner reas = this.createReasoner(ont);
            return reas.getReasonerName();
        }

        public string getReasonerDescription()
        {
            return @"HermiT is reasoner for ontologies written using the Web Ontology Language (OWL). 
Given an OWL file, HermiT can determine whether or not the ontology is consistent, identify subsumption relationships between classes, and much more.
HermiT is the first publicly-available OWL reasoner based on a novel “hypertableau” calculus which provides much more efficient reasoning than any previously-known algorithm. 
Ontologies which previously required minutes or hours to classify can often by classified in seconds by HermiT, 
and HermiT is the first reasoner able to classify a number of ontologies which had previously proven too complex for any available system to handle.
HermiT uses direct semantics and passes all OWL 2 conformance tests for direct semantics reasoners.";
        }

        public string getReasonerVersion()
        {
            OWLOntologyManager owlMan = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
            OWLOntology ont = owlMan.createOntology();
            OWLReasoner reas = this.createReasoner(ont);
            org.semanticweb.owlapi.util.Version ver = reas.getReasonerVersion();
            return ver.getMajor()+"."+ver.getMinor()+"."+ver.getPatch();
        }
    }
}
