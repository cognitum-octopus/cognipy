package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaTop extends JenaNode
{
	public JenaTop(DLToOWLNameConv owlNC, String freeVarId)
	{
		super(owlNC, freeVarId);
	}

	@Override
	public String ToJenaRule()
	{
		return "( " + GetFreeVariableId() + " rdf:type <http://www.w3.org/2002/07/owl#NamedIndividual> )";
	}
}