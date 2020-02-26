using CommandLine;
using CommandLine.Text;
using System;
using System.Collections.Generic;

namespace CogniPyCLI
{
    public sealed partial class Options
    {
        public Options()
        {
        }

        [Option('i', "interactive", Required = false, Default = false, HelpText = "Open in the interactive mode?")]
        public bool Interactive { get; set; }

        [Option('f', "file", HelpText = "Input CNL file", Required = false)]
        public string InputFile { get; set; }

        [Usage(ApplicationAlias = "CogniPy")]
        public static IEnumerable<Example> Examples
        {
            get
            {
                yield return new Example("Normal scenario", new Options { InputFile = "file.encnl" });
            }
        }

    }


    class Program
    {
        static void Main(string[] args)
        {
            Parser.Default.ParseArguments<Options>(args)
                               .WithParsed<Options>(options =>
                               {
#if !DEBUG
            try
#endif
                                   {
                                       InteractiveMode.EntryPoint(options);
                                       Environment.Exit(0);
                                   }
#if !DEBUG
            catch (Exception ex)
            {
                Console.ForegroundColor = ConsoleColor.Black;
                Console.BackgroundColor = ConsoleColor.Red;
                Console.Error.WriteLine(ex.Message);
                Console.Error.WriteLine(ex.StackTrace);
                Environment.Exit(2);
            }
#endif
                               });

        }
    }
}
