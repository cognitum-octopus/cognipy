using CogniPy;
using CogniPy.Splitting;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace ConsoleApp1
{
    internal class Program
    {
        static void Main(string[] args)
        {
            var feClient = new CogniPySvr();

            if(true)
            {
                feClient.LoadCnl("D:\\ROOT\\causalspark\\gUFO\\gufo.encnl", true, true);
                feClient.GetAnnotationValue("partition", "\"comment\"[rdfs]", "en", "Concept");
            }

            if (false)
            {
                var cnlSentences = new List<string>() {
                    "Example-Sport-Enterprise is a sport-enterprise .",
                    "Example-Person-01 is a person.",
                    "If a sport-enterprise exists then it must be-true-that the person holds-senior-leadership-role-in the sport-enterprise and the person is-responsible-for Sustainability.",
                    "Every holds-senior-leadership-role-in-proof is a proof.",
                    "If a proof is a holds-senior-leadership-role-in-proof and the proof has-subject a thing(1) and the proof has-object a thing(2) then the thing(1) holds-senior-leadership-role-in the thing(2).",
                    "Every is-responsible-for-proof is a proof.",
                    "Every is-responsible-for-proof must be a proof.",
                    "If a proof is a is-responsible-for-proof and the proof has-subject a thing(1) and the proof has-object a thing(2) then the thing(1) is-responsible-for the thing(2).",
                    "Example-Website-Proof-02 is a is-responsible-for-proof and has-subject Example-Person-01 and has-object Sustainability.",
                    "Example-Website-Proof-01 is a holds-senior-leadership-role-in-proof and has-subject Example-Person-01 and has-object Example-Sport-Enterprise."
            };

                feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true, true);
                feClient.GetSuperConceptsOf("a thing", false);
                Console.WriteLine(feClient.GetReasoningInfo());

                feClient.LoadModularizer();
                var mod = feClient.GetModule("", new string[] { "Girls" });

                Console.WriteLine(mod);
            }

        }
    }
}
