package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+InstanceValue+87
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Instance)] public partial class InstanceValue: Statement
public class InstanceValue extends Statement
{
	public InstanceValue(Parser yyp)
	{
		super(yyp);
	}
	public Node R;
	public Instance I;
	public Value V;
	public InstanceValue(Parser yyp, Node r, ID i, Value v, Modality m)
	{
		super(yyp);
	R = r.me();
	I = new NamedInstance(null);
	I.name = i.getYytext();
	V = v;
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
		return "InstanceValue";
	}
	@Override
	public int getYynumDl()
	{
		return 87;
	}
}