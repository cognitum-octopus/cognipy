package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public enum ConfigurationEntry
{
	ReasoningProfile,
	ReasoningRadius,
	BreakingChangeRadius,
	IntegrityConstraints,
	DefaultNamespace,
	PublicSPARQLEndpoint,
	DatabaseVersion,
	GraphDatabase,
	CacheEnabled,
	CacheDatabase,
	NamespaceModularizationEnabled,
	DomainAndRangeMaterialization;

	public static final int SIZE = java.lang.Integer.SIZE;

	public int getValue()
	{
		return this.ordinal();
	}

	public static ConfigurationEntry forValue(int value)
	{
		return values()[value];
	}
}