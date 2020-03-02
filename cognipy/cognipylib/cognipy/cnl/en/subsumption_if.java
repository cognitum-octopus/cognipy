package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+subsumption_if+107
public class subsumption_if extends sentence
{
	public subsumption_if(Parser yyp)
	{
		super(yyp);
	}
	public orloop c;
	public orloop d;

	public subsumption_if(Parser yyp, orloop c_, String modality_, orloop d_)
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
		return "subsumption_if";
	}
	@Override
	public int getYynumEndl()
	{
		return 107;
	}
}