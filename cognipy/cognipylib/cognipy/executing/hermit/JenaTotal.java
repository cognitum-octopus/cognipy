package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaTotal extends JenaNode
{
	private String dt;
	public JenaTotal(DLToOWLNameConv owlNC, String freeVarId, String dt)
	{
		super(owlNC, freeVarId);
		this.dt = dt;
	}

	@Override
	public String ToJenaRule()
	{
		return "isDType(" + GetFreeVariableId() + "," + dt + ")";
	}
}