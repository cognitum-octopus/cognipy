using FluentEditorClientLib;
using NUnit.Framework;
using Ontorion.FluentEditorClient;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace FluentEditorClientUnitTests
{
    [TestFixture]
    public class GraphQueryTests
    {
        [Test]
        public void DescribeInstance()
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "John has-surname equal-to 'Doe'.",
                    "John has-surname equal-to 'Boe'.",
                    "John is-living-in Europe."
            };
            var feClient = new FluentEditorClient();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), materialize: false);

            //var instance = feClient.GetInstancesOf("John", false);
            var result = feClient.GetInstanceDescription("John");
        }
    }
}
