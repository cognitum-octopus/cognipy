package cognipy;

import cognipy.models.*;
import java.util.*;

public interface IModularizer
{
	void LoadCnlFromString(String cnl);
	void LoadFromReasoner(ICogniPySvr client);
	void KnowledgeInsert(String text);
	void KnowledgeDelete(String text);
	java.lang.Iterable<String> GetModule(java.lang.Iterable<String> signature);
}