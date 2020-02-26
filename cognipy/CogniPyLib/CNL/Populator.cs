using System.Collections.Generic;

namespace CogniPy.CNL.DL
{
    public interface Populator
    {
        IEnumerable<KeyValuePair<string, string>> Populate(string sentenceBeginning, string str, List<string> forms, int max);
    }
}
