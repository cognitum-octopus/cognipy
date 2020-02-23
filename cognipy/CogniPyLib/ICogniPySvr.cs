using FluentEditorClientLib;
using FluentEditorClientLib.models;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

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
        void LoadCnl(string filename, bool loadAnnotations, bool materialize, bool modalCheck);
        IEnumerable<AnnotationResult> GetAnnotationsForSignature(IEnumerable<string> cnlEntities);
        Dictionary<string, ConstraintResult> GetConstraints(List<string> descriptions);
        List<FeClientStatement> ToCNLStatementList();
        Dictionary<string, InstanceDescription> DescribeInstances(string query);
        List<string> GetSubConceptsOf(string cnlName, bool direct);
        List<string> GetInstancesOf(string cnlName, bool direct);
        void KnowledgeInsert(string text, bool loadAnnotations, bool materialize);
        void KnowledgeDelete(string text, bool materialize);

        void LoadCnlFromString(string cnl, bool loadAnnotations, bool materialize, bool modalCheck);
        void LoadRdf(string uri, bool loadAnnotations, bool materialize, bool modalCheck);
        void LoadRdfFromString(string rdf, bool loadAnnotations, bool materialize, bool modalCheck);
        string ToCNL(bool includeAnnotations);
        List<string> ToCNLList(bool includeAnnotations);
        List<string> GetSuperConceptsOf(string cnlName, bool direct);
    }

}
