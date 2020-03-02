package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public enum GraphBackend
{
	Titan,
	Virtuoso,
	BasicMode, //, Oracle
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
	 Cumulus;
//#endif

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static GraphBackend forValue(int value)
	{
		return values()[value];
	}
}