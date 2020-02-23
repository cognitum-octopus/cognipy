using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace Ontorion.CNL.DL
{
    public interface Populator
    {
        IEnumerable<KeyValuePair<string, string>> Populate(string sentenceBeginning, string str, List<string> forms, int max);
    }
}
