package tools;

import java.util.*;
import java.io.*;

public class Charset
{
	public byte m_cat = Byte.values()[0];
	public char m_generic; // not explicitly Using'ed allUsed
	public Hashtable m_chars = new Hashtable(); // char->bool
	private Charset()
	{
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public Charset(byte cat)
	{
		m_cat = cat;
		for (m_generic = Character.MIN_VALUE;Character.getType(m_generic) != cat;m_generic++)
		{
			;
		}
		m_chars.put(m_generic, true);
	}
//#endif
	public static Encoding GetEncoding(String enc, tangible.RefObject<Boolean> toupper, ErrorHandler erh)
	{
		switch (enc)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if !SILVERLIGHT
			case "":
				return Encoding.Default; // locale-specific
			case "ASCII":
				return Encoding.ASCII;
			case "ASCIICAPS":
				toupper.argValue = true;
				return Encoding.ASCII; // toupper is currently ignored in scripts
			case "UTF7":
				return Encoding.UTF7;
//#endif
			case "UTF8":
				return Encoding.UTF8;
			case "Unicode":
				return Encoding.Unicode;
			default:
				try
				{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if! SILVERLIGHT
					if (Character.isDigit(enc.charAt(0)))
					{
						return Encoding.GetEncoding(Integer.parseInt(enc)); // codepage
					}
//#endif
					return Encoding.GetEncoding(enc);
				}
				catch (RuntimeException e)
				{
					erh.Error(new CSToolsException(43, "Warning: Encoding " + enc + " unknown: ignored"));
				}
				break;
		}
		return Encoding.UTF8;
	}
	public static Object Serialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new Charset();
		}
		Charset c = (Charset)o;
		if (s.getEncode())
		{
			s.Serialise((int)c.m_cat);
			s.Serialise(c.m_generic);
			s.Serialise(c.m_chars);
			return null;
		}
		c.m_cat = (byte)s.Deserialise();
		c.m_generic = (Character)s.Deserialise();
		c.m_chars = (Hashtable)s.Deserialise();
		return c;
	}
}