package cognipy.cnl.en;

import cognipy.cnl.dl.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public final class ENNameingConvention
{
	public static final String TOPROLENAME = "\"<->\"";
	public static final String BOTTOMROLENAME = "\"<x>\"";

	public static DlName ToDL(EnName eng, cognipy.cnl.en.endict.WordKind kind)
	{
		cognipy.cnl.en.EnName.Parts parts = eng.Split();
		DlName.Parts dlp = new DlName.Parts();
		dlp.term = parts.term;
		dlp.local = parts.kind == EnName.Parts.Kind.BigName;
		dlp.quoted = parts.quoted;

		if (parts.quoted || parts.kind != EnName.Parts.Kind.Name || kind == endict.WordKind.NormalForm)
		{
			dlp.name = parts.name;
		}
		else
		{
			String name = parts.name;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var arr = name.split("[-]", -1);
			if (kind == endict.WordKind.SimplePast || kind == endict.WordKind.PastParticiple || kind == endict.WordKind.PluralFormVerb)
			{
				arr[0] = CNLFactory.lex.toDL_Simple(arr[0], kind);
			}
			else if (kind == endict.WordKind.PluralFormNoun)
			{
				arr[arr.Length - 1] = CNLFactory.lex.toDL_Simple(arr[arr.Length - 1], kind);
			}
			dlp.name = tangible.StringHelper.join("-", arr);
		}
		return dlp.Combine();
	}

	public static EnName FromDL(DlName dl, endict.WordKind kind, boolean bigName)
	{
		cognipy.cnl.dl.DlName.Parts dlp = dl.Split();
		EnName.Parts parts = new EnName.Parts();
		parts.kind = dlp.local ? EnName.Parts.Kind.BigName : (bigName ? EnName.Parts.Kind.VeryBigName : EnName.Parts.Kind.Name);
		parts.term = dlp.term;
		parts.quoted = dlp.quoted;
		if (dlp.quoted || bigName || kind == endict.WordKind.NormalForm)
		{
			if (cognipy.cnl.en.KeyWords.Me.isKeyword(dlp.name))
			{
				parts.quoted = true;
			}
			parts.name = dlp.name;
		}
		else
		{
			String name = dlp.name;
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var arr = name.split("[-]", -1);
			if (kind == endict.WordKind.SimplePast || kind == endict.WordKind.PastParticiple || kind == endict.WordKind.PluralFormVerb)
			{
				arr[0] = CNLFactory.lex.toN_Simple(arr[0], kind);
			}
			else if (kind == endict.WordKind.PluralFormNoun)
			{
				arr[arr.Length - 1] = CNLFactory.lex.toN_Simple(arr[arr.Length - 1], kind);
			}
			parts.name = tangible.StringHelper.join("-", arr);
		}
		return parts.Combine();
	}

	public static EnName FromDL(DlName dl, boolean bigName)
	{
		return FromDL(dl, endict.WordKind.NormalForm, bigName);
	}
}