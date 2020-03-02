package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+equivalence2+109
public class equivalence2 extends sentence
{
	public equivalence2(Parser yyp)
	{
		super(yyp);
	}
	public orloop c;
	public orloop d;

	public equivalence2(Parser yyp, orloop c_, String modality_, orloop d_)
	{
		super(yyp);
	c = c_;
	d = d_;
	modality = modality_;
	}
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}


	@Override
	public String getYynameEndl()
	{
		return "equivalence2";
	}
	@Override
	public int getYynumEndl()
	{
		return 109;
	}
}