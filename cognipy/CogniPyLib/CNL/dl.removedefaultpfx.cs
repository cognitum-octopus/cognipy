using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;

namespace CogniPy.CNL.DL
{
    public class RemoveDefaultPfxVisitor : CogniPy.CNL.DL.GenericVisitor
    {
        string defaultPfx;
        public RemoveDefaultPfxVisitor(string defaultPfx,string defaultNamespace=null) {
            if (!string.IsNullOrWhiteSpace(defaultPfx))
                this.defaultPfx = defaultPfx;
            else if (!string.IsNullOrWhiteSpace(defaultNamespace) && !defaultNamespace.StartsWith("<") && !defaultNamespace.EndsWith(">"))
                this.defaultPfx = "<" + defaultNamespace + ">";
            else if (!string.IsNullOrWhiteSpace(defaultNamespace))
                this.defaultPfx = defaultNamespace;
        }

        string removeDefaultPfx(string nm)
        {
            var parst = new DlName() { id = nm }.Split();
            
            var trm = (parst.term == defaultPfx) ? null : parst.term;
            return new DlName.Parts() { name = parst.name, local = parst.local, quoted = parst.quoted, term = trm }.Combine().id;
        }

        public override object Visit(DLAnnotationAxiom e)
        {
            e.annotName = removeDefaultPfx(e.annotName);
            e.subject = removeDefaultPfx(e.subject);
            return base.Visit(e);
        }

        public override object Visit(Atomic e)
        {
            e.id = removeDefaultPfx(e.id);
            return base.Visit(e);
        }

        public override object Visit(NamedInstance e)
        {
            e.name = removeDefaultPfx(e.name);
            return base.Visit(e);
        }
        
        public override object Visit(DisjointUnion e)
        {
            e.name = removeDefaultPfx(e.name);
            return base.Visit(e);
        }

        public override object Visit(InstanceOf e)
        {
            if (e.I is NamedInstance)
                (e.I as NamedInstance).name = removeDefaultPfx((e.I as NamedInstance).name);
            return base.Visit(e);
        }

        public override object Visit(RelatedInstances e)
        {
            if (e.I is NamedInstance)
                (e.I as NamedInstance).name = removeDefaultPfx((e.I as NamedInstance).name);
            if (e.J is NamedInstance)
                (e.J as NamedInstance).name = removeDefaultPfx((e.J as NamedInstance).name);
            return base.Visit(e);
        }

        public override object Visit(InstanceValue e)
        {
            if (e.I is NamedInstance)
                (e.I as NamedInstance).name = removeDefaultPfx((e.I as NamedInstance).name);
            return base.Visit(e);
        }

        public override object Visit(SwrlRole e)
        {
            e.R = removeDefaultPfx(e.R);
            return base.Visit(e);
        }

        public override object Visit(SwrlDataProperty e)
        {
            e.R = removeDefaultPfx(e.R);
            return base.Visit(e);
        }

        public override object Visit(SwrlIVal e)
        {
            e.I = removeDefaultPfx(e.I);
            return base.Visit(e);
        }

        public override object Visit(SwrlVarList e)
        {
            e.list = (from x in e.list select x.accept(this) as IExeVar).ToList();
            return base.Visit(e);
        }
    }
}
