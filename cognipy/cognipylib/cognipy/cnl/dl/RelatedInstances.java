package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+RelatedInstances+85
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Instance)] public partial class RelatedInstances: Statement
public class RelatedInstances extends Statement
{
	public RelatedInstances(Parser yyp)
	{
		super(yyp);
	}
	public Node R;
	public Instance I;
	public Instance J;
	public RelatedInstances(Parser yyp, Node r, ID i, ID j, Modality m)
	{
		super(yyp);
	R = r.me();
	I = new NamedInstance(null);
	I.name = i.getYytext();
	J = new NamedInstance(null);
	J.name = j.getYytext();
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
		return "RelatedInstances";
	}
	@Override
	public int getYynumDl()
	{
		return 85;
	}
}