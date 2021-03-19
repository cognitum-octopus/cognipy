using CogniPy.models;
using System.Collections.Generic;

namespace CogniPy
{
    public interface IModularizer
    {
        void LoadCnlFromString(string cnl);
        void LoadFromReasoner(ICogniPySvr client);
        void KnowledgeInsert(string text);
        void KnowledgeDelete(string text);
        IEnumerable<string> GetModule(IEnumerable<string> signature);
    }

    public interface ICogniPySvr
    {
        void LoadCnl(string filename, bool loadAnnotations, bool materialize, bool modalCheck, bool throwOnError);
        IEnumerable<AnnotationResult> GetAnnotationsForSignature(IEnumerable<string> cnlEntities);
        Dictionary<string, ConstraintResult> GetConstraints(List<string> descriptions);
        List<CogniPyStatement> ToCNLStatementList();
        Dictionary<string, InstanceDescription> DescribeInstances(string query);
        List<string> GetSubConceptsOf(string cnlName, bool direct);
        List<string> GetInstancesOf(string cnlName, bool direct);
        void KnowledgeInsert(string text, bool loadAnnotations, bool materialize);
        void KnowledgeDelete(string text, bool materialize);

        void LoadCnlFromString(string cnl, bool loadAnnotations, bool materialize, bool modalCheck, bool throwOnError);
        void LoadRdf(string uri, bool loadAnnotations, bool materialize, bool modalCheck, bool throwOnError);
        void LoadRdfFromString(string rdf, bool loadAnnotations, bool materialize, bool modalCheck, bool throwOnError);
        string ToCNL(bool includeAnnotations);
        List<string> ToCNLList(bool includeAnnotations);
        List<string> GetSuperConceptsOf(string cnlName, bool direct);
    }

}
