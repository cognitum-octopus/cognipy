using NUnit.Framework;
using FluentEditorClientLib;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Ontorion.FluentEditorClient;
using Ontorion;
using CogniPy;

namespace CogniPyUnitTests
{
    [TestFixture]
    public class BasicReasoningTests
    {
        [Test]
        [TestCase(true)]
        [TestCase(false)]
        public void MaterializationTest(bool materialize)
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "Every man is a human-being."
            };
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n",cnlSentences),true, materialize: materialize);
            //var res = feClient.SparqlQuery("SELECT * WHERE {?x ?y ?z}");
            var result = feClient.ToCNL(true,true);
            Assert.IsNotNull(result);
            if(materialize)
                Assert.IsTrue(result.Contains("John is a human-being."));
            else
                Assert.IsFalse(result.Contains("John is a human-being."));    
        }

        [Test]
        public void IncorrectCnlTest()
        {
            var feClient = new CogniPySvr();
            try
            {
                feClient.LoadCnlFromString(string.Join("\r\n", "Every man is a human-being"), true, true);
            }
            catch(Exception ex)
            {
                return;
            }
            Assert.Fail("I was expecting the client to throw a parse exception.");
        }


        [Test]
        public void OneSentenceIncorrectCnlTest()
        {
            var feClient = new CogniPySvr();
            var knowledge = "Vendor-0 has-value equal-to 3.4880694143167\r\nVendor-0 is a vendor.\r\nVendor-0 has-message-type equal-to 'Receipt'.\r\n\r\n";
            try
            {
                feClient.LoadCnlFromString(knowledge, false, true);
            }
            catch (Exception ex)
            {
                return;
            }
            Assert.Fail("I was expecting the client to throw a parse exception.");
        }

        [Test]
        public void OneSentenceIncorrectCnlTestII()
        {
            var feClient = new CogniPySvr();
            var knowledge = "Vendor-0 has-value equal-to 3.4880694143167.\r\nVendor-0 is a vendor.\r\nVendor-0 has-message-type equal-to Receipt'.\r\n\r\n";
            try
            {
                feClient.LoadCnlFromString(knowledge, true, true);
            }
            catch (Exception ex)
            {
                return;
            }
            Assert.Fail("I was expecting the client to throw a parse exception.");
        }

        [Test]
        public void NonInitializedTest()
        {
            var feClient = new CogniPySvr();
            try
            {
                feClient.KnowledgeInsert("Every man is a human-being.", true, true);
            }
            catch (InvalidOperationException ex)
            {
                return;
            }
            Assert.Fail("Expecting an InvalidOperationException.");
        }

        [Test]
        public void NoCnlTest()
        {
            var feClient = new CogniPySvr();
            var cnl = feClient.ToCNL(true);
            Assert.AreEqual("",cnl);
        }

        [Test]
        public void SparqlQueryToMaterializedGraphTest()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "Every man is a human-being."
            };
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);
            var res = feClient.SparqlQuery("SELECT * WHERE {?x rdf:type :man}");
            Assert.IsNotNull(res);
            Assert.AreEqual(1, res.Item2.Count());
        }

        [Test]
        public void AddKnowledgeGetSuperConcept()
        {
            var cnlSentences = new List<string>() {
                    "Every man is a human-being."
            };
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);
            feClient.KnowledgeInsert("John is a man.", true, true);

            var res1 = feClient.GetSuperConceptsOf("John", false);
            Assert.Contains("human-being", res1);

            var res2 = feClient.SparqlQuery(feClient.SelectSuperconceptsSPARQL("John", false));
            var cnlRes2 = feClient.TranslateQueryResultsIntoCnlInPlace(res2);
            
            Assert.Contains("human-being", cnlRes2.Item2.SelectMany(x => x).ToList());
        }


        [Test]
        public void BasciSWRLRuleReasoning()
        {
            var cnlSentences = new List<string>() {

                "If an element-1-form concern[sfo] a subject[sfo] and an element-2-holder-form concern[sfo] a subject[sfo] then the element-2-holder-form is an element-2-form.",

                "Element-1-Form is an element-1-form.",
                "Doupa is a subject[sfo].",
                "Element-2-Holder-Form is an element-2-holder-form.",

                "Element-1-Form concerns[sfo] Doupa .",
                "Element-2-Holder-Form concerns[sfo] Doupa .",
            };
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);


            var result = feClient.GetSuperConceptsOf("Element-2-Holder-Form", false);
            Assert.Contains("element-2-form", result);
        }
    }
}
