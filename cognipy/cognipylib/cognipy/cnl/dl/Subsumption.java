package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Subsumption+67
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Concept)] public partial class Subsumption: Statement
public class Subsumption extends Statement
{
	public Subsumption(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public Node D;
	public Subsumption(Parser yyp, Node c, Node d, Modality m)
	{
		super(yyp);
		C = c.me();
		D = d.me();
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
		return "Subsumption";
	}
	@Override
	public int getYynumDl()
	{
		return 67;
	}
}