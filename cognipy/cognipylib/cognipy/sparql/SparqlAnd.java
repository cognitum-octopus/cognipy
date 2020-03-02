package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlAnd extends SparqlNode
{
	private ArrayList<SparqlNode> nodes;
	public SparqlAnd(DLToOWLNameConv owlNC, String freeVarId, ArrayList<SparqlNode> nodes)
	{
		super(owlNC, freeVarId);
		this.nodes = nodes;
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
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		return tangible.StringHelper.join(" .\r\n", (from n in nodes select n.ToSparqlBody(meanSuperConcept)));
	}
	@Override
	public String ToSparqlFilter(boolean b, boolean removeClass)
	{
		return ToSparqlFilter();
	}
	@Override
	public String ToSparqlFilter()
	{
		ArrayList<String> toJ = new ArrayList<String>();
		for (SparqlNode n : nodes)
		{
			String f = n.ToSparqlFilter();
			if (!tangible.StringHelper.isNullOrWhiteSpace(f))
			{
				toJ.add(f);
			}
		}
		if (!toJ.isEmpty())
		{
			return "(" + tangible.StringHelper.join(" &&\r\n", toJ) + ")";
		}
		else
		{
			return "";
		}
	}
	@Override
	public boolean UseDistinct()
	{
		for (SparqlNode node : nodes)
		{
			if (node.UseDistinct())
			{
				return true;
			}
		}
		return false;
	}
}