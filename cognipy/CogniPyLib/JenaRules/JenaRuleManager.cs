using CogniPy.CNL.DL;
using CogniPy.Configuration;
using org.apache.jena.graph;
using org.apache.jena.graph.impl;
using org.apache.jena.reasoner.rulesys;
using org.apache.jena.util;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Numerics;
using System.Runtime.CompilerServices;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;

namespace CogniPy.Executing.HermiT
{

    public static class RuleExtensions
    {
        // from https://github.com/datastax/csharp-driver/blob/353f65ae28232b7dee9a3c91249faa586de875a1/src/Cassandra/DecimalTypeAdapter.cs
        public static decimal BigDecimalToDecimal( byte[] Unscaled, int Scale)
        {
            var bigintBytes = new byte[Unscaled.Length];
            Array.Copy(Unscaled, 0, bigintBytes, 0, bigintBytes.Length);

            Array.Reverse(bigintBytes);
            var bigInteger = new BigInteger(bigintBytes);
            var isNegative = bigInteger < 0;

            bigInteger = BigInteger.Abs(bigInteger);
            bigintBytes = bigInteger.ToByteArray();
            if (bigintBytes.Length > 13 || (bigintBytes.Length == 13 && bigintBytes[12] != 0))
            {
                throw new ArgumentOutOfRangeException(
                    "decimalBuf",
                    "this java.math.BigDecimal is too big to fit into System.Decimal");
            }

            var intArray = new int[3];
            Buffer.BlockCopy(bigintBytes, 0, intArray, 0, Math.Min(12, bigintBytes.Length));

            return new decimal(intArray[0], intArray[1], intArray[2], isNegative, (byte) Scale);
        }

        public static object getValFromJenaLiteral(object val)
        {
            if (val is java.lang.Double)
                val = (val as java.lang.Double).doubleValue();
            else if (val is java.lang.Float)
                val = (val as java.lang.Float).doubleValue();
            else if (val is float)
                val = (double)val;
            else if (val is java.math.BigDecimal)
                val = BigDecimalToDecimal(((java.math.BigDecimal)val).unscaledValue().toByteArray(), ((java.math.BigDecimal)val).scale());
            else if (val is java.lang.Number)
                val = (val as java.lang.Number).intValue();
            else if (val is java.lang.Integer)
                val = (val as java.lang.Integer).intValue();
            else if (val is java.lang.Long)
                val = (val as java.lang.Long).intValue();
            else if (val is long)
                val = (int)val;
            else if (val is java.lang.String)
                val = val.ToString();
            else if (val is java.lang.Boolean)
                val = (val as java.lang.Boolean).booleanValue();
            else if (val is org.apache.jena.datatypes.xsd.XSDDateTime)
                val = DateTimeOffset.Parse((val as org.apache.jena.datatypes.xsd.XSDDateTime).toString());
            else if (val.ToString().EndsWith(xsdDayTimeDuration))
                val = System.Xml.XmlConvert.ToTimeSpan(val.ToString().Substring(0, val.ToString().Length - xsdDayTimeDuration.Length));

            return val;
        }

        const string xsdDayTimeDuration = "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration";

        public static bool isSimpleJenaObject(object o)
        {
            return isInteger(o) || isDouble(o) || o is string || o is bool || o is DateTimeOffset || o is TimeSpan;
        }

        public static bool isDouble(object val)
        {
            return (val is double || val is float);
        }
        public static bool isInteger(object v1)
        {
            return v1 is int || v1 is long || v1 is short || v1 is uint || v1 is ulong || v1 is ushort;
        }
        public static string lex(org.apache.jena.graph.Node n, Builtin bi, RuleContext context)
        {
            if (n.isBlank())
            {
                return n.getBlankNodeLabel();
            }
            else if (n.isURI())
            {
                return n.getURI();
            }
            else if (n.isLiteral())
            {
                return n.getLiteralLexicalForm();
            }
            else
            {
                throw new BuiltinException(bi, context, "Illegal node type: " + n);
            }
        }
    }

    public class SwrlIterator : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "swrlIterator";
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            int ruleIdx = (this.getArg(0, args, context).getLiteralValue() as java.lang.Integer).intValue();
            var rule = JenaRuleManager.GetReasonerExt(context).SwrlIterators[ruleIdx];


            Dictionary<string, int> varNameToIndex = new Dictionary<string, int>();
            var bodyL = context.getRule().bodyLength();
            for (int i = 0; i < bodyL; i++)
            {
                var rV = new List<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
                {
                    var elem = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.TriplePattern;
                    if (elem != null)
                    {

                        var o = elem.getObject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                        var s = elem.getSubject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                        if (o != null)
                            rV.Add(o);
                        if (s != null)
                            rV.Add(s);
                    }
                }
                {
                    var elem = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.rulesys.Functor;
                    if (elem != null)
                    {

                        foreach (var v in elem.getArgs())
                        {
                            var o = v as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                            if (o != null)
                                rV.Add(o);
                        }
                    }
                }

                foreach (var r in rV)
                {
                    if (!varNameToIndex.ContainsKey(r.getName()))
                        varNameToIndex.Add(r.getName(), r.getIndex());
                }
            }

            var env = (context.getEnv() as org.apache.jena.reasoner.rulesys.impl.BindingVector).getEnvironment();
            object[] vals = new object[env.Length];
            for (int i = 0; i < env.Length; i++)
            {
                if (env[i].isURI())
                    vals[i] = ext.TheInvTransform.renderEntity(env[i].getURI(), ARS.EntityKind.Instance);
                else if (env[i].isLiteral())
                    vals[i] = RuleExtensions.getValFromJenaLiteral(env[i].getLiteralValue());
            }

            var iteratorVarP = rule.vars.list.First();
            var collectionVarP = rule.vars.list.Last();

            if (collectionVarP.isVar() && iteratorVarP.isVar())
            {
                var cv = "?" + (collectionVarP as ISwrlVar).getVar().Replace("-", "_");
                var iv = "?" + (iteratorVarP as ISwrlVar).getVar().Replace("-", "_");
                var collectionVar = vals[varNameToIndex[cv]];

                ext.TheSwrlIterateProc.context = context;
                ext.TheSwrlIterateProc.allVars = vals;
                ext.TheSwrlIterateProc.iterVar = iv;
                ext.TheSwrlIterateProc.varNameToIndex = varNameToIndex;

                foreach (var it in ext.Outer.ItarateOver(JenaRuleManager.getObject(collectionVar.ToString())))
                {
                    ext.TheSwrlIterateProc.iterVal = it;
                    ext.TheSwrlIterateProc.Visit(rule.slc);
                }
            }
        }
    }

    public class ExecuteExternalRule : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "executeExternalRule";
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            var method = getArg(0, args, context).getLiteralValue().ToString();
            List<object> vals = new List<object>();
            //vals.Add(ext.TheAccessObject);
            for (int i = 1; i < args.Length; i++)
            {
                var n = getArg(i, args, context);
                if (n.isLiteral())
                    vals.Add(JenaRuleManager.getObject(n));
                else if (n.isURI())
                {
                    var type = ext.GetTypeOfNode(context, n);
                    if (ext.TheAccessObject.PassParamsInCNL)
                        vals.Add(new CogniPy.GraphEntity() { Name = ext.TheAccessObject.CnlFromUri(n.getURI(), type), Kind = type });
                    else
                        vals.Add(new CogniPy.GraphEntity() { Name = n.getURI().ToString(), Kind = type });
                }
                else
                    return;
            }

            var mth = ext.Outer.GetMethod(method);
            mth.Invoke(ext.Outer, vals.ToArray());
        }
    }

    public class ReactiveCSharpRule : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "reactiveRule";
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            int ruleIdx = (this.getArg(0, args, context).getLiteralValue() as java.lang.Integer).intValue();
            var rule = JenaRuleManager.GetReasonerExt(context).ExeRules[ruleIdx];
            var ruleType = OWLServices.ReactiveRuleCompiler.LoadRuleType(rule.Item1.Substring(2, rule.Item1.Length - 4), "", rule.Item2);

            Dictionary<string, int> varNameToIndex = new Dictionary<string, int>();
            var bodyL = context.getRule().bodyLength();
            for (int i = 0; i < bodyL; i++)
            {
                var elem = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.TriplePattern;
                var o = elem.getObject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                var s = elem.getSubject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                var rV = new List<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
                if (o != null)
                    rV.Add(o);
                if (s != null)
                    rV.Add(s);
                foreach (var r in rV)
                {
                    if (!varNameToIndex.ContainsKey(r.getName()))
                        varNameToIndex.Add(r.getName(), r.getIndex());
                }
            }

            Dictionary<int, int> thosToThat = new Dictionary<int, int>();
            var ruleVars = rule.Item2;
            int idx = 0;
            foreach (var v in ruleVars)
            {
                if (v.isVar())
                {
                    var vn = "?" + (v as ISwrlVar).getVar().Replace("-", "_");
                    thosToThat.Add(idx, varNameToIndex[vn]);
                }
                idx++;
            }

            var env = (context.getEnv() as org.apache.jena.reasoner.rulesys.impl.BindingVector).getEnvironment();
            object[] vals = new object[env.Length];
            for (int i = 0; i < env.Length; i++)
            {
                if (env[i].isURI())
                    vals[thosToThat[i]] = ext.TheInvTransform.renderEntity(env[i].getURI(), ARS.EntityKind.Instance);
                else if (env[i].isLiteral())
                    vals[thosToThat[i]] = RuleExtensions.getValFromJenaLiteral(env[i].getLiteralValue());
            }


            dynamic ruleInstance = Activator.CreateInstance(ruleType);

            ruleInstance.Ontorion = ext.TheAccessObject;
            ruleInstance.Outer = ext.Outer;

            ruleInstance.Execute(vals, new Action<HashSet<string>>((kb) =>
                {
                    ext.TheAccessObject.KnowledgeInsert(kb);
                }),
                new Action<HashSet<string>>((kb) =>
                {
                    ext.TheAccessObject.KnowledgeDelete(kb);
                }),
                null);
        }
    }



    class PrintOutToConsole : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "printOutToConsole";
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            print(args, length, context);
            return true;
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            print(args, length, context);
        }

        public void print(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            for (int i = 0; i < length; i++)
            {
                Console.Write(PrintUtil.print(this.getArg(i, args, context)) + " ");
            }
            Console.WriteLine();
        }
    }

    public class DebugTraceBuiltIn : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "debugTraceBuiltIn";
        }

        public override int getArgLength()
        {
            return 1;
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            checkArgs(length, context);
            var n0 = getArg(0, args, context);
            var str = n0.getLiteralValue().ToString();

            Dictionary<int, string> indexToVarName = new Dictionary<int, string>();
            var bodyL = context.getRule().bodyLength();
            for (int i = 0; i < bodyL; i++)
            {
                var rV = new List<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
                var elem = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.TriplePattern;
                if (elem != null)
                {
                    var o = elem.getObject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                    var s = elem.getSubject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                    var p = elem.getPredicate() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                    if (o != null)
                        rV.Add(o);
                    if (s != null)
                        rV.Add(s);
                    if (p != null)
                        rV.Add(p);
                }
                else
                {
                    var func = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.rulesys.Functor;
                    if (func != null)
                    {
                        var argsX = func.getArgs();
                        for (int j = 0; j < func.getArgLength(); j++)
                        {
                            var o = argsX[j] as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                            if (o != null)
                                rV.Add(o);
                        }
                    }
                }
                foreach (var r in rV)
                {
                    if (!indexToVarName.ContainsKey(r.getIndex()))
                        indexToVarName.Add(r.getIndex(), r.getName().Replace("_", "-"));
                }
            }

            Dictionary<string, Tuple<string, object>> vals = new Dictionary<string, Tuple<string, object>>();

            var env = (context.getEnv() as org.apache.jena.reasoner.rulesys.impl.BindingVector).getEnvironment();
            for (int i = 0; i < env.Length; i++)
            {
                if (indexToVarName.ContainsKey(i))
                {
                    if (!vals.ContainsKey(indexToVarName[i]))
                    {
                        if (env[i].isURI())
                        {
                            var G = context.getGraph();
                            var ek = ARS.EntityKind.Concept;
                            if (G.contains(env[i], org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode()))
                                ek = ARS.EntityKind.Instance;
                            else if (G.contains(env[i], org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode()))
                                ek = ARS.EntityKind.Role;
                            else if (G.contains(env[i], org.apache.jena.vocabulary.RDF.type.asNode(), org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode()))
                                ek = ARS.EntityKind.DataRole;

                            vals.Add(indexToVarName[i], Tuple.Create<string, object>(ek.ToString(), ext.TheInvTransform.renderEntity(env[i].getURI(), ek)));
                        }
                        else if (env[i].isLiteral())
                            vals.Add(indexToVarName[i], Tuple.Create<string, object>(null, JenaRuleManager.getObject(env[i])));
                        else //unnamed entity detected
                            return;
                    }
                }
            }

            if (ext.DebugAction != null)
                ext.DebugAction(str, vals);
        }
    }

    public class JenaValue : DoubleLink
    {
        public bool IsInstance;
        public object Value;
    }

    public class ModalCheckerBuiltIn : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "modalCheckerBuiltIn";
        }

        public override int getArgLength()
        {
            return 3;
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            checkArgs(length, context);
            var tpy = (getArg(0, args, context).getLiteralValue() as java.lang.Integer).intValue();
            var normal = (getArg(1, args, context).getLiteralValue() as java.lang.Integer).intValue() == 0;
            var str = getArg(2, args, context).getLiteralValue().ToString();

            Dictionary<int, string> indexToVarName = new Dictionary<int, string>();
            var bodyL = context.getRule().bodyLength();
            for (int i = 0; i < bodyL; i++)
            {
                var rV = new List<org.apache.jena.reasoner.rulesys.Node_RuleVariable>();
                var elem = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.TriplePattern;
                if (elem != null)
                {
                    var o = elem.getObject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                    var s = elem.getSubject() as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                    if (o != null)
                        rV.Add(o);
                    if (s != null)
                        rV.Add(s);
                }
                else
                {
                    var func = context.getRule().getBodyElement(i) as org.apache.jena.reasoner.rulesys.Functor;
                    if (func != null)
                    {
                        var argsX = func.getArgs();
                        for (int j = 0; j < func.getArgLength(); j++)
                        {
                            var o = argsX[j] as org.apache.jena.reasoner.rulesys.Node_RuleVariable;
                            if (o != null)
                                rV.Add(o);
                        }
                    }
                }
                foreach (var r in rV)
                {
                    if (!indexToVarName.ContainsKey(r.getIndex()))
                        indexToVarName.Add(r.getIndex(), r.getName().Replace("_", "-"));
                }
            }


            LinkedDictionary<string, JenaValue> vals = new LinkedDictionary<string, JenaValue>();

            var env = (context.getEnv() as org.apache.jena.reasoner.rulesys.impl.BindingVector).getEnvironment();
            for (int i = 0; i < env.Length; i++)
            {
                if (indexToVarName.ContainsKey(i))
                {
                    if (!vals.ContainsKey(indexToVarName[i]))
                    {
                        if (env[i].isURI())
                            vals.Add(indexToVarName[i], new JenaValue() { IsInstance = true, Value = ext.TheAccessObject.CnlFromUri(env[i].getURI(), "instance") });
                        else if (env[i].isLiteral())
                            vals.Add(indexToVarName[i], new JenaValue() { IsInstance = false, Value = RuleExtensions.getValFromJenaLiteral(env[i].getLiteralValue()) });
                        else
                        {
                            ext.SetModalVals(tpy == 0, normal, ext.TheAccessObject.CnlFromDLString(str), new LinkedDictionary<string, JenaValue>());
                            return;
                        }
                    }
                }
            }

            ext.SetModalVals(tpy == 0, normal, ext.TheAccessObject.CnlFromDLString(str), vals);

        }

    }


    public class OntologyError : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "ontologyError";
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            var title = getArg(0, args, context).getLiteralValue().ToString();
            var message = getArg(1, args, context).getLiteralValue().ToString();

            var infgraph = context.getGraph();

            List<Tuple<object, string>> vals = new List<Tuple<object, string>>();
            for (int i = 2; i < args.Length; i++)
            {
                var n = args[i];
                if (n.isLiteral())
                    vals.Add(Tuple.Create<object, string>(JenaRuleManager.getObject(n), "value"));
                else if (n.isURI())
                {
                    var type = ext.GetTypeOfNode(context, n);
                    var name = ext.TheAccessObject.CnlFromUri(n.getURI(), type);
                    vals.Add(Tuple.Create<object, string>(name, type));
                }
            }
            ext.AddOntologyError(title, message, vals);
        }
    }

    class StringLength : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "stringLength";
        }

        public override int getArgLength()
        {
            return 3;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);
            var n0 = getArg(0, args, context);
            var n1 = getArg(1, args, context);
            var n2 = getArg(2, args, context);
            var len = (n0.getLiteralValue() as java.lang.Integer).intValue();
            var kind = n1.getLiteralValue().ToString();
            var str = n2.getLiteralValue().ToString();
            if (kind == "=")
                return str.Length == len;
            else if (kind == "≤")
                return str.Length <= len;
            else if (kind == "≥")
                return str.Length >= len;
            return false;
        }

    }

    class ExecuteExternalFunction : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "executeExternalFunction";
        }

        public override int getArgLength()
        {
            return 0;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            checkArgs(length, context);
            var result = getArg(0, args, context);
            var method = getArg(1, args, context).getLiteral().toString();
            List<object> vals = new List<object>();
            //vals.Add(ext.TheAccessObject);
            for (int i = 2; i < args.Length; i++)
            {
                var n = getArg(i, args, context);
                if (n.isLiteral())
                    vals.Add(JenaRuleManager.getObject(n));
                else if (n.isURI())
                {
                    var type = ext.GetTypeOfNode(context, n);
                    if (ext.TheAccessObject.PassParamsInCNL)
                        vals.Add(new CogniPy.GraphEntity() { Name = ext.TheAccessObject.CnlFromUri(n.getURI(), type), Kind = type });
                    else
                        vals.Add(new CogniPy.GraphEntity() { Name = n.getURI().ToString(), Kind = type });
                }
                else
                    return false;
            }

            var mth = ext.Outer.GetMethod(method);

            if (result.isLiteral())
            {
                var resu = JenaRuleManager.getObject(result);
                if (!(resu is bool) || (((bool)resu) != true))
                    vals.Add(resu);
                var res = mth.Invoke(ext.Outer, vals.ToArray());
                return (bool)res;
            }
            else
            {
                var res = mth.Invoke(ext.Outer, vals.ToArray());
                return context.getEnv().bind(result, JenaRuleManager.getLiteral(res));
            }
        }

    }


    class PairwizeDifferentAtleastOnce : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "pairwizeDifferentAtleastOnce";
        }

        public override bool isMonotonic()
        {
            return true;
        }
        public override bool isSafe()
        {
            return true;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            for (int i = 0; i < args.Length; i += 2)
            {
                var n0 = getArg(i, args, context);
                var n1 = getArg(i + 1, args, context);
                if (!n0.equals(n1))
                    return true;
            }
            return false;
        }

    }
    class SetupCommonToListAsSubject : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "setupCommonToListAsSubject";
        }

        public override int getArgLength()
        {
            return 3;
        }

        public override bool isMonotonic()
        {
            return true;
        }

        public override bool isSafe()
        {
            return true;
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);
            var n0 = getArg(0, args, context);
            var n1 = getArg(2, args, context);
            var n2 = getArg(1, args, context);
            var l = Util.convertList(n0, context);

            var infgraph = context.getGraph();
            var liter = l.iterator();
            Dictionary<string, org.apache.jena.graph.Node> nodes = new Dictionary<string, org.apache.jena.graph.Node>();
            HashSet<string> commons = new HashSet<string>();
            commons.Add(n2.toString());
            nodes.Add(n2.toString(), n2);
            liter.next();
            while (liter.hasNext())
            {
                var x = liter.next() as org.apache.jena.graph.Node;
                var ci = infgraph.find(null, org.apache.jena.vocabulary.RDF.Nodes.type, x);
                var hs = new HashSet<string>();
                while (ci.hasNext())
                {
                    var trip = ci.next() as org.apache.jena.graph.Triple;
                    var nod = trip.getSubject();
                    hs.Add(nod.toString());
                }

                commons.IntersectWith(hs);
                ci.close();
            }

            foreach (var c in commons)
            {
                var t = new Triple(nodes[c], org.apache.jena.vocabulary.RDF.Nodes.type, n1);
                if (!context.contains(t))
                {

                    context.add(t);
                    var infGraph = (ForwardRuleInfGraphI)context.getGraph();
                    if (infGraph.shouldLogDerivations())
                    {
                        var rule = context.getRule();
                        java.util.List matchList = null;
                        // Create derivation record
                        matchList = new java.util.ArrayList(rule.bodyLength());
                        for (int i = 0; i < rule.bodyLength(); i++)
                        {
                            Object clause = rule.getBodyElement(i);
                            if (clause is org.apache.jena.reasoner.TriplePattern)
                            {
                                var trp = context.getEnv().instantiate((org.apache.jena.reasoner.TriplePattern)clause);
                                matchList.add(trp);
                            }
                        }
                        infGraph.logDerivation(t, new RuleDerivation(context.getRule(), t, matchList, infGraph));
                    }
                }
            }
        }

    }

    class ListAllEntriesAre : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "listAllEntriesAre";
        }

        public override int getArgLength()
        {
            return 2;
        }

        public override bool isMonotonic()
        {
            return true;
        }
        public override bool isSafe()
        {
            return true;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);
            var n0 = getArg(0, args, context);
            var n1 = getArg(1, args, context);
            var l = Util.convertList(n1, context);
            var liter = l.iterator();
            bool allOk = true;
            var infgraph = context.getGraph();
            while (liter.hasNext())
            {
                var x = liter.next() as org.apache.jena.graph.Node;

                if (!infgraph.contains(n0, org.apache.jena.vocabulary.RDF.Nodes.type, x))
                {
                    allOk = false;
                    break;
                }
            }

            return allOk;
        }

        public override void headAction(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var infGraph = (ForwardRuleInfGraphI)context.getGraph();
            if (infGraph.shouldLogDerivations())
            {
                var rule = context.getRule();
                java.util.List matchList = null;
                // Create derivation record
                matchList = new java.util.ArrayList(rule.bodyLength());
                var n0 = getArg(0, args, context);
                var n1 = getArg(1, args, context);
                var n2 = getArg(2, args, context);
                var l = Util.convertList(n1, context);
                var liter = l.iterator();
                var infgraph = context.getGraph();
                while (liter.hasNext())
                {
                    var x = liter.next() as org.apache.jena.graph.Node;

                    if (infgraph.contains(n0, org.apache.jena.vocabulary.RDF.Nodes.type, x))
                        matchList.add(new Triple(n0, org.apache.jena.vocabulary.RDF.Nodes.type, x));
                    else
                        return;
                }
                Triple t = new Triple(n0, org.apache.jena.vocabulary.RDF.Nodes.type, n2);
                infGraph.logDerivation(t, new RuleDerivation(context.getRule(), t, matchList, infGraph));
            }
        }
    }


    class ListTestSubjectPairewise : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "listTestSubjectPairewise";
        }

        public override int getArgLength()
        {
            return 2;
        }

        public override bool isMonotonic()
        {
            return true;
        }
        public override bool isSafe()
        {
            return true;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);
            var n0 = getArg(0, args, context);
            var n1 = getArg(1, args, context);
            var l = Util.convertList(n1, context);
            var liter = l.iterator();
            bool allOk = true;
            while (liter.hasNext())
            {
                var x = liter.next() as org.apache.jena.graph.Node;
                var a = Util.getPropValue(n0, x, context);
                var b = Util.getPropValue(n1, x, context);
                if (a == null || b == null || !a.equals(b))
                {
                    allOk = false;
                    break;
                }
            }
            return allOk;
        }

    }

    class ListAnyTwoEqualMembers : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "listAnyTwoEqualMembers";
        }

        public override int getArgLength()
        {
            return 1;
        }

        public override bool isMonotonic()
        {
            return true;
        }
        public override bool isSafe()
        {
            return true;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);
            var n0 = getArg(0, args, context);
            var l = Util.convertList(n0, context);
            var liter = l.iterator();
            int idx = 0;
            while (liter.hasNext())
            {
                var x = liter.next() as org.apache.jena.graph.Node;
                var liter2 = l.iterator();
                for (int i = 0; i < idx; i++)
                    liter2.next();
                while (liter2.hasNext())
                {
                    var y = liter2.next() as org.apache.jena.graph.Node;
                    if (x.equals(y))
                        return true;
                }
                idx++;
            }
            return false;
        }

    }

    // SWRL BuILTINS

    class ComplexStringOperation : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "complexStringOperation";
        }

        public override int getArgLength()
        {
            return 5;
        }

        public static string replace(string str, string pattern, string replacement)
        {
            if (string.IsNullOrEmpty(str))
            {
                return str;
            }
            var rgx = new System.Text.RegularExpressions.Regex(pattern);
            return rgx.Replace(str, replacement);
        }

        public static string translate(string str, string searchChars, string replaceChars)
        {
            if (string.IsNullOrEmpty(str))
            {
                return str;
            }
            StringBuilder buffer = new StringBuilder(str.Length);
            char[] chrs = str.ToCharArray();
            char[] withChrs = replaceChars.ToCharArray();
            int sz = chrs.Length;
            int withMax = replaceChars.Length - 1;
            for (int i = 0; i < sz; i++)
            {
                int idx = searchChars.IndexOf(chrs[i]);
                if (idx != -1)
                {
                    if (idx <= withMax)
                        buffer.Append(withChrs[idx]);
                }
                else
                {
                    buffer.Append(chrs[i]);
                }
            }
            return buffer.ToString();
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var kind = getArg(0, args, context).getLiteral().toString();

            var n0 = getArg(1, args, context);
            var n1 = RuleExtensions.lex(getArg(3, args, context), this, context);
            var n2 = RuleExtensions.lex(getArg(2, args, context), this, context);
            var n3 = RuleExtensions.lex(getArg(4, args, context), this, context);

            var sb = "";

            if (kind == "translate")
            {
                sb = translate(n1, n2, n3);
            }
            else if (kind == "replace")
            {
                sb = replace(n1, n2, n3);
            }
            else
                throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);

            if (n0.isLiteral())
                return RuleExtensions.lex(n0, this, context).CompareTo(sb.ToString()) == 0;
            else
            {
                org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb.ToString());
                return context.getEnv().bind(n0, result);
            }


        }
    }

    class SimpleStringOperation : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "simpleStringOperation";
        }

        public override int getArgLength()
        {
            return 0;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var kind = getArg(0, args, context).getLiteral().toString();

            var n0 = getArg(1, args, context);
            var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
            var n2 = getArg(3, args, context);

            var sb = "";

            if (kind == "substring")
            {
                var p1 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p1))
                    return false;

                if (length < 4)
                    sb = n1.Substring(Convert.ToInt32(p1));
                else
                {
                    var n3 = getArg(4, args, context);
                    var p2 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
                    if (!RuleExtensions.isInteger(p2))
                        return false;
                    sb = n1.Substring(Convert.ToInt32(p1), Convert.ToInt32(p2));
                }
            }
            else if (kind == "substring-before")
            {
                var arg = RuleExtensions.lex(n2, this, context);
                var p = n1.IndexOf(arg);
                if (p > 0)
                    sb = n1.Substring(0, p);
            }
            else if (kind == "substring-after")
            {
                var arg = RuleExtensions.lex(n2, this, context);
                var p = n1.IndexOf(arg);
                if (p >= 0)
                {
                    if (p + arg.Length < n1.Length)
                        sb = n1.Substring(p + arg.Length);
                }
            }
            else
                throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);

            if (n0.isLiteral())
                return RuleExtensions.lex(n0, this, context).CompareTo(sb.ToString()) == 0;
            else
            {
                org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb.ToString());
                return context.getEnv().bind(n0, result);
            }
        }
    }

    class StringUnary : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "stringUnary";
        }

        public override int getArgLength()
        {
            return 3;
        }
        static readonly System.Text.RegularExpressions.Regex trimmer = new System.Text.RegularExpressions.Regex(@"\s\s+", System.Text.RegularExpressions.RegexOptions.Compiled);

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var kind = getArg(0, args, context).getLiteral().toString();

            var n0 = getArg(1, args, context);

            if (kind == "case-ignore")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                    return string.Compare(RuleExtensions.lex(n0, this, context), n1, true) == 0;
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else if (kind == "length")
            {

                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                var len = n1.Length;
                if (n0.isLiteral())
                {
                    var p1 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p1))
                        return false;
                    return len == Convert.ToInt32(p1);
                }
                else
                {
                    return context.getEnv().bind(n0, Util.makeDoubleNode(len));
                }
            }
            else if (kind == "space-normalize")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                var sb = trimmer.Replace(n1, " ").Trim();
                if (n0.isLiteral())
                    return string.Compare(RuleExtensions.lex(n0, this, context), sb) == 0;
                else
                {
                    org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb);
                    return context.getEnv().bind(n0, result);
                }
            }
            else if (kind == "upper-case")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                var sb = n1.ToUpper();
                if (n0.isLiteral())
                    return string.Compare(RuleExtensions.lex(n0, this, context), sb) == 0;
                else
                {
                    org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb);
                    return context.getEnv().bind(n0, result);
                }
            }
            else if (kind == "lower-case")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                var sb = n1.ToLower();
                if (n0.isLiteral())
                    return string.Compare(RuleExtensions.lex(n0, this, context), sb) == 0;
                else
                {
                    org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb);
                    return context.getEnv().bind(n0, result);
                }
            }
            else if (kind == "contains")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                    return RuleExtensions.lex(n0, this, context).Contains(n1);
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else if (kind == "contains-case-ignore")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                    return RuleExtensions.lex(n0, this, context).ToLower().Contains(n1.ToLower());
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else if (kind == "starts-with")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                    return RuleExtensions.lex(n0, this, context).StartsWith(n1);
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else if (kind == "ends-with")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                    return RuleExtensions.lex(n0, this, context).EndsWith(n1);
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else if (kind == "matches")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                {
                    var ext = JenaRuleManager.GetReasonerExt(context);
                    if (!ext.MatchedRegexes.ContainsKey(n1))
                        ext.MatchedRegexes.Add(n1, new System.Text.RegularExpressions.Regex(n1, RegexOptions.Compiled));
                    return ext.MatchedRegexes[n1].IsMatch(RuleExtensions.lex(n0, this, context));
                }
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else if (kind == "sounds-like")
            {
                var n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
                if (n0.isLiteral())
                    return Soundex(n1).CompareTo(Soundex(RuleExtensions.lex(n0, this, context))) == 0;
                else
                {
                    org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(Soundex(n1));
                    return context.getEnv().bind(n0, result);
                }
            }
            else
                throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);

        }

        public static string Soundex(string word)
        {
            const int MaxSoundexCodeLength = 4;

            var soundexCode = new StringBuilder();
            var previousWasHOrW = false;

            word = Regex.Replace(
                word == null ? string.Empty : word.ToUpper(),
                    @"[^\w\s]",
                        string.Empty);

            if (string.IsNullOrEmpty(word))
                return string.Empty.PadRight(MaxSoundexCodeLength, '0');

            soundexCode.Append(word.First());

            for (var i = 1; i < word.Length; i++)
            {
                var numberCharForCurrentLetter =
                    GetCharNumberForLetter(word[i]);

                if (i == 1 &&
                        numberCharForCurrentLetter ==
                            GetCharNumberForLetter(soundexCode[0]))
                    continue;

                if (soundexCode.Length > 2 && previousWasHOrW &&
                        numberCharForCurrentLetter ==
                            soundexCode[soundexCode.Length - 2])
                    continue;

                if (soundexCode.Length > 0 &&
                        numberCharForCurrentLetter ==
                            soundexCode[soundexCode.Length - 1])
                    continue;

                soundexCode.Append(numberCharForCurrentLetter);

                previousWasHOrW = "HW".Contains(word[i]);
            }

            return soundexCode
                    .Replace("0", string.Empty)
                        .ToString()
                            .PadRight(MaxSoundexCodeLength, '0')
                                .Substring(0, MaxSoundexCodeLength);
        }

        private static char GetCharNumberForLetter(char letter)
        {
            if ("BFPV".Contains(letter)) return '1';
            if ("CGJKQSXZ".Contains(letter)) return '2';
            if ("DT".Contains(letter)) return '3';
            if ('L' == letter) return '4';
            if ("MN".Contains(letter)) return '5';
            if ('R' == letter) return '6';

            return '0';
        }

    }

    class ConcatenateStrings : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "concatenateStrings";
        }

        public override int getArgLength()
        {
            return 0;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            if (length < 1)
                throw new BuiltinException(this, context, "Must have at least 1 argument to " + getName());
            var n0 = getArg(0, args, context);

            StringBuilder sb = new StringBuilder();
            for (int i = 1; i < length; i++)
                sb.Append(RuleExtensions.lex(getArg(i, args, context), this, context));

            if (n0.isLiteral())
                return RuleExtensions.lex(n0, this, context).CompareTo(sb.ToString()) == 0;
            else
            {
                org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb.ToString());
                return context.getEnv().bind(n0, result);
            }
        }

    }

    class SumNumbers : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "sumNumbers";
        }

        public override int getArgLength()
        {
            return 0;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            if (length < 2)
                throw new BuiltinException(this, context, "Must have at least 2 arguments to " + getName());

            bool allInts = true;

            for (int i = 0; i < length; i++)
            {
                var n = getArg(i, args, context);
                if (!n.isLiteral() && (i == 0 && !n.isVariable()))
                    return false;
                if (n.isLiteral())
                {
                    var v = RuleExtensions.getValFromJenaLiteral(n.getLiteralValue());
                    if (!RuleExtensions.isInteger(v))
                    {
                        allInts = false;
                        if (!RuleExtensions.isDouble(v))
                            return false;
                    }
                }
            }

            var n0 = getArg(0, args, context);
            if (allInts)
            {
                long sum = 0;
                for (int i = 1; i < length; i++)
                {
                    var nx = getArg(i, args, context);
                    var vx = RuleExtensions.getValFromJenaLiteral(nx.getLiteralValue());
                    sum += Convert.ToInt64(vx);

                }
                if (n0.isLiteral())
                    return Convert.ToInt64(RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue())) == sum;
                else
                    return context.getEnv().bind(n0, Util.makeLongNode(sum));
            }
            else
            {
                double sum = 0.0;
                for (int i = 1; i < length; i++)
                {
                    var nx = getArg(i, args, context);
                    var vx = RuleExtensions.getValFromJenaLiteral(nx.getLiteralValue());
                    sum += Convert.ToDouble(vx);

                }
                if (n0.isLiteral())
                    return Convert.ToDouble(RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue())) == sum;
                else
                    return context.getEnv().bind(n0, Util.makeDoubleNode(sum));
            }
        }
    }

    class MultiplyNumbers : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "multiplyNumbers";
        }

        public override int getArgLength()
        {
            return 0;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            if (length < 2)
                throw new BuiltinException(this, context, "Must have at least 2 arguments to " + getName());

            bool allInts = true;

            for (int i = 0; i < length; i++)
            {
                var n = getArg(i, args, context);
                if (!n.isLiteral() && (i == 0 && !n.isVariable()))
                    return false;
                if (n.isLiteral())
                {
                    var v = RuleExtensions.getValFromJenaLiteral(n.getLiteralValue());
                    if (!RuleExtensions.isInteger(v))
                    {
                        allInts = false;
                        if (!RuleExtensions.isDouble(v))
                            return false;
                    }
                }
            }

            var n0 = getArg(0, args, context);
            if (allInts)
            {
                long product = 1;
                for (int i = 1; i < length; i++)
                {
                    var nx = getArg(i, args, context);
                    var vx = RuleExtensions.getValFromJenaLiteral(nx.getLiteralValue());
                    product *= Convert.ToInt64(vx);

                }
                if (n0.isLiteral())
                    return Convert.ToInt64(RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue())) == product;
                else
                    return context.getEnv().bind(n0, Util.makeLongNode(product));
            }
            else
            {
                double product = 1.0;
                for (int i = 1; i < length; i++)
                {
                    var nx = getArg(i, args, context);
                    var vx = RuleExtensions.getValFromJenaLiteral(nx.getLiteralValue());
                    product *= Convert.ToDouble(vx);

                }
                if (n0.isLiteral())
                    return Convert.ToDouble(RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue())) == product;
                else
                    return context.getEnv().bind(n0, Util.makeDoubleNode(product));
            }
        }

    }

    class BooleanUnary : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "booleanUnary";
        }

        public override int getArgLength()
        {
            return 3;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var kind = getArg(0, args, context).getLiteral().toString();

            var n0 = getArg(1, args, context);
            if (kind == "not")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!(p1 is bool))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!(p0 is bool))
                        return false;

                    return Convert.ToBoolean(p1) == !Convert.ToBoolean(p0);
                }
                else
                    throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
            }
            else
                throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
        }
    }

    class MathBinary : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "mathBinary";
        }

        public override int getArgLength()
        {
            return 4;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var kind = getArg(0, args, context).getLiteral().toString();

            var n0 = getArg(1, args, context);
            if (kind == "subtract")
            {
                var n1 = getArg(2, args, context);
                var n2 = getArg(3, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2) && !RuleExtensions.isDouble(p2))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1) && RuleExtensions.isInteger(p2))
                        return Convert.ToInt64(p0) == Convert.ToInt64(p1) - Convert.ToInt64(p2);
                    else
                        return Convert.ToDouble(p0) == Convert.ToDouble(p1) - Convert.ToDouble(p2);
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(p1) - Convert.ToInt64(p2)));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(Convert.ToDouble(p1) - Convert.ToDouble(p2)));
                }
            }
            else if (kind == "divide")
            {
                var n1 = getArg(2, args, context);
                var n2 = getArg(3, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2) && !RuleExtensions.isDouble(p2))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    return Convert.ToDouble(p0) == Convert.ToDouble(p1) / Convert.ToDouble(p2);
                }
                else
                {
                    return context.getEnv().bind(n0, Util.makeDoubleNode(Convert.ToDouble(p1) / Convert.ToDouble(p2)));
                }
            }
            else if (kind == "power")
            {
                var n1 = getArg(2, args, context);
                var n2 = getArg(3, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2) && !RuleExtensions.isDouble(p2))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    return Convert.ToDouble(p0) == Math.Pow(Convert.ToDouble(p1), Convert.ToDouble(p2));
                }
                else
                {
                    return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Pow(Convert.ToDouble(p1), Convert.ToDouble(p2))));
                }
            }
            else if (kind == "int-divide")
            {
                var n1 = getArg(2, args, context);
                var n2 = getArg(3, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1))
                    return false;

                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0))
                        return false;
                    return Convert.ToInt64(p0) == Convert.ToInt64(p1) / Convert.ToInt64(p2);
                }
                else
                {
                    return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(p1) / Convert.ToInt64(p2)));
                }
            }
            else if (kind == "modulo")
            {
                var n1 = getArg(2, args, context);
                var n2 = getArg(3, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1))
                    return false;

                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0))
                        return false;
                    return Convert.ToInt64(p0) == Convert.ToInt64(p1) % Convert.ToInt64(p2);
                }
                else
                {
                    return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(p1) % Convert.ToInt64(p2)));
                }
            }
            else if (kind == "round-half-to-even")
            {
                var n1 = getArg(2, args, context);
                var n2 = getArg(3, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isDouble(p1) && !RuleExtensions.isInteger(p1))
                    return false;

                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
                        return Convert.ToInt64(p0) == Convert.ToInt64(RoundHalfToEven(Convert.ToDouble(p1), Convert.ToInt32(p2)));
                    else
                        return Convert.ToDouble(p0) == RoundHalfToEven(Convert.ToDouble(p1), Convert.ToInt32(p2));
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(RoundHalfToEven(Convert.ToDouble(p1), Convert.ToInt32(p2)))));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(RoundHalfToEven(Convert.ToDouble(p1), Convert.ToInt32(p2))));
                }
            }
            else
            {
                throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
            }
        }

        static double RoundHalfToEven(double x, int precision)
        {
            if (precision >= 0)
                return Math.Round(x, precision, MidpointRounding.ToEven);
            else
            {
                var fac = Math.Pow(10.0, -precision);
                return fac * Math.Round(x / fac, 0, MidpointRounding.ToEven);
            }
        }
    }

    class MathUnary : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "mathUnary";
        }

        public override int getArgLength()
        {
            return 3;
        }
        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var kind = getArg(0, args, context).getLiteral().toString();

            var n0 = getArg(1, args, context);
            if (kind == "minus")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
                        return Convert.ToInt64(p0) == -Convert.ToInt64(p1);
                    else
                        return Convert.ToDouble(p0) == -Convert.ToDouble(p1);
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(-Convert.ToInt64(p1)));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(-Convert.ToDouble(p1)));
                }
            }
            else if (kind == "absolute")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
                        return Convert.ToInt64(p0) == Math.Abs(Convert.ToInt64(p1));
                    else
                        return Convert.ToDouble(p0) == Math.Abs(Convert.ToDouble(p1));
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(Math.Abs(Convert.ToInt64(p1))));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Abs(Convert.ToDouble(p1))));
                }
            }
            else if (kind == "ceiling")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
                        return Convert.ToInt64(p0) == Convert.ToInt64(p1);
                    else
                        return Convert.ToDouble(p0) == Math.Ceiling(Convert.ToDouble(p1));
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(p1)));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Ceiling(Convert.ToDouble(p1))));
                }
            }
            else if (kind == "floor")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
                        return Convert.ToInt64(p0) == Convert.ToInt64(p1);
                    else
                        return Convert.ToDouble(p0) == Math.Floor(Convert.ToDouble(p1));
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(p1)));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Floor(Convert.ToDouble(p1))));
                }
            }
            else if (kind == "round")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    if (RuleExtensions.isInteger(p0) && RuleExtensions.isInteger(p1))
                        return Convert.ToInt64(p0) == Convert.ToInt64(p1);
                    else
                        return Convert.ToDouble(p0) == Math.Round(Convert.ToDouble(p1));
                }
                else
                {
                    if (RuleExtensions.isInteger(p1))
                        return context.getEnv().bind(n0, Util.makeLongNode(Convert.ToInt64(p1)));
                    else
                        return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Round(Convert.ToDouble(p1))));
                }
            }
            else if (kind == "sine")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    return Convert.ToDouble(p0) == Math.Sin(Convert.ToDouble(p1));
                }
                else
                    return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Sin(Convert.ToDouble(p1))));
            }
            else if (kind == "cosine")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    return Convert.ToDouble(p0) == Math.Cos(Convert.ToDouble(p1));
                }
                else
                    return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Cos(Convert.ToDouble(p1))));
            }
            else if (kind == "tangent")
            {
                var n1 = getArg(2, args, context);

                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1) && !RuleExtensions.isDouble(p1))
                    return false;

                if (n0.isLiteral())
                {
                    var p0 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
                    if (!RuleExtensions.isInteger(p0) && !RuleExtensions.isDouble(p0))
                        return false;
                    return Convert.ToDouble(p0) == Math.Tan(Convert.ToDouble(p1));
                }
                else
                    return context.getEnv().bind(n0, Util.makeDoubleNode(Math.Tan(Convert.ToDouble(p1))));
            }
            else
                throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
        }
    }


    class CreateDatetime : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "createDatetime";
        }

        public override int getArgLength()
        {
            return 8;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var n0 = getArg(0, args, context);
            var n1 = getArg(2, args, context);
            var n2 = getArg(3, args, context);
            var n3 = getArg(4, args, context);
            var n4 = getArg(5, args, context);
            var n5 = getArg(6, args, context);
            var n6 = getArg(7, args, context);

            if (!n0.isLiteral() && n1.isLiteral() && n2.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral()) // bind to datetime
            {
                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1))
                    return false;
                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2))
                    return false;
                var p3 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
                if (!RuleExtensions.isInteger(p3))
                    return false;
                var p4 = RuleExtensions.getValFromJenaLiteral(n4.getLiteralValue());
                if (!RuleExtensions.isInteger(p4))
                    return false;
                var p5 = RuleExtensions.getValFromJenaLiteral(n5.getLiteralValue());
                if (!RuleExtensions.isInteger(p5))
                    return false;
                var p6 = RuleExtensions.getValFromJenaLiteral(n6.getLiteralValue());
                if (!RuleExtensions.isInteger(p6) && !RuleExtensions.isDouble(p6))
                    return false;
                var y = Convert.ToInt32(p1);
                var M = Convert.ToInt32(p2);
                var d = Convert.ToInt32(p3);
                var h = Convert.ToInt32(p4);
                var m = Convert.ToInt32(p5);
                int s;
                int ms;
                if (!RuleExtensions.isDouble(p6))
                {
                    s = Convert.ToInt32(p6);
                    ms = 0;
                }
                else
                {
                    var dbl = Convert.ToDouble(p6);
                    s = (int)Math.Truncate(dbl);
                    ms = (int)Math.Truncate((dbl - s) * 1000.0);
                }
                var dt = new DateTimeOffset(y, M, d, h, m, s, ms, TimeSpan.Zero);
                var l = org.apache.jena.graph.NodeFactory.createLiteral(dt.ToString("s") + "^^http://www.w3.org/2001/XMLSchema#dateTime");
                return context.getEnv().bind(n0, l);
            }
            else if (n0.isLiteral() && !n1.isLiteral() && !n2.isLiteral() && !n3.isLiteral() && !n4.isLiteral() && !n5.isLiteral() && !n6.isLiteral())//split datetime into parts
            {
                var val = n0.getLiteralValue();
                if (val is org.apache.jena.datatypes.xsd.XSDDateTime)
                {
                    var jdtm = val as org.apache.jena.datatypes.xsd.XSDDateTime;
                    return
                        context.getEnv().bind(n1, Util.makeIntNode(jdtm.getYears()))
                        && context.getEnv().bind(n2, Util.makeIntNode(jdtm.getMonths()))
                        && context.getEnv().bind(n3, Util.makeIntNode(jdtm.getDays()))
                        && context.getEnv().bind(n4, Util.makeIntNode(jdtm.getHours()))
                        && context.getEnv().bind(n5, Util.makeIntNode(jdtm.getMinutes()))
                        && context.getEnv().bind(n6, Util.makeDoubleNode(jdtm.getSeconds()))
                        ;
                }
                else
                    return false;
            }
            else if (n0.isLiteral() && n1.isLiteral() && n2.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral())//compare
            {
                var p1 = RuleExtensions.getValFromJenaLiteral(n1.getLiteralValue());
                if (!RuleExtensions.isInteger(p1))
                    return false;
                var p2 = RuleExtensions.getValFromJenaLiteral(n2.getLiteralValue());
                if (!RuleExtensions.isInteger(p2))
                    return false;
                var p3 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
                if (!RuleExtensions.isInteger(p3))
                    return false;
                var p4 = RuleExtensions.getValFromJenaLiteral(n4.getLiteralValue());
                if (!RuleExtensions.isInteger(p4))
                    return false;
                var p5 = RuleExtensions.getValFromJenaLiteral(n5.getLiteralValue());
                if (!RuleExtensions.isInteger(p5))
                    return false;
                var p6 = RuleExtensions.getValFromJenaLiteral(n6.getLiteralValue());
                if (!RuleExtensions.isInteger(p6) && !RuleExtensions.isDouble(p6))
                    return false;
                var y = Convert.ToInt32(p1);
                var M = Convert.ToInt32(p2);
                var d = Convert.ToInt32(p3);
                var h = Convert.ToInt32(p4);
                var m = Convert.ToInt32(p5);
                int s = Convert.ToInt32(p5);
                int ms;
                if (!RuleExtensions.isDouble(p6))
                {
                    s = Convert.ToInt32(p6);
                    ms = 0;
                }
                else
                {
                    var dbl = Convert.ToDouble(p6);
                    s = (int)Math.Truncate(dbl);
                    ms = (int)Math.Truncate((dbl - s) * 1000.0);
                }

                var val = n0.getLiteralValue();
                if (val is org.apache.jena.datatypes.xsd.XSDDateTime)
                {
                    var jdtm = val as org.apache.jena.datatypes.xsd.XSDDateTime;
                    return y == jdtm.getYears()
                        && M == jdtm.getMonths()
                        && d == jdtm.getDays()
                        && h == jdtm.getHours()
                        && m == jdtm.getMinutes()
                        && s == (int)Math.Truncate(jdtm.getSeconds())
                        && ms == (int)Math.Truncate((jdtm.getSeconds() - s) * 1000.0); ;
                }
                else
                    return false;
            }

            return false;
        }
    }

    class CreateDuration : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "createDuration";
        }

        public override int getArgLength()
        {
            return 8;
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            checkArgs(length, context);

            var n0 = getArg(0, args, context);
            //var n1 = getArg(2, args, context);
            //var n2 = getArg(3, args, context);
            var n3 = getArg(4, args, context);
            var n4 = getArg(5, args, context);
            var n5 = getArg(6, args, context);
            var n6 = getArg(7, args, context);

            if (!n0.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral()) // bind to datetime
            {
                var p3 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
                if (!RuleExtensions.isInteger(p3))
                    return false;
                var p4 = RuleExtensions.getValFromJenaLiteral(n4.getLiteralValue());
                if (!RuleExtensions.isInteger(p4))
                    return false;
                var p5 = RuleExtensions.getValFromJenaLiteral(n5.getLiteralValue());
                if (!RuleExtensions.isInteger(p5))
                    return false;
                var p6 = RuleExtensions.getValFromJenaLiteral(n6.getLiteralValue());
                if (!RuleExtensions.isInteger(p6) && !RuleExtensions.isDouble(p6))
                    return false;
                var d = Convert.ToInt32(p3);
                var h = Convert.ToInt32(p4);
                var m = Convert.ToInt32(p5);
                int s;
                int ms;
                if (!RuleExtensions.isDouble(p6))
                {
                    s = Convert.ToInt32(p6);
                    ms = 0;
                }
                else
                {
                    var dbl = Convert.ToDouble(p6);
                    s = (int)Math.Truncate(dbl);
                    ms = (int)Math.Truncate((dbl - s) * 1000.0);
                }
                var dt = new TimeSpan(d, h, m, s, ms);
                var l = org.apache.jena.graph.NodeFactory.createLiteral(System.Xml.XmlConvert.ToString(dt) + "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration");
                return context.getEnv().bind(n0, l);
            }
            else if (n0.isLiteral() && !n3.isLiteral() && !n4.isLiteral() && !n5.isLiteral() && !n6.isLiteral())//split datetime into parts
            {
                var val = n0.getLiteralValue();
                const string durSuf = "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration";
                if (val is string && (val as string).EndsWith(durSuf))
                {
                    var tm = (val as string).Substring(0, (val as string).Length - durSuf.Length);
                    var jdtm = System.Xml.XmlConvert.ToTimeSpan(tm);
                    return
                        context.getEnv().bind(n3, Util.makeIntNode(jdtm.Days))
                        && context.getEnv().bind(n4, Util.makeIntNode(jdtm.Hours))
                        && context.getEnv().bind(n5, Util.makeIntNode(jdtm.Minutes))
                        && context.getEnv().bind(n6, Util.makeDoubleNode((double)jdtm.Seconds + ((double)jdtm.Milliseconds) / 1000.0))
                        ;
                }
                else
                    return false;
            }
            else if (n0.isLiteral() && n3.isLiteral() && n4.isLiteral() && n5.isLiteral() && n6.isLiteral())//compare
            {

                var p3 = RuleExtensions.getValFromJenaLiteral(n3.getLiteralValue());
                if (!RuleExtensions.isInteger(p3))
                    return false;
                var p4 = RuleExtensions.getValFromJenaLiteral(n4.getLiteralValue());
                if (!RuleExtensions.isInteger(p4))
                    return false;
                var p5 = RuleExtensions.getValFromJenaLiteral(n5.getLiteralValue());
                if (!RuleExtensions.isInteger(p5))
                    return false;
                var p6 = RuleExtensions.getValFromJenaLiteral(n6.getLiteralValue());
                if (!RuleExtensions.isInteger(p6) && !RuleExtensions.isDouble(p6))
                    return false;

                var d = Convert.ToInt32(p3);
                var h = Convert.ToInt32(p4);
                var m = Convert.ToInt32(p5);
                int s = Convert.ToInt32(p5);
                int ms;
                if (!RuleExtensions.isDouble(p6))
                {
                    s = Convert.ToInt32(p6);
                    ms = 0;
                }
                else
                {
                    var dbl = Convert.ToDouble(p6);
                    s = (int)Math.Truncate(dbl);
                    ms = (int)Math.Truncate((dbl - s) * 1000.0);
                }

                var val = n0.getLiteralValue();
                const string durSuf = "^^http://www.w3.org/2001/XMLSchema#dayTimeDuration";
                if (val is string && (val as string).EndsWith(durSuf))
                {
                    var tm = (val as string).Substring(0, (val as string).Length - durSuf.Length);
                    var jdtm = System.Xml.XmlConvert.ToTimeSpan(tm);

                    return d == jdtm.Days
                    && h == jdtm.Hours
                    && m == jdtm.Minutes
                    && s == jdtm.Seconds
                    && ms == jdtm.Milliseconds;
                }
                else
                    return false;
            }

            return false;
        }
    }

    class Alpha : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "alpha";
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            var n0 = getArg(0, args, context);
            var n1 = getArg(1, args, context);

            if (n1.isVariable())
            {
                if (!n0.isLiteral())
                    return false;

                var lx = RuleExtensions.lex(getArg(0, args, context), this, context);

                string uri = ext.TheAccessObject.UriFromCnl(lx, "instance");

                org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createURI(uri);
                return context.getEnv().bind(n1, result);

            }
            else
            {
                if (!n1.isURI())
                    return false;

                var n1type = ext.GetTypeOfNode(context, n1);
                string n1n = ext.TheAccessObject.CnlFromUri(n1.getURI(), n1type);

                if (n0.isLiteral())
                {
                    var lx = RuleExtensions.lex(getArg(0, args, context), this, context);
                    return string.Compare(n1n, lx) == 0;
                }
                else
                {
                    org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(n1n);
                    return context.getEnv().bind(n0, result);
                }
            }
        }

    }

    class Annot : org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
    {
        public override string getName()
        {
            return "annotation";
        }

        public override bool bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
        {
            var ext = JenaRuleManager.GetReasonerExt(context);

            var n3 = getArg(0, args, context);
            var n0 = getArg(1, args, context);
            var n1 = getArg(2, args, context);
            var n2 = getArg(3, args, context);

            if (n3.isVariable())
            {
                var n0type = ext.GetTypeOfNode(context, n0);
                string n0n = ext.TheAccessObject.CnlFromUri(n0.getURI(), n0type);

                var prop = RuleExtensions.lex(n1, this, context);
                var lng = RuleExtensions.lex(n2, this, context);

                var annotVal = ext.TheAccessObject.GetAnnotationValue(n0n, prop, lng, null);
                if (annotVal == null)
                    return false;
                return context.getEnv().bind(n3, org.apache.jena.graph.NodeFactory.createLiteral(annotVal.ToString()));
            }
            else
            {

                var n0type = ext.GetTypeOfNode(context, n0);
                string n0n = ext.TheAccessObject.CnlFromUri(n0.getURI(), n0type);

                var n1type = ext.GetTypeOfNode(context, n1);
                string n1n = ext.TheAccessObject.CnlFromUri(n0.getURI(), n1type);

                var lng = RuleExtensions.lex(n2, this, context);

                var annotVal = ext.TheAccessObject.GetAnnotationValue(n0n, n1, lng, null);

                var lx = RuleExtensions.lex(n3, this, context);
                return string.Compare(annotVal.ToString(), lx) == 0;
            }
        }

    }

    internal class ReasonerExt
    {
        public Dictionary<string, Regex> MatchedRegexes = new Dictionary<string, Regex>();
        public Dictionary<int, Tuple<string, List<IExeVar>>> ExeRules;
        public Dictionary<int, SwrlIterate> SwrlIterators;
        public Action<string, Dictionary<string, Tuple<string, object>>> DebugAction;
        public dynamic TheAccessObject;
        public dynamic Outer;
        public CogniPy.ARS.InvTransform TheInvTransform;
        public SwrlIterateProc TheSwrlIterateProc;

        ConcurrentDictionary<string, List<LinkedDictionary<string, JenaValue>>> bodies = new ConcurrentDictionary<string, List<LinkedDictionary<string, JenaValue>>>();
        HashSet<string> normals = new HashSet<string>();
        ConcurrentDictionary<string, List<LinkedDictionary<string, JenaValue>>> heads = new ConcurrentDictionary<string, List<LinkedDictionary<string, JenaValue>>>();
        List<Tuple<string, string, List<Tuple<object, string>>>> ontologyErrors = new List<Tuple<string, string, List<Tuple<object, string>>>>();

        public string GetTypeOfNode(RuleContext context, org.apache.jena.graph.Node n)
        {
            if (TheAccessObject.SWRLOnly)
                return "instance";
            else
            {
                var infgraph = context.getGraph();

                bool isInstance = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.NamedIndividual.asNode());
                bool isRole = false;
                bool isDataRole = false;
                if (!isInstance)
                    isRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.ObjectProperty.asNode());
                if (!isInstance && !isRole)
                    isDataRole = infgraph.contains(n, org.apache.jena.vocabulary.RDF.Nodes.type, org.apache.jena.vocabulary.OWL2.DatatypeProperty.asNode());

                bool isConcept = !isInstance && !isRole && !isDataRole;

                return isInstance ? "instance" : (isConcept ? "concept" : (isRole ? "role" : "datarole"));
            }
        }

        public void AddOntologyError(string title, string content, List<Tuple<object, string>> vals)
        {
            ontologyErrors.Add(Tuple.Create(title, content, vals));
        }

        public void SetModalVals(bool isBody, bool isNormal, string str, LinkedDictionary<string, JenaValue> vals)
        {
            var dic = (isBody ? bodies : heads);
            if (!dic.ContainsKey(str))
                dic.TryAdd(str, new List<LinkedDictionary<string, JenaValue>>());

            dic[str].Add(vals);
            if (isNormal)
                normals.Add(str);
        }

        private bool AIsB(LinkedDictionary<string, JenaValue> A, LinkedDictionary<string, JenaValue> B)
        {
            foreach (var kv in A)
            {
                if (!B.ContainsKey(kv.Key))
                    return false;
                if (!kv.Value.Value.Equals(B[kv.Key].Value))
                    return false;
            }
            return true;
        }

        public List<Tuple<string, string, List<Tuple<object, string>>>> GetOntologyErrors()
        {
            return ontologyErrors;
        }

        public Dictionary<string, List<LinkedDictionary<string, JenaValue>>> Validate()
        {
            var res = new Dictionary<string, List<LinkedDictionary<string, JenaValue>>>();
            foreach (var b in bodies)
            {
                if (normals.Contains(b.Key))
                {
                    if (!heads.ContainsKey(b.Key))
                    {
                        if (!res.ContainsKey(b.Key))
                            res.Add(b.Key, new List<LinkedDictionary<string, JenaValue>>());
                        res[b.Key].AddRange(b.Value);
                    }
                    else
                    {
                        foreach (var body in b.Value)
                        {
                            bool found = false;
                            foreach (var head in heads[b.Key])
                            {
                                if (AIsB(body, head))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (!found)
                            {
                                if (!res.ContainsKey(b.Key))
                                    res.Add(b.Key, new List<LinkedDictionary<string, JenaValue>>());
                                res[b.Key].Add(body);
                            }
                        }
                    }
                }
                else
                {
                    if (heads.ContainsKey(b.Key))
                    {
                        foreach (var body in b.Value)
                        {
                            bool found = false;
                            foreach (var head in heads[b.Key])
                            {
                                if (AIsB(body, head))
                                {
                                    found = true;
                                    break;
                                }
                            }
                            if (found)
                            {
                                if (!res.ContainsKey(b.Key))
                                    res.Add(b.Key, new List<LinkedDictionary<string, JenaValue>>());
                                res[b.Key].Add(body);
                            }
                        }
                    }
                }
            }
            return res;
        }

    }

    internal class JenaRuleManager
    {
        static java.util.Map PfxMap = new java.util.HashMap();
        static string[] aboxRules;
        static string[] sameAsRules;
        static string[] moreRules;
        static string[] tboxRules;

        static Regex splitter = new Regex(@"\[\s*([a-zA-Z0-9\-]+)\s*\:[^\]]*\]", RegexOptions.Compiled | RegexOptions.Multiline);
        static java.util.List loadRules(string[] ruleFiles, java.util.Map map, bool debugModeOn)
        {
            var rules = new java.util.ArrayList();
            foreach (var f in ruleFiles)
            {
                string ruls = new StreamReader(FindResourceString(f)).ReadToEnd();
                if (debugModeOn)
                {
                    ruls = splitter.Replace(ruls, new MatchEvaluator((m) =>
                   {
                       var iid = "\'#" + m.Groups[1].Value + "\'";
                       return m.Value.Replace(@"]", @", debugTraceBuiltIn(" + iid + @")]");
                   }));
                }
                var mem = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.ByteArrayInputStream(System.Text.Encoding.ASCII.GetBytes(ruls))));
                var rs = jena.RuleMap.loadRules(mem, map);
                rules.addAll(rs);
            }
            return rules;
        }

        static Stream FindResourceString(string shortName)
        {
            var name = (from x in System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceNames() where x.EndsWith("." + shortName) select x).First();
            return System.Reflection.Assembly.GetExecutingAssembly().GetManifestResourceStream(name);
        }

        static JenaRuleManager()
        {
            BuiltinRegistry.theRegistry.register(new PrintOutToConsole());
            BuiltinRegistry.theRegistry.register(new PairwizeDifferentAtleastOnce());
            BuiltinRegistry.theRegistry.register(new SetupCommonToListAsSubject());
            BuiltinRegistry.theRegistry.register(new ListTestSubjectPairewise());
            BuiltinRegistry.theRegistry.register(new ListAllEntriesAre());
            BuiltinRegistry.theRegistry.register(new ListAnyTwoEqualMembers());
            BuiltinRegistry.theRegistry.register(new StringLength());
            BuiltinRegistry.theRegistry.register(new SwrlIterator());
            BuiltinRegistry.theRegistry.register(new DebugTraceBuiltIn());
            BuiltinRegistry.theRegistry.register(new ModalCheckerBuiltIn());
            BuiltinRegistry.theRegistry.register(new OntologyError());

            BuiltinRegistry.theRegistry.register(new ConcatenateStrings());
            BuiltinRegistry.theRegistry.register(new SumNumbers());
            BuiltinRegistry.theRegistry.register(new MultiplyNumbers());
            BuiltinRegistry.theRegistry.register(new ComplexStringOperation());
            BuiltinRegistry.theRegistry.register(new SimpleStringOperation());
            BuiltinRegistry.theRegistry.register(new StringUnary());
            BuiltinRegistry.theRegistry.register(new BooleanUnary());
            BuiltinRegistry.theRegistry.register(new MathUnary());
            BuiltinRegistry.theRegistry.register(new MathBinary());
            BuiltinRegistry.theRegistry.register(new CreateDatetime());
            BuiltinRegistry.theRegistry.register(new CreateDuration());
            BuiltinRegistry.theRegistry.register(new Alpha());
            BuiltinRegistry.theRegistry.register(new Annot());
            BuiltinRegistry.theRegistry.register(new ExecuteExternalRule());
            BuiltinRegistry.theRegistry.register(new ExecuteExternalFunction());


            PfxMap.put("rdfs", "http://www.w3.org/2000/01/rdf-schema#");
            PfxMap.put("owl", "http://www.w3.org/2002/07/owl#");
            PfxMap.put("xsd", "http://www.w3.org/2001/XMLSchema#");
            PfxMap.put("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#");

            aboxRules = new string[]{
                            "class-axioms.owlrl.jena",
                            "cls.owlrl.jena",
                            "props.owlrl.jena",
                            "class-axioms.owlrl.val.jena",
                            "cls.owlrl.val.jena",
                            "props.owlrl.val.jena",
                        };

            sameAsRules = new string[]{
                           "same-as.owlrl.jena",
                           "same-as.owlrl.val.jena",
                         };

            moreRules = new string[]{
                            "more.owlrl.jena",
                            "more.owlrl.val.jena",
                        };

            tboxRules = new string[]{
                            "datatypes.owlrl.jena",
                            "datatypes.owlrl.val.jena",
                            "schema.owlrl.jena"
                        };

        }

        public static java.util.List GetGeneralRules(MatMode mode, bool extended, bool sameAs, bool debugModeOn)
        {
            java.util.List rules = new java.util.ArrayList();

            if (mode != MatMode.SWRLOnly)
            {
                if (mode != MatMode.Tbox)
                    rules.addAll(loadRules(aboxRules, PfxMap, debugModeOn));
                rules.addAll(loadRules(tboxRules, PfxMap, debugModeOn));
                if (extended)
                    rules.addAll(loadRules(moreRules, PfxMap, debugModeOn));
                if (sameAs)
                    rules.addAll(loadRules(sameAsRules, PfxMap, debugModeOn));
            }
            return rules;
        }

        public static java.util.List GetRule(string scr)
        {
            return jena.RuleMap.loadRules(new java.io.BufferedReader(new java.io.StringReader(scr)), JenaRuleManager.PfxMap);
        }

        private static ConditionalWeakTable<GenericRuleReasoner, ReasonerExt> GenericRuleReasonerExt = new ConditionalWeakTable<GenericRuleReasoner, ReasonerExt>();

        public static GenericRuleReasoner CreateReasoner(java.util.List rules, Action<string, Dictionary<string, Tuple<string, object>>> debugAction, Dictionary<int, Tuple<string, List<IExeVar>>> ExeRules, Dictionary<int, SwrlIterate> SwrlIterators, dynamic accessObject, dynamic outer, CogniPy.ARS.InvTransform invTransform, SwrlIterateProc sproc)
        {
            var rete_reasoner = new org.apache.jena.reasoner.rulesys.GenericRuleReasoner(rules);
            rete_reasoner.setMode(org.apache.jena.reasoner.rulesys.GenericRuleReasoner.FORWARD_RETE);
            GenericRuleReasonerExt.Add(rete_reasoner, new ReasonerExt() { DebugAction = debugAction, ExeRules = ExeRules, SwrlIterators = SwrlIterators, Outer = outer, TheAccessObject = accessObject, TheInvTransform = invTransform, TheSwrlIterateProc = sproc });
            return rete_reasoner;
        }

        public static GenericRuleReasoner CloneReasoner(GenericRuleReasoner reasoner, dynamic accessObject, dynamic outer)
        {
            var rete_reasoner = new org.apache.jena.reasoner.rulesys.GenericRuleReasoner(reasoner.getRules());
            rete_reasoner.setMode(org.apache.jena.reasoner.rulesys.GenericRuleReasoner.FORWARD_RETE);
            ReasonerExt ret;
            if (!GenericRuleReasonerExt.TryGetValue(reasoner, out ret))
                return null;
            GenericRuleReasonerExt.Add(rete_reasoner, new ReasonerExt() { DebugAction = ret.DebugAction, ExeRules = ret.ExeRules, SwrlIterators = ret.SwrlIterators, Outer = outer, TheAccessObject = accessObject, TheInvTransform = ret.TheInvTransform, TheSwrlIterateProc = ret.TheSwrlIterateProc });
            return rete_reasoner;
        }

        public static GenericRuleReasoner BootstrapReasonerForAboxChanges(GenericRuleReasoner reasonerTo, GenericRuleReasoner reasonerFrom, dynamic accessObject, dynamic outer)
        {
            ReasonerExt ret;
            if (!GenericRuleReasonerExt.TryGetValue(reasonerFrom, out ret))
                return null;
            GenericRuleReasonerExt.Add(reasonerTo, new ReasonerExt() { DebugAction = ret.DebugAction, ExeRules = ret.ExeRules, SwrlIterators = ret.SwrlIterators, Outer = outer, TheAccessObject = accessObject, TheInvTransform = ret.TheInvTransform, TheSwrlIterateProc = ret.TheSwrlIterateProc });
            return reasonerTo;
        }

        public static List<Tuple<string, string, List<Tuple<object, string>>>> GetOntologyErrors(GenericRuleReasoner reasoner)
        {
            ReasonerExt ret;
            if (GenericRuleReasonerExt.TryGetValue(reasoner, out ret))
                return ret.GetOntologyErrors();
            return null;
        }

        public static Dictionary<string, List<LinkedDictionary<string, JenaValue>>> GetModalValidationResult(GenericRuleReasoner reasoner)
        {
            ReasonerExt ret;
            if (GenericRuleReasonerExt.TryGetValue(reasoner, out ret))
                return ret.Validate();
            return null;
        }

        public static ReasonerExt GetReasonerExt(RuleContext context)
        {
            var gr = context.getGraph().getReasoner() as GenericRuleReasoner;
            if (gr != null)
            {
                ReasonerExt ret;
                if (GenericRuleReasonerExt.TryGetValue(gr, out ret))
                    return ret;
            }
            throw new InvalidOperationException();
        }

        public static void Setup()
        {
            Unique = new UniqueId<object>();
        }

        public class UniqueId<T>
            where T : class
        {
            long counter = 0;
            ConditionalWeakTable<T, object> ids = new ConditionalWeakTable<T, object>();
            ConcurrentDictionary<long, object> rew = new ConcurrentDictionary<long, object>();

            public long GetId(T obj)
            {
                var id = (long)ids.GetValue(obj, _ => Interlocked.Increment(ref counter));
                rew.TryAdd(id, obj);
                return id;
            }

            public T GetObject(long id)
            {
                object wr;
                if (rew.TryGetValue(id, out wr))
                    return wr as T;
                else
                    return null;
            }

        }

        static UniqueId<object> Unique = null;

        public const string DynamicTypeURI = "http://ontorion.com/dynamicType";

        public static object getObject(string lex)
        {
            var sid = lex.Substring(0, lex.Length - 2 - DynamicTypeURI.Length);
            var id = long.Parse(sid);
            return Unique.GetObject(id);
        }

        public static object getObject(org.apache.jena.graph.Node n)
        {
            var lex = n.getLiteralLexicalForm();
            if (lex.EndsWith("^^" + DynamicTypeURI))
            {
                var sid = lex.Substring(0, lex.Length - 2 - DynamicTypeURI.Length);
                var id = long.Parse(sid);
                return Unique.GetObject(id);
            }
            else
                return RuleExtensions.getValFromJenaLiteral(n.getLiteralValue());
        }

        public static org.apache.jena.graph.Node getLiteral(object res)
        {
            if (RuleExtensions.isSimpleJenaObject(res))
                return NodeFactory.createLiteral(LiteralLabelFactory.create((object)res));
            else
                return NodeFactory.createLiteral(Unique.GetId((object)res).ToString() + "^^" + DynamicTypeURI);

        }

        public static string registerObject(object o)
        {
            return Unique.GetId(o).ToString() + "^^" + DynamicTypeURI;
        }

    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // DoubleLink
    //
    // Base type for Values in LinkedDictionary<K, V>
    //
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    [DebuggerDisplay("prev = {prev} next = {next}")]
    public class DoubleLink
    {
        DoubleLink next;
        DoubleLink prev;

        public DoubleLink Next
        {
            get { return next; }
            // Rude setter: doesn't examine or patch existing links 
            set
            {
                value.prev = this;
                next = value;
            }
        }

        public DoubleLink Prev
        {
            get { return prev; }
            // Rude setter: doesn't examine or patch existing links 
            set
            {
                value.next = this;
                prev = value;
            }
        }

        // Remove this item from a list by patching over it
        public void Unlink()
        {
            prev.next = next;
            next.prev = prev;
            next = prev = null;
        }

        // Insert this item into a list after the specified element
        public void InsertAfter(DoubleLink e)
        {
            e.next.prev = this;
            next = e.next;
            prev = e;
            e.next = this;
        }
    };

    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // LinkedDictionary<K, V>
    //
    // Enhancement to a Dictionary<K,V> object which maintains an ordering of all Values such that they can be
    // iterated in the forward or reverse direction, according to this ordering, in O(N). The type used for V
    // must inherit from DoubleLink class, a doubly-linked list. The ordering is established by calling 
    // InsertAtFront for element(s) which have been added to the associated Dictionary<K,V> but which are not yet 
    // in the list. In a typical use, the list might be ordered by time of touch: the "current" element or element
    // of interest is located by its key, unlinked from the list, and reinserted at the front, all in O(1) time.
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public class LinkedDictionary<K, V> : Dictionary<K, V> where V : DoubleLink
    {
        readonly DoubleLink first = new DoubleLink();
        readonly DoubleLink last = new DoubleLink();

        public LinkedDictionary()
        {
            first.Next = last;
        }

        /// <summary>
        /// Enumerate in forward order
        /// </summary>
        public IEnumerable<V> Forward
        {
            get
            {
                DoubleLink i = first.Next;
                while (i != last)
                {
                    yield return (V)i;
                    i = i.Next;
                }
            }
        }

        /// <summary>
        /// Enumerate in reverse order
        /// </summary>
        public IEnumerable<V> Reverse
        {
            get
            {
                DoubleLink i = last.Prev;
                while (i != first)
                {
                    yield return (V)i;
                    i = i.Prev;
                }
            }
        }

        /// <summary>
        /// Truncates all element(s) beyond e in the ordering. They should already be removed from the dictionary, 
        /// this only unlinks them
        /// </summary>
        public void TruncateAt(V e)
        {
            e.Next = last;
        }

        /// <summary>
        /// Element should already be removed from the dictionary, this only unlinks it
        /// </summary>
        public void Unlink(V e)
        {
            e.Unlink();
        }

        /// <summary>
        /// Element should already be in dictionary, but not linked
        /// </summary>
        public void LinkToFront(V e)
        {
            e.InsertAfter(first);
        }
    };

}



