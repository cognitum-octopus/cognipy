package cognipy.cnl.dl;

import cognipy.*;
import cognipy.cnl.*;

public class SetDefaultPfxVisitor extends cognipy.cnl.dl.GenericVisitor
{
	private String defaultPfx;

	public SetDefaultPfxVisitor(String defaultPfx)
	{
		this(defaultPfx, null);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SetDefaultPfxVisitor(string defaultPfx, string defaultNamespace = null)
	public SetDefaultPfxVisitor(String defaultPfx, String defaultNamespace)
	{
		if (!tangible.StringHelper.isNullOrWhiteSpace(defaultPfx))
		{
			this.defaultPfx = defaultPfx;
		}
		else if (!tangible.StringHelper.isNullOrWhiteSpace(defaultNamespace) && !defaultNamespace.startsWith("<") && !defaultNamespace.endsWith(">"))
		{
			this.defaultPfx = "<" + defaultNamespace + ">";
		}
		else if (!tangible.StringHelper.isNullOrWhiteSpace(defaultNamespace))
		{
			this.defaultPfx = defaultNamespace;
		}
	}

	private String applyDefaultPfx(String nm)
	{
		DlName parst = new DlName();
		parst.id = nm;
		String trm = tangible.StringHelper.isNullOrEmpty(parst.term) ? defaultPfx : parst.term;
		DlName.Parts tempVar = new DlName.Parts();
		tempVar.name = parst.name;
		tempVar.local = parst.local;
		tempVar.quoted = parst.quoted;
		tempVar.term = trm;
		return tempVar.Combine().id;
	}

	@Override
	public Object Visit(DLAnnotationAxiom e)
	{
		e.annotName = applyDefaultPfx(e.annotName);
		e.setSubject(applyDefaultPfx(e.getSubject()));
		return super.Visit(e);
	}

	@Override
	public Object Visit(Atomic e)
	{
		e.id = applyDefaultPfx(e.id);
		return super.Visit(e);
	}

	@Override
	public Object Visit(NamedInstance e)
	{
		e.name = applyDefaultPfx(e.name);
		return super.Visit(e);
	}

	@Override
	public Object Visit(DisjointUnion e)
	{
		e.name = applyDefaultPfx(e.name);
		return super.Visit(e);
	}

	@Override
	public Object Visit(InstanceOf e)
	{
		if (e.I instanceof NamedInstance)
		{
			(e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name = applyDefaultPfx((e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name);
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(RelatedInstances e)
	{
		if (e.I instanceof NamedInstance)
		{
			(e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name = applyDefaultPfx((e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name);
		}
		if (e.J instanceof NamedInstance)
		{
			(e.J instanceof NamedInstance ? (NamedInstance)e.J : null).name = applyDefaultPfx((e.J instanceof NamedInstance ? (NamedInstance)e.J : null).name);
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(InstanceValue e)
	{
		if (e.I instanceof NamedInstance)
		{
			(e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name = applyDefaultPfx((e.I instanceof NamedInstance ? (NamedInstance)e.I : null).name);
		}
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlRole e)
	{
		e.R = applyDefaultPfx(e.R);
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlDataProperty e)
	{
		e.R = applyDefaultPfx(e.R);
		return super.Visit(e);
	}

	@Override
	public Object Visit(SwrlIVal e)
	{
		e.I = applyDefaultPfx(e.I);
		return super.Visit(e);
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