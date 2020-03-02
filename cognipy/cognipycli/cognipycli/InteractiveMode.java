package cognipycli;

import CogniPy.*;
import Newtonsoft.Json.*;
import Newtonsoft.Json.Linq.*;
import java.util.*;
import java.io.*;

public class InteractiveMode
{
	public static void EntryPoint(String[] args)
	{
		InputStream inputStream = Console.OpenStandardInput();
		OutputStream outputStream = Console.OpenStandardOutput();

		InputStreamReader reader = new InputStreamReader(inputStream);
		OutputStreamWriter writer = new OutputStreamWriter(outputStream);

		HashMap<String, CogniPySvr> clients = new HashMap<String, CogniPySvr>();

		while (true)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var cmd = reader.ReadLine();

			if (cmd.equals("@create"))
			{
				String uid = UUID.NewGuid().toString();
				CogniPySvr fe = new CogniPySvr();
				clients.put(uid, fe);
				writer.write(uid);
			}
			else if (cmd.equals("@delete"))
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var uid = reader.ReadLine();
				clients.remove(uid);
				writer.write("@deleted");
			}
			else if (cmd == null || cmd.equals("@exit"))
			{
				break;
			}
			else
			{
				try
				{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
					var uid = reader.ReadLine();
					CogniPySvr fe = clients.get(uid);
					StringBuilder sb = new StringBuilder();
					while (true)
					{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var line = reader.ReadLine();
						if (line.equals("\0"))
						{
							break;
						}
						sb.append(line);
					}
					JsonSerializer serializer = new JsonSerializer();
					Object parms = serializer.<Object[]>Deserialize(new JsonTextReader(new StringReader(sb.toString())));
					Object ret = null;
					try
					{
						java.lang.reflect.Method method = fe.getClass().getMethod(cmd);
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
						var ptp = method.GetParameters();
						ArrayList<Object> cps = new ArrayList<Object>();
						for (int idx = 0; idx < ptp.Length; idx++)
						{
							Object cp;
							if (parms[idx] instanceof JToken)
							{
								cp = (parms[idx] instanceof JToken ? (JToken)parms[idx] : null).ToObject(ptp[idx].ParameterType);
							}
							else
							{
								cp = parms[idx];
							}
							cps.add(cp);
						}

						ret = method.Invoke(fe, cps.toArray(new Object[0]));
					}
					catch (AmbiguousMatchException e)
					{
						ret = fe.getClass().InvokeMember(cmd, BindingFlags.DeclaredOnly.getValue() | BindingFlags.Public.getValue() | BindingFlags.NonPublic.getValue() | BindingFlags.Instance.getValue() | BindingFlags.InvokeMethod.getValue(), null, fe, parms);
					}

					writer.write("@result" + System.lineSeparator());
					serializer.Serialize(new JsonTextWriter(writer), ret);
				}
				catch (RuntimeException ex)
				{
					writer.write("@exception" + System.lineSeparator());
					JsonTextWriter w = new JsonTextWriter(writer);
					JsonSerializer serializer = new JsonSerializer();
					serializer.Serialize(w, ex);
				}
			}
			writer.WriteLine();
			writer.write("\0" + System.lineSeparator());
			writer.flush();
		}
	}
}