using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.reasoner;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Cognitum.OwlApi.Net
{
    public interface NetReasonerFactory : org.semanticweb.owlapi.reasoner.OWLReasonerFactory
    {
       void SetProgrMonitor(ReasonerProgressMonitor progrMonitorExt);
       string getReasonerDescription();
       string getReasonerVersion();
    }
}
