package cognipy.cnl.dl;

import cognipy.ars.*;
import org.apache.jena.graph.*;
import org.apache.jena.rdf.model.*;
import org.apache.jena.reasoner.rulesys.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class SwrlIterateProc extends GenericVisitor
{

	public static class NotInProfileException extends RuntimeException
	{
	}

	private void NotInProfile()
	{
		throw new NotInProfileException();
	}

	private Model model;
	private String defaultNS;

	public RuleContext context;

	public String iterVar;
	public Object iterVal;
	public HashMap<String, Integer> varNameToIndex;
	public Object[] allVars;
	private boolean swrlOnly = false;


	public SwrlIterateProc(Model model)
	{
		this(model, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SwrlIterateProc(Model model, bool swrlOnly = false)
	public SwrlIterateProc(Model model, boolean swrlOnly)
	{
		this.model = model;
		this.swrlOnly = swrlOnly;
	}

	private DLToOWLNameConv owlNC = new DLToOWLNameConv();

	public final void setOWLDataFactory(String defaultNS, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		this.owlNC.setOWLFormat(defaultNS, namespaceManager, lex);
	}

	private void AddIfNotExists(org.apache.jena.graph.Node s, org.apache.jena.graph.Node v, org.apache.jena.graph.Node o)
	{
		Triple t = new Triple(s, v, o);
		if (!context.contains(t))
		{
			context.add(t);
		}
	}

	@Override
	public Object Visit(SwrlInstance e)
	{
		if (e.C instanceof CNL.DL.Atomic)
		{
			Object tempVar = e.I.accept(this);
			org.apache.jena.graph.Node inst = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;

			IRI cls = owlNC.getIRIFromId((e.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.C : null).id, EntityKind.Concept);
			org.apache.jena.graph.Node clsn = org.apache.jena.graph.NodeFactory.createURI(cls.toString());
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), clsn);
			if (!swrlOnly)
			{
				AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
				AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
				AddIfNotExists(clsn, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Class.asNode());
			}
		}
		else
		{
			NotInProfile();
			return super.Visit(e);
		}
		return null;
	}

	@Override
	public Object Visit(SwrlRole e)
	{
		Object tempVar = e.I.accept(this);
		org.apache.jena.graph.Node inst = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;
		Object tempVar2 = e.J.accept(this);
		org.apache.jena.graph.Node jnst = tempVar2 instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar2 : null;

		IRI rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
		org.apache.jena.graph.Node reln = org.apache.jena.graph.NodeFactory.createURI(rel.toString());
		AddIfNotExists(inst, reln, jnst);
		if (!swrlOnly)
		{
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			AddIfNotExists(reln, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode());
		}
		return null;
	}

	@Override
	public Object Visit(SwrlSameAs e)
	{
		Object tempVar = e.I.accept(this);
		org.apache.jena.graph.Node inst = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;
		Object tempVar2 = e.J.accept(this);
		org.apache.jena.graph.Node jnst = tempVar2 instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar2 : null;

		AddIfNotExists(inst, org.apache.jena.vocabulary.OWL2.sameAs.asNode(), jnst);
		if (!swrlOnly)
		{
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
		}
		return null;
	}

	@Override
	public Object Visit(SwrlDifferentFrom e)
	{
		Object tempVar = e.I.accept(this);
		org.apache.jena.graph.Node inst = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;
		Object tempVar2 = e.J.accept(this);
		org.apache.jena.graph.Node jnst = tempVar2 instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar2 : null;

		AddIfNotExists(inst, org.apache.jena.vocabulary.OWL2.differentFrom.asNode(), jnst);
		if (!swrlOnly)
		{
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
		}
		return null;
	}

	@Override
	public Object Visit(SwrlDataRange e)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public Object Visit(SwrlDataProperty e)
	{
		Object tempVar = e.IO.accept(this);
		org.apache.jena.graph.Node inst = tempVar instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar : null;

		IRI rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
		org.apache.jena.graph.Node reln = org.apache.jena.graph.NodeFactory.createURI(rel.toString());
		Object tempVar2 = e.DO.accept(this);
		org.apache.jena.graph.Node dv = tempVar2 instanceof org.apache.jena.graph.Node ? (org.apache.jena.graph.Node)tempVar2 : null;
		AddIfNotExists(inst, reln, dv);
		if (!swrlOnly)
		{
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
			AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
			AddIfNotExists(reln, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());
		}
		return null;
	}

	@Override
	public Object Visit(SwrlBuiltIn e)
	{
		throw new UnsupportedOperationException();
	}

	//SwrlNodes

	@Override
	public Object Visit(SwrlIVal e)
	{
		return org.apache.jena.graph.NodeFactory.createURI(owlNC.getIRIFromId(e.I, EntityKind.Instance).toString());
	}

	@Override
	public Object Visit(SwrlIVar e)
	{
		String varN = "?" + e.VAR.replace("-", "_");
		Object val;
		if (iterVar.equals(varN))
		{
			val = iterVal;
		}
		else
		{
			val = allVars[varNameToIndex.get(varN)];
		}
		return org.apache.jena.graph.NodeFactory.createURI(owlNC.getIRIFromId(val.toString(), EntityKind.Instance).toString());
	}

	private static Regex DtmRg = new Regex("(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?))?", RegexOptions.Compiled);
	private static String completeDTMVal(String val)
	{
		System.Text.RegularExpressions.Match m = DtmRg.Match(val);
		String dta = m.Groups["date"].Value;
		String tm = m.Groups["time"].Value;
		StringBuilder sb = new StringBuilder();
		sb.append(dta);
		if (tangible.StringHelper.isNullOrEmpty(tm))
		{
			sb.append("T00:00:00");
		}
		else
		{
			sb.append(tm);
		}
		if (tm.length() == "T00:00".length())
		{
			sb.append(":00");
		}
		return sb.toString();
	}

	private Literal getLiteralVal(Value v)
	{
		if (model == null)
		{
			return null;
		}

		if (v instanceof CNL.DL.Bool)
		{
			return model.createTypedLiteral(v.ToBool(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDboolean);
		}
		if (v instanceof CNL.DL.String)
		{
			return model.createTypedLiteral(v.toString(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
		}
		if (v instanceof CNL.DL.Float)
		{
			return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdouble);
		}
		if (v instanceof CNL.DL.Number)
		{
			return model.createTypedLiteral(new java.lang.Integer(v.ToInt()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger);
		}
		if (v instanceof CNL.DL.DateTimeVal)
		{
			return model.createTypedLiteral(completeDTMVal(v.ToStringExact()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdateTime);
		}
		if (v instanceof CNL.DL.Duration)
		{
			return model.createTypedLiteral(v.ToStringExact(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDduration);
		}

		return model.createTypedLiteral(v.toString()); //TODO xsd:date i inne typy
	}

	@Override
	public Object Visit(SwrlDVal e)
	{
		return getLiteralVal(e.Val).asNode();
	}

	@Override
	public Object Visit(SwrlDVar e)
	{
		String varN = "?" + e.VAR.replace("-", "_");
		Object val;
		if (iterVar.equals(varN))
		{
			val = iterVal;
		}
		else
		{
			val = allVars[varNameToIndex.get(varN)];
		}
		return getLiteralVal(Value.FromObject(val)).asNode();
	}
	//bounds

}