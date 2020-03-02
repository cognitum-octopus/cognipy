package cognipy.owl;

import cognipy.ars.*;
import org.semanticweb.owlapi.model.*;
import cognipy.*;
import java.io.*;

public final class OWLPathUriTools
{
	public static String Path2Uri(String fileName)
	{
		return Uri.EscapeUriString(fileName.replace('\\', '/'));
	}

	public static String Uri2Path(String uri)
	{
		return Uri.UnescapeDataString(uri);
	}

	public static IRI Path2IRI(String fileName)
	{
		return IRI.create(fileName.replace('\\', '/'));
	}

	public static String IRI2Path(IRI iri)
	{
		return iri.toURI().toString().replace('\\', '/');
	}

	public static String CombinePath(String dir, String end)
	{
		String pt = (new File(dir)).getAbsolutePath().replace("/", "\\");
		String et = end.replace("/", "\\");
		return pt + ((pt.endsWith("\\") || et.startsWith("\\")) ? "" : "\\") + et;
	}

	public static String CombineUri(String uri, String end)
	{
		String urun = Uri.UnescapeDataString(uri);
		return Uri.EscapeUriString(urun + (urun.endsWith("/") ? "" : "/") + Uri.UnescapeDataString(end));
	}

	public static String Iri2Dl(String uri, EntityKind type)
	{
		InvTransform invTrans = new InvTransform();

		OwlName tempVar = new OwlName();
		tempVar.iri = OWL.OWLPathUriTools.Path2IRI(uri);
		String n = invTrans.renderEntity(tempVar, type);
		if (n == null)
		{
			return "";
		}
		return n;
	}
}