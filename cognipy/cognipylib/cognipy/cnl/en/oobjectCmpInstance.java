package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectCmpInstance+150
public class oobjectCmpInstance extends oobjectCardinal
{
	public oobjectCmpInstance(Parser yyp)
	{
		super(yyp);
	}
	public instance i;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public oobjectCmpInstance(Parser yyp, String Cmp_, String Cnt_, instance i_)
	{
		super(yyp);
	i = i_;
	Cmp = Cmp_;
	Cnt = Cnt_;
	}


	@Override
	public String getYynameEndl()
	{
		return "oobjectCmpInstance";
	}
	@Override
	public int getYynumEndl()
	{
		return 150;
	}
}