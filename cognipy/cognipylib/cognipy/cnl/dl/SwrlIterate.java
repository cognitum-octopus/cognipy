package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlIterate+132
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Rule)] public partial class SwrlIterate: Statement
public class SwrlIterate extends Statement
{
	public SwrlIterate(Parser yyp)
	{
		super(yyp);
	}
	public SwrlItemList slp, slc;
	public SwrlVarList vars;
	public SwrlIterate(Parser yyp, SwrlItemList slp_, SwrlItemList slc_, SwrlVarList vars_)
	{
		super(yyp);
	slp = slp_;
	slc = slc_;
	this.vars = vars_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlIterate";
	}
	@Override
	public int getYynumDl()
	{
		return 132;
	}
}