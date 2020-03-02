package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public enum ProfileValidationOptions
{
	OWLRL,
	OWLRLP,
	OWLEL;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static ProfileValidationOptions forValue(int value)
	{
		return values()[value];
	}
}