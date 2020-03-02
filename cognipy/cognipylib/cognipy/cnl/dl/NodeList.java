package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+NodeList+89
public class NodeList extends PartialSymbol
{
	public NodeList(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Node> List;
	public NodeList(Parser yyp, Node I)
	{
		super(yyp);
	List = new ArrayList<Node>();
	List.add(I);
	}
	public NodeList(Parser yyp, NodeList cl, Node I)
	{
		super(yyp);
	List = cl.List;
	List.add(I);
	}


	@Override
	public String getYynameDl()
	{
		return "NodeList";
	}
	@Override
	public int getYynumDl()
	{
		return 89;
	}
}