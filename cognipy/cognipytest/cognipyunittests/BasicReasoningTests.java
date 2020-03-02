package cognipyunittests;

import CogniPy.*;
import NUnit.Framework.*;
import java.util.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestFixture] public class BasicReasoningTests
public class BasicReasoningTests
{
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase(true)][TestCase(false)] public void MaterializationTest(bool materialize)
	public final void MaterializationTest(boolean materialize)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "Every man is a human-being."));
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, materialize: materialize, false);
		//var res = feClient.SparqlQuery("SELECT * WHERE {?x ?y ?z}");
		String result = feClient.ToCNL(true, true);
		assert result != null;
		if (materialize)
		{
			assert result.contains("John is a human-being.");
		}
		else
		{
			assert!result.contains("John is a human-being.");
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void IncorrectCnlTest()
	public final void IncorrectCnlTest()
	{
		CogniPySvr feClient = new CogniPySvr();
		try
		{
			feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", "Every man is a human-being"), true, true, false);
		}
		catch (RuntimeException ex)
		{
			return;
		}
		Assert.Fail("I was expecting the client to throw a parse exception.");
	}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void OneSentenceIncorrectCnlTest()
	public final void OneSentenceIncorrectCnlTest()
	{
		CogniPySvr feClient = new CogniPySvr();
		String knowledge = "Vendor-0 has-value equal-to 3.4880694143167\r\nVendor-0 is a vendor.\r\nVendor-0 has-message-type equal-to 'Receipt'.\r\n\r\n";
		try
		{
			feClient.LoadCnlFromString(knowledge, false, true, false);
		}
		catch (RuntimeException ex)
		{
			return;
		}
		Assert.Fail("I was expecting the client to throw a parse exception.");
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void OneSentenceIncorrectCnlTestII()
	public final void OneSentenceIncorrectCnlTestII()
	{
		CogniPySvr feClient = new CogniPySvr();
		String knowledge = "Vendor-0 has-value equal-to 3.4880694143167.\r\nVendor-0 is a vendor.\r\nVendor-0 has-message-type equal-to Receipt'.\r\n\r\n";
		try
		{
			feClient.LoadCnlFromString(knowledge, true, true, false);
		}
		catch (RuntimeException ex)
		{
			return;
		}
		Assert.Fail("I was expecting the client to throw a parse exception.");
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void NonInitializedTest()
	public final void NonInitializedTest()
	{
		CogniPySvr feClient = new CogniPySvr();
		try
		{
			feClient.KnowledgeInsert("Every man is a human-being.", true, true);
		}
		catch (IllegalStateException ex)
		{
			return;
		}
		Assert.Fail("Expecting an InvalidOperationException.");
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void NoCnlTest()
	public final void NoCnlTest()
	{
		CogniPySvr feClient = new CogniPySvr();
		String cnl = feClient.ToCNL(true);
		assert "" == cnl;
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void SparqlQueryToMaterializedGraphTest()
	public final void SparqlQueryToMaterializedGraphTest()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "Every man is a human-being."));
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);
		System.Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res = feClient.SparqlQueryInternal("SELECT * WHERE {?x rdf:type :man}", true, true, null);
		assert res != null;
		assert 1 == res.Item2.size();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void AddKnowledgeGetSuperConcept()
	public final void AddKnowledgeGetSuperConcept()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every man is a human-being."));
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);
		feClient.KnowledgeInsert("John is a man.", true, true);

		ArrayList<String> res1 = feClient.GetSuperConceptsOf("John", false);
		Assert.Contains("human-being", res1);

		System.Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res2 = feClient.SparqlQueryInternal(feClient.SelectSuperconceptsSPARQL("John", false), true, true, null);
		System.Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> cnlRes2 = feClient.TranslateQueryResultsIntoCnlInPlace(res2);

		Assert.Contains("human-being", cnlRes2.Item2.SelectMany(x -> x).ToList());
	}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void BasciSWRLRuleReasoning()
	public final void BasciSWRLRuleReasoning()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("If an element-1-form concern[sfo] a subject[sfo] and an element-2-holder-form concern[sfo] a subject[sfo] then the element-2-holder-form is an element-2-form.", "Element-1-Form is an element-1-form.", "Doupa is a subject[sfo].", "Element-2-Holder-Form is an element-2-holder-form.", "Element-1-Form concerns[sfo] Doupa .", "Element-2-Holder-Form concerns[sfo] Doupa ."));
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);


		ArrayList<String> result = feClient.GetSuperConceptsOf("Element-2-Holder-Form", false);
		Assert.Contains("element-2-form", result);
	}
}