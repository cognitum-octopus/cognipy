package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlNot extends SparqlNode
{
	private SparqlNode node;
	public SparqlNot(DLToOWLNameConv owlNC, String freeVarId, SparqlNode node)
	{
		super(owlNC, freeVarId);
		this.node = node;
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
		return node.ToSparqlBody(meanSuperConcept, instance);
	}
	@Override
	public String ToSparqlFilter(boolean b, boolean removeClass)
	{
		return ToSparqlFilter();
	}
	@Override
	public String ToSparqlFilter()
	{
		String f = node.ToSparqlFilter();
		if (!tangible.StringHelper.isNullOrWhiteSpace(f))
		{
			return "!(" + f + ")";
		}
		else
		{
			return "";
		}
	}
	@Override
	public boolean UseDistinct()
	{
		return node.UseDistinct();
	}
}