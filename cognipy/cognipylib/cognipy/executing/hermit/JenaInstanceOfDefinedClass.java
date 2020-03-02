package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaInstanceOfDefinedClass extends JenaNode
{
	private String clsname;
	private String emptyVarId;
	public JenaInstanceOfDefinedClass(DLToOWLNameConv owlNC, String freeVarId, String clsname, String emptyVarId)
	{
		super(owlNC, freeVarId);
		this.clsname = clsname;
		this.emptyVarId = emptyVarId;
	}

	@Override
	public String ToJenaRule()
	{
		return "( " + GetFreeVariableId() + " rdf:type " + ((clsname == null) ? "<http://www.w3.org/2002/07/owl#NamedIndividual>" : ToOwlName(clsname, ARS.EntityKind.Concept)) + " ) ";
	}
}