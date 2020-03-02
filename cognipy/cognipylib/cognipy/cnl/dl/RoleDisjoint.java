package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+RoleDisjoint+76
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Role)] public partial class RoleDisjoint: Statement
public class RoleDisjoint extends Statement
{
	public RoleDisjoint(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> Disjoints;
	public RoleDisjoint(Parser yyp, Node c, Node d, Modality m)
	{
		super(yyp);
		Disjoints = new ArrayList<Node>();
		Disjoints.add(c.me());
		Disjoints.add(d.me());
		modality = m;
	}
	public RoleDisjoint(Parser yyp, NodeList il, Modality m)
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
		return "RoleDisjoint";
	}
	@Override
	public int getYynumDl()
	{
		return 76;
	}
}