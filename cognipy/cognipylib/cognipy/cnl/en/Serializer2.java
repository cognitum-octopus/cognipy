package cognipy.cnl.en;

import cognipy.cnl.dl.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

public class Serializer2 implements cognipy.cnl.en.IVisitor
{
	private VisitingParam<Boolean> useBrack = new VisitingParam<Boolean>(false);
	private VisitingParam<Boolean> isModal = new VisitingParam<Boolean>(false);
	private VisitingParam<Boolean> isPlural = new VisitingParam<Boolean>(false);
	public AnnotationManager annotMan = new AnnotationManager();

	public final String Serialize(paragraph p)
	{
		if (!annotMan.GetAnnotationSubjects().isEmpty())
		{
			annotMan.clearAnnotations();
		}
		Object tempVar = p.accept(this);
		String r = tempVar instanceof String ? (String)tempVar : null;
		return r;
	}
	public final String Serialize(sentence s)
	{
		if (!annotMan.GetAnnotationSubjects().isEmpty())
		{
			annotMan.clearAnnotations();
		}
		Object tempVar = s.accept(this);
		String r = tempVar instanceof String ? (String)tempVar : null;
		if (!(s instanceof dlannotationassertion))
		{
			r = EnsureBigStart(r);
		}
		return r;
	}
	public final String Serialize(orloop p)
	{
		if (!annotMan.GetAnnotationSubjects().isEmpty())
		{
			annotMan.clearAnnotations();
		}
		Object tempVar = p.accept(this);
		String r = tempVar instanceof String ? (String)tempVar : null;
		return r;
	}
	public final String Serialize(boundFacets p)
	{
		if (!annotMan.GetAnnotationSubjects().isEmpty())
		{
			annotMan.clearAnnotations();
		}
		Object tempVar = p.accept(this);
		String r = tempVar instanceof String ? (String)tempVar : null;
		return r;
	}

	public final String Serialize(boundTop p)
	{
		if (!annotMan.GetAnnotationSubjects().isEmpty())
		{
			annotMan.clearAnnotations();
		}
		Object tempVar = p.accept(this);
		String r = tempVar instanceof String ? (String)tempVar : null;
		return r;
	}
	public final String Serialize(boundTotal p)
	{
		if (!annotMan.GetAnnotationSubjects().isEmpty())
		{
			annotMan.clearAnnotations();
		}
		Object tempVar = p.accept(this);
		String r = tempVar instanceof String ? (String)tempVar : null;
		return r;
	}

	public final void Assert(boolean b)
	{
		if (!b)
		{
//C# TO JAVA CONVERTER TODO TASK: There is no preprocessor in Java:
//#if DEBUG
			System.Diagnostics.Debugger.Break();
//#endif
			throw new RuntimeException("Conversion Assertion Failed.");
		}
	}

	private String EnsureBigStart(String snt)
	{
		if (snt.length() > 0)
		{
			return Character.toUpperCase(snt.charAt(0)) + (snt.length() > 1 ? snt.substring(1) : "");
		}
		else
		{
			return snt;
		}
	}

	public final Object Visit(paragraph p)
	{
		StringBuilder sb = new StringBuilder();
		for (sentence x : p.sentences)
		{
			Object tempVar = x.accept(this);
			String str = tempVar instanceof String ? (String)tempVar : null;
			if (getSerializeAnnotations() && x instanceof annotation && str.startsWith("Annotations:"))
			{
				annotMan.loadW3CAnnotationsFromText(str, true);
			}

			if (!(x instanceof dlannotationassertion || (x instanceof annotation && str.startsWith("Annotations:"))))
			{
				sb.append(EnsureBigStart(str) + "\r\n");
			}
		}

		if (getSerializeAnnotations())
		{
			sb.append(annotMan.SerializeAnnotations());
		}

		return sb.toString();
	}

	public final String Modality(String tok)
	{
		switch (tok)
		{
			case "□":
				return "must";
			case "◊":
				return "should";
			case "◊◊":
				return "can";
			case "~◊◊":
				return "must-not";
			case "~◊":
				return "should-not";
			case "~□":
				return "can-not";
			default:
				return null;
		}
	}

	public final String Modality2(String tok)
	{
		String r = Modality(tok);
		if (r == null)
		{
			return null;
		}
		else
		{
			return KeyWords.Me.Get("IT") + " " + r + " " + KeyWords.Me.Get("BETRUETHAT");
		}
	}

	public final Object Visit(subsumption p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append(p.c.accept(this));
		sb.append(" ");
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(modal);
				sb.append(" ");
				sb.append(p.d.accept(this));
			}
			else
			{
				sb.append(p.d.accept(this));
			}
		}
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(nosubsumption p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append(p.c.accept(this));
		sb.append(" ");
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(modal);
				sb.append(" ");
				sb.append(p.d.accept(this));
			}
			else
			{
				sb.append(p.d.accept(this));
			}
		}
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(equivalence2 p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append(KeyWords.Me.Get("SOMETHING"));
		sb.append(" ");
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(modal);
				sb.append(" ");
				sb.append(p.c.accept(this));
			}
			else
			{
				sb.append(p.c.accept(this));
			}
		}
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IFANDONLYIFIT"));
		sb.append(" ");
		sb.append(p.d.accept(this));
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	//public object Visit(equivalence_def p)
	//{
	//    StringBuilder sb = new StringBuilder();
	//    string modal = Modality(p.modality);
	//    sb.Append(p.c.accept(this));
	//    sb.Append(" ");
	//    using (isModal.set(modal != null))
	//    {
	//        if (modal != null)
	//        {
	//            sb.Append(modal);
	//            sb.Append(" ");
	//            sb.Append(p.d.accept(this));
	//        }
	//        else
	//            sb.Append(p.d.accept(this));
	//    }
	//    sb.Append(KeyWords.Me.Get("END"));
	//    return sb.ToString();
	//}

	public final Object Visit(subsumption_if p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append(KeyWords.Me.Get("IF"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("SOMETHING"));
		sb.append(" ");
		sb.append(p.c.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("THEN"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IT"));
		sb.append(" ");
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(modal);
				sb.append(" ");
				sb.append(p.d.accept(this));
			}
			else
			{
				sb.append(p.d.accept(this));
			}
		}
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(datatypedef p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append(KeyWords.Me.Get("EVERY"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("VALUEOF"));
		sb.append(" ");
		sb.append(p.name);
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IS"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("SOMETHING"));
		sb.append(" ");
		sb.append(p.db.accept(this));
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(exclusives p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("anything");
		String modal = Modality(p.modality);
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(" ");
				sb.append(modal);
			}
			sb.append(" ");
			sb.append(KeyWords.Me.Get("EITHER"));
			for (int i = 0; i < p.objectRoleExprs.size(); i++)
			{
				try (useBrack.setIf(i < p.objectRoleExprs.size() - 1, true))
				{
					objectRoleExpr e = p.objectRoleExprs.get(i);
					if (i == 0)
					{
						sb.append(" ");
						sb.append(e.accept(this));
					}
					else
					{
						if (i == p.objectRoleExprs.size() - 1)
						{
							sb.append(" or");
						}
						else
						{
							sb.append(",");
						}
						sb.append(" ");
						sb.append(e.accept(this));
					}
				}
			}
		}
		sb.append(" ");
		sb.append(KeyWords.Me.Get("ORSOMETHINGELSE"));
		sb.append(".");
		return sb.toString();
	}

	public final Object Visit(exclusiveunion p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("something");
		sb.append(" ");
		String modal = Modality(p.modality);
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(" ");
				sb.append(modal);
				sb.append(" ");
			}
			sb.append(isBeAre());
		}
		sb.append(" ");
		sb.append(addAOrAn(p.name));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IFANDONLYIFITEITHER"));
		for (int i = 0; i < p.objectRoleExprs.size(); i++)
		{
			objectRoleExpr e = p.objectRoleExprs.get(i);
			try (useBrack.setIf(i < p.objectRoleExprs.size() - 1, true))
			{
				if (i == 0)
				{
					sb.append(" ");
					sb.append(e.accept(this));
				}
				else
				{
					if (i == p.objectRoleExprs.size() - 1)
					{
						sb.append(" or");
					}
					else
					{
						sb.append(",");
					}
					sb.append(" ");
					sb.append(e.accept(this));
				}
			}
		}
		sb.append(".");
		return sb.toString();
	}

	public final Object Visit(rolesubsumption p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append("If");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		boolean first = true;
		for (role x : p.subChain)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(" something that ");
			}
			sb.append(x.accept(this));
		}
		sb.append(" ");
		sb.append("Y");
		sb.append(" ");
		sb.append("then");
		sb.append(" ");
		sb.append(p.superRole.accept(this));
		sb.append(".");
		return sb.toString();
	}

	//public object Visit(roleequivalence p)
	//{
	//    StringBuilder sb = new StringBuilder();
	//    bool first = true;
	//    foreach (var e in p.equals)
	//    {
	//        if (first)
	//        {
	//            sb.Append(e.accept(this));
	//            first = false;
	//        }
	//        else
	//        {
	//            if (p.equals.IndexOf(e) == p.equals.Count - 1)
	//                sb.Append(" and");
	//            else
	//                sb.Append(",");
	//            sb.Append(" ");
	//            sb.Append(e.accept(this));
	//        }
	//    }

	//    sb.Append(" ");

	//    string modal = Modality(p.modality);
	//    using (isModal.set(modal != null))
	//    {
	//        if (modal != null)
	//        {
	//            sb.Append(modal);
	//            sb.Append(" ");
	//        }
	//    }
	//    sb.Append("means-the-same");
	//    sb.Append(".");
	//    return sb.ToString();
	//}

	public final Object Visit(roleequivalence2 p)
	{
		StringBuilder sb = new StringBuilder();
		//bool first = true;

		sb.append("X");
		sb.append(" ");
		sb.append(p.r.accept(this));
		sb.append(" ");
		sb.append("Y");
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IFANDONLYIF"));
		sb.append(" ");
		sb.append(p.s.accept(this));
		sb.append(".");
		return sb.toString();
	}

	//public object Visit(roledisjoint p)
	//{
	//    StringBuilder sb = new StringBuilder();
	//    bool first = true;
	//    foreach (var e in p.different)
	//    {
	//        if (first)
	//        {
	//            sb.Append(e.accept(this));
	//            first = false;
	//        }
	//        else
	//        {
	//            if (p.different.IndexOf(e) == p.different.Count - 1)
	//                sb.Append(" and");
	//            else
	//                sb.Append(",");
	//            sb.Append(" ");
	//            sb.Append(e.accept(this));
	//        }
	//    }

	//    sb.Append(" ");

	//    string modal = Modality(p.modality);
	//    using (isModal.set(modal != null))
	//    {
	//        if (modal != null)
	//        {
	//            sb.Append(modal);
	//            sb.Append(" ");
	//        }
	//        using (isPlural.set(true))
	//        {
	//            sb.Append(isBeAre());
	//        }
	//    }
	//    sb.Append(" ");
	//    sb.Append("different");
	//    sb.Append(".");
	//    return sb.ToString();
	//}

	public final Object Visit(roledisjoint2 p)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("If");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		sb.append(p.r.accept(this));
		sb.append(" ");
		sb.append("Y");
		sb.append(" ");
		sb.append("then");
		sb.append(" ");
		sb.append(p.s.accept(this));
		sb.append(".");
		return sb.toString();
	}

	public final Object Visit(datarolesubsumption p)
	{
		StringBuilder sb = new StringBuilder();
		String modal = Modality(p.modality);
		sb.append("If");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		sb.append(p.subRole.accept(this));
		sb.append(" ");
		sb.append("equal-to Y");
		sb.append(" ");
		sb.append("then");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		try (isModal.set(modal != null))
		{
			if (modal != null)
			{
				sb.append(modal);
				sb.append(" ");
				sb.append(p.superRole.accept(this));
			}
			else
			{
				sb.append(p.superRole.accept(this));
			}
		}
		sb.append(" ");
		sb.append("equal-to Y");
		sb.append(".");
		return sb.toString();
	}

	//public object Visit(dataroleequivalence p)
	//{
	//    StringBuilder sb = new StringBuilder();
	//    bool first = true;
	//    foreach (var e in p.equals)
	//    {
	//        if (first)
	//        {
	//            sb.Append("X");
	//            sb.Append(" ");
	//            sb.Append(e.accept(this));
	//            sb.Append(" ");
	//            sb.Append("value");
	//            first = false;
	//        }
	//        else
	//        {
	//            if (p.equals.IndexOf(e) == p.equals.Count - 1)
	//                sb.Append(" and");
	//            else
	//                sb.Append(",");
	//            sb.Append(" ");
	//            sb.Append("X");
	//            sb.Append(" ");
	//            sb.Append(e.accept(this));
	//            sb.Append(" ");
	//            sb.Append("value");
	//        }
	//    }

	//    sb.Append(" ");

	//    string modal = Modality(p.modality);
	//    using (isModal.set(modal != null))
	//    {
	//        if (modal != null)
	//        {
	//            sb.Append(modal);
	//            sb.Append(" ");
	//        }
	//    }
	//    sb.Append("means-the-same");
	//    sb.Append(".");
	//    return sb.ToString();
	//}

	public final Object Visit(dataroleequivalence2 p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("X");
		sb.append(" ");
		sb.append(p.r.accept(this));
		sb.append(" ");
		sb.append("equal-to Y");
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IFANDONLYIF"));
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		sb.append(p.s.accept(this));
		sb.append(" ");
		sb.append("equal-to Y");
		sb.append(".");
		return sb.toString();
	}

	//public object Visit(dataroledisjoint p)
	//{
	//    StringBuilder sb = new StringBuilder();
	//    bool first = true;
	//    foreach (var e in p.different)
	//    {
	//        if (first)
	//        {
	//            sb.Append("X");
	//            sb.Append(" ");
	//            sb.Append(e.accept(this));
	//            sb.Append(" ");
	//            sb.Append("value");
	//            first = false;
	//        }
	//        else
	//        {
	//            if (p.different.IndexOf(e) == p.different.Count - 1)
	//                sb.Append(" and");
	//            else
	//                sb.Append(",");
	//            sb.Append(" ");
	//            sb.Append("X");
	//            sb.Append(" ");
	//            sb.Append(e.accept(this));
	//            sb.Append(" ");
	//            sb.Append("value");
	//        }
	//    }

	//    sb.Append(" ");

	//    string modal = Modality(p.modality);
	//    using (isModal.set(modal != null))
	//    {
	//        if (modal != null)
	//        {
	//            sb.Append(modal);
	//            sb.Append(" ");
	//        }
	//        using (isPlural.set(true))
	//        {
	//            sb.Append(isBeAre());
	//        }
	//    }
	//    sb.Append(" ");
	//    sb.Append("different");
	//    sb.Append(".");
	//    return sb.ToString();
	//}

	public final Object Visit(dataroledisjoint2 p)
	{
		StringBuilder sb = new StringBuilder();

		sb.append("If");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		sb.append(p.r.accept(this));
		sb.append(" ");
		sb.append("equal-to Y");
		sb.append(" ");
		sb.append("then");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		sb.append("does-not");
		sb.append(" ");
		sb.append(p.s.accept(this));
		sb.append(" ");
		sb.append("equal-to Y");
		sb.append(".");
		return sb.toString();
	}

	public final Object Visit(haskey p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("Every");
		sb.append(" ");
		sb.append("X");
		sb.append(" ");
		sb.append("that");
		sb.append(" ");
		sb.append(p.s.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("ISUNIQUEIF"));
		sb.append(" ");
		boolean first = true;
		{
			for (role e : p.roles)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(" ");
					sb.append("and");
					sb.append(" ");
				}
				sb.append("X");
				sb.append(" ");
				sb.append(e.accept(this));
				sb.append(" ");
				sb.append("something");
			}
		}
		{
			for (role e : p.dataroles)
			{
				if (first)
				{
					first = false;
				}
				else
				{
					sb.append(" ");
					sb.append("and");
					sb.append(" ");
				}
				sb.append("X");
				sb.append(" ");
				sb.append(e.accept(this));
				sb.append(" equal-to ");
				sb.append("something");
			}
		}
		sb.append(".");
		return sb.toString();
	}
	public final Object Visit(subjectEvery p)
	{
		return KeyWords.Me.Get("EVERY") + " " + p.s.accept(this);
	}
	public final Object Visit(subjectEverything p)
	{
		return KeyWords.Me.Get("EVERYTHING") + (p.t != null ? (" " + p.t.accept(this)) : "");
	}
	public final Object Visit(subjectNo p)
	{
		return KeyWords.Me.Get("NO") + " " + p.s.accept(this);
	}
	public final Object Visit(subjectNothing p)
	{
		return KeyWords.Me.Get("NOTHING");
	}
	public final Object Visit(subjectBigName p)
	{
		return bigname(p.name);
	}
	public final Object Visit(subjectThe p)
	{
		if (p.only)
		{
			return KeyWords.Me.Get("THEONEANDONLY") + " " + p.s.accept(this);
		}
		else
		{
			return KeyWords.Me.Get("THE") + " " + p.s.accept(this);
		}
	}

	public final String isBeAre()
	{
		if (isModal.get())
		{
			return "be";
		}
		else if (isPlural.get())
		{
			return "are";
		}
		else
		{
			return "is";
		}
	}

	public final Object Visit(objectRoleExpr1 p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(isBeAre());
		if (p.Negated)
		{
			sb.append(" ");
			sb.append("not");
		}
		sb.append(" ");
		sb.append(p.s.accept(this));
		return sb.toString();
	}
	public final Object Visit(roleWithXY p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.inverse ? "Y" : "X");
		sb.append(" ");
		sb.append(p.name);
		sb.append(" ");
		sb.append(p.inverse ? "X" : "Y");
		return sb.toString();
	}
	public final Object Visit(notRoleWithXY p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.inverse ? "Y" : "X");
		sb.append(" does-not ");
		sb.append(p.name);
		sb.append(" ");
		sb.append(p.inverse ? "X" : "Y");
		return sb.toString();
	}
	public final Object Visit(role p)
	{
		if (!p.inverse)
		{
			return p.name;
		}
		else
		{
			StringBuilder sb = new StringBuilder();
			sb.append(isBeAre());
			sb.append(" ");
			sb.append(p.name);
			sb.append(" ");
			sb.append("by");
			return sb.toString();
		}
	}
	public final Object Visit(objectRoleExpr2 p)
	{
		StringBuilder sb = new StringBuilder();
		if (p.Negated)
		{
			if (!p.r.inverse)
			{
				if (isModal.get())
				{
					sb.append("do-not");
				}
				else
				{
					sb.append("does-not");
				}
				sb.append(" ");
				sb.append(p.r.accept(this));
			}
			else
			{
				if (isModal.get())
				{
					sb.append("be-not");
				}
				else if (isPlural.get())
				{
					sb.append("are-not");
				}
				else
				{
					sb.append("is-not");
				}
				sb.append(" ");
				sb.append(p.r.name);
				sb.append(" ");
				sb.append("by");
			}
		}
		else
		{
			sb.append(p.r.accept(this));
		}
		if (p.s != null)
		{
			sb.append(" ");
			try (isModal.set(p.Negated))
			{
				sb.append(p.s.accept(this));
			}
		}
		return sb.toString();
	}

	public final Object Visit(objectRoleExpr3 p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.r.accept(this));
		sb.append(" ");
		if (p.r.name == null || !p.r.name.startsWith("has-"))
		{
			sb.append("something ");
		}
		sb.append(p.t.accept(this));
		return sb.toString();
	}

	public final Object Visit(oobjectA p)
	{
		try (isModal.set(false))
		{
			Object tempVar = p.s.accept(this);
			String nm = tempVar instanceof String ? (String)tempVar : null;
			if (p.s instanceof singleOneOf)
			{
				return nm;
			}
			else
			{
				return addAOrAn(nm);
			}
		}
	}

	private String addAOrAn(String nm)
	{
		if (nm.startsWith("(") || nm.startsWith("that "))
		{
			return nm;
		}
		else
		{
			return (isPlural.get() ? "" : ((nm.startsWith("a") || nm.startsWith("e") || nm.startsWith("i") || nm.startsWith("o")) ? "an" : "a") + " ") + nm;
		}
	}

	public final Object Visit(instanceThe p)
	{
		try (isModal.set(false))
		{
			return (p.only ? KeyWords.Me.Get("THEONEANDONLY") : KeyWords.Me.Get("THE")) + " " + p.s.accept(this);
		}
	}
	public final Object Visit(instanceBigName p)
	{
		return bigname(p.name);
	}
	public final Object Visit(oobjectInstance p)
	{
		try (isModal.set(false))
		{
			return p.i.accept(this);
		}
	}

	public final Object Visit(oobjectOnly p)
	{
		try (isModal.set(false))
		{
			try (isPlural.set(true))
			{
				return "nothing-but" + " " + p.s.accept(this);
			}
		}
	}

	public final Object Visit(oobjectOnlyInstance p)
	{
		try (isModal.set(false))
		{
			return "nothing-but" + " " + p.i.accept(this);
		}
	}

	private String word_number(String wcnt)
	{
		long cnt = Long.parseLong(wcnt);
		switch (cnt)
		{
			case 0:
				return "zero";
			case 1:
				return "one";
			case 2:
				return "two";
			case 3:
				return "three";
			case 4:
				return "four";
			case 5:
				return "five";
			case 6:
				return "six";
			case 7:
				return "seven";
			case 8:
				return "eight";
			case 9:
				return "nine";
			default:
				return String.valueOf(cnt);
		}
	}

	private String comparer(String str)
	{
		if (str.contains("<>"))
		{
			return "different-than ";
		}
		else if (str.contains("<"))
		{
			return "less-than ";
		}
		else if (str.contains(">"))
		{
			return "more-than ";
		}
		else if (str.contains("="))
		{
			return "";
		}
		else if (str.contains("≤"))
		{
			return "at-most ";
		}
		else if (str.contains("≥"))
		{
			return "at-least ";
		}
		else if (str.contains("≠"))
		{
			return "different-than ";
		}
		else
		{
			return null;
		}
	}

	private String comparer2(String str)
	{
		if (str.startsWith("<->"))
		{
			return "that-has-length " + ((str.length() > "<->".length()) ? (comparer2(str.substring("<->".length() + 1))) : "");
		}
		else if (str.contains("<>"))
		{
			return "different-from ";
		}
		else if (str.contains("<"))
		{
			return "lower-than ";
		}
		else if (str.contains(">"))
		{
			return "greater-than ";
		}
		else if (str.contains("="))
		{
			return "equal-to ";
		}
		else if (str.contains("≤"))
		{
			return "lower-or-equal-to ";
		}
		else if (str.contains("≥"))
		{
			return "greater-or-equal-to ";
		}
		else if (str.contains("≠"))
		{
			return "different-from ";
		}
		else if (str.contains("#"))
		{
			return "that-matches-pattern ";
		}
		else
		{
			throw new IllegalStateException("Unknown Facet in Grammar");
		}
	}

	public final Object Visit(oobjectCmp p)
	{
		try (isModal.set(false))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(comparer(p.Cmp));
			sb.append(word_number(p.Cnt));
			sb.append(" ");
			try (isPlural.set(Long.parseLong(p.Cnt) != 1))
			{
				sb.append(p.s.accept(this));
			}
			return sb.toString();
		}
	}
	public final Object Visit(oobjectCmpInstance p)
	{
		try (isModal.set(false))
		{
			StringBuilder sb = new StringBuilder();
			sb.append(comparer(p.Cmp));
			sb.append(word_number(p.Cnt));
			sb.append(" ");
			try (isPlural.set(Long.parseLong(p.Cnt) != 1))
			{
				sb.append(p.i.accept(this));
			}
			return sb.toString();
		}
	}

	public final Object Visit(oobjectBnd p)
	{
		return p.b.accept(this);
	}
	public final Object Visit(oobjectOnlyBnd p)
	{
		return "nothing-but" + " " + p.b.accept(this);
	}
	public final Object Visit(oobjectCmpBnd p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(comparer(p.Cmp));
		sb.append(word_number(p.Cnt));
		sb.append(" ");
		sb.append(p.b.accept(this));
		return sb.toString();
	}

	public final Object Visit(oobjectSelf p)
	{
		return "itself";
	}
	public final Object Visit(oobjectSomething p)
	{
		return "something";
	}
	public final Object Visit(oobjectNothing p)
	{
		return "nothing";
	}
	public final Object Visit(oobjectOnlyNothing p)
	{
		return "none";
	}
	public final Object Visit(oobjectSomethingThat p)
	{
		try (isModal.set(false))
		{
			return "something" + " " + p.t.accept(this);
		}
	}
	public final Object Visit(oobjectOnlySomethingThat p)
	{
		try (isModal.set(false))
		{
			return "nothing-but" + " " + "something" + " " + p.t.accept(this);
		}
	}

	private String name(String str)
	{
		return str;
	}

	private String bigname(String str)
	{
		return str;
	}

	private String thing()
	{
		if (isPlural.get())
		{
			return "things";
		}
		else
		{
			return "thing";
		}
	}

	public final Object Visit(singleName p)
	{
		return name(p.name);
	}
	public final Object Visit(singleThing p)
	{
		return thing();
	}
	public final Object Visit(singleNameThat p)
	{
		String str = name(p.name) + " ";
		try (isModal.set(false))
		{
			return str + p.t.accept(this);
		}
	}
	public final Object Visit(singleThingThat p)
	{
		String str = thing() + " ";
		try (isModal.set(false))
		{
			return str + p.t.accept(this);
		}
	}

	public final Object Visit(thatOrLoop p)
	{
		Object snt = p.o.accept(this);
		String th = "that" + " " + snt;
		if (useBrack.get())
		{
			th = "(" + th + ")";
		}
		return th;
	}
	public final Object Visit(singleOneOf p)
	{
		try (isModal.set(false))
		{
			StringBuilder sb = new StringBuilder();
			sb.append("either ");
			try (isPlural.set(false))
			{
				boolean first = true;
				for (instance i : p.insts)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						if (p.insts.indexOf(i) == p.insts.size() - 1)
						{
							sb.append(" or");
						}
						else
						{
							sb.append(",");
						}
						sb.append(" ");
					}
					sb.append(i.accept(this));
				}
			}
			return useBrack.get() ? "(" + sb.toString() + ")" : sb.toString();
		}
	}
	public final Object Visit(andloop p)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < p.exprs.size(); i++)
		{
			objectRoleExpr e = p.exprs.get(i);
			if (i > 0)
			{
				sb.append(" and ");
			}
			try (useBrack.setIf(i < p.exprs.size() - 1, true))
			{
				sb.append(e.accept(this));
			}
		}
		return sb.toString();
	}
	public final Object Visit(orloop p)
	{
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < p.exprs.size(); i++)
		{
			andloop e = p.exprs.get(i);
			if (i > 0)
			{
				sb.append(" and-or ");
			}
			try (useBrack.setIf(i < p.exprs.size() - 1, true))
			{
				sb.append(e.accept(this));
			}
		}
		return sb.toString();
	}

	public final Object Visit(facet p)
	{
		return comparer2(p.Cmp) + p.V.accept(this);
	}

	public final Object Visit(boundVal p)
	{
		return comparer2(p.Cmp) + p.V.accept(this);
	}

	public final Object Visit(facetList p)
	{
		boolean multi = p.Facets.size() > 1;
		StringBuilder sb = new StringBuilder();

		if (multi)
		{
			sb.append(KeyWords.Me.Get("OPEN"));
		}
		boolean first = true;
		for (facet f : p.Facets)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(KeyWords.Me.Get("COMMA"));
				sb.append(" ");
			}
			sb.append(f.accept(this));
		}
		if (multi)
		{
			sb.append(KeyWords.Me.Get("CLOSE"));
		}
		return sb.toString();
	}

	public final Object Visit(boundFacets p)
	{
		return p.l.accept(this);
	}

	public final Object Visit(boundNot p)
	{
		return "not " + brack(p, p.bnd);
	}

	public final Object Visit(boundAnd p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < p.List.size(); i++)
		{
			abstractbound e = p.List.get(i);
			if (i > 0)
			{
				sb.append(" ");
				sb.append(KeyWords.Me.Get("ASWELLAS"));
				sb.append(" ");
			}
			sb.append(brack(p, e));
		}
		sb.append(")");
		return sb.toString();
	}

	private String brack(abstractbound parent, abstractbound child)
	{
		if (child.priority() == 0 || (child.priority() >= parent.priority()))
		{
			Object tempVar = child.accept(this);
			return tempVar instanceof String ? (String)tempVar : null;
		}
		else
		{
			Object tempVar2 = child.accept(this);
			return "(" + tempVar2 instanceof String ? (String)tempVar2 : null + ")";
		}
	}

	public final Object Visit(boundOr p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(");
		for (int i = 0; i < p.List.size(); i++)
		{
			abstractbound e = p.List.get(i);
			if (i > 0)
			{
				sb.append(" or ");
			}
			sb.append(brack(p, e));
		}
		sb.append(")");
		return sb.toString();
	}

	public final Object Visit(boundTop p)
	{
		return "(some value)";
	}
	public final Object Visit(boundTotal p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(some");
		sb.append(" ");
		switch (p.Kind)
		{
			case "NUM":
				sb.append("integer");
				break;
			case "BOL":
				sb.append("boolean");
				break;
			case "DBL":
				sb.append("real");
				break;
			case "DTM":
				sb.append("datetime");
				break;
			case "DUR":
				sb.append("duration");
				break;
			case "STR":
				sb.append("string");
				break;
			default:
				Assert(false);
				break;
		}
		sb.append(" ");
		sb.append("value)");
		return sb.toString();
	}

	public final Object Visit(boundDataType p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("(some");
		sb.append(" ");
		sb.append(p.name);
		sb.append(" ");
		sb.append("value)");
		return sb.toString();
	}

	public final Object Visit(boundOneOf p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append("either");
		boolean First = true;
		for (dataval val : p.vals)
		{
			if (First)
			{
				First = false;
				sb.append(" ");
			}
			else
			{
				if (p.vals.indexOf(val) == p.vals.size() - 1)
				{
					sb.append(" or");
				}
				else
				{
					sb.append(",");
				}
				sb.append(" ");
			}
			sb.append(val.accept(this).toString());
		}
		return "(" + sb.toString() + ")";
	}
	public final Object Visit(Number p)
	{
		return p.getVal();
	}
	public final Object Visit(Bool p)
	{
		return p.getVal();
	}
	public final Object Visit(StrData p)
	{
		return p.getVal();
	}
	public final Object Visit(DateTimeData p)
	{
		return p.getVal();
	}
	public final Object Visit(Duration p)
	{
		return p.getVal();
	}
	public final Object Visit(Float p)
	{
		return p.getVal();
	}

	public final Object Visit(annotation p)
	{
		return p.txt;
	}

	public final Object Visit(dlannotationassertion p)
	{
		W3CAnnotation w3cAnnot = new W3CAnnotation(true);
		w3cAnnot.setType(p.annotName);
		w3cAnnot.setValue(p.value);
		w3cAnnot.setLanguage(p.language);
		annotMan.appendAnnotations(p.subject, p.subjKind, new ArrayList<W3CAnnotation>(Arrays.asList(w3cAnnot)));
		return "Annotations:" + p.subject.replace(".", "..") + " " + p.subjKind.replace(".", "..") + ": " + w3cAnnot.toString().replace(".", "..") + ".";
	}

	public final Object Visit(swrlrule p)
	{
		String modal = Modality2(p.modality);

		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("IF"));
		sb.append(" ");
		inRuleBody = true;
		sb.append(p.Predicate.accept(this));
		inRuleBody = false;
		sb.append(" ");
		sb.append(KeyWords.Me.Get("THEN"));
		sb.append(" ");
		if (modal != null)
		{
			sb.append(modal);
			sb.append(" ");
		}
		inModalSwrl = (modal != null);
		sb.append(p.Result.accept(this));
		inModalSwrl = false;
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(swrlrulefor p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("IF"));
		sb.append(" ");
		inRuleBody = true;
		sb.append(p.Predicate.accept(this));
		inRuleBody = false;
		sb.append(" ");
		sb.append(KeyWords.Me.Get("THEN"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("FOR"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("EVERY"));
		sb.append(" ");
//C# TO JAVA CONVERTER TODO TASK: There is no equivalent to implicit typing in Java unless the Java 10 inferred typing option is selected:
		var frs = p.Collection.exevars.get(0);

		if (frs instanceof datavalvar)
		{
			sb.append(KeyWords.Me.Get("VALUE"));
			sb.append(KeyWords.Me.Get("OPEN"));
			sb.append((frs instanceof datavalvar ? (datavalvar)frs : null).num);
			sb.append(KeyWords.Me.Get("CLOSE"));
		}
		else if (frs instanceof identobject_name)
		{
			if ((frs instanceof identobject_name ? (identobject_name)frs : null).name == null)
			{
				sb.append(KeyWords.Me.Get("THING"));
				sb.append(KeyWords.Me.Get("OPEN"));
				sb.append((frs instanceof identobject_name ? (identobject_name)frs : null).num);
				sb.append(KeyWords.Me.Get("CLOSE"));
			}
			else
			{
				throw new UnsupportedOperationException();
			}
		}
		else
		{
			throw new UnsupportedOperationException();
		}

		sb.append(" ");
		sb.append(KeyWords.Me.Get("FROM"));
		sb.append(" ");
		sb.append(p.Collection.exevars.get(p.Collection.exevars.size() - 1).accept(this));
		sb.append(" ");
		sb.append(p.Result.accept(this));
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(clause p)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (condition x : p.Conditions)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(" ");
				sb.append(KeyWords.Me.Get("AND"));
				sb.append(" ");
			}
			sb.append(x.accept(this));
		}
		return sb.toString();
	}

	public final Object Visit(clause_result p)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (condition_result x : p.Conditions)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(" ");
				sb.append(KeyWords.Me.Get("AND"));
				sb.append(" ");
			}
			sb.append(x.accept(this));
		}
		return sb.toString();
	}

	public final Object Visit(condition_is p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		if (p.condition_kind == condition_kind.None)
		{
			sb.append(KeyWords.Me.Get("ISTHESAMEAS"));
			sb.append(" ");
		}
		else
		{
			sb.append(KeyWords.Me.Get("ISNOTTHESAMEAS"));
			sb.append(" ");
		}
		sb.append(p.objectB.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_exists p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("EXISTS"));
		return sb.toString();
	}

	public final Object Visit(condition_definition p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IS"));
		sb.append(" ");
		sb.append(p.objectClass.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_role p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		if (p.condition_kind == condition_kind.None)
		{
			sb.append(p.role);
		}
		else
		{
			sb.append(KeyWords.Me.Get("IS"));
			sb.append(" ");
			sb.append(p.role);
			sb.append(" ");
			sb.append(KeyWords.Me.Get("BY"));
		}
		sb.append(" ");
		sb.append(p.objectB.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_data_property p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		sb.append(p.property_name);
		sb.append(" ");
		sb.append(KeyWords.Me.Get("EQUALTO"));
		sb.append(" ");
		sb.append(p.d_object.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_data_property_bound p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		sb.append(p.property_name);
		sb.append(" ");
		sb.append(p.bnd.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_data_bound p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.d_object.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IS"));
		sb.append(" ");
		sb.append(p.bound.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_result_is p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		if (p.condition_kind == condition_kind.None)
		{
			sb.append(KeyWords.Me.Get("ISTHESAMEAS"));
			sb.append(" ");
		}
		else
		{
			sb.append(KeyWords.Me.Get("ISNOTTHESAMEAS"));
			sb.append(" ");
		}
		sb.append(p.objectB.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_result_definition p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IS"));
		sb.append(" ");
		sb.append(p.objectClass.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_result_role p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		if (p.condition_kind == condition_kind.None)
		{
			sb.append(p.role);
		}
		else
		{
			sb.append(KeyWords.Me.Get("IS"));
			sb.append(" ");
			sb.append(p.role);
			sb.append(" ");
			sb.append(KeyWords.Me.Get("BY"));
		}
		sb.append(" ");
		sb.append(p.objectB.accept(this));
		return sb.toString();
	}

	public final Object Visit(condition_result_data_property p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.objectA.accept(this));
		sb.append(" ");
		sb.append(p.property_name);
		sb.append(" ");
		sb.append(KeyWords.Me.Get("EQUALTO"));
		sb.append(" ");
		sb.append(p.d_object.accept(this));
		return sb.toString();
	}

	public final Object Visit(objectr_nio p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.notidentobject.accept(this));
		return sb.toString();
	}

	public final Object Visit(objectr_io p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.identobject.accept(this));
		return sb.toString();
	}

	public final String a_name(String nm)
	{
		return ((nm.startsWith("a") || nm.startsWith("e") || nm.startsWith("i") || nm.startsWith("o") || nm.startsWith("u")) ? "an" : "a") + " " + nm;
	}

	private HashSet<String> bodyVars = new HashSet<String>();

	public final Object Visit(notidentobject p)
	{
		if (getTemplateMode())
		{
			String k = "name_" + p.name + "_" + (p.num != null ? "(" + p.num + ")" : "");
			if (inRuleBody || bodyVars.contains(k))
			{
				if (!templateVars.containsKey(k))
				{
					templateVars.put(k, templateIdx++);
				}

				if (inRuleBody)
				{
					bodyVars.add(k);
				}

				return "{" + templateVars.get(k) + "}";
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append((p.name == null ? "a thing" : a_name(p.name)) + (p.num != null ? "(" + p.num + ")" : ""));
		return sb.toString();
	}

	public final Object Visit(identobject_name p)
	{
		if (getTemplateMode())
		{
			String k = "name_" + p.name + "_" + (p.num != null ? "(" + p.num + ")" : "");
			if (inRuleBody || bodyVars.contains(k))
			{
				if (!templateVars.containsKey(k))
				{
					templateVars.put(k, templateIdx++);
				}

				return "{" + templateVars.get(k) + "}";
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("THE"));
		sb.append(" ");
		sb.append(p.name != null ? p.name : "thing");
		if (p.num != null)
		{
			sb.append("(" + p.num + ")");
		}
		return sb.toString();
	}

	public final Object Visit(identobject_inst p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.i.accept(this));
		return sb.toString();
	}

	public final Object Visit(instancer p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(bigname(p.name));
		return sb.toString();
	}

	public final Object Visit(datavalval p)
	{
		return p.dv.accept(this).toString();
	}

	private int templateIdx = 0;
	private HashMap<String, Integer> templateVars = new HashMap<String, Integer>();

	public final Object Visit(datavalvar p)
	{
		if (getTemplateMode())
		{
			String k = "value" + p.num.toString();
			if (!inModalSwrl || templateVars.containsKey(k))
			{
				if (!templateVars.containsKey(k))
				{
					templateVars.put(k, templateIdx++);
				}

				return "{" + templateVars.get(k) + "}";
			}
		}

		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("THE"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("VALUE"));
		sb.append(KeyWords.Me.Get("OPEN"));
		sb.append(p.num);
		sb.append(KeyWords.Me.Get("CLOSE"));
		return sb.toString();
	}

	public final Object Visit(condition_builtin p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.bi.accept(this));
		return sb.toString();
	}
	public final Object Visit(condition_result_builtin p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.bi.accept(this));
		return sb.toString();
	}
	public final Object Visit(builtin_cmp p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.a.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("IS"));
		sb.append(" ");
		sb.append(comparer2(p.cmp));
		sb.append(p.b.accept(this));
		return sb.toString();
	}

	private String equals()
	{
		return KeyWords.Me.Get("IS") + " " + KeyWords.Me.Get("EQUALTO");
	}

	public final Object Visit(builtin_list p)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (datavaler l : p.vals)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(" ");
				sb.append(p.tpy);
				sb.append(" ");
			}
			sb.append(l.accept(this));
		}
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.result.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_bin p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.b.accept(this));
		sb.append(" ");
		sb.append(p.tpy);
		sb.append(" ");
		sb.append(p.d.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.result.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_unary_cmp p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.tpy);
		sb.append(" ");
		sb.append(p.b.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.result.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_unary_free p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.a.accept(this));
		sb.append(" ");
		sb.append(p.tpy);
		sb.append(" ");
		sb.append(p.b.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_substr p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("SUBSTRING"));
		sb.append(" ");
		sb.append(p.b.accept(this));
		sb.append(" ");
		sb.append(p.tpy);
		sb.append(" ");
		sb.append(p.c.accept(this));
		if (p.d != null)
		{
			sb.append(KeyWords.Me.Get("THATHASLENGTH"));
			sb.append(" ");
			sb.append(p.d.accept(this));
		}
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.result.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_trans p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.tpy);
		sb.append(" ");
		sb.append(p.b.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("FROM"));
		sb.append(" ");
		sb.append(p.c.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("WITH"));
		sb.append(" ");
		sb.append(p.d.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.result.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_duration p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.d.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.a.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_datetime p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.d.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.a.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_alpha p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("THE"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("ALPHA"));
		sb.append(" ");
		sb.append(p.a.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.b.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_annot p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("ANNOTATION"));
		sb.append(" ");
		sb.append(p.prop.accept(this));
		sb.append(" ");
		sb.append(p.lang.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("FROM"));
		sb.append(" ");
		sb.append(p.a.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.b.accept(this));
		return sb.toString();
	}

	public final Object Visit(builtin_exe p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("RESULTOF"));
		sb.append(" ");
		sb.append(p.name);
		sb.append(" ");
		sb.append(KeyWords.Me.Get("FOR"));
		sb.append(" ");
		sb.append(p.ea.accept(this));
		sb.append(" ");
		sb.append(equals());
		sb.append(" ");
		sb.append(p.a.accept(this));
		return sb.toString();
	}

	private static boolean isZero(datavaler dv)
	{
		if (dv instanceof datavalval)
		{
			if ((dv instanceof datavalval ? (datavalval)dv : null).dv instanceof Number)
			{
				return ((dv instanceof datavalval ? (datavalval)dv : null).dv instanceof Number ? (Number)(dv instanceof datavalval ? (datavalval)dv : null).dv : null).val.equals("0");
			}
		}
		return false;
	}

	public final Object Visit(duration_w p)
	{
		ArrayList<datavaler> vlist = new ArrayList<datavaler>(Arrays.asList(p.y, p.W, p.d, p.h, p.m, p.s));
		ArrayList<String> app = new ArrayList<String>(Arrays.asList("years", "weeks", "days", "hours", "minutes", "seconds"));

		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < vlist.size(); i++)
		{
			if (!isZero(vlist.get(i)))
			{
				break;
			}
		}

		int j;
		for (j = vlist.size() - 1; j > i; j--)
		{
			if (!isZero(vlist.get(j)))
			{
				break;
			}
		}

		for (int x = i; x <= j; x++)
		{
			if (sb.length() > 0)
			{
				sb.append(" ");
			}
			sb.append(vlist.get(x).accept(this));
			sb.append(" ");
			sb.append(app.get(x));
		}
		return sb.toString();
	}

	public final Object Visit(duration_m p)
	{
		ArrayList<datavaler> vlist = new ArrayList<datavaler>(Arrays.asList(p.y, p.M, p.d, p.h, p.m, p.s));
		ArrayList<String> app = new ArrayList<String>(Arrays.asList("years", "months", "days", "hours", "minutes", "seconds"));

		StringBuilder sb = new StringBuilder();
		int i;
		for (i = 0; i < vlist.size(); i++)
		{
			if (!isZero(vlist.get(i)))
			{
				break;
			}
		}

		int j;
		for (j = vlist.size() - 1; j > i; j--)
		{
			if (!isZero(vlist.get(j)))
			{
				break;
			}
		}

		for (int x = i; x <= j; x++)
		{
			if (sb.length() > 0)
			{
				sb.append(" ");
			}
			sb.append(vlist.get(x).accept(this));
			sb.append(" ");
			sb.append(app.get(x));
		}
		return sb.toString();
	}

	public final Object Visit(datetime p)
	{
		StringBuilder sb = new StringBuilder();
		if (!isZero(p.y) || !isZero(p.M) || !isZero(p.d))
		{
			sb.append(KeyWords.Me.Get("DATE"));
			sb.append(" ");
			sb.append(p.y.accept(this));
			if (sb.length() > 0)
			{
				sb.append(" - ");
			}
			sb.append(p.M.accept(this));
			if (sb.length() > 0)
			{
				sb.append(" - ");
			}
			sb.append(p.d.accept(this));
		}
		if (!isZero(p.h) || !isZero(p.m) || !isZero(p.s))
		{
			if (sb.length() > 0)
			{
				sb.append(" ");
			}
			sb.append(KeyWords.Me.Get("TIME"));
			sb.append(" ");
			sb.append(p.h.accept(this));
			if (sb.length() > 0)
			{
				sb.append(" : ");
			}
			sb.append(p.m.accept(this));
			if (sb.length() > 0)
			{
				sb.append(" : ");
			}
			sb.append(p.s.accept(this));
		}
		return sb.toString();
	}

	//////////// EXE //////////////////////////////////////////////////////////////

	public final Object Visit(exerule p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(KeyWords.Me.Get("IF"));
		sb.append(" ");
		sb.append(p.slp.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("THEN"));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("FOR"));
		sb.append(" ");
		sb.append(p.args.accept(this));
		sb.append(" ");
		sb.append(KeyWords.Me.Get("EXECUTE"));
		sb.append(" ");
		sb.append(p.exe);
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}

	public final Object Visit(exeargs p)
	{
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (iexevar x : p.exevars)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				sb.append(" ");
				sb.append(KeyWords.Me.Get("AND"));
				sb.append(" ");
			}
			sb.append(x.accept(this));
		}
		return sb.toString();
	}


	private boolean SerializeAnnotations;
	public final boolean getSerializeAnnotations()
	{
		return SerializeAnnotations;
	}
	public final void setSerializeAnnotations(boolean value)
	{
		SerializeAnnotations = value;
	}

	private boolean TemplateMode;
	public final boolean getTemplateMode()
	{
		return TemplateMode;
	}
	public final void setTemplateMode(boolean value)
	{
		TemplateMode = value;
	}

	private boolean inRuleBody = false;
	private boolean inModalSwrl = false;

	public final Object Visit(code p)
	{
		StringBuilder sb = new StringBuilder();
		sb.append(p.exe);
		sb.append(" ");
		sb.append(KeyWords.Me.Get("END"));
		return sb.toString();
	}
}