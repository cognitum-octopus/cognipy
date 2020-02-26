using CogniPy.CNL.DL;
using System;
using System.Collections.Generic;
using System.Linq;

namespace CogniPy.CNL
{
    internal class DLModSimplifier : GenericVisitor
    {
        private NamedInstance getSingleNamgedInstance(Node C)
        {
            if (C is InstanceSet)
            {
                if ((C as InstanceSet).Instances.Count == 1)
                {
                    if ((C as InstanceSet).Instances[0] is NamedInstance)
                    {
                        return (C as InstanceSet).Instances[0] as NamedInstance;
                    }
                }
            }
            return null;
        }

        private Value getSingleEqualValue(AbstractBound C)
        {
            if (C is ValueSet)
            {
                if ((C as ValueSet).Values.Count == 1)
                    return (C as ValueSet).Values[0] as Value;
            }
            else if (C is BoundFacets)
            {
                if ((C as BoundFacets).FL.List.Count == 1 && (C as BoundFacets).FL.List[0].Kind == "=")
                    return (C as BoundFacets).FL.List[0].V;
            }
            else if (C is BoundVal)
            {
                if ((C as BoundVal).Kind == "=")
                    return (C as BoundVal).V;
            }
            else if (C is BoundOr)
            {
                if ((C as BoundOr).List.Count == 1)
                    return getSingleEqualValue((C as BoundOr).List.First());
            }
            else if (C is BoundAnd)
            {
                if ((C as BoundAnd).List.Count == 1)
                    return getSingleEqualValue((C as BoundAnd).List.First());
            }
            return null;
        }

        public override object Visit(Paragraph e)
        {
            List<Statement> newStmt = new List<Statement>();
            foreach (var x in e.Statements)
            {
                var o = x.accept(this);
                if (o is Statement)
                    newStmt.Add(o as Statement);
                if (o is IEnumerable<Statement>)
                    newStmt.AddRange(o as IEnumerable<Statement>);
            }
            return new Paragraph(null) { Statements = newStmt };
        }

        private object ConvertToAbox(NamedInstance iC, Statement.Modality modality, Node D)
        {
            if (D is SomeRestriction)
            {
                var iS = getSingleNamgedInstance((D as SomeRestriction).C);
                if (iS != null)
                {
                    var r = (D as SomeRestriction).R;

                    bool inv = false;
                    while (r is RoleInversion)
                    {
                        r = (r as RoleInversion).R;
                        inv = !inv;
                    }
                    if (!inv)
                        return new RelatedInstances(null) { R = r, I = iC, J = iS, modality = modality };
                    else
                        return new RelatedInstances(null) { R = r, J = iC, I = iS, modality = modality };
                }
            }
            else if (D is ConceptAnd)
            {
                List<Statement> ret = new List<Statement>();

                foreach (var E in (D as ConceptAnd).Exprs)
                {
                    var r = ConvertToAbox(iC, modality, E);
                    if (r == null)
                        return null;
                    else if (r is List<Statement>)
                        ret.AddRange(r as List<Statement>);
                    else if (r is Statement)
                        ret.Add(r as Statement);
                    else
                        throw new InvalidOperationException();
                }
                return ret;
            }
            else if (D is SomeValueRestriction)
            {
                var iV = getSingleEqualValue((D as SomeValueRestriction).B);
                if (iV != null)
                {
                    return new InstanceValue(null) { R = (D as SomeValueRestriction).R, I = iC, V = iV, modality = modality };
                }
            }

            return new InstanceOf(null) { I = new NamedInstance(null) { name = iC.name }, C = D, modality = modality };
        }

        public override object Visit(Subsumption e)
        {
            var iC = getSingleNamgedInstance(e.C);
            var iD = getSingleNamgedInstance(e.D);
            if (iC != null)
            {
                if (iD != null)
                {
                    var list = new InstanceList(null) { List = new List<Instance>(new Instance[] { iC, iD }) };
                    return new SameInstances(null, list, e.modality);
                }
                else
                    return ConvertToAbox(iC, e.modality, e.D);
            }
            return e;
        }

        public override object Visit(RelatedInstances e)
        {
            var r = e.R;

            bool inv = false;
            while (r is RoleInversion)
            {
                r = (r as RoleInversion).R;
                inv = !inv;
            }
            if (!inv)
                return e;
            else
                return new RelatedInstances(null) { R = r, J = e.I, I = e.J, modality = e.modality };
        }

    }
}