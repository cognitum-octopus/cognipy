using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;
using uk.ac.manchester.cs.jfact;

namespace Cognitum.OwlApi.Net.JFact
{
    public class NetReasonerFactoryImpl : Cognitum.OwlApi.Net.NetReasonerFactory
    {
        public NetReasonerFactoryImpl()
        {
        }

        private ReasonerProgressMonitor progrMonitor = new NullReasonerProgressMonitor();
        public void SetProgrMonitor(ReasonerProgressMonitor progrMonitorExt)
        {
            progrMonitor = progrMonitorExt;
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology)
        {
            var config = new SimpleConfiguration(progrMonitor);
            OWLReasonerFactory reasonerFactory = new JFactFactory();
            return reasonerFactory.createNonBufferingReasoner(ontology, config);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
        {
            OWLReasonerFactory reasonerFactory = new JFactFactory();
            return reasonerFactory.createNonBufferingReasoner(ontology, config);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology)
        {
            var config = new SimpleConfiguration(progrMonitor);
            OWLReasonerFactory reasonerFactory = new JFactFactory();
            return reasonerFactory.createReasoner(ontology, config);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology,org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration config)
        {
            OWLReasonerFactory reasonerFactory = new JFactFactory();
            return reasonerFactory.createReasoner(ontology, config);
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
            return @"JFact is an OWL DL reasoner, based on FaCT++. It supports OWL DL and (partially) OWL 2 DL.";
        }

        public string getReasonerVersion()
        {
            OWLOntologyManager owlMan = org.semanticweb.owlapi.apibinding.OWLManager.createOWLOntologyManager();
            OWLOntology ont = owlMan.createOntology();
            OWLReasoner reas = this.createReasoner(ont);
            org.semanticweb.owlapi.util.Version ver = reas.getReasonerVersion();
            return ver.getMajor() + "." + ver.getMinor() + "." + ver.getPatch();
        }
    }
}
