package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

/** 
 OWLDL --> DL/DL
 OWLRLP --> DL/RL
 NONE --> no reasoning executed
*/
public enum ReasoningConfiguration
{
	OWLDL,
	OWLRLP,
	NONE;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static ReasoningConfiguration forValue(int value)
	{
		return values()[value];
	}
}