using org.apache.jena.rdf.model;
using CogniPy.ARS;
using CogniPy.CNL.EN;
using org.semanticweb.owlapi.vocab;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using CogniPy.Executing.HermiT;

namespace CogniPy.CNL.DL
{
    public class GenerateJenaRules : GenericVisitor
    {

        public class NotInProfileException : Exception { }

        void NotInProfile()
        {
            throw new NotInProfileException();
        }

        Model model;
        bool useSWRL;
        string defaultNS;
        bool debugSWRL = false;
        bool runExeRules = false;
        bool debugExeRules = false;
        bool modalCheckerRules = false;
        bool swrlOnly = false;

        public GenerateJenaRules(Model model, bool modalChecker, bool useSWRL = true,bool debugSWRL=false,bool runExeRules=false,bool debugExeRules=false, bool swrlOnly=false)
        {
            this.model = model;
            this.useSWRL = useSWRL;
            this.debugSWRL = debugSWRL;
            this.runExeRules = runExeRules;
            this.debugExeRules = debugExeRules;
            this.modalCheckerRules = modalChecker;
            this.swrlOnly = swrlOnly;
        }

        DLToOWLNameConv owlNC = new DLToOWLNameConv();

        public void setOWLDataFactory(string defaultNS, PrefixOWLOntologyFormat namespaceManager, CogniPy.CNL.EN.endict lex)
        {
            this.owlNC.setOWLFormat(defaultNS, namespaceManager, lex);
        }


         Dictionary<string, Statement> id2stmt;
        
        public void setId2stmt(Dictionary<string, Statement> id2stmt)
        {
            this.id2stmt = id2stmt;
        }

        StringBuilder sb = null;
        static int prp_spo2_cnt = 0;
        static int swrl_cnt = 0;
        static int prp_key_cnt = 0;
        static int sme_cnt = 0;

        public string Generate(Paragraph p)
        {
            sb = new StringBuilder();
            p.accept(this);
            return sb.ToString();
        }

        public bool Validate(Statement stmt)
        {
            try
            {
                sb = new StringBuilder();
                stmt.accept(this);
                return true;
            }
            catch (NotInProfileException)
            {
                return false;
            }
        }

        public override object Visit(Paragraph e)
        {
            foreach (var stmt in e.Statements)
            {
                if (!useSWRL && stmt is SwrlStatement)
                    continue;
                var osb = sb;
                try
                {
                    sb = new StringBuilder();
                    stmt.accept(this);
                    osb.Append(sb.ToString());
                }
                catch (NotInProfileException)
                {
                }
                finally
                {
                    sb = osb;
                }
            }
            return null;
        }

        private Tuple<string,string,string> SolveSingleSome(string cid, object restr)
        {
            if (restr is CogniPy.CNL.DL.SomeRestriction)
            {
                var Rest = restr as SomeRestriction;
                if (Rest.C is InstanceSet & Rest.R is Atomic)
                {
                    var iSet = Rest.C as InstanceSet;
                    if (iSet.Instances.Count == 1 && iSet.Instances[0] is NamedInstance)
                    {
                        var q = owlNC.getIRIFromId(cid, EntityKind.Concept);
                        var r = owlNC.getIRIFromId((Rest.R as CNL.DL.Atomic).id, EntityKind.Role);
                        var isn = owlNC.getIRIFromId((iSet.Instances[0] as NamedInstance).name, EntityKind.Instance);

                        return Tuple.Create("<" + q.ToString() + ">", "<" + r.ToString() + ">", "<" + isn.ToString() + ">");

                    }
                }
            }
            else if (restr is SomeValueRestriction)
            {
                var Rest = restr as SomeValueRestriction;
                if (Rest.B is ValueSet & Rest.R is Atomic)
                {
                    var vSet = Rest.B as ValueSet;
                    if (vSet.Values.Count == 1)
                    {

                        var q = owlNC.getIRIFromId(cid, EntityKind.Concept);
                        var r = owlNC.getIRIFromId((Rest.R as CNL.DL.Atomic).id, EntityKind.Role);
                        var vn = getLiteralVal2(vSet.Values[0] as Value);
                        return Tuple.Create("<" + q.ToString() + ">", "<" + r.ToString() + ">", vn);
                    }
                }
            }
            return null;
        }

        public void appendDebugString(StringBuilder sb,CNL.DL.Statement stmt )
        {
            if (model != null)
            {
                if (debugExeRules)
                {
                    DL.Serializer ser = new Serializer();
                    var dl = ser.Serialize(stmt);
                    var iid = model.createTypedLiteral("\'" + dl.Replace("\'", "\\\'").ToString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
                    sb.Append(" debugTraceBuiltIn (" + iid + "),");
                }
            }
        }

        public override object Visit(Subsumption e)
        {
            if (e.modality == Statement.Modality.IS)
            {
                if (e.C is CogniPy.CNL.DL.Atomic)
                {
                    if (e.D is CogniPy.CNL.DL.SomeRestriction || e.D is CogniPy.CNL.DL.SomeValueRestriction)
                    {
                        var tpl = SolveSingleSome((e.C as CNL.DL.Atomic).id, e.D);
                        if (tpl != null)
                        {
                            var idx = Interlocked.Increment(ref sme_cnt).ToString();
                            sb.Append("[sme-" + idx + ": ");
                            id2stmt.Add("sme-" + idx, e);
                            sb.Append("(?X rdf:type " + tpl.Item1 + ") -> (?X " + tpl.Item2 + " " + tpl.Item3 + ")");
                            appendDebugString(sb, e);
                            sb.AppendLine("]");
                        }
                    }
                    else if (e.D is CNL.DL.ConceptAnd)
                    {
                        var A = e.D as CNL.DL.ConceptAnd;
                        bool found = false;
                        foreach (var x in A.Exprs)
                        {
                            var tpl = SolveSingleSome((e.C as CNL.DL.Atomic).id, x);
                            if (tpl != null)
                            {
                                if (!found)
                                {
                                    found = true;
                                    var idx = Interlocked.Increment(ref sme_cnt).ToString();
                                    sb.Append("[sme-" + idx + ": (?X rdf:type " + tpl.Item1 + ") -> ");
                                    id2stmt.Add("sme-" + idx, e);
                                }
                                sb.Append("(?X " + tpl.Item2 + " " + tpl.Item3 + ")");
                            }
                        }
                        if (found)
                        {
                            appendDebugString(sb, e);
                            sb.AppendLine("]");
                        }
                    }
                }
                return base.Visit(e);
            }
            else if (modalCheckerRules)
            {
                var id = Interlocked.Increment(ref swrl_cnt).ToString();
                id2stmt.Add("subsumption-modal-body-" + id, e);
                string iid = null;
                if (model != null)
                {
                    //                iid = model.createTypedLiteral("\"" + id.ToString() + "\"", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
                    DL.Serializer ser = new Serializer();
                    var dl = ser.Serialize(e);
                    iid = model.createTypedLiteral("\'" + dl.Replace("\'", "\\\'").ToString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
                }

                TransformToJenaRules jenarulesTransform = new TransformToJenaRules();
                jenarulesTransform.setOWLDataFactory(owlNC);
                string ruleC = null;
                string ruleD = null;
                try
                {
                    ruleC = jenarulesTransform.ConvertToGetInstancesOf(e.C);
                    ruleD = jenarulesTransform.ConvertToGetInstancesOf(e.D);

                    bool normal = (e.modality == Statement.Modality.CAN || e.modality == Statement.Modality.SHOULD || e.modality == Statement.Modality.MUST);
                    sb.Append("[subsumption-modal-body-" + id + ": ");
                    sb.Append(ruleC);
                    sb.Append(" -> ");
                    if (model != null)
                        sb.Append(" modalCheckerBuiltIn (0," + (normal ? "0," : "1,") + iid + ")");
                    sb.AppendLine("]");
                    sb.Append("[subsumption-modal-head-" + id + ": ");
                    sb.Append(ruleC);
                    sb.Append(",");
                    sb.Append(ruleD);
                    sb.Append(" -> ");
                    if (model != null)
                        sb.Append(" modalCheckerBuiltIn (1," + (normal ? "0," : "1,") + iid + ")");
                    appendDebugString(sb, e);
                    sb.AppendLine("]");
                }
                catch (CogniPy.Executing.HermiTClient.ReasoningServiceException)
                {
                    ruleC = "";
                    ruleD = "";
                }
                catch
                {
                }
                return null;
            }
            else
                return null;
        }

        public override object Visit(ComplexRoleInclusion e)
        {
            var id = "prp-spo2-" + Interlocked.Increment(ref prp_spo2_cnt).ToString();
            id2stmt.Add(id, e);

            sb.Append("["+id + ": ");
            int varid = 0;
            foreach (var r in e.RoleChain)
            {
                if (varid > 0)
                {
                    sb.Append(", ");
                }
                if (r is CNL.DL.Atomic)
                {
                    var q = owlNC.getIRIFromId((r as CNL.DL.Atomic).id, EntityKind.Role);
                    sb.Append("(?X" + varid.ToString() + " <" + q + "> " + "?X" + (varid + 1).ToString() + ")");
                }
                else if (r is CNL.DL.RoleInversion)
                {
                    var rr = (r as CNL.DL.RoleInversion).R;
                    if (rr is CNL.DL.Atomic)
                    {
                        var q = owlNC.getIRIFromId((rr as CNL.DL.Atomic).id, EntityKind.Role);
                        sb.Append("(?X" + (varid + 1).ToString() + " <" + q + "> " + "?X" + varid.ToString() + ")");
                    }
                }
                varid++;
            }

            sb.Append(" -> ");

            {
                var r = e.R;
                if (r is CNL.DL.Atomic)
                {
                    var q = owlNC.getIRIFromId((r as CNL.DL.Atomic).id, EntityKind.Role);
                    sb.Append("(?X0 <" + q + "> ?X" + varid.ToString() + ")");
                }
                else if (r is CNL.DL.RoleInversion)
                {
                    var rr = (r as CNL.DL.RoleInversion).R;
                    if (rr is CNL.DL.Atomic)
                    {
                        var q = owlNC.getIRIFromId((rr as CNL.DL.Atomic).id, EntityKind.Role);
                        sb.Append("(?X" + varid.ToString() + " <" + q + "> " + "?X0)");
                    }
                }
            }
            appendDebugString(sb, e);
            sb.AppendLine("]");
            return null;
        }

        public override object Visit(HasKey e)
        {
            StringBuilder[] side = new StringBuilder[] { new StringBuilder(), new StringBuilder() };

            var conc = owlNC.getIRIFromId((e.C as CNL.DL.Atomic).id, EntityKind.Concept);
            for (int i = 0; i <= 1; i++)
            {
                side[i].Append("(?X" + i.ToString() + " rdf:type <" + conc + ">)");
            }

            int varid = 0;
            foreach (var r in e.Roles)
            {
                for (int i = 0; i <= 1; i++)
                {
                    side[i].Append(", ");

                    if (r is CNL.DL.Atomic)
                    {
                        var q = owlNC.getIRIFromId((r as CNL.DL.Atomic).id, EntityKind.Role);
                        side[i].Append("(?X" + i.ToString() + " <" + q + "> " + "?Y" + varid.ToString() + "X" + i.ToString() + ")");
                    }
                    else if (r is CNL.DL.RoleInversion)
                    {
                        var rr = (r as CNL.DL.RoleInversion).R;
                        if (rr is CNL.DL.Atomic)
                        {
                            var q = owlNC.getIRIFromId((rr as CNL.DL.Atomic).id, EntityKind.Role);
                            side[i].Append("(?Y" + varid.ToString() + "X" + i.ToString() + " <" + q + "> ?X" + i.ToString() + ")");
                        }
                    }
                }
                varid++;
            }
            foreach (var r in e.DataRoles)
            {
                for (int i = 0; i <= 1; i++)
                {
                    side[i].Append(", ");

                    if (r is CNL.DL.Atomic)
                    {
                        var q = owlNC.getIRIFromId((r as CNL.DL.Atomic).id, EntityKind.DataRole);
                        side[i].Append("(?X" + i.ToString() + " <" + q + "> " + "?Y" + varid.ToString() + "X" + i.ToString() + ")");
                    }
                }
                varid++;
            }

            // for sameas

            var idx = Interlocked.Increment(ref prp_key_cnt).ToString().ToString();
            sb.Append("[prp-key-1-" + idx + ": ");
            id2stmt.Add("prp-key-1-" + idx, e);

            sb.Append(side.First() + "," + side.Last() + ", notEqual(?X0, ?X1)");
            for (int z = 0; z < varid; z++)
            {
                sb.Append(", equal(");
                for (int i = 0; i <= 1; i++)
                {
                    if (i > 0)
                        sb.Append(" ,");
                    sb.Append("?Y" + z.ToString() + "X" + i.ToString());
                }
                sb.Append(")");
            }
            sb.Append(" -> (?X0 owl:sameAs ?X1)");
            appendDebugString(sb, e);
            sb.AppendLine("]");

            //for different

            var idx2 = Interlocked.Increment(ref prp_key_cnt).ToString();
            sb.Append("[prp-key-2-" + idx2 + ": ");
            id2stmt.Add("prp-key-2-" + idx2, e);

            sb.Append(side.First() + "," + side.Last() + ", notEqual(?X0, ?X1)");
            sb.Append(", pairwizeDifferentAtleastOnce(");
            bool first = true;
            for (int z = 0; z < varid; z++)
            {
                for (int i = 0; i <= 1; i++)
                {
                    if (!first)
                        sb.Append(" ,");
                    else
                        first = false;
                    sb.Append("?Y" + z.ToString() + "X" + i.ToString());
                }
            }

            sb.Append(")");
            sb.Append(" -> (?X0 owl:differentFrom ?X1)");
            appendDebugString(sb, e);
            sb.AppendLine("]");

            return null;
        }

        bool inSwrlBody = false;
        public override object Visit(SwrlStatement e)
        {
            var id = Interlocked.Increment(ref swrl_cnt).ToString();
            id2stmt.Add("swrl-" + id, e);
            string iid = null;
            if (model != null)
            {
                //                iid = model.createTypedLiteral("\"" + id.ToString() + "\"", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
                DL.Serializer ser = new Serializer();
                var dl = ser.Serialize(e);
                iid = model.createTypedLiteral("\'" + dl.Replace("\'", "\\\'").ToString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
            }
            if (e.modality == Statement.Modality.IS)
            {
                sb.Append("[swrl-" + id + ": ");
                inSwrlBody = true;
                e.slp.accept(this);
                sb.Append(" -> ");
                if (model != null && debugSWRL)
                    sb.Append(" debugTraceBuiltIn (" + iid + "),");
                inSwrlBody = false;
                e.slc.accept(this);
                sb.AppendLine("]");
            }
            else if(modalCheckerRules)
            {
                bool normal = (e.modality == Statement.Modality.CAN || e.modality == Statement.Modality.SHOULD || e.modality == Statement.Modality.MUST);
                sb.Append("[swrl-modal-body-" + id + ": ");
                inSwrlBody = true;
                e.slp.accept(this);
                sb.Append(" -> ");
                if (model != null)
                    sb.Append(" modalCheckerBuiltIn (0," + (normal ? "0," : "1,") + iid + ")");
                inSwrlBody = false;
                sb.AppendLine("]");

                sb.Append("[swrl-modal-head-" + id + ": ");
                inSwrlBody = true;
                e.slp.accept(this);
                sb.Append(", ");
                e.slc.accept(this);
                sb.Append(" -> ");
                if (model != null)
                    sb.Append(" modalCheckerBuiltIn (1," + (normal ? "0," : "1,") + iid + ")");
                inSwrlBody = false;
                sb.AppendLine("]");
            }
            return null;
        }

        public Dictionary<int, SwrlIterate> TheIterators = new Dictionary<int, SwrlIterate>();

        public override object Visit(SwrlIterate rule)
        {
            if (runExeRules)
            {
                var idx = Interlocked.Increment(ref swrl_cnt);
                id2stmt.Add("swrl-" + idx.ToString(), rule);
                sb.Append("[swrl-" + idx.ToString() + ": ");
                rule.slp.accept(this);
                sb.Append(" -> ");
                if (model != null)
                {
                    DL.Serializer ser = new Serializer();
                    var dl = ser.Serialize(rule);
                    var iid = model.createTypedLiteral("\'" + dl.Replace("\'", "\\\'").ToString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
                    if(debugExeRules)
                        sb.Append(" debugTraceBuiltIn (" + iid + "),");
                }
                sb.Append("swrlIterator(" + idx.ToString() + ")");
                sb.AppendLine("]");

                TheIterators.Add(idx, rule);
            }
            return null;
        }
        
        public override object Visit(SwrlItemList e)
        {
            bool firstOne = true;
            foreach (var i in e.list)
            {
                if (firstOne)
                    firstOne = false;
                else
                    sb.Append(", ");
                i.accept(this);
            }
            return null;
        }

        //SwrlItems

        public override object Visit(SwrlInstance e)
        {
            if (e.C is CNL.DL.Atomic)
            {
                sb.Append("(");
                var inst = e.I.accept(this);
                sb.Append(inst);
                sb.Append(" rdf:type ");
                var cls = owlNC.getIRIFromId((e.C as CNL.DL.Atomic).id, EntityKind.Concept);
                sb.Append("<" + cls + ">");
                sb.Append(")");
                if (!inSwrlBody && !swrlOnly)
                {
                    sb.Append("(");
                    sb.Append(inst);
                    sb.Append(" rdf:type ");
                    sb.Append("owl:NamedIndividual");
                    sb.Append(")");
                    sb.Append("(");
                    sb.Append("<" + cls + ">");
                    sb.Append(" rdf:type ");
                    sb.Append("owl:Class");
                    sb.Append(")");
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
            sb.Append("(");
            var inst = e.I.accept(this);
            sb.Append(inst);
            var rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
            sb.Append(" <");
            sb.Append(rel);
            sb.Append("> ");
            var jnst = e.J.accept(this);
            sb.Append(jnst);
            sb.Append(")");
            if (!inSwrlBody && !swrlOnly)
            {
                sb.Append("(");
                sb.Append( inst );
                sb.Append(" rdf:type ");
                sb.Append("owl:NamedIndividual");
                sb.Append(")");
                sb.Append("(");
                sb.Append( jnst );
                sb.Append(" rdf:type ");
                sb.Append("owl:NamedIndividual");
                sb.Append(")");
                sb.Append("(");
                sb.Append("<" + rel + ">");
                sb.Append(" rdf:type ");
                sb.Append("owl:ObjectProperty");
                sb.Append(")");
            }
            return null;
        }

        public override object Visit(SwrlSameAs e)
        {
            var inst = e.I.accept(this);
            var jnst = e.J.accept(this);
            if (inSwrlBody)
            {
                sb.Append("equal(");
                sb.Append(inst);
                sb.Append(",");
                sb.Append(jnst);
                sb.Append(")");
            }
            else
            {
                sb.Append("(");
                sb.Append(inst);
                sb.Append(", owl:sameAs, ");
                sb.Append(jnst);
                sb.Append(")");

                if (!swrlOnly)
                {
                    sb.Append("(");
                    sb.Append(inst);
                    sb.Append(" rdf:type ");
                    sb.Append("owl:NamedIndividual");
                    sb.Append(")");
                    sb.Append("(");
                    sb.Append(jnst);
                    sb.Append(" rdf:type ");
                    sb.Append("owl:NamedIndividual");
                    sb.Append(")");
                }
            }
            return null;
        }

        public override object Visit(SwrlDifferentFrom e)
        {
            var inst = e.I.accept(this);
            var jnst = e.J.accept(this);
            if (inSwrlBody)
            {
                sb.Append("notEqual(");
                sb.Append(inst);
                sb.Append(",");
                sb.Append(jnst);
                sb.Append(")");
            }
            else
            {
                sb.Append("(");
                sb.Append(inst);
                sb.Append(", owl:differentFrom, ");
                sb.Append(jnst);
                sb.Append(")");

                if (!swrlOnly)
                {
                    sb.Append("(");
                    sb.Append(inst);
                    sb.Append(" rdf:type ");
                    sb.Append("owl:NamedIndividual");
                    sb.Append(")");
                    sb.Append("(");
                    sb.Append(jnst);
                    sb.Append(" rdf:type ");
                    sb.Append("owl:NamedIndividual");
                    sb.Append(")");
                }
            }
            return null;
        }

        public override object Visit(SwrlDataRange e)
        {
            using (curFacetVal.set(e.DO.accept(this).ToString()))
            {
                sb.Append(e.B.accept(this));
            }
            return null;
        }

        public override object Visit(SwrlDataProperty e)
        {
            sb.Append("(");
            var inst = e.IO.accept(this);
            sb.Append(inst);
            var rel = owlNC.getIRIFromId(e.R, EntityKind.Role);
            sb.Append(" <");
            sb.Append(rel);
            sb.Append("> ");
            sb.Append(e.DO.accept(this));
            sb.Append(")");
            if (!inSwrlBody && !swrlOnly)
            {
                sb.Append("(");
                sb.Append(inst);
                sb.Append(" rdf:type ");
                sb.Append("owl:NamedIndividual");
                sb.Append(")");
                sb.Append("(");
                sb.Append("<" + rel + ">");
                sb.Append(" rdf:type ");
                sb.Append("owl:DatatypeProperty");
                sb.Append(")");
            }
            return null;
        }

        void SwrlBuiltInNoImpl(SwrlBuiltIn e)
        {
            throw new NotImplementedException("Builtin :" + e.builtInName + " is not supported yet.");
        }

        void AppendComparator(StringBuilder sb, string comparator, string A, string B)
        {
            if (comparator == "≤")
                sb.Append("le(" + A + ", " + B + ")");
            else if (comparator == "<")
                sb.Append("lessThan(" + A + ", " + B + ")");
            else if (comparator == "≥")
                sb.Append("ge(" + A + ", " + B + ")");
            else if (comparator == ">")
                sb.Append("greaterThan(" + A + ", " + B + ")");
            else if (comparator == "=")
                sb.Append("equal(" + A + ", " + B + ")");
            else if (comparator == "≠")
                sb.Append("notEqual(" + A + ", " + B + ")");
            else
                throw new NotImplementedException("Builtin :" + comparator + " is not supported yet.");
        }

        string mapCode(string code)
        {
            switch (code)
            {
                case "≤": return "<=";
                case "≥": return ">=";
                case "≠": return "<>";
                default: return code;
            }
        }

        public override object Visit(SwrlBuiltIn e)
        {
            if (model == null)
                return null;
            var builtInName = e.builtInName;
            var btag = KeyWords.Me.GetTag(mapCode(builtInName));
            if (btag == "CMP" || btag == "EQ")
            {
                var A = e.Values[1];
                var B = e.Values[0];
                AppendComparator(sb, e.builtInName, A.accept(this).ToString(), B.accept(this).ToString());
            }
            else
            {
                var lst = e.Values[e.Values.Count-1].accept(this).ToString();;

                for (int i = 0; i < e.Values.Count - 1; i++)
                {
                    var dn = e.Values[i].accept(this).ToString();
                    lst += ", ";
                    lst += dn;
                }

                if (builtInName == "plus" || builtInName == "times" || builtInName == "followed-by")
                {
                    if (builtInName == "followed-by")
                        sb.Append("concatenateStrings(" + lst + ")");
                    else if (builtInName == "plus")
                        sb.Append("sumNumbers(" + lst + ")");
                    else if (builtInName == "times")
                        sb.Append("multiplyNumbers(" + lst + ")");
                    else
                        SwrlBuiltInNoImpl(e);
                }
                else if (builtInName == "datetime" || builtInName=="duration")
                {
                    if (builtInName == "datetime")
                        sb.Append("createDatetime(" + lst + ")");
                    else if (builtInName == "duration")
                        sb.Append("createDuration(" + lst + ")");
                }
                else if (e.builtInName == "alpha-representation-of")
                {
                    sb.Append("alpha(" + lst + ")");
                }
                else if (e.builtInName == "annotation")
                {
                    sb.Append("annotation(" + lst + ")");
                }
                else if (e.builtInName == "execute")
                {
                    sb.Append("executeExternalFunction(" + lst + ")");
                }
                else if (builtInName == "translated" || builtInName == "replaced")
                {
                    if (builtInName == "translated")
                        sb.Append("complexStringOperation('translate'," + lst + ")");
                    else if (builtInName == "replaced")
                        sb.Append("complexStringOperation('replace'," + lst + ")");
                }
                else if (builtInName == "from" || builtInName == "before" || builtInName == "after")
                {
                    if (e.builtInName == "from")
                        sb.Append("simpleStringOperation('substring'," + lst + ")");
                    else if (e.builtInName == "before")
                        sb.Append("simpleStringOperation('substring-before'," + lst + ")");
                    else if (e.builtInName == "after")
                        sb.Append("simpleStringOperation('substring-after'," + lst + ")");
                    else
                        SwrlBuiltInNoImpl(e);
                }
                else if (e.Values.Count == 3)
                {
                    if (e.builtInName == "minus")
                        sb.Append("mathBinary('subtract'," + lst + ")");
                    else if (e.builtInName == "divided-by")
                        sb.Append("mathBinary('divide'," + lst + ")");
                    else if (e.builtInName == "integer-divided-by")
                        sb.Append("mathBinary('int-divide'," + lst + ")");
                    else if (e.builtInName == "modulo")
                        sb.Append("mathBinary('modulo'," + lst + ")");
                    else if (e.builtInName == "raised-to-the-power-of")
                        sb.Append("mathBinary('power'," + lst + ")");
                    else if (e.builtInName == "rounded-with-the-precision-of")
                        sb.Append("mathBinary('round-half-to-even'," + lst + ")");
                    else
                        SwrlBuiltInNoImpl(e);
                }
                else if (e.Values.Count == 2)
                {
                    if (e.builtInName == "not")
                        sb.Append("booleanUnary('not'," + lst + ")");
                    else if (e.builtInName == "minus")
                        sb.Append("mathUnary('minus'," + lst + ")");
                    else if (e.builtInName == "absolute-value-of")
                        sb.Append("mathUnary('absolute'," + lst + ")");
                    else if (e.builtInName == "ceiling-of")
                        sb.Append("mathUnary('ceiling'," + lst + ")");
                    else if (e.builtInName == "floor-of")
                        sb.Append("mathUnary('floor'," + lst + ")");
                    else if (e.builtInName == "round-of")
                        sb.Append("mathUnary('round'," + lst + ")");
                    else if (e.builtInName == "sine-of")
                        sb.Append("mathUnary('sine'," + lst + ")");
                    else if (e.builtInName == "cosine-of")
                        sb.Append("mathUnary('cosine'," + lst + ")");
                    else if (e.builtInName == "tangent-of")
                        sb.Append("mathUnary('tangent'," + lst + ")");
                    else if (e.builtInName == "case-ignored")
                        sb.Append("stringUnary('case-ignore'," + lst + ")");
                    else if (e.builtInName == "length-of")
                        sb.Append("stringUnary('length'," + lst + ")");
                    else if (e.builtInName == "space-normalized")
                        sb.Append("stringUnary('space-normalize'," + lst + ")");
                    else if (e.builtInName == "upper-cased")
                        sb.Append("stringUnary('upper-case'," + lst + ")");
                    else if (e.builtInName == "lower-cased")
                        sb.Append("stringUnary('lower-case'," + lst + ")");
                    else if (e.builtInName == "contains-string")
                        sb.Append("stringUnary('contains'," + lst + ")");
                    else if (e.builtInName == "starts-with-string")
                        sb.Append("stringUnary('starts-with'," + lst + ")");
                    else if (e.builtInName == "ends-with-string")
                        sb.Append("stringUnary('ends-with'," + lst + ")");
                    else if (e.builtInName == "matches-string")
                        sb.Append("stringUnary('matches'," + lst + ")");
                    else if (e.builtInName == "contains-case-ignored-string")
                        sb.Append("stringUnary('contains-case-ignore'," + lst + ")");
                    else if (e.builtInName == "sounds-like-string")
                        sb.Append("stringUnary('sounds-like'," + lst + ")");
                    else
                        SwrlBuiltInNoImpl(e);
                }
                else
                    SwrlBuiltInNoImpl(e);

            }
            return null;
        }

        //SwrlNodes

        public override object Visit(SwrlIVal e)
        {
            DlName dl = new DlName() { id = e.I };
            var dlp = dl.Split();
            if(char.IsLower(dlp.name[0]))
                return "<" + owlNC.getIRIFromId(e.I, EntityKind.Role) + ">";
            else
                return "<" + owlNC.getIRIFromId(e.I, EntityKind.Instance) + ">";
        }

        public override object Visit(SwrlIVar e)
        {
            return "?" + e.VAR.Replace("-", "_");
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

        Literal getLiteralVal(Value v)
        {
            if (model == null) return null;

            if (v is CNL.DL.Bool) return model.createTypedLiteral(v.ToBool()?"true":"false", org.apache.jena.datatypes.xsd.XSDDatatype.XSDboolean);
            if (v is CNL.DL.String) return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring);
            if (v is CNL.DL.Float) return model.createTypedLiteral(v.getVal(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdouble);
            if (v is CNL.DL.Number) return model.createTypedLiteral(new java.lang.Integer(v.ToInt()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDinteger);
            if (v is CNL.DL.DateTimeVal) return model.createTypedLiteral(completeDTMVal(v.ToStringExact()), org.apache.jena.datatypes.xsd.XSDDatatype.XSDdateTime);
            if (v is CNL.DL.Duration) return model.createTypedLiteral(v.ToStringExact(), org.apache.jena.datatypes.xsd.XSDDatatype.XSDduration);

            return model.createTypedLiteral(v.ToString()); //TODO xsd:date i inne typy
        }

        string getLiteralVal2(Value v)
        {
            var vv = getLiteralVal(v);
            if (vv == null)
                return null;
            var l = vv.ToString();
            var p = l.LastIndexOf('^');
            if (p == -1)
                    return vv.toString();
            else
                return "'" + l.Substring(0, p - 1).Replace("\\", "\\\\") + "'^^" + l.Substring(p + 1).Replace("http://www.w3.org/2001/XMLSchema#", "xsd:");
        }

        public override object Visit(SwrlDVal e)
        {
            return getLiteralVal2(e.Val);
        }

        public override object Visit(SwrlDVar e)
        {
            return "?" + e.VAR.Replace("-", "_");
        }
        //bounds

        public override object Visit(BoundFacets e)
        {
            return string.Join(", ", from f in e.FL.List select f.accept(this).ToString());
        }

        bool boundNot = false;

        public override object Visit(BoundNot e)
        {
            boundNot = !boundNot;
            var r = e.B.accept(this);
            boundNot = !boundNot;
            return r;
        }

        public override object Visit(BoundAnd e)
        {
            if (!boundNot)
            {
                return string.Join(", ", from f in e.List select f.accept(this).ToString());
            }
            else
            {
                NotInProfile();
            }
            return base.Visit(e);
        }

        public override object Visit(BoundOr e)
        {
            if (boundNot)
            {
                return string.Join(", ", from f in e.List select f.accept(this).ToString());
            }
            else
            {
                NotInProfile();
            }
            return base.Visit(e);
        }

        public override object Visit(BoundVal e)
        {
            var v = curFacetVal.get();
            var val = getLiteralVal2(e.V);
            if ((!boundNot && e.Kind == "≠") || (boundNot && e.Kind == "="))
            {
                return "notEqual(" + v + ", " + val + ")";
            }
            if ((boundNot && e.Kind == "≠") || (!boundNot && e.Kind == "="))
            {
                return "equal(" + v + ", " + val + ")";
            }
            NotInProfile();
            return base.Visit(e);
        }

        public override object Visit(TotalBound e)
        {
            NotInProfile();
            return base.Visit(e);
        }

        public override object Visit(DTBound e)
        {
            NotInProfile();
            return base.Visit(e);
        }

        public override object Visit(TopBound e)
        {
            NotInProfile();
            return base.Visit(e);
        }

        public override object Visit(ValueSet e)
        {
            var v = curFacetVal.get();
            if (!boundNot)
            {
                return string.Join(", ", from f in e.Values select "equal(" + v + ", " + getLiteralVal2(f) + ")");
            }
            else if (e.Values.Count == 1)
            {
                var val = getLiteralVal2(e.Values.First());
                return "notEqual(" + v + ", " + val + ")";
            }
            else
                NotInProfile();
            return base.Visit(e);
        }

        VisitingParam<string> curFacetVal = new VisitingParam<string>(null);

        public override object Visit(Facet e)
        {
            var v = curFacetVal.get();
            var val = getLiteralVal2(e.V);
            if (val == null)
                return "";

            if (e.Kind == "≤")
                return (boundNot ? "ge" : "le") + "(" + v + ", " + val + ")";
            else if (e.Kind == "<")
                return (boundNot ? "greaterThan" : "lessThan") + "(" + v + ", " + val + ")";
            else if (e.Kind == "≥")
                return (boundNot ? "le" : "ge") + "(" + v + ", " + val + ")";
            else if (e.Kind == ">")
                return (boundNot ? "lessThan" : "greaterThan") + "(" + v + ", " + val + ")";
            else if (e.Kind == "#" && !boundNot)
                return "regex(" + val + ", " + v + ")";
            else if (e.Kind == "<->" && !boundNot)
                return "stringLength(" + val + ", '=', " + v + ")";
            else if (e.Kind == "<-> ≥" && !boundNot)
                return "stringLength(" + val + ", '≥', " + v + ")";
            else if (e.Kind == "<-> ≤" && !boundNot)
                return "stringLength(" + val + ", '≤', " + v + ")";
            else if (e.Kind == "<->" && !boundNot)
                return string.Format("stringLength(" + val + ", '=', {0})", v);
            else if (e.Kind == "<-> ≥" && !boundNot)
                return string.Format("stringLength(" + val + ", '≥', {0})", v);
            else if (e.Kind == "<-> ≤" && !boundNot)
                return string.Format("stringLength(" + val + ", '≤', {0})", v);

            NotInProfile();

            return base.Visit(e);
        }

        public Dictionary<int, Tuple<string, List<IExeVar>>> TheRules = new Dictionary<int, Tuple<string, List<IExeVar>>>();

        public override object Visit(ExeStatement rule)
        {
            if (runExeRules)
            {
                var idx = Interlocked.Increment(ref swrl_cnt);
                id2stmt.Add("swrl-" + idx.ToString(), rule);
                sb.Append("[swrl-" + idx.ToString() + ": ");
                rule.slp.accept(this);
                sb.Append(" -> ");
                if (model != null)
                {
                    DL.Serializer ser = new Serializer();
                    var nrule = new CNL.DL.ExeStatement(null) { args = rule.args, exe = "<?...?>", slp = rule.slp };
                    var dl = ser.Serialize(nrule);
                    var iid = model.createTypedLiteral("\'" + dl.Replace("\'", "\\\'").ToString() + "\'", org.apache.jena.datatypes.xsd.XSDDatatype.XSDstring).toString();
                    if(debugExeRules)
                        sb.Append(" debugTraceBuiltIn (" + iid + "),");
                }

                var lst = rule.exe;

                for (int i = 0; i < rule.args.list.Count ; i++)
                {
                    var dn = rule.args.list[i].accept(this).ToString();
                    lst += ", ";
                    lst += dn;
                }

                sb.Append("executeExternalRule(" +lst + ")");
                sb.AppendLine("]");

            }
            return null;
        }
    }
}
    