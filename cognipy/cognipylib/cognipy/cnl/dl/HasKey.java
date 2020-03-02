package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+HasKey+92
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Concept)] public partial class HasKey: Statement
public class HasKey extends Statement
{
	public HasKey(Parser yyp)
	{
		super(yyp);
	}
	public Node C;
	public ArrayList<Node> Roles;
	public ArrayList<Node> DataRoles;
	public HasKey(Parser yyp, Node c, NodeList roles, NodeList dataroles)
	{
		super(yyp);
		C = c;
		Roles = (roles == null ? new ArrayList<Node>() : roles.List);
		DataRoles = (dataroles == null ? new ArrayList<Node>() : dataroles.List);
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "HasKey";
	}
	@Override
	public int getYynumDl()
	{
		return 92;
	}
}