package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+ComplexRoleInclusion+77
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Role)] public partial class ComplexRoleInclusion: Statement
public class ComplexRoleInclusion extends Statement
{
	public ComplexRoleInclusion(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> RoleChain;
	public Node R;
	public ComplexRoleInclusion(Parser yyp, RoleChain cn, Node r, Modality m)
	{
		super(yyp);
	RoleChain = cn.List;
	R = r.me();
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
		return "ComplexRoleInclusion";
	}
	@Override
	public int getYynumDl()
	{
		return 77;
	}
}