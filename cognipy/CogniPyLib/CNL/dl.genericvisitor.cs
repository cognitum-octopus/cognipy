using System.Collections.Generic;

namespace CogniPy.CNL.DL
{
    public class GenericVisitor : CogniPy.CNL.DL.IVisitor
    {
        protected VisitingParam<string> isKindOf = new VisitingParam<string>("C");

        public virtual object Visit(CogniPy.CNL.DL.Paragraph e)
        {
            List<Statement> newStmt = new List<Statement>();
            foreach (var x in e.Statements)
                x.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.Subsumption e)
        {
            e.C.accept(this);
            e.D.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.Equivalence e)
        {
            for (int i = 0; i < e.Equivalents.Count; i++)
                e.Equivalents[i].accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.Disjoint e)
        {
            for (int i = 0; i < e.Disjoints.Count; i++)
                e.Disjoints[i].accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.DisjointUnion e)
        {
            for (int i = 0; i < e.Union.Count; i++)
                e.Union[i].accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.HasKey e)
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

        public virtual object Visit(CogniPy.CNL.DL.RoleInclusion e)
        {
            using (isKindOf.set("R"))
            {
                e.C.accept(this);
                e.D.accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.RoleEquivalence e)
        {
            using (isKindOf.set("R"))
            {
                for (int i = 0; i < e.Equivalents.Count; i++)
                    e.Equivalents[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.RoleDisjoint e)
        {
            using (isKindOf.set("R"))
            {
                for (int i = 0; i < e.Disjoints.Count; i++)
                    e.Disjoints[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.DataRoleInclusion e)
        {
            using (isKindOf.set("D"))
            {
                e.C.accept(this);
                e.D.accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.DataRoleEquivalence e)
        {
            using (isKindOf.set("D"))
            {
                for (int i = 0; i < e.Equivalents.Count; i++)
                    e.Equivalents[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.DataRoleDisjoint e)
        {
            using (isKindOf.set("D"))
            {
                for (int i = 0; i < e.Disjoints.Count; i++)
                    e.Disjoints[i].accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.ComplexRoleInclusion e)
        {
            using (isKindOf.set("R"))
            {
                foreach (Node n in e.RoleChain)
                    n.accept(this);
                e.R.accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.InstanceOf e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
            }
            e.I.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.RelatedInstances e)
        {
            using (isKindOf.set("R"))
            {
                e.R.accept(this);
            }
            e.I.accept(this);
            e.J.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.NamedInstance e)
        {
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.UnnamedInstance e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.InstanceValue e)
        {
            using (isKindOf.set("D"))
            {
                e.R.accept(this);
            }
            e.I.accept(this);
            e.V.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.SameInstances e)
        {
            foreach (var i in e.Instances)
                i.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.DifferentInstances e)
        {
            foreach (var i in e.Instances)
                i.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.Number e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.DecimalNumber e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.String e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.Float e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.Bool e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.TopBound e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.TotalBound e)
        {
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.Facet e)
        {
            return e.V.accept(this);
        }

        public virtual object Visit(CogniPy.CNL.DL.FacetList e)
        {
            foreach (var F in e.List)
                F.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.BoundFacets e)
        {
            return e.FL.accept(this);
        }


        public virtual object Visit(CogniPy.CNL.DL.BoundOr e)
        {
            foreach (var C in e.List)
                C.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.BoundAnd e)
        {
            foreach (var C in e.List)
                C.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.BoundNot e)
        {
            e.B.accept(this);
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.BoundVal e)
        {
            return e.V.accept(this);
        }

        public virtual object Visit(CogniPy.CNL.DL.ValueSet e)
        {
            foreach (var val in e.Values)
            {
                val.accept(this);
            }
            return e;
        }

        public virtual object Visit(CogniPy.CNL.DL.Atomic e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.Top e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.Bottom e)
        {
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.RoleInversion e)
        {
            using (isKindOf.set("R"))
            {
                e.R.accept(this);
                return e;
            }
        }
        public virtual object Visit(CogniPy.CNL.DL.InstanceSet e)
        {
            using (isKindOf.set("I"))
            {
                foreach (var I in e.Instances)
                    I.accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.ConceptOr e)
        {
            using (isKindOf.set("C"))
            {
                foreach (var C in e.Exprs)
                    C.accept(this);
                return e;
            }
        }

        public virtual object Visit(CogniPy.CNL.DL.ConceptAnd e)
        {
            using (isKindOf.set("C"))
            {
                foreach (var C in e.Exprs)
                    C.accept(this);
                return e;
            }
        }
        public virtual object Visit(CogniPy.CNL.DL.ConceptNot e)
        {
            using (isKindOf.set("C"))
            {
                e.C.accept(this);
                return e;
            }
        }
        public virtual object Visit(CogniPy.CNL.DL.OnlyRestriction e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            using (isKindOf.set("C"))
                e.C.accept(this);
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.SomeRestriction e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            using (isKindOf.set("C"))
                e.C.accept(this);
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.OnlyValueRestriction e)
        {
            using (isKindOf.set("D"))
                e.R.accept(this);
            e.B.accept(this);
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.SomeValueRestriction e)
        {
            using (isKindOf.set("D"))
                e.R.accept(this);
            e.B.accept(this);
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.SelfReference e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.NumberRestriction e)
        {
            using (isKindOf.set("R"))
                e.R.accept(this);
            using (isKindOf.set("C"))
                e.C.accept(this);
            return e;
        }
        public virtual object Visit(CogniPy.CNL.DL.NumberValueRestriction e)
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
            foreach (var v in e.Values)
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
