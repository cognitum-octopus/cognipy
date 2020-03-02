package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class SparqlRelatedToValueFilter extends SparqlNode
{


	private CNL.DL.Value value;
	private String attribute;
	private String varVarId;
	private String bound;
	public SparqlRelatedToValueFilter(DLToOWLNameConv owlNC, String freeVarId, String attribute, String bound, String varVarId, CNL.DL.Value value)
	{
		super(owlNC, freeVarId);
		this.attribute = attribute;
		this.value = value;
		this.varVarId = varVarId;
		this.bound = bound;
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
		if (bound.equals("="))
		{
			return GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + GetLiteralVal(value);
		}
		else
		{
			return GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + varVarId;
		}
	}

	@Override
	public String ToSparqlFilter(boolean includeTopBot, boolean removeClass)
	{
		return ToSparqlFilter();
	}

	@Override
	public String ToSparqlFilter()
	{
		if (bound.equals("="))
		{
			return "";
		}
		else if (bound.equals("#"))
		{
			return "regex(" + varVarId + ", " + GetLiteralVal(value) + ")";
		}
		else
		{
			String b = bound;
			if (bound.equals("≤"))
			{
				b = "<=";
			}
			else if (bound.equals("≥"))
			{
				b = ">=";
			}
			else if (bound.equals("≠"))
			{
				b = "!=";
			}

			return varVarId + " " + b + " " + GetLiteralVal(value);
		}
	}
}