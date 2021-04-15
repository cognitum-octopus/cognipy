using CogniPy;
using Newtonsoft.Json;
using Newtonsoft.Json.Linq;
using System;
using System.Collections.Generic;
using System.IO;
using System.Reflection;
using System.Text;

namespace CogniPyCLI
{
    class InteractiveMode
    {
         public static void EntryPoint(string[] args)
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
                else if (cmd == null || cmd == "@exit")
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
                        while (true)
                        {
                            var line = reader.ReadLine();
                            if (line == "\0")
                                break;
                            sb.Append(line);
                        }
                        JsonSerializer serializer = JsonSerializer.Create(new JsonSerializerSettings
                        {
                            Formatting = Newtonsoft.Json.Formatting.Indented,
                            ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore
                        });
                        var parms = serializer.Deserialize<object[]>(new JsonTextReader(new StringReader(sb.ToString())));
                        object ret = null;
                        try
                        {
                            var method = fe.GetType().GetMethod(cmd);
                            var ptp = method.GetParameters();
                            var cps = new List<object>();
                            for (var idx = 0; idx < ptp.Length; idx++)
                            {
                                object cp;
                                if (parms[idx] is JToken)
                                    cp = (parms[idx] as JToken).ToObject(ptp[idx].ParameterType);
                                else
                                    cp = parms[idx];
                                cps.Add(cp);
                            }

                            ret = method.Invoke(fe, cps.ToArray());
                        }
                        catch (AmbiguousMatchException)
                        { 
                            ret = fe.GetType().InvokeMember(cmd, BindingFlags.DeclaredOnly |
                                                               BindingFlags.Public | BindingFlags.NonPublic |
                                                               BindingFlags.Instance | BindingFlags.InvokeMethod,
                                null, fe, parms);
                        }

                        writer.WriteLine("@result");
                        serializer.Serialize(new JsonTextWriter(writer), ret);
                    }
                    catch (Exception ex)
                    {
                        if (ex is TargetInvocationException)
                            ex = ex.InnerException;

                        writer.WriteLine("@exception");
                        var w = new JsonTextWriter(writer);
                        JsonSerializer serializer = JsonSerializer.Create(new JsonSerializerSettings
                        {
                            Formatting = Newtonsoft.Json.Formatting.Indented,
                            ReferenceLoopHandling = Newtonsoft.Json.ReferenceLoopHandling.Ignore
                        });
                        serializer.Serialize(w,new object[] { ex.GetType().Name, ex });
                    }
                }
                writer.WriteLine();
                writer.WriteLine("\0");
                writer.Flush();
            }
        }
    }
}
