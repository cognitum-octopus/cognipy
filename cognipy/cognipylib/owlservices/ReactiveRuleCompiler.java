package owlservices;

import cognipy.cnl.dl.*;
import java.util.*;
import java.io.*;
import java.nio.file.*;

public class ReactiveRuleCompiler
{
	private static final String classTemplate = "" + "\r\n" +
"            using System;" + "\r\n" +
"            using System.Linq;" + "\r\n" +
"            using System.Collections.Generic;" + "\r\n" +
"            using System.Linq.Expressions;" + "\r\n" +
"            " + "\r\n" +
"            public class Rule" + "\r\n" +
"            {{" + "\r\n" +
"                public void Execute(object[] Matches, Action<HashSet<string>>  knowledgeInsert,  Action<HashSet<string>> knowledgeDelete, Action<int, HashSet<string>> delayedKnowledgeInsert) {{" + "\r\n" +
" /*{1}*/" + "\r\n" +
"                    this.DlDelayedKnowledgeInsert = delayedKnowledgeInsert;" + "\r\n" +
"                    this.DlKnowledgeDelete= knowledgeDelete;" + "\r\n" +
"                    this.DlKnowledgeInsert= knowledgeInsert;" + "\r\n" +
"                    {2};" + "\r\n" +
"                    {0};" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public dynamic Ontorion;" + "\r\n" +
"                public dynamic Factory;" + "\r\n" +
"                public dynamic Outer;" + "\r\n" +
"                public dynamic Executor;" + "\r\n" +
"                public Action<int, HashSet<string>> DlDelayedKnowledgeInsert;" + "\r\n" +
"                public Action<HashSet<string>> DlKnowledgeInsert;" + "\r\n" +
"                public Action<HashSet<string>> DlKnowledgeDelete;" + "\r\n" +
"\r\n" +
"                public string ID(string dl) {{ return Ontorion.ID(dl); }}" + "\r\n" +
"                public string EN(string en){{return Ontorion.EN(en);}}" + "\r\n" +
"                public string InstanceDL(string en){{return Ontorion.InstanceDL(en);}}" + "\r\n" +
"                public void SetProgress(double completed){{Ontorion.SetProgress(completed);}}" + "\r\n" +
"                public void WriteMessage(int priority, string message){{Ontorion.WriteMessage(priority,message);}}" + "\r\n" +
"                public void WriteMessage(string message){{Ontorion.WriteMessage(0,message);}}" + "\r\n" +
"                public void WriteDebugMessage(string message){{Ontorion.WriteDebugMessage(message);}}" + "\r\n" +
"                public string UniqueID(){{return Ontorion.UniqueID();}}" + "\r\n" +
"\r\n" +
"                public static void BreakPoint(){{" + "\r\n" +
"                    System.Diagnostics.Debugger.Launch();" + "\r\n" +
"                    System.Diagnostics.Debugger.Break();" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public void Move(string agentId, string agentStateId, string messageId," + "\r\n" +
"                    string messageTitle, Func<string> task)" + "\r\n" +
"                {{" + "\r\n" +
"                    Ontorion.AgentSupport.Move(agentId,agentStateId,messageId,messageTitle,task);" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public void Once(string uniqueness, Action task)" + "\r\n" +
"                {{" + "\r\n" +
"                    Ontorion.AgentSupport.Once(uniqueness,task);" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public dynamic External(string name)" + "\r\n" +
"                {{" + "\r\n" +
"                    return Factory.External(name,this);" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public void KnowledgeInsertWithDelay(string knowledge, int timeout_ms)" + "\r\n" +
"                {{" + "\r\n" +
"                    DlDelayedKnowledgeInsert(timeout_ms, Ontorion.KnowledgeSplit(knowledge));" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public void KnowledgeDelete(string knowledge)" + "\r\n" +
"                {{" + "\r\n" +
"                    DlKnowledgeDelete(Ontorion.KnowledgeSplit(knowledge));" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public void KnowledgeInsert(string knowledge)" + "\r\n" +
"                {{" + "\r\n" +
"                    DlKnowledgeInsert(Ontorion.KnowledgeSplit(knowledge));" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public string CreateMessage(string origin, string header, string body)" + "\r\n" +
"                {{" + "\r\n" +
"                    return Ontorion.AgentSupport.CreateMessage(origin, header,body);" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public void CreateAgent(string agentid, string state)" + "\r\n" +
"                {{" + "\r\n" +
"                    Ontorion.AgentSupport.CreateAgent(agentid,state);" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public string GetMessageBody(string messageId)" + "\r\n" +
"                {{" + "\r\n" +
"                    return Ontorion.AgentSupport.GetMessageBody(messageId);" + "\r\n" +
"                }}" + "\r\n" +
"\r\n" +
"                public string GetMessageHeader(string messageId)" + "\r\n" +
"                {{" + "\r\n" +
"                    return Ontorion.AgentSupport.GetMessageHeader(messageId);" + "\r\n" +
"                }}" + "\r\n" +
"            }}" + "\r\n" +
"        ";

	private static HashMap<String, java.lang.Class> TypeCache = new HashMap<String, java.lang.Class>();

	public static java.lang.Class LoadRuleType(String rule, String ruleHead, ArrayList<IExeVar> vars)
	{
		synchronized (TypeCache)
		{
			if (!TypeCache.containsKey(rule))
			{
				StringBuilder sb = new StringBuilder();
				HashMap<String, Integer> maxnos = new HashMap<String, Integer>();
				for (IExeVar v : vars)
				{
					if (v.isVar())
					{
						int dpos = (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().lastIndexOf('-');
						String k = (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().substring(0, dpos);
						String l = (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().substring(dpos + 1);
						if (l.charAt(0) == 'x' || l.charAt(0) == '0')
						{
							if (l.charAt(0) == '0')
							{
								int idx = Integer.parseInt(l);
								if (!maxnos.containsKey("?" + k))
								{
									maxnos.put("?" + k, idx);
								}
								else
								{
									maxnos.put("?" + k, Math.max(maxnos.get("?" + k), idx));
								}
							}
							else
							{
								if (!maxnos.containsKey("?" + k))
								{
									maxnos.put("?" + k, 1);
								}
							}
						}
						else
						{
							int idx = Integer.parseInt(l);
							if (!maxnos.containsKey(k))
							{
								maxnos.put(k, idx);
							}
							else
							{
								maxnos.put(k, Math.max(maxnos.get(k), idx));
							}
						}
					}
				}
				sb.append("var __menum=Matches.GetEnumerator();" + "\r\n");
				HashSet<String> nosd = new HashSet<String>();
				for (IExeVar v : vars)
				{
					if (v.isVar())
					{
						sb.append("__menum.MoveNext();" + "\r\n");
						int dpos = (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().lastIndexOf('-');
						String k = (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().substring(0, dpos);
						String l = (v instanceof ISwrlVar ? (ISwrlVar)v : null).getVar().substring(dpos + 1);
						if (l.charAt(0) == 'x' || l.charAt(0) == '0')
						{
							if (!nosd.contains("?" + k))
							{
								if ((l.charAt(0) == 'x' && maxnos.get("?" + k).equals(1)) || (l.charAt(0) == '0' && maxnos.get("?" + k).equals(Integer.parseInt(l))))
								{
									sb.append("var ");
									sb.append(k.replace("-", "_"));
									if (v instanceof SwrlIVar)
									{
										sb.append(" = ID(__menum.Current as string) ;" + "\r\n");
									}
									else
									{
										sb.append(" = __menum.Current;" + "\r\n");
									}
									nosd.add("?" + k);
								}
							}
						}
						else
						{
							if (!nosd.contains(k))
							{
								sb.append("var ");
								sb.append(k.replace("-", "_"));
								if (v instanceof SwrlIVar)
								{
									sb.append("= new Dictionary<int,string>();" + "\r\n");
								}
								else
								{
									sb.append("= new Dictionary<int,dynamic>();" + "\r\n");
								}
								nosd.add(k);
							}
							int idx = Integer.parseInt(l);
							if (v instanceof SwrlIVar)
							{
								sb.append(k + ".Add(" + String.valueOf(idx) + ", ID(__menum.Current as string));");
							}
							else
							{
								sb.append(k + ".Add(" + String.valueOf(idx) + ", __menum.Current);");
							}
						}
					}
				}
				String classSource = String.format(classTemplate, rule, ruleHead, sb.toString());
				Assembly assembly = CompileAssembly(classSource);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var type = assembly.GetType("Rule");
				TypeCache.put(rule, type);
			}
			return TypeCache.get(rule);
		}
	}

	private static Assembly CompileAssembly(String source)
	{
		CompilerParameters compilerParameters = new CompilerParameters();
		compilerParameters.setGenerateExecutable(false);
		compilerParameters.setGenerateInMemory(true);
		compilerParameters.setReferencedAssemblies({"System.dll", "System.Core.dll", "System.Data.dll", "Microsoft.CSharp.dll"});
		compilerParameters.setIncludeDebugInformation(true);

		String[] References = { };

		for (String reference : References)
		{
			compilerParameters.ReferencedAssemblies.Add(AppDomain.CurrentDomain.BaseDirectory + String.format("\\%1$s.dll", reference));
		}

		CSharpCodeProvider compileProvider = new CSharpCodeProvider();

		String fileName = (new File(Paths.get(Path.GetTempPath()).resolve(Path.GetTempFileName() + ".cs").toString())).getAbsolutePath();
		Files.writeString(fileName, source);
		System.CodeDom.Compiler.CompilerResults results = compileProvider.CompileAssemblyFromFile(compilerParameters, fileName);

		//            var results = compileProvider.CompileAssemblyFromSource(compilerParameters, source);
		if (results.Errors.HasErrors)
		{
			StringBuilder msg = new StringBuilder();
			int idx = 1;
			for (CompilerError err : results.Errors)
			{
				msg.append(String.format("%2$s[%3$s] : %1$s", err.ErrorText, err.IsWarning ? "WARNING" : "ERROR", idx++));
				msg.append("\r\n");
			}
			throw new RuleCompilationException(msg.toString());
		}

		System.Reflection.Assembly assembly = results.CompiledAssembly;
		return assembly;
	}
}