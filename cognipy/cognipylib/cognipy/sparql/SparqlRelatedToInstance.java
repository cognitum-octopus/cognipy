package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlRelatedToInstance extends SparqlNode
{
	private String instName;
	private String role;
	private boolean isInversed;
	public SparqlRelatedToInstance(DLToOWLNameConv owlNC, String freeVarId, String instName, String role, boolean isInversed)
	{
		super(owlNC, freeVarId);
		this.instName = instName;
		this.role = role;
		this.isInversed = isInversed;
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
			return GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + ToOwlName(instName, ARS.EntityKind.Instance);
		}
		else
		{
			return ToOwlName(instName, ARS.EntityKind.Instance) + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId();
		}
	}
}