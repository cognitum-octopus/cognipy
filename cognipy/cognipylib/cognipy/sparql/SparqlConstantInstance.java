package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlConstantInstance extends SparqlNode
{
	private String instName;
	private boolean useTypeOf;
	public SparqlConstantInstance(DLToOWLNameConv owlNC, String freeVarId, String instName, boolean useTypeOf)
	{
		super(owlNC, freeVarId);
		this.useTypeOf = useTypeOf;
		this.instName = instName;
	}
	@Override
	public String ToSparqlFilter()
	{
		return GetFreeVariableId() + " = " + ToOwlName(instName, ARS.EntityKind.Instance);
	}

	@Override
	public String ToSparqlFilter(boolean includeTopBot, boolean removeClass)
	{
		if (!includeTopBot)
		{
			return "( " + GetFreeVariableId() + " != owl:Thing" + " && " + GetFreeVariableId() + " != owl:Nothing" + " )";
		}
		else
		{
			return "";
		}
	}


	@Override
	public String ToSparqlBody(boolean meanSuperConcept)
	{
		return ToSparqlBody(meanSuperConcept, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override string ToSparqlBody(bool meanSuperConcept, bool instance = true)
	@Override
	public String ToSparqlBody(boolean meanSuperConcept, boolean instance)
	{
		if (meanSuperConcept)
		{
			return ToOwlName(instName, ARS.EntityKind.Instance) + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + GetFreeVariableId();
		}
		else
		{
			return "";
		}
	}


	@Override
	public String ToSparqlMinus(boolean meanSuperConcept)
	{
		return ToSparqlMinus(meanSuperConcept, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public override string ToSparqlMinus(bool meanSuperConcept, bool instance = true)
	@Override
	public String ToSparqlMinus(boolean meanSuperConcept, boolean instance)
	{
		if (!meanSuperConcept)
		{
			return "";
		}

		String freeId2 = "?x1";
		if (GetFreeVariableId().equals(freeId2))
		{
			freeId2 = "?x2";
		}

		String firstBody = ToSparqlBody(meanSuperConcept, instance);
		String minusBody = firstBody + ".\r\n";
		minusBody += firstBody.replace(GetFreeVariableId(), freeId2) + ".\r\n";
		minusBody += freeId2 + " rdfs:subClassOf " + GetFreeVariableId() + ".\r\n";

		return minusBody;
	}
}