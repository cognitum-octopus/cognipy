using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace Cognitum.OwlApi.Net.Test
{
    class Program
    {
        static void Main(string[] args)
        {
            AppDomain.CurrentDomain.AssemblyResolve += new ResolveEventHandler(TryOwlReasoner.CurrentDomain_AssemblyResolve);

            TryOwlReasoner.GetOwlInfo(@"Reasoners\ELK\");

            TryOwlReasoner.GetOwlInfo(@"Reasoners\JFact\");

            TryOwlReasoner.GetOwlInfo(@"Reasoners\HermiT\");

            TryOwlReasoner.GetOwlInfo(@"Reasoners\Pellet\");

            Examples ex = new Examples();

            ex.shouldLoad();
            Console.ReadLine();
        }
    }

}
