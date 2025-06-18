using System.Collections.Generic;
using System.Linq;

namespace CogniPy.Splitting
{
    public class ScriptSet
    {
        public ScriptSet()
        {
        }

        public ScriptSet(ScriptSet x)
        {
            script = new Dictionary<string, ScriptLine>(x.script);
        }

        public ScriptSet(IEnumerable<ScriptLine> mod)
        {
            foreach (var line in mod)
                Add(line);
        }

        private Dictionary<string, ScriptLine> script = new Dictionary<string, ScriptLine>();

        public bool Add(ScriptLine line)
        {
            if (!script.ContainsKey(line.Logic()))
            {
                script.Add(line.Logic(), line);
                return true;
            }
            return false;
        }

        public bool Remove(ScriptLine line)
        {
            if (script.ContainsKey(line.Logic()))
            {
                script.Remove(line.Logic());
                return true;
            }
            return false;
        }

        public bool Empty()
        {
            return script.Count == 0;
        }

        public bool IsABoxOnly()
        {
            foreach (var line in script)
            {
                if (!line.Value.IsABox())
                    return false;
            }
            return true;
        }

        public bool NoInstances()
        {
            foreach (var line in script)
            {
                if (line.Value.HasInstance())
                    return false;
            }
            return true;
        }

        public HashSet<string> GetSignature()
        {
            HashSet<string> totalSign = new HashSet<string>();
            foreach (var line in script)
                totalSign.UnionWith(line.Value.GetSignature());

            return totalSign;
        }

        public ScriptSet GetAnnotations()
        {
            return new ScriptSet(from l in script.Values where l.IsAnnotation() select l);
        }

        public ScriptSet Logic()
        {
            return new ScriptSet(from l in script.Values select l);
        }

        public IEnumerable<string> LogicSet()
        {
            return from l in script.Values select l.Logic();
        }

        /// <summary>
        /// Returns the constraints contained in the current script set
        /// </summary>
        /// <returns></returns>
        public IEnumerable<string> GetConstraintSet()
        {
            return from l in script.Values where l.IsConstraint() select l.Logic();
        }

        public IEnumerable<string> GetExeRuleSet()
        {
            return from l in script.Values where l.IsExeRuleExpression() select l.Logic();
        }

        public IEnumerable<string> GetKeys()
        {
            return script.Keys;
        }

        public IEnumerable<ScriptLine> GetScript()
        {
            return script.Values;
        }
    }
}