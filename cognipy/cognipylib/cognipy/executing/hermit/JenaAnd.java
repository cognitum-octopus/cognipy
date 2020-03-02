package cognipy.executing.hermit;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class JenaAnd extends JenaNode
{
	private ArrayList<JenaNode> nodes;
	public JenaAnd(DLToOWLNameConv owlNC, String freeVarId, ArrayList<JenaNode> nodes)
	{
		super(owlNC, freeVarId);
		this.nodes = nodes;
	}
	@Override
	public String ToJenaRule()
	{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		return tangible.StringHelper.join(",", (from n in nodes select n.ToJenaRule()));
	}
}