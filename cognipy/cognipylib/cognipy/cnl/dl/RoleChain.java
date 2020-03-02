package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+RoleChain+130
public class RoleChain extends PartialSymbol
{
	public RoleChain(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> List;
	public RoleChain(Parser yyp, Node R, Node S)
	{
		super(yyp);
	List = new ArrayList<Node>();
	List.add(R.me());
	List.add(S.me());
	}
	public RoleChain(Parser yyp, RoleChain cl, Node R)
	{
		super(yyp);
	List = cl.List;
	List.add(R.me());
	}


	@Override
	public String getYynameDl()
	{
		return "RoleChain";
	}
	@Override
	public int getYynumDl()
	{
		return 130;
	}
}