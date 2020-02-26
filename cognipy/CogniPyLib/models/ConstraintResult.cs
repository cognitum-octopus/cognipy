using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CogniPy.models
{
    public class ConstraintResult
    {
        /// <summary>
        /// Concept for which the constraint was asked
        /// </summary>
        public string Concept { get; set; }

        public Dictionary<CogniPy.CNL.DL.Statement.Modality, List<string>> Relations { get; set; }

        public Dictionary<CogniPy.CNL.DL.Statement.Modality, List<string>> ThirdElement { get; set; }
    }
}
