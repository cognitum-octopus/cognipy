package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+RoleEquivalence+75
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Role)] public partial class RoleEquivalence: Statement
public class RoleEquivalence extends Statement
{
	public RoleEquivalence(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> Equivalents;
	public RoleEquivalence(Parser yyp, Node c, Node d, Modality m)
	{
		super(yyp);
		Equivalents = new ArrayList<Node>();
		Equivalents.add(c.me());
		Equivalents.add(d.me());
		modality = m;
	}
	public RoleEquivalence(Parser yyp, NodeList il, Modality m)
	{
		super(yyp);
		Equivalents = new ArrayList<Node>();
		for (Node e : il.List)
		{
			Equivalents.add(e.me());
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
		return "RoleEquivalence";
	}
	@Override
	public int getYynumDl()
	{
		return 75;
	}
}