package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+Disjoint+71
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Concept)] public partial class Disjoint: Statement
public class Disjoint extends Statement
{
	public Disjoint(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> Disjoints;
	public Disjoint(Parser yyp, Node c, Node d, Modality m)
	{
		super(yyp);
		Disjoints = new ArrayList<Node>();
		Disjoints.add(c.me());
		Disjoints.add(d.me());
		modality = m;
	}
	public Disjoint(Parser yyp, NodeList il, Modality m)
	{
		super(yyp);
		Disjoints = new ArrayList<Node>();
		for (Node e : il.List)
		{
			Disjoints.add(e.me());
		}
		modality = m;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "Disjoint";
	}
	@Override
	public int getYynumDl()
	{
		return 71;
	}
}