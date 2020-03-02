package cognipy;

import cognipy.models.*;
import java.util.*;

public interface ICogniPySvr
{
	void LoadCnl(String filename, boolean loadAnnotations, boolean materialize, boolean modalCheck);
	java.lang.Iterable<AnnotationResult> GetAnnotationsForSignature(java.lang.Iterable<String> cnlEntities);
	HashMap<String, ConstraintResult> GetConstraints(ArrayList<String> descriptions);
	ArrayList<CogniPyStatement> ToCNLStatementList();
	HashMap<String, InstanceDescription> DescribeInstances(String query);
	ArrayList<String> GetSubConceptsOf(String cnlName, boolean direct);
	ArrayList<String> GetInstancesOf(String cnlName, boolean direct);
	void KnowledgeInsert(String text, boolean loadAnnotations, boolean materialize);
	void KnowledgeDelete(String text, boolean materialize);

	void LoadCnlFromString(String cnl, boolean loadAnnotations, boolean materialize, boolean modalCheck);
	void LoadRdf(String uri, boolean loadAnnotations, boolean materialize, boolean modalCheck);
	void LoadRdfFromString(String rdf, boolean loadAnnotations, boolean materialize, boolean modalCheck);
	String ToCNL(boolean includeAnnotations);
	ArrayList<String> ToCNLList(boolean includeAnnotations);
	ArrayList<String> GetSuperConceptsOf(String cnlName, boolean direct);
}