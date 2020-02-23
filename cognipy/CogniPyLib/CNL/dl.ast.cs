using System.Collections.Generic;
using Tools;
using System;
using System.Diagnostics;

namespace Ontorion.CNL.DL
{
    public interface IAccept
    {
        object accept(IVisitor v);
    }

    public enum StatementType {Rule,Role,Concept,Instance,Annotation}

    public class StatementAttr : Attribute
    {
        public StatementType type {get; private set;}
        public StatementAttr(StatementType type)
        {
            this.type = type;
        }
    }

    public partial class PartialSymbol : TOKEN
    {
        public PartialSymbol(Parser yyp) : base(yyp) { }

        public override string yyname
        {
            get
            {
                return (string)this.GetType().GetProperty("yyname_" + this.yyps.GetType().Name).GetValue(this, new object[] { });
            }
        }
        public override int yynum
        {
            get
            {
                return (int)this.GetType().GetProperty("yynum_" + this.yyps.GetType().Name).GetValue(this, new object[] { });
            }
        }

        public virtual string yyname_dl { get { return null; } }
        public virtual int yynum_dl { get { return 0; } }
    }

    public partial class Statement : IAccept
    {
        public enum Modality { IS, MUST, SHOULD, CAN, MUSTNOT, SHOULDNOT, CANNOT }
        public Modality modality;
        public virtual object accept(IVisitor v) { return null; }
        public Statement(Parser yyp) : base(yyp) { }
    }

    public partial class Paragraph : IAccept
    {
        public Paragraph(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Statement> Statements;
        public Paragraph(Parser yyp, Statement S)
            : base(yyp)
        { Statements = new System.Collections.Generic.List<Statement>(); Statements.Add(S); }
        public Paragraph(Parser yyp, Paragraph tu, Statement S)
            : base(yyp)
        { Statements = tu.Statements; Statements.Add(S); }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class Node : IAccept
    {
        public virtual object accept(IVisitor v) { return null; }
        public virtual int priority() { return 0; }
        public Node me()
        {
            return this is Identity ? (this as Identity).C : this;
        }
        public Node(Parser yyp) : base(yyp) { }
    }

    public partial class Expression : Node
    {
        public Expression(Parser yyp) : base(yyp) { }
    }

    public partial class modality
    {
        public Statement.Modality mod;
        public modality(Parser yyp) : base(yyp) { }
    }

    public partial class CmpOrID :IAccept
    {
        public CmpOrID(Parser yyp) : base(yyp) { }

        public object accept(IVisitor v)
        {
            return null;
        }
    }

    [StatementAttr(StatementType.Annotation)]
    public partial class DLAnnotationAxiom : Statement
    {
        public DLAnnotationAxiom(Parser yyp) : base(yyp) { }
        public DLAnnotationAxiom(Parser yyp, string subject,string subjectKind, string annotName, string language, string value)
            : base(yyp)
        {
            this.subject = subject; this.annotName = annotName; this.language = language; this.value = value; this.subjKind = subjectKind;
        }
        public DLAnnotationAxiom(Parser yyp, string subject, string subjectKind, string annotName, string value)
            : base(yyp)
        {
            this.subject = subject; this.annotName = annotName; this.language = ""; this.value = value; this.subjKind = subjectKind;
        }

        string _subject;
        public string subject
        {
            get 
            {
                if (_subjKind == Ontorion.ARS.EntityKind.Statement && _subject.StartsWith("\"") && _subject.EndsWith("\"") && !System.String.IsNullOrWhiteSpace(_subject))
                    return _subject.Substring(1, _subject.Length - 2);
                else
                    return _subject;
            }
            set { _subject = value; }
        }

        Ontorion.ARS.EntityKind _subjKind;
        public string subjKind
        {
            get
            {
                return _subjKind.ToString();
            }
            set
            {
                _subjKind = Ontorion.CNL.AnnotationManager.ParseSubjectKind(value);
            }
        }

        public string annotName;
        public string value;
        public string language;
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Annotation)]
    public partial class Annotation : Statement
    {
        public Annotation(Parser yyp) : base(yyp) { }
        public string txt;
        public Annotation(Parser yyp, string txt)
            : base(yyp)
        {
            this.txt = txt;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Concept)]
    public partial class Subsumption : Statement
    {
        public Subsumption(Parser yyp) : base(yyp) { }
        public Node C;
        public Node D;
        public Subsumption(Parser yyp, Node c, Node d, Modality m)
            : base(yyp)
        {
            C = c.me(); D = d.me(); modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Concept)]
    public partial class Equivalence : Statement
    {
        public Equivalence(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Equivalents;
        public Equivalence(Parser yyp, Node c, Node d, Modality m)
            : base(yyp)
        {
            Equivalents = new System.Collections.Generic.List<Node>();
            Equivalents.Add(c.me());
            Equivalents.Add(d.me()); modality = m;
        }
        public Equivalence(Parser yyp, NodeList il, Modality m)
            : base(yyp)
        {
            Equivalents = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Equivalents.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Concept)]
    public partial class Disjoint : Statement
    {
        public Disjoint(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Disjoints;
        public Disjoint(Parser yyp, Node c, Node d, Modality m)
            : base(yyp)
        {
            Disjoints = new System.Collections.Generic.List<Node>();
            Disjoints.Add(c.me()); Disjoints.Add(d.me());
            modality = m;
        }
        public Disjoint(Parser yyp, NodeList il, Modality m)
            : base(yyp)
        {
            Disjoints = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Disjoints.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Concept)]
    public partial class DisjointUnion : Statement
    {
        public DisjointUnion(Parser yyp) : base(yyp) { }
        public string name;
        public System.Collections.Generic.List<Node> Union;
        public DisjointUnion(Parser yyp, ID i, NodeList il, Modality m)
            : base(yyp)
        {
            name = i.yytext;
            Union = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Union.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class DataTypeDefinition : Statement
    {
        public DataTypeDefinition(Parser yyp) : base(yyp) { }
        public string name;
        public AbstractBound B;
        public DataTypeDefinition(Parser yyp, ID i, AbstractBound B)
            : base(yyp)
        {
            name = i.yytext;
            this.B = B.me();
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class RoleInclusion : Statement
    {
        public RoleInclusion(Parser yyp) : base(yyp) { }
        public Node C;
        public Node D;
        public RoleInclusion(Parser yyp, Node c, Node d, Modality m)
            : base(yyp)
        { C = c.me(); D = d.me(); modality = m; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class RoleEquivalence : Statement
    {
        public RoleEquivalence(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Equivalents;
        public RoleEquivalence(Parser yyp, Node c, Node d, Modality m)
            : base(yyp)
        {
            Equivalents = new System.Collections.Generic.List<Node>();
            Equivalents.Add(c.me());
            Equivalents.Add(d.me()); modality = m;
        }
        public RoleEquivalence(Parser yyp, NodeList il, Modality m)
            : base(yyp)
        {
            Equivalents = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Equivalents.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class RoleDisjoint : Statement
    {
        public RoleDisjoint(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Disjoints;
        public RoleDisjoint(Parser yyp, Node c, Node d, Modality m)
            : base(yyp)
        {
            Disjoints = new System.Collections.Generic.List<Node>(); Disjoints.Add(c.me()); Disjoints.Add(d.me()); modality = m;
        }
        public RoleDisjoint(Parser yyp, NodeList il, Modality m)
            : base(yyp)
        {
            Disjoints = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Disjoints.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class ComplexRoleInclusion : Statement
    {
        public ComplexRoleInclusion(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> RoleChain;
        public Node R;
        public ComplexRoleInclusion(Parser yyp, RoleChain cn, Node r, Modality m) : base(yyp) { RoleChain = cn.List; R = r.me(); modality = m; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class DataRoleInclusion : Statement
    {
        public DataRoleInclusion(Parser yyp) : base(yyp) { }
        public Node C;
        public Node D;
        public DataRoleInclusion(Parser yyp, Node c, Node d, Modality m) : base(yyp) { C = c.me(); D = d.me(); modality = m; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class DataRoleEquivalence : Statement
    {
        public DataRoleEquivalence(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Equivalents;
        public DataRoleEquivalence(Parser yyp, Node c, Node d, Modality m) : base(yyp) { Equivalents = new System.Collections.Generic.List<Node>(); Equivalents.Add(c.me()); Equivalents.Add(d.me()); modality = m; }
        public DataRoleEquivalence(Parser yyp, NodeList il, Modality m)
            : base(yyp)
        {
            Equivalents = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Equivalents.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Role)]
    public partial class DataRoleDisjoint : Statement
    {
        public DataRoleDisjoint(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Disjoints;
        public DataRoleDisjoint(Parser yyp, Node c, Node d, Modality m) : base(yyp) { Disjoints = new System.Collections.Generic.List<Node>(); Disjoints.Add(c.me()); Disjoints.Add(d.me()); modality = m; }
        public DataRoleDisjoint(Parser yyp, NodeList il, Modality m)
            : base(yyp)
        {
            Disjoints = new System.Collections.Generic.List<Node>();
            foreach (var e in il.List)
                Disjoints.Add(e.me());
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class Instance : IAccept
    {
        public Instance(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }
    public partial class NamedInstance : Instance
    {
        public NamedInstance(Parser yyp) : base(yyp) { }
        public string name;
        public NamedInstance(Parser yyp, ID i) : base(yyp) { name = i.yytext; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string ToString()
        {
            return name;
        }
    }
    public partial class UnnamedInstance : Instance
    {
        public UnnamedInstance(Parser yyp) : base(yyp) { }
        public Node C;
        public bool Only;
        public UnnamedInstance(Parser yyp, bool only, Node c) : base(yyp) { C = c.me(); Only = only; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string ToString()
        {
            throw new InvalidOperationException();
        }
    }

    [StatementAttr(StatementType.Instance)]
    public partial class InstanceOf : Statement
    {
        public InstanceOf(Parser yyp) : base(yyp) { }
        public Node C;
        public Instance I;
        public InstanceOf(Parser yyp, Node c, ID i, Modality m) : base(yyp) { C = c.me(); I = new NamedInstance(null) { name = i.yytext }; modality = m; }
        public InstanceOf(Parser yyp, Node c, Modality m) : base(yyp) { C = c.me(); I = new UnnamedInstance(null, false, new CNL.DL.Top(null)); modality = m; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Instance)]
    public partial class RelatedInstances : Statement
    {
        public RelatedInstances(Parser yyp) : base(yyp) { }
        public Node R;
        public Instance I;
        public Instance J;
        public RelatedInstances(Parser yyp, Node r, ID i, ID j, Modality m)
            : base(yyp)
        { R = r.me(); I = new NamedInstance(null) { name = i.yytext }; J = new NamedInstance(null) { name = j.yytext }; modality = m; }
     
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class Value : IAccept
    {

        public static Value FromObject(object obj)
        {
            if (obj.GetType() == typeof(int))
            {
                return new CNL.DL.Number(null) { val = obj.ToString() };
            }
            else if (obj.GetType() == typeof(string))
            {
                return new CNL.DL.String(null) { val = "'" + obj.ToString().Replace("'", "''") + "'" };
            }
            else if (obj.GetType() == typeof(double))
            {
                return new CNL.DL.Float(null) { val = obj.ToString() };
            }
            else if (obj.GetType() == typeof(bool))
            {
                return new CNL.DL.Bool(null) { val = ((bool)obj)?"[1]":"[0]" };
            }
            else if (obj.GetType() == typeof(DateTimeOffset))
            {
//                Debugger.Break(); // lets check if it can be serialized this way
                return new CNL.DL.DateTimeVal(null) { val = ((DateTimeOffset)obj).ToString("s") };
            }
            else if (obj.GetType() == typeof(TimeSpan))
            {
//                Debugger.Break(); // lets check if it can be serialized this way
                return new CNL.DL.Duration(null) { val = System.Xml.XmlConvert.ToString((TimeSpan)obj) };
            }
            throw new InvalidOperationException();
        }

        public static object ToObject(Value val)
        {
            if (val.GetType() == typeof(CNL.DL.Number))
            {
                return int.Parse(val.getVal());
            }
            else if (val.GetType() == typeof(CNL.DL.String))
            {
                var v = val.getVal();
                return v.Substring(1, v.Length - 2).Replace("''", "'");
            }
            else if (val.GetType() == typeof(CNL.DL.Float))
            {
                return double.Parse(val.getVal(), en_cult);
            }
            else if (val.GetType() == typeof(CNL.DL.Bool))
            {
                return val.getVal() == "[1]";
            }
            else if (val.GetType() == typeof(CNL.DL.DateTimeVal))
            {
                return DateTimeOffset.Parse(val.getVal());
            }
            else if (val.GetType() == typeof(CNL.DL.Duration))
            {
                return System.Xml.XmlConvert.ToTimeSpan(val.getVal());
            }
            throw new InvalidOperationException();
        }

        public static Value MakeFrom(string typeTag, string val)
        {
            switch (typeTag)
            {
                case "I":
                    return new CNL.DL.Number(null) { val = val };
                case "S":
                    return new CNL.DL.String(null) { val = val };
                case "F":
                    return new CNL.DL.Float(null) { val = val };
                case "B":
                    return new CNL.DL.Bool(null) { val = val };
                case "T":
                    return new CNL.DL.DateTimeVal(null) { val = val };
                case "D":
                    return new CNL.DL.Duration(null) { val = val };
                default:
                    throw new InvalidOperationException();
            }
        }

 

        public Value(Parser yyp) : base(yyp) { }

        public virtual object accept(IVisitor v) { return null; }
        public virtual string getVal() { return null; }
        public virtual string getTypeTag() { return null;}

        public string ToStringExact()
        {
            return getVal();
        }

        public override string ToString()
        {
            return getVal();
        }

        static System.Globalization.CultureInfo en_cult = new System.Globalization.CultureInfo("en-US");
        public double ToDouble()
        {
            return double.Parse(getVal(), en_cult.NumberFormat);
        }

        public int ToInt()
        {
            return int.Parse(getVal());
        }

        public bool ToBool()
        {
            return getVal() == "[1]";
        }
    }

    [StatementAttr(StatementType.Instance)]
    public partial class InstanceValue : Statement
    {
        public InstanceValue(Parser yyp) : base(yyp) { }
        public Node R;
        public Instance I;
        public Value V;
        public InstanceValue(Parser yyp, Node r, ID i, Value v, Modality m) : base(yyp) { R = r.me(); I = new NamedInstance(null) { name = i.yytext }; V = v; modality = m; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class InstanceList
    {
        public System.Collections.Generic.List<Instance> List;
        public InstanceList(Parser yyp) : base(yyp) { }
        public InstanceList(Parser yyp, Instance I)
            : base(yyp)
        { List = new System.Collections.Generic.List<Instance>(); List.Add(I); }
        public InstanceList(Parser yyp, InstanceList cl, Instance I)
            : base(yyp)
        { List = cl.List; List.Add(I); }
    }

    public partial class NodeList
    {
        public NodeList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> List;
        public NodeList(Parser yyp, Node I)
            : base(yyp)
        { List = new System.Collections.Generic.List<Node>(); List.Add(I); }
        public NodeList(Parser yyp, NodeList cl, Node I)
            : base(yyp)
        { List = cl.List; List.Add(I); }
    }

    [StatementAttr(StatementType.Instance)]
    public partial class SameInstances : Statement
    {
        public SameInstances(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Instance> Instances;
        public SameInstances(Parser yyp, ID I, ID J, Modality m)
            : base(yyp)
        {
            Instances = new System.Collections.Generic.List<Instance>();
            Instances.Add(new NamedInstance(yyp, I));
            Instances.Add(new NamedInstance(yyp, J));
            modality = m;
        }
        public SameInstances(Parser yyp, InstanceList il, Modality m)
            : base(yyp)
        {
            Instances = il.List;
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Instance)]
    public partial class DifferentInstances : Statement
    {
        public DifferentInstances(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Instance> Instances;
        public DifferentInstances(Parser yyp, ID I, ID J, Modality m)
            : base(yyp)
        {
            Instances = new System.Collections.Generic.List<Instance>();
            Instances.Add(new NamedInstance(yyp, I));
            Instances.Add(new NamedInstance(yyp, J));
            modality = m;
        }
        public DifferentInstances(Parser yyp, InstanceList il, Modality m)
            : base(yyp)
        {
            Instances = il.List;
            modality = m;
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    [StatementAttr(StatementType.Concept)]
    public partial class HasKey : Statement
    {
        public HasKey(Parser yyp) : base(yyp) { }
        public Node C;
        public System.Collections.Generic.List<Node> Roles;
        public System.Collections.Generic.List<Node> DataRoles;
        public HasKey(Parser yyp, Node c, NodeList roles, NodeList dataroles)
            : base(yyp)
        {
            C = c;
            Roles = (roles == null ? new System.Collections.Generic.List<Node>() : roles.List);
            DataRoles = (dataroles == null ? new System.Collections.Generic.List<Node>() : dataroles.List);
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class Number : Value
    {
        public Number(Parser yyp) : base(yyp) { }
        public string val;
        public Number(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
        public override string getTypeTag() { return "I"; }
    }
    public partial class String : Value
    {
        public String(Parser yyp) : base(yyp) { }
        public string val;
        public String(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
        public override string getTypeTag() { return "S"; }
        public override string ToString()
        {
            return getVal().Substring(1, getVal().Length - 2).Replace("\'\'", "\'");
        }
    }
    public partial class Float : Value
    {
        public Float(Parser yyp) : base(yyp) { }
        public string val;
        public Float(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
        public override string getTypeTag() { return "F"; }
    }
    public partial class Bool : Value
    {
        public Bool(Parser yyp) : base(yyp) { }
        public string val;
        public Bool(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
        public override string getTypeTag() { return "B"; }
    }
    public partial class DateTimeVal : Value
    {
        public DateTimeVal(Parser yyp) : base(yyp) { }
        public string val;
        public DateTimeVal(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
        public override string getTypeTag() { return "T"; }
    }
    public partial class Duration : Value
    {
        public Duration(Parser yyp) : base(yyp) { }
        public string val;
        public Duration(Parser yyp, string v) : base(yyp) { val = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override string getVal() { return val.ToString(); }
        public override string getTypeTag() { return "T"; }
    }
    public partial class ValueList
    {
        public ValueList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Value> List;
        public ValueList(Parser yyp, Value V)
            : base(yyp)
        { List = new System.Collections.Generic.List<Value>(); List.Add(V); }
        public ValueList(Parser yyp, ValueList cl, Value V)
            : base(yyp)
        { List = cl.List; List.Add(V); }
    }

    public partial class Facet : IAccept
    {
        public string Kind;
        public Value V;
        public Facet(Parser yyp) : base(yyp) { }
        public Facet(Parser yyp, string k, Value v) : base(yyp) { Kind = k; V = v; }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class FacetList : IAccept
    {
        public List<Facet> List;
        public FacetList(Parser yyp) : base(yyp) { }
        public FacetList(Parser yyp, Facet f) : base(yyp) { List = new List<Facet>() { f }; }
        public FacetList(Parser yyp, Facet f1, Facet f2) : base(yyp) { List = new List<Facet>() { f1, f2 }; }
        public FacetList(Parser yyp, FacetList l, Facet f) : base(yyp) { List = l.List; List.Add(f); }
        public virtual object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class AbstractBound : IAccept
    {
        public AbstractBound(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
        public virtual int priority() { return 0; }
        public AbstractBound me()
        {
            return this is IdentityBound ? (this as IdentityBound).B : this;
        }
    }

    public partial class BoundFacets : AbstractBound
    {
        public BoundFacets(Parser yyp) : base(yyp) { }
        public FacetList FL;
        public BoundFacets(Parser yyp, FacetList fl) : base(yyp) { FL = fl; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class IdentityBound : AbstractBound
    {
        public IdentityBound(Parser yyp) : base(yyp) { }
        public AbstractBound B;
        public IdentityBound(Parser yyp, AbstractBound B) : base(yyp) { this.B = B; }
        public override object accept(IVisitor v)
        {
            throw new InvalidOperationException();
        }
    }

    public partial class BoundNot : AbstractBound
    {
        public override int priority() { return 5; }
        public AbstractBound B;
        public BoundNot(Parser yyp) : base(yyp) { }
        public BoundNot(Parser yyp, AbstractBound B) : base(yyp) { this.B = B.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class BoundAnd : AbstractBound
    {
        public override int priority() { return 3; }
        public List<AbstractBound> List;
        public BoundAnd(Parser yyp) : base(yyp) { }
        public BoundAnd(Parser yyp, AbstractBound c, AbstractBound d) : base(yyp) 
        {
            if (c.me() is BoundAnd)
                List = (c.me() as BoundAnd).List;
            else
                List = new System.Collections.Generic.List<AbstractBound>() { c.me() };
            if (d.me() is BoundAnd)
            {
                if (List == null)
                    List = new System.Collections.Generic.List<AbstractBound>();
                List.AddRange((d.me() as BoundAnd).List);
            }
            else
            {
                if (List == null)
                    List = new System.Collections.Generic.List<AbstractBound>();
                List.Add(d.me());
            }
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class BoundOr : AbstractBound
    {
        public override int priority() { return 2; }
        public List<AbstractBound> List;
        public BoundOr(Parser yyp) : base(yyp) { }
        public BoundOr(Parser yyp, AbstractBound c, AbstractBound d)
            : base(yyp)
        {
            if (c.me() is BoundOr)
                List = (c.me() as BoundOr).List;
            else
                List = new System.Collections.Generic.List<AbstractBound>() { c.me() };
            if (d.me() is BoundOr)
            {
                if (List == null)
                    List = new System.Collections.Generic.List<AbstractBound>();
                List.AddRange((d.me() as BoundOr).List);
            }
            else
            {
                if (List == null)
                    List = new System.Collections.Generic.List<AbstractBound>();
                List.Add(d.me());
            }
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    
    public partial class BoundVal : AbstractBound
    {
        public string Kind;
        public Value V;
        public BoundVal(Parser yyp) : base(yyp) { }
        public BoundVal(Parser yyp, string k, Value v) : base(yyp) { Kind = k; V = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    
    public partial class TotalBound : AbstractBound
    {
        public TotalBound(Parser yyp) : base(yyp) { }
        public Value V;
        public TotalBound(Parser yyp, Value v) : base(yyp) { V = v; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class DTBound : AbstractBound
    {
        public DTBound(Parser yyp) : base(yyp) { }
        public string name;
        public DTBound(Parser yyp, ID ID) : base(yyp) { name = ID.yytext; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class TopBound : AbstractBound
    {
        public TopBound(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class ValueSet : AbstractBound
    {
        public ValueSet(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Value> Values;
        public ValueSet(Parser yyp, ValueList vl) : base(yyp) { Values = vl.List; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class Identity : Node
    {
        public Identity(Parser yyp) : base(yyp) { }
        public Node C;
        public Identity(Parser yyp, Node c) : base(yyp) { C = c; }
        public override object accept(IVisitor v)
        {
            throw new InvalidOperationException();
        }
    }

    public partial class Atomic : Node
    {
        public string id;
        public Atomic(Parser yyp) : base(yyp) { }
        public Atomic(Parser yyp, ID A) : base(yyp) { id = A.yytext; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class Top : Node
    {
        public Top(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class Bottom : Node
    {
        public Bottom(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class RoleInversion : Expression
    {
        public RoleInversion(Parser yyp) : base(yyp) { }
        public Node R;
        public RoleInversion(Parser yyp, Node r)
            : base(yyp)
        {
            R = r.me();
        }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override int priority() { return 5; }
    }

    public partial class InstanceSet : Expression
    {
        public InstanceSet(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Instance> Instances;
        public InstanceSet(Parser yyp, InstanceList il) : base(yyp) { Instances = il.List; }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }
    public partial class ConceptList : Expression
    {
        public ConceptList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> Exprs = null;
    }
    public partial class ConceptOr : ConceptList
    {
        public ConceptOr(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override int priority() { return 2; }
        public ConceptOr(Parser yyp, Node c, Node d)
            : base(yyp)
        {
            if (c.me() is ConceptOr)
                Exprs = (c.me() as ConceptOr).Exprs;
            else
                Exprs = new System.Collections.Generic.List<Node>() { c.me() };
            if (d.me() is ConceptOr)
            {
                if (Exprs == null)
                    Exprs = new System.Collections.Generic.List<Node>();
                Exprs.AddRange((d.me() as ConceptOr).Exprs);
            }
            else
            {
                if (Exprs == null)
                    Exprs = new System.Collections.Generic.List<Node>();
                Exprs.Add(d.me());
            }
        }
    }

    public partial class ConceptAnd : ConceptList
    {
        public ConceptAnd(Parser yyp) : base(yyp) { }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override int priority() { return 3; }
        public ConceptAnd(Parser yyp, Node c, Node d)
            : base(yyp)
        {
            if (c.me() is ConceptAnd)
                Exprs = (c.me() as ConceptAnd).Exprs;
            else
                Exprs = new System.Collections.Generic.List<Node>() { c.me() };
            if (d.me() is ConceptAnd)
            {
                if (Exprs == null)
                    Exprs = new System.Collections.Generic.List<Node>();
                Exprs.AddRange((d.me() as ConceptAnd).Exprs);
            }
            else
            {
                if (Exprs == null)
                    Exprs = new System.Collections.Generic.List<Node>();
                Exprs.Add(d.me());
            }
        }
    }

    public partial class ConceptNot : Expression
    {
        public ConceptNot(Parser yyp) : base(yyp) { }
        public Node C;
        public ConceptNot(Parser yyp, Node c) : base(yyp) { C = c.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
        public override int priority() { return 5; }
    }

    public partial class Restriction : Expression
    {
        public Restriction(Parser yyp) : base(yyp) { }
        public Node R;
        public override int priority() { return 1; }
    }
    public partial class OnlyRestriction : Restriction
    {
        public OnlyRestriction(Parser yyp) : base(yyp) { }
        public Node C;
        public OnlyRestriction(Parser yyp, Node r, Node c) : base(yyp) { R = r.me(); C = c.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class SomeRestriction : Restriction
    {
        public SomeRestriction(Parser yyp) : base(yyp) { }
        public Node C;
        public SomeRestriction(Parser yyp, Node r, Node c) : base(yyp) { R = r.me(); C = c.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class OnlyValueRestriction : Restriction
    {
        public OnlyValueRestriction(Parser yyp) : base(yyp) { }
        public AbstractBound B;
        public OnlyValueRestriction(Parser yyp, Node r, AbstractBound b) : base(yyp) { R = r.me(); B = b.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class SomeValueRestriction : Restriction
    {
        public SomeValueRestriction(Parser yyp) : base(yyp) { }
        public AbstractBound B;
        public SomeValueRestriction(Parser yyp, Node r, AbstractBound b) : base(yyp) { R = r.me(); B = b.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class SelfReference : Restriction
    {
        public SelfReference(Parser yyp) : base(yyp) { }
        public SelfReference(Parser yyp, Node r) : base(yyp) { R = r.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class CardinalRestriction : Restriction
    {
        public CardinalRestriction(Parser yyp) : base(yyp) { }
        public string N;
        public string Kind;
    }

    public partial class NumberRestriction : CardinalRestriction
    {
        public NumberRestriction(Parser yyp) : base(yyp) { }
        public Node C;
        public NumberRestriction(Parser yyp, string k, Node r, string n, Node c) : base(yyp) { Kind = k; N = n; R = r.me(); C = c.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class NumberValueRestriction : CardinalRestriction
    {
        public NumberValueRestriction(Parser yyp) : base(yyp) { }
        public AbstractBound B;
        public NumberValueRestriction(Parser yyp, string k, Node r, string n, AbstractBound b) : base(yyp) { Kind = k; N = n; R = r.me(); B = b.me(); }
        public override object accept(IVisitor v)
        {
            return v.Visit(this);
        }
    }

    public partial class RoleChain
    {
        public RoleChain(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<Node> List;
        public RoleChain(Parser yyp, Node R, Node S)
            : base(yyp)
        { List = new System.Collections.Generic.List<Node>(); List.Add(R.me()); List.Add(S.me()); }
        public RoleChain(Parser yyp, RoleChain cl, Node R)
            : base(yyp)
        { List = cl.List; List.Add(R.me()); }
    }

    [StatementAttr(StatementType.Rule)]
    public partial class SwrlStatement : Statement
    {
        public SwrlStatement(Parser yyp) : base(yyp) { }
        public SwrlItemList slp, slc;
        public SwrlStatement(Parser yyp, SwrlItemList slp_, SwrlItemList slc_, Modality modality=Modality.IS) : base(yyp) { slp = slp_; slc = slc_; this.modality = modality; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    [StatementAttr(StatementType.Rule)]
    public partial class SwrlIterate : Statement
    {
        public SwrlIterate(Parser yyp) : base(yyp) { }
        public SwrlItemList slp, slc;
        public SwrlVarList vars;
        public SwrlIterate(Parser yyp, SwrlItemList slp_, SwrlItemList slc_, SwrlVarList vars_) : base(yyp) { slp = slp_; slc = slc_; this.vars = vars_; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }    

    public partial class SwrlItemList : IAccept
    {
        public SwrlItemList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<SwrlItem> list;
        public SwrlItemList(Parser yyp, SwrlItem sid_) : base(yyp)
        {
            list = new List<SwrlItem>(); list.Add(sid_);
        }

        public SwrlItemList(Parser yyp, SwrlItemList sl_, SwrlItem sid_) : base(yyp)
        {
            list = sl_.list;
            list.Add(sid_);
        }

        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class SwrlItem : IAccept
    {
        public SwrlItem(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class SwrlInstance : SwrlItem
    {
        public SwrlInstance(Parser yyp) : base(yyp) { }
        public Node C;
        public SwrlIObject I;
        public SwrlInstance(Parser yyp, Node C, SwrlIObject I) : base(yyp) { this.C = C.me(); this.I = I; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class SwrlRole : SwrlItem
    {
        public SwrlRole(Parser yyp) : base(yyp) { }
        public string R;
        public SwrlIObject I, J;
        public SwrlRole(Parser yyp, ID R, SwrlIObject I, SwrlIObject J) : base(yyp) { this.R = R.yytext; this.I = I; this.J = J; }
        public SwrlRole(Parser yyp, string R, SwrlIObject I, SwrlIObject J) : base(yyp) { this.R = R; this.I = I; this.J = J; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class SwrlSameAs : SwrlItem
    {
        public SwrlSameAs(Parser yyp) : base(yyp) { }
        public SwrlIObject I, J;
        public SwrlSameAs(Parser yyp, SwrlIObject I, SwrlIObject J) : base(yyp) { this.I = I; this.J = J; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class SwrlDifferentFrom : SwrlItem
    {
        public SwrlDifferentFrom(Parser yyp) : base(yyp) { }
        public SwrlIObject I, J;
        public SwrlDifferentFrom(Parser yyp, SwrlIObject I, SwrlIObject J) : base(yyp) { this.I = I; this.J = J; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class SwrlBuiltIn : SwrlItem
    {
        public SwrlBuiltIn(Parser yyp) : base(yyp) { }
        public SwrlBuiltIn(Parser yyp, string builtInName, SwrlObjectList DOL) : base(yyp) { this.builtInName = builtInName; this.Values = DOL.Values; }
        public SwrlBuiltIn(Parser yyp, string builtInName, List<ISwrlObject> Values) : base(yyp) { this.builtInName = builtInName; this.Values = Values; }
        public List<ISwrlObject> Values;
        public string builtInName = null;
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public interface ISwrlObject 
    {
        object accept(IVisitor v);
    }

    public partial class SwrlDObject : ISwrlObject, IAccept
    {
        public SwrlDObject(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }

    public partial class SwrlObjectList : IAccept
    {
        public SwrlObjectList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<ISwrlObject> Values;

        public SwrlObjectList(Parser yyp, ISwrlObject a)
            : base(yyp)
        {
            Values = new List<ISwrlObject>();
            Values.Add(a);
        }

        public SwrlObjectList(Parser yyp, SwrlObjectList sl, ISwrlObject a)
            : base(yyp)
        {
            Values = sl.Values;
            Values.Add(a);
        }

        public virtual object accept(IVisitor v) { return null; }
    }

    public interface IExeVar : IAccept
    {
        bool isVar();
    }

    public interface ISwrlVar : IAccept
    {
        string getVar();
    }

    public partial class SwrlDVar : SwrlDObject, ISwrlVar, IExeVar
    {
        public SwrlDVar(Parser yyp) : base(yyp) { }
        public string VAR;
        public SwrlDVar(Parser yyp, ID VAR) : base(yyp) { this.VAR = VAR.yytext; }
        public SwrlDVar(Parser yyp, string VAR) : base(yyp) { this.VAR = VAR; }
        public override object accept(IVisitor v) { return v.Visit(this); }

        public string getVar()
        {
            return VAR;
        }

        public bool isVar()
        {
            return true;
        }
    }

    public partial class SwrlDVal : SwrlDObject, IExeVar
    {
        public SwrlDVal(Parser yyp) : base(yyp) { }
        public Value Val;
        public SwrlDVal(Parser yyp, Value Val) : base(yyp) { this.Val = Val; }
        public override object accept(IVisitor v) { return v.Visit(this); }

        public bool isVar()
        {
            return false;
        }
    }

    public partial class SwrlIObject : ISwrlObject, IAccept
    {
        public SwrlIObject(Parser yyp) : base(yyp) { }
        public virtual object accept(IVisitor v) { return null; }
    }


    public partial class SwrlIVar : SwrlIObject, ISwrlVar, IExeVar
    {
        public SwrlIVar(Parser yyp) : base(yyp) { }
        public string VAR;
        public SwrlIVar(Parser yyp, ID VAR) : base(yyp) { this.VAR = VAR.yytext; }
        public SwrlIVar(Parser yyp, string VAR) : base(yyp) { this.VAR = VAR; }
        public override object accept(IVisitor v) { return v.Visit(this); }

        public string getVar()
        {
            return VAR;
        }
        public bool isVar()
        {
            return true;
        }
    }

    public partial class SwrlIVal : SwrlIObject, IExeVar
    {
        public SwrlIVal(Parser yyp) : base(yyp) { }
        public string I;
        public SwrlIVal(Parser yyp, ID I) : base(yyp) { this.I = I.yytext; }
        public SwrlIVal(Parser yyp, string I) : base(yyp) { this.I = I; }
        public override object accept(IVisitor v) { return v.Visit(this); }
        public bool isVar()
        {
            return false;
        }
    }

    public partial class SwrlDataRange : SwrlItem
    {
        public SwrlDataRange(Parser yyp) : base(yyp) { }
        public AbstractBound B;
        public SwrlDObject DO;
        public SwrlDataRange(Parser yyp, AbstractBound B, SwrlDObject DO) : base(yyp) { this.B = B.me(); this.DO = DO; }
        public override object accept(IVisitor v) { return v.Visit(this); }
    }
    
    public partial class SwrlDataProperty : SwrlItem
    {
        public SwrlDataProperty(Parser yyp) : base(yyp) { }
        public string R;
        public SwrlIObject IO;
        public SwrlDObject DO;
        public SwrlDataProperty(Parser yyp, ID R, SwrlIObject IO, SwrlDObject DO) : base(yyp) { this.R = R.yytext; this.IO = IO; this.DO = DO; } 
        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    [StatementAttr(StatementType.Rule)]
    public partial class ExeStatement : Statement
    {
        public ExeStatement(Parser yyp) : base(yyp) { }
        public SwrlItemList slp;
        public SwrlVarList args;
        public string exe;
        public ExeStatement(Parser yyp, SwrlItemList slp_, SwrlVarList args_, string exe_)
            : base(yyp)
        {
            slp = slp_;
            args = args_;
            exe = exe_;
        }

        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    [StatementAttr(StatementType.Rule)]
    public partial class CodeStatement : Statement
    {
        public CodeStatement(Parser yyp) : base(yyp) { }
        public string exe;
        public CodeStatement(Parser yyp, string exe_)
            : base(yyp)
        {
            exe = exe_;
        }

        public override object accept(IVisitor v) { return v.Visit(this); }
    }

    public partial class SwrlVarList : IAccept
    {
        public SwrlVarList(Parser yyp) : base(yyp) { }
        public System.Collections.Generic.List<IExeVar> list;

        public SwrlVarList(Parser yyp, ID ins, ID dat)
            : base(yyp)
        {
            list = new List<IExeVar>();
            if (ins != null)
                list.Add(new SwrlIVar(yyp, ins));
            if (dat != null)
                list.Add(new SwrlDVar(yyp, dat));
        }

        public SwrlVarList(Parser yyp, ID ins, ID dat, SwrlVarList sl)
            : base(yyp)
        {
            list = sl.list;
            if (ins != null)
                list.Add(new SwrlIVar(yyp, ins));
            if (dat != null)
                list.Add(new SwrlDVar(yyp, dat));
        }

        public virtual object accept(IVisitor v) { return v.Visit(this); }
    }

}