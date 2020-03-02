package cognipy.cnl.en;

import cognipy.*;
import cognipy.cnl.*;
import java.util.*;
import java.io.*;

public class endict
{
	private HashMap<String, String> pp = new HashMap<String, String>();
	private HashMap<String, String> i_pp = new HashMap<String, String>();
	private HashMap<String, String> sp = new HashMap<String, String>();
	private HashMap<String, String> i_sp = new HashMap<String, String>();
	private HashMap<String, String> pl = new HashMap<String, String>();
	private HashMap<String, String> i_pl = new HashMap<String, String>();

	public endict(InputStream s)
	{
		if (s == null)
		{
			return;
		}

		InputStreamReader sr = new InputStreamReader(s);
		try
		{
			init(sr);
		}
		catch (RuntimeException ex)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
			Debugger.Break();
//#endif
			throw ex;
		}
	}

	private void init(InputStreamReader tr)
	{
		while (!tr.EndOfStream)
		{
			if (tr.ReadLine().startsWith("%plural form"))
			{
				break;
			}
		}
		while (!tr.EndOfStream)
		{
			String str = tr.ReadLine();
			if (str.startsWith("%simple past"))
			{
				break;
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var v = str.split("[:]", -1);
			pl.put(v[1], v[0]);
			i_pl.put(v[0], v[1]);
		}
		while (!tr.EndOfStream)
		{
			String str = tr.ReadLine();
			if (str.startsWith("%past participle"))
			{
				break;
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var v = str.split("[:]", -1);
			sp.put(v[1], v[0]);
			i_sp.put(v[0], v[1]);
		}
		while (!tr.EndOfStream)
		{
			String str = tr.ReadLine();
			if (str == null)
			{
				break;
			}
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var v = str.split("[:]", -1);
			pp.put(v[1], v[0]);
			i_pp.put(v[0], v[1]);
		}

	}

	public enum WordKind
	{
		PastParticiple,
		SimplePast,
		PluralFormNoun,
		PluralFormVerb,
		NormalForm;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static WordKind forValue(int value)
		{
			return values()[value];
		}
	}

	public final String toDL_Simple(String A, WordKind k)
	{
		if (A.length() > 0 && Character.isDigit(A.charAt(0)))
		{
			return A;
		}

		if (k == WordKind.SimplePast)
		{
			if (A.equals("was"))
			{
				return "was";
			}
			else if (sp.containsKey(A))
			{
				return sp.get(A);
			}
			else
			{
				if (A.endsWith("ed") && A.length() != 2)
				{
					return A.substring(0, A.length() - 2);
				}
				else if (A.endsWith("d") && A.length() != 1)
				{
					return A.substring(0, A.length() - 1);
				}
				else
				{
					return A;
				}
			}
		}
		else if (k == WordKind.PastParticiple)
		{
			if (pp.containsKey(A))
			{
				return pp.get(A);
			}
			else
			{
				if (A.equals("was"))
				{
					return "was";
				}
				else if (A.equals("is") || A.equals("are"))
				{
					return "be";
				}
				else if (A.endsWith("s") && A.length() != 1)
				{
					return A.substring(0, A.length() - 1);
				}
				else
				{
					return A;
				}
			}
		}
		else if (k == WordKind.PluralFormNoun)
		{
			if (pl.containsKey(A))
			{
				return pl.get(A);
			}
			else
			{
				if (A.endsWith("s") && A.length() != 1)
				{
					return A.substring(0, A.length() - 1);
				}
				else
				{
					return A;
				}
			}
		}
		else if (k == WordKind.PluralFormVerb)
		{
			if (A.equals("was"))
			{
				return "was";
			}
			else if (A.equals("are") || A.equals("is"))
			{
				return "be";
			}
			else
			{
				return A;
			}
		}
		else
		{
			return A;
		}
	}

	public final String toN_Simple(String A, WordKind k)
	{
		if (A.length() > 0 && Character.isDigit(A.charAt(0)))
		{
			return A;
		}

		if (k == WordKind.SimplePast)
		{
			if (i_sp.containsKey(A))
			{
				return i_sp.get(A);
			}
			if (A.equals("was"))
			{
				return "was";
			}
			else if (A.equals("be"))
			{
				return "is";
			}
			else
			{
				if (A.endsWith("e") && A.length() != 1)
				{
					return A + "d";
				}
				else
				{
					return A + "ed";
				}
			}
		}
		else if (k == WordKind.PastParticiple)
		{
			if (i_pp.containsKey(A))
			{
				return i_pp.get(A);
			}
			else if (A.equals("was"))
			{
				return "was";
			}
			else if (A.equals("be"))
			{
				return "is";
			}
			else
			{
				return A + "s";
			}
		}
		else if (k == WordKind.PluralFormNoun)
		{
			if (i_pl.containsKey(A))
			{
				return i_pl.get(A);
			}
			else
			{
				return A + "s";
			}
		}
		else if (k == WordKind.PluralFormVerb)
		{
			if (A.equals("was"))
			{
				return "was";
			}
			else if (A.equals("be"))
			{
				return "are";
			}
			else
			{
				return A;
			}
		}
		else
		{
			return A;
		}
	}
}