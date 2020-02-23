using System;
using System.Collections.Generic;
using System.Text;

namespace Ontorion.CNL.DL
{
    public class GenericVisitor : Ontorion.CNL.DL.IVisitor
    {
        protected VisitingParam<string> isKindOf = new VisitingParam<string>("C");

        public virtual object Visit(Ontorion.CNL.DL.Paragraph e)
        {
            List<Statement> newStmt = new List<Statement>();
            foreach (var x in e.Statements)
                x.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.Subsumption e)
        {
            e.C.accept(this);
            e.D.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.Equivalence e)
        {
            for (int i = 0; i < e.Equivalents.Count; i++)
                e.Equivalents[i].accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.Disjoint e)
        {
            for (int i = 0; i < e.Disjoints.Count; i++)
                e.Disjoints[i].accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.DisjointUnion e)
        {
            for (int i = 0; i < e.Union.Count; i++)
                e.Union[i].accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.HasKey e)
        {
            using (isKindOf.set("D"))
            {
                for (int i = 0; i < e.DataRoles.Count; i++)
                    e.DataRoles[i].accept(this);
            }
            using (isKindOf.set("R"))
            {
                for (int i = 0; i < e.Roles.Count; i++)
                    e.Roles[i].accept(this);
            }
            e.C.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.RoleInclusion e)
        {
            using (isKindOf.set("R"))
            {
                e.C.accept(this);
                e.D.accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.RoleEquivalence e)
        {
            using (isKindOf.set("R"))
            {
                for (int i = 0; i < e.Equivalents.Count; i++)
                    e.Equivalents[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.RoleDisjoint e)
        {
            using (isKindOf.set("R"))
            {
                for (int i = 0; i < e.Disjoints.Count; i++)
                    e.Disjoints[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.DataRoleInclusion e)
        {
            using (isKindOf.set("D"))
            {
                e.C.accept(this);
                e.D.accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.DataRoleEquivalence e)
        {
            using (isKindOf.set("D"))
            {
                for (int i = 0; i < e.Equivalents.Count; i++)
                    e.Equivalents[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.DataRoleDisjoint e)
        {
            using (isKindOf.set("D"))
            {
                for (int i = 0; i < e.Disjoints.Count; i++)
                    e.Disjoints[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.ComplexRoleInclusion e)
        {
            using (isKindOf.set("R"))
            {
                foreach (Node n in e.RoleChain)
                    n.accept(this);
                e.R.accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.InstanceOf e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
            }
            e.I.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.RelatedInstances e)
        {
            using (isKindOf.set("R"))
            {
                e.R.accept(this);
            }
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.NamedInstance e)
        {
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.UnnamedInstance e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.InstanceValue e)
        {
            using (isKindOf.set("D"))
            {
                e.R.accept(this);
                return e;
            }
            e.I.accept(this);
            e.V.accept(this);
        }

        public virtual object Visit(Ontorion.CNL.DL.SameInstances e)
        {
            foreach (var i in e.Instances)
                i.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.DifferentInstances e)
        {
            foreach (var i in e.Instances)
                i.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.Number e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.String e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.Float e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.Bool e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.TopBound e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.TotalBound e)
        {
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.Facet e)
        {
            return e.V.accept(this);
        }

        public virtual object Visit(Ontorion.CNL.DL.FacetList e)
        {
            foreach (var F in e.List)
                F.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.BoundFacets e)
        {
            return e.FL.accept(this);
        }


        public virtual object Visit(Ontorion.CNL.DL.BoundOr e)
        {
            foreach (var C in e.List)
                C.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.BoundAnd e)
        {
            foreach (var C in e.List)
                C.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.BoundNot e)
        {
            e.B.accept(this);
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.BoundVal e)
        {
            return e.V.accept(this);
        }

        public virtual object Visit(Ontorion.CNL.DL.ValueSet e)
        {
            foreach (var val in e.Values)
            {
                val.accept(this);
            }
            return e;
        }

        public virtual object Visit(Ontorion.CNL.DL.Atomic e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.Top e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.Bottom e)
        {
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.RoleInversion e)
        {
            using (isKindOf.set("R"))
            {
                e.R.accept(this);
                return e;
            }
        }
        public virtual object Visit(Ontorion.CNL.DL.InstanceSet e)
        {
            using (isKindOf.set("I"))
            {
                foreach (var I in e.Instances)
                    I.accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.ConceptOr e)
        {
            using (isKindOf.set("C"))
            {
                foreach (var C in e.Exprs)
                    C.accept(this);
                return e;
            }
        }

        public virtual object Visit(Ontorion.CNL.DL.ConceptAnd e)
        {
            using (isKindOf.set("C"))
            {
                foreach (var C in e.Exprs)
                    C.accept(this);
                return e;
            }
        }
        public virtual object Visit(Ontorion.CNL.DL.ConceptNot e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
                return e;
            }
        }
        public virtual object Visit(Ontorion.CNL.DL.OnlyRestriction e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            using (isKindOf.set("C"))
                e.C.accept(this);
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.SomeRestriction e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            using (isKindOf.set("C"))
                e.C.accept(this);
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.OnlyValueRestriction e)
        {
            using (isKindOf.set("D"))
                e.R.accept(this);
            e.B.accept(this);
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.SomeValueRestriction e)
        {
            using (isKindOf.set("D"))
                e.R.accept(this);
            e.B.accept(this);
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.SelfReference e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.NumberRestriction e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            using (isKindOf.set("C"))
                e.C.accept(this);
            return e;
        }
        public virtual object Visit(Ontorion.CNL.DL.NumberValueRestriction e)
        {
            using (isKindOf.set("D"))
                e.R.accept(this);
            e.B.accept(this);
            return e;
        }

        public virtual object Visit(Annotation e)
        {
            return e;
        }

        public virtual object Visit(DLAnnotationAxiom e)
        {
            return e;
        }

        public virtual object Visit(DateTimeVal e)
        {
            return e;
        }
        public virtual object Visit(Duration e)
        {
            return e;
        }

        public virtual object Visit(SwrlStatement e)
        {
            e.slp.accept(this);
            e.slc.accept(this);
            return e;
        }

        public virtual object Visit(SwrlIterate e)
        {
            e.slp.accept(this);
            e.slc.accept(this);
            e.vars.accept(this);
            return e;
        }
        
        public virtual object Visit(SwrlItemList e)
        {
            foreach (var i in e.list)
                i.accept(this);
            return e;
        }

        public virtual object Visit(SwrlInstance e)
        {
            using (isKindOf.set("C"))
                e.C.accept(this);
            e.I.accept(this);
            return e;
        }

        public virtual object Visit(SwrlRole e)
        {
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public virtual object Visit(SwrlSameAs e)
        {
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public virtual object Visit(SwrlDifferentFrom e)
        {
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public virtual object Visit(SwrlDataProperty e)
        {
            e.IO.accept(this);
            e.DO.accept(this);
            return e;
        }

        public virtual object Visit(SwrlDataRange e)
        {
            e.B.accept(this);
            e.DO.accept(this);
            return e;
        }

        public virtual object Visit(SwrlBuiltIn e)
        {
            foreach(var v in e.Values)
                v.accept(this);
            return e;
        }

        public virtual object Visit(ExeStatement e)
        {
            e.slp.accept(this);
            e.args.accept(this);
            return e;
        }

        public virtual object Visit(SwrlVarList e)
        {
            foreach (var x in e.list)
                x.accept(this);
            return e;
        }


        public virtual object Visit(SwrlDVal e)
        {
            e.Val.accept(this);
            return e;
        }

        public virtual object Visit(SwrlDVar e)
        {
            return e;
        }

        public virtual object Visit(SwrlIVal e)
        {
            return e;
        }

        public virtual object Visit(SwrlIVar e)
        {
            return e;
        }

        public virtual object Visit(DataTypeDefinition e)
        {
            e.B.accept(this);
            return e;
        }

        public virtual object Visit(DTBound e)
        {
            return e;
        }

        public virtual object Visit(CodeStatement e)
        {
            return e;
        }

    }
}
