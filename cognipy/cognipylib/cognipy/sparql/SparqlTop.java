package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlTop extends SparqlNode
{
	public SparqlTop(DLToOWLNameConv owlNC, String freeVarId)
	{
		super(owlNC, freeVarId);
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
			return "owl:Thing rdfs:subClassOf " + GetFreeVariableId();
		}
		else
		{
			return GetFreeVariableId() + " rdfs:subClassOf owl:Thing";
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
		String freeId2 = "?x1";
		if (GetFreeVariableId().equals(freeId2))
		{
			freeId2 = "?x2";
		}

		String firstBody = ToSparqlBody(meanSuperConcept, instance);
		String minusBody = firstBody + ".\r\n ";
		minusBody += firstBody.replace(GetFreeVariableId(), freeId2) + ".\r\n";
		if (!instance)
		{
			if (!meanSuperConcept)
			{
				minusBody += GetFreeVariableId() + " rdfs:subClassOf " + freeId2 + ".\r\n";
			}
			else
			{
				minusBody += freeId2 + " rdfs:subClassOf " + GetFreeVariableId() + ".\r\n";
			}
		}
		else
		{
			minusBody = "";
		}
		return minusBody;
	}

	@Override
	public boolean UseDistinct()
	{
		return false;
	}
}