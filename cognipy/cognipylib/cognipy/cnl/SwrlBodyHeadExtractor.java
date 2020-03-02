package cognipy.cnl;

import cognipy.cnl.dl.*;
import cognipy.*;
import java.util.*;

public class SwrlBodyHeadExtractor extends GenericVisitor
{

	public final SwrlRuleStruct GetBodyAndHead(Paragraph e)
	{
		Object tempVar = e.accept(this);
		return tempVar instanceof SwrlRuleStruct ? (SwrlRuleStruct)tempVar : null;
	}

	private ArrayList<String> allheadPart = new ArrayList<String>();
	private ArrayList<String> allbodyPart = new ArrayList<String>();
	private boolean isBody = false;
	private void addToHeadOrBody(String val, String type)
	{
		if (!isBody && !allheadPart.contains(val + type))
		{
			allheadPart.add(val + type);
		}
		else if (isBody && !allbodyPart.contains(val + type))
		{
			allbodyPart.add(val + type);
		}
	}

	@Override
	public Object Visit(Paragraph e)
	{
		SwrlRuleStruct swrlStr = new SwrlRuleStruct();
		for (Statement x : e.Statements)
		{
			allheadPart.clear();
			allbodyPart.clear();
			x.accept(this);
			for (String hd : allheadPart)
			{
				if (!swrlStr.head.contains(hd))
				{
					swrlStr.head.add(hd);
				}
			}
			for (String bd : allbodyPart)
			{
				if (!swrlStr.body.contains(bd))
				{
					swrlStr.body.add(bd);
				}
			}
		}
		return swrlStr;
	}


	@Override
	public Object Visit(SwrlStatement e)
	{
		isBody = false;
		e.slp.accept(this);
		isBody = true;
		e.slc.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlItemList e)
	{
		for (SwrlItem i : e.list)
		{
			i.accept(this);
		}
		return e;
	}

	@Override
	public Object Visit(SwrlInstance e)
	{
		try (isKindOf.set("C"))
		{
			e.C.accept(this);
		}
		e.I.accept(this);

		return e;
	}

	@Override
	public Object Visit(SwrlRole e)
	{
		addToHeadOrBody(e.R.toString(), ":R");
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlSameAs e)
	{
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlDifferentFrom e)
	{
		e.I.accept(this);
		e.J.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlDataProperty e)
	{
		try (isKindOf.set("D"))
		{
			e.IO.accept(this);
			e.DO.accept(this);
		}
		addToHeadOrBody(e.R.toString(), ":R");
		return e;
	}

	@Override
	public Object Visit(SwrlDataRange e)
	{
		try (isKindOf.set("D"))
		{
			e.B.accept(this);
			e.DO.accept(this);
		}
		return e;
	}

	@Override
	public Object Visit(SwrlBuiltIn e)
	{
		return e;
	}

	@Override
	public Object Visit(ExeStatement e)
	{
		e.slp.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlIterate e)
	{
		e.slp.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlVarList e)
	{
		for (IExeVar x : e.list)
		{
			x.accept(this);
		}
		return e;
	}


	@Override
	public Object Visit(SwrlDVal e)
	{
		e.Val.accept(this);
		return e;
	}

	@Override
	public Object Visit(SwrlDVar e)
	{
		return e.VAR;
	}

	@Override
	public Object Visit(SwrlIVal e)
	{
		addToHeadOrBody(e.toString(), ":I");
		return e;
	}

	@Override
	public Object Visit(SwrlIVar e)
	{
		return e.VAR;
	}

	@Override
	public Object Visit(cognipy.cnl.dl.Atomic e)
	{
		if (isKindOf.get().equals("C"))
		{
			addToHeadOrBody(e.id, ":C");
		}
		return e;
	}

	@Override
	public Object Visit(cognipy.cnl.dl.Number e)
	{
		addToHeadOrBody(e.val, ":V");
		return e;
	}
	@Override
	public Object Visit(cognipy.cnl.dl.String e)
	{
		addToHeadOrBody(e.val, ":V");
		return e;
	}
	@Override
	public Object Visit(cognipy.cnl.dl.Float e)
	{
		addToHeadOrBody(e.val, ":V");
		return e;
	}
	@Override
	public Object Visit(cognipy.cnl.dl.Bool e)
	{
		addToHeadOrBody(e.val, ":V");
		return e;
	}

	@Override
	public Object Visit(DateTimeVal e)
	{
		addToHeadOrBody(e.val, ":V");
		return e;
	}
}