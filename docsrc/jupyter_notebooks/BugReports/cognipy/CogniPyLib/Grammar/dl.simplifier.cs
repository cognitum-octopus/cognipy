using System;
using System.Collections.Generic;
using System.Text;

namespace Ontorion.CNL.DL
{
    public class Simplifier : Ontorion.CNL.DL.IVisitor
    {
        public void Simplify(Ontorion.CNL.DL.Paragraph p)
        {
            p = p.accept(this) as Paragraph;
        }

        public void Simplify(Ontorion.CNL.DL.Statement s)
        {
            s = s.accept(this) as Statement;
        }

        public object Visit(Ontorion.CNL.DL.Paragraph e) 
        {
            List<Statement> newStmt = new List<Statement>();
            foreach (var x in e.Statements)
                newStmt.Add(x.accept(this) as Ontorion.CNL.DL.Statement);
            e.Statements = newStmt;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.Subsumption e) 
        {
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            e.D = e.D.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        
        public object Visit(Ontorion.CNL.DL.Equivalence e)
        {
            for (int i = 0; i < e.Equivalents.Count;i++ )
                e.Equivalents[i] = e.Equivalents[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Equivalents); 
            return e;
        }

        public object Visit(Ontorion.CNL.DL.Disjoint e)
        {
            for (int i = 0; i < e.Disjoints.Count; i++)
                e.Disjoints[i] = e.Disjoints[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Disjoints);
            return e;
        }

        public object Visit(Ontorion.CNL.DL.DisjointUnion e)
        {
            for (int i = 0; i < e.Union.Count; i++)
                e.Union[i] = e.Union[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Union);
            return e;
        }

        public object Visit(Ontorion.CNL.DL.HasKey e)
        {
            for (int i = 0; i < e.DataRoles.Count; i++)
                e.DataRoles[i] = e.DataRoles[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.DataRoles);
            for (int i = 0; i < e.Roles.Count; i++)
                e.Roles[i] = e.Roles[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Roles);
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.RoleInclusion e)
        {
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            e.D = e.D.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.RoleEquivalence e)
        {
            for (int i = 0; i < e.Equivalents.Count; i++)
                e.Equivalents[i] = e.Equivalents[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Equivalents);
            return e;
        }

        public object Visit(Ontorion.CNL.DL.RoleDisjoint e)
        {
            for (int i = 0; i < e.Disjoints.Count; i++)
                e.Disjoints[i] = e.Disjoints[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Disjoints); 
            return e;
        }

        public object Visit(Ontorion.CNL.DL.DataRoleInclusion e)
        {
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            e.D = e.D.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.DataRoleEquivalence e)
        {
            for (int i = 0; i < e.Equivalents.Count; i++)
                e.Equivalents[i] = e.Equivalents[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Equivalents);
            return e;
        }

        public object Visit(Ontorion.CNL.DL.DataRoleDisjoint e)
        {
            for (int i = 0; i < e.Disjoints.Count; i++)
                e.Disjoints[i] = e.Disjoints[i].accept(this) as Ontorion.CNL.DL.Node;
            Sort(e.Disjoints); 
            return e;
        }

        public object Visit(Ontorion.CNL.DL.ComplexRoleInclusion e)
        {
            List<Node> newRoleChain = new List<Node>();
            foreach (Node n in e.RoleChain)
                newRoleChain.Add(n.accept(this) as Node);
            e.RoleChain = newRoleChain;
            e.R=e.R.accept(this) as Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.InstanceOf e)
        {
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.RelatedInstances e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public static void Assert(bool b)
        {
            if (!b)
            {
#if DEBUG
                System.Diagnostics.Debugger.Break();
#endif
                throw new Exception("DL Simplifier Assertion Failed.");
            }
        }

        public object Visit(Ontorion.CNL.DL.NamedInstance e)
        {
            return e;
        }

        public object Visit(Ontorion.CNL.DL.UnnamedInstance e)
        {
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.InstanceValue e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }

        public object Visit(Ontorion.CNL.DL.SameInstances e)
        {
            return e;
        }
        
        public object Visit(Ontorion.CNL.DL.DifferentInstances e)
        {
            return e;
        }

        public object Visit(Ontorion.CNL.DL.Number e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.String e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.Float e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.Bool e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.TopBound e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.TotalBound e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.Bound e)
        {
            Assert(false);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.ValueSet e)
        {
            Assert(false);
            return e;
        }

        public object Visit(Ontorion.CNL.DL.Atomic e)
        {
            return e;
        }
        public object Visit(Ontorion.CNL.DL.Top e)
        {
            return e;
        }
        public object Visit(Ontorion.CNL.DL.Bottom e)
        {
            return e;
        }
        public object Visit(Ontorion.CNL.DL.RoleInversion e)
        {
            if (e.R is RoleInversion)
                return (e.R as RoleInversion).R.accept(this);
            else
            {
                e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
                return e;
            }
        }
        public object Visit(Ontorion.CNL.DL.InstanceSet e)
        {
            List<Instance> newInstances = new List<Instance>();
            foreach (var I in e.Instances)
                newInstances.Add(I.accept(this) as Instance);
            Sort(newInstances);
            e.Instances = newInstances;
            return e;
        }

        void Sort(List<Instance> nodes)
        {
            Ontorion.CNL.DL.Serializer ser = new Serializer();
            nodes.Sort(delegate(Instance a, Instance b)
            {
                var aStr = ser.Serialize(a);
                var bStr = ser.Serialize(b);
                return aStr.CompareTo(bStr);
            });
            var it = nodes.GetEnumerator();
            if (it.MoveNext())
            {
                var toDel = new List<Instance>();
                var oldn = it.Current;
                var oldStr = ser.Serialize(oldn);
                while (it.MoveNext())
                {
                    var str = ser.Serialize(it.Current);
                    if (str == oldStr)
                        toDel.Add(it.Current);
                    else
                    {
                        oldStr = str;
                    }
                }
                foreach (var n in toDel)
                    nodes.Remove(n);
            }
        }
        
        void Sort(List<Node> nodes)
        {
            Ontorion.CNL.DL.Serializer  ser =  new Serializer();
            nodes.Sort(delegate(Node a, Node b) 
            {
                var aStr = ser.Serialize(a);
                var bStr = ser.Serialize(b);
                return aStr.CompareTo(bStr); 
            });
            var it = nodes.GetEnumerator();
            if (it.MoveNext())
            {
                var toDel = new List<Node>();
                var oldn = it.Current;
                var oldStr = ser.Serialize(oldn);
                while (it.MoveNext())
                {
                    var str = ser.Serialize(it.Current);
                    if (str == oldStr)
                        toDel.Add(it.Current);
                    else
                    {
                        oldStr = str;
                    }
                }
                foreach (var n in toDel)
                    nodes.Remove(n);
            }
        }

        public object Visit(Ontorion.CNL.DL.ConceptOr e)
        {
            List<Node> newExprs = new List<Node>();
            foreach (var C in e.Exprs)
            {
                var newC = C.accept(this) as Node;
                if (!(newC is Bottom))
                    newExprs.Add(newC);
            }
            Sort(newExprs);
            if (newExprs.Count == 0)
                return new Top(null);
            else if (newExprs.Count == 1)
                return newExprs[0];
            else
            {
                e.Exprs = newExprs;
                return e;
            }
        }
        public object Visit(Ontorion.CNL.DL.ConceptAnd e)
        {
            List<Node> newExprs = new List<Node>();
            foreach (var C in e.Exprs)
            {
                var newC = C.accept(this) as Node;
                if(!(newC is Top))
                    newExprs.Add(newC);
            }
            Sort(newExprs);
            if (newExprs.Count == 0)
                return new Top(null);
            else if (newExprs.Count == 1)
                return newExprs[0];
            else
            {
                e.Exprs = newExprs;
                return e;
            }
        }
        public object Visit(Ontorion.CNL.DL.ConceptNot e)
        {
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        public object Visit(Ontorion.CNL.DL.OnlyRestriction e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        public object Visit(Ontorion.CNL.DL.SomeRestriction e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        public object Visit(Ontorion.CNL.DL.OnlyValueRestriction e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        public object Visit(Ontorion.CNL.DL.SomeValueRestriction e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        public object Visit(Ontorion.CNL.DL.SelfReference e)
        {
            e.R.accept(this);
            return e;
        }
        public object Visit(Ontorion.CNL.DL.NumberRestriction e)
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            e.C = e.C.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
        public object Visit(Ontorion.CNL.DL.NumberValueRestriction e) 
        {
            e.R = e.R.accept(this) as Ontorion.CNL.DL.Node;
            return e;
        }
    }  
}
