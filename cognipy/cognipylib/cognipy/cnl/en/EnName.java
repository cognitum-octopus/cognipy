package cognipy.cnl.en;

import cognipy.cnl.dl.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class EnName
{
	public String id;

	private static final String PFX = "The-";

	public static class Parts
	{
		public boolean quoted;
		public enum Kind
		{
			Name,
			BigName,
			VeryBigName;

			public static final int SIZE = java.lang.Integer.SIZE;

			public int getValue()
			{
				return this.ordinal();
			}

			public static Kind forValue(int value)
			{
				return values()[value];
			}
		}
		public Kind kind = Kind.values()[0];

		public String name;
		public String term = null;

		private static boolean IsUrlWellFormed(String url)
		{
			Uri uriRes;
			if (url != null)
			{
				if (!url.startsWith("http://"))
				{
					return false;
				}
				else
				{
					tangible.OutObject<System.Uri> tempOut_uriRes = new tangible.OutObject<System.Uri>();
					if (!Uri.TryCreate(url, UriKind.Absolute, tempOut_uriRes))
					{
					uriRes = tempOut_uriRes.argValue;
						return false;
					}
				else
				{
					uriRes = tempOut_uriRes.argValue;
				}
				}
			}
			else
			{
				return false;
			}
			return true;
		}

		private String encode(String str)
		{
			return "\"" + str.replace("\"", "\"\"") + "\"";
		}
		public final EnName Combine()
		{
			StringBuilder sb = new StringBuilder();
			if (quoted)
			{
				if (kind == Kind.BigName)
				{
					sb.append(PFX);
				}
				else if (kind == Kind.VeryBigName)
				{
					sb.append(PFX.toUpperCase());
				}
				sb.append(encode(name));
			}
			else
			{
				sb.append(name);
			}
			if (term != null)
			{
				sb.append("[");
				if (IsUrlWellFormed(term))
				{
					sb.append("<" + term + ">");
				}
				else
				{
					sb.append(term);
				}
				sb.append("]");
			}
			EnName tempVar = new EnName();
			tempVar.id = sb.toString();
			return tempVar;
		}

		public final Parts Clone()
		{
			Parts tempVar = new Parts();
			tempVar.kind = this.kind;
			tempVar.name = this.name;
			tempVar.quoted = this.quoted;
			tempVar.term = this.term;
			return (tempVar);
		}
	}

//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [ThreadStatic] static Dictionary<string, Parts> cache = null;
	private static HashMap<String, Parts> cache = null;

	public final Parts Split()
	{
		if (cache == null)
		{
			cache = new HashMap<String, Parts>();
		}
		if (id == null)
		{
		}
		if (!cache.containsKey(id))
		{
			CNLFactory factory = new CNLFactory();
			tools.Lexer lexer = factory.getLexer();
			lexer.Start(id);
			tools.TOKEN token = lexer.Next();
			Parts ret = new Parts();
			if (token == null)
			{
				return ret;
			}
			String tokStr = token.getYytext();
			String termsStr = "";
			if (token.getYytext().endsWith("]"))
			{
				int trmp = token.getYytext().lastIndexOf('[');
				termsStr = token.getYytext().substring(trmp);
				tokStr = token.getYytext().substring(0, token.getYytext().length() - termsStr.length()).trim();
				ret.term = termsStr.substring(1, 1 + termsStr.length() - 2);
			}

			if (token instanceof VERYBIGNAME)
			{
				ret.kind = Parts.Kind.VeryBigName;
			}
			else if (token instanceof BIGNAME)
			{
				ret.kind = Parts.Kind.BigName;
			}
			else if (token instanceof NAME)
			{
				ret.kind = Parts.Kind.Name;
			}

			ret.quoted = (tokStr.startsWith(PFX + "\"") || tokStr.startsWith(PFX.toUpperCase() + "\"")) && tokStr.endsWith("\"");
			if (ret.quoted)
			{
				ret.name = tangible.StringHelper.substring(tokStr, PFX.length() + 1, tokStr.length() - PFX.length() - 2).replace("\"\"", "\"");
			}
			else
			{
				ret.quoted = tokStr.startsWith("\"") && tokStr.endsWith("\"");
				if (ret.quoted)
				{
					ret.name = tokStr.substring(1, 1 + tokStr.length() - 2).replace("\"\"", "\"");
				}
				else
				{
					ret.name = tokStr;
				}
			}

			cache.put(id, ret);
			return ret.Clone();
		}
		return cache.get(id).Clone();
	}
}