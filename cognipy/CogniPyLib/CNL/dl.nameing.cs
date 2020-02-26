using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace CogniPy.ARS
{
    public enum EntityKind { Concept, Role, DataRole, DataType, Instance, SWRLVariable, Annotation,Statement }
}

namespace CogniPy.CNL.DL
{

    public class DlName
    {

        public string id;
        public class Parts
        {
            public bool quoted;
            public bool local;
            public string name;
            public string term = null;

            public DlName Combine()
            {
                return new DlName() { id = (local ? "_" : "") + (quoted ? "\"" + name.Replace("\"", "\"\"") + "\"" : name) + (term != null ? (":" + term) : "") };
            }

            public Parts Clone()
            {
                return (new Parts() { local=this.local,name=this.name,quoted=this.quoted,term=this.term});
            }
        }

        [ThreadStatic]
        static Dictionary<string, Parts> cache = null;

        public Parts Split()
        {
            if (cache == null)
                cache = new Dictionary<string, Parts>();

            if (!cache.ContainsKey(id))
            {
                Parts ret = new Parts();
                ret.local = id.StartsWith("_");
                var name = id.Substring(ret.local ? 1 : 0);
                var ddpos = name.IndexOf(':');
                if (ddpos >= 0)
                {
                    ret.term = name.Substring(ddpos + 1);
                    name = name.Substring(0, ddpos);
                }
                ret.quoted = name.StartsWith("\"");
                ret.name = ret.quoted ? name.Substring(1, name.Length - 2).Replace("\"\"", "\"") : name;
                cache[id] = ret;
                return ret.Clone();
            }
            return cache[id].Clone();
        }
    }
}
