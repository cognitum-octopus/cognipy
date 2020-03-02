package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;

public class FullUriVisitor extends cognipy.cnl.dl.GenericVisitor
{
	private tangible.Func1Param<String, String> pfx2ns;
	private String _defaultNs;

	public FullUriVisitor(Func<String, String> pfx2ns)
	{
		this(pfx2ns, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public FullUriVisitor(Func<string, string> pfx2ns, string defaultNs = null)
	public FullUriVisitor(tangible.Func1Param<String, String> pfx2ns, String defaultNs)
	{
		if (pfx2ns == null)
		{
			throw new RuntimeException("Cannot initialize the FullUriVisitor without giving the prefix to namespace map.");
		}
		this.pfx2ns = (String arg) -> pfx2ns.invoke(arg);
		this._defaultNs = defaultNs;
	}

	private String applyFullUri(String nm)
	{
		return CNLTools.DLToFullUri(nm, ARS.EntityKind.Instance, pfx2ns, _defaultNs);
	}

	@Override
	public Object Visit(DLAnnotationAxiom e)
	{
		e.annotName = applyFullUri(e.annotName);
		if (!ARS.EntityKind.Statement.toString().equals(e.getSubjKind()))
		{
			e.setSubject(applyFullUri(e.getSubject()));
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(Atomic e)
	{
		e.id = applyFullUri(e.id);
		return super.Visit(e);
	}

	@Override
	public Object Visit(NamedInstance e)
	{
		e.name = applyFullUri(e.name);
		return super.Visit(e);
	}

	@Override
	public Object Visit(DisjointUnion e)
	{
		e.name = applyFullUri(e.name);
		return super.Visit(e);
	}

	@Override
	public Object Visit(InstanceOf e)
	{
		if (e.I instanceof NamedInstance)
		{
			(e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name = applyFullUri((e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name);
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(RelatedInstances e)
	{
		if (e.I instanceof NamedInstance)
		{
			(e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name = applyFullUri((e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name);
		}
		if (e.J instanceof NamedInstance)
		{
			(e.J instanceof NamedInstance ? (NamedInstance)e.J : null).name = applyFullUri((e.J instanceof NamedInstance ? (NamedInstance)e.J : null).name);
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(InstanceValue e)
	{
		if (e.I instanceof NamedInstance)
		{
			(e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name = applyFullUri((e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name);
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlInstance e)
	{
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlRole e)
	{
		e.J.accept(this);
		e.R = applyFullUri(e.R);
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlSameAs e)
	{
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlDifferentFrom e)
	{
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlDataProperty e)
	{
		e.R = applyFullUri(e.R);
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlIVal e)
	{
		e.I = applyFullUri(e.I);
		return e;
	}

	@Override
	public Object Visit(SwrlDVal e)
	{
		return e;
	}

	@Override
	public Object Visit(SwrlIVar e)
	{
		return e;
	}

	@Override
	public Object Visit(SwrlDVar e)
	{
		return e;
	}

	@Override
	public Object Visit(SwrlVarList e)
	{
		Object tempVar = x.accept(this);
//C# TO JAVA CONVERTER TODO TASK: There is no Java equivalent to LINQ query syntax:
		e.list = (from x in e.list select tempVar instanceof IExeVar ? (IExeVar)tempVar : null).ToList();
		return super.Visit(e);
	}

}