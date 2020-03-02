package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+nosubsumption+106
public class nosubsumption extends sentence
{
	public nosubsumption(Parser yyp)
	{
		super(yyp);
	}
	public nosubject c;
	public orloop d;

	public nosubsumption(Parser yyp, nosubject c_, String modality_, orloop d_)
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
		return "nosubsumption";
	}
	@Override
	public int getYynumEndl()
	{
		return 106;
	}
}