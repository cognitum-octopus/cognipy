package cognipy.ars;

import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

public class DLToOWLNameConv
{
	private HashMap<String, String> Pfx2ns = new HashMap<String, String>();
	private HashMap<String, String> Ns2pfx = new HashMap<String, String>();
	private String defaultNs;

	private cognipy.cnl.en.endict lex = null;

	public final void ClearOWLFormat()
	{
		Pfx2ns = new HashMap<String, String>();
		Ns2pfx = new HashMap<String, String>();
		lex = null;
	}

	public final void setOWLFormat(String defaultNS, PrefixOWLOntologyFormat namespaceManager, cognipy.cnl.en.endict lex)
	{
		this.defaultNs = defaultNS;
		Map map = namespaceManager.getPrefixName2PrefixMap();
		Iterator keys = map.keySet().iterator();
		while (keys.hasNext())
		{
			String k = keys.next().toString();
			String v = map.get(k).toString();
			k = k.split("[:]", -1)[0];
			if (tangible.StringHelper.isNullOrEmpty(k))
			{
				defaultNs = v;
				continue;
			}
			if (!Pfx2ns.containsKey(k))
			{
				Pfx2ns.put(k, v);
			}
			if (!Ns2pfx.containsKey(v))
			{
				Ns2pfx.put(v, k);
			}
		}

		this.lex = lex;
	}

	private IOwlNameingConvention owlNameingConvention = new OwlNameingConventionCamelCase();
	public HashMap<String, String> InvUriMappings = new HashMap<String, String>();

	private static NamespaceUtil namespaceUtil = new NamespaceUtil();

	// this is used ToDL names --> namespace should be inside <>
	private String ns2pfx(String arg)
	{
		if (arg == null)
		{
			return "<" + defaultNs + ">";
		}

		if (!arg.endsWith("/") && !arg.endsWith("#") && !arg.contains("#"))
		{
			arg += "#";
		}

		if (!Ns2pfx.containsKey(arg) && !arg.startsWith("<") && !arg.endsWith(">"))
		{
			return "<" + arg + ">";
		}
		else if (!Ns2pfx.containsKey(arg))
		{
			return arg;
		}

		return Ns2pfx.get(arg);
	}

	private String pfx2ns(String arg)
	{
		if (arg == null)
		{
			return defaultNs;
		}

		if (!Pfx2ns.containsKey(arg))
		{
			if (arg.startsWith("<") && arg.endsWith(">"))
			{
				String argg = arg.substring(1, 1 + arg.length() - 2);
				if (!argg.endsWith("/") && !argg.endsWith("#") && !argg.contains("#"))
				{
					argg += "#";
				}
				return argg;
			}
			else
			{
				if (namespaceUtil.getNamespace2PrefixMap().containsValue(arg))
				{
					Iterator iter = namespaceUtil.getNamespace2PrefixMap().keySet().iterator();
					while (iter.hasNext())
					{
						Object k = iter.next();
						if (namespaceUtil.getNamespace2PrefixMap().get(k).toString().equals(arg))
						{
							return k.toString();
						}
					}
				}
				else
				{
					return "http://unknown.prefix/" + arg + "#";
				}
			}
		}

		return Pfx2ns.get(arg);
	}

	public final IRI getIRIFromId(String qname, EntityKind makeFor)
	{
		if (InvUriMappings.containsKey(qname))
		{
			return IRI.create(InvUriMappings.get(qname));
		}
		else
		{
			DlName dl = new DlName();
			dl.id = qname;
			cognipy.cnl.dl.DlName.Parts dlp = dl.Split();
			if (InvUriMappings.containsKey(dlp.name) && ns2pfx(IRI.create(InvUriMappings.get(dlp.name)).getNamespace()).equals(dlp.term))
			{
				return IRI.create(InvUriMappings.get(dlp.name));
			}
			else
			{
				DlName tempVar = new DlName();
				tempVar.id = qname;
				return owlNameingConvention.FromDL(tempVar, lex, (string arg) -> pfx2ns(arg), makeFor).iri;
			}
		}
	}

	public final DlName ToDL(String uri, ARS.EntityKind makeFor)
	{
		cognipy.ars.OwlName owlName = new cognipy.ars.OwlName();
		owlName.iri = IRI.create(uri);
		return owlNameingConvention.ToDL(owlName, lex, (string arg) -> ns2pfx(arg), makeFor);
	}

}