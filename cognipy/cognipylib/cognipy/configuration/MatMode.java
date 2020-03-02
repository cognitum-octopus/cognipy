package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public enum MatMode
{
	Tbox,
	Abox,
	Both,
	SWRLOnly;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static MatMode forValue(int value)
	{
		return values()[value];
	}
}