package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+DisjointUnion+72
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Concept)] public partial class DisjointUnion: Statement
public class DisjointUnion extends Statement
{
	public DisjointUnion(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public ArrayList<Node> Union;
	public DisjointUnion(Parser yyp, ID i, NodeList il, Modality m)
	{
		super(yyp);
		name = i.getYytext();
		Union = new ArrayList<Node>();
		for (Node e : il.List)
		{
			Union.add(e.me());
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
		return "DisjointUnion";
	}
	@Override
	public int getYynumDl()
	{
		return 72;
	}
}