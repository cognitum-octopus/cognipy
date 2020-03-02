package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;
import java.util.*;

//%+SameInstances+90
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Instance)] public partial class SameInstances: Statement
public class SameInstances extends Statement
{
	public SameInstances(Parser yyp)
	{
		super(yyp);
	}
	public ArrayList<Instance> Instances;
	public SameInstances(Parser yyp, ID I, ID J, Modality m)
	{
		super(yyp);
		Instances = new ArrayList<Instance>();
		Instances.add(new NamedInstance(yyp, I));
		Instances.add(new NamedInstance(yyp, J));
		modality = m;
	}
	public SameInstances(Parser yyp, InstanceList il, Modality m)
	{
		super(yyp);
		Instances = il.List;
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
		return "SameInstances";
	}
	@Override
	public int getYynumDl()
	{
		return 90;
	}
}