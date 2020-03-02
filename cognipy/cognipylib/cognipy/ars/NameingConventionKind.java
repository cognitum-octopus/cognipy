package cognipy.ars;

import org.semanticweb.owlapi.model.*;
import org.semanticweb.owlapi.util.*;
import org.semanticweb.owlapi.vocab.*;
import cognipy.*;
import java.util.*;

//using Ontorion.OWL;


public enum NameingConventionKind
{
	Smart(0),
	CamelCase(1),
	Dashed(2),
	Underscored(3);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, NameingConventionKind> mappings;
	private static java.util.HashMap<Integer, NameingConventionKind> getMappings()
	{
		if (mappings == null)
		{
			synchronized (NameingConventionKind.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, NameingConventionKind>();
				}
			}
		}
		return mappings;
	}

	private NameingConventionKind(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static NameingConventionKind forValue(int value)
	{
		return getMappings().get(value);
	}
}