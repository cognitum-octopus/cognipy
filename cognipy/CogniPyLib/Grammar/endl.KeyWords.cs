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
ikw["at-most"] = "COMPARER";
ikw["at-least"] = "COMPARER";
ikw["less-than"] = "COMPARER";
ikw["more-than"] = "COMPARER";
ikw["different-than"] = "COMPARER";
ikw["either"] = "EITHER";
ikw["lower-than"] = "COMPARER2";
ikw["greater-than"] = "COMPARER2";
ikw["equal-to"] = "EQUALTO";
ikw["different-from"] = "DIFFERENTFROM";
ikw["lower-or-equal-to"] = "COMPARER2";
ikw["greater-or-equal-to"] = "COMPARER2";
ikw["that-matches-pattern"] = "MATCHES";
ikw["that-has-length"] = "THATHASLENGTH";
ikw["the-one-and-only"] = "THEONEANDONLY";
ikw["the-one-and-only"] = "THEONEANDONLY";
ikw["the"] = "THE";
ikw["the"] = "THE";
ikw["nothing-but"] = "NOTHINGBUT";
ikw["is-unique-if"] = "ISUNIQUEIF";
ikw["if-and-only-if"] = "IFANDONLYIF";
ikw["if-and-only-if-it"] = "IFANDONLYIFIT";
ikw["if-and-only-if-it-either"] = "IFANDONLYIFITEITHER";
ikw["anything"] = "ANYTHING";
ikw["anything"] = "ANYTHING";
ikw["or-something-else"] = "ORSOMETHINGELSE";
ikw["does-not"] = "DOESNOT";
ikw["do-not"] = "DONOT";
ikw["is-not"] = "ISNOT";
ikw["be-not"] = "BENOT";
ikw["are-not"] = "ARENOT";
ikw["is-the-same-as"] = "ISTHESAMEAS";
ikw["is-not-the-same-as"] = "ISNOTTHESAMEAS";
ikw["if"] = "IF";
ikw["if"] = "IF";
ikw["then"] = "THEN";
ikw["it"] = "IT";
ikw["every"] = "EVERY";
ikw["every"] = "EVERY";
ikw["no"] = "NO";
ikw["no"] = "NO";
ikw["every-single-thing"] = "EVERYTHING";
ikw["every-single-thing"] = "EVERYTHING";
ikw["nothing"] = "NOTHING";
ikw["nothing"] = "NOTHING";
ikw["something"] = "SOMETHING";
ikw["something"] = "SOMETHING";
ikw["none"] = "NONE";
ikw["thing"] = "THING";
ikw["things"] = "THINGS";
ikw["that"] = "THAT";
ikw["is"] = "IS";
ikw["a"] = "AN";
ikw["a"] = "AN";
ikw["an"] = "AN";
ikw["an"] = "AN";
ikw["not"] = "NOT";
ikw["and"] = "AND";
ikw["or"] = "OR";
ikw["and-or"] = "ANDOR";
ikw["as-well-as"] = "ASWELLAS";
ikw["by"] = "BY";
ikw["itself"] = "ITSELF";
ikw["must"] = "MODAL";
ikw["should"] = "MODAL";
ikw["can"] = "MODAL";
ikw["must-not"] = "MODAL";
ikw["should-not"] = "MODAL";
ikw["can-not"] = "MODAL";
ikw["be"] = "BE";
ikw["are"] = "BE";
ikw["x"] = "X";
ikw["x"] = "X";
ikw["y"] = "Y";
ikw["y"] = "Y";
ikw["zero"] = "WORDNUM";
ikw["one"] = "WORDNUM";
ikw["two"] = "WORDNUM";
ikw["three"] = "WORDNUM";
ikw["four"] = "WORDNUM";
ikw["five"] = "WORDNUM";
ikw["six"] = "WORDNUM";
ikw["seven"] = "WORDNUM";
ikw["eight"] = "WORDNUM";
ikw["nine"] = "WORDNUM";
ikw["some"] = "SOME";
ikw["value"] = "VALUE";
ikw["value-of"] = "VALUEOF";
ikw["integer"] = "DATATYPENAME";
ikw["string"] = "DATATYPENAME";
ikw["real"] = "DATATYPENAME";
ikw["decimal"] = "DATATYPENAME";
ikw["boolean"] = "DATATYPENAME";
ikw["duration"] = "DATATYPENAME";
ikw["datetime"] = "DATATYPENAME";
ikw["date"] = "DATE";
ikw["time"] = "TIME";
ikw["true"] = "BOL";
ikw["false"] = "BOL";
ikw["execute"] = "EXECUTE";
ikw["for"] = "FOR";
ikw["exists"] = "EXISTS";
ikw["divided-by"] = "BINOP";
ikw["integer-divided-by"] = "BINOP";
ikw["modulo"] = "BINOP";
ikw["rounded-with-the-precision-of"] = "BINOP";
ikw["raised-to-the-power-of"] = "BINOP";
ikw["substring"] = "SUBSTRING";
ikw["from"] = "FROM";
ikw["with"] = "WITH";
ikw["before"] = "SUBSTRINGFIX";
ikw["after"] = "SUBSTRINGFIX";
ikw["absolute-value-of"] = "UNOP";
ikw["ceiling-of"] = "UNOP";
ikw["floor-of"] = "UNOP";
ikw["round-of"] = "UNOP";
ikw["sine-of"] = "UNOP";
ikw["cosine-of"] = "UNOP";
ikw["tangent-of"] = "UNOP";
ikw["case-ignored"] = "UNOP";
ikw["length-of"] = "UNOP";
ikw["space-normalized"] = "UNOP";
ikw["upper-cased"] = "UNOP";
ikw["lower-cased"] = "UNOP";
ikw["contains-string"] = "UNOP2";
ikw["starts-with-string"] = "UNOP2";
ikw["ends-with-string"] = "UNOP2";
ikw["matches-string"] = "UNOP2";
ikw["contains-case-ignored-string"] = "UNOP2";
ikw["sounds-like-string"] = "UNOP2";
ikw["translated"] = "TRANSLATEDREPLACED";
ikw["replaced"] = "TRANSLATEDREPLACED";
ikw["="] = "EQ";
ikw["<>"] = "CMP";
ikw["<="] = "CMP";
ikw[">="] = "CMP";
ikw["<"] = "CMP";
ikw[">"] = "CMP";
ikw["plus"] = "PLUS";
ikw["+"] = "PLUS";
ikw["minus"] = "MINUS";
ikw["-"] = "DASH";
ikw[":"] = "COLON";
ikw["times"] = "TIMES";
ikw["*"] = "TIMES";
ikw["followed-by"] = "FOLLOWEDBY";
ikw["++"] = "FOLLOWEDBY";
ikw["days"] = "DAYS";
ikw["hours"] = "HOURS";
ikw["minutes"] = "MINUTES";
ikw["seconds"] = "SECONDS";
ikw["alpha-representation-of"] = "ALPHA";
ikw["annotation"] = "ANNOTATION";
ikw["result-of"] = "RESULTOF";
ikw["be-true-that"] = "BETRUETHAT";


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
