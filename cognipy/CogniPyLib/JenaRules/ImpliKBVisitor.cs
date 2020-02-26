using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;

namespace CogniPy.Executing.HermiT
{
    public class ImpliKBVisitor : CogniPy.CNL.DL.GenericVisitor
    {
        HashSet<Tuple<string, string>> typeOf = new HashSet<Tuple<string, string>>();
        HashSet<Tuple<string, string, string>> relatedTo = new HashSet<Tuple<string, string, string>>();
        HashSet<Tuple<string, string, string, string>> valueOf = new HashSet<Tuple<string, string, string, string>>();
        HashSet<string> others = new HashSet<string>();
        bool isImported = false;

        bool askMode = false;
        bool? entailmentFound;

        CNL.DL.Serializer ser = new CNL.DL.Serializer();

        public void Import(CNL.DL.Paragraph p)
        {
            foreach (var stmt in p.Statements)
                others.Add(ser.Serialize(stmt));
            this.Visit(p);
            isImported = true;
        }

        public bool IsEntailed(CNL.DL.Statement e)
        {
            if (!isImported) return false; 

            entailmentFound = null;
            askMode = true;
            e.accept(this);
            askMode = false;
            if (!entailmentFound.HasValue)
            {
                return others.Contains(ser.Serialize(e));
            }
            else
                return entailmentFound.Value;
        }

        public override object Visit(CNL.DL.InstanceOf e)
        {
            if (e.I is CNL.DL.NamedInstance)
            {
                if (e.C is CNL.DL.Atomic)
                {
                    var tup = Tuple.Create((e.I as CNL.DL.NamedInstance).name, (e.C as CNL.DL.Atomic).id);
                    if (askMode)
                        entailmentFound = typeOf.Contains(tup);
                    else
                        typeOf.Add(tup);
                }
                else if (e.C is CNL.DL.Top)
                {
                    var tup = Tuple.Create((e.I as CNL.DL.NamedInstance).name, "");
                    if (askMode)
                        entailmentFound = typeOf.Contains(tup);
                    else
                        typeOf.Add(tup);
                }
            }

            return null;
        }

        public override object Visit(CNL.DL.RelatedInstances e)
        {
            if (e.I is CNL.DL.NamedInstance && e.J is CNL.DL.NamedInstance)
            {
                if (e.R is CNL.DL.Atomic)
                {
                    var tup = Tuple.Create((e.I as CNL.DL.NamedInstance).name, (e.R as CNL.DL.Atomic).id, (e.J as CNL.DL.NamedInstance).name);
                    if (askMode)
                        entailmentFound = relatedTo.Contains(tup);
                    else
                        relatedTo.Add(tup);
                }
            }
            return null;
        }

        public override object Visit(CNL.DL.InstanceValue e)
        {
            if (e.I is CNL.DL.NamedInstance)
            {
                if (e.R is CNL.DL.Atomic)
                {
                    var tup = Tuple.Create((e.I as CNL.DL.NamedInstance).name, (e.R as CNL.DL.Atomic).id, e.V.getTypeTag(), e.V.getVal());
                    if (askMode)
                        entailmentFound = valueOf.Contains(tup);
                    else
                        valueOf.Add(tup);
                }
            }
            return null;
        }

    }
}
