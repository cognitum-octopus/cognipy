package tools;

import java.util.*;
import java.io.*;

// Malcolm Crowe 1995,2000
// As far as possible the regular expression notation follows that of lex



// We cleverly arrange for the YyLexer class to serialize itself out of a simple integer array.
// So: to use the lexer generated for a script, include the generated tokens.cs file in the build,
// This defines classes tokens (subclass of Lexer) and yytokens (subclass of YyLexer). 

// Call Lexer::Start() to start the input engine going, and then use the
// Lexer::Next() function to get successive TOKENs.
// Note that if you are using ParserGenerator, this is done for you. 

public class YyLexer // we will gather all formerly static definitions for lexing here and in LexerGenerate
{
	// Deserializing 
	public final void GetDfa()
	{
		if (!tokens.isEmpty())
		{
			return;
		}
		Serialiser f = new Serialiser(arr);
		f.VersionCheck();
		m_encoding = (Encoding)f.Deserialise();
		toupper = (Boolean)f.Deserialise();
		cats = (Hashtable)f.Deserialise();
		m_gencat = (byte)f.Deserialise();
		usingEOF = (Boolean)f.Deserialise();
		starts = (Hashtable)f.Deserialise();
		Dfa.SetTokens(this, starts);
		tokens = (Hashtable)f.Deserialise();
		reswds = (Hashtable)f.Deserialise();
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public final void EmitDfa(TextWriter outFile)
	{
		System.out.println("Serializing the lexer");
		Serialiser f = new Serialiser(outFile);
		f.VersionCheck();
		f.Serialise(m_encoding);
		f.Serialise(toupper);
		f.Serialise(cats);
		f.Serialise(m_gencat);
		f.Serialise(usingEOF);
		f.Serialise(starts);
		f.Serialise(tokens);
		f.Serialise(reswds);
		outFile.WriteLine("0};");
	}
//#endif
	// support for Unicode character sets
	public Encoding m_encoding = Encoding.UTF8; // overwritten by Deserialize
	public final void setInputEncoding(String value)
	{
		tangible.RefObject<Boolean> tempRef_toupper = new tangible.RefObject<Boolean>(toupper);
		m_encoding = Charset.GetEncoding(value, tempRef_toupper, erh);
	toupper = tempRef_toupper.argValue;
	}
	public boolean usingEOF = false;
	public boolean toupper = false; // for ASCIICAPS
	public Hashtable cats = new Hashtable(); // UnicodeCategory -> Charset
	public byte m_gencat = Byte.values()[0]; // not a UsingCat unless all usbale cats in use
									 // support for lexer states
	public Hashtable starts = new Hashtable(); // string->Dfa
											   // support for serialization
	protected int[] arr; // defined in generated tokens class
						 // support for token classes
	public Hashtable types = new Hashtable(); // string->TCreator
	public Hashtable tokens = new Hashtable(); // string->TokClassDef
											   // support for reserved word sets
	public Hashtable reswds = new Hashtable(); // int->ResWds
	public ErrorHandler erh;
	public YyLexer(ErrorHandler eh)
	{
		erh = eh;
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
		UsingCat((byte)Character.OTHER_PUNCTUATION);
		m_gencat = Character.OTHER_PUNCTUATION;
//#endif
		new Tfactory(this, "TOKEN", (Lexer yyl) -> Tokenfactory(yyl));
	}
	protected final Object Tokenfactory(Lexer yyl)
	{
		return new TOKEN(yyl);
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	public final Charset UsingCat(byte cat)
	{
		if (cat == m_gencat)
		{
			for (int j = 0;j < 28;j++)
			{
				if (!Enum.IsDefined(Byte.class,j))
				{
					continue;
				}
				byte u = (byte)j;
				if (u == Character.SURROGATE)
				{
					continue;
				}
				if (cats.get(u) == null)
				{
					UsingCat(u);
					m_gencat = u;
				}
			}
			return (Charset)cats.get(cat);
		}
		if (cats.get(cat) != null)
		{
			return (Charset)cats.get(cat);
		}
		Charset rv = new Charset(cat);
		cats.put(cat, rv);
		return rv;
	}
	public final void UsingChar(char ch)
	{
		byte cat = Character.getType(ch);
		Charset cs = UsingCat(cat);
		if (cs.m_generic == ch)
		{
			do
			{
				if (cs.m_generic == Character.MAX_VALUE)
				{
					cs.m_generic = ch; // all used: this m_generic will never be used
					return;
				}
				cs.m_generic++;
			} while (Character.getType(cs.m_generic) != cs.m_cat || cs.m_chars.containsKey(cs.m_generic));
			cs.m_chars.put(cs.m_generic, true);
		}
		else
		{
			cs.m_chars.put(ch, true);
		}
	}
//#endif
	public final char Filter(char ch)
	{
		byte cat = Character.getType(ch);
		Charset cs = (Charset)cats.get(cat);
		if (cs == null)
		{
			cs = (Charset)cats.get(m_gencat);
		}
		if (cs.m_chars.containsKey(ch))
		{
			return ch;
		}
		return cs.m_generic;
	}
	private boolean testEOF(char ch)
	{
		byte cat = Character.getType(ch);
		return (cat == Character.UNASSIGNED);
	}
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if (GENTIME)
	private boolean CharIsSymbol(char c)
	{
		byte u = Character.getType(c);
		return (u == Character.OTHER_SYMBOL || u == Character.MODIFIER_SYMBOL || u == Character.CURRENCY_SYMBOL || u == Character.MATH_SYMBOL);
	}
	private boolean CharIsSeparator(char c)
	{
		byte u = Character.getType(c);
		return (u == Character.PARAGRAPH_SEPARATOR || u == Character.LINE_SEPARATOR || u == Character.SPACE_SEPARATOR);
	}
	public final ChTest GetTest(String name)
	{
		try
		{
			Object o = Byte.valueOf(name);
			if (o != null)
			{
				byte cat = (byte)o;
				UsingCat(cat);
				return new ChTest((new CatTest(cat)).Test);
			}
		}
		catch (RuntimeException e)
		{
		}
		switch (name)
		{
			case "Symbol":
				UsingCat((byte)Character.OTHER_SYMBOL);
				UsingCat((byte)Character.MODIFIER_SYMBOL);
				UsingCat((byte)Character.CURRENCY_SYMBOL);
				UsingCat((byte)Character.MATH_SYMBOL);
				return (char ch) -> CharIsSymbol(ch);
			case "Punctuation":
				UsingCat((byte)Character.OTHER_PUNCTUATION);
				UsingCat((byte)Character.FINAL_QUOTE_PUNCTUATION);
				UsingCat((byte)Character.INITIAL_QUOTE_PUNCTUATION);
				UsingCat((byte)Character.END_PUNCTUATION);
				UsingCat((byte)Character.START_PUNCTUATION);
				UsingCat((byte)Character.DASH_PUNCTUATION);
				UsingCat((byte)Character.CONNECTOR_PUNCTUATION);
				return Character.IsPunctuation;
				/*			case "PrivateUse": 
								UsingCat(UnicodeCategory.PrivateUse);
								return new ChTest(Char.IsPrivateUse); */
			case "Separator":
				UsingCat((byte)Character.PARAGRAPH_SEPARATOR);
				UsingCat((byte)Character.LINE_SEPARATOR);
				UsingCat((byte)Character.SPACE_SEPARATOR);
				return (char ch) -> CharIsSeparator(ch);
			case "WhiteSpace":
				UsingCat((byte)Character.CONTROL);
				UsingCat((byte)Character.PARAGRAPH_SEPARATOR);
				UsingCat((byte)Character.LINE_SEPARATOR);
				UsingCat((byte)Character.SPACE_SEPARATOR);
				return Character.isWhitespace;
			case "Number":
				UsingCat((byte)Character.OTHER_NUMBER);
				UsingCat((byte)Character.LETTER_NUMBER);
				UsingCat((byte)Character.DECIMAL_DIGIT_NUMBER);
				return Character.IsNumber;
			case "Digit":
				UsingCat((byte)Character.DECIMAL_DIGIT_NUMBER);
				return Character.isDigit;
				/*			case "Mark": 
								UsingCat(UnicodeCategory.EnclosingMark);
								UsingCat(UnicodeCategory.SpacingCombiningMark);
								UsingCat(UnicodeCategory.NonSpacingMark);
								return new ChTest(Char.IsMark); */
			case "Letter":
				UsingCat((byte)Character.OTHER_LETTER);
				UsingCat((byte)Character.MODIFIER_LETTER);
				UsingCat((byte)Character.TITLECASE_LETTER);
				UsingCat((byte)Character.LOWERCASE_LETTER);
				UsingCat((byte)Character.UPPERCASE_LETTER);
				return Character.isLetter;
			case "Lower":
				UsingCat((byte)Character.LOWERCASE_LETTER);
				return Character.isLowerCase;
			case "Upper":
				UsingCat((byte)Character.UPPERCASE_LETTER);
				return Character.isUpperCase;
			case "EOF":
				UsingCat((byte)Character.UNASSIGNED);
				UsingChar((char)0xFFFF);
				usingEOF = true;
				return (char ch) -> testEOF(ch);
			default:
				erh.Error(new CSToolsException(24, "No such Charset " + name));
				break;
		}
		return Character.isISOControl; // not reached
	}
//#endif
	public TOKEN OldAction(Lexer yyl, tangible.RefObject<String> yytext, int action, tangible.RefObject<Boolean> reject)
	{
		return null;
	}
	public final Iterator GetEnumerator()
	{
		return tokens.values().iterator();
	}
}