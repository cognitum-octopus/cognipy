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
    public class ModularisationTests
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
        public void SR14BUG()
        {
            var reasoner = new CogniPySvr();
            reasoner.LoadCnl(Path.Combine(AssemblyDirectory, "TestFiles", "TestTBox.encnl"), true, false);
            var desc = reasoner.DescribeInstances("Nikolai");
            Assert.IsEmpty(desc);
        }

        [Test]
        public void SR11BUG()
        {
            var toinstert = new string[]{
"Deal-Criteria-2 is a reputational-risk-network-deal-criteria.",
"Deal-Criteria-2 is a reputational-risk-network-component.",
"Deal-Criteria-2 has-sql-name equal-to ''.",
"Deal-Criteria-2 has-availability equal-to 'Data-Available'.",
"Deal-Criteria-2 has-unit equal-to ''.",
"Deal-Criteria-2 has-aggregation equal-to 'None'.",
"Annotations:\r\n_Deal-Criteria-2 Instance: node-label 'Importance'@en\r\n_Deal-Criteria-2 Instance: node-description 'Importance'@en\r\n."
};
            var reasoner = new CogniPySvr();
            reasoner.LoadCnl(Path.Combine(AssemblyDirectory, "TestFiles", "ont2.encnl"), true, true);
            foreach (var l in toinstert)
                reasoner.KnowledgeInsert(l, true, true);
            var sups = reasoner.GetSuperConceptsOf("Deal-Criteria-2", false);
            Assert.AreEqual(sups.Count(), 4);

            var descr = reasoner.DescribeInstances("Deal-Criteria-2");
            Assert.AreEqual(descr.Count(), 1);
        }

        [Test]
        public void StrageBug2a2BUG()
        {
            var reasoner = new CogniPySvr();
            reasoner.LoadCnlFromString("Bubu influences-with-weight-of-1 Dudu.", true, true);
            var toDel = "Bubu influence-with-weight-of-1 Dudu.";
            reasoner.KnowledgeDelete(toDel, true);
            var cnl = reasoner.ToCNL(false);
        }

        [Test]
        public void SR15BUG()
        {
            var reasoner = new CogniPySvr();
            var ontologyPath = Path.Combine(AssemblyDirectory, "TestFiles", "TestOntology.encnl");
            reasoner.LoadCnl(ontologyPath, true, true);

            const string concept = "reputational-risk-network-component";

            var instances = reasoner.GetInstancesOf(concept, false);

            var descriptionsBeforeDeletion = reasoner.DescribeInstancesByName(instances);

            var toDel = new List<string>
{
    "Deal-Criteria-14 is a deal-criteria.",
    "Deal-Criteria-11 is a deal-criteria.",
    "Deal-Criteria-26 is a deal-criteria.",
    "Deal-Criteria-8 is a deal-criteria.",
    "Deal-Criteria-2 is a deal-criteria.",
    "Deal-Criteria-20 is a deal-criteria.",
    "Deal-Criteria-23 is a deal-criteria.",
    "Deal-Criteria-5 is a deal-criteria.",
    "Deal-Criteria-17 is a deal-criteria."
};
            reasoner.KnowledgeDelete(string.Join("\r\n", toDel), true);

            var descriptionsAfterDeletion = reasoner.DescribeInstancesByName(instances);
        }
    }
}