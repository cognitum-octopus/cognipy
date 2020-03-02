package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+ExeStatement+149
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Rule)] public partial class ExeStatement: Statement
public class ExeStatement extends Statement
{
	public ExeStatement(Parser yyp)
	{
		super(yyp);
	}
	public SwrlItemList slp;
	public SwrlVarList args;
	public String exe;
	public ExeStatement(Parser yyp, SwrlItemList slp_, SwrlVarList args_, String exe_)
	{
		super(yyp);
		slp = slp_;
		args = args_;
		exe = exe_;
	}

	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "ExeStatement";
	}
	@Override
	public int getYynumDl()
	{
		return 149;
	}
}