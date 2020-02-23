using CogniPy;
using Newtonsoft.Json;
using Ontorion.FluentEditorClient;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Reflection;
using System.Text;
using System.Threading.Tasks;

namespace CogniPyCLI
{
    class InteractiveMode
    {
        public static void EntryPoint(Options options)
        {
            Stream inputStream = Console.OpenStandardInput();
            Stream outputStream = Console.OpenStandardOutput();

            var reader = new StreamReader(inputStream);
            var writer = new StreamWriter(outputStream);

            Dictionary<string, CogniPySvr> clients = new Dictionary<string, CogniPySvr>();

            while (true)
            {
                var cmd = reader.ReadLine();

                if (cmd == "@create")
                {
                    var uid = Guid.NewGuid().ToString();
                    var fe = new CogniPySvr();
                    clients.Add(uid, fe);
                    writer.Write(uid);
                }
                else if (cmd == "@delete")
                {
                    var uid = reader.ReadLine();
                    clients.Remove(uid);
                    writer.Write("@deleted");
                }
                else if (cmd==null || cmd == "@exit")
                {
                    break;
                }
                else
                {
                    try
                    {
                        var uid = reader.ReadLine();
                        var fe = clients[uid];
                        StringBuilder sb = new StringBuilder();
                        while(true)
                        {
                            var line = reader.ReadLine();
                            if (line == "\0")
                                break;
                            sb.Append(line);
                        }
                        var sread = new StringReader(sb.ToString());
                        var r = new JsonTextReader(sread);
                        var w = new JsonTextWriter(writer);
                        JsonSerializer serializer = new JsonSerializer();
                        var parms = serializer.Deserialize<object[]>(r);
                        var ret = fe.GetType().InvokeMember(cmd, BindingFlags.DeclaredOnly |
                                BindingFlags.Public | BindingFlags.NonPublic |
                                BindingFlags.Instance | BindingFlags.InvokeMethod, null, fe, parms);
                        writer.WriteLine("@result");
                        serializer.Serialize(w, ret);
                    }
                    catch(Exception ex)
                    {
                        writer.WriteLine("@exception");
                        var w = new JsonTextWriter(writer);
                        JsonSerializer serializer = new JsonSerializer();
                        serializer.Serialize(w, ex);
                    }
                }
                writer.WriteLine();
                writer.WriteLine("\0");
                writer.Flush();
            }
        }
    }
}
