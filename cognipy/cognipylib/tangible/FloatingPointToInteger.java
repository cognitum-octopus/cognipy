package tangible;

//----------------------------------------------------------------------------------------
//	Copyright © 2007 - 2020 Tangible Software Solutions, Inc.
//	This class can be used by anyone provided that the copyright notice remains intact.
//
//	This class is used to convert System.Convert methods which convert from
//	floating point types to integral types.
//----------------------------------------------------------------------------------------
public final class FloatingPointToInteger
{
	public static byte ToSByte(double source)
	{
		byte floor = (byte)Math.floor(source);
		if (Math.abs(source - floor) == 0.5)
		{
			if (floor % 2 == 0)
				return floor;
			else
				return (byte)Math.ceil(source);
		}
		else if (Math.abs(source - floor) < 0.5)
			return floor;
		else
			return (byte)Math.ceil(source);
	}

	public static short ToInt16(double source)
	{
		short floor = (short)Math.floor(source);
		if (Math.abs(source - floor) == 0.5)
		{
			if (floor % 2 == 0)
				return floor;
			else
				return (short)Math.ceil(source);
		}
		else if (Math.abs(source - floor) < 0.5)
			return floor;
		else
			return (short)Math.ceil(source);
	}

	public static int ToInt32(double source)
	{
		int floor = (int)Math.floor(source);
		if (Math.abs(source - floor) == 0.5)
		{
			if (floor % 2 == 0)
				return floor;
			else
				return (int)Math.ceil(source);
		}
		else if (Math.abs(source - floor) < 0.5)
			return floor;
		else
			return (int)Math.ceil(source);
	}

	public static long ToInt64(double source)
	{
		long floor = (long)Math.floor(source);
		if (Math.abs(source - floor) == 0.5)
		{
			if (floor % 2 == 0)
				return floor;
			else
				return (long)Math.ceil(source);
		}
		else if (Math.abs(source - floor) < 0.5)
			return floor;
		else
			return (long)Math.ceil(source);
	}
}