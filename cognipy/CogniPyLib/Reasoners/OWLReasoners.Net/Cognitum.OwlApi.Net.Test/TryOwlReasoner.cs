using org.semanticweb.owlapi.apibinding;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;

namespace Cognitum.OwlApi.Net.Test
{
    class TryOwlReasoner
    {
        static OWLOntologyManager manager = null;
        static IRI ontologyIRI = IRI.create("http://ontorion.com/unknown.owl/#");

        public static void GetOwlInfo(string reasonerDllFolder)
        {
            manager = OWLManager.createOWLOntologyManager();
            OWLOntology ontology = manager.createOntology(ontologyIRI);

            ReasoningService aa = new ReasoningService(reasonerDllFolder);
            OWLReasoner reasoner = aa.reasonerFact.createReasoner(ontology);
            Console.WriteLine(aa.reasonerFact.getReasonerName() + " Version:" + aa.reasonerFact.getReasonerVersion() + "\r\n Description:" + aa.reasonerFact.getReasonerDescription());
            Console.ReadLine();
        }


        public static System.Reflection.Assembly CurrentDomain_AssemblyResolve(object sender, ResolveEventArgs args)
        {
            Assembly asm = null;

            if (args.Name.Contains(".Net"))
            {
                string baseDir = @"Reasoners\";
                asm = Assembly.LoadFrom(Path.Combine(baseDir, args.Name + ".dll"));
            }

            return asm;
        }
    }

    class ProgressMonitor : ReasonerProgressMonitor
    {
        ReasoningService me;
        public ProgressMonitor(ReasoningService me) { this.me = me; }
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

    class ReasoningService
    {
        public Cognitum.OwlApi.Net.NetReasonerFactory reasonerFact = null;
        public ReasoningService(string basePath)
        {
            foreach (string file in Directory.GetFiles(basePath))
            {
                if (file.EndsWith(".dll"))
                {
                    Assembly ass = Assembly.LoadFrom(file);

                    string classBaseName = Path.GetFileName(file).Replace(".dll", "");
                    Type reasonerImpl = ass.GetType(classBaseName + ".NetReasonerFactoryImpl");
                    if (reasonerImpl != null)
                    {
                        reasonerFact = (Cognitum.OwlApi.Net.NetReasonerFactory)Activator.CreateInstance(reasonerImpl);
                    }
                }
            }
            
            reasonerFact.SetProgrMonitor(new ProgressMonitor(this));
        }
        public void fireReasonerTaskBusy() { }
        public void fireReasonerTaskProgressChanged(int i1, int i2) { }
        public void fireReasonerTaskStarted(string str) { }
        public void fireReasonerTaskStopped() { }
    }
}
