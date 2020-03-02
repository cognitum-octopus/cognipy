package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaRelatedToValueFilter extends JenaNode
{
	private CNL.DL.Value value;
	private String attribute;
	private String varVarId;
	private String bound;
	public JenaRelatedToValueFilter(DLToOWLNameConv owlNC, String freeVarId, String attribute, String bound, String varVarId, CNL.DL.Value value)
	{
		super(owlNC, freeVarId);
		this.attribute = attribute;
		this.value = value;
		this.varVarId = varVarId;
		this.bound = bound;
	}
	@Override
	public String ToJenaRule()
	{
		if (bound.equals("="))
		{
			return "( " + GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + GetLiteralVal(value) + " )";
		}
		else
		{
			return "( " + GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + varVarId + " )";
		}
	}
}