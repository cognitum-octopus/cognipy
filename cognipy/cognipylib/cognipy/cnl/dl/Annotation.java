package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+Annotation+68
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Annotation)] public partial class Annotation: Statement
public class Annotation extends Statement
{
	public Annotation(Parser yyp)
	{
		super(yyp);
	}
	public String txt;
	public Annotation(Parser yyp, String txt)
	{
		super(yyp);
		this.txt = txt;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "Annotation";
	}
	@Override
	public int getYynumDl()
	{
		return 68;
	}
}