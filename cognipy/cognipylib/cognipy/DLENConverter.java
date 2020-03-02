package cognipy;

import java.util.*;

public class DLENConverter
{
	private cognipy.cnl.CNLTools tools;
	private tangible.Func1Param<String, String> _ns2pfx;
	private tangible.Func1Param<String, String> getPfx2NsSource;
	private String _defaultNamespace;

	public DLENConverter(cognipy.cnl.CNLTools tools, tangible.Func1Param<String, String> ns2pfx, tangible.Func1Param<String, String> getPfx2NsSource, String defaultNamespaceProvider) //Func<string, string, string> pfx2ns,
	{
		this.tools = tools;
		this._ns2pfx = (String arg) -> ns2pfx.invoke(arg);
		this.getPfx2NsSource = (String arg) -> getPfx2NsSource.invoke(arg);
		this._defaultNamespace = defaultNamespaceProvider;
	}


	public final String DL(String en)
	{
		return DL(en, cognipy.cnl.en.endict.WordKind.NormalForm);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string DL(string en, CogniPy.CNL.EN.endict.WordKind wkrd = CogniPy.CNL.EN.endict.WordKind.NormalForm)
	public final String DL(String en, cognipy.cnl.en.endict.WordKind wkrd)
	{
		if (en.startsWith("a ") || en.startsWith("an "))
		{
			en = en.split("[ ]", -1).Last();
		}
		cognipy.cnl.en.EnName allParts = new cognipy.cnl.en.EnName();
		allParts.id = en;
		if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && !allParts.term.Contains("<"))
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var tterm = getPfx2NsSource.invoke(allParts.term);
			if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
			{
				allParts.term = "<" + tterm + ">";
			}
			else
			{
				throw new RuntimeException("No namespace found for prefix " + allParts.term + ". You need to define it before saving into Ontorion.");
			}
		}
		else if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && allParts.term.startsWith("<") && allParts.term.endsWith(">"))
		{
			String tterm = cognipy.cnl.CNLTools.GetCanonicalNs(allParts.term.substring(1, 1 + allParts.term.Length - 2)); // string without < and >
			allParts.term = "<" + tterm + ">";
		}
		else //add the default namespace
		{
			String defaultNs = _defaultNamespace;
			allParts.term = String.format("<%1$s>", defaultNs);
		}

		return cognipy.cnl.en.ENNameingConvention.ToDL(allParts.Combine(), wkrd).id;
	}

	public final String CanonToEng(String symbol)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var ea = symbol.split(new char[] {':'}, StringSplitOptions.RemoveEmptyEntries);
		boolean BigName = false;
		String pfx = "";
		if (ea[0].equals("C"))
		{
			pfx = "a ";
		}
		else if (ea[0].equals("I"))
		{
			BigName = true;
		}
		return pfx + EN(symbol.substring(2, symbol.length()), BigName);
	}

	public final String CNLQueryToDL(String query)
	{
		query = query.replace((char)160, ' ');
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var stmt = tools.GetEN2DLAst("Every loooooked-for is " + query + " .", true, true, getPfx2NsSource).Statements.get(0);
		cognipy.cnl.dl.Paragraph tempVar = new cognipy.cnl.dl.Paragraph(null);
		tempVar.Statements = new ArrayList<cognipy.cnl.dl.Statement>(Arrays.asList(stmt));
		return tools.SerializeDLAst(tempVar);
	}

	public final String EngToCanon(String expr)
	{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var ea = expr.split(new char[] {'\r', '\n', '\t', ' '}, StringSplitOptions.RemoveEmptyEntries);
		String pfx = "";
		cognipy.cnl.en.endict.WordKind wk = cognipy.cnl.en.endict.WordKind.NormalForm;
		if (ea.Length == 2 && (ea[0].toLowerCase().equals("a") || ea[0].toLowerCase().equals("an")))
		{
			pfx = "C:";
		}
		else if (ea.Length == 1)
		{
			if (ea[0].Length > 1)
			{
				if (Character.isUpperCase(ea[0][0]))
				{
					pfx = "I:";
				}
				else
				{
					pfx = "R:";
					wk = cognipy.cnl.en.endict.WordKind.PastParticiple;
				}
			}
		}
		return pfx + DL(ea[ea.Count() - 1], wk);
	}

	public final String ENNamespaceToPrefix(String en)
	{
		cognipy.cnl.en.EnName allParts = new cognipy.cnl.en.EnName();
		allParts.id = en;
		if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && allParts.term.startsWith("<") && allParts.term.endsWith(">"))
		{
			String nss = allParts.term.substring(1, 1 + allParts.term.Length - 2);
			if (_defaultNamespace.equals(nss)) // remove if the namespace is the default one.
			{
				allParts.term = null;
			}
			else
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var tterm = _ns2pfx.invoke(nss);
				if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
				{
					allParts.term = tterm;
				}
			}
		}

		return allParts.Combine().id;
	}


	public final String EN(String dl, boolean bigName)
	{
		return EN(dl, bigName, cognipy.cnl.en.endict.WordKind.NormalForm);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public string EN(string dl, bool bigName, CogniPy.CNL.EN.endict.WordKind wrdKnd = CogniPy.CNL.EN.endict.WordKind.NormalForm)
	public final String EN(String dl, boolean bigName, cognipy.cnl.en.endict.WordKind wrdKnd)
	{
		if (dl.equals("⊤"))
		{
			return "thing";
		}

		cognipy.cnl.dl.DlName tempVar = new cognipy.cnl.dl.DlName();
		tempVar.id = dl;
		cognipy.cnl.en.EnName.Parts allParts = cognipy.cnl.en.ENNameingConvention.FromDL(tempVar, wrdKnd, bigName).Split();
		if (!tangible.StringHelper.isNullOrWhiteSpace(allParts.term) && allParts.term.startsWith("<") && allParts.term.endsWith(">"))
		{
			String nss = allParts.term.substring(1, 1 + allParts.term.length() - 2);
			if (_defaultNamespace.equals(nss)) // remove if the namespace is the default one.
			{
				allParts.term = null;
			}
			else
			{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
				var tterm = _ns2pfx.invoke(nss);
				if (!tangible.StringHelper.isNullOrWhiteSpace(tterm))
				{
					allParts.term = tterm;
				}
			}
		}

		return allParts.Combine().id;
	}
}