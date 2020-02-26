using System;
using System.Collections.Generic;
using System.Text;
using System.Linq;
using System.Data.Linq;
using org.semanticweb.owlapi.model;
using org.semanticweb.owlapi.vocab;
using System.Globalization;
using org.semanticweb.owlapi.reasoner;
using CogniPy.CNL.DL;
using CogniPy.CNL.EN;
using org.coode.xml;
using System.Text.RegularExpressions;
using System.Diagnostics;
using org.semanticweb.owlapi.util;
using CogniPy.ARS;

namespace CogniPy.SPARQL
{
    public abstract class SparqlNode
    {
        DLToOWLNameConv owlNC = null;

        public string ToOwlName(string name, ARS.EntityKind whatFor)
        {
            return "<" + owlNC.getIRIFromId(name, whatFor).toString() + ">";
        }

        private string freeVarId;

        public SparqlNode(DLToOWLNameConv owlNC, string freeVarId) { this.freeVarId = freeVarId; this.owlNC = owlNC; }
        public string GetFreeVariableId()
        {
            return freeVarId;
        }
        public abstract string ToSparqlBody(bool meanSuperConcept, bool instance = true);
        public virtual string ToSparqlMinus(bool meanSuperConcept, bool instance = true)
        {
            return "";
        }
        public virtual string ToSparqlFilter()
        {
            return "";
        }
        
        public virtual string ToSparqlFilter(bool includeTopBot, bool removeClass)
        {
            return "";
        }

        public virtual bool UseDistinct() { return false; }


        public string ToCombinedBlock(bool meanSuperConcept,bool instance=true)
        {
            var bod = ToSparqlBody(meanSuperConcept,instance);
            var flt = ToSparqlFilter();

            return bod +
                (string.IsNullOrEmpty(flt) ? "" : "\r\nFILTER(" + flt + ")");
        }

        public string ToCombinedBlock(bool meanSuperConcept, bool instance, bool direct, bool includeTopBot, bool removeClass)
        {
            var bod = ToSparqlBody(meanSuperConcept,instance);
            var flt = ToSparqlFilter(includeTopBot, removeClass);

            string wholeBody = bod;
            if (direct)
            {
                var min = ToSparqlMinus(meanSuperConcept,instance);
                if (!string.IsNullOrEmpty(min))
                {
                    wholeBody = "{" + bod + "}\r\nMINUS{"+min+"}";
                }
            }
            return wholeBody +
                (string.IsNullOrEmpty(flt) ? "" : "\r\nFILTER(" + flt + ")");
        }

        static Regex DtmRg = new Regex(@"(?<date>([1-9][0-9]{3}-[0-1][0-9]-[0-3][0-9]))(?<time>(T[0-2][0-9]:[0-5][0-9](:[0-5][0-9](.[0-9]+)?)?)(Z|((\+|\-)[0-2][0-9]:[0-5][0-9]))?)?", RegexOptions.Compiled);
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
            if (str.Length == 0)
                return str;
            char begChar = str[0];
            if (begChar != '\'' && begChar != '\"')
                return str;
            
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
                    else if (c != begChar)
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
            var ttpos = uri.LastIndexOf('^') ;
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
                ttag = uri.Substring(ttpos+1).ToLower();
            }
            if (ttag.EndsWith(">"))
                ttag = ttag.Substring(1, ttag.Length - 2);
            if (ttag.EndsWith("boolean"))
                return (str.CompareTo("true")==0);
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

    public class SparqlInstanceOfDefinedClass : SparqlNode
    {
        string clsname;
        string emptyVarId;
        bool useTypeOf = false;
        public SparqlInstanceOfDefinedClass(DLToOWLNameConv owlNC, string freeVarId, string clsname, string emptyVarId, bool useTypeOf)
            : base(owlNC, freeVarId)
        {
            this.useTypeOf = useTypeOf;
            this.clsname = clsname;
            this.emptyVarId = emptyVarId;
        }

        public override string ToSparqlFilter(bool includeTopBot, bool removeClass)
        {
            var flt = removeClass ? ToOwlName(clsname, ARS.EntityKind.Concept) + "!=" + GetFreeVariableId() : "";
            if (!includeTopBot)
                return "( " + (string.IsNullOrEmpty(flt) ? "" : (flt + " && ")) + GetFreeVariableId() + " != owl:Thing" + " && " + GetFreeVariableId() + " != owl:Nothing" + " )";
            else
                return string.IsNullOrEmpty(flt) ? "" : "( " + flt + " )";
        }

        public override string ToSparqlBody( bool meanSuperConcept, bool instance = true)
        {
            if (!instance)
            {
                if (meanSuperConcept)
                    return ((clsname == null) ? emptyVarId : ToOwlName(clsname, ARS.EntityKind.Concept)) + " rdfs:subClassOf " + GetFreeVariableId();
                else
                    return GetFreeVariableId() + " rdfs:subClassOf " + ((clsname == null) ? emptyVarId : ToOwlName(clsname, ARS.EntityKind.Concept));
            }
            else
            {
                return GetFreeVariableId() + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + ((clsname == null) ? "<http://www.w3.org/2002/07/owl#NamedIndividual>" : ToOwlName(clsname, ARS.EntityKind.Concept));
            }
        }

        public override string ToSparqlMinus( bool meanSuperConcept, bool instance = true)
        {
            string freeId2 = "?x1";
            if (freeId2 == GetFreeVariableId())
            {
                freeId2="?x2";
            }

            string firstBody = ToSparqlBody(meanSuperConcept,instance);
            string minusBody = firstBody + ".\r\n ";
            if (!instance)
            {
                minusBody += firstBody.Replace(GetFreeVariableId(),freeId2)+".\r\n";
                if (!meanSuperConcept)
                {
                    minusBody += GetFreeVariableId() + " rdfs:subClassOf " + freeId2 + ".\r\n";
                }
                else
                {
                    minusBody += freeId2 + " rdfs:subClassOf " + GetFreeVariableId() + ".\r\n";
                }
                minusBody += "FILTER(!isBlank(" + freeId2 + "))" + ".\r\n";
                minusBody += "FILTER(" + freeId2 + "!=" + GetFreeVariableId() + ")" + ".\r\n";
                minusBody += "FILTER(" + freeId2 + "!=" + ToOwlName(clsname, ARS.EntityKind.Concept) + ")" + ".\r\n";
                minusBody += "FILTER(" + ToOwlName(clsname, ARS.EntityKind.Concept) + "!=" + GetFreeVariableId() + ")" + ".\r\n";
            }
            else
            {
                minusBody+= GetFreeVariableId()+ " rdf:type " + freeId2+".\r\n";
                if (clsname != null)
                {
                    minusBody += freeId2 + " rdfs:subClassOf " + ToOwlName(clsname, ARS.EntityKind.Concept);
                    minusBody += "FILTER(" + freeId2 + "!=" + ToOwlName(clsname, ARS.EntityKind.Concept) + ")" + ".\r\n";
                }
                else
                    minusBody += "FILTER(" + freeId2 + " != <http://www.w3.org/2002/07/owl#NamedIndividual> && " + freeId2 + " != <http://www.w3.org/2002/07/owl#Thing>)";
            }
            return minusBody;
        }

        public override bool UseDistinct()
        {
            return clsname == null;
        }
    }

    public class SparqlTop : SparqlNode
    {
        public SparqlTop(DLToOWLNameConv owlNC, string freeVarId)
            : base(owlNC, freeVarId)
        {
        }

        public override string ToSparqlFilter(bool includeTopBot, bool removeClass)
        {
            if (!includeTopBot)
                return "( " + GetFreeVariableId() + " != owl:Thing" + " && " + GetFreeVariableId() + " != owl:Nothing" + " )";
            else
                return "";
        }

        public override string ToSparqlBody(bool meanSuperConcept, bool instance = true)
        {
            if (meanSuperConcept)
                return "owl:Thing rdfs:subClassOf " + GetFreeVariableId();
            else
                return GetFreeVariableId() + " rdfs:subClassOf owl:Thing";
        }

        public override string ToSparqlMinus(bool meanSuperConcept, bool instance = true)
        {
            string freeId2 = "?x1";
            if (freeId2 == GetFreeVariableId())
            {
                freeId2 = "?x2";
            }

            string firstBody = ToSparqlBody(meanSuperConcept, instance);
            string minusBody = firstBody + ".\r\n ";
            minusBody += firstBody.Replace(GetFreeVariableId(), freeId2) + ".\r\n";
            if (!instance)
            {
                if (!meanSuperConcept)
                {
                    minusBody += GetFreeVariableId() + " rdfs:subClassOf " + freeId2 + ".\r\n";
                }
                else
                {
                    minusBody += freeId2 + " rdfs:subClassOf " + GetFreeVariableId() + ".\r\n";
                }
            }
            else
            {
                minusBody = "";
            }
            return minusBody;
        }

        public override bool UseDistinct()
        {
            return false;
        }
    }

    public class SparqlRelatedToValueFilter : SparqlNode
    {


        CNL.DL.Value value;
        string attribute;
        string varVarId;
        string bound;
        public SparqlRelatedToValueFilter(DLToOWLNameConv owlNC, string freeVarId, string attribute, string bound, string varVarId, CNL.DL.Value value)
            : base(owlNC, freeVarId)
        {
            this.attribute = attribute;
            this.value = value;
            this.varVarId = varVarId;
            this.bound = bound;
        }
        public override string ToSparqlBody( bool meanSuperConcept,bool instance=true )
        {
            if (bound == "=")
                return GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + GetLiteralVal(value);
            else
                return GetFreeVariableId() + " " + ToOwlName(attribute, ARS.EntityKind.DataRole) + " " + varVarId;
        }

        public override string ToSparqlFilter(bool includeTopBot, bool removeClass)
        {
            return ToSparqlFilter();
        }

        public override string ToSparqlFilter()
        {
            if (bound == "=")
                return "";
            else if(bound=="#")
                return "regex("+ varVarId + ", "+ GetLiteralVal(value) + ")";
            else
            {
                var b = bound;
                if (bound == "≤")
                    b = "<=";
                else if (bound == "≥")
                    b = ">=";
                else if (bound == "≠")
                    b = "!=";

                return varVarId + " " + b + " " + GetLiteralVal(value);
            }
        }
    }

    public class SparqlRelatedToVariable : SparqlNode
    {
        string variableId;
        string role;
        bool isInversed;
        bool useDistinct;
        public SparqlRelatedToVariable(DLToOWLNameConv owlNC, string freeVarId, string variableId, string role, bool isInversed, bool useDistinct = false)
            : base(owlNC, freeVarId)
        {
            this.variableId = variableId;
            this.role = role;
            this.isInversed = isInversed;
            this.useDistinct = useDistinct;
        }
        public override string ToSparqlBody( bool meanSuperConcept,bool instance=true )
        {
            if (!isInversed)
                return GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + variableId;
            else
                return variableId + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId();
        }
        public override bool UseDistinct()
        {
            return useDistinct;
        }
    }

    public class SparqlRelatedToInstance : SparqlNode
    {
        string instName;
        string role;
        bool isInversed;
        public SparqlRelatedToInstance(DLToOWLNameConv owlNC, string freeVarId, string instName, string role, bool isInversed)
            : base(owlNC, freeVarId)
        {
            this.instName = instName;
            this.role = role;
            this.isInversed = isInversed;
        }
        public override string ToSparqlBody( bool meanSuperConcept,bool instance=true )
        {
            if (!isInversed)
                return GetFreeVariableId() + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + ToOwlName(instName, ARS.EntityKind.Instance);
            else
                return ToOwlName(instName, ARS.EntityKind.Instance) + " " + ToOwlName(role, ARS.EntityKind.Role) + " " + GetFreeVariableId();
        }
    }

    public class SparqlConstantInstance : SparqlNode
    {
        string instName;
        bool useTypeOf;
        public SparqlConstantInstance(DLToOWLNameConv owlNC, string freeVarId, string instName, bool useTypeOf)
            : base(owlNC, freeVarId)
        {
            this.useTypeOf = useTypeOf;
            this.instName = instName;
        }
        public override string ToSparqlFilter()
        {
           return GetFreeVariableId() + " = " + ToOwlName(instName, ARS.EntityKind.Instance);
        }

        public override string ToSparqlFilter(bool includeTopBot, bool removeClass)
        {
            if (!includeTopBot)
                return "( " + GetFreeVariableId() + " != owl:Thing" + " && " + GetFreeVariableId() + " != owl:Nothing" + " )";
            else
                return "";
        }

        public override string ToSparqlBody( bool meanSuperConcept, bool instance = true)
        {
            if (meanSuperConcept)
            {
                return ToOwlName(instName, ARS.EntityKind.Instance) + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + GetFreeVariableId();
            }
            else
                return "";
        }

        public override string ToSparqlMinus( bool meanSuperConcept, bool instance = true)
        {
            if (!meanSuperConcept)
            {
                return "";
            }

            string freeId2 = "?x1";
            if (freeId2 == GetFreeVariableId())
            {
                freeId2 = "?x2";
            }

            string firstBody = ToSparqlBody(meanSuperConcept, instance);
            string minusBody = firstBody + ".\r\n";
            minusBody += firstBody.Replace(GetFreeVariableId(), freeId2)+".\r\n";
            minusBody += freeId2 + " rdfs:subClassOf " + GetFreeVariableId() + ".\r\n";

            return minusBody;
        }
    }

    public class SparqlAnd : SparqlNode
    {
        List<SparqlNode> nodes;
        public SparqlAnd(DLToOWLNameConv owlNC, string freeVarId, List<SparqlNode> nodes)
            : base(owlNC, freeVarId)
        {
            this.nodes = nodes;
        }
        public override string ToSparqlBody( bool meanSuperConcept,bool instance=true )
        {
            return string.Join(" .\r\n", (from n in nodes select n.ToSparqlBody(meanSuperConcept)));
        }
        public override string ToSparqlFilter(bool b, bool removeClass)
        {
            return ToSparqlFilter();
        }
        public override string ToSparqlFilter()
        {
            var toJ = new List<string>();
            foreach (var n in nodes)
            {
                var f = n.ToSparqlFilter();
                if (!string.IsNullOrWhiteSpace(f))
                    toJ.Add(f);
            }
            if (toJ.Count > 0)
                return "(" + string.Join(" &&\r\n", toJ) + ")";
            else
                return "";
        }
        public override bool UseDistinct()
        {
            foreach (var node in nodes)
                if (node.UseDistinct())
                    return true;
            return false;
        }
    }

    public class SparqlOr : SparqlNode
    {
        List<SparqlNode> nodes;
        public SparqlOr(DLToOWLNameConv owlNC, string freeVarId, List<SparqlNode> nodes)
            : base(owlNC, freeVarId)
        {
            this.nodes = nodes;
        }
        public override string ToSparqlBody( bool meanSuperConcept,bool instance=true)
        {
            return string.Join("\r\nUNION\r\n", (from n in nodes select "{" + n.ToCombinedBlock(meanSuperConcept) + "}"));
        }
        public override string ToSparqlFilter(bool b, bool removeClass)
        {
            return ToSparqlFilter();
        }
        public override string ToSparqlFilter()
        {
            var toJ = new List<string>();
            foreach (var n in nodes)
            {
                var f = n.ToSparqlFilter();
                if (!string.IsNullOrWhiteSpace(f))
                    toJ.Add(f);
            }
            if (toJ.Count > 0)
                return "(" + string.Join(" ||\r\n", toJ) + ")";
            else
                return "";
        }
        public override bool UseDistinct()
        {
            foreach (var node in nodes)
                if (node.UseDistinct())
                    return true;
            return false;
        }
    }

    public class SparqlNot : SparqlNode
    {
        SparqlNode node;
        public SparqlNot(DLToOWLNameConv owlNC, string freeVarId, SparqlNode node)
            : base(owlNC, freeVarId)
        {
            this.node = node;
        }
        public override string ToSparqlBody(bool meanSuperConcept, bool instance = true)
        {
            return node.ToSparqlBody(meanSuperConcept, instance);
        }
        public override string ToSparqlFilter(bool b, bool removeClass)
        {
            return ToSparqlFilter();
        }
        public override string ToSparqlFilter()
        {
            var f = node.ToSparqlFilter();
            if (!string.IsNullOrWhiteSpace(f))
                return "!(" + f + ")";
            else
                return "";
        }
        public override bool UseDistinct()
        {
            return node.UseDistinct();
        }
    }
    
    public class Transform : CogniPy.CNL.DL.GenericVisitor
    {

        DLToOWLNameConv _owlNC = new DLToOWLNameConv();
        DLToOWLNameConv owlNC
        {
            get
            {
                if (_pfx2ns != null)
                {
                    var tmpStoredPfxs = _pfx2ns();
                    bool diff = false;
                    //TODO is this efficient? maybe we can implement this in another way?
                    foreach (var pf in tmpStoredPfxs)
                    {
                        if (!storedPfxs.ContainsKey(pf.Key) || storedPfxs[pf.Key] != pf.Value)
                        {
                            diff = true;
                            break;
                        }
                    }

                    if (diff)
                    {
                        storedPfxs = tmpStoredPfxs;
                        PrefixOWLOntologyFormat namespaceManager = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
                        this._owlNC.ClearOWLFormat();
                        if(_defaultNs != null)
                            namespaceManager.setDefaultPrefix(_defaultNs);
                        foreach (var kv in storedPfxs)
                        {
                            namespaceManager.setPrefix(kv.Key, kv.Value);
                        }

                        this._owlNC.setOWLFormat(_defaultNs, namespaceManager, _lex);
                    }
                }
                return _owlNC;
            }
            set{_owlNC=value;}
        }

        Dictionary<string, string> storedPfxs=new Dictionary<string,string>();

        private OWLDataFactory factory;

        public Dictionary<string, string> InvUriMappings { get { return owlNC.InvUriMappings; } set { owlNC.InvUriMappings = value; } }

        //TODO probably this initializer should not exist because the prefixes are not loaded dynamically...
        public void setOWLDataFactory(string defaultNS, OWLDataFactory factory, PrefixOWLOntologyFormat namespaceManager, CogniPy.CNL.EN.endict lex)
        {
            this.factory = factory;
            this._pfx2ns = null ;
            this._lex = lex;
            this._defaultNs = defaultNS;
            this.owlNC.setOWLFormat(defaultNS, namespaceManager, _lex);
        }

        Func<Dictionary<string, string>> _pfx2ns;
        CogniPy.CNL.EN.endict _lex;
        string _defaultNs;
        public void setOWLDataFactory(string defaultNs, Func<Dictionary<string,string>> pfx2ns,CogniPy.CNL.EN.endict lex)
        {
            this._defaultNs = defaultNs;

            this._pfx2ns = pfx2ns;
            this._lex = lex;

            if (_defaultNs != null)
            {
                // set the default prefix so that this one at least is remembered....
                PrefixOWLOntologyFormat namespaceManager = new org.semanticweb.owlapi.io.RDFXMLOntologyFormat();
                this._owlNC.ClearOWLFormat();
                namespaceManager.setDefaultPrefix(_defaultNs);
                this._owlNC.setOWLFormat(_defaultNs, namespaceManager, _lex);
            }
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

        public static string PREAMBLE =
@"PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>
PREFIX rdfs: <http://www.w3.org/2000/01/rdf-schema#>
PREFIX owl: <http://www.w3.org/2002/07/owl#>
PREFIX xsd: <http://www.w3.org/2001/XMLSchema#>
PREFIX fn: <http://www.w3.org/2005/xpath-functions#>
";
        bool useTypeOf;

        public string ConvertToGetInstancesOf(CNL.DL.Node n,  List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding,
            out Dictionary<string, string> attributeBinding, out string defaultIntance, int offset, int pageSize, bool useTypeOf = false, bool direct = true, string order = "NONE")
        {

            var rr = ConvertToGetInstancesOfDetails(n, roles, attributes, out roleBinding, out attributeBinding, out defaultIntance, useTypeOf, direct, order);

            var distinct = rr[0];
            var selectVars = rr[1];
            var whereBlock = rr[2];
            var filterBlock = rr[3];
            var whereBlock2 = rr[4];
            var orderByBlock = rr[5];

            var q = PREAMBLE + "SELECT " + distinct + selectVars + "\r\n"
                + "WHERE {" + (whereBlock2 != null ? "{" : "") + whereBlock;
            if (filterBlock != null)
                q += " FILTER (" + filterBlock + ")";
            if (whereBlock2 != null)
            {
                q += "} UNION {";
                q += whereBlock2;
                q += " FILTER (";
                q += filterBlock.Replace(" = <", " != <");
                q += ")";
                q += "}";
            }
            q += "}";
            if (orderByBlock != null)
                q += orderByBlock;

            q += (pageSize > -1 ? " LIMIT " + pageSize + " OFFSET " + offset : string.Empty);
            
            return q;

        }

        public string ConvertToGetTypesOf(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding,
    out Dictionary<string, string> attributeBinding, out string defaultIntance, int offset, int pageSize, bool useTypeOf = false, bool direct = true, string order = "NONE")
        {

            var rr = ConvertToGetTypesOfDetails(n, roles, attributes, out roleBinding, out attributeBinding, out defaultIntance, useTypeOf, direct, order);

            var distinct = rr[0];
            var selectVars = rr[1];
            var whereBlock = rr[2];
            var filterBlock = rr[3];
            var whereBlock2 = rr[4];
            var orderByBlock = rr[5];

            var q = PREAMBLE + "SELECT " + distinct + selectVars + "\r\n"
                + "WHERE {" + (whereBlock2 != null ? "{" : "") + whereBlock;
            if (filterBlock != null)
                q += " FILTER (" + filterBlock + ")";
            if (whereBlock2 != null)
            {
                q += "} UNION {";
                q += whereBlock2;
                q += "}";
            }
            q += "}";
            if (orderByBlock != null)
                q += orderByBlock;

            q += (pageSize > -1 ? " LIMIT " + pageSize + " OFFSET " + offset : string.Empty);

            return q;

        }

        public string[] ConvertToGetInstancesOfDetails(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding,
            out Dictionary<string, string> attributeBinding, out string defaultIntance, bool useTypeOf = false, bool direct = true, string order = "NONE")
        {
            this.useTypeOf = useTypeOf;
            freeVarIdBase = 0;
            roleBinding = null;
            attributeBinding = null;
            string selectVars;
            string whereBlock;
            string whereBlock2 = null;
            string orderByBlock = null;

            string filterBlock = null;
            bool distinct = false;
            string lhs;

            bool ordering = order != "NONE";
            if (n is InstanceSet)
            {
                var insts = (n as InstanceSet).Instances;
                defaultIntance = (insts.First() as NamedInstance).name;
                if (insts.Count == 1)
                {
                    lhs = ToOwlName(defaultIntance, ARS.EntityKind.Instance);
                    selectVars = "?z0";

                    whereBlock = "?z0" + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + "<http://www.w3.org/2002/07/owl#NamedIndividual>";

                    whereBlock2 = "?z0 owl:sameAs " + lhs;
                    filterBlock = "?z0 = " + lhs;
                }
                else if (insts.Count > 1)
                {
                    lhs = string.Join(",", (from i in insts select ToOwlName((i as NamedInstance).name, ARS.EntityKind.Instance)));
                    selectVars = "?z0";

                    whereBlock = "?z0" + (useTypeOf ? " rdf:type " : " rdf:instanceOf ") + "<http://www.w3.org/2002/07/owl#NamedIndividual>";

                    whereBlock2 = null;
                    filterBlock = "?z0 IN (" + lhs + ")";
                }
                else
                    throw new InvalidOperationException();
            }
            else
            {
                using (activeFreeVarId.set(newFreeVarId()))
                {
                    var sparqlNode = n.accept(this) as SparqlNode;
                    distinct = sparqlNode.UseDistinct();

                    lhs = sparqlNode.GetFreeVariableId();
                    selectVars = sparqlNode.GetFreeVariableId();
                    whereBlock = sparqlNode.ToCombinedBlock(false, true, direct, true, false);
                    defaultIntance = null;
                }
            }

            if (ordering)
            {
                whereBlock += ". "+selectVars+" <http://www.ontorion.com#label> ?z1";
                orderByBlock = string.Format(" ORDER BY {0}(?z1) ", order);
                distinct = true;
            }

            if (roles != null)
            {
                roleBinding = new Dictionary<string, string>();
                foreach (var role in roles)
                {
                    var roleId = newFreeVarId();
                    roleBinding.Add(roleId.Substring(1), role);
                    selectVars += " " + roleId;
                    whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(role, ARS.EntityKind.DataRole) + " " + roleId + "}";
                }
            }

            if (attributes != null)
            {
                attributeBinding = new Dictionary<string, string>();
                foreach (var attr in attributes)
                {
                    var attrId = newFreeVarId();
                    attributeBinding.Add(attrId.Substring(1), attr);
                    selectVars += " " + attrId;
                    whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(attr, ARS.EntityKind.DataRole) + " " + attrId + "}";
                }
            }

            return new string[] { (distinct ? "DISTINCT " : " "), selectVars, whereBlock, filterBlock, whereBlock2, orderByBlock };
        }

        public string[] ConvertToGetTypesOfDetails(CNL.DL.Node n, List<string> roles, List<string> attributes, out Dictionary<string, string> roleBinding,
            out Dictionary<string, string> attributeBinding, out string defaultIntance, bool useTypeOf = false, bool direct = true, string order = "NONE")
        {
            this.useTypeOf = useTypeOf;
            freeVarIdBase = 0;
            roleBinding = null;
            attributeBinding = null;
            string selectVars;
            string whereBlock;
            string whereBlock2 = null;
            string orderByBlock = null;

            string filterBlock = null;
            bool distinct = false;
            string lhs;

            bool ordering = order != "NONE";
            if (n is InstanceSet)
            {
                var insts = (n as InstanceSet).Instances;
                if (insts.Count != 1)
                    throw new InvalidOperationException();

                defaultIntance = (insts.First() as NamedInstance).name;
                lhs = ToOwlName(defaultIntance, ARS.EntityKind.Instance);

                using (activeFreeVarId.set(newFreeVarId()))
                {
                    selectVars = activeFreeVarId.get();
                    whereBlock = lhs + (useTypeOf ? " rdf:type " : " rdf:instanceOf ")+ activeFreeVarId.get();
                    defaultIntance = null;
                    var flt = lhs + "!=" + activeFreeVarId.get();
                    filterBlock = "( " + flt + " && " + activeFreeVarId.get() + " != owl:Thing" + " && " + activeFreeVarId.get() + " != owl:Nothing" + " )";
                    if(direct)
                    {
                        string freeId2 = "?x1";
                        if (freeId2 == activeFreeVarId.get())
                        {
                            freeId2 = "?x2";
                        }

                        var minusBody = whereBlock.Replace(activeFreeVarId.get(), freeId2) + ".\r\n";
                        minusBody += freeId2 + " rdfs:subClassOf " + activeFreeVarId.get() + ".\r\n";
                        minusBody += "FILTER(" + freeId2 + "!=" + activeFreeVarId.get() + ")" + ".\r\n";
                        minusBody += "FILTER(" + freeId2 + "!=" + lhs + ")" + ".\r\n";
                        minusBody += "FILTER(" + lhs + "!=" + activeFreeVarId.get() + ")" + ".\r\n";
                        whereBlock += ". MINUS {" + minusBody + "}";
                    }
                }
            }
            else
                throw new InvalidOperationException();

            if (ordering)
            {
                whereBlock += ". " + selectVars + " <http://www.ontorion.com#label> ?z1";
                orderByBlock = string.Format(" ORDER BY {0}(?z1) ", order);
                distinct = true;
            }

            if (roles != null)
            {
                roleBinding = new Dictionary<string, string>();
                foreach (var role in roles)
                {
                    var roleId = newFreeVarId();
                    roleBinding.Add(roleId.Substring(1), role);
                    selectVars += " " + roleId;
                    whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(role, ARS.EntityKind.DataRole) + " " + roleId + "}";
                }
            }

            if (attributes != null)
            {
                attributeBinding = new Dictionary<string, string>();
                foreach (var attr in attributes)
                {
                    var attrId = newFreeVarId();
                    attributeBinding.Add(attrId.Substring(1), attr);
                    selectVars += " " + attrId;
                    whereBlock += "\r\nOPTIONAL {" + lhs + " " + ToOwlName(attr, ARS.EntityKind.DataRole) + " " + attrId + "}";
                }
            }

            return new string[] { (distinct ? "DISTINCT " : " "), selectVars, whereBlock, filterBlock, whereBlock2, orderByBlock };
        }


        public string ConvertToGetSuperconceptsOf(CNL.DL.Node n, bool direct, bool includeTopBot, int offset, int pageSize, bool useTypeOf = false, string order = "NONE")
        {
            return ConvertToGetRelatedConceptOf(n, true, direct, includeTopBot, offset, pageSize, useTypeOf, order);
        }

        public string ConvertToGetSubconceptsOf(CNL.DL.Node n, bool direct, bool includeTopBot, int offset, int pageSize, bool useTypeOf = false, string order = "NONE")
        {
            return ConvertToGetRelatedConceptOf(n, false, direct, includeTopBot, offset, pageSize, useTypeOf, order);
        }

        public string ConvertToGetRelatedConceptOf(CNL.DL.Node n,  bool meanSuperConcept, bool direct, bool includeTopBot, int offset, int pageSize, bool useTypeOf = false, string order = "NONE")
        {
            this.useTypeOf = useTypeOf;
            freeVarIdBase = 0;
            string selectVars;
            string whereBlock;
            bool distinct = false;

            bool ordering = order != "NONE";

            using (activeFreeVarId.set(newFreeVarId()))
            {
                var sparqlNode = n.accept(this) as SparqlNode;
                if (n is CogniPy.CNL.DL.Top)
                    sparqlNode = new SparqlTop(owlNC, sparqlNode.GetFreeVariableId());

                distinct = sparqlNode.UseDistinct();

                selectVars = sparqlNode.GetFreeVariableId();
                whereBlock = sparqlNode.ToCombinedBlock(meanSuperConcept, false, direct, includeTopBot,true);

                if (ordering)
                    whereBlock += ". " + selectVars + " <http://www.ontorion.com#label> ?z1";
            }


            return PREAMBLE + "SELECT " + (distinct ? "DISTINCT " : " ") + selectVars + "\r\n"
                + "WHERE {" + whereBlock + "}"
                + (ordering ? string.Format(" ORDER BY {0}(?z1) ", order) : string.Empty )
                + (pageSize > -1 ? " LIMIT "+pageSize+" OFFSET " + offset : string.Empty );
        }

        public string ConvertToSolutionExists(CNL.DL.Node n, bool useTypeOf = false)
        {
            this.useTypeOf = useTypeOf;
            freeVarIdBase = 0;

            using (activeFreeVarId.set(newFreeVarId()))
            {
                var sparqlNode = n.accept(this) as SparqlNode;

                return PREAMBLE + "ASK {" + sparqlNode.ToCombinedBlock(false) +"}";
            }
        }
        public override object Visit(Top e)
        {
            if (isKindOf.get() == "C")
            {
                return new SparqlInstanceOfDefinedClass(owlNC, activeFreeVarId.get(), null, newFreeVarId(), useTypeOf);
            }
            return null;
        }

        public override object Visit(Atomic e)
        {
            if (isKindOf.get() == "C")
            {
                return new SparqlInstanceOfDefinedClass(owlNC, activeFreeVarId.get(), e.id, null, useTypeOf);
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
                    return new SparqlRelatedToInstance(owlNC, activeFreeVarId.get(), (instSet.Instances.First() as NamedInstance).name, r.Item2, r.Item1);
                }
                else
                {
                    List<SparqlNode> nodes = new List<SparqlNode>();
                    foreach (var inst in instSet.Instances)
                        nodes.Add(new SparqlRelatedToInstance(owlNC, activeFreeVarId.get(), (inst as NamedInstance).name, r.Item2, r.Item1));
                    return new SparqlOr(owlNC, activeFreeVarId.get(), nodes);
                }
            }
            else if (e.C is Top)
            {
                return new SparqlRelatedToVariable(owlNC, activeFreeVarId.get(), newFreeVarId(), r.Item2, r.Item1, true);
            }
            else
            {
                SparqlNode c;
                using (activeFreeVarId.set(newFreeVarId()))
                    c = e.C.accept(this) as SparqlNode;

                var d = new SparqlRelatedToVariable(owlNC, activeFreeVarId.get(), c.GetFreeVariableId(), r.Item2, r.Item1);

                return new SparqlAnd(owlNC, activeFreeVarId.get(), new List<SparqlNode>() { c, d });
            }
        }

        public override object Visit(SelfReference e)
        {
            Tuple<bool, string> r;
            using (isKindOf.set("R"))
                r = e.R.accept(this) as Tuple<bool, string>;

            return new SparqlRelatedToVariable(owlNC, activeFreeVarId.get(), activeFreeVarId.get(), r.Item2, r.Item1);
        }

        public override object Visit(BoundNot e)
        {
            return new SparqlNot(owlNC, activeFreeVarId.get(), e.B.accept(this) as SparqlNode);
        }

        public override object Visit(BoundAnd e)
        {
            var nodes = (from x in e.List
                         select (x.accept(this) as SparqlNode)).ToList();

            return new SparqlAnd(owlNC, activeFreeVarId.get(), nodes);
        }

        public override object Visit(BoundOr e)
        {
            var nodes = (from x in e.List
                         select (x.accept(this) as SparqlNode)).ToList();

            return new SparqlOr(owlNC, activeFreeVarId.get(), nodes);
        }

        public override object Visit(CogniPy.CNL.DL.BoundFacets e)
        {
            var nodes = (from x in e.FL.List
                         select new SparqlRelatedToValueFilter(owlNC, activeFreeVarId.get(),
                             activeAttribute.get(), x.Kind, x.Kind == "=" ? null : newFreeVarId(), x.V)).ToList<SparqlNode>();

            if (nodes.Count == 1)
                return nodes.First();
            else
                return new SparqlAnd(owlNC, activeFreeVarId.get(), nodes);
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
            return new SparqlRelatedToValueFilter(owlNC, activeFreeVarId.get(),
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
            return new SparqlRelatedToVariable(owlNC, activeFreeVarId.get(), newFreeVarId(), activeAttribute.get(), false, true);
        }

        public override object Visit(InstanceSet e)
        {
            return new SparqlConstantInstance(owlNC, activeFreeVarId.get(), (e.Instances.First() as NamedInstance).name, useTypeOf);
        }

        public override object Visit(ConceptAnd e)
        {
            using (isKindOf.set("C"))
            {
                List<SparqlNode> nodes = new List<SparqlNode>();
                foreach (var expr in e.Exprs)
                {
                    nodes.Add(expr.accept(this) as SparqlNode);
                }
                return new SparqlAnd(owlNC, activeFreeVarId.get(), nodes);
            }
        }

        public override object Visit(ConceptOr e)
        {
            using (isKindOf.set("C"))
            {
                List<SparqlNode> nodes = new List<SparqlNode>();
                foreach (var expr in e.Exprs)
                {
                    nodes.Add(expr.accept(this) as SparqlNode);
                }
                return new SparqlOr(owlNC, activeFreeVarId.get(), nodes);
            }
        }

    }
}
