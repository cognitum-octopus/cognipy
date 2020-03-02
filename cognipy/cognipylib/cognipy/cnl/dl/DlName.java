package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class DlName
{

	public String id;
	public static class Parts
	{
		public boolean quoted;
		public boolean local;
		public String name;
		public String term = null;

		public final DlName Combine()
		{
			DlName tempVar = new DlName();
			tempVar.id = (local ? "_" : "") + (quoted ? "\"" + name.replace("\"", "\"\"") + "\"" : name) + (term != null ? (":" + term) : "");
			return tempVar;
		}

		public final Parts Clone()
		{
			Parts tempVar = new Parts();
			tempVar.local = this.local;
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

		if (!cache.containsKey(id))
		{
			Parts ret = new Parts();
			ret.local = id.startsWith("_");
			String name = id.substring(ret.local ? 1 : 0);
			int ddpos = name.indexOf(':');
			if (ddpos >= 0)
			{
				ret.term = name.substring(ddpos + 1);
				name = name.substring(0, ddpos);
			}
			ret.quoted = name.startsWith("\"");
			ret.name = ret.quoted ? name.substring(1, 1 + name.length() - 2).replace("\"\"", "\"") : name;
			cache.put(id, ret);
			return ret.Clone();
		}
		return cache.get(id).Clone();
	}
}