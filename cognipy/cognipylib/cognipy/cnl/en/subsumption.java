package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subsumption+105
public class subsumption extends sentence
{
	public subsumption(Parser yyp)
	{
		super(yyp);
	}
	public subject c;
	public orloop d;

	public subsumption(Parser yyp, subject c_, String modality_, orloop d_)
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
		return "subsumption";
	}
	@Override
	public int getYynumEndl()
	{
		return 105;
	}
}