package cognipy.executing.hermit;

import cognipy.cnl.dl.*;
import cognipy.configuration.*;
import org.apache.jena.graph.*;
import org.apache.jena.graph.impl.*;
import org.apache.jena.reasoner.rulesys.*;
import org.apache.jena.util.*;
import cognipy.*;
import java.util.*;
import java.io.*;

public class StringUnary extends org.apache.jena.reasoner.rulesys.builtins.BaseBuiltin
{
	@Override
	public String getName()
	{
		return "stringUnary";
	}

	@Override
	public int getArgLength()
	{
		return 3;
	}
	private static final System.Text.RegularExpressions.Regex trimmer = new System.Text.RegularExpressions.Regex("\\s\\s+", System.Text.RegularExpressions.RegexOptions.Compiled);

	@Override
	public boolean bodyCall(org.apache.jena.graph.Node[] args, int length, RuleContext context)
	{
		checkArgs(length, context);

		String kind = getArg(0, args, context).getLiteral().toString();

		org.apache.jena.graph.Node n0 = getArg(1, args, context);

		if (kind.equals("case-ignore"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).compareToIgnoreCase(n1) == 0;
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else if (kind.equals("length"))
		{

			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			int len = n1.length();
			if (n0.isLiteral())
			{
				Object p1 = RuleExtensions.getValFromJenaLiteral(n0.getLiteralValue());
				if (!RuleExtensions.isInteger(p1))
				{
					return false;
				}
				return len == (Integer)p1;
			}
			else
			{
				return context.getEnv().bind(n0, Util.makeDoubleNode(len));
			}
		}
		else if (kind.equals("space-normalize"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			String sb = trimmer.Replace(n1, " ").trim();
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).compareTo(sb) == 0;
			}
			else
			{
				org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb);
				return context.getEnv().bind(n0, result);
			}
		}
		else if (kind.equals("upper-case"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			String sb = n1.toUpperCase();
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).compareTo(sb) == 0;
			}
			else
			{
				org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb);
				return context.getEnv().bind(n0, result);
			}
		}
		else if (kind.equals("lower-case"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			String sb = n1.toLowerCase();
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).compareTo(sb) == 0;
			}
			else
			{
				org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(sb);
				return context.getEnv().bind(n0, result);
			}
		}
		else if (kind.equals("contains"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).contains(n1);
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else if (kind.equals("contains-case-ignore"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).toLowerCase().contains(n1.toLowerCase());
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else if (kind.equals("starts-with"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).startsWith(n1);
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else if (kind.equals("ends-with"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				return RuleExtensions.lex(n0, this, context).endsWith(n1);
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else if (kind.equals("matches"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				cognipy.executing.hermit.ReasonerExt ext = JenaRuleManager.GetReasonerExt(context);
				if (!ext.MatchedRegexes.containsKey(n1))
				{
					ext.MatchedRegexes.put(n1, new System.Text.RegularExpressions.Regex(n1, RegexOptions.Compiled));
				}
				return ext.MatchedRegexes.get(n1).IsMatch(RuleExtensions.lex(n0, this, context));
			}
			else
			{
				throw new BuiltinException(this, context, "Builtin usable for tests only " + getName() + ":" + kind);
			}
		}
		else if (kind.equals("sounds-like"))
		{
			String n1 = RuleExtensions.lex(getArg(2, args, context), this, context);
			if (n0.isLiteral())
			{
				return Soundex(n1).compareTo(Soundex(RuleExtensions.lex(n0, this, context))) == 0;
			}
			else
			{
				org.apache.jena.graph.Node result = org.apache.jena.graph.NodeFactory.createLiteral(Soundex(n1));
				return context.getEnv().bind(n0, result);
			}
		}
		else
		{
			throw new BuiltinException(this, context, "Unimplemented kind of BuiltIn " + getName() + ":" + kind);
		}

	}

	public static String Soundex(String word)
	{
		final int MaxSoundexCodeLength = 4;

		StringBuilder soundexCode = new StringBuilder();
		boolean previousWasHOrW = false;

		word = Regex.Replace(word == null ? "" : word.toUpperCase(), "[^\\w\\s]", "");

		if (tangible.StringHelper.isNullOrEmpty(word))
		{
			return tangible.StringHelper.padRight("", MaxSoundexCodeLength, '0');
		}

		soundexCode.append(word.First());

		for (int i = 1; i < word.length(); i++)
		{
			char numberCharForCurrentLetter = GetCharNumberForLetter(word.charAt(i));

			if (i == 1 && numberCharForCurrentLetter == GetCharNumberForLetter(soundexCode.charAt(0)))
			{
				continue;
			}

			if (soundexCode.length() > 2 && previousWasHOrW && numberCharForCurrentLetter == soundexCode.charAt(soundexCode.length() - 2))
			{
				continue;
			}

			if (soundexCode.length() > 0 && numberCharForCurrentLetter == soundexCode.charAt(soundexCode.length() - 1))
			{
				continue;
			}

			soundexCode.append(numberCharForCurrentLetter);

			previousWasHOrW = "HW".contains(word.charAt(i));
		}

		return tangible.StringHelper.padRight(soundexCode.Replace("0", "").toString(), MaxSoundexCodeLength, '0').substring(0, MaxSoundexCodeLength);
	}

	private static char GetCharNumberForLetter(char letter)
	{
		if ("BFPV".contains(letter))
		{
			return '1';
		}
		if ("CGJKQSXZ".contains(letter))
		{
			return '2';
		}
		if ("DT".contains(letter))
		{
			return '3';
		}
		if ('L' == letter)
		{
			return '4';
		}
		if ("MN".contains(letter))
		{
			return '5';
		}
		if ('R' == letter)
		{
			return '6';
		}

		return '0';
	}

}