package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+DataRoleEquivalence+79
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Role)] public partial class DataRoleEquivalence: Statement
public class DataRoleEquivalence extends Statement
{
	public DataRoleEquivalence(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> Equivalents;
	public DataRoleEquivalence(Parser yyp, Node c, Node d, Modality m)
	{
		super(yyp);
	Equivalents = new ArrayList<Node>();
	Equivalents.add(c.me());
	Equivalents.add(d.me());
	modality = m;
	}
	public DataRoleEquivalence(Parser yyp, NodeList il, Modality m)
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
		return "DataRoleEquivalence";
	}
	@Override
	public int getYynumDl()
	{
		return 79;
	}
}