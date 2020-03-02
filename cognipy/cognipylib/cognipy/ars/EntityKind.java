package cognipy.ars;

import cognipy.*;
import java.util.*;

public enum EntityKind
{
	Concept,
	Role,
	DataRole,
	DataType,
	Instance,
	SWRLVariable,
	Annotation,
	Statement;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static EntityKind forValue(int value)
	{
		return values()[value];
	}
}