using Microsoft.CSharp;
using Ontorion.CNL.DL;
using System;
using System.CodeDom.Compiler;
using System.Collections.Generic;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;

namespace OWLServices
{

    public class ReactiveRuleCompiler
    {
        private const string classTemplate = @"
            using System;
            using System.Linq;
            using System.Collections.Generic;
            using System.Linq.Expressions;
            
            public class Rule
            {{
                public void Execute(object[] Matches, Action<HashSet<string>>  knowledgeInsert,  Action<HashSet<string>> knowledgeDelete, Action<int, HashSet<string>> delayedKnowledgeInsert) {{
                    /*{1}*/
                    this.DlDelayedKnowledgeInsert = delayedKnowledgeInsert;
                    this.DlKnowledgeDelete= knowledgeDelete;
                    this.DlKnowledgeInsert= knowledgeInsert;
                    {2};
                    {0};
                }}

                public dynamic Ontorion;
                public dynamic Factory;
                public dynamic Outer;
                public dynamic Executor;
                public Action<int, HashSet<string>> DlDelayedKnowledgeInsert;
                public Action<HashSet<string>> DlKnowledgeInsert;
                public Action<HashSet<string>> DlKnowledgeDelete;

                public string ID(string dl) {{ return Ontorion.ID(dl); }}
                public string EN(string en){{return Ontorion.EN(en);}}
                public string InstanceDL(string en){{return Ontorion.InstanceDL(en);}}
                public void SetProgress(double completed){{Ontorion.SetProgress(completed);}}
                public void WriteMessage(int priority, string message){{Ontorion.WriteMessage(priority,message);}}
                public void WriteMessage(string message){{Ontorion.WriteMessage(0,message);}}
                public void WriteDebugMessage(string message){{Ontorion.WriteDebugMessage(message);}}
                public string UniqueID(){{return Ontorion.UniqueID();}}

                public static void BreakPoint(){{
                    System.Diagnostics.Debugger.Launch();
                    System.Diagnostics.Debugger.Break();
                }}

                public void Move(string agentId, string agentStateId, string messageId,
                    string messageTitle, Func<string> task)
                {{
                    Ontorion.AgentSupport.Move(agentId,agentStateId,messageId,messageTitle,task);
                }}

                public void Once(string uniqueness, Action task)
                {{
                    Ontorion.AgentSupport.Once(uniqueness,task);
                }}

                public dynamic External(string name)
                {{
                    return Factory.External(name,this);
                }}

                public void KnowledgeInsertWithDelay(string knowledge, int timeout_ms)
                {{
                    DlDelayedKnowledgeInsert(timeout_ms, Ontorion.KnowledgeSplit(knowledge));
                }}

                public void KnowledgeDelete(string knowledge)
                {{
                    DlKnowledgeDelete(Ontorion.KnowledgeSplit(knowledge));
                }}

                public void KnowledgeInsert(string knowledge)
                {{
                    DlKnowledgeInsert(Ontorion.KnowledgeSplit(knowledge));
                }}

                public string CreateMessage(string origin, string header, string body)
                {{
                    return Ontorion.AgentSupport.CreateMessage(origin, header,body);
                }}

                public void CreateAgent(string agentid, string state)
                {{
                    Ontorion.AgentSupport.CreateAgent(agentid,state);
                }}

                public string GetMessageBody(string messageId)
                {{
                    return Ontorion.AgentSupport.GetMessageBody(messageId);
                }}

                public string GetMessageHeader(string messageId)
                {{
                    return Ontorion.AgentSupport.GetMessageHeader(messageId);
                }}
            }}
        ";

        private static Dictionary<string, Type> TypeCache = new Dictionary<string, Type>();

        public static Type LoadRuleType(string rule, string ruleHead, List<IExeVar> vars)
        {
            lock (TypeCache)
            {
                if (!TypeCache.ContainsKey(rule))
                {
                    StringBuilder sb = new StringBuilder();
                    Dictionary<string, int> maxnos = new Dictionary<string, int>();
                    foreach (var v in vars)
                    {
                        if (v.isVar())
                        {
                            var dpos = (v as ISwrlVar).getVar().LastIndexOf('-');
                            var k = (v as ISwrlVar).getVar().Substring(0, dpos);
                            var l = (v as ISwrlVar).getVar().Substring(dpos + 1);
                            if (l[0] == 'x' || l[0] == '0')
                            {
                                if (l[0] == '0')
                                {
                                    var idx = int.Parse(l);
                                    if (!maxnos.ContainsKey("?" + k))
                                        maxnos.Add("?" + k, idx);
                                    else
                                        maxnos["?" + k] = Math.Max(maxnos["?" + k], idx);
                                }
                                else
                                {
                                    if (!maxnos.ContainsKey("?" + k))
                                        maxnos.Add("?" + k, 1);
                                }
                            }
                            else
                            {
                                var idx = int.Parse(l);
                                if (!maxnos.ContainsKey(k))
                                    maxnos.Add(k, idx);
                                else
                                    maxnos[k] = Math.Max(maxnos[k], idx);
                            }
                        }
                    }
                    sb.AppendLine("var __menum=Matches.GetEnumerator();");
                    HashSet<string> nosd = new HashSet<string>();
                    foreach (var v in vars)
                    {
                        if (v.isVar())
                        {
                            sb.AppendLine("__menum.MoveNext();");
                            var dpos = (v as ISwrlVar).getVar().LastIndexOf('-');
                            var k = (v as ISwrlVar).getVar().Substring(0, dpos);
                            var l = (v as ISwrlVar).getVar().Substring(dpos + 1);
                            if (l[0] == 'x' || l[0] == '0')
                            {
                                if (!nosd.Contains("?" + k))
                                {
                                    if ((l[0] == 'x' && maxnos["?" + k] == 1) || (l[0] == '0' && maxnos["?" + k] == int.Parse(l)))
                                    {
                                        sb.Append("var ");
                                        sb.Append(k.Replace("-", "_"));
                                        if (v is SwrlIVar)
                                            sb.AppendLine(" = ID(__menum.Current as string) ;");
                                        else
                                            sb.AppendLine(" = __menum.Current;");
                                        nosd.Add("?" + k);
                                    }
                                }
                            }
                            else
                            {
                                if (!nosd.Contains(k))
                                {
                                    sb.Append("var ");
                                    sb.Append(k.Replace("-", "_"));
                                    if (v is SwrlIVar)
                                        sb.AppendLine("= new Dictionary<int,string>();");
                                    else
                                        sb.AppendLine("= new Dictionary<int,dynamic>();");
                                    nosd.Add(k);
                                }
                                var idx = int.Parse(l);
                                if (v is SwrlIVar)
                                    sb.Append(k + ".Add(" + idx.ToString() + ", ID(__menum.Current as string));");
                                else
                                    sb.Append(k + ".Add(" + idx.ToString() + ", __menum.Current);");
                            }
                        }
                    }
                    var classSource = string.Format(classTemplate, rule, ruleHead, sb.ToString());
                    var assembly = CompileAssembly(classSource);
                    var type = assembly.GetType("Rule");
                    TypeCache[rule] = type;
                }
                return TypeCache[rule];
            }
        }

        private static Assembly CompileAssembly(string source)
        {
            var compilerParameters = new CompilerParameters()
            {
                GenerateExecutable = false,
                GenerateInMemory = true,
                ReferencedAssemblies =
                {
                    "System.dll", // needed for linq + expressions to compile
                    "System.Core.dll", // needed for linq + expressions to compile
                    "System.Data.dll", // needed for linq + expressions to compile
                    "Microsoft.CSharp.dll", // needed for linq + expressions to compile
                },
                IncludeDebugInformation = true,
            };

            string[] References = {
                };

            foreach (var reference in References)
            {
                compilerParameters.ReferencedAssemblies.Add(AppDomain.CurrentDomain.BaseDirectory
                           + string.Format("\\{0}.dll", reference));
            }

            var compileProvider = new CSharpCodeProvider();

            var fileName = Path.GetFullPath(Path.Combine(Path.GetTempPath(), Path.GetTempFileName() + ".cs"));
            File.WriteAllText(fileName, source);
            var results = compileProvider.CompileAssemblyFromFile(compilerParameters, fileName);

            //            var results = compileProvider.CompileAssemblyFromSource(compilerParameters, source);
            if (results.Errors.HasErrors)
            {
                StringBuilder msg = new StringBuilder();
                int idx = 1;
                foreach (CompilerError err in results.Errors)
                {
                    msg.AppendFormat("{1}[{2}] : {0}", err.ErrorText, err.IsWarning ? "WARNING" : "ERROR", idx++);
                    msg.AppendLine();
                }
                throw new RuleCompilationException(msg.ToString());
            }

            var assembly = results.CompiledAssembly;
            return assembly;
        }
    }
    public class RuleCompilationException : Exception
    {
        public RuleCompilationException(string msg) : base(msg)
        {
        }
    }
}
