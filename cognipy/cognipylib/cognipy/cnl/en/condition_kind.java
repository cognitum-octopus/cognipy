package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

////////////////// SWRL //////////////////////////////////////




























public class condition_kind
{
	public static final condition_kind None = new condition_kind(0);
	public static final condition_kind Not = new condition_kind(0x1);
	public static final condition_kind Inv = new condition_kind(0x2);
	public static final condition_kind All = new condition_kind(0x1 | 0x2);

	public static final int SIZE = java.lang.Integer.SIZE;

	private int intValue;
	private static java.util.HashMap<Integer, condition_kind> mappings;
	private static java.util.HashMap<Integer, condition_kind> getMappings()
	{
		if (mappings == null)
		{
			synchronized (condition_kind.class)
			{
				if (mappings == null)
				{
					mappings = new java.util.HashMap<Integer, condition_kind>();
				}
			}
		}
		return mappings;
	}

	private condition_kind(int value)
	{
		intValue = value;
		synchronized (condition_kind.class)
		{
			getMappings().put(value, this);
		}
	}

	public int getValue()
	{
		return intValue;
	}

	public static condition_kind forValue(int value)
	{
		synchronized (condition_kind.class)
		{
			condition_kind enumObj = getMappings().get(value);
			if (enumObj == null)
			{
				return new condition_kind(value);
			}
			else
			{
				return enumObj;
			}
		}
	}
}