package cognipy.cnl.en;

import cnl.net.*;
import cognipy.*;
import cognipy.cnl.*;

public class TaxonomyNodeEN extends TaxonomyNode
{
	public TaxonomyNodeEN(cognipy.collections.IInvokableProvider invokableProvider)
	{
		super(invokableProvider);
	}

	@Override
	public String getENText()
	{
		if (cachedENText == null)
		{
			StringBuilder sb = new StringBuilder();
			boolean first = true;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			for (var s : names)
			{
				if (s == null)
				{
					continue;
				}
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(" ≡ ");
				}

				String ident;
				if (s.EndsWith("⁻"))
				{
					cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
					tempVar.id = s.EndsWith("⁻") ? s.Substring(0, s.Length - 1) : s;
					ident = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar, CNL.EN.endict.WordKind.SimplePast, false).id + " by";
				}
				else
				{
					cognipy.cnl.dl.DlName tempVar2 = new cognipy.cnl.dl.DlName();
					tempVar2.id = s;
					ident = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar2, false).id;
				}

				if (s.StartsWith("["))
				{
					String sts = s.Substring(1, s.Length - 2).trim();
					if (sts.equals("⊤"))
					{
						sts = "thing";
					}
					sb.append("the " + sts);
				}
				else
				{
					sb.append(ident);
				}

			}
			cachedENText = sb.toString();
		}
		return cachedENText;
	}

	public final String getTextNoPrefix()
	{
		String enText = this.getENText();
		if (enText.endsWith("]"))
		{
				// we should check here if we are in Ontorion mode, otherwise it will change the way in which it is displayed also on the normal taxonomy!
			int indx = enText.indexOf("[");
			if (indx != -1)
			{
				String str = enText.substring(indx);
				if (str != null)
				{
					this.setPrefix(str);
					enText = tangible.StringHelper.remove(enText, indx);
				}
			}
		}

		return enText;
	}
}