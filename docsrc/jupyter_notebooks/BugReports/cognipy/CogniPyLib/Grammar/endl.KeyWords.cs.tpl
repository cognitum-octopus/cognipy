using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Diagnostics;
using System.Text.RegularExpressions;
using System.Reflection;

//todo - check roles in ENDLPL - seems that it is a dead code
//todo - MARK - should be realy unique

namespace CogniPy.CNL.EN
{
    public class KeyWords
    {
        Dictionary<string, HashSet<string>> kw = new Dictionary<string, HashSet<string>>();
        Dictionary<string, string> ikw = new Dictionary<string, string>();

        public KeyWords()
        {
##########

            ikw["<? ?>"] = "CODE";
            ikw["("] = "OPEN";
            ikw[")"] = "CLOSE";
            ikw["."]="END";
            ikw[","]="COMMA";

            ikw["<="]="CMP";
            ikw[">="]="CMP";
            ikw["="]="CMP";
            ikw["<>"]="CMP";
            ikw["<"]="CMP";
            ikw[">"]="CMP";

            foreach (var kv in ikw)
			{
                var kww = kv.Value;
                var wrd = kv.Key;

                if (wrd == "x" || wrd == "y")
                    wrd = wrd.ToUpper();

                if (!kw.ContainsKey(kww))
                    kw.Add(kww, new HashSet<string>());
                kw[kww].Add(wrd);
			}
		}

        public static KeyWords Me = new KeyWords();

        public string[] GetAllKeywords()
        {
            return ikw.Keys.ToArray();
        }

        public bool keywordExist(string wrd)
        {
            return kw.ContainsKey(wrd);
        }

        public bool isKeyword(string wrd)
        {
            return ikw.ContainsKey(wrd.Trim().ToLower());
        }

        public string Get(string wrd)
        {
	        return kw[wrd].First();
        }

        public HashSet<string> GetAll(string wrd)
        {
            return kw[wrd];
        }
		
		public string GetTag(string wrd)
        {
            return ikw[wrd.Trim().ToLower()];
        }
		
		public bool IsProducer(string wrd)
        {
            foreach (var k in kw[wrd])
                if (k.First() == '(' || k.First() == '{' || k.First() == '[' || k.First() == ',')
                    return true;
            return false;
        }
    }

}
