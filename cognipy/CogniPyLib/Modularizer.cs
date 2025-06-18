using CogniPy.CNL;
using CogniPy.CNL.DL;
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Linq;

namespace CogniPy.Splitting
{
    public class ScriptLine
    {
        private string logic;
        private CNL.DL.Statement stmt = null;

        public bool IsAnnotation()
        {
            if (GetStatement() is DLAnnotationAxiom)
            {
                return true;
            }
            else
            {
                return false;
            }
        }

        public bool IsABox()
        {
            return (GetStatement() is CNL.DL.InstanceOf) || (GetStatement() is CNL.DL.RelatedInstances) || (GetStatement() is CNL.DL.InstanceValue);
        }

        public bool HasInstance()
        {
            return IsABox();
        }

        public ScriptLine(string logic)
        {
            this.logic = logic;
        }

        public CNL.DL.Statement GetStatement()
        {
            if (stmt == null)
            {
                var ast = DLToys.ParseDL(logic);
                Debug.Assert(ast.Statements.Count == 1);
                DLModSimplifier simli = new DLModSimplifier();
                var nstmt = simli.Visit(ast) as CNL.DL.Paragraph;
                stmt = nstmt.Statements.First();
            }
            return stmt;
        }

        public CNL.DL.StatementType GetStatementType()
        {
            var attr = (CogniPy.CNL.DL.StatementAttr)GetStatement().GetType().GetCustomAttributes(typeof(CogniPy.CNL.DL.StatementAttr), true).First();
            return attr.type;
        }

        public IEnumerable<string> GetSignature()
        {
            return DLToys.GetSignatureFromStatement(GetStatement());
        }

        public struct LociNode
        {
            public string symbol;
            public string expression;
            public string loci;
            public int num_loci;
        }

        public List<LociNode> GetLoci(LocalityKind kind)
        {
            bool r;
            var mod = DLToys.GetModulationFromStatement(GetStatement(), kind, out r);
            var reexpr = DLToys.MakeExpressionFromStatement(GetStatement());
            bool isTautology = DLToys.IsAny(mod);
            if (isTautology)
            {
                return new List<LociNode>(new LociNode[]{ new LociNode(){
                                symbol="\0", loci = "\0", num_loci = 1, expression=reexpr}});
            }
            else
            {
                var ret = new List<LociNode>();
                int num_loci = mod.Count;
                if (num_loci == 0)
                {
                    var sign = DLToys.GetSignatureFromStatement(GetStatement());
                    foreach (var symbol in sign)
                        ret.Add(new LociNode() { symbol = symbol, loci = "", num_loci = 0, expression = reexpr });
                }
                else
                {
                    foreach (var loci_l in mod)
                    {
                        loci_l.Sort();
                        var loci = string.Join("\n", loci_l);
                        foreach (var symbol in loci_l)
                            ret.Add(new LociNode() { symbol = symbol, loci = loci, num_loci = num_loci, expression = reexpr });
                    }
                }
                return ret;
            }

        }
        public string GetKind()
        {
            if (IsConstraint())
                return "C";
            else if (IsAnnotation())
                return "A";
            else if (IsSwrlExpression())
                return "RU";
            else if (IsExeRuleExpression())
                return "AR";
            else if (IsConceptStatement())
                return "SC";
            else if (IsInstanceStatement())
                return "SI";
            else if (IsRoleStatement())
                return "SR";
            else
                return "";
        }

        public bool IsConstraint()
        {
            return GetStatement().modality != Statement.Modality.IS;
        }

        public bool IsSwrlExpression()
        {
            return GetStatement() is SwrlStatement;
        }

        public bool IsExeRuleExpression()
        {
            return GetStatement() is ExeStatement;
        }

        public bool IsConceptStatement()
        {
            return GetStatementType() == CNL.DL.StatementType.Concept;
        }

        public bool IsInstanceStatement()
        {
            return GetStatementType() == CNL.DL.StatementType.Instance;
        }

        public bool IsRoleStatement()
        {
            return GetStatementType() == CNL.DL.StatementType.Role;
        }

        public string Logic()
        {
            return logic;
        }

    }

    public interface Modularizer
    {
        IEnumerable<ScriptLine> GetModule(IEnumerable<string> signature, LocalityKind kind, int reasoningRadius, bool getModalities, bool modularizeByNamespaces);

        void Begin();

        void Insert(IEnumerable<ScriptLine> script, bool modularizeByNamespaces);

        void Delete(IEnumerable<ScriptLine> script, bool modularizeByNamespaces);

        void Apply();

        IEnumerable<string> GetNewAnnotationsSet(IEnumerable<string> annotations);

        IEnumerable<string> GetOldAnnotationsSet(IEnumerable<string> annotations);
    }
}