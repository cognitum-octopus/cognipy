package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public enum ReasoningMode
{
	SROIQ,
	RL,
	NONE;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static ReasoningMode forValue(int value)
	{
		return values()[value];
	}
}