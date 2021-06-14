using CogniPy;
using NUnit.Framework;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;

namespace CogniPyUnitTests
{
    [TestFixture]
    public class InputOutputTests
    {
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
        public void LoadFileWithReference()
        {
            var feClient = new CogniPySvr();

            feClient.LoadCnl(Path.Combine(AssemblyDirectory, "TestFiles", "CSHC.encnl"), true, true);
            var instances = new string[] { "Dynamic[sfo]", "Answer-2-1-10-1" };
            var result = feClient.DescribeInstancesByName(instances);
            CollectionAssert.AreEquivalent(instances, result.Select(x => x.Instance).ToArray());
        }


        [Test]
        public void ToCnlList()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "Every man is a human-being."
            };
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, false);
            var cnlOut = feClient.ToCNLList(true, true, true);

            Assert.AreEqual(cnlSentences.Count(), cnlOut.Count());
            for (int i = 0; i < cnlOut.Count(); i++)
                Assert.AreEqual(cnlSentences[i], cnlOut[i]);
        }

        void CheckStatementCount(CogniPyStatement stmt, int Nconcept, int Ninstances, int NRoles, int NDataRole)
        {
            Assert.AreEqual(stmt.Concepts.Count(), Nconcept);
            Assert.AreEqual(stmt.Instances.Count(), Ninstances);
            Assert.AreEqual(stmt.Roles.Count(), NRoles);
            Assert.AreEqual(stmt.DataRoles.Count(), NRoles);
        }

        [Test]
        public void ToCnlStatementList()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "Every man is a human-being."
            };
            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, false);
            var cnlOut = feClient.ToCNLStatementList(true);

            Assert.AreEqual(cnlSentences.Count(), cnlOut.Count());
            for (int i = 0; i < cnlOut.Count(); i++)
            {
                Assert.AreEqual(cnlOut[i].CnlStatement, cnlSentences[i]);
                if (i == 0)
                    CheckStatementCount(cnlOut[i], 1, 1, 0, 0);
                else if (i == 1)
                    CheckStatementCount(cnlOut[i], 2, 0, 0, 0);
            }
        }

        [Test]
        public void RuleDebuggerTest()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "Mary is a woman.",
                    "John has-friend Mary.",
                    "Mary has-name equal-to 'Mary'.",
                    "If a man has-friend a woman and the woman has-name equal-to 'Mary' then the man is an happy-man."
            };
            var feClient = new CogniPySvr();
            var ruleId = feClient.GetStatementId(cnlSentences[4]);
            feClient.SetDebugListener((statementId, elements) =>
            {
                Assert.AreEqual(ruleId, statementId);
                Assert.AreEqual(2, elements.Count());
                Assert.AreEqual("John", elements[0].Value.ToString());
                Assert.AreEqual("man", elements[0].Name);
                Assert.AreEqual("Mary", elements[1].Value.ToString());
                Assert.AreEqual("woman", elements[1].Name);
            }, (s, c) => Tuple.Create(s, c));
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

        }
        
        [Test]
        public void LoadRdfOntologies()
        {
            var feClient = new CogniPySvr();

            Assert.DoesNotThrow(() => feClient.LoadRdf(Path.Combine(AssemblyDirectory, "TestFiles", "AEO.owl"), true, true,true));
            Assert.DoesNotThrow(() => feClient.LoadRdf(Path.Combine(AssemblyDirectory, "TestFiles", "BAMS.owl"), true, true,true));
        }
    }
}
