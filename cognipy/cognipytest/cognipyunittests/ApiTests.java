package cognipyunittests;

import CogniPy.*;
import NUnit.Framework.*;
import static CogniPy.CNL.DL.Statement.*;
import java.util.*;
import java.io.*;

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [TestFixture] public class ApiTests
public class ApiTests
{
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void DescribeInstanceTest()
	public final void DescribeInstanceTest()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "John has-nickname equal-to 'Jojo'.", "John has-friend Martha.", "Mary is a man.", "Mary has-nickname equal-to 'Mojo'.", "Mary has-friend Martha."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

		HashMap<String, CogniPy.models.InstanceDescription> result = feClient.DescribeInstances("John");
		assert 1 == result.size();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var instanceDescription = result.FirstOrDefault();
		assert instanceDescription != null;

		assert "John" == instanceDescription.Key;
		assert 1 == instanceDescription.Value.AttributeValues.Count;

		Assert.True(instanceDescription.Value.AttributeValues.ContainsKey("has-nickname"));
		assert "Jojo" == (String)instanceDescription.Value.AttributeValues["has-nickname"].First();

		assert 1 == instanceDescription.Value.RelatedInstances.Count;
		Assert.True(instanceDescription.Value.RelatedInstances.ContainsKey("has-friend"));
		assert "Martha" == instanceDescription.Value.RelatedInstances["has-friend"].First();
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void DescribeInstanceQueryWithPrefixTest()
	public final void DescribeInstanceQueryWithPrefixTest()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John[sfo] is a man.", "John[sfo] has-nickname equal-to 'Jojo'.", "John[sfo] has-friend Martha.", "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

		HashMap<String, CogniPy.models.InstanceDescription> result = feClient.DescribeInstances("John[sfo]");
		assert 1 == result.size();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var instanceDescription = result.FirstOrDefault();
		assert instanceDescription != null;

		assert "John[sfo]" == instanceDescription.Key;
		assert 1 == instanceDescription.Value.AttributeValues.Count;

		Assert.True(instanceDescription.Value.AttributeValues.ContainsKey("has-nickname"));
		assert "Jojo" == (String)instanceDescription.Value.AttributeValues["has-nickname"].First();

		assert 1 == instanceDescription.Value.RelatedInstances.Count;
		Assert.True(instanceDescription.Value.RelatedInstances.ContainsKey("has-friend"));
		assert "Martha" == instanceDescription.Value.RelatedInstances["has-friend"].First();
	}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void DescribeInstancesTest()
	public final void DescribeInstancesTest()
	{
		ArrayList<String> instances = new ArrayList<String>(Arrays.asList("John", "Mark"));
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "John has-nickname equal-to 'Jojo'.", "John has-friend Martha.", "Mark is a man.", "Mark has-nickname equal-to 'Maro'.", "Mark has-friend Mery."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

		HashMap<String, CogniPy.models.InstanceDescription> result = feClient.DescribeInstances("a man");
		ArrayList<Object> instancesFromResult = result.keySet().ToList();

		CollectionAssert.AreEquivalent(instances, instancesFromResult);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void DescribeInstancesNoRelationNoAttributeTest()
	public final void DescribeInstancesNoRelationNoAttributeTest()
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("John is a man.", "John has-friend Martha.", "Mark is a man.", "Mark has-nickname equal-to 'Maro'."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

		HashMap<String, CogniPy.models.InstanceDescription> result = feClient.DescribeInstances("a man");

		assert "Martha" == result["John"].RelatedInstances.get("has-friend").First();
		assert 0 == result["John"].AttributeValues.size();

		assert "Maro" == (String)result["Mark"].AttributeValues.get("has-nickname").First();
		assert 0 == result["Mark"].RelatedInstances.size();
	}


	public static String getAssemblyDirectory()
	{
		String codeBase = Assembly.GetExecutingAssembly().CodeBase;
		UriBuilder uri = new UriBuilder(codeBase);
		String path = Uri.UnescapeDataString(uri.Path);
		return (new File(path)).getParent();
	}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void GetConstraintsBig()
	public final void GetConstraintsBig()
	{
		CogniPySvr feClient = new CogniPySvr();

		feClient.LoadCnl(Path.Combine(getAssemblyDirectory(), "TestFiles", "CSHC.encnl"), true, true, false);

		Stopwatch sw = new Stopwatch();
		sw.Start();
		feClient.KnowledgeInsert("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>] is a form[sfo].", true, true);
		sw.Stop();
		long elapUpdate = sw.ElapsedMilliseconds;

		sw.Restart();
		HashMap<String, CogniPy.models.ConstraintResult> constraints = feClient.GetConstraints(new ArrayList<String>(Arrays.asList("form[sfo]", "element-1-form", "Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]")));
		sw.Stop();
		long elap = sw.ElapsedMilliseconds;
		Assert.Contains("form[sfo]", constraints.keySet());
		Assert.Contains("element-1-form", constraints.keySet());
		Assert.Contains("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]", constraints.keySet());
		assert elap < 1000;

		sw.Restart();
		ArrayList<String> res1 = feClient.GetSuperConceptsOf("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]", false);
		Assert.Contains("form[sfo]", res1);
		sw.Stop();
		long elapReasoner = sw.ElapsedMilliseconds;

		sw.Restart();
		System.Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> res2 = feClient.SparqlQueryInternal(feClient.SelectSuperconceptsSPARQL("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]", false), true, true, null);
		System.Tuple<ArrayList<String>, ArrayList<ArrayList<Object>>> cnlRes2 = feClient.TranslateQueryResultsIntoCnlInPlace(res2);
		long elapSparql = sw.ElapsedMilliseconds;

		Assert.Contains("form[sfo]", cnlRes2.Item2.SelectMany(x -> x).ToList());
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase("Bubu", Modality.MUST, new string[2] { "have-value", "have-this[sfo]" }, new string[2] { "smth[sfo]", "(some string value)" })][TestCase("Bubu[sfo]", Modality.MUST, new string[2] { "have-value", "have-this[sfo]" }, new string[2] { "smth[sfo]", "(some string value)" })][TestCase("a data-location-form", Modality.MUST, new string[1] { "concern" }, new string[1] { "data-location-section-2" })][TestCase("data-location-form", Modality.MUST, new string[1] { "concern" }, new string[1] { "data-location-section-2" })][TestCase("some-concept[sfo]", Modality.MUST, new string[1] { "concern" }, new string[1] { "other-concept" })][TestCase("Element-1-Form-D-14-08-2018-T-14-50-7[<http://www.sfo.cognitum.eu/Survey/a536f37b-00f5-492d-80ff-c84948d862ec#>]", Modality.MUST, new string[1] { "concern" }, new string[1] { "other-concept" })] public void GetConstraintsTest(string subjectToCheck, Modality constraint, IEnumerable<string> relationsToCheck, IEnumerable<string> objectsToCheck)
	public final void GetConstraintsTest(String subjectToCheck, CogniPy.CNL.DL.Statement.Modality constraint, java.lang.Iterable<String> relationsToCheck, java.lang.Iterable<String> objectsToCheck)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every data-location-form is a form.", "Every data-location-form can concern a data-location-section-1.", "Every data-location-form must concern a data-location-section-2.", "Data-Location-Form is a data-location-form.", "If a data-location-form is a bubu then the data-location-form is a bibi.", "Every bubu must have-value (some string value).", "Every bubu must have-value (some datetime value).", "Every bubu must have-value (some integer value).", "Every bubu must have-value (some real value).", "Every bubu must have-value (some boolean value).", "Every bubu must have-value (some duration value).", "Every bubu must have-this[sfo] a smth[sfo].", "Bubu is a bubu.", "Bubu[sfo] is a bubu.", "Every some-concept[sfo] must concern an other-concept.", "Element-1-Form-D-14-08-2018-T-14-50-7[<http://www.sfo.cognitum.eu/Survey/a536f37b-00f5-492d-80ff-c84948d862ec#>] is a some-concept[sfo].", "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

		Stopwatch sw = new Stopwatch();
		sw.Start();
		HashMap<String, CogniPy.models.ConstraintResult> constraints = feClient.GetConstraints(new ArrayList<String>(Arrays.asList(subjectToCheck))); //, "a data-location-form", "Baba[sfo]", "baba[sfo]"
		sw.Stop();
		long elap = sw.ElapsedMilliseconds;
		CollectionAssert.Contains(constraints.keySet(), subjectToCheck);
		CollectionAssert.IsSubsetOf(relationsToCheck, constraints[subjectToCheck].Relations.get(constraint));
		CollectionAssert.IsSubsetOf(objectsToCheck, constraints[subjectToCheck].ThirdElement.get(constraint));
	}

	private Regex rgxForCnlFormat = new Regex(""".+\"\\[.+\\]", RegexOptions.Compiled);
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase("biba", "en", new string[1] { "bela" })][TestCase("sdsgd", "ar", new string[1] { "Ala" })] public void GetAnnotationsTest(string ax, string lan, string[] signature)
	public final void GetAnnotationsTest(String ax, String lan, String[] signature)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every data-location-form is a form.", "Annotations: bela Concept: \"comment\":rdfs 'kaka maka' bela Concept: \"comment\":rdfs 'biba'@en _Ala Instance: \"backwardCompatibleWith\":owl 'sdsgd'@ar ."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);
		java.lang.Iterable<CogniPy.models.AnnotationResult> result = feClient.GetAnnotationsForSignature(signature);

		//TODO: add also test for adding annotation on a role.
		Assert.Contains(ax, result.Select(x -> x.Value.toString()).ToList());
		Assert.True(rgxForCnlFormat.IsMatch(result.Select(ann -> ann.Property).First()));
		Assert.Contains(lan, result.Select(x -> x.Language).ToList());
	}



//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase("a data-location-form", true, new string[1] { "location-form" })][TestCase("a data-location-form", false, new string[2] { "form", "location-form" })][TestCase("Data-Location-Form", true, new string[2] { "data-location-form", "data-location-form[sfo]" })][TestCase("Data-Location-Form[sfo]", false, new string[3] { "form", "location-form", "data-location-form" })][TestCase("Data-Location-Form", false, new string[4] { "form", "location-form", "data-location-form", "data-location-form[sfo]" })] public void CloneTest(string cnl, bool direct, string[] asserts)
	public final void CloneTest(String cnl, boolean direct, String[] asserts)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every location-form is a form.", "Every data-location-form is a location-form.", "Data-Location-Form is a data-location-form.", "Kiki must hava a miki.", "Data-Location-Form is a data-location-form[sfo].", "Data-Location-Form[sfo] is a data-location-form.", "References: [sfo] 'http://sfo.com' ('http://sfo.com') .", "Annotations: bela Concept: \"comment\":rdfs 'kaka maka' bela Concept: \"comment\":rdfs 'biba'@en _Ala Instance: \"backwardCompatibleWith\":owl 'sdsgd'@ar .", "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);

		ArrayList<String> superconcepts = feClient.GetSuperConceptsOf(cnl, direct);

		CogniPy.CogniPySvr clone1 = feClient.CloneForAboxChangesOnly();
		clone1.KnowledgeInsert("Clone-1 is a form.", true, true);
		ArrayList<String> superconcepts2 = clone1.GetSuperConceptsOf(cnl, direct);

		CogniPy.CogniPySvr clone2 = feClient.CloneForAboxChangesOnly();
		clone2.KnowledgeInsert("Clone-2 is a form.", true, true);

		ArrayList<String> resClone1 = clone1.GetInstancesOf("a form", false);
		ArrayList<String> resClone2 = clone2.GetInstancesOf("a form", false);

		CollectionAssert.DoesNotContain(resClone2, "Clone-1");
		CollectionAssert.DoesNotContain(resClone1, "Clone-2");

		CollectionAssert.IsNotEmpty(superconcepts2);
		CollectionAssert.AreEquivalent(asserts, superconcepts2);

		ArrayList<String> instances = clone2.GetInstancesOf("a form", direct);

		CollectionAssert.DoesNotContain(instances, "Clone-1");
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void CloneReasonerBigOntologyTest()
	public final void CloneReasonerBigOntologyTest()
	{
		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnl(Path.Combine(getAssemblyDirectory(), "TestFiles", "RODO.encnl"), true, true, false);

		CogniPy.CogniPySvr clone1 = feClient.CloneForAboxChangesOnly();
		ArrayList<String> toAdd = new ArrayList<String>(Arrays.asList("Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is a element-1-form.", "Survey[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is a subject[sfo].", "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] concern[sfo] Survey[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>].", "Survey[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is-concerned-by[sfo] Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>].", "Element-1-1-Section-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is a element-1-1-section.", "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] concern[sfo] Element-1-1-Section-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>]."));


		clone1.KnowledgeInsert(tangible.StringHelper.join("\r\n", toAdd), false, true);
		clone1.KnowledgeDelete(tangible.StringHelper.join("\r\n", toAdd), true);

		ArrayList<String> instances = clone1.GetInstancesOf("element-1-form", false);
		CollectionAssert.DoesNotContain(instances, "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>]");
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase("a data-location-form", true, new string[1] { "location-form" })][TestCase("a data-location-form", false, new string[2] { "form", "location-form" })][TestCase("Data-Location-Form", true, new string[2] { "data-location-form", "data-location-form[sfo]" })][TestCase("Data-Location-Form[sfo]", false, new string[3] { "form", "location-form", "data-location-form" })][TestCase("Data-Location-Form", false, new string[4] { "form", "location-form", "data-location-form", "data-location-form[sfo]" })] public void GetSuperConceptsTest(string cnl, bool direct, string[] asserts)
	public final void GetSuperConceptsTest(String cnl, boolean direct, String[] asserts)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every location-form is a form.", "Every data-location-form is a location-form.", "Data-Location-Form is a data-location-form.", "Data-Location-Form is a data-location-form[sfo].", "Data-Location-Form[sfo] is a data-location-form.", "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);
		ArrayList<String> superconcepts = feClient.GetSuperConceptsOf(cnl, direct);

		CollectionAssert.IsNotEmpty(superconcepts);
		CollectionAssert.AreEquivalent(asserts, superconcepts);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase("a form", true, new string[] { "location-form" })][TestCase("a form", false, new string[] { "data-location-form", "data-location-form[sfo]", "location-form" })][TestCase("a location-form", true, new string[] { "data-location-form", "data-location-form[sfo]" })][TestCase("a location-form", false, new string[] { "data-location-form", "data-location-form[sfo]" })][TestCase("a data-location-form", true, new string[] { })][TestCase("a data-location-form", false, new string[] { })] public void GetSubConceptsTest(string cnl, bool direct, string[] asserts)
	public final void GetSubConceptsTest(String cnl, boolean direct, String[] asserts)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every location-form is a form.", "Every data-location-form is a location-form.", "Every data-location-form[sfo] is a location-form.", "Data-Location-Form is a data-location-form.", "Data-Location-Form is a data-location-form[sfo].", "Data-Location-Form[sfo] is a data-location-form.", "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);
		ArrayList<String> subconcepts = feClient.GetSubConceptsOf(cnl, direct);
		if (asserts.Count() == 0)
		{
			CollectionAssert.IsEmpty(subconcepts);
		}
		else
		{
			CollectionAssert.IsNotEmpty(subconcepts);
			CollectionAssert.AreEquivalent(asserts, subconcepts);
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase(false)][TestCase(true)][TestCase(true, false)] public void ToCNLListTest(bool includeImplicit, bool removeTrivials = true)

	public final void ToCNLListTest(boolean includeImplicit)
	{
		ToCNLListTest(includeImplicit, true);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: [Test][TestCase(false)][TestCase(true)][TestCase(true, false)] public void ToCNLListTest(bool includeImplicit, bool removeTrivials = true)
	public final void ToCNLListTest(boolean includeImplicit, boolean removeTrivials)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every location-form is a form.", "Every data-location-form is a location-form.", "Data-Location-Form is a data-location-form.", "If a form is a data-location-form then the form is-a-form-of-type Special-Form.", "Annotations:\r\n_Operational-Risk Instance: network-description 'Network of operational risk.'@en\r\n."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences.<String>Union(new ArrayList<String>(Arrays.asList("Comment: This comment should not be returned.")))), true, true, false);
		ArrayList<String> sentencesReturned = feClient.ToCNLList(includeImplicit, removeTrivials, true);

		Assert.AreEqual(sentencesReturned.size(), sentencesReturned.Distinct().ToList().Count, "There are duplicate senetences!");

		if (includeImplicit)
		{
			if (removeTrivials)
			{
				assert 9 == sentencesReturned.size();
			}
			else
			{
				assert 12 == sentencesReturned.size();
			}
		}
		else
		{
			CollectionAssert.AreEquivalent(cnlSentences, sentencesReturned);
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase(true)][TestCase(false)] public void ToCNLStatementListTest(bool includeImplicit)
	public final void ToCNLStatementListTest(boolean includeImplicit)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Every location-form is a form.", "Every data-location-form is a location-form.", "Data-Location-Form is a data-location-form.", "If a form is a data-location-form then the form is-a-form-of-type Special-Form."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences.<String>Union(new ArrayList<String>(Arrays.asList("Comment: This comment should not be returned.")))), true, true, false);



		ArrayList<CogniPy.CogniPyStatement> sentencesReturned = feClient.ToCNLStatementList(includeImplicit);

		Assert.AreEqual(sentencesReturned.size(), sentencesReturned.Distinct().ToList().Count, "There are duplicate senetences!");

		if (includeImplicit)
		{
			assert 11 == sentencesReturned.size();
		}
		else
		{
			CollectionAssert.AreEquivalent(cnlSentences, sentencesReturned.Select(s -> s.CnlStatement));
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void GetInstancesOfTest()
	public final void GetInstancesOfTest()
	{
		ArrayList<String> cnlSentencesToInsert = new ArrayList<String>(Arrays.asList("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>] is a subject[sfo]."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnl(Path.Combine(getAssemblyDirectory(), "TestFiles", "CSHC.encnl"), true, true, false);
		feClient.KnowledgeInsert(cnlSentencesToInsert.get(0), true, true);
		feClient.KnowledgeInsert(cnlSentencesToInsert.get(cnlSentencesToInsert.size() - 1), true, true);

		ArrayList<String> instances = feClient.GetInstancesOf("a subject[sfo]", false);

		Assert.Contains("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>]", instances);

		ArrayList<String> instances2 = feClient.GetInstancesOf("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>]", false);
		Assert.Contains("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>]", instances2);
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test] public void GetInstancesOfWhenInsertWithAnnotationsTest()
	public final void GetInstancesOfWhenInsertWithAnnotationsTest()
	{ //BUG: SR-10
		ArrayList<String> initialOntology = new ArrayList<String>(Arrays.asList("Every-single-thing has-label nothing-but (some string value).", "Every deal-criteria is a network-component.", "Every reputational-risk-component is a network-component.", "Every reputational-risk-network-deal-criteria is a deal-criteria.", "Every reputational-risk-network-deal-criteria is a reputational-risk-network-component.", "Every reputational-risk-top-outcome is a reputational-risk-network-component.", "Reputational-Risk is a reputational-risk-top-outcome.", "Reputational-Risk has-label equal-to 'Reputational Risk'.", "Reputational-Risk is a positive-outcome.", "Reputational-Risk has-network-id equal-to 'Net-re'.", "Reputational-Risk is a network.", "Deal-Criteria-2 influences-with-weight-of-1 Reputational-Risk."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", initialOntology), true, true, false);

		ArrayList<String> toInsert = new ArrayList<String>(Arrays.asList("Deal-Criteria-2 is a reputational-risk-network-deal-criteria.", "Deal-Criteria-2 is a reputational-risk-network-component.", "Deal-Criteria-2 has-sql-name equal-to ''.", "Deal-Criteria-2 has-availability equal-to 'Data-Available'.", "Deal-Criteria-2 has-unit equal-to ''.", "Deal-Criteria-2 has-aggregation equal-to 'None'.", "Annotations:\r\n_Deal-Criteria-2 Instance: node-label 'Importance'@en\r\n_Deal-Criteria-2 Instance: node-description 'Importance'@en\r\n."));

		feClient.KnowledgeInsert(tangible.StringHelper.join("\r\n", toInsert), true, true);

		ArrayList<String> instances = feClient.GetInstancesOf("a reputational-risk-network-component", false);
		assert 2 == instances.size();
	}


//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [Test][TestCase(true, true)][TestCase(true, false)][TestCase(false, true)][TestCase(false, false)] public void KnowledgeInsertAnnotationsTest(bool includeImplicit, bool removeTrivials)
	public final void KnowledgeInsertAnnotationsTest(boolean includeImplicit, boolean removeTrivials)
	{
		ArrayList<String> cnlSentences = new ArrayList<String>(Arrays.asList("Data-Location-Form is a data-location-form.", "Annotations: _Data-Location-Form Instance: description 'A data location form.'@en.", "Comment: This comment should not be returned as CNL statement."));

		CogniPySvr feClient = new CogniPySvr();
		feClient.LoadCnlFromString(tangible.StringHelper.join("\r\n", cnlSentences), true, true, false);


		ArrayList<String> toInsert = new ArrayList<String>(Arrays.asList("Operational-Risk is a thing.", "Annotations: _Operational-Risk Instance: network-description 'Network of operational risk.'@en."));
		feClient.KnowledgeInsert(tangible.StringHelper.join("\r\n", toInsert), true, true);
		java.lang.Iterable<CogniPy.models.AnnotationResult> annots = feClient.GetAnnotationsForSignature(new ArrayList<String>(Arrays.asList("Operational-Risk")));
		assert annots.stream().filter(a -> a.Property.equals("network-description")).count() > 0;

		//Check for number of Annotations: blocks, should be exactly 1 
		String toCnl = feClient.ToCNL(includeImplicit, true);
		assert 1 == toCnl.split("[ ]", -1).stream().filter(tok -> tok.Contains("Annotations:")).count();

		ArrayList<String> toCnlList = feClient.ToCNLList(includeImplicit, removeTrivials, true);
		assert 1 == toCnlList.size()(s -> s.Contains("Annotations:"));
	}
}