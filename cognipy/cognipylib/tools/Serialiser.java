package tools;

import java.util.*;

public class Serialiser
{
	private static final String Version = "4.5";
	public final void VersionCheck()
	{
		if (getEncode())
		{
			Serialise(Version);
		}
		else
		{
			Object tempVar = Deserialise();
			String version = tempVar instanceof String ? (String)tempVar : null;
			if (version == null)
			{
				throw new RuntimeException("Serialisation error - found data from version 4.4 or earlier");
			}
			else if (!Version.equals(version))
			{
				throw new RuntimeException("Serialisation error - expected version " + Version + ", found data from version " + version);
			}
		}
	}
	private enum SerType
	{
		Null,
		Int,
		Bool,
		Char,
		String,
		Hashtable,
		Encoding,
		UnicodeCategory,
		Symtype,
		Charset,
		TokClassDef,
		Action,
		Dfa,
		ResWds, // 4.7
		ParserOldAction,
		ParserSimpleAction,
		ParserShift,
		ParserReduce,
		ParseState,
		ParsingInfo,
		CSymbol,
		Literal,
		Production,
		EOF;

		public static final int SIZE = java.lang.Integer.SIZE;

		public int getValue()
		{
			return this.ordinal();
		}

		public static SerType forValue(int value)
		{
			return values()[value];
		}
	}
	@FunctionalInterface
	private interface ObjectSerialiser
	{
		Object invoke(Object o, Serialiser s);
	}
	static
	{
		srs.put(SerType.Null, (Object o, Serialiser s) -> NullSerialise(o, s));
		tps.put(Integer.class, SerType.Int);
		srs.put(SerType.Int, (Object o, Serialiser s) -> IntSerialise(o, s));
		tps.put(String.class, SerType.String);
		srs.put(SerType.String, (Object o, Serialiser s) -> StringSerialise(o, s));
		tps.put(Hashtable.class, SerType.Hashtable);
		srs.put(SerType.Hashtable, (Object o, Serialiser s) -> HashtableSerialise(o, s));
		tps.put(Character.class, SerType.Char);
		srs.put(SerType.Char, (Object o, Serialiser s) -> CharSerialise(o, s));
		tps.put(Boolean.class, SerType.Bool);
		srs.put(SerType.Bool, (Object o, Serialiser s) -> BoolSerialise(o, s));
		tps.put(Encoding.class, SerType.Encoding);
		srs.put(SerType.Encoding, (Object o, Serialiser s) -> EncodingSerialise(o, s));
		tps.put(Byte.class, SerType.UnicodeCategory);
		srs.put(SerType.UnicodeCategory, (Object o, Serialiser s) -> UnicodeCategorySerialise(o, s));
		tps.put(CSymbol.SymType.class, SerType.Symtype);
		srs.put(SerType.Symtype, (Object o, Serialiser s) -> SymtypeSerialise(o, s));
		tps.put(Charset.class, SerType.Charset);
		srs.put(SerType.Charset, (Object o, Serialiser s) -> Charset.Serialise(o, s));
		tps.put(TokClassDef.class, SerType.TokClassDef);
		srs.put(SerType.TokClassDef, (Object o, Serialiser s) -> TokClassDef.Serialise(o, s));
		tps.put(Dfa.class, SerType.Dfa);
		srs.put(SerType.Dfa, (Object o, Serialiser s) -> Dfa.Serialise(o, s));
		tps.put(ResWds.class, SerType.ResWds);
		srs.put(SerType.ResWds, (Object o, Serialiser s) -> ResWds.Serialise(o, s)); // 4.7
		tps.put(Dfa.Action.class, SerType.Action);
		srs.put(SerType.Action, (Object o, Serialiser s) -> Dfa.Action.Serialise(o, s));
		tps.put(ParserOldAction.class, SerType.ParserOldAction);
		srs.put(SerType.ParserOldAction, (Object o, Serialiser s) -> ParserOldAction.Serialise(o, s));
		tps.put(ParserSimpleAction.class, SerType.ParserSimpleAction);
		srs.put(SerType.ParserSimpleAction, (Object o, Serialiser s) -> ParserSimpleAction.Serialise(o, s));
		tps.put(ParserShift.class, SerType.ParserShift);
		srs.put(SerType.ParserShift, (Object o, Serialiser s) -> ParserShift.Serialise(o, s));
		tps.put(ParserReduce.class, SerType.ParserReduce);
		srs.put(SerType.ParserReduce, (Object o, Serialiser s) -> ParserReduce.Serialise(o, s));
		tps.put(ParseState.class, SerType.ParseState);
		srs.put(SerType.ParseState, (Object o, Serialiser s) -> ParseState.Serialise(o, s));
		tps.put(ParsingInfo.class, SerType.ParsingInfo);
		srs.put(SerType.ParsingInfo, (Object o, Serialiser s) -> ParsingInfo.Serialise(o, s));
		tps.put(CSymbol.class, SerType.CSymbol);
		srs.put(SerType.CSymbol, (Object o, Serialiser s) -> CSymbol.Serialise(o, s));
		tps.put(Literal.class, SerType.Literal);
		srs.put(SerType.Literal, (Object o, Serialiser s) -> Literal.Serialise(o, s));
		tps.put(Production.class, SerType.Production);
		srs.put(SerType.Production, (Object o, Serialiser s) -> Production.Serialise(o, s));
		tps.put(EOF.class, SerType.EOF);
		srs.put(SerType.EOF, (Object o, Serialiser s) -> EOF.Serialise(o, s));
	}
	// on Encode, we ignore the return value which is always null
	// Otherwise, o if non-null is an instance of the subclass
	private TextWriter f = null;
	private int[] b = null;
	private int pos = 0;
	private Hashtable obs = new Hashtable(); // object->int (code) or int->object (decode)
	private static Hashtable tps = new Hashtable(); // type->SerType
	private static Hashtable srs = new Hashtable(); // SerType->ObjectSerialiser
	private int id = 100;
	private int cl = 0;
	public Serialiser(TextWriter ff)
	{
		f = ff;
	}
	public Serialiser(int[] bb)
	{
		b = bb;
	}
	public final boolean getEncode()
	{
		return f != null;
	}
	private void _Write(SerType t)
	{
		_Write(t.getValue());
	}
	public final void _Write(int i)
	{
		if (cl == 5)
		{
			f.WriteLine();
			cl = 0;
		}
		cl++;
		f.Write(i);
		f.Write(",");
	}
	public final int _Read()
	{
		return b[pos++];
	}
	private static Object NullSerialise(Object o, Serialiser s)
	{
		return null;
	}
	private static Object IntSerialise(Object o, Serialiser s)
	{
		if (s.getEncode())
		{
			s._Write((Integer)o);
			return null;
		}
		return s._Read();
	}
	private static Object StringSerialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return "";
		}
		Encoding e = new UnicodeEncoding();
		if (s.getEncode())
		{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] b = e.GetBytes((string)o);
			byte[] b = e.GetBytes((String)o);
			s._Write(b.length);
			for (int j = 0; j < b.length; j++)
			{
				s._Write((int)b[j]);
			}
			return null;
		}
		int ln = s._Read();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] bb = new byte[ln];
		byte[] bb = new byte[ln];
		for (int k = 0; k < ln; k++)
		{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: bb[k] = (byte)s._Read();
			bb[k] = (byte)s._Read();
		}
		String r = e.GetString(bb, 0, ln);
		return r;
	}
	private static Object HashtableSerialise(Object o, Serialiser s)
	{
		if (s == null)
		{
			return new Hashtable();
		}
		Hashtable h = (Hashtable)o;
		if (s.getEncode())
		{
			s._Write(h.size());
			for (Object d : h)
			{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if SILVERLIGHT
				s.Serialise(d.Key);
				s.Serialise(d.Value);
//#else
				s.Serialise(((Map.Entry)d).getKey());
				s.Serialise(((Map.Entry)d).getValue());
//#endif
			}
			return null;
		}
		int ct = s._Read();
		for (int j = 0; j < ct; j++)
		{
			Object k = s.Deserialise();
			Object v = s.Deserialise();
			h.put(k, v);
		}
		return h;
	}
	private static Object CharSerialise(Object o, Serialiser s)
	{
		Encoding e = new UnicodeEncoding();
		if (s.getEncode())
		{
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] b = e.GetBytes(new string((char)o, 1));
			byte[] b = e.GetBytes(tangible.StringHelper.repeatChar((Character)o, 1));
			s._Write((int)b[0]);
			s._Write((int)b[1]);
			return null;
		}
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: byte[] bb = new byte[2];
		byte[] bb = new byte[2];
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: bb[0] = (byte)s._Read();
		bb[0] = (byte)s._Read();
//C# TO JAVA CONVERTER WARNING: Unsigned integer types have no direct equivalent in Java:
//ORIGINAL LINE: bb[1] = (byte)s._Read();
		bb[1] = (byte)s._Read();
		String r = e.GetString(bb, 0, 2);
		return r.charAt(0);
	}
	private static Object BoolSerialise(Object o, Serialiser s)
	{
		if (s.getEncode())
		{
			s._Write(((Boolean)o) ? 1 : 0);
			return null;
		}
		int v = s._Read();
		return v != 0;
	}
	private static Object EncodingSerialise(Object o, Serialiser s)
	{
		if (s.getEncode())
		{
			Encoding e = (Encoding)o;
			s.Serialise(e.WebName);
			return null;
		}
		String str = (String)s.Deserialise();
		switch (str)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if !SILVERLIGHT
			case "us-ascii":
				return Encoding.ASCII;
//#endif
			case "utf-16":
				return Encoding.Unicode;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if !SILVERLIGHT
			case "utf-7":
				return Encoding.UTF7;
//#endif
			case "utf-8":
				return Encoding.UTF8;

			default:
				try
				{
					return Encoding.GetEncoding(str); // 4.7f
				}
				catch (RuntimeException e)
				{
					throw new RuntimeException("Unknown encoding");
				}
		}
	}
	private static Object UnicodeCategorySerialise(Object o, Serialiser s)
	{
		if (s.getEncode())
		{
			s._Write((Integer)o);
			return null;
		}
		return (byte)s._Read();
	}
	private static Object SymtypeSerialise(Object o, Serialiser s)
	{
		if (s.getEncode())
		{
			s._Write((Integer)o);
			return null;
		}
		return CSymbol.SymType.forValue(s._Read());
	}
	public final void Serialise(Object o)
	{
		if (o == null)
		{
			_Write(SerType.Null);
			return;
		}
		if (o instanceof Encoding)
		{
			_Write(SerType.Encoding);
			EncodingSerialise(o, this);
			return;
		}
		java.lang.Class t = o.getClass();
		if (t.IsClass)
		{
			Object p = obs.get(o);
			if (p != null)
			{
				_Write((Integer)p);
				return;
			}
			else
			{
				int e = ++id;
				_Write(e);
				obs.put(o, e);
			}
		}
		Object so = tps.get(t);
		if (so != null)
		{
			SerType s = (SerType)so;
			_Write(s);
			ObjectSerialiser os = (ObjectSerialiser)srs.get(s);
			os.invoke(o, this);
		}
		else
		{
			throw new RuntimeException("unknown type " + t.FullName);
		}
	}
	public final Object Deserialise()
	{
		int t = _Read();
		int u = 0;
		if (t > 100)
		{
			u = t;
			if (u <= obs.size() + 100)
			{
				return obs.get(u);
			}
			t = _Read();
		}
		ObjectSerialiser os = (ObjectSerialiser)srs.get(SerType.forValue(t));
		if (os != null)
		{
			if (u > 0)
			{ // ?? strange bug in mono: workaround in CSTools 4.5a leads to not chaining assignments here
				Object r = os.invoke(null, null); // allow for recursive structures: create and store first
				obs.put(u, r);
				r = os.invoke(r, this); // really deserialise it
				obs.put(u, r); // we need to store it again for strings
				return r;
			}
			return os.invoke(null, this);
		}
		else
		{
			throw new RuntimeException("unknown type " + t);
		}
	}
}