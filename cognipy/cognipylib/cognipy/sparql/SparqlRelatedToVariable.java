package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlRelatedToVariable extends SparqlNode
{
	private String variableId;
	private String role;
	private boolean isInversed;
	private boolean useDistinct;

	public SparqlRelatedToVariable(DLToOWLNameConv owlNC, String freeVarId, String variableId, String role, boolean isInversed)
	{
		this(owlNC, freeVarId, variableId, role, isInversed, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SparqlRelatedToVariable(DLToOWLNameConv owlNC, string freeVarId, string variableId, string role, bool isInversed, bool useDistinct = false)
	public SparqlRelatedToVariable(DLToOWLNameConv owlNC, String freeVarId, String variableId, String role, boolean isInversed, boolean useDistinct)
	{
		super(owlNC, freeVarId);
		this.variableId = variableId;
		this.role = role;
		this.isInversed = isInversed;
		this.useDistinct = useDistinct;
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
		if (!isInversed)
		{
			return GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + variableId;
		}
		else
		{
			return variableId + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId();
		}
	}
	@Override
	public boolean UseDistinct()
	{
		return useDistinct;
	}
}