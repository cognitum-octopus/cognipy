using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Cognitum.OwlApi.Net.Pellet
{
    public class NetReasonerFactoryImpl : Cognitum.OwlApi.Net.NetReasonerFactory
    {
        public NetReasonerFactoryImpl()
        {
            //Assembly assReasoner = Assembly.LoadFrom(@"Reasoners\Pellet\pellet.dll");
        }

        private ReasonerProgressMonitor progrMonitor = new NullReasonerProgressMonitor();
        public void SetProgrMonitor(ReasonerProgressMonitor progrMonitorExt)
        {
            progrMonitor = progrMonitorExt;
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology)
        {
            var config = new SimpleConfiguration(progrMonitor);

            return new com.clarkparsia.pellet.owlapiv3.PelletReasoner(ontology, config, BufferingMode.NON_BUFFERING);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createNonBufferingReasoner(OWLOntology ontology, OWLReasonerConfiguration config)
        {
            return new com.clarkparsia.pellet.owlapiv3.PelletReasoner(ontology, config, BufferingMode.NON_BUFFERING);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology)
        {
            var config = new SimpleConfiguration(progrMonitor);

            return new com.clarkparsia.pellet.owlapiv3.PelletReasoner(ontology, config, BufferingMode.BUFFERING);
        }

        public org.semanticweb.owlapi.reasoner.OWLReasoner createReasoner(OWLOntology ontology,org.semanticweb.owlapi.reasoner.OWLReasonerConfiguration config)
        {
            return new com.clarkparsia.pellet.owlapiv3.PelletReasoner(ontology, config, BufferingMode.BUFFERING);
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
            return @"Pellet is an OWL 2 reasoner. Pellet provides standard and cutting-edge reasoning services for OWL ontologies.";
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
