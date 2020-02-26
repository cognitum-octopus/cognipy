using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CogniPy.models
{
    public class InstanceDescription
    {
        //
        // Summary:
        //     Instance for which the description was asked
        public string Instance { get; set; }
        //
        // Summary:
        //     ObjectProperties of the instance (e.g. John has-friend Marta., RelatedInstances
        //     will contain {has-friend,Marta})
        public Dictionary<string, IEnumerable<string>> RelatedInstances { get; set; }
        //
        // Summary:
        //     DataProperties of the instance (e.g. Dog has-legs equal-to 4., AttributeValues
        //     will contain {has-legs,4})
        public Dictionary<string, IEnumerable<object>> AttributeValues { get; set; }
    }
}
