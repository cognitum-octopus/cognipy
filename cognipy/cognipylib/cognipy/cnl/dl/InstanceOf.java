package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+InstanceOf+84
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Instance)] public partial class InstanceOf: Statement
public class InstanceOf extends Statement
{
	public InstanceOf(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public Instance I;
	public InstanceOf(Parser yyp, Node c, ID i, Modality m)
	{
		super(yyp);
	C = c.me();
	I = new NamedInstance(null);
	I.name = i.getYytext();
	modality = m;
	}
	public InstanceOf(Parser yyp, Node c, Modality m)
	{
		super(yyp);
	C = c.me();
	I = new UnnamedInstance(null, false, new CNL.DL.Top(null));
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
		return "InstanceOf";
	}
	@Override
	public int getYynumDl()
	{
		return 84;
	}
}