using CogniPy;
using NUnit.Framework;
using Ontorion.FluentEditorClient;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CogniPyUnitTests
{
    [TestFixture]
    public class CnlManipulationTests
    {
        [Test]
        public void AddCnl()
        {
            var feClient = new CogniPySvr();
            // reason the new context
            var CnlContent = "Every man is a human-being.\r\n Every human-being has-name equal-to 'aaa'.";
            var CnlToAdd = new List<string>() {
                    "John is a man.",
                    "Every man is a human-being."
                };

            feClient.LoadCnlFromString(CnlContent, true, true);
            feClient.KnowledgeInsert(string.Join("\r\n", CnlToAdd), true, true);
            var mergedCnl = feClient.ToCNLList(false);

            foreach (var cnl in CnlToAdd)
            {
                Assert.IsTrue(mergedCnl.Any(x => x == cnl));
            }
            Assert.AreEqual(3,mergedCnl.Count());
        }

        [Test]
        public void AddSubsumptionTop()
        {
            var feClient = new CogniPySvr();

            try
            {
                feClient.LoadCnlFromString("My-Instance-1 is a thing.", true, true);
                feClient.KnowledgeInsert("My-Instance-2 is a thing.", true, true);
                feClient.KnowledgeInsert("Every my-concept is a thing.",true,true);
            }
            catch(Exception ex)
            {
                Assert.Fail(ex.Message);
            }
        }

        [Test]
        public void AddNumberCnl()
        {
            var feClient = new CogniPySvr();
            // reason the new context
            var CnlContent = "Vendor-0 is a vendor.";
            var CnlToAdd = new List<string>() {
                    "Vendor-0 has-latitude equal-to 43.737345.",
                    "Vendor-0 has-longitude equal-to -79.442286."
                };

            feClient.LoadCnlFromString(CnlContent, true, false);
            feClient.KnowledgeInsert(string.Join("\r\n", CnlToAdd), true, false);

            var mergedCnl = feClient.ToCNLList(false);

            foreach (var cnl in CnlToAdd)
            {
                Assert.IsTrue(mergedCnl.Any(x => x == cnl));
            }
            Assert.AreEqual(3, mergedCnl.Count());
        }
        [Test]
        public void RemoveCnl()
        {
            var feClient = new CogniPySvr();
            // reason the new context
            var CnlContent = "John is a man.\r\nEvery man is a human-being.\r\n Every human-being has-name equal-to 'aaa'.\r\n";
            var CnlToRemove = new List<string>() {
                    "John is a man."
                    //,                    "Every man is a human-being."
                };

            feClient.LoadCnlFromString(CnlContent, true, true);
            feClient.KnowledgeDelete(string.Join("\r\n", CnlToRemove), false);
            var mergedCnl = feClient.ToCNLList(true,true,true);

            foreach (var cnl in CnlToRemove)
            {
                Assert.IsTrue(!mergedCnl.Any(x => x == cnl));
            }
            Assert.AreEqual(2, mergedCnl.Count());
        }

        [Test]
        public void RulesKeptAfterMergeTest()
        {
            var feClient = new CogniPySvr();
            // reason the new context
            var CnlContent = new List<string>() {
                "If a man is a human-being then the man is a cat."
            };
            var CnlToAdd = new List<string>() {
                    "John is a man."
                };

            feClient.LoadCnlFromString(string.Join("\r\n",CnlContent), true, true);
            feClient.KnowledgeInsert(string.Join("\r\n", CnlToAdd), true, true);
            var mergedCnl = feClient.ToCNLList(true, true, true);

            Assert.AreEqual(2, mergedCnl.Count());
            foreach(var cnl in CnlContent)
            {
                Assert.IsTrue(mergedCnl.Any(x => x == cnl));
            }

            foreach (var cnl in CnlToAdd)
            {
                Assert.IsTrue(mergedCnl.Any(x => x == cnl));
            }

        }

        [Test]
        [TestCase("John", "instance")]
        [TestCase("man", "concept")]
        [TestCase("has-name", "role")]
        [TestCase("John[TIS]", "instance")]
        public void FromToUriTest(string cnlName,string type)
        {
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString("John is a man.\r\nReferences: [TIS] (\"http://www.somenamespace.com/\").",true,false);
            
            var uri = feClient.UriFromCnl(cnlName, type);
            var transformedCnl = feClient.CnlFromUri(uri, type);

            Assert.AreEqual(cnlName, transformedCnl);
        }
    }
}
