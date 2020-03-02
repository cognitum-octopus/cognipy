package cognipy;

import java.util.*;

public enum StatementType
{
	Concept,
	Rule,
	Role,
	Instance,
	Annotation,
	Constraint;

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