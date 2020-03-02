package cognipy.ars;

import cognipy.cnl.dl.*;
import org.semanticweb.owlapi.model.*;
import cognipy.*;
import java.util.*;

public class IRIParser
{
	private static ArrayList<Character> CharsForInternalUse = new ArrayList<Character>(Arrays.asList('\u0001', '\u0002'));
	public static String getFirstCharForInternalUse()
	{
		return CharsForInternalUse.get(0).toString();
	}
	public static String getSecondCharForInternalUse()
	{
		return CharsForInternalUse.get(1).toString();
	}
	private static char[] URLUnreserved = new char[] {'-', '.', '_', '~'};

	/** 
	 Encodes an input string into an IRI
	 
	 @param input
	 @return 
	*/
	public static String encodeToIRI(String input)
	{
		StringBuilder strBld = new StringBuilder();

//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var charStr = input.toCharArray();
		int len = charStr.Length;
		for (int i = 0; i < len; i++)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
			var el = charStr[i];
			if (CharsForInternalUse.contains(el))
			{
				strBld.append(Uri.EscapeUriString(el.toString()));
			}
			else if (Character.isLetter(el) || Character.IsNumber(el))
			{
				strBld.append(el);
			}
			else if (URLUnreserved.Contains(el))
			{
				strBld.append("%");
				strBld.append(String.format("%1$.2X", new Integer(el)));
			}
			else
			{
				strBld.append(Uri.EscapeUriString(el.toString()));
			}
		}
		return strBld.toString();
	}

	/** 
	 Decodes an IRI to a string
	 
	 @param IRI
	 @return 
	*/
	public static String decodeIRI(String IRI)
	{
		StringBuilder strBld = new StringBuilder();
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var charStr = IRI.toCharArray();
		for (int i = 0; i < charStr.Length; i++)
		{
			if (charStr[i] == '%' && i + 2 < charStr.Length) // case in which an hexadecimal character is found
			{
				StringBuilder tmpStrBld = new StringBuilder();
				tmpStrBld.append(charStr[i]);
				tmpStrBld.append(charStr[i + 1]);
				tmpStrBld.append(charStr[i + 2]);
				String unescapedChar = Uri.UnescapeDataString(tmpStrBld.toString());
				if (tmpStrBld.toString().equals(unescapedChar) && i + 5 < charStr.Length) // UTF8 escaped char
				{
					tmpStrBld.append(charStr[i + 3]);
					tmpStrBld.append(charStr[i + 4]);
					tmpStrBld.append(charStr[i + 5]);
					unescapedChar = Uri.UnescapeDataString(tmpStrBld.toString());
					i = i + 5;
				}
				else
				{
					i = i + 2;
				}

				strBld.append(unescapedChar);
			}
			else if (Character.isLetter(charStr[i]) || Character.IsNumber(charStr[i]))
			{
				strBld.append(charStr[i]);
			}
			else
			{
				strBld.append(Uri.UnescapeDataString(charStr[i].toString()));
			}
		}
		return strBld.toString();
	}

	public static boolean AreNamespacesEqual(String ns1Ext, String ns2Ext)
	{
		String ns1 = ns1Ext;
		String ns2 = ns2Ext;
		if (ns1.startsWith("<"))
		{
			ns1 = ns1.substring(1, 1 + ns1.length() - 2);
		}

		if (ns2.startsWith("<"))
		{
			ns1 = ns1.substring(1, 1 + ns1.length() - 2);
		}

		if (ns1.endsWith("#") || ns1.endsWith("/"))
		{
			ns1 = ns1.substring(0, ns1.length() - 1);
		}

		if (ns2.endsWith("#") || ns2.endsWith("/"))
		{
			ns2 = ns2.substring(0, ns2.length() - 1);
		}

		return ns1.equals(ns2);
	}
}