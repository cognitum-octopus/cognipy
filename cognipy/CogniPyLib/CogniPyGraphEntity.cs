using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CogniPy
{
    public struct CogniPyGraphEntity
    {
        public string Name { get; set; }
        public override string ToString()
        {
            return Name;
        }
    }
}
