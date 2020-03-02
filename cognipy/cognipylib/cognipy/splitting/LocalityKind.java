package cognipy.splitting;

import cognipy.cnl.dl.*;
import cognipy.*;
import java.util.*;

public enum LocalityKind
{
	Top(1),
	Bottom(0);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, LocalityKind> mappings;
	private static java.util.HashMap<Integer, LocalityKind> getMappings()
	{
		if (mappings == null)
		{
			synchronized (LocalityKind.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, LocalityKind>();
				}
			}
		}
		return mappings;
	}

	private LocalityKind(int value)
	{
		intValue = value;
		getMappings().put(value, this);
	}

	public int getValue()
	{
		return intValue;
	}

	public static LocalityKind forValue(int value)
	{
		return getMappings().get(value);
	}
}