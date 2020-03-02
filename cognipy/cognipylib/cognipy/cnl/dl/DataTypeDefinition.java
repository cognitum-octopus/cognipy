package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+DataTypeDefinition+73
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Role)] public partial class DataTypeDefinition: Statement
public class DataTypeDefinition extends Statement
{
	public DataTypeDefinition(Parser yyp)
	{
		super(yyp);
	}
	public String name;
	public AbstractBound B;
	public DataTypeDefinition(Parser yyp, ID i, AbstractBound B)
	{
		super(yyp);
		name = i.getYytext();
		this.B = B.me();
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "DataTypeDefinition";
	}
	@Override
	public int getYynumDl()
	{
		return 73;
	}
}