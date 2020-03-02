package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaRelatedToInstance extends JenaNode
{
	private String instName;
	private String role;
	private boolean isInversed;
	public JenaRelatedToInstance(DLToOWLNameConv owlNC, String freeVarId, String instName, String role, boolean isInversed)
	{
		super(owlNC, freeVarId);
		this.instName = instName;
		this.role = role;
		this.isInversed = isInversed;
	}
	@Override
	public String ToJenaRule()
	{
		if (!isInversed)
		{
			return "( " + GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + ToOwlName(instName, ARS.EntityKind.Instance) + " )";
		}
		else
		{
			return "( " + ToOwlName(instName, ARS.EntityKind.Instance) + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId() + " )";
		}
	}
}