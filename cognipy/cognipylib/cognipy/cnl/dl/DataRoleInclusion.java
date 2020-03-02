package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DataRoleInclusion+78
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Role)] public partial class DataRoleInclusion: Statement
public class DataRoleInclusion extends Statement
{
	public DataRoleInclusion(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public Node D;
	public DataRoleInclusion(Parser yyp, Node c, Node d, Modality m)
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
		return "DataRoleInclusion";
	}
	@Override
	public int getYynumDl()
	{
		return 78;
	}
}