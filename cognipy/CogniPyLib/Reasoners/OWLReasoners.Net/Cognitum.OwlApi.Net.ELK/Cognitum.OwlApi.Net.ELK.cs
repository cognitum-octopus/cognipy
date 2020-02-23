using org.semanticweb.elk.owlapi;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Cognitum.OwlApi.Net.ELK
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
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            return reasonerFactory.createNonBufferingReasoner(ontology, config);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
        {
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            return reasonerFactory.createNonBufferingReasoner(ontology, config);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology)
        {
            var config = new SimpleConfiguration(progrMonitor);
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            return reasonerFactory.createReasoner(ontology, config);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology,org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration config)
        {
            OWLReasonerFactory reasonerFactory = new ElkReasonerFactory();
            return reasonerFactory.createReasoner(ontology, config);
        }

        public string getReasonerName()
        {
            return "ELK";
        }

        public string getReasonerDescription()
        {
            return @"ELK is a reasoner for OWL 2 ontologies that currently supports a part of the OWL EL ontology language. 
The goal of ELK is to provide a very fast reasoning engine for OWL EL. 
Currently, the supported OWL features and reasoning tasks are still limited (but already sufficient for important ontologies such as SNOMED CT). ";
        }

        public string getReasonerVersion()
        {
            return "0.5.0";
        }
    }
}
