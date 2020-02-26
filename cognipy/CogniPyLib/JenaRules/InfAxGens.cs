using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.util;

namespace CogniPy.Executing.HermiT
{
    public class InferredPropertyAssertionGeneratorNoTopProps : InferredPropertyAssertionGenerator
    {
        bool useData;
        public InferredPropertyAssertionGeneratorNoTopProps(bool useData)
        {
            this.useData = useData;
        }

        protected override void addAxioms(org.semanticweb.owlapi.model.OWLNamedIndividual entity, org.semanticweb.owlapi.reasoner.OWLReasoner reasoner, org.semanticweb.owlapi.model.OWLDataFactory dataFactory, java.util.Set result)
        {
            {
                var propIter = reasoner.getRootOntology().getObjectPropertiesInSignature(true).iterator();
                while (propIter.hasNext())
                {
                    var prop = propIter.next() as OWLObjectProperty;
                    if (!prop.isTopEntity())
                    {
                        var valueIter = reasoner.getObjectPropertyValues(entity, prop).getFlattened().iterator();
                        while (valueIter.hasNext())
                            result.add(dataFactory.getOWLObjectPropertyAssertionAxiom(prop, entity, valueIter.next() as OWLNamedIndividual));
                    }
                }
            }
            if(useData)
            {
                var propIter = reasoner.getRootOntology().getDataPropertiesInSignature(true).iterator();
                while (propIter.hasNext())
                {
                    var prop = propIter.next() as OWLDataProperty;
                    if (!prop.isTopEntity())
                    {
                        var valueIter = reasoner.getDataPropertyValues(entity, prop).iterator();
                        while (valueIter.hasNext())
                            result.add(dataFactory.getOWLDataPropertyAssertionAxiom(prop, entity, valueIter.next() as OWLLiteral));
                    }
                }
            }


        }
    }
}
