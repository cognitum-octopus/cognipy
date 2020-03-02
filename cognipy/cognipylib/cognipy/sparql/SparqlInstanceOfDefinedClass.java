package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlInstanceOfDefinedClass extends SparqlNode
{
	private String clsname;
	private String emptyVarId;
	private boolean useTypeOf = false;
	public SparqlInstanceOfDefinedClass(DLToOWLNameConv owlNC, String freeVarId, String clsname, String emptyVarId, boolean useTypeOf)
	{
		super(owlNC, freeVarId);
		this.useTypeOf = useTypeOf;
		this.clsname = clsname;
		this.emptyVarId = emptyVarId;
	}

	@Override
	public String ToSparqlFilter(boolean includeTopBot, boolean removeClass)
	{
		String flt = removeClass ? ToOwlName(clsname, ARS.EntityKind.Concept) + "!=" + GetFreeVariableId() : "";
		if (!includeTopBot)
		{
			return "( " + (tangible.StringHelper.isNullOrEmpty(flt) ? "" : (flt + " && ")) + GetFreeVariableId() + " != owl:Thing" + " && " + GetFreeVariableId() + " != owl:Nothing" + " )";
		}
		else
		{
			return tangible.StringHelper.isNullOrEmpty(flt) ? "" : "( " + flt + " )";
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
		if (!instance)
		{
			if (meanSuperConcept)
			{
				return ((clsname == null) ? emptyVarId : ToOwlName(clsname, ARS.EntityKind.Concept)) + " rdfs:subClassOf " + GetFreeVariableId();
			}
			else
			{
				return GetFreeVariableId() + " rdfs:subClassOf " + ((clsname == null) ? emptyVarId : ToOwlName(clsname, ARS.EntityKind.Concept));
			}
		}
		else
		{
			return GetFreeVariableId() + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + ((clsname == null) ? "<http://www.w3.org/2002/07/owl#NamedIndividual>" : ToOwlName(clsname, ARS.EntityKind.Concept));
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
		if (!instance)
		{
			minusBody += firstBody.replace(GetFreeVariableId(), freeId2) + ".\r\n";
			if (!meanSuperConcept)
			{
				minusBody += GetFreeVariableId() + " rdfs:subClassOf " + freeId2 + ".\r\n";
			}
			else
			{
				minusBody += freeId2 + " rdfs:subClassOf " + GetFreeVariableId() + ".\r\n";
			}
			minusBody += "FILTER(!isBlank(" + freeId2 + "))" + ".\r\n";
			minusBody += "FILTER(" + freeId2 + "!=" + GetFreeVariableId() + ")" + ".\r\n";
			minusBody += "FILTER(" + freeId2 + "!=" + ToOwlName(clsname, ARS.EntityKind.Concept) + ")" + ".\r\n";
			minusBody += "FILTER(" + ToOwlName(clsname, ARS.EntityKind.Concept) + "!=" + GetFreeVariableId() + ")" + ".\r\n";
		}
		else
		{
			minusBody += GetFreeVariableId() + " rdf:type " + freeId2 + ".\r\n";
			if (clsname != null)
			{
				minusBody += freeId2 + " rdfs:subClassOf " + ToOwlName(clsname, ARS.EntityKind.Concept);
				minusBody += "FILTER(" + freeId2 + "!=" + ToOwlName(clsname, ARS.EntityKind.Concept) + ")" + ".\r\n";
			}
			else
			{
				minusBody += "FILTER(" + freeId2 + " != <http://www.w3.org/2002/07/owl#NamedIndividual> && " + freeId2 + " != <http://www.w3.org/2002/07/owl#Thing>)";
			}
		}
		return minusBody;
	}

	@Override
	public boolean UseDistinct()
	{
		return clsname == null;
	}
}