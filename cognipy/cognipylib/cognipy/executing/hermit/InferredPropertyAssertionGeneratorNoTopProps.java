package cognipy.executing.hermit;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import cognipy.*;
import java.util.*;

public class InferredPropertyAssertionGeneratorNoTopProps extends InferredPropertyAssertionGenerator
{
	private boolean useData;
	public InferredPropertyAssertionGeneratorNoTopProps(boolean useData)
	{
		this.useData = useData;
	}

	@Override
	protected void addAxioms(org.semanticweb.owlapi.model.OWLNamedIndividual entity, org.semanticweb.owlapi.reasoner.OWLReasoner reasoner, org.semanticweb.owlapi.model.OWLDataFactory dataFactory, Set result)
	{
		{
			Iterator propIter = reasoner.getRootOntology().getObjectPropertiesInSignature(true).iterator();
			while (propIter.hasNext())
			{
				Object tempVar = propIter.next();
				OWLObjectProperty prop = tempVar instanceof OWLObjectProperty ? (OWLObjectProperty)tempVar : null;
				if (!prop.isTopEntity())
				{
					Iterator valueIter = reasoner.getObjectPropertyValues(entity, prop).getFlattened().iterator();
					while (valueIter.hasNext())
					{
						Object tempVar2 = valueIter.next();
						result.add(dataFactory.getOWLObjectPropertyAssertionAxiom(prop, entity, tempVar2 instanceof OWLNamedIndividual ? (OWLNamedIndividual)tempVar2 : null));
					}
				}
			}
		}
		if (useData)
		{
			Iterator propIter = reasoner.getRootOntology().getDataPropertiesInSignature(true).iterator();
			while (propIter.hasNext())
			{
				Object tempVar3 = propIter.next();
				OWLDataProperty prop = tempVar3 instanceof OWLDataProperty ? (OWLDataProperty)tempVar3 : null;
				if (!prop.isTopEntity())
				{
					Iterator valueIter = reasoner.getDataPropertyValues(entity, prop).iterator();
					while (valueIter.hasNext())
					{
						Object tempVar4 = valueIter.next();
						result.add(dataFactory.getOWLDataPropertyAssertionAxiom(prop, entity, tempVar4 instanceof OWLLiteral ? (OWLLiteral)tempVar4 : null));
					}
				}
			}
		}


	}
}