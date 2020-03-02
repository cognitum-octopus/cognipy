package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+CodeStatement+150
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Rule)] public partial class CodeStatement: Statement
public class CodeStatement extends Statement
{
	public CodeStatement(Parser yyp)
	{
		super(yyp);
	}
	public String exe;
	public CodeStatement(Parser yyp, String exe_)
	{
		super(yyp);
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
		return "CodeStatement";
	}
	@Override
	public int getYynumDl()
	{
		return 150;
	}
}