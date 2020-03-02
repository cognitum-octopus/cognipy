package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public class DefaultValue
{

	private Version databaseVersion;
	/** 
	 Creates an instance of the default value class. While initializing, you can set the default values for some of the properties.
	 
	 @param DatabaseVersion
	 @param default_namespace default value for the namespace
	 @param default_graphdb default value for the graph database
	*/

	public DefaultValue(Version DatabaseVersion, String default_namespace)
	{
		this(DatabaseVersion, default_namespace, GraphBackend.Titan);
	}

	public DefaultValue(Version DatabaseVersion)
	{
		this(DatabaseVersion, null, GraphBackend.Titan);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public DefaultValue(Version DatabaseVersion, string default_namespace = null, GraphBackend default_graphdb = GraphBackend.Titan)
	public DefaultValue(Version DatabaseVersion, String default_namespace, GraphBackend default_graphdb)
	{
		if (!tangible.StringHelper.isNullOrWhiteSpace(default_namespace))
		{
			DEFAULT_NAMESPACE = default_namespace;
		}

		DEFAULT_GRAPH_DB = default_graphdb;

		databaseVersion = DatabaseVersion;
	}

	private String DEFAULT_NAMESPACE = "http://www.ontorion.com/testontology.owl#";
	private GraphBackend DEFAULT_GRAPH_DB = GraphBackend.values()[0];

	public final Object getDefaultValue(ConfigurationEntry confEntry)
	{
		switch (confEntry)
		{
			case DefaultNamespace:
				return DEFAULT_NAMESPACE;
			case IntegrityConstraints:
				return false;
			case ReasoningProfile:
				return ReasoningConfiguration.OWLRLP.toString();
			case ReasoningRadius:
				return 3;
			case BreakingChangeRadius:
				return 3;
			case PublicSPARQLEndpoint:
				return false;
			case DatabaseVersion:
				return databaseVersion.toString();
			case GraphDatabase:
				return DEFAULT_GRAPH_DB.toString();
			case CacheEnabled:
				return false;
			case DomainAndRangeMaterialization:
				return false;
			case CacheDatabase:
				return CacheBackend.InMemory.toString();
			case NamespaceModularizationEnabled:
				return false;
			default:
				throw new RuntimeException("No default value specified for ConfigurationEntry " + confEntry.toString());
		}
	}
}