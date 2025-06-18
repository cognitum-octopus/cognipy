using System;
using System.Collections.Generic;
using System.IO;
using System.Text;
using CogniPy;
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
                            var genset = File.ReadAllText(o.Input);
                            outtext = cpy.GenerateExamples(genset);
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
