using CogniPy;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text.RegularExpressions;
using static CogniPy.CNL.DL.Statement;

namespace CogniPyUnitTests
{
    [TestFixture]
    public class ApiTests
    {
        [Test]
        public void DescribeInstanceTest()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "John has-nickname equal-to 'Jojo'.",
                    "John has-friend Martha.",
                    "Mary is a man.",
                    "Mary has-nickname equal-to 'Mojo'.",
                    "Mary has-friend Martha."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

            var result = feClient.DescribeInstances("John");
            Assert.AreEqual(1, result.Count);

            var instanceDescription = result.FirstOrDefault();
            Assert.IsNotNull(instanceDescription);

            Assert.AreEqual("John", instanceDescription.Key);
            Assert.AreEqual(1, instanceDescription.Value.AttributeValues.Count);

            Assert.True(instanceDescription.Value.AttributeValues.ContainsKey("has-nickname"));
            Assert.AreEqual("Jojo", (string)instanceDescription.Value.AttributeValues["has-nickname"].First());

            Assert.AreEqual(1, instanceDescription.Value.RelatedInstances.Count);
            Assert.True(instanceDescription.Value.RelatedInstances.ContainsKey("has-friend"));
            Assert.AreEqual("Martha", instanceDescription.Value.RelatedInstances["has-friend"].First());
        }

        [Test]
        public void DescribeInstanceQueryWithPrefixTest()
        {
            var cnlSentences = new List<string>() {
                    "John[sfo] is a man.",
                    "John[sfo] has-nickname equal-to 'Jojo'.",
                    "John[sfo] has-friend Martha.",
                    "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

            var result = feClient.DescribeInstances("John[sfo]");
            Assert.AreEqual(1, result.Count);

            var instanceDescription = result.FirstOrDefault();
            Assert.IsNotNull(instanceDescription);

            Assert.AreEqual("John[sfo]", instanceDescription.Key);
            Assert.AreEqual(1, instanceDescription.Value.AttributeValues.Count);

            Assert.True(instanceDescription.Value.AttributeValues.ContainsKey("has-nickname"));
            Assert.AreEqual("Jojo", (string)instanceDescription.Value.AttributeValues["has-nickname"].First());

            Assert.AreEqual(1, instanceDescription.Value.RelatedInstances.Count);
            Assert.True(instanceDescription.Value.RelatedInstances.ContainsKey("has-friend"));
            Assert.AreEqual("Martha", instanceDescription.Value.RelatedInstances["has-friend"].First());
        }


        [Test]
        public void DescribeInstancesTest()
        {
            List<string> instances = new List<string>() { "John", "Mark" };
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "John has-nickname equal-to 'Jojo'.",
                    "John has-friend Martha.",

                    "Mark is a man.",
                    "Mark has-nickname equal-to 'Maro'.",
                    "Mark has-friend Mery."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

            var result = feClient.DescribeInstances("a man");
            var instancesFromResult = result.Keys.ToList();

            CollectionAssert.AreEquivalent(instances, instancesFromResult);
        }

        [Test]
        public void DescribeInstancesNoRelationNoAttributeTest()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "John has-friend Martha.",

                    "Mark is a man.",
                    "Mark has-nickname equal-to 'Maro'."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

            var result = feClient.DescribeInstances("a man");

            Assert.AreEqual("Martha", result["John"].RelatedInstances["has-friend"].First());
            Assert.AreEqual(0, result["John"].AttributeValues.Count);

            Assert.AreEqual("Maro", (string)result["Mark"].AttributeValues["has-nickname"].First());
            Assert.AreEqual(0, result["Mark"].RelatedInstances.Count);
        }


        public static string AssemblyDirectory
        {
            get
            {
                string codeBase = Assembly.GetExecutingAssembly().CodeBase;
                UriBuilder uri = new UriBuilder(codeBase);
                string path = Uri.UnescapeDataString(uri.Path);
                return Path.GetDirectoryName(path);
            }
        }


        [Test]
        public void GetConstraintsBig()
        {
            var feClient = new CogniPySvr();

            feClient.LoadCnl(Path.Combine(AssemblyDirectory, "TestFiles", "CSHC.encnl"), true, true);

            Stopwatch sw = new Stopwatch();
            sw.Start();
            feClient.KnowledgeInsert("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>] is a form[sfo].", true, true);
            sw.Stop();
            var elapUpdate = sw.ElapsedMilliseconds;

            sw.Restart();
            var constraints = feClient.GetConstraints(new List<string>() { "form[sfo]", "element-1-form", "Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]" });
            sw.Stop();
            var elap = sw.ElapsedMilliseconds;
            Assert.Contains("form[sfo]", constraints.Keys);
            Assert.Contains("element-1-form", constraints.Keys);
            Assert.Contains("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]", constraints.Keys);
            Assert.IsTrue(elap < 1000);

            sw.Restart();
            var res1 = feClient.GetSuperConceptsOf("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]", false);
            Assert.Contains("form[sfo]", res1);
            sw.Stop();
            var elapReasoner = sw.ElapsedMilliseconds;

            sw.Restart();
            var res2 = feClient.SparqlQueryInternal(feClient.SelectSuperconceptsSPARQL("Element-1-Form-D-14-08-2018-T-14-55-26[<http://www.sfo.cognitum.eu/Survey/5d09ffea-d461-4335-a3c9-03e74aec92eb#>]", false));
            var cnlRes2 = feClient.TranslateQueryResultsIntoCnlInPlace(res2);
            var elapSparql = sw.ElapsedMilliseconds;

            Assert.Contains("form[sfo]", cnlRes2.Item2.SelectMany(x => x).ToList());
        }

        [Test]
        [TestCase("Bubu", Modality.MUST, new string[2] { "have-value", "have-this[sfo]" }, new string[2] { "smth[sfo]", "(some string value)" })]
        [TestCase("Bubu[sfo]", Modality.MUST, new string[2] { "have-value", "have-this[sfo]" }, new string[2] { "smth[sfo]", "(some string value)" })]
        [TestCase("a data-location-form", Modality.MUST, new string[1] { "concern" }, new string[1] { "data-location-section-2" })]
        [TestCase("data-location-form", Modality.MUST, new string[1] { "concern" }, new string[1] { "data-location-section-2" })]
        [TestCase("some-concept[sfo]", Modality.MUST, new string[1] { "concern" }, new string[1] { "other-concept" })]
        [TestCase("Element-1-Form-D-14-08-2018-T-14-50-7[<http://www.sfo.cognitum.eu/Survey/a536f37b-00f5-492d-80ff-c84948d862ec#>]", Modality.MUST, new string[1] { "concern" }, new string[1] { "other-concept" })]
        public void GetConstraintsTest(string subjectToCheck, Modality constraint, IEnumerable<string> relationsToCheck, IEnumerable<string> objectsToCheck)
        {
            var cnlSentences = new List<string>() {
                "Every data-location-form is a form.",
                "Every data-location-form can concern a data-location-section-1.",
                "Every data-location-form must concern a data-location-section-2.",
                "Data-Location-Form is a data-location-form.",

                "If a data-location-form is a bubu then the data-location-form is a bibi.",
                "Every bubu must have-value (some string value).",
                "Every bubu must have-value (some datetime value).",
                "Every bubu must have-value (some integer value).",
                "Every bubu must have-value (some real value).",
                "Every bubu must have-value (some boolean value).",
                "Every bubu must have-value (some duration value).",
                "Every bubu must have-this[sfo] a smth[sfo].",
                "Bubu is a bubu.",
                "Bubu[sfo] is a bubu.",

                "Every some-concept[sfo] must concern an other-concept.",

                "Element-1-Form-D-14-08-2018-T-14-50-7[<http://www.sfo.cognitum.eu/Survey/a536f37b-00f5-492d-80ff-c84948d862ec#>] is a some-concept[sfo].",

                "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

            var sw = new Stopwatch();
            sw.Start();
            var constraints = feClient.GetConstraints(new List<string>() { subjectToCheck }); //, "a data-location-form", "Baba[sfo]", "baba[sfo]"
            sw.Stop();
            var elap = sw.ElapsedMilliseconds;
            CollectionAssert.Contains(constraints.Keys, subjectToCheck);
            CollectionAssert.IsSubsetOf(relationsToCheck, constraints[subjectToCheck].Relations[constraint]);
            CollectionAssert.IsSubsetOf(objectsToCheck, constraints[subjectToCheck].ThirdElement[constraint]);
        }

        Regex rgxForCnlFormat = new Regex(@""".+""\[.+\]", RegexOptions.Compiled);
        [Test]
        [TestCase("biba", "en", new string[1] { "bela" })]
        [TestCase("sdsgd", "ar", new string[1] { "Ala" })]
        public void GetAnnotationsTest(string ax, string lan, string[] signature)
        {
            var cnlSentences = new List<string>() {
                "Every data-location-form is a form.",
                @"Annotations:
bela Concept: ""comment"":rdfs 'kaka maka'
bela Concept: ""comment"":rdfs 'biba'@en
_Ala Instance: ""backwardCompatibleWith"":owl 'sdsgd'@ar
."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);
            var result = feClient.GetAnnotationsForSignature(signature);

            //TODO: add also test for adding annotation on a role.
            Assert.Contains(ax, result.Select(x => x.Value.ToString()).ToList());
            Assert.True(rgxForCnlFormat.IsMatch(result.Select(ann => ann.Property).First()));
            Assert.Contains(lan, result.Select(x => x.Language).ToList());
        }



        [Test]
        [TestCase("a data-location-form", true, new string[1] { "location-form" })]
        [TestCase("a data-location-form", false, new string[2] { "form", "location-form" })]
        [TestCase("Data-Location-Form", true, new string[2] { "data-location-form", "data-location-form[sfo]" })]
        [TestCase("Data-Location-Form[sfo]", false, new string[3] { "form", "location-form", "data-location-form" })]
        [TestCase("Data-Location-Form", false, new string[4] { "form", "location-form", "data-location-form", "data-location-form[sfo]" })]
        public void CloneTest(string cnl, bool direct, string[] asserts)
        {
            var cnlSentences = new List<string>() {
                "Every location-form is a form.",
                "Every data-location-form is a location-form.",
                "Data-Location-Form is a data-location-form.",
                "Kiki must hava a miki.",

                "Data-Location-Form is a data-location-form[sfo].",
                "Data-Location-Form[sfo] is a data-location-form.",
                "References: [sfo] 'http://sfo.com' ('http://sfo.com') .",
                @"Annotations:
bela Concept: ""comment"":rdfs 'kaka maka'
bela Concept: ""comment"":rdfs 'biba'@en
_Ala Instance: ""backwardCompatibleWith"":owl 'sdsgd'@ar
.",
                "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

            var superconcepts = feClient.GetSuperConceptsOf(cnl, direct);

            var clone1 = feClient.CloneForAboxChangesOnly();
            clone1.KnowledgeInsert("Clone-1 is a form.", true, true);
            var superconcepts2 = clone1.GetSuperConceptsOf(cnl, direct);

            var clone2 = feClient.CloneForAboxChangesOnly();
            clone2.KnowledgeInsert("Clone-2 is a form.", true, true);

            var resClone1 = clone1.GetInstancesOf("a form", false);
            var resClone2 = clone2.GetInstancesOf("a form", false);

            CollectionAssert.DoesNotContain(resClone2, "Clone-1");
            CollectionAssert.DoesNotContain(resClone1, "Clone-2");

            CollectionAssert.IsNotEmpty(superconcepts2);
            CollectionAssert.AreEquivalent(asserts, superconcepts2);

            var instances = clone2.GetInstancesOf("a form", direct);

            CollectionAssert.DoesNotContain(instances, "Clone-1");
        }

        [Test]
        public void CloneReasonerBigOntologyTest()
        {
            var feClient = new CogniPySvr();
            feClient.LoadCnl(Path.Combine(AssemblyDirectory, "TestFiles", "RODO.encnl"), true, true);

            var clone1 = feClient.CloneForAboxChangesOnly();
            var toAdd = new List<string>() {
            "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is a element-1-form.",
            "Survey[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is a subject[sfo].",
            "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] concern[sfo] Survey[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>].",
            "Survey[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is-concerned-by[sfo] Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>].",
            "Element-1-1-Section-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] is a element-1-1-section.",
            "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>] concern[sfo] Element-1-1-Section-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>]."
            };


            clone1.KnowledgeInsert(string.Join("\r\n", toAdd), false, true);
            clone1.KnowledgeDelete(string.Join("\r\n", toAdd), true);

            var instances = clone1.GetInstancesOf("element-1-form", false);
            CollectionAssert.DoesNotContain(instances, "Element-1-Form-D-24-01-2019-T-15-42-57[<http://www.sfo.cognitum.eu/Survey/2d3ce1df-a509-4c00-9a25-ac20e54efdf8#>]");
        }

        [Test]
        [TestCase("a data-location-form", true, new string[1] { "location-form" })]
        [TestCase("a data-location-form", false, new string[2] { "form", "location-form" })]
        [TestCase("Data-Location-Form", true, new string[2] { "data-location-form", "data-location-form[sfo]" })]
        [TestCase("Data-Location-Form[sfo]", false, new string[3] { "form", "location-form", "data-location-form" })]
        [TestCase("Data-Location-Form", false, new string[4] { "form", "location-form", "data-location-form", "data-location-form[sfo]" })]
        public void GetSuperConceptsTest(string cnl, bool direct, string[] asserts)
        {
            var cnlSentences = new List<string>() {
                "Every location-form is a form.",
                "Every data-location-form is a location-form.",
                "Data-Location-Form is a data-location-form.",

                "Data-Location-Form is a data-location-form[sfo].",
                "Data-Location-Form[sfo] is a data-location-form.",

                "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);
            var superconcepts = feClient.GetSuperConceptsOf(cnl, direct);

            CollectionAssert.IsNotEmpty(superconcepts);
            CollectionAssert.AreEquivalent(asserts, superconcepts);
        }

        [Test]
        [TestCase("a form", true, new string[] { "location-form" })]
        [TestCase("a form", false, new string[] { "data-location-form", "data-location-form[sfo]", "location-form" })]
        [TestCase("a location-form", true, new string[] { "data-location-form", "data-location-form[sfo]" })]
        [TestCase("a location-form", false, new string[] { "data-location-form", "data-location-form[sfo]" })]
        [TestCase("a data-location-form", true, new string[] { })]
        [TestCase("a data-location-form", false, new string[] { })]
        public void GetSubConceptsTest(string cnl, bool direct, string[] asserts)
        {
            var cnlSentences = new List<string>() {
                "Every location-form is a form.",
                "Every data-location-form is a location-form.",
                "Every data-location-form[sfo] is a location-form.",
                "Data-Location-Form is a data-location-form.",

                "Data-Location-Form is a data-location-form[sfo].",
                "Data-Location-Form[sfo] is a data-location-form.",

                "References: [sfo] 'http://sfo.com' ('http://sfo.com') ."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);
            var subconcepts = feClient.GetSubConceptsOf(cnl, direct);
            if (asserts.Count() == 0)
                CollectionAssert.IsEmpty(subconcepts);
            else
            {
                CollectionAssert.IsNotEmpty(subconcepts);
                CollectionAssert.AreEquivalent(asserts, subconcepts);
            }
        }

        [Test]
        [TestCase(false)]
        [TestCase(true)]
        [TestCase(true, false)]
        public void ToCNLListTest(bool includeImplicit, bool removeTrivials = true)
        {
            var cnlSentences = new List<string>() {
                "Every location-form is a form.",
                "Every data-location-form is a location-form.",
                "Data-Location-Form is a data-location-form.",
                "If a form is a data-location-form then the form is-a-form-of-type Special-Form.",
                "Annotations:\r\n_Operational-Risk Instance: network-description 'Network of operational risk.'@en\r\n.",
            };

            var feClient = new CogniPySvr();
            feClient.SetDebugListener((statementId, elements) =>
            {
                int x = 10;
            }, (s, c) => Tuple.Create(s, c));
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences.Union<string>(new List<string>() { "Comment: This comment should not be returned." })), true, true);
            var sentencesReturned = feClient.ToCNLList(includeImplicit, removeTrivials, true);

            Assert.AreEqual(sentencesReturned.Count, sentencesReturned.Distinct().ToList().Count, "There are duplicate senetences!");

            if (includeImplicit)
            {
                if (removeTrivials)
                    Assert.AreEqual(9, sentencesReturned.Count);
                else
                    Assert.AreEqual(14, sentencesReturned.Count);
            }
            else
                CollectionAssert.AreEquivalent(cnlSentences, sentencesReturned);
        }

        [Test]
        [TestCase(true)]
        [TestCase(false)]
        public void ToCNLStatementListTest(bool includeImplicit)
        {
            var cnlSentences = new List<string>() {
                "Every location-form is a form.",
                "Every data-location-form is a location-form.",
                "Data-Location-Form is a data-location-form.",
                "If a form is a data-location-form then the form is-a-form-of-type Special-Form."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences.Union<string>(new List<string>() { "Comment: This comment should not be returned." })), true, true);



            var sentencesReturned = feClient.ToCNLStatementList(includeImplicit);

            Assert.AreEqual(sentencesReturned.Count, sentencesReturned.Distinct().ToList().Count, "There are duplicate senetences!");

            if (includeImplicit)
                Assert.AreEqual(13, sentencesReturned.Count);
            else
                CollectionAssert.AreEquivalent(cnlSentences, sentencesReturned.Select(s => s.CnlStatement));
        }

        [Test]
        public void GetInstancesOfTest()
        {
            var cnlSentencesToInsert = new List<string>() {
                "Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>] is a subject[sfo].",
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnl(Path.Combine(AssemblyDirectory, "TestFiles", "CSHC.encnl"), true, true);
            feClient.KnowledgeInsert(cnlSentencesToInsert.First(), true, true);
            feClient.KnowledgeInsert(cnlSentencesToInsert.Last(), true, true);

            var instances = feClient.GetInstancesOf("a subject[sfo]", false);

            Assert.Contains("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>]", instances);

            var instances2 = feClient.GetInstancesOf("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>]", false);
            Assert.Contains("Survey[<http://www.sfo.cognitum.eu/Survey/29ed4b1d-fa55-479b-93aa-7773b30bda6b#>]", instances2);
        }

        [Test]
        public void GetInstancesOfWhenInsertWithAnnotationsTest()
        {//BUG: SR-10
            var initialOntology = new List<string>(){
                "Every-single-thing has-label nothing-but (some string value).",
                "Every deal-criteria is a network-component.",
                "Every reputational-risk-component is a network-component.",
                "Every reputational-risk-network-deal-criteria is a deal-criteria.",
                "Every reputational-risk-network-deal-criteria is a reputational-risk-network-component.",
                "Every reputational-risk-top-outcome is a reputational-risk-network-component.",
                "Reputational-Risk is a reputational-risk-top-outcome.",
                "Reputational-Risk has-label equal-to 'Reputational Risk'.",
                "Reputational-Risk is a positive-outcome.",
                "Reputational-Risk has-network-id equal-to 'Net-re'.",
                "Reputational-Risk is a network.",
                "Deal-Criteria-2 influences-with-weight-of-1 Reputational-Risk."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", initialOntology), true, true);

            var toInsert = new List<string>()
            {
                "Deal-Criteria-2 is a reputational-risk-network-deal-criteria.",
                "Deal-Criteria-2 is a reputational-risk-network-component.",
                "Deal-Criteria-2 has-sql-name equal-to ''.",
                "Deal-Criteria-2 has-availability equal-to 'Data-Available'.",
                "Deal-Criteria-2 has-unit equal-to ''.",
                "Deal-Criteria-2 has-aggregation equal-to 'None'.",
                "Annotations:\r\n_Deal-Criteria-2 Instance: node-label 'Importance'@en\r\n_Deal-Criteria-2 Instance: node-description 'Importance'@en\r\n."
            };

            feClient.KnowledgeInsert(string.Join("\r\n", toInsert), true, true);

            var instances = feClient.GetInstancesOf("a reputational-risk-network-component", false);
            Assert.AreEqual(2, instances.Count);
        }


        [Test]
        [TestCase(true, true)]
        [TestCase(true, false)]
        [TestCase(false, true)]
        [TestCase(false, false)]
        public void KnowledgeInsertAnnotationsTest(bool includeImplicit, bool removeTrivials)
        {
            var cnlSentences = new List<string>() {
                "Data-Location-Form is a data-location-form.",
                "Annotations: _Data-Location-Form Instance: description 'A data location form.'@en.",
                "Comment: This comment should not be returned as CNL statement."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);


            var toInsert = new List<string>()
            {
                "Operational-Risk is a thing.",
                "Annotations: _Operational-Risk Instance: network-description 'Network of operational risk.'@en."
            };
            feClient.KnowledgeInsert(string.Join("\r\n", toInsert), true, true);
            var annots = feClient.GetAnnotationsForSignature(new List<string>() { "Operational-Risk" });
            Assert.IsTrue(annots.Where(a => a.Property == "network-description").Count() > 0);

            //Check for number of Annotations: blocks, should be exactly 1 
            var toCnl = feClient.ToCNL(includeImplicit, true);
            Assert.AreEqual(1, toCnl.Split(' ').Where(tok => tok.Contains("Annotations:")).Count());

            var toCnlList = feClient.ToCNLList(includeImplicit, removeTrivials, true);
            Assert.AreEqual(1, toCnlList.Count(s => s.Contains("Annotations:")));
        }
        
        [Test]
        public void AnnotationsTest()
        {
            var feClient = new CogniPySvr();
            feClient.LoadRdf(Path.Combine(AssemblyDirectory, "TestFiles", "TestAnnotations.owl"), true, true, true);

            var toRdf = feClient.ToRDF(true);

            var annotations = new List<string>()
            {
                "<rdf:Description rdf:about=\"http://www.cognitum.eu/onto#bela\">",
                "<rdfs:comment rdf:datatype=\"http://www.w3.org/2001/XMLSchema#string\">kaka maka</rdfs:comment>",
                "<rdfs:comment xml:lang=\"en\">biba</rdfs:comment>",
                "<rdf:Description rdf:about=\"http://www.cognitum.eu/onto#Ala\">",
                "<owl:backwardCompatibleWith xml:lang=\"ar\">sdsgd</owl:backwardCompatibleWith>"
            };
            
            Assert.IsTrue(annotations.All(s=> toRdf.Contains(s)));
        }
    }
}
