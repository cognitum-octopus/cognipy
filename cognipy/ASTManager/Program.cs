using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using CommandLine;
using Newtonsoft.Json;

namespace ASTManager
{

    class MainClass
    {
        public class Options
        {
            [Value(0, MetaName = "command",
                HelpText = "one of: generate, mangle",
                Required = true)]
            public string Command { get; set; }

            [Option('i', "input", HelpText = "Input file", Required = true)]
            public string Input { get; set; }

            [Option('o', "output", HelpText = "Output file")]
            public string Output { get; set; }
        }

        public class Generator
        {
            public string name;
            public object[] args=null;
            public int cnt = 1;
        }

        public class ExampleGeneratorSetup
        {
            public Int64 seed;
            public string[] nouns;
            public string[] roles;
            public string[] dataroles;
            public string[] big_names;
            public string[] strings;
            public Int64 min_int;
            public Int64 max_int;
            public double min_float;
            public double max_float;
            public Generator[] generators;
        }

        static void Main(string[] args)
        {
            Parser.Default.ParseArguments<Options>(args)
                   .WithParsed<Options>(o =>
                   {
                       string outtext = "";
                       var cpy = new CogniPy.CogniPySvr();

                       if (o.Command == "mangle")
                       {
                           outtext = cpy.MangleCnl(File.ReadAllText(o.Input));
                       }
                       else if (o.Command == "maketempgen")
                       {
                           JsonSerializer serializer = JsonSerializer.Create(new JsonSerializerSettings
                           {
                               Formatting = Newtonsoft.Json.Formatting.Indented,
                               ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore
                           });
                           var genset = new ExampleGeneratorSetup();
                           genset.big_names = cpy.tools.example_big_names.ToArray();
                           genset.nouns = cpy.tools.example_nouns.ToArray();
                           genset.roles = cpy.tools.example_roles.ToArray();
                           genset.dataroles = cpy.tools.example_dataroles.ToArray();
                           genset.generators = new List<Generator>() { new Generator() { name = "genname", args = new string[] { "x" }, cnt=1 } }.ToArray();
                           using (StreamWriter file = File.CreateText(o.Input))
                               serializer.Serialize(new JsonTextWriter(file), genset);
                       }
                       else if (o.Command == "generate")
                       {
                           JsonSerializer serializer = JsonSerializer.Create(new JsonSerializerSettings
                           {
                               Formatting = Newtonsoft.Json.Formatting.Indented,
                               ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore
                           });
                           var genset = serializer.Deserialize<ExampleGeneratorSetup>(new JsonTextReader(File.OpenText(o.Input)));
                           cpy.tools.reset_random_seed(genset.seed);
                           cpy.tools.set_example_big_names(new List<string>(genset.big_names));
                           cpy.tools.set_example_nouns(new List<string>(genset.nouns));
                           cpy.tools.set_example_roles(new List<string>(genset.roles));
                           cpy.tools.set_example_dataroles(new List<string>(genset.dataroles));
                           cpy.tools.set_example_strings(new List<string>(genset.strings));
                           cpy.tools.min_int = (int)genset.min_int;
                           cpy.tools.max_int = (int)genset.max_int;
                           cpy.tools.min_float = genset.min_float;
                           cpy.tools.max_float = genset.max_float;
                           var sb = new StringBuilder();
                           foreach (var gen in genset.generators)
                           {
                               for (int i = 0; i < gen.cnt; i++)
                               {
                                   var method = cpy.tools.GetType().GetMethod(gen.name);
                                   var ptp = method.GetParameters();
                                   var cps = new List<object>();
                                   if (gen.args != null)
                                       cps = new List<object>(gen.args);

                                   var ret = method.Invoke(cpy.tools, cps.ToArray()) as string;
                                   sb.AppendLine(ret);
                               }
                           }
                           outtext = sb.Replace("^","").ToString();
                       }
                       else
                       {
                           throw new NotImplementedException();
                       }

                       if (o.Output is null)
                       {
                           Console.WriteLine(outtext);
                       }
                       else
                       {
                           File.WriteAllText(o.Output, outtext);
                       }
                   });
        }
    }
}
