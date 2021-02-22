
using CogniPy.ARS;
using System;
using System.Collections.Generic;
using System.Text;


namespace CogniPy.CNL.DL
{
    public class Serializer : CogniPy.CNL.DL.IVisitor
    {
        HashSet<Tuple<EntityKind, string>> signature = new HashSet<Tuple<EntityKind, string>>();
        Dictionary<string, Value> foundValues = new Dictionary<string, Value>();
        HashSet<Tuple<string, string, string>> dataValues = new HashSet<Tuple<string, string, string>>();
        VisitingParam<EntityKind> isKindOf = new VisitingParam<EntityKind>(EntityKind.Concept);
        VisitingParam<string> attributeName = new VisitingParam<string>(null);
        Dictionary<string, HashSet<string>> dependentAttrs = new Dictionary<string, HashSet<string>>();
        HashSet<string> swrlAttrsBody = new HashSet<string>();
        HashSet<string> swrlAttrsHead = new HashSet<string>();
        List<CNL.DL.InstanceValue> instanceValues = new List<InstanceValue>();
        VisitingParam<HashSet<string>> swrlCurAttr = new VisitingParam<HashSet<string>>(null);

        public List<CNL.DL.InstanceValue> GetInstanceValues()
        {
            return instanceValues;
        }

        public HashSet<Tuple<EntityKind, string>> GetTaggedSignature()
        {
            return signature;
        }

        public static string entName(ARS.EntityKind kind)
        {
            switch (kind)
            {
                case EntityKind.Concept: return "C";
                case EntityKind.Instance: return "I";
                case EntityKind.Role: return "R";
                case EntityKind.DataRole: return "D";
                case EntityKind.DataType: return "T";
                case EntityKind.Annotation: return "N";
                case EntityKind.Statement: return "S";
            }
            throw new InvalidOperationException();
        }
        public HashSet<string> GetSignature()
        {
            HashSet<string> ret = new HashSet<string>();
            foreach (var s in signature)
            {
                ret.Add(entName(s.Item1) + ":" + s.Item2);
            }
            return ret;
        }

        public Dictionary<string, HashSet<string>> GetDependentAttrs()
        {
            return dependentAttrs;
        }


        public HashSet<Tuple<string, string, string>> GetDataValues()
        {
            HashSet<Tuple<string, string, string>> ret = new HashSet<Tuple<string, string, string>>();
            Dictionary<string, HashSet<Tuple<string, string>>> dtt = new Dictionary<string, HashSet<Tuple<string, string>>>();
            HashSet<Tuple<string, string>> more = new HashSet<Tuple<string, string>>();
            foreach (var k in dataValues)
            {
                if (k.Item1 == "")
                {
                    more.Add(Tuple.Create(k.Item2, k.Item3.Substring(1)));
                }
                else if (k.Item2[0] == '\r')
                {
                    var ki = k.Item2.Substring(1);
                    if (!dtt.ContainsKey(ki))
                        dtt.Add(ki, new HashSet<Tuple<string, string>>());
                    dtt[ki].Add(Tuple.Create(k.Item1, k.Item3));
                }
                else
                    ret.Add(k);
            }
            foreach (var m in more)
            {
                if (dtt.ContainsKey(m.Item2))
                {
                    foreach (var n in dtt[m.Item2])
                    {
                        ret.Add(Tuple.Create(n.Item1, m.Item1, n.Item2));
                    }
                }
            }
            return ret;
        }

        public Serializer(bool simplifyBrackets = false) { this.simplifyBrackets = simplifyBrackets; }

        bool simplifyBrackets = false;

        object brack(Node parent, Node child)
        {
            if (simplifyBrackets && (child.priority() == 0 || (child.priority() >= parent.priority())))
            {
                return child.accept(this);
            }
            else
            {
                return "(" + child.accept(this) + ")";
            }
        }

        object brack(AbstractBound parent, AbstractBound child)
        {
            if (simplifyBrackets && (child.priority() == 0 || (child.priority() >= parent.priority())))
            {
                return child.accept(this);
            }
            else
            {
                return "(" + child.accept(this) + ")";
            }
        }

        public string Serialize(CogniPy.CNL.DL.Paragraph p)
        {
            signature = new HashSet<Tuple<EntityKind, string>>();
            dataValues = new HashSet<Tuple<string, string, string>>();
            foundValues = new Dictionary<string, Value>();
            dependentAttrs = new Dictionary<string, HashSet<string>>();
            instanceValues = new List<InstanceValue>();
            return p.accept(this) as string;
        }

        public string Serialize(CogniPy.CNL.DL.Statement s)
        {
            signature = new HashSet<Tuple<EntityKind, string>>();
            dataValues = new HashSet<Tuple<string, string, string>>();
            foundValues = new Dictionary<string, Value>();
            dependentAttrs = new Dictionary<string, HashSet<string>>();
            instanceValues = new List<InstanceValue>();
            return s.accept(this) as string;
        }

        public string Serialize(CogniPy.CNL.DL.Node n)
        {
            signature = new HashSet<Tuple<EntityKind, string>>();
            dataValues = new HashSet<Tuple<string, string, string>>();
            foundValues = new Dictionary<string, Value>();
            dependentAttrs = new Dictionary<string, HashSet<string>>();
            instanceValues = new List<InstanceValue>();
            return n.accept(this) as string;
        }

        public string Serialize(CogniPy.CNL.DL.Instance n)
        {
            signature = new HashSet<Tuple<EntityKind, string>>();
            dataValues = new HashSet<Tuple<string, string, string>>();
            foundValues = new Dictionary<string, Value>();
            dependentAttrs = new Dictionary<string, HashSet<string>>();
            instanceValues = new List<InstanceValue>();
            return n.accept(this) as string;
        }

        public object Visit(CogniPy.CNL.DL.Paragraph e)
        {
            StringBuilder sb = new StringBuilder();
            foreach (var x in e.Statements)
            {
                if (x != null)
                {
                    var str = x.accept(this) as string;
                    if (!System.String.IsNullOrWhiteSpace(str))
                        sb.AppendLine(str);
                }
            }
            return sb.ToString();
        }

        public string Modality(CogniPy.CNL.DL.Statement.Modality m)
        {
            switch (m)
            {
                case Statement.Modality.MUST: return "□";
                case Statement.Modality.SHOULD: return "◊";
                case Statement.Modality.CAN: return "◊◊";
                case Statement.Modality.MUSTNOT: return "~◊◊";
                case Statement.Modality.SHOULDNOT: return "~◊";
                case Statement.Modality.CANNOT: return "~□";
                default: return "";
            }
        }

        public object Visit(CogniPy.CNL.DL.Subsumption e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.C.accept(this));
            sb.Append("⊑");
            sb.Append(Modality(e.modality));
            sb.Append(e.D.accept(this));
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.Equivalence e)
        {
            StringBuilder sb = new StringBuilder();
            if (e.Equivalents.Count == 2)
            {
                sb.Append(e.Equivalents[0].accept(this));
                sb.Append("≡");
                sb.Append(Modality(e.modality));
                sb.Append(e.Equivalents[1].accept(this));
            }
            else
            {
                sb.Append("≡");
                sb.Append(Modality(e.modality));
                sb.Append("(");
                bool first = true;
                foreach (var x in e.Equivalents)
                {
                    if (first)
                        first = false;
                    else
                        sb.Append(",");
                    sb.Append(x.accept(this));
                }
                sb.Append(")");
            }
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.Disjoint e)
        {
            StringBuilder sb = new StringBuilder();
            if (e.Disjoints.Count == 2)
            {
                sb.Append(e.Disjoints[0].accept(this));
                sb.Append("⊑");
                sb.Append(Modality(e.modality));
                sb.Append("￢");
                sb.Append(e.Disjoints[1].accept(this));
                return sb.ToString();
            }
            else
            {
                sb.Append("￢≡(");
                bool first = true;
                foreach (var x in e.Disjoints)
                {
                    if (first)
                        first = false;
                    else
                        sb.Append(",");
                    sb.Append(x.accept(this));
                }
                sb.Append(")");
            }
            return sb.ToString();
        }

        public object Visit(DataTypeDefinition e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.name);
            signature.Add(Tuple.Create(EntityKind.DataType, e.name));
            sb.Append("≡≡");
            using (attributeName.set("\r" + e.name))
                sb.Append(e.B.accept(this));
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.DisjointUnion e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.name);
            sb.Append("￢≡(");
            bool first = true;
            foreach (var x in e.Union)
            {
                if (first)
                    first = false;
                else
                    sb.Append(",");
                sb.Append(x.accept(this));
            }
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.RoleInclusion e)
        {
            using (isKindOf.set(EntityKind.Role))
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(e.C.accept(this));
                sb.Append("⊆");
                sb.Append(Modality(e.modality));
                sb.Append(e.D.accept(this));
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.RoleEquivalence e)
        {
            using (isKindOf.set(EntityKind.Role))
            {
                StringBuilder sb = new StringBuilder();
                if (e.Equivalents.Count == 2)
                {
                    sb.Append(e.Equivalents[0].accept(this));
                    sb.Append("≗");
                    sb.Append(e.Equivalents[1].accept(this));
                }
                else
                {
                    sb.Append("≗(");
                    bool first = true;
                    foreach (var x in e.Equivalents)
                    {
                        if (first)
                            first = false;
                        else
                            sb.Append(",");
                        sb.Append(x.accept(this));
                    }
                    sb.Append(")");
                }
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.RoleDisjoint e)
        {
            using (isKindOf.set(EntityKind.Role))
            {
                StringBuilder sb = new StringBuilder();
                if (e.Disjoints.Count == 2)
                {
                    sb.Append(e.Disjoints[0].accept(this));
                    sb.Append("⊆￢");
                    sb.Append(e.Disjoints[1].accept(this));
                }
                else
                {
                    sb.Append("￢≗(");
                    bool first = true;
                    foreach (var x in e.Disjoints)
                    {
                        if (first)
                            first = false;
                        else
                            sb.Append(",");
                        sb.Append(x.accept(this));
                    }
                    sb.Append(")");
                }
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.ComplexRoleInclusion e)
        {
            using (isKindOf.set(EntityKind.Role))
            {
                StringBuilder sb = new StringBuilder();
                bool first = true;
                foreach (Node n in e.RoleChain)
                {
                    if (first)
                        first = false;
                    else
                        sb.Append("○");
                    sb.Append(n.accept(this));
                }
                sb.Append("⊆");
                sb.Append(Modality(e.modality));
                sb.Append(e.R.accept(this));
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.DataRoleInclusion e)
        {
            using (isKindOf.set(EntityKind.DataRole))
            {
                StringBuilder sb = new StringBuilder();
                sb.Append(e.C.accept(this));
                sb.Append("⋐");
                sb.Append(Modality(e.modality));
                sb.Append(e.D.accept(this));
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.DataRoleEquivalence e)
        {
            using (isKindOf.set(EntityKind.DataRole))
            {
                StringBuilder sb = new StringBuilder();
                if (e.Equivalents.Count == 2)
                {
                    sb.Append(e.Equivalents[0].accept(this));
                    sb.Append("≣");
                    sb.Append(e.Equivalents[1].accept(this));
                }
                else
                {
                    sb.Append("≣(");
                    bool first = true;
                    foreach (var x in e.Equivalents)
                    {
                        if (first)
                            first = false;
                        else
                            sb.Append(",");
                        sb.Append(x.accept(this));
                    }
                    sb.Append(")");
                }
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.DataRoleDisjoint e)
        {
            using (isKindOf.set(EntityKind.DataRole))
            {
                StringBuilder sb = new StringBuilder();
                if (e.Disjoints.Count == 2)
                {
                    sb.Append(e.Disjoints[0].accept(this));
                    sb.Append("⋐￢");
                    sb.Append(e.Disjoints[1].accept(this));
                }
                else
                {
                    sb.Append("￢≣(");
                    bool first = true;
                    foreach (var x in e.Disjoints)
                    {
                        if (first)
                            first = false;
                        else
                            sb.Append(",");
                        sb.Append(x.accept(this));
                    }
                    sb.Append(")");
                }
                return sb.ToString();
            }
        }

        public object Visit(CogniPy.CNL.DL.InstanceOf e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.C.accept(this));
            sb.Append(Modality(e.modality));
            sb.Append("(");
            sb.Append(e.I.accept(this));
            if (e.I is NamedInstance)
                signature.Add(Tuple.Create(EntityKind.Instance, (e.I as NamedInstance).name));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.RelatedInstances e)
        {
            StringBuilder sb = new StringBuilder();
            using (isKindOf.set(EntityKind.Role))
            {
                sb.Append(e.R.accept(this));
                sb.Append(Modality(e.modality));
            }
            sb.Append("(");
            sb.Append(e.I.accept(this));
            if (e.I is NamedInstance)
                signature.Add(Tuple.Create(EntityKind.Instance, (e.I as NamedInstance).name));
            sb.Append(",");
            sb.Append(e.J.accept(this));
            if (e.J is NamedInstance)
                signature.Add(Tuple.Create(EntityKind.Instance, (e.J as NamedInstance).name));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.NamedInstance e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.name);
            signature.Add(Tuple.Create(EntityKind.Instance, e.name));
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.UnnamedInstance e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("[");
            if (e.Only)
                sb.Append("[");
            string conceptExpr;
            using (isKindOf.set(EntityKind.Concept))
            {
                conceptExpr = (string)e.C.accept(this);
            }
            sb.Append(conceptExpr);
            if (e.Only)
                sb.Append("]");
            sb.Append("]");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.InstanceValue e)
        {
            instanceValues.Add(e);
            StringBuilder sb = new StringBuilder();
            using (isKindOf.set(EntityKind.DataRole))
            {
                sb.Append(e.R.accept(this));
            }
            sb.Append(Modality(e.modality));
            sb.Append("(");
            sb.Append(e.I.accept(this));
            if (e.I is NamedInstance)
                signature.Add(Tuple.Create(EntityKind.Instance, (e.I as NamedInstance).name));
            sb.Append(",");
            using (attributeName.set(null))
                sb.Append(e.V.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SameInstances e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("=");
            sb.Append(Modality(e.modality));
            sb.Append("{");
            bool first = true;
            foreach (var I in e.Instances)
            {
                if (first)
                    first = false;
                else
                    sb.Append(",");
                sb.Append(I.accept(this));
            }
            sb.Append("}");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.DifferentInstances e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("≠");
            sb.Append(Modality(e.modality));
            sb.Append("{");
            bool first = true;
            foreach (var I in e.Instances)
            {
                if (first)
                    first = false;
                else
                    sb.Append(",");
                sb.Append(I.accept(this));
            }
            sb.Append("}");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.HasKey e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.C.accept(this));
            sb.Append("○⊑");
            if (e.Roles.Count > 0)
            {
                using (isKindOf.set(EntityKind.Role))
                {
                    sb.Append("(");
                    bool first = true;
                    foreach (var x in e.Roles)
                    {
                        if (first)
                            first = false;
                        else
                            sb.Append(",");
                        sb.Append(x.accept(this));
                    }
                    sb.Append(")");
                }
            }
            if (e.DataRoles.Count > 0)
            {
                sb.Append("⊓");
                using (isKindOf.set(EntityKind.DataRole))
                {
                    sb.Append("(");
                    bool first = true;
                    foreach (var x in e.DataRoles)
                    {
                        if (first)
                            first = false;
                        else
                            sb.Append(",");
                        sb.Append(x.accept(this));
                    }
                    sb.Append(")");
                }
            }
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.Number e)
        {
            var sval = e.val.ToString();
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            if (DeltaKind == "<" || DeltaKind == "≠")
            {
                var sval2 = (int.Parse(sval) - 1).ToString();
                if (attributeName.get() != null)
                    dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval2));
                if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval2))
                    foundValues.Add(e.getTypeTag() + ":" + sval2, e);
            }
            if (DeltaKind == ">" || DeltaKind == "≠")
            {
                var sval2 = (int.Parse(sval) + 1).ToString();
                if (attributeName.get() != null)
                    dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval2));
                if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval2))
                    foundValues.Add(e.getTypeTag() + ":" + sval2, e);
            }
            return sval;
        }

        public object Visit(CogniPy.CNL.DL.String e)
        {
            var sval = e.val;
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            return sval;
        }
        public object Visit(CogniPy.CNL.DL.Float e)
        {
            var sval = e.val.ToString();
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            return sval;
        }
        public object Visit(CogniPy.CNL.DL.DecimalNumber e)
        {
            var sval = e.val.ToString();
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            return sval;
        }

        public object Visit(CogniPy.CNL.DL.Bool e)
        {
            var sval = e.val.ToString();
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            return sval;
        }
        public object Visit(CogniPy.CNL.DL.DateTimeVal e)
        {
            var sval = e.val.ToString();
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            return sval;
        }
        public object Visit(Duration e)
        {
            var sval = e.val.ToString();
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create(e.getTypeTag(), attributeName.get(), sval));
            if (!foundValues.ContainsKey(e.getTypeTag() + ":" + sval))
                foundValues.Add(e.getTypeTag() + ":" + sval, e);
            return sval;
        }

        string DeltaKind = null;
        public object Visit(Facet e)
        {
            DeltaKind = e.Kind;
            var r = e.Kind + e.V.accept(this);
            DeltaKind = null;
            return r;
        }

        public object Visit(FacetList e)
        {
            bool multi = e.List.Count > 1;
            StringBuilder sb = new StringBuilder();

            if (multi)
                sb.Append("(");
            bool first = true;
            foreach (var f in e.List)
            {
                if (first)
                    first = false;
                else
                {
                    sb.Append(", ");
                }
                sb.Append(f.accept(this));
            }
            if (multi)
                sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.BoundFacets e)
        {
            return e.FL.accept(this);
        }

        public object Visit(BoundOr e)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var B in e.List)
            {
                if (first)
                    first = false;
                else
                    sb.Append("⊔");
                sb.Append(brack(e, B));
            }
            return sb.ToString();
        }

        public object Visit(BoundAnd e)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var B in e.List)
            {
                if (first)
                    first = false;
                else
                    sb.Append("⊓");
                sb.Append(brack(e, B));
            }
            return sb.ToString();
        }

        public object Visit(BoundNot e)
        {
            return "￢" + brack(e, e.B);
        }

        public object Visit(BoundVal e)
        {
            return e.Kind + e.V.accept(this);
        }

        public object Visit(CogniPy.CNL.DL.TotalBound e)
        {
            using (attributeName.set(null))
                return "≤⊔≥" + e.V.accept(this);
        }

        public object Visit(DTBound e)
        {
            signature.Add(Tuple.Create(EntityKind.DataType, e.name));
            if (attributeName.get() != null)
                dataValues.Add(Tuple.Create("", attributeName.get(), "\r" + e.name));
            return "≤⊔≥" + e.name;
        }

        public object Visit(CogniPy.CNL.DL.TopBound e)
        {
            return "⊤";
        }

        public object Visit(CogniPy.CNL.DL.ValueSet e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("{");
            bool first = true;
            foreach (Value n in e.Values)
            {
                if (first)
                    first = false;
                else
                    sb.Append(",");
                sb.Append(n.accept(this));
            }
            sb.Append("}");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.Atomic e)
        {
            signature.Add(Tuple.Create(isKindOf.get(), e.id));
            return e.id;
        }
        public object Visit(CogniPy.CNL.DL.Top e)
        {
            return "⊤";
        }
        public object Visit(CogniPy.CNL.DL.Bottom e)
        {
            return "⊥";
        }
        public object Visit(CogniPy.CNL.DL.RoleInversion e)
        {
            using (isKindOf.set(EntityKind.Role))
            {
                return brack(e, e.R) + "⁻";
            }
        }
        public object Visit(CogniPy.CNL.DL.InstanceSet e)
        {
            using (isKindOf.set(EntityKind.Instance))
            {
                StringBuilder sb = new StringBuilder();
                sb.Append("{");
                bool first = true;
                foreach (var I in e.Instances)
                {
                    if (first)
                        first = false;
                    else
                        sb.Append(",");
                    sb.Append(I.accept(this));
                }
                sb.Append("}");
                return sb.ToString();
            }
        }
        public object Visit(CogniPy.CNL.DL.ConceptOr e)
        {
            using (isKindOf.set(EntityKind.Concept))
            {
                StringBuilder sb = new StringBuilder();
                bool first = true;
                foreach (var C in e.Exprs)
                {
                    if (first)
                        first = false;
                    else
                        sb.Append("⊔");
                    sb.Append(brack(e, C));
                }
                return sb.ToString();
            }
        }
        public object Visit(CogniPy.CNL.DL.ConceptAnd e)
        {
            using (isKindOf.set(EntityKind.Concept))
            {
                StringBuilder sb = new StringBuilder();
                bool first = true;
                foreach (var C in e.Exprs)
                {
                    if (first)
                        first = false;
                    else
                        sb.Append("⊓");
                    sb.Append(brack(e, C));
                }
                return sb.ToString();
            }
        }
        public object Visit(CogniPy.CNL.DL.ConceptNot e)
        {
            using (isKindOf.set(EntityKind.Concept))
            {
                return "￢" + brack(e, e.C);
            }
        }
        public object Visit(CogniPy.CNL.DL.OnlyRestriction e)
        {
            StringBuilder sb = new StringBuilder();
            using (isKindOf.set(EntityKind.Role))
            {
                sb.Append("∀" + brack(e, e.R) + ".");
            }
            using (isKindOf.set(EntityKind.Concept))
            {
                sb.Append(brack(e, e.C));
            }
            return sb.ToString();
        }
        public object Visit(CogniPy.CNL.DL.SomeRestriction e)
        {
            StringBuilder sb = new StringBuilder();
            using (isKindOf.set(EntityKind.Role))
            {
                sb.Append("∃" + brack(e, e.R) + ".");
            }
            using (isKindOf.set(EntityKind.Concept))
            {
                sb.Append(brack(e, e.C));
            }
            return sb.ToString();
        }
        public object Visit(CogniPy.CNL.DL.OnlyValueRestriction e)
        {
            StringBuilder sb = new StringBuilder();
            string attrName;
            using (isKindOf.set(EntityKind.DataRole))
            {
                attrName = (string)e.R.accept(this);
                sb.Append("∀" + brack(e, e.R));
            }
            using (attributeName.set(attrName))
                sb.Append(e.B.accept(this));
            return sb.ToString();
        }
        public object Visit(CogniPy.CNL.DL.SomeValueRestriction e)
        {
            StringBuilder sb = new StringBuilder();
            string attrName;
            using (isKindOf.set(EntityKind.DataRole))
            {
                attrName = (string)e.R.accept(this);
                sb.Append("∃" + brack(e, e.R));
            }
            using (attributeName.set(attrName))
                sb.Append(e.B.accept(this));
            return sb.ToString();
        }
        public object Visit(CogniPy.CNL.DL.SelfReference e)
        {
            using (isKindOf.set(EntityKind.Role))
            {
                return "∃" + brack(e, e.R) + ".○";
            }
        }
        public object Visit(CogniPy.CNL.DL.NumberRestriction e)
        {
            StringBuilder sb = new StringBuilder();
            using (isKindOf.set(EntityKind.Role))
            {
                sb.Append(e.Kind + e.N.ToString() + " " + brack(e, e.R) + ".");
            }
            using (isKindOf.set(EntityKind.Concept))
            {
                sb.Append(brack(e, e.C));
            }
            return sb.ToString();
        }
        public object Visit(CogniPy.CNL.DL.NumberValueRestriction e)
        {
            StringBuilder sb = new StringBuilder();
            string attrName;
            using (isKindOf.set(EntityKind.DataRole))
            {
                attrName = (string)e.R.accept(this);
                sb.Append(e.Kind + e.N.ToString() + " " + brack(e, e.R));
            }
            using (attributeName.set(attrName))
                sb.Append(e.B.accept(this));
            return sb.ToString();
        }


        public object Visit(Annotation a)
        {
            return a.txt;
        }

        public object Visit(DLAnnotationAxiom a)
        {
            signature.Add(Tuple.Create(EntityKind.Annotation, a.annotName));
            CogniPy.ARS.EntityKind result = CogniPy.CNL.AnnotationManager.ParseSubjectKind(a.subjKind);
            if (result != EntityKind.Statement)
                signature.Add(Tuple.Create(result, a.subject));
            // in case the annotation is a statement, the subject is not added to the signature. We could do this but we would need to parse the statement and it could be time consuming.
            // TODO [Annotations on Statements] is it needed to add to the signature the entities that are inside the statement?

            string val = a.value;
            // if the subject is null we are probably dealing with a non-standard rdf --> return null
            if (System.String.IsNullOrWhiteSpace(a.subject) || System.String.IsNullOrWhiteSpace(a.subjKind))
                return null;
            string subj = a.subject.Trim();
            if (result == EntityKind.Statement)
            {
                // !! at this point the statement subject is expected NOT inside quotes!! If it is inside quotes, somewhere it was incorrectly constructed.
                subj = "\"" + subj.Replace("\"", "''") + "\"";
            }


            string kind = a.subjKind.Trim();
            string annotName = a.annotName.Trim();
            string lang = "";
            if (!System.String.IsNullOrWhiteSpace(a.language))
                lang = a.language.Trim();

            if (!System.String.IsNullOrWhiteSpace(val))
            {
                if (!val.StartsWith("'"))
                {
                    val = "'" + val;
                }
                if (!val.EndsWith("'"))
                {
                    val += "'";
                }
            }
            else
                val += "''";

            var retur = "# " + subj + " " + kind + " " + annotName + " " + lang + " " + val;
            return retur;
        }

        public object Visit(CogniPy.CNL.DL.SwrlStatement e)
        {
            swrlAttrsBody = new HashSet<string>();
            swrlAttrsHead = new HashSet<string>();
            StringBuilder sb = new StringBuilder();
            sb.Append("⌂");
            using (swrlCurAttr.set(swrlAttrsBody))
                sb.Append(e.slp.accept(this));
            sb.Append(" ");
            sb.Append(Modality(e.modality));
            sb.Append(" →");
            using (swrlCurAttr.set(swrlAttrsHead))
                sb.Append(e.slc.accept(this));
            foreach (var b in swrlAttrsBody)
            {
                if (!dependentAttrs.ContainsKey(b))
                    dependentAttrs.Add(b, new HashSet<string>());
                dependentAttrs[b].UnionWith(swrlAttrsHead);
            }
            return sb.ToString();
        }

        public object Visit(SwrlIterate e)
        {
            swrlAttrsBody = new HashSet<string>();
            swrlAttrsHead = new HashSet<string>();
            StringBuilder sb = new StringBuilder();
            sb.Append("⌂");
            using (swrlCurAttr.set(swrlAttrsBody))
                sb.Append(e.slp.accept(this));
            sb.Append("→→");
            using (swrlCurAttr.set(swrlAttrsHead))
                sb.Append(e.slc.accept(this));

            sb.Append("(");
            using (swrlCurAttr.set(swrlAttrsHead))
                sb.Append(e.vars.accept(this));
            sb.Append(")");

            foreach (var b in swrlAttrsBody)
            {
                if (!dependentAttrs.ContainsKey(b))
                    dependentAttrs.Add(b, new HashSet<string>());
                dependentAttrs[b].UnionWith(swrlAttrsHead);
            }
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SwrlItemList e)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var I in e.list)
            {
                if (first) first = false;
                else sb.Append("⋀");
                sb.Append(I.accept(this));
            }
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SwrlInstance e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("○");
            using (isKindOf.set(EntityKind.Concept))
                sb.Append(e.C.accept(this));
            sb.Append("(");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.I.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SwrlRole e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.R);
            signature.Add(Tuple.Create(EntityKind.Role, e.R));
            sb.Append("(");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.I.accept(this));
            sb.Append(",");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.J.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SwrlSameAs e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("=(");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.I.accept(this));
            sb.Append(",");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.J.accept(this));
            sb.Append(")");
            return sb.ToString();
        }


        public object Visit(CogniPy.CNL.DL.SwrlDifferentFrom e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("≠(");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.I.accept(this));
            sb.Append(",");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.J.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SwrlDataProperty e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("°");
            var attrName = e.R;
            swrlCurAttr.get().Add(attrName);
            signature.Add(Tuple.Create(EntityKind.DataRole, attrName));
            sb.Append(attrName);
            sb.Append("(");
            using (isKindOf.set(EntityKind.Instance))
                sb.Append(e.IO.accept(this));
            sb.Append(",");
            using (attributeName.set(attrName))
                sb.Append(e.DO.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CogniPy.CNL.DL.SwrlDataRange e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("°");
            sb.Append(e.B.accept(this));
            sb.Append("(");
            sb.Append(e.DO.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(SwrlBuiltIn e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append("° ");
            sb.Append(":");
            sb.Append(e.builtInName);
            sb.Append(" ");
            sb.Append("(");
            for (int i = 0; i < e.Values.Count; i++)
            {
                if (i > 0)
                    sb.Append(" , ");
                sb.Append(e.Values[i].accept(this));
            }
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(ExeStatement e)
        {
            swrlAttrsBody = new HashSet<string>();
            swrlAttrsHead = new HashSet<string>();
            StringBuilder sb = new StringBuilder();
            sb.Append("⌂");
            using (swrlCurAttr.set(swrlAttrsBody))
                sb.Append(e.slp.accept(this));
            sb.Append("~→");
            sb.Append(e.exe);
            sb.Append("(");
            using (swrlCurAttr.set(swrlAttrsHead))
                sb.Append(e.args.accept(this));
            sb.Append(")");
            return sb.ToString();
        }

        public object Visit(CodeStatement e)
        {
            StringBuilder sb = new StringBuilder();
            sb.Append(e.exe);
            return sb.ToString();
        }

        public virtual object Visit(SwrlVarList e)
        {
            StringBuilder sb = new StringBuilder();
            bool first = true;
            foreach (var I in e.list)
            {
                if (first) first = false;
                else sb.Append(", ");
                sb.Append(I.accept(this));
            }
            return sb.ToString();
        }

        public object Visit(SwrlDVal e)
        {
            return e.Val.accept(this).ToString();
        }

        public virtual object Visit(SwrlDVar e)
        {
            return "?:" + e.VAR;
        }

        public object Visit(SwrlIVal e)
        {
            signature.Add(Tuple.Create(EntityKind.Instance, e.I));
            return e.I;
        }

        public virtual object Visit(SwrlIVar e)
        {
            return "?" + e.VAR;
        }




    }
}
