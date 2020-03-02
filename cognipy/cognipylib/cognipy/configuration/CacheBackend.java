package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public enum CacheBackend
{
	InMemory,
	Cassandra,
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
		 MemCached;
//#endif

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static CacheBackend forValue(int value)
	{
		return values()[value];
	}
}