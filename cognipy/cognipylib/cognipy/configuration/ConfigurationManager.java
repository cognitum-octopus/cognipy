package cognipy.configuration;

import cognipy.*;
import java.util.*;
import java.io.*;

public interface ConfigurationManager extends Closeable
{
	/** 
	 Gets and sets the reasoning mode for the current database
	*/
	ReasoningMode getReasoningModeTBox();

	ReasoningMode getReasoningModeABox();

	String getReasoningProfile();
	void setReasoningProfile(String value);
	ReasoningConfiguration getReasoningProfileEnum();
	/** 
	 Gets and sets the reasoning radius for the current database
	*/
	int getReasoningRadius();
	void setReasoningRadius(int value);

	/** 
	 Gets and sets the breaking change radius for the current database
	*/
	int getBreakingChangeRadius();
	void setBreakingChangeRadius(int value);

	/** 
	 Gets or sets a boolean deciding if integrity constraints should be checked
	*/
	boolean getCheckIntegrityConstraints();
	void setCheckIntegrityConstraints(boolean value);

	String getDefaultNamespace();
	void setDefaultNamespace(String value);

	boolean getIsSPARQLEndpointPublic();
	void setIsSPARQLEndpointPublic(boolean value);

	String getDatabaseVersion();

	/** 
	 Get and sets if the cache is enabled.
	*/
	boolean getCacheEnabled();
	void setCacheEnabled(boolean value);

	/** 
	 Get and sets the Domain and Range will be materialized.
	*/
	boolean getDomainAndRangeMaterialization();
	void setDomainAndRangeMaterialization(boolean value);

	GraphBackend getGraphDatabase();

	/** 
	 Get and sets the cache database to use
	*/
	CacheBackend getCacheDatabase();
	void setCacheDatabase(CacheBackend value);

	HashMap<ConfigurationEntry, String> getDatabaseConfiguration();

	void changeDatabaseConfiguration(HashMap<ConfigurationEntry, Object> configToChange);

	/** 
	 Get and sets if the namespace modularization is enabled.
	*/
	boolean getNamespaceModularizationEnabled();
	void setNamespaceModularizationEnabled(boolean value);
}