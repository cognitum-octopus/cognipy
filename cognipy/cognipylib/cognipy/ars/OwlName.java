package cognipy.ars;

import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import cognipy.*;
import java.util.*;

public class OwlName
{
	private static String globalInstanceIndicator = IRIParser.getSecondCharForInternalUse();

	public static class Parts
	{
		public String ns;
		public String name;
		public boolean global;
		/** 
		 IRI compliant name
		*/
		public final String getEncodedName()
		{
			return IRIParser.encodeToIRI((global ? globalInstanceIndicator : "") + name);
		}
		public final OwlName Combine()
		{
			String sep = "";
			if (!ns.endsWith("/") && !ns.endsWith("#") && !ns.contains("#"))
			{
				sep = "#";
			}

			if (name.contains("/") && ns.endsWith("/"))
			{
				ns = ns.substring(0, ns.length() - 1) + "#";
			}

			OwlName tempVar = new OwlName();
			tempVar.iri = IRI.create(ns + sep + getEncodedName());
			return tempVar;
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [ThreadStatic] static Dictionary<string, Parts> cache = null;
	private static HashMap<String, Parts> cache = null;
	public IRI iri;
	public final Parts Split()
	{
		if (cache == null)
		{
			cache = new HashMap<String, Parts>();
		}
		try
		{
			String key = iri.toString();
			if (cache.containsKey(key))
			{
				return cache.get(key);
			}
			else
			{
				if (iri.getScheme().equals("file"))
				{
					String shortForm = iri.getFragment();
					String ns = iri.getNamespace();
					boolean isGlobal = shortForm.startsWith(globalInstanceIndicator);
					if (isGlobal)
					{
						shortForm = shortForm.substring(1);
					}
					Parts tempVar = new Parts();
					tempVar.ns = ns;
					tempVar.name = shortForm;
					tempVar.global = isGlobal;
					cache.put(key, tempVar);
					return cache.get(key);
				}
				else
				{
					Uri u = new Uri(key, UriKind.RelativeOrAbsolute);
					if (u.IsAbsoluteUri)
					{
						if (!tangible.StringHelper.isNullOrWhiteSpace(u.Fragment))
						{
							if (u.Fragment.startsWith("#"))
							{
								String shortForm = Uri.UnescapeDataString(u.Fragment.substring(1));
								String ns = key.substring(0, key.length() - IRIParser.encodeToIRI(shortForm).length() - 1) + "#";
								boolean isGlobal = shortForm.startsWith(globalInstanceIndicator);
								if (isGlobal)
								{
									shortForm = shortForm.substring(1);
								}
								Parts tempVar2 = new Parts();
								tempVar2.ns = ns;
								tempVar2.name = shortForm;
								tempVar2.global = isGlobal;
								cache.put(key, tempVar2);
								return cache.get(key);
							}
						}
						if (!tangible.StringHelper.isNullOrWhiteSpace(u.Segments[u.Segments.Count() - 1]))
						{
							String shortForm = Uri.UnescapeDataString(u.Segments[u.Segments.Count() - 1]);
							String segmFullyEncoded = IRIParser.encodeToIRI(IRIParser.decodeIRI(u.Segments[u.Segments.Count() - 1]));
							String ns = u.OriginalString.substring(0, u.OriginalString.length() - segmFullyEncoded.length());
							boolean isGlobal = shortForm.startsWith(globalInstanceIndicator);
							if (isGlobal)
							{
								shortForm = shortForm.substring(1);
							}
							Parts tempVar3 = new Parts();
							tempVar3.ns = ns;
							tempVar3.name = shortForm;
							tempVar3.global = isGlobal;
							cache.put(key, tempVar3);
							return cache.get(key);
						}
					}
					else
					{
						String str = IRIParser.decodeIRI(iri.toString());
						if (str.contains('#'))
						{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
							var pr = str.split("[#]", -1);
							String shortForm = pr[1];
							String ns = pr[0] + "#";
							boolean isGlobal = shortForm.startsWith(globalInstanceIndicator);
							if (isGlobal)
							{
								shortForm = shortForm.substring(1);
							}
							Parts tempVar4 = new Parts();
							tempVar4.ns = ns;
							tempVar4.name = shortForm;
							tempVar4.global = isGlobal;
							return tempVar4;
						}
						else
						{
							boolean isGlobal = str.startsWith(globalInstanceIndicator);
							if (isGlobal)
							{
								str = str.substring(1);
							}
							Parts tempVar5 = new Parts();
							tempVar5.name = str;
							tempVar5.global = isGlobal;
							return tempVar5;
						}
					}
				}
			}
			return null;
		}
		catch (RuntimeException e)
		{
			String str = IRIParser.decodeIRI(iri.toString());
			if (str.contains('#'))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var pr = str.split("[#]", -1);
				String shortForm = pr[1];
				String ns = pr[0] + "#";
				boolean isGlobal = shortForm.startsWith(globalInstanceIndicator);
				if (isGlobal)
				{
					shortForm = shortForm.substring(1);
				}
				Parts tempVar6 = new Parts();
				tempVar6.ns = ns;
				tempVar6.name = shortForm;
				tempVar6.global = isGlobal;
				return tempVar6;
			}
			else
			{
				boolean isGlobal = str.startsWith(globalInstanceIndicator);
				if (isGlobal)
				{
					str = str.substring(1);
				}
				Parts tempVar7 = new Parts();
				tempVar7.name = str;
				tempVar7.global = isGlobal;
				return tempVar7;
			}
		}

	}

	public static Parts Split(String uri)
	{
		OwlName tempVar = new OwlName();
		tempVar.iri = IRI.create(uri);
		return tempVar.Split();
	}

	public static EntityKind getKind(OWLEntity ent)
	{
		if (ent instanceof OWLClass)
		{
			return EntityKind.Concept;
		}
		else if (ent instanceof OWLIndividual)
		{
			return EntityKind.Instance;
		}
		else if (ent instanceof OWLDataProperty)
		{
			return EntityKind.DataRole;
		}
		else if (ent instanceof OWLObjectProperty)
		{
			return EntityKind.Role;
		}
		else if (ent instanceof OWLDatatype)
		{
			return EntityKind.DataType;
		}
		else if (ent instanceof OWLAnnotationProperty)
		{
			return EntityKind.Annotation;
		}
		else if (ent instanceof SWRLVariable)
		{
			return EntityKind.SWRLVariable;
		}
		else
		{
			throw new IllegalStateException();
		}
	}

}