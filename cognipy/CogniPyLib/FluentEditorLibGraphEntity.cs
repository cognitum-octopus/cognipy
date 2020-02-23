using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ontorion.FluentEditorClient
{
    public struct FluentEditorLibGraphEntity
    {
        public string Name { get; set; }
        public override string ToString()
        {
            return Name;
        }
    }
}
