package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public enum StatementType
{
	Rule,
	Role,
	Concept,
	Instance,
	Annotation;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static StatementType forValue(int value)
	{
		return values()[value];
	}
}