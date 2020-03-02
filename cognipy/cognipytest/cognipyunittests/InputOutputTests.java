package cognipyunittests;

import CogniPy.*;
import NUnit.Framework.*;
import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestFixture] public class InputOutputTests
public class InputOutputTests
{
	public static String getAssemblyDirectory()
	{
		String codeBase = Assembly.GetExecutingAssembly().CodeBase;
		UriBuilder uri = new UriBuilder(codeBase);
		String path = Uri.UnescapeDataString(uri.Path);
		return (new File(path)).getParent();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void LoadFileWithReference()
	public final void LoadFileWithReference()
	{
		CogniPySvr feClient = new CogniPySvr();

		feClient.LoadCnl(Path.Combine(getAssemblyDirectory(), "TestFiles", "CSHC.encnl"), true, true, false);
		String[] instances = new String[] {"Dynamic[sfo]", "Answer-2-1-10-1"};
		java.lang.Iterable<CogniPy.models.InstanceDescription> result = feClient.DescribeInstancesByName(instances);
		CollectionAssert.AreEquivalent(instances, result.Select(x -> x.Instance).ToArray());
	}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void ToCnlList()
	public final void ToCnlList()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "Every man is a human-being."));
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, false, false);
		ArrayList<String> cnlOut = feClient.ToCNLList(true, true, true);

		assert cnlSentences.size() == cnlOut.size();
		for (int i = 0; i < cnlOut.size(); i++)
		{
			assert cnlSentences.get(i) == cnlOut[i];
		}
	}

	private void CheckStatementCount(CogniPyStatement stmt, int Nconcept, int Ninstances, int NRoles, int NDataRole)
	{
		assert stmt.Concepts.size() == Nconcept;
		assert stmt.Instances.size() == Ninstances;
		assert stmt.Roles.size() == NRoles;
		assert stmt.DataRoles.size() == NRoles;
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void ToCnlStatementList()
	public final void ToCnlStatementList()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "Every man is a human-being."));
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, false, false);
		ArrayList<CogniPy.CogniPyStatement> cnlOut = feClient.ToCNLStatementList(true);

		assert cnlSentences.size() == cnlOut.size();
		for (int i = 0; i < cnlOut.size(); i++)
		{
			assert cnlOut[i].CnlStatement == cnlSentences.get(i);
			if (i == 0)
			{
				CheckStatementCount(cnlOut[i], 1, 1, 0, 0);
			}
			else if (i == 1)
			{
				CheckStatementCount(cnlOut[i], 2, 0, 0, 0);
			}
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void RuleDebuggerTest()
	public final void RuleDebuggerTest()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "Mary is a woman.", "John has-friend Mary.", "Mary has-name equal-to 'Mary'.", "If a man has-friend a woman and the woman has-name equal-to 'Mary' then the man is an happy-man."));
		CogniPySvr feClient = new CogniPySvr();
		String ruleId = feClient.GetStatementId(cnlSentences.get(4));
		feClient.SetDebugListener((statementId, elements) ->
		{
				assert ruleId == statementId;
				assert 2 == elements.Count();
				assert "John" == elements[0].Value.toString();
				assert "man" == elements[0].Name;
				assert "Mary" == elements[1].Value.toString();
				assert "woman" == elements[1].Name;
		}, (s, c) -> Tuple.Create(s, c));
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

	}
}