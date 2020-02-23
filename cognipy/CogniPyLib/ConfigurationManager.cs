using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace Ontorion.Configuration
{
    /// <summary>
    /// OWLDL --> DL/DL
    /// OWLRLP --> DL/RL
    /// NONE --> no reasoning executed
    /// </summary>
    public enum ReasoningConfiguration { OWLDL,OWLRLP, NONE }
    public enum ProfileValidationOptions { OWLRL, OWLRLP, OWLEL }
    public enum ReasoningMode { SROIQ, RL, NONE }
    public enum MatMode { Tbox, Abox, Both, SWRLOnly };
    public enum GraphBackend { Titan, Virtuoso, BasicMode  //, Oracle
#if DEBUG
        , Cumulus 
#endif
    };
    public enum CacheBackend
    {
        InMemory, Cassandra
#if DEBUG
            , MemCached
#endif
    };
    public enum ConfigurationEntry { ReasoningProfile, ReasoningRadius, BreakingChangeRadius, IntegrityConstraints, DefaultNamespace, PublicSPARQLEndpoint, DatabaseVersion, GraphDatabase, CacheEnabled, CacheDatabase, NamespaceModularizationEnabled , DomainAndRangeMaterialization};
    public class DefaultValue
    {

        Version databaseVersion;
        /// <summary>
        /// Creates an instance of the default value class. While initializing, you can set the default values for some of the properties.
        /// </summary>
        /// <param name="DatabaseVersion"></param>
        /// <param name="default_namespace">default value for the namespace</param>
        /// <param name="default_graphdb">default value for the graph database</param>
        public DefaultValue(Version DatabaseVersion, string default_namespace=null,GraphBackend default_graphdb=GraphBackend.Titan)
        {
            if (!String.IsNullOrWhiteSpace(default_namespace))
                DEFAULT_NAMESPACE = default_namespace;

            DEFAULT_GRAPH_DB = default_graphdb;

            databaseVersion = DatabaseVersion;
        }

        string DEFAULT_NAMESPACE = "http://www.ontorion.com/testontology.owl#";
        GraphBackend DEFAULT_GRAPH_DB;

        public object getDefaultValue(ConfigurationEntry confEntry)
        {
            switch(confEntry)
            {
                case ConfigurationEntry.DefaultNamespace:
                    return DEFAULT_NAMESPACE;
                case ConfigurationEntry.IntegrityConstraints:
                    return false;
                case ConfigurationEntry.ReasoningProfile:
                    return ReasoningConfiguration.OWLRLP.ToString();
                case ConfigurationEntry.ReasoningRadius:
                    return 3;
                case ConfigurationEntry.BreakingChangeRadius:
                    return 3;
                case ConfigurationEntry.PublicSPARQLEndpoint:
                    return false;
                case ConfigurationEntry.DatabaseVersion:
                    return databaseVersion.ToString();
                case ConfigurationEntry.GraphDatabase:
                    return DEFAULT_GRAPH_DB.ToString();
                case ConfigurationEntry.CacheEnabled:
                    return false;
                case ConfigurationEntry.DomainAndRangeMaterialization:
                    return false;
                case ConfigurationEntry.CacheDatabase:
                    return CacheBackend.InMemory.ToString();
                case ConfigurationEntry.NamespaceModularizationEnabled:
                    return false;
                default:
                    throw new Exception("No default value specified for ConfigurationEntry "+confEntry.ToString());
            }
        }
    }

    public interface ConfigurationManager : IDisposable
    {
        /// <summary>
        /// Gets and sets the reasoning mode for the current database
        /// </summary>
        ReasoningMode ReasoningModeTBox { get; }

        ReasoningMode ReasoningModeABox { get; }

        string ReasoningProfile { get; set; }
        ReasoningConfiguration ReasoningProfileEnum { get; }
        /// <summary>
        /// Gets and sets the reasoning radius for the current database
        /// </summary>
        int ReasoningRadius { get; set; }

        /// <summary>
        /// Gets and sets the breaking change radius for the current database
        /// </summary>
        int BreakingChangeRadius { get; set; }

        /// <summary>
        /// Gets or sets a boolean deciding if integrity constraints should be checked
        /// </summary>
        bool CheckIntegrityConstraints { get; set; }

        string DefaultNamespace { get; set; }

        bool IsSPARQLEndpointPublic { get; set; }

        string DatabaseVersion { get; }

        /// <summary>
        /// Get and sets if the cache is enabled.
        /// </summary>
        bool CacheEnabled { get; set; }

        /// <summary>
        /// Get and sets the Domain and Range will be materialized.
        /// </summary>
        bool DomainAndRangeMaterialization { get; set; }

        GraphBackend GraphDatabase { get; }

        /// <summary>
        /// Get and sets the cache database to use
        /// </summary>
        CacheBackend CacheDatabase { get; set; }

        Dictionary<ConfigurationEntry, string> getDatabaseConfiguration();

        void changeDatabaseConfiguration(Dictionary<ConfigurationEntry, object> configToChange);

        /// <summary>
        /// Get and sets if the namespace modularization is enabled.
        /// </summary>
        bool NamespaceModularizationEnabled { get; set; }
    }
}
