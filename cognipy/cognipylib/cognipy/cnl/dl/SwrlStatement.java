package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+SwrlStatement+131
//C# TO JAVA CONVERTER TODO TASK: Java annotations will not correspond to .NET attributes:
//ORIGINAL LINE: [StatementAttr(StatementType.Rule)] public partial class SwrlStatement: Statement
public class SwrlStatement extends Statement
{
	public SwrlStatement(Parser yyp)
	{
		super(yyp);
	}
	public SwrlItemList slp, slc;

	public SwrlStatement(Parser yyp, SwrlItemList slp_, SwrlItemList slc_)
	{
		this(yyp, slp_, slc_, Modality.IS);
	}

//C# TO JAVA CONVERTER NOTE: Java does not support optional parameters. Overloaded method(s) are created above:
//ORIGINAL LINE: public SwrlStatement(Parser yyp, SwrlItemList slp_, SwrlItemList slc_, Modality modality = Modality.IS)
	public SwrlStatement(Parser yyp, SwrlItemList slp_, SwrlItemList slc_, Modality modality)
	{
		super(yyp);
	slp = slp_;
	slc = slc_;
	this.modality = modality;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameDl()
	{
		return "SwrlStatement";
	}
	@Override
	public int getYynumDl()
	{
		return 131;
	}
}