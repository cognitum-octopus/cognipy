package cognipy.sparql;

import cognipy.ars.*;
import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class Transform extends cognipy.cnl.dl.GenericVisitor
{

	private DLToOWLNameConv _owlNC = new DLToOWLNameConv();
	private DLToOWLNameConv getOwlNC()
	{
		if (_pfx2ns != null)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var tmpStoredPfxs = _pfx2ns.invoke();
			boolean diff = false;
				//TODO is this efficient? maybe we can implement this in another way?
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var pf : tmpStoredPfxs)
			{
				if (!storedPfxs.containsKey(pf.Key) || !storedPfxs.get(pf.Key).equals(pf.Value))
				{
					diff = true;
					break;
				}
			}

			if (diff)
			{
				storedPfxs = tmpStoredPfxs;
				PrefixOWLOntologyFormat namespaceManager = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
				this._owlNC.ClearOWLFormat();
				if (_defaultNs != null)
				{
					namespaceManager.setDefaultPrefix(_defaultNs);
				}
				for (Map.Entry<String, String> kv : storedPfxs.entrySet())
				{
					namespaceManager.setPrefix(kv.getKey(), kv.getValue());
				}

				this._owlNC.setOWLFormat(_defaultNs, namespaceManager, _lex);
			}
		}
		return _owlNC;
	}
	private void setOwlNC(DLToOWLNameConv value)
	{
		_owlNC = value;
	}

	private HashMap<String, String> storedPfxs = new HashMap<String, String>();

	private OWLDataFactory factory;

	public final HashMap<String, String> getInvUriMappings()
	{
		return getOwlNC().InvUriMappings;
	}
	public final void setInvUriMappings(HashMap<String, String> value)
	{
		getOwlNC().InvUriMappings = value;
	}

	//TODO probably this initializer should not exist because the prefixes are not loaded dynamically...
	public final void setOWLDataFactory(String defaultNS, OWLDataFactory factory, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		this.factory = factory;
		this._pfx2ns = () -> null.invoke();
		this._lex = lex;
		this._defaultNs = defaultNS;
		this.getOwlNC().setOWLFormat(defaultNS, namespaceManager, _lex);
	}

	private tangible.Func0Param<HashMap<String, String>> _pfx2ns;
	private cognipy.cnl.en.endict _lex;
	private String _defaultNs;
	public final void setOWLDataFactory(String defaultNs, tangible.Func0Param<HashMap<String, String>> pfx2ns, cognipy.cnl.en.endict lex)
	{
		this._defaultNs = defaultNs;

		this._pfx2ns = () -> pfx2ns.invoke();
		this._lex = lex;

		if (_defaultNs != null)
		{
			// set the default prefix so that this one at least is remembered....
			PrefixOWLOntologyFormat namespaceManager = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
			this._owlNC.ClearOWLFormat();
			namespaceManager.setDefaultPrefix(_defaultNs);
			this._owlNC.setOWLFormat(_defaultNs, namespaceManager, _lex);
		}
	}

	public final String ToOwlName(String name, ARS.EntityKind whatFor)
	{
		return "<" + getOwlNC().getIRIFromId(name, whatFor).toString() + ">";
	}

	public final DlName ToDL(String uri, ARS.EntityKind makeFor)
	{
		return getOwlNC().ToDL(uri, makeFor);
	}

	private int freeVarIdBase = 0;
	private VisitingParam<String> activeFreeVarId = new VisitingParam<String>(null);
	public final String newFreeVarId()
	{
		return "?x" + freeVarIdBase++.toString();
	}

	public static String PREAMBLE = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>" + "\r\n" +
"PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>" + "\r\n" +
"PREFIX owl: <http://www.w3.org/2002/07/owl#>" + "\r\n" +
"PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>" + "\r\n" +
"PREFIX fn: <http://www.w3.org/2005/xpath-functions#>" + "\r\n" +
"";
	private boolean useTypeOf;


	public final String ConvertToGetInstancesOf(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize, boolean useTypeOf, boolean direct)
	{
		return ConvertToGetInstancesOf(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, offset, pageSize, useTypeOf, direct, "NONE");
	}

	public final String ConvertToGetInstancesOf(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize, boolean useTypeOf)
	{
		return ConvertToGetInstancesOf(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, offset, pageSize, useTypeOf, true, "NONE");
	}

	public final String ConvertToGetInstancesOf(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize)
	{
		return ConvertToGetInstancesOf(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, offset, pageSize, false, true, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ConvertToGetInstancesOf(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding, out Dictionary<string, string> attributeBinding, out string defaultIntance, int offset, int pageSize, bool useTypeOf = false, bool direct = true, string order = "NONE")
	public final String ConvertToGetInstancesOf(CNL.DL.Node n, ArrayList<String> roles, ArrayList<String> attributes, tangible.OutObject<HashMap<String, String>> roleBinding, tangible.OutObject<HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize, boolean useTypeOf, boolean direct, String order)
	{

		String[] rr = ConvertToGetInstancesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, useTypeOf, direct, order);

		String distinct = rr[0];
		String selectVars = rr[1];
		String whereBlock = rr[2];
		String filterBlock = rr[3];
		String whereBlock2 = rr[4];
		String orderByBlock = rr[5];

		String q = PREAMBLE + "SELECT " + distinct + selectVars + "\r\n" + "WHERE {" + (whereBlock2 != null ? "{" : "") + whereBlock;
		if (filterBlock != null)
		{
			q += " FILTER (" + filterBlock + ")";
		}
		if (whereBlock2 != null)
		{
			q += "} UNION {";
			q += whereBlock2;
			q += " FILTER (";
			q += filterBlock.replace(" = <", " != <");
			q += ")";
			q += "}";
		}
		q += "}";
		if (orderByBlock != null)
		{
			q += orderByBlock;
		}

		q += (pageSize > -1 ? " LIMIT " + pageSize + " OFFSET " + offset : "");

		return q;

	}


	public final String ConvertToGetTypesOf(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize, boolean useTypeOf, boolean direct)
	{
		return ConvertToGetTypesOf(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, offset, pageSize, useTypeOf, direct, "NONE");
	}

	public final String ConvertToGetTypesOf(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize, boolean useTypeOf)
	{
		return ConvertToGetTypesOf(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, offset, pageSize, useTypeOf, true, "NONE");
	}

	public final String ConvertToGetTypesOf(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize)
	{
		return ConvertToGetTypesOf(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, offset, pageSize, false, true, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ConvertToGetTypesOf(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding, out Dictionary<string, string> attributeBinding, out string defaultIntance, int offset, int pageSize, bool useTypeOf = false, bool direct = true, string order = "NONE")
	public final String ConvertToGetTypesOf(CNL.DL.Node n, ArrayList<String> roles, ArrayList<String> attributes, tangible.OutObject<HashMap<String, String>> roleBinding, tangible.OutObject<HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, int offset, int pageSize, boolean useTypeOf, boolean direct, String order)
	{

		String[] rr = ConvertToGetTypesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, useTypeOf, direct, order);

		String distinct = rr[0];
		String selectVars = rr[1];
		String whereBlock = rr[2];
		String filterBlock = rr[3];
		String whereBlock2 = rr[4];
		String orderByBlock = rr[5];

		String q = PREAMBLE + "SELECT " + distinct + selectVars + "\r\n" + "WHERE {" + (whereBlock2 != null ? "{" : "") + whereBlock;
		if (filterBlock != null)
		{
			q += " FILTER (" + filterBlock + ")";
		}
		if (whereBlock2 != null)
		{
			q += "} UNION {";
			q += whereBlock2;
			q += "}";
		}
		q += "}";
		if (orderByBlock != null)
		{
			q += orderByBlock;
		}

		q += (pageSize > -1 ? " LIMIT " + pageSize + " OFFSET " + offset : "");

		return q;

	}


	public final String[] ConvertToGetInstancesOfDetails(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, boolean useTypeOf, boolean direct)
	{
		return ConvertToGetInstancesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, useTypeOf, direct, "NONE");
	}

	public final String[] ConvertToGetInstancesOfDetails(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, boolean useTypeOf)
	{
		return ConvertToGetInstancesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, useTypeOf, true, "NONE");
	}

	public final String[] ConvertToGetInstancesOfDetails(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance)
	{
		return ConvertToGetInstancesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, false, true, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string[] ConvertToGetInstancesOfDetails(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding, out Dictionary<string, string> attributeBinding, out string defaultIntance, bool useTypeOf = false, bool direct = true, string order = "NONE")
	public final String[] ConvertToGetInstancesOfDetails(CNL.DL.Node n, ArrayList<String> roles, ArrayList<String> attributes, tangible.OutObject<HashMap<String, String>> roleBinding, tangible.OutObject<HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, boolean useTypeOf, boolean direct, String order)
	{
		this.useTypeOf = useTypeOf;
		freeVarIdBase = 0;
		roleBinding.argValue = null;
		attributeBinding.argValue = null;
		String selectVars;
		String whereBlock;
		String whereBlock2 = null;
		String orderByBlock = null;

		String filterBlock = null;
		boolean distinct = false;
		String lhs;

		boolean ordering = !order.equals("NONE");
		if (n instanceof InstanceSet)
		{
			ArrayList<Instance> insts = (n instanceof InstanceSet ? (InstanceSet)n : null).Instances;
			Object tempVar = insts.get(0);
			defaultIntance.argValue = (tempVar instanceof NamedInstance ? (NamedInstance)tempVar : null).name;
			if (insts.size() == 1)
			{
				lhs = ToOwlName(defaultIntance.argValue, ARS.EntityKind.Instance);
				selectVars = "?z0";

				whereBlock = "?z0" + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + "<http://www.w3.org/2002/07/owl#NamedIndividual>";

				whereBlock2 = "?z0 owl:sameAs " + lhs;
				filterBlock = "?z0 = " + lhs;
			}
			else if (insts.size() > 1)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
				lhs = tangible.StringHelper.join(",", (from i in insts select ToOwlName((i instanceof NamedInstance ? (NamedInstance)i : null).name, ARS.EntityKind.Instance)));
				selectVars = "?z0";

				whereBlock = "?z0" + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + "<http://www.w3.org/2002/07/owl#NamedIndividual>";

				whereBlock2 = null;
				filterBlock = "?z0 IN (" + lhs + ")";
			}
			else
			{
				throw new IllegalStateException();
			}
		}
		else
		{
			try (activeFreeVarId.set(newFreeVarId()))
			{
				Object tempVar2 = n.accept(this);
				SparqlNode sparqlNode = tempVar2 instanceof SparqlNode ? (SparqlNode)tempVar2 : null;
				distinct = sparqlNode.UseDistinct();

				lhs = sparqlNode.GetFreeVariableId();
				selectVars = sparqlNode.GetFreeVariableId();
				whereBlock = sparqlNode.ToCombinedBlock(false, true, direct, true, false);
				defaultIntance.argValue = null;
			}
		}

		if (ordering)
		{
			whereBlock += ". " + selectVars + " <http://www.ontorion.com#label> ?z1";
			orderByBlock = String.format(" ORDER BY %1$s(?z1) ", order);
			distinct = true;
		}

		if (roles != null)
		{
			roleBinding.argValue = new HashMap<String, String>();
			for (String role : roles)
			{
				String roleId = newFreeVarId();
				roleBinding.argValue.put(roleId.substring(1), role);
				selectVars += " " + roleId;
				whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(role, ARS.EntityKind.DataRole) + " " + roleId + "}";
			}
		}

		if (attributes != null)
		{
			attributeBinding.argValue = new HashMap<String, String>();
			for (String attr : attributes)
			{
				String attrId = newFreeVarId();
				attributeBinding.argValue.put(attrId.substring(1), attr);
				selectVars += " " + attrId;
				whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(attr, ARS.EntityKind.DataRole) + " " + attrId + "}";
			}
		}

		return new String[] {(distinct ? "DISTINCT " : " "), selectVars, whereBlock, filterBlock, whereBlock2, orderByBlock};
	}


	public final String[] ConvertToGetTypesOfDetails(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, boolean useTypeOf, boolean direct)
	{
		return ConvertToGetTypesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, useTypeOf, direct, "NONE");
	}

	public final String[] ConvertToGetTypesOfDetails(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, boolean useTypeOf)
	{
		return ConvertToGetTypesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, useTypeOf, true, "NONE");
	}

	public final String[] ConvertToGetTypesOfDetails(CNL.DL.Node n, java.util.ArrayList<String> roles, java.util.ArrayList<String> attributes, tangible.OutObject<java.util.HashMap<String, String>> roleBinding, tangible.OutObject<java.util.HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance)
	{
		return ConvertToGetTypesOfDetails(n, roles, attributes, roleBinding, attributeBinding, defaultIntance, false, true, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string[] ConvertToGetTypesOfDetails(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding, out Dictionary<string, string> attributeBinding, out string defaultIntance, bool useTypeOf = false, bool direct = true, string order = "NONE")
	public final String[] ConvertToGetTypesOfDetails(CNL.DL.Node n, ArrayList<String> roles, ArrayList<String> attributes, tangible.OutObject<HashMap<String, String>> roleBinding, tangible.OutObject<HashMap<String, String>> attributeBinding, tangible.OutObject<String> defaultIntance, boolean useTypeOf, boolean direct, String order)
	{
		this.useTypeOf = useTypeOf;
		freeVarIdBase = 0;
		roleBinding.argValue = null;
		attributeBinding.argValue = null;
		String selectVars;
		String whereBlock;
		String whereBlock2 = null;
		String orderByBlock = null;

		String filterBlock = null;
		boolean distinct = false;
		String lhs;

		boolean ordering = !order.equals("NONE");
		if (n instanceof InstanceSet)
		{
			ArrayList<Instance> insts = (n instanceof InstanceSet ? (InstanceSet)n : null).Instances;
			if (insts.size() != 1)
			{
				throw new IllegalStateException();
			}

			Object tempVar = insts.get(0);
			defaultIntance.argValue = (tempVar instanceof NamedInstance ? (NamedInstance)tempVar : null).name;
			lhs = ToOwlName(defaultIntance.argValue, ARS.EntityKind.Instance);

			try (activeFreeVarId.set(newFreeVarId()))
			{
				selectVars = activeFreeVarId.get();
				whereBlock = lhs + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + activeFreeVarId.get();
				defaultIntance.argValue = null;
				String flt = lhs + "!=" + activeFreeVarId.get();
				filterBlock = "( " + flt + " && " + activeFreeVarId.get() + " != owl:Thing" + " && " + activeFreeVarId.get() + " != owl:Nothing" + " )";
				if (direct)
				{
					String freeId2 = "?x1";
					if (activeFreeVarId.get().equals(freeId2))
					{
						freeId2 = "?x2";
					}

					String minusBody = whereBlock.replace(activeFreeVarId.get(), freeId2) + ".\r\n";
					minusBody += freeId2 + " rdfs:subClassOf " + activeFreeVarId.get() + ".\r\n";
					minusBody += "FILTER(" + freeId2 + "!=" + activeFreeVarId.get() + ")" + ".\r\n";
					minusBody += "FILTER(" + freeId2 + "!=" + lhs + ")" + ".\r\n";
					minusBody += "FILTER(" + lhs + "!=" + activeFreeVarId.get() + ")" + ".\r\n";
					whereBlock += ". MINUS {" + minusBody + "}";
				}
			}
		}
		else
		{
			throw new IllegalStateException();
		}

		if (ordering)
		{
			whereBlock += ". " + selectVars + " <http://www.ontorion.com#label> ?z1";
			orderByBlock = String.format(" ORDER BY %1$s(?z1) ", order);
			distinct = true;
		}

		if (roles != null)
		{
			roleBinding.argValue = new HashMap<String, String>();
			for (String role : roles)
			{
				String roleId = newFreeVarId();
				roleBinding.argValue.put(roleId.substring(1), role);
				selectVars += " " + roleId;
				whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(role, ARS.EntityKind.DataRole) + " " + roleId + "}";
			}
		}

		if (attributes != null)
		{
			attributeBinding.argValue = new HashMap<String, String>();
			for (String attr : attributes)
			{
				String attrId = newFreeVarId();
				attributeBinding.argValue.put(attrId.substring(1), attr);
				selectVars += " " + attrId;
				whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(attr, ARS.EntityKind.DataRole) + " " + attrId + "}";
			}
		}

		return new String[] {(distinct ? "DISTINCT " : " "), selectVars, whereBlock, filterBlock, whereBlock2, orderByBlock};
	}



	public final String ConvertToGetSuperconceptsOf(CNL.DL.Node n, boolean direct, boolean includeTopBot, int offset, int pageSize, boolean useTypeOf)
	{
		return ConvertToGetSuperconceptsOf(n, direct, includeTopBot, offset, pageSize, useTypeOf, "NONE");
	}

	public final String ConvertToGetSuperconceptsOf(CNL.DL.Node n, boolean direct, boolean includeTopBot, int offset, int pageSize)
	{
		return ConvertToGetSuperconceptsOf(n, direct, includeTopBot, offset, pageSize, false, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ConvertToGetSuperconceptsOf(CNL.DL.Node n, bool direct, bool includeTopBot, int offset, int pageSize, bool useTypeOf = false, string order = "NONE")
	public final String ConvertToGetSuperconceptsOf(CNL.DL.Node n, boolean direct, boolean includeTopBot, int offset, int pageSize, boolean useTypeOf, String order)
	{
		return ConvertToGetRelatedConceptOf(n, true, direct, includeTopBot, offset, pageSize, useTypeOf, order);
	}


	public final String ConvertToGetSubconceptsOf(CNL.DL.Node n, boolean direct, boolean includeTopBot, int offset, int pageSize, boolean useTypeOf)
	{
		return ConvertToGetSubconceptsOf(n, direct, includeTopBot, offset, pageSize, useTypeOf, "NONE");
	}

	public final String ConvertToGetSubconceptsOf(CNL.DL.Node n, boolean direct, boolean includeTopBot, int offset, int pageSize)
	{
		return ConvertToGetSubconceptsOf(n, direct, includeTopBot, offset, pageSize, false, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ConvertToGetSubconceptsOf(CNL.DL.Node n, bool direct, bool includeTopBot, int offset, int pageSize, bool useTypeOf = false, string order = "NONE")
	public final String ConvertToGetSubconceptsOf(CNL.DL.Node n, boolean direct, boolean includeTopBot, int offset, int pageSize, boolean useTypeOf, String order)
	{
		return ConvertToGetRelatedConceptOf(n, false, direct, includeTopBot, offset, pageSize, useTypeOf, order);
	}


	public final String ConvertToGetRelatedConceptOf(CNL.DL.Node n, boolean meanSuperConcept, boolean direct, boolean includeTopBot, int offset, int pageSize, boolean useTypeOf)
	{
		return ConvertToGetRelatedConceptOf(n, meanSuperConcept, direct, includeTopBot, offset, pageSize, useTypeOf, "NONE");
	}

	public final String ConvertToGetRelatedConceptOf(CNL.DL.Node n, boolean meanSuperConcept, boolean direct, boolean includeTopBot, int offset, int pageSize)
	{
		return ConvertToGetRelatedConceptOf(n, meanSuperConcept, direct, includeTopBot, offset, pageSize, false, "NONE");
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ConvertToGetRelatedConceptOf(CNL.DL.Node n, bool meanSuperConcept, bool direct, bool includeTopBot, int offset, int pageSize, bool useTypeOf = false, string order = "NONE")
	public final String ConvertToGetRelatedConceptOf(CNL.DL.Node n, boolean meanSuperConcept, boolean direct, boolean includeTopBot, int offset, int pageSize, boolean useTypeOf, String order)
	{
		this.useTypeOf = useTypeOf;
		freeVarIdBase = 0;
		String selectVars;
		String whereBlock;
		boolean distinct = false;

		boolean ordering = !order.equals("NONE");

		try (activeFreeVarId.set(newFreeVarId()))
		{
			Object tempVar = n.accept(this);
			SparqlNode sparqlNode = tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null;
			if (n instanceof cognipy.cnl.dl.Top)
			{
				sparqlNode = new SparqlTop(getOwlNC(), sparqlNode.GetFreeVariableId());
			}

			distinct = sparqlNode.UseDistinct();

			selectVars = sparqlNode.GetFreeVariableId();
			whereBlock = sparqlNode.ToCombinedBlock(meanSuperConcept, false, direct, includeTopBot, true);

			if (ordering)
			{
				whereBlock += ". " + selectVars + " <http://www.ontorion.com#label> ?z1";
			}
		}


		return PREAMBLE + "SELECT " + (distinct ? "DISTINCT " : " ") + selectVars + "\r\n" + "WHERE {" + whereBlock + "}" + (ordering ? String.format(" ORDER BY %1$s(?z1) ", order) : "") + (pageSize > -1 ? " LIMIT " + pageSize + " OFFSET " + offset : "");
	}


	public final String ConvertToSolutionExists(CNL.DL.Node n)
	{
		return ConvertToSolutionExists(n, false);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string ConvertToSolutionExists(CNL.DL.Node n, bool useTypeOf = false)
	public final String ConvertToSolutionExists(CNL.DL.Node n, boolean useTypeOf)
	{
		this.useTypeOf = useTypeOf;
		freeVarIdBase = 0;

		try (activeFreeVarId.set(newFreeVarId()))
		{
			Object tempVar = n.accept(this);
			SparqlNode sparqlNode = tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null;

			return PREAMBLE + "ASK {" + sparqlNode.ToCombinedBlock(false) + "}";
		}
	}
	@Override
	public Object Visit(Top e)
	{
		if (isKindOf.get().equals("C"))
		{
			return new SparqlInstanceOfDefinedClass(getOwlNC(), activeFreeVarId.get(), null, newFreeVarId(), useTypeOf);
		}
		return null;
	}

	@Override
	public Object Visit(Atomic e)
	{
		if (isKindOf.get().equals("C"))
		{
			return new SparqlInstanceOfDefinedClass(getOwlNC(), activeFreeVarId.get(), e.id, null, useTypeOf);
		}
		if (isKindOf.get().equals("R"))
		{
			return Tuple.Create(false, e.id);
		}
		else
		{
			return e.id;
		}
	}

	@Override
	public Object Visit(RoleInversion e)
	{
		try (isKindOf.set("R"))
		{
			Object tempVar = e.R.accept(this);
			Tuple<Boolean, String> r = tempVar instanceof Tuple<Boolean, String> ? (Tuple<Boolean, String>)tempVar : null;
			return Tuple.Create(!r.Item1, r.Item2);
		}
	}

	@Override
	public Object Visit(SomeRestriction e)
	{
		Tuple<Boolean, String> r;
		try (isKindOf.set("R"))
		{
			Object tempVar = e.R.accept(this);
			r = tempVar instanceof Tuple<Boolean, String> ? (Tuple<Boolean, String>)tempVar : null;
		}

		if (e.C instanceof InstanceSet)
		{
			InstanceSet instSet = e.C instanceof InstanceSet ? (InstanceSet)e.C : null;
			if (instSet.Instances.size() == 1)
			{
				Object tempVar2 = instSet.Instances.get(0);
				return new SparqlRelatedToInstance(getOwlNC(), activeFreeVarId.get(), (tempVar2 instanceof NamedInstance ? (NamedInstance)tempVar2 : null).name, r.Item2, r.Item1);
			}
			else
			{
				ArrayList<SparqlNode> nodes = new ArrayList<SparqlNode>();
				for (Instance inst : instSet.Instances)
				{
					nodes.add(new SparqlRelatedToInstance(getOwlNC(), activeFreeVarId.get(), (inst instanceof NamedInstance ? (NamedInstance)inst : null).name, r.Item2, r.Item1));
				}
				return new SparqlOr(getOwlNC(), activeFreeVarId.get(), nodes);
			}
		}
		else if (e.C instanceof Top)
		{
			return new SparqlRelatedToVariable(getOwlNC(), activeFreeVarId.get(), newFreeVarId(), r.Item2, r.Item1, true);
		}
		else
		{
			SparqlNode c;
			try (activeFreeVarId.set(newFreeVarId()))
			{
				Object tempVar3 = e.C.accept(this);
				c = tempVar3 instanceof SparqlNode ? (SparqlNode)tempVar3 : null;
			}

			SparqlRelatedToVariable d = new SparqlRelatedToVariable(getOwlNC(), activeFreeVarId.get(), c.GetFreeVariableId(), r.Item2, r.Item1);

			return new SparqlAnd(getOwlNC(), activeFreeVarId.get(), new ArrayList<SparqlNode>(Arrays.asList( c, d )));
		}
	}

	@Override
	public Object Visit(SelfReference e)
	{
		Tuple<Boolean, String> r;
		try (isKindOf.set("R"))
		{
			Object tempVar = e.R.accept(this);
			r = tempVar instanceof Tuple<Boolean, String> ? (Tuple<Boolean, String>)tempVar : null;
		}

		return new SparqlRelatedToVariable(getOwlNC(), activeFreeVarId.get(), activeFreeVarId.get(), r.Item2, r.Item1);
	}

	@Override
	public Object Visit(BoundNot e)
	{
		Object tempVar = e.B.accept(this);
		return new SparqlNot(getOwlNC(), activeFreeVarId.get(), tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null);
	}

	@Override
	public Object Visit(BoundAnd e)
	{
		Object tempVar = x.accept(this);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		ArrayList<Object> nodes = (from x in e.List select (tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null)).ToList();

		return new SparqlAnd(getOwlNC(), activeFreeVarId.get(), nodes);
	}

	@Override
	public Object Visit(BoundOr e)
	{
		Object tempVar = x.accept(this);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		ArrayList<Object> nodes = (from x in e.List select (tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null)).ToList();

		return new SparqlOr(getOwlNC(), activeFreeVarId.get(), nodes);
	}

	@Override
	public Object Visit(cognipy.cnl.dl.BoundFacets e)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		var nodes = (from x in e.FL.List select new SparqlRelatedToValueFilter(getOwlNC(), activeFreeVarId.get(), activeAttribute.get(), x.Kind, x.Kind.equals("=") ? null : newFreeVarId(), x.V)).<SparqlNode>ToList();

		if (nodes.Count == 1)
		{
			return nodes.First();
		}
		else
		{
			return new SparqlAnd(getOwlNC(), activeFreeVarId.get(), nodes);
		}
	}

	@Override
	public Object Visit(cognipy.cnl.dl.FacetList e)
	{
		ArrayList<Tuple<String, CNL.DL.Value>> r = new ArrayList<Tuple<String, CNL.DL.Value>>();
		for (Facet F : e.List)
		{
			r.add(Tuple.Create(F.Kind, F.V));
		}
		return r;
	}

	@Override
	public Object Visit(BoundVal e)
	{
		return new SparqlRelatedToValueFilter(getOwlNC(), activeFreeVarId.get(), activeAttribute.get(), e.Kind, e.Kind.equals("=") ? null : newFreeVarId(), e.V);
	}

	private VisitingParam<String> activeAttribute = new VisitingParam<String>(null);

	@Override
	public Object Visit(SomeValueRestriction e)
	{
		String d;
		try (isKindOf.set("D"))
		{
			Object tempVar = e.R.accept(this);
			d = tempVar instanceof String ? (String)tempVar : null;
		}
		try (activeAttribute.set(d))
		{
			return e.B.accept(this);
		}
	}

	@Override
	public Object Visit(TopBound e)
	{
		return new SparqlRelatedToVariable(getOwlNC(), activeFreeVarId.get(), newFreeVarId(), activeAttribute.get(), false, true);
	}

	@Override
	public Object Visit(InstanceSet e)
	{
		Object tempVar = e.Instances.get(0);
		return new SparqlConstantInstance(getOwlNC(), activeFreeVarId.get(), (tempVar instanceof NamedInstance ? (NamedInstance)tempVar : null).name, useTypeOf);
	}

	@Override
	public Object Visit(ConceptAnd e)
	{
		try (isKindOf.set("C"))
		{
			ArrayList<SparqlNode> nodes = new ArrayList<SparqlNode>();
			for (Node expr : e.Exprs)
			{
				Object tempVar = expr.accept(this);
				nodes.add(tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null);
			}
			return new SparqlAnd(getOwlNC(), activeFreeVarId.get(), nodes);
		}
	}

	@Override
	public Object Visit(ConceptOr e)
	{
		try (isKindOf.set("C"))
		{
			ArrayList<SparqlNode> nodes = new ArrayList<SparqlNode>();
			for (Node expr : e.Exprs)
			{
				Object tempVar = expr.accept(this);
				nodes.add(tempVar instanceof SparqlNode ? (SparqlNode)tempVar : null);
			}
			return new SparqlOr(getOwlNC(), activeFreeVarId.get(), nodes);
		}
	}

}