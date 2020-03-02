package cognipy.executing.hermit;

import cognipy.*;
import java.util.*;

public class ImpliKBVisitor extends cognipy.cnl.dl.GenericVisitor
{
	private HashSet<Tuple<String, String>> typeOf = new HashSet<Tuple<String, String>>();
	private HashSet<Tuple<String, String, String>> relatedTo = new HashSet<Tuple<String, String, String>>();
	private HashSet<Tuple<String, String, String, String>> valueOf = new HashSet<Tuple<String, String, String, String>>();
	private HashSet<String> others = new HashSet<String>();
	private boolean isImported = false;

	private boolean askMode = false;
	private Boolean entailmentFound = null;

	private CNL.DL.Serializer ser = new CNL.DL.Serializer();

	public final void Import(CNL.DL.Paragraph p)
	{
		for (Statement stmt : p.Statements)
		{
			others.add(ser.Serialize(stmt));
		}
		this.Visit(p);
		isImported = true;
	}

	public final boolean IsEntailed(CNL.DL.Statement e)
	{
		if (!isImported)
		{
			return false;
		}

		entailmentFound = null;
		askMode = true;
		e.accept(this);
		askMode = false;
		if (entailmentFound == null)
		{
			return others.contains(ser.Serialize(e));
		}
		else
		{
			return entailmentFound.booleanValue();
		}
	}

	@Override
	public Object Visit(CNL.DL.InstanceOf e)
	{
		if (e.I instanceof CNL.DL.NamedInstance)
		{
			if (e.C instanceof CNL.DL.Atomic)
			{
				System.Tuple<T1, T2> tup = Tuple.Create((e.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)e.I : null).name, (e.C instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.C : null).id);
				if (askMode)
				{
					entailmentFound = typeOf.contains(tup);
				}
				else
				{
					typeOf.add(tup);
				}
			}
			else if (e.C instanceof CNL.DL.Top)
			{
				System.Tuple<T1, T2> tup = Tuple.Create((e.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)e.I : null).name, "");
				if (askMode)
				{
					entailmentFound = typeOf.contains(tup);
				}
				else
				{
					typeOf.add(tup);
				}
			}
		}

		return null;
	}

	@Override
	public Object Visit(CNL.DL.RelatedInstances e)
	{
		if (e.I instanceof CNL.DL.NamedInstance && e.J instanceof CNL.DL.NamedInstance)
		{
			if (e.R instanceof CNL.DL.Atomic)
			{
				System.Tuple<T1, T2, T3> tup = Tuple.Create((e.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)e.I : null).name, (e.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.R : null).id, (e.J instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)e.J : null).name);
				if (askMode)
				{
					entailmentFound = relatedTo.contains(tup);
				}
				else
				{
					relatedTo.add(tup);
				}
			}
		}
		return null;
	}

	@Override
	public Object Visit(CNL.DL.InstanceValue e)
	{
		if (e.I instanceof CNL.DL.NamedInstance)
		{
			if (e.R instanceof CNL.DL.Atomic)
			{
				System.Tuple<T1, T2, T3, T4> tup = Tuple.Create((e.I instanceof CNL.DL.NamedInstance ? (CNL.DL.NamedInstance)e.I : null).name, (e.R instanceof CNL.DL.Atomic ? (CNL.DL.Atomic)e.R : null).id, e.V.getTypeTag(), e.V.getVal());
				if (askMode)
				{
					entailmentFound = valueOf.contains(tup);
				}
				else
				{
					valueOf.add(tup);
				}
			}
		}
		return null;
	}

}