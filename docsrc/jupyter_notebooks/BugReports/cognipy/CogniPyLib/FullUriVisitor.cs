using System;
using System.Linq;

namespace CogniPy.CNL.DL
{
    public class FullUriVisitor : CogniPy.CNL.DL.GenericVisitor
    {
        Func<string, string> pfx2ns;
        string _defaultNs;
        public FullUriVisitor(Func<string, string> pfx2ns, string defaultNs = null)
        {
            if (pfx2ns == null)
                throw new Exception("Cannot initialize the FullUriVisitor without giving the prefix to namespace map.");
            this.pfx2ns = pfx2ns;
            this._defaultNs = defaultNs;
        }

        string applyFullUri(string nm)
        {
            return CNLTools.DLToFullUri(nm, ARS.EntityKind.Instance, pfx2ns, _defaultNs);
        }

        public override object Visit(DLAnnotationAxiom e)
        {
            e.annotName = applyFullUri(e.annotName);
            if (e.subjKind != ARS.EntityKind.Statement.ToString())
                e.subject = applyFullUri(e.subject);
            return base.Visit(e);
        }

        public override object Visit(Atomic e)
        {
            e.id = applyFullUri(e.id);
            return base.Visit(e);
        }

        public override object Visit(NamedInstance e)
        {
            e.name = applyFullUri(e.name);
            return base.Visit(e);
        }

        public override object Visit(DisjointUnion e)
        {
            e.name = applyFullUri(e.name);
            return base.Visit(e);
        }

        public override object Visit(InstanceOf e)
        {
            if (e.I is NamedInstance)
                (e.I as NamedInstance).name = applyFullUri((e.I as NamedInstance).name);
            return base.Visit(e);
        }

        public override object Visit(RelatedInstances e)
        {
            if (e.I is NamedInstance)
                (e.I as NamedInstance).name = applyFullUri((e.I as NamedInstance).name);
            if (e.J is NamedInstance)
                (e.J as NamedInstance).name = applyFullUri((e.J as NamedInstance).name);
            return base.Visit(e);
        }

        public override object Visit(InstanceValue e)
        {
            if (e.I is NamedInstance)
                (e.I as NamedInstance).name = applyFullUri((e.I as NamedInstance).name);
            return base.Visit(e);
        }

        public override object Visit(SwrlInstance e)
        {
            return base.Visit(e);
        }

        public override object Visit(SwrlRole e)
        {
            e.J.accept(this);
            e.R = applyFullUri(e.R);
            return base.Visit(e);
        }

        public override object Visit(SwrlSameAs e)
        {
            return base.Visit(e);
        }

        public override object Visit(SwrlDifferentFrom e)
        {
            return base.Visit(e);
        }

        public override object Visit(SwrlDataProperty e)
        {
            e.R = applyFullUri(e.R);
            return base.Visit(e);
        }

        public override object Visit(SwrlIVal e)
        {
            e.I = applyFullUri(e.I);
            return e;
        }

        public override object Visit(SwrlDVal e)
        {
            return e;
        }

        public override object Visit(SwrlIVar e)
        {
            return e;
        }

        public override object Visit(SwrlDVar e)
        {
            return e;
        }

        public override object Visit(SwrlVarList e)
        {
            e.list = (from x in e.list select x.accept(this) as IExeVar).ToList();
            return base.Visit(e);
        }

    }
}
