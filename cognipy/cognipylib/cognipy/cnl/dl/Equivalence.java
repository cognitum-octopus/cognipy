package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+Equivalence+70
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Concept)] public partial class Equivalence: Statement
public class Equivalence extends Statement
{
	public Equivalence(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> Equivalents;
	public Equivalence(Parser yyp, Node c, Node d, Modality m)
	{
		super(yyp);
		Equivalents = new ArrayList<Node>();
		Equivalents.add(c.me());
		Equivalents.add(d.me());
		modality = m;
	}
	public Equivalence(Parser yyp, NodeList il, Modality m)
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
		return "Equivalence";
	}
	@Override
	public int getYynumDl()
	{
		return 70;
	}
}