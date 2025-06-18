using NMemory;
using NMemory.Tables;
using CogniPy.CNL.DL;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace CogniPy.Splitting.Memory
{
    public class knowledge
    {
        public string symbol { get; set; }

        public string kind { get; set; }

        public string expression { get; set; }
    }

    public class modularizer_bot
    {
        public string symbol { get; set; }

        public string ns { get; set; }

        public string expression { get; set; }

        public string loci { get; set; }

        public int num_loci { get; set; }
    }

    public class modularizer_top
    {
        public string symbol { get; set; }

        public string ns { get; set; }

        public string expression { get; set; }

        public string loci { get; set; }

        public int num_loci { get; set; }
    }

    public class MemoryModularizer : Database, Modularizer
    {
        ITable<knowledge> knowledge;
        ITable<modularizer_bot> mod_bot;
        ITable<modularizer_top> mod_top;
        HashSet<string> signature = new HashSet<string>();

        public MemoryModularizer()
        {
            knowledge = Tables.Create<knowledge, Tuple<string, string, string>>(p => Tuple.Create(p.symbol, p.kind, p.expression), null);
            mod_bot = Tables.Create<modularizer_bot, Tuple<string, string,string,  string>>(p => Tuple.Create(p.symbol, p.ns, p.expression, p.loci), null);
            mod_top = Tables.Create<modularizer_top, Tuple<string, string, string, string>>(p => Tuple.Create(p.symbol, p.ns, p.expression, p.loci), null);
        }

        public void Begin()
        {
        }

        static readonly HashSet<string> HashSetWithSingleEmptyString = new HashSet<string>() { "" };
        public HashSet<string> GetNssesFromLoci(List<ScriptLine.LociNode> mod, bool modularizeByNamespaces)
        {
            if (modularizeByNamespaces)
            {
                var locInsts = from loci in mod where IsLocalInstance(loci.symbol) select loci.symbol;
                var Ns = new HashSet<string>(from inst in locInsts select GetNs(inst));
                Ns.Remove("");
                if (Ns.Count == 0)
                    Ns.Add("");
                return Ns;
            }
            else
                return HashSetWithSingleEmptyString;
        }

        public void Apply()
        {
        }

        public void Insert(IEnumerable<ScriptLine> script, bool modularizeByNamespaces)
        {
            foreach (var line in script)
            {
                {
                    var s = line.GetSignature();
                    signature.UnionWith(s);
                    var kn = line.GetKind();
                    var l = line.Logic();
                    foreach (var ss in s)
                        knowledge.Insert(new knowledge() { symbol = ss, kind = kn, expression = l });
                }
                foreach (var kind in DLToys.LocalityKinds)
                {
                    var mod = line.GetLoci(kind);
                    foreach (var ns in GetNssesFromLoci(mod, modularizeByNamespaces))
                    {
                        foreach (var loci in mod)
                        {
                            if (kind == LocalityKind.Bottom)
                                mod_bot.Insert(new modularizer_bot() { symbol = loci.symbol, ns = ns, loci = loci.loci, expression = loci.expression, num_loci = loci.num_loci });
                            else
                                mod_top.Insert(new modularizer_top() { symbol = loci.symbol, ns = ns, loci = loci.loci, expression = loci.expression, num_loci = loci.num_loci });
                        }
                    }
                }
            }
        }

        public void Delete(IEnumerable<ScriptLine> script, bool modularizeByNamespaces)
        {
            foreach (var line in script)
            {
                {
                    var s = line.GetSignature();
                    signature.UnionWith(s);
                    var kn = line.GetKind();
                    if (kn != null)
                    {
                        var l = line.Logic();
                        var doDel = (from k in knowledge where s.Contains(k.symbol) && k.kind == kn && k.expression == l select k);
                        foreach (var d in doDel)
                            knowledge.Delete(d);
                    }
                }
                foreach (var kind in DLToys.LocalityKinds)
                {
                    var mod = line.GetLoci(kind);
                    foreach (var ns in GetNssesFromLoci(mod, modularizeByNamespaces))
                    {
                        foreach (var loci in mod)
                        {
                            if (kind == LocalityKind.Bottom)
                            {
                                var tb = mod_bot;
                                var doDel = (from m in tb where (m.symbol == loci.symbol) && (m.ns == ns) && (m.loci == loci.loci) && (m.expression == loci.expression) select m);
                                foreach (var d in doDel)
                                    tb.Delete(d);
                            }
                            else
                            {
                                var tb = mod_top;
                                var doDel = (from m in tb where (m.symbol == loci.symbol) && (m.ns == ns) && (m.loci == loci.loci) && (m.expression == loci.expression) select m);
                                foreach (var d in doDel)
                                    tb.Delete(d);
                            }
                        }
                    }
                }
            }
        }


        IEnumerable<dynamic> GetMod(LocalityKind kind, HashSet<string> curSignature, HashSet<string> namespaces)
        {
            if (kind == LocalityKind.Bottom)
            {
                var tb = mod_bot;
                foreach (var ns in namespaces)
                {
                    var query = (from m in tb where m.ns == ns && curSignature.Contains(m.symbol) select m);
                    foreach (var m in query)
                        yield return m;
                }
            }
            else
            {
                var tb = mod_top;
                foreach (var ns in namespaces)
                {
                    var query = (from m in tb where m.ns == ns && curSignature.Contains(m.symbol) select m);
                    foreach (var m in query)
                        yield return m;
                }
            }
        }

        private string GetNs(string symbol)
        {
            return new CNL.DL.DlName() { id = symbol.Substring(2) }.Split().term ?? "";
        }

        private bool IsLocalInstance(string symbol)
        {
            return symbol.StartsWith("I:_");
        }

        public IEnumerable<ScriptLine> GetModule(IEnumerable<string> signature, LocalityKind kind, int reasoningRadius,  bool getModalities, bool modularizeByNamespaces)
        {
            HashSet<string> outSignature;

            List<ScriptLine> ret = new List<ScriptLine>();

            var visited = new Dictionary<string, HashSet<string>>();
            var curSignature = new HashSet<string>(signature);
            var ommitSign = new HashSet<string>();
            var namespaces = new HashSet<string>();
            if (modularizeByNamespaces)
                namespaces = new HashSet<string>((from x in curSignature select GetNs(x)));
            else
                namespaces = HashSetWithSingleEmptyString;

            curSignature.Add("∀");
            while (true)
            {
                var newSign = new HashSet<string>();
                foreach (var row in GetMod(kind, curSignature, namespaces))
                {
                    if (!visited.ContainsKey(row.expression))
                        visited.Add(row.expression, new HashSet<string>());

                    if (visited[row.expression].Add(row.loci))
                    {
                        if (visited[row.expression].Count >= row.num_loci)
                        {
                            var line = new ScriptLine(row.expression);
                            if (!getModalities && line.GetStatement().modality != Statement.Modality.IS)
                                continue;

                            var toAdd = new HashSet<string>(line.GetSignature());
                            var ommit = new HashSet<string>(from i in toAdd where IsLocalInstance(i) select i);
                            if (modularizeByNamespaces)
                            {
                                var omNss = new HashSet<string>(from i in ommit select GetNs(i));
                                omNss.ExceptWith(namespaces);
                                if (omNss.Count > 0)
                                    continue;
                            }

                            ommit.ExceptWith(curSignature);
                            if (reasoningRadius == -1 || reasoningRadius > 0 || ommit.Count == 0)
                                ret.Add(line);
                            ommitSign.UnionWith(ommit);
                            toAdd.ExceptWith(ommit);
                            newSign.UnionWith(toAdd);

                        }
                    }
                }

                if (reasoningRadius == -1 || reasoningRadius > 0)
                {
                    if (reasoningRadius != -1)
                        reasoningRadius--;

                    newSign.UnionWith(ommitSign);
                    ommitSign.Clear();
                }
                newSign.ExceptWith(curSignature);
                if (modularizeByNamespaces)
                    namespaces.UnionWith(from x in newSign select GetNs(x));
                if (newSign.Count == 0) break;
                curSignature = newSign;
            }
            outSignature = curSignature;
            return ret;
        }


       public IEnumerable<string> GetNewAnnotationsSet(IEnumerable<string> annotations)
       {
           throw new NotImplementedException();
       }

       public IEnumerable<string> GetOldAnnotationsSet(IEnumerable<string> annotations)
       {
           throw new NotImplementedException();
       }
    }
}
