using CogniPy;

namespace Tester
{
    internal class Program
    {
        static void Main(string[] args)
        {
            var cnlSentences = new List<string>() {
                    "John is a man.",
                    "John has-nickname equal-to 'Jojo'.",
                    "John has-friend Martha.",
                    "Mary is a man.",
                    "Mary has-nickname equal-to 'Mojo'.",
                    "Mary has-friend Martha."
            };

            var feClient = new CogniPySvr();
            feClient.LoadCnlFromString(string.Join("\r\n", cnlSentences), true, true);

        }
    }
}
