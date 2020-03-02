package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+InstanceList+88
public class InstanceList extends PartialSymbol
{
	public ArrayList<Instance> List;
	public InstanceList(Parser yyp)
	{
		super(yyp);
	}
	public InstanceList(Parser yyp, Instance I)
	{
		super(yyp);
	List = new ArrayList<Instance>();
	List.add(I);
	}
	public InstanceList(Parser yyp, InstanceList cl, Instance I)
	{
		super(yyp);
	List = cl.List;
	List.add(I);
	}


	@Override
	public String getYynameDl()
	{
		return "InstanceList";
	}
	@Override
	public int getYynumDl()
	{
		return 88;
	}
}