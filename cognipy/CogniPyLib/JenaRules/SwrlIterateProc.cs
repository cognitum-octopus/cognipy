using CogniPy.ARS;
using org.apache.jena.graph;
using org.apache.jena.rdf.model;
using org.apache.jena.reasoner.rulesys;
using org.semanticweb.owlapi.vocab;
using System;
using System.Collections.Generic;
using System.Text;
using System.Text.RegularExpressions;

namespace CogniPy.CNL.DL
{
    public class SwrlIterateProc : GenericVisitor
    {

        public class NotInProfileException : Exception { }

        void NotInProfile()
        {
            throw new NotInProfileException();
        }

        Model model;
        string defaultNS;

        public RuleContext context;

        public string iterVar;
        public object iterVal;
        public Dictionary<string, int> varNameToIndex;
        public object[] allVars;
        bool swrlOnly = false;

        public SwrlIterateProc(Model model, bool swrlOnly = false)
        {
            this.model = model;
            this.swrlOnly = swrlOnly;
        }

        DLToOWLNameConv owlNC = new DLToOWLNameConv();

        public void setOWLDataFactory(string defaultNS, PrefixOWLOntologyFormat namespaceManager, CogniPy.CNL.EN.endict lex)
        {
            this.owlNC.setOWLFormat(defaultNS, namespaceManager, lex);
        }

        void AddIfNotExists(org.apache.jena.graph.Node s, org.apache.jena.graph.Node v, org.apache.jena.graph.Node o)
        {
            var t = new Triple(s, v, o);
            if (!context.contains(t))
                context.add(t);
        }

        public override object Visit(SwrlInstance e)
        {
            if (e.C is CNL.DL.Atomic)
            {
                var inst = e.I.accept(this) as org.apache.jena.graph.Node;

                var cls = owlNC.getIRIFromId((e.C as CNL.DL.Atomic).id, EntityKind.Concept);
                var clsn = org.apache.jena.graph.NodeFactory.createURI(cls.toString());
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), clsn);
                if (!swrlOnly)
                {
                    AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                    AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                    AddIfNotExists(clsn, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Class.asNode());
                }
            }
            else
            {
                NotInProfile();
                return base.Visit(e);
            }
            return null;
        }

        public override object Visit(SwrlRole e)
        {
            var inst = e.I.accept(this) as org.apache.jena.graph.Node;
            var jnst = e.J.accept(this) as org.apache.jena.graph.Node;

            var rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
            var reln = org.apache.jena.graph.NodeFactory.createURI(rel.toString());
            AddIfNotExists(inst, reln, jnst);
            if (!swrlOnly)
            {
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                AddIfNotExists(reln, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode());
            }
            return null;
        }

        public override object Visit(SwrlSameAs e)
        {
            var inst = e.I.accept(this) as org.apache.jena.graph.Node;
            var jnst = e.J.accept(this) as org.apache.jena.graph.Node;

            AddIfNotExists(inst, org.apache.jena.vocabulary.OWL2.sameAs.asNode(), jnst);
            if (!swrlOnly)
            {
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
            }
            return null;
        }

        public override object Visit(SwrlDifferentFrom e)
        {
            var inst = e.I.accept(this) as org.apache.jena.graph.Node;
            var jnst = e.J.accept(this) as org.apache.jena.graph.Node;

            AddIfNotExists(inst, org.apache.jena.vocabulary.OWL2.differentFrom.asNode(), jnst);
            if (!swrlOnly)
            {
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(jnst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
            }
            return null;
        }

        public override object Visit(SwrlDataRange e)
        {
            throw new NotImplementedException();
        }

        public override object Visit(SwrlDataProperty e)
        {
            var inst = e.IO.accept(this) as org.apache.jena.graph.Node;

            var rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
            var reln = org.apache.jena.graph.NodeFactory.createURI(rel.toString());
            var dv = e.DO.accept(this) as org.apache.jena.graph.Node;
            AddIfNotExists(inst, reln, dv);
            if (!swrlOnly)
            {
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.Thing.asNode());
                AddIfNotExists(inst, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                AddIfNotExists(reln, org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());
            }
            return null;
        }

        public override object Visit(SwrlBuiltIn e)
        {
            throw new NotImplementedException();
        }

        //SwrlNodes

        public override object Visit(SwrlIVal e)
        {
            return org.apache.jena.graph.NodeFactory.createURI(owlNC.getIRIFromId(e.I, EntityKind.Instance).toString());
        }

        public override object Visit(SwrlIVar e)
        {
            var varN = "?" + e.VAR.Replace("-", "_");
            object val;
            if (iterVar == varN)
                val = iterVal;
            else
                val = allVars[varNameToIndex[varN]];
            return org.apache.jena.graph.NodeFactory.createURI(owlNC.getIRIFromId(val.ToString(), EntityKind.Instance).toString());
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

        Literal getLiteralVal(Value v)
        {
            if (model == null) return null;

            if (v is CNL.DL.Bool) return model.createTypedLiteral(v.ToBool(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDboolean);
            if (v is CNL.DL.String) return model.createTypedLiteral(v.ToString(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
            if (v is CNL.DL.Float) return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdouble);
            if (v is CNL.DL.Number) return model.createTypedLiteral(new java.lang.Integer(v.ToInt()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger);
            if (v is CNL.DL.DateTimeVal) return model.createTypedLiteral(completeDTMVal(v.ToStringExact()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdateTime);
            if (v is CNL.DL.Duration) return model.createTypedLiteral(v.ToStringExact(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDduration);

            return model.createTypedLiteral(v.ToString()); //TODO xsd:date i inne typy
        }

        public override object Visit(SwrlDVal e)
        {
            return getLiteralVal(e.Val).asNode();
        }

        public override object Visit(SwrlDVar e)
        {
            var varN = "?" + e.VAR.Replace("-", "_");
            object val;
            if (iterVar == varN)
                val = iterVal;
            else
                val = allVars[varNameToIndex[varN]];
            return getLiteralVal(Value.FromObject(val)).asNode();
        }
        //bounds

    }
}
