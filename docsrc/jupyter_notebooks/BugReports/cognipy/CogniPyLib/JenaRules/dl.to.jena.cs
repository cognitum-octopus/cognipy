using CogniPy.ARS;
using CogniPy.CNL.DL;
using org.semanticweb.owlapi.vocab;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;

namespace CogniPy.Executing.HermiT
{
    public abstract class JenaNode
    {
        DLToOWLNameConv owlNC = null;

        public string ToOwlName(string name, ARS.EntityKind whatFor)
        {
            return "<" + owlNC.getIRIFromId(name, whatFor).toString() + ">";
        }

        private string freeVarId;

        public JenaNode(DLToOWLNameConv owlNC, string freeVarId) { this.freeVarId = freeVarId; this.owlNC = owlNC; }
        public string GetFreeVariableId()
        {
            return freeVarId;
        }
        public abstract string ToJenaRule();

        public string ToCombinedBlock()
        {
            return ToJenaRule();
        }

        static Regex DtmRg = new Regex(@"(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?))?", RegexOptions.Compiled);
        static string completeDTMVal(string val)
        {
            var m = DtmRg.Match(val);
            var dta = m.Groups["date"].Value;
            var tm = m.Groups["time"].Value;
            StringBuilder sb = new StringBuilder();
            sb.Append(dta);
            if (string.IsNullOrEmpty(tm))
                sb.Append("T00:00:00");
            else
                sb.Append(tm);
            if (tm.Length == "T00:00".Length)
                sb.Append(":00");
            return sb.ToString();
        }

        static string escapeString(string str)
        {
            StringBuilder ret = new StringBuilder();
            ret.Append(@"""");
            foreach (var c in str)
            {
                switch (c)
                {
                    case '\t': ret.Append(@"\t"); break;
                    case '\n': ret.Append(@"\n"); break;
                    case '\r': ret.Append(@"\r"); break;
                    case (char)0x0008: ret.Append(@"\b"); break;
                    case (char)0x000C: ret.Append(@"\f"); break;
                    case '\"': ret.Append(@"\"""); break;
                    case '\'': ret.Append(@"\'"); break;
                    case '\\': ret.Append(@"\\"); break;
                    default: ret.Append(c); break;
                }
            }
            ret.Append(@"""");
            return ret.ToString();
        }

        static string unescapeString(string str)
        {
            StringBuilder ret = new StringBuilder();
            bool wasBS = false;
            foreach (var c in str)
            {
                if (wasBS)
                {
                    switch (c)
                    {
                        case 't': ret.Append('\t'); break;
                        case 'n': ret.Append('\n'); break;
                        case 'r': ret.Append('\r'); break;
                        case 'b': ret.Append((char)0x0008); break;
                        case 'f': ret.Append((char)0x000C); break;
                        case '\"': ret.Append('\"'); break;
                        case '\'': ret.Append('\''); break;
                        case '\\': ret.Append('\\'); break;
                        default: ret.Append(c); break;
                    }
                    wasBS = false;
                }
                else
                {
                    if (c == '\\')
                        wasBS = true;
                    else if (c != '\"')
                        ret.Append(c);
                }
            }
            return ret.ToString();
        }

        public static string GetLiteralVal(CogniPy.CNL.DL.Value v)
        {
            if (v is CNL.DL.Bool) return escapeString(v.ToBool() ? "true" : "false") + "^^xsd:boolean";
            if (v is CNL.DL.String) return escapeString(v.ToString()) + "^^xsd:string";
            if (v is CNL.DL.Float) return escapeString(v.ToStringExact()) + "^^xsd:double";
            if (v is CNL.DL.Number) return escapeString(v.ToStringExact()) + "^^xsd:integer";
            if (v is CNL.DL.DateTimeVal) return escapeString(completeDTMVal(v.ToStringExact())) + "^^xsd:dateTime";
            if (v is CNL.DL.Duration) return escapeString(v.ToStringExact()) + "^^xsd:duration";

            return escapeString(v.ToString());
        }

        static System.Globalization.CultureInfo en_cult = new System.Globalization.CultureInfo("en-US");

        static public object ToTypedValue(string uri)
        {
            var ttpos = uri.LastIndexOf('^');
            string ttag;
            string str;
            if (ttpos <= 0)
            {
                ttag = "string";
                str = unescapeString(uri);
            }
            else
            {
                var ttxpos = uri.IndexOf('^');
                str = unescapeString(uri.Substring(0, ttxpos));
                ttag = uri.Substring(ttpos + 1).ToLower();
            }
            if (ttag.EndsWith(">"))
                ttag = ttag.Substring(1, ttag.Length - 2);
            if (ttag.EndsWith("boolean"))
                return (str.CompareTo("true") == 0);
            else if (ttag.EndsWith("string"))
                return str;
            else if (ttag.EndsWith("double"))
                return double.Parse(str, en_cult);
            else if (ttag.EndsWith("integer") || ttag.EndsWith("int"))
                return int.Parse(str);
            else if (ttag.EndsWith("dateTime") || ttag.EndsWith("datetime"))
                return DateTimeOffset.Parse(str);
            else
                return null;
        }
    }

    public class JenaInstanceOfDefinedClass : JenaNode
    {
        string clsname;
        string emptyVarId;
        public JenaInstanceOfDefinedClass(DLToOWLNameConv owlNC, string freeVarId, string clsname, string emptyVarId)
            : base(owlNC, freeVarId)
        {
            this.clsname = clsname;
            this.emptyVarId = emptyVarId;
        }

        public override string ToJenaRule()
        {
            return "( " + GetFreeVariableId() + " rdf:type " + ((clsname == null) ? "<http://www.w3.org/2002/07/owl#NamedIndividual>" : ToOwlName(clsname, ARS.EntityKind.Concept)) + " ) ";
        }
    }

    public class JenaTop : JenaNode
    {
        public JenaTop(DLToOWLNameConv owlNC, string freeVarId)
            : base(owlNC, freeVarId)
        {
        }

        public override string ToJenaRule()
        {
            return "( " + GetFreeVariableId() + " rdf:type <http://www.w3.org/2002/07/owl#NamedIndividual> )";
        }
    }

    public class JenaTotal : JenaNode
    {
        string dt;
        public JenaTotal(DLToOWLNameConv owlNC, string freeVarId, string dt)
            : base(owlNC, freeVarId)
        {
            this.dt = dt;
        }

        public override string ToJenaRule()
        {
            return "isDType(" + GetFreeVariableId() + "," + dt + ")";
        }
    }

    public class JenaRelatedToValueFilter : JenaNode
    {
        CNL.DL.Value value;
        string attribute;
        string varVarId;
        string bound;
        public JenaRelatedToValueFilter(DLToOWLNameConv owlNC, string freeVarId, string attribute, string bound, string varVarId, CNL.DL.Value value)
            : base(owlNC, freeVarId)
        {
            this.attribute = attribute;
            this.value = value;
            this.varVarId = varVarId;
            this.bound = bound;
        }
        public override string ToJenaRule()
        {
            if (bound == "=")
                return "( " + GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + GetLiteralVal(value) + " )";
            else
                return "( " + GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + varVarId + " )";
        }
    }

    public class JenaRelatedToVariable : JenaNode
    {
        string variableId;
        string role;
        bool isInversed;
        bool useDistinct;
        public JenaRelatedToVariable(DLToOWLNameConv owlNC, string freeVarId, string variableId, string role, bool isInversed, bool useDistinct = false)
            : base(owlNC, freeVarId)
        {
            this.variableId = variableId;
            this.role = role;
            this.isInversed = isInversed;
            this.useDistinct = useDistinct;
        }
        public override string ToJenaRule()
        {
            if (!isInversed)
                return "( " + GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + variableId + " )";
            else
                return "( " + variableId + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId() + " )";
        }
    }

    public class JenaRelatedToInstance : JenaNode
    {
        string instName;
        string role;
        bool isInversed;
        public JenaRelatedToInstance(DLToOWLNameConv owlNC, string freeVarId, string instName, string role, bool isInversed)
            : base(owlNC, freeVarId)
        {
            this.instName = instName;
            this.role = role;
            this.isInversed = isInversed;
        }
        public override string ToJenaRule()
        {
            if (!isInversed)
                return "( " + GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + ToOwlName(instName, ARS.EntityKind.Instance) + " )";
            else
                return "( " + ToOwlName(instName, ARS.EntityKind.Instance) + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId() + " )";
        }
    }

    public class JenaAnd : JenaNode
    {
        List<JenaNode> nodes;
        public JenaAnd(DLToOWLNameConv owlNC, string freeVarId, List<JenaNode> nodes)
            : base(owlNC, freeVarId)
        {
            this.nodes = nodes;
        }
        public override string ToJenaRule()
        {
            return string.Join(",", (from n in nodes select n.ToJenaRule()));
        }
    }

    public class TransformToJenaRules : CogniPy.CNL.DL.GenericVisitor
    {

        DLToOWLNameConv owlNC;

        public Dictionary<string, string> InvUriMappings { get { return owlNC.InvUriMappings; } set { owlNC.InvUriMappings = value; } }

        public void setOWLDataFactory(DLToOWLNameConv owlNC)
        {
            this.owlNC = owlNC;
        }

        public string ToOwlName(string name, ARS.EntityKind whatFor)
        {
            return "<" + owlNC.getIRIFromId(name, whatFor).toString() + ">";
        }

        public DlName ToDL(string uri, ARS.EntityKind makeFor)
        {
            return owlNC.ToDL(uri, makeFor);
        }

        int freeVarIdBase = 0;
        VisitingParam<string> activeFreeVarId = new VisitingParam<string>(null);
        public string newFreeVarId()
        {
            return "?x" + freeVarIdBase++.ToString();
        }

        public string ConvertToGetInstancesOf(CNL.DL.Node n)
        {
            freeVarIdBase = 0;
            string selectVars;
            string whereBlock;
            string lhs;

            using (activeFreeVarId.set(newFreeVarId()))
            {
                var sparqlNode = n.accept(this) as JenaNode;
                lhs = sparqlNode.GetFreeVariableId();
                selectVars = sparqlNode.GetFreeVariableId();
                whereBlock = sparqlNode.ToCombinedBlock();
            }
            return whereBlock;
        }

        public override object Visit(Top e)
        {
            if (isKindOf.get() == "C")
            {
                return new JenaInstanceOfDefinedClass(owlNC, activeFreeVarId.get(), null, newFreeVarId());
            }
            return null;
        }

        public override object Visit(Atomic e)
        {
            if (isKindOf.get() == "C")
            {
                return new JenaInstanceOfDefinedClass(owlNC, activeFreeVarId.get(), e.id, null);
            }
            if (isKindOf.get() == "R")
                return Tuple.Create(false, e.id);
            else
                return e.id;
        }

        public override object Visit(RoleInversion e)
        {
            using (isKindOf.set("R"))
            {
                var r = e.R.accept(this) as Tuple<bool, string>;
                return Tuple.Create(!r.Item1, r.Item2);
            }
        }

        public override object Visit(SomeRestriction e)
        {
            Tuple<bool, string> r;
            using (isKindOf.set("R"))
                r = e.R.accept(this) as Tuple<bool, string>;

            if (e.C is InstanceSet)
            {
                var instSet = e.C as InstanceSet;
                if (instSet.Instances.Count == 1)
                {
                    return new JenaRelatedToInstance(owlNC, activeFreeVarId.get(), (instSet.Instances.First() as NamedInstance).name, r.Item2, r.Item1);
                }
                else
                {
                    return null;
                }
            }
            else if (e.C is Top)
            {
                return new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), newFreeVarId(), r.Item2, r.Item1, true);
            }
            else
            {
                JenaNode c;
                using (activeFreeVarId.set(newFreeVarId()))
                    c = e.C.accept(this) as JenaNode;

                var d = new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), c.GetFreeVariableId(), r.Item2, r.Item1);

                return new JenaAnd(owlNC, activeFreeVarId.get(), new List<JenaNode>() { c, d });
            }
        }

        public override object Visit(SelfReference e)
        {
            Tuple<bool, string> r;
            using (isKindOf.set("R"))
                r = e.R.accept(this) as Tuple<bool, string>;

            return new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), activeFreeVarId.get(), r.Item2, r.Item1);
        }

        public override object Visit(BoundAnd e)
        {
            var nodes = (from x in e.List
                         select (x.accept(this) as JenaNode)).ToList();

            return new JenaAnd(owlNC, activeFreeVarId.get(), nodes);
        }

        public override object Visit(CogniPy.CNL.DL.BoundFacets e)
        {
            var nodes = (from x in e.FL.List
                         select new JenaRelatedToValueFilter(owlNC, activeFreeVarId.get(),
                             activeAttribute.get(), x.Kind, x.Kind == "=" ? null : newFreeVarId(), x.V)).ToList<JenaNode>();

            if (nodes.Count == 1)
                return nodes.First();
            else
                return new JenaAnd(owlNC, activeFreeVarId.get(), nodes);
        }

        public override object Visit(CogniPy.CNL.DL.FacetList e)
        {
            var r = new List<Tuple<string, CNL.DL.Value>>();
            foreach (var F in e.List)
                r.Add(Tuple.Create(F.Kind, F.V));
            return r;
        }

        public override object Visit(BoundVal e)
        {
            return new JenaRelatedToValueFilter(owlNC, activeFreeVarId.get(),
                activeAttribute.get(), e.Kind, e.Kind == "=" ? null : newFreeVarId(), e.V);
        }

        VisitingParam<string> activeAttribute = new VisitingParam<string>(null);

        public override object Visit(SomeValueRestriction e)
        {
            string d;
            using (isKindOf.set("D"))
                d = e.R.accept(this) as string;
            using (activeAttribute.set(d))
            {
                return e.B.accept(this);
            }
        }

        public override object Visit(TopBound e)
        {
            return new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), newFreeVarId(), activeAttribute.get(), false, true);
        }

        string getLiteralDatatypeString(Value v)
        {
            if (v is CNL.DL.Bool)
                return OWL2Datatype.XSD_BOOLEAN.getIRI().toString();
            else if (v is CNL.DL.String)
                return OWL2Datatype.XSD_STRING.getIRI().toString();
            else if (v is CNL.DL.Float)
                return OWL2Datatype.XSD_DOUBLE.getIRI().toString();
            else if (v is CNL.DL.Number)
                return OWL2Datatype.XSD_INT.getIRI().toString();
            else if (v is CNL.DL.DateTimeVal)
                return OWL2Datatype.XSD_DATE_TIME.getIRI().toString();
            else if (v is CNL.DL.Duration)
                return "http://www.w3.org/2001/XMLSchema#dayTimeDuration";
            else
                return OWL2Datatype.RDFS_LITERAL.getIRI().toString();
        }
        public override object Visit(TotalBound e)
        {
            var nv = newFreeVarId();
            return new JenaAnd(owlNC, activeFreeVarId.get(), new List<JenaNode>() {
                new JenaRelatedToVariable(owlNC, activeFreeVarId.get(), nv, activeAttribute.get(), false, true),
                new JenaTotal(owlNC, nv, getLiteralDatatypeString(e.V))});
        }

        public override object Visit(ConceptAnd e)
        {
            using (isKindOf.set("C"))
            {
                List<JenaNode> nodes = new List<JenaNode>();
                foreach (var expr in e.Exprs)
                {
                    nodes.Add(expr.accept(this) as JenaNode);
                }
                return new JenaAnd(owlNC, activeFreeVarId.get(), nodes);
            }
        }

    }
}
