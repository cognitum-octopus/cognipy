package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaRelatedToVariable extends JenaNode
{
	private String variableId;
	private String role;
	private boolean isInversed;
	private boolean useDistinct;

	public JenaRelatedToVariable(DLToOWLNameConv owlNC, String freeVarId, String variableId, String role, boolean isInversed)
	{
		this(owlNC, freeVarId, variableId, role, isInversed, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public JenaRelatedToVariable(DLToOWLNameConv owlNC, string freeVarId, string variableId, string role, bool isInversed, bool useDistinct = false)
	public JenaRelatedToVariable(DLToOWLNameConv owlNC, String freeVarId, String variableId, String role, boolean isInversed, boolean useDistinct)
	{
		super(owlNC, freeVarId);
		this.variableId = variableId;
		this.role = role;
		this.isInversed = isInversed;
		this.useDistinct = useDistinct;
	}
	@Override
	public String ToJenaRule()
	{
		if (!isInversed)
		{
			return "( " + GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + variableId + " )";
		}
		else
		{
			return "( " + variableId + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId() + " )";
		}
	}
}