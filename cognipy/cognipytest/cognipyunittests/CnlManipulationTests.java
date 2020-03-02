package cognipyunittests;

import CogniPy.*;
import NUnit.Framework.*;
import java.util.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestFixture] public class CnlManipulationTests
public class CnlManipulationTests
{
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void AddCnl()
	public final void AddCnl()
	{
		CogniPySvr feClient = new CogniPySvr();
		// reason the new context
		String CnlContent = "Every man is a human-being.\r\n Every human-being has-name equal-to 'aaa'.";
		ArrayList<String> CnlToAdd = new ArrayList<String>(Arrays.asList("John is a man."));

		feClient.LoadCnlFromString(CnlContent, true, true, false);
		feClient.KnowledgeInsert(tangible.StringHelper.join("\r\n", CnlToAdd), true, true);
		ArrayList<String> mergedCnl = feClient.ToCNLList(false);

		for (String cnl : CnlToAdd)
		{
			assert mergedCnl.Any(x = cnl.equals(> x));
		}
		assert 3 == mergedCnl.size();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void AddNumberCnl()
	public final void AddNumberCnl()
	{
		CogniPySvr feClient = new CogniPySvr();
		// reason the new context
		String CnlContent = "Vendor-0 is a vendor.";
		ArrayList<String> CnlToAdd = new ArrayList<String>(Arrays.asList("Vendor-0 has-latitude equal-to 43.737345.", "Vendor-0 has-longitude equal-to -79.442286."));

		feClient.LoadCnlFromString(CnlContent, true, false, false);
		feClient.KnowledgeInsert(tangible.StringHelper.join("\r\n", CnlToAdd), true, false);

		ArrayList<String> mergedCnl = feClient.ToCNLList(false);

		for (String cnl : CnlToAdd)
		{
			assert mergedCnl.Any(x = cnl.equals(> x));
		}
		assert 3 == mergedCnl.size();
	}
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void RemoveCnl()
	public final void RemoveCnl()
	{
		CogniPySvr feClient = new CogniPySvr();
		// reason the new context
		String CnlContent = "John is a man.\r\nEvery man is a human-being.\r\n Every human-being has-name equal-to 'aaa'.\r\n";
		ArrayList<String> CnlToRemove = new ArrayList<String>(Arrays.asList("John is a man."));

		feClient.LoadCnlFromString(CnlContent, true, true, false);
		feClient.KnowledgeDelete(tangible.StringHelper.join("\r\n", CnlToRemove), false);
		ArrayList<String> mergedCnl = feClient.ToCNLList(true, true, true);

		for (String cnl : CnlToRemove)
		{
			assert!mergedCnl.Any(x = cnl.equals(> x));
		}
		assert 2 == mergedCnl.size();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void RulesKeptAfterMergeTest()
	public final void RulesKeptAfterMergeTest()
	{
		CogniPySvr feClient = new CogniPySvr();
		// reason the new context
		ArrayList<String> CnlContent = new ArrayList<String>(Arrays.asList("If a man is a human-being then the man is a cat."));
		ArrayList<String> CnlToAdd = new ArrayList<String>(Arrays.asList("John is a man."));

		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", CnlContent), true, true, false);
		feClient.KnowledgeInsert(tangible.StringHelper.join("\r\n", CnlToAdd), true, true);
		ArrayList<String> mergedCnl = feClient.ToCNLList(true, true, true);

		assert 2 == mergedCnl.size();
		for (String cnl : CnlContent)
		{
			assert mergedCnl.Any(x = cnl.equals(> x));
		}

		for (String cnl : CnlToAdd)
		{
			assert mergedCnl.Any(x = cnl.equals(> x));
		}

	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase("John", "instance")][TestCase("man", "concept")][TestCase("has-name", "role")][TestCase("John[TIS]", "instance")] public void FromToUriTest(string cnlName, string type)
	public final void FromToUriTest(String cnlName, String type)
	{
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString("John is a man.\r\nReferences: [TIS] (\"http://www.somenamespace.com/\").", true, false, false);

		String uri = feClient.UriFromCnl(cnlName, type);
		String transformedCnl = feClient.CnlFromUri(uri, type);

		assert cnlName == transformedCnl;
	}
}