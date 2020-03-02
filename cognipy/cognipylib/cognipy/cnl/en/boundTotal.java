package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+boundTotal+190
public class boundTotal extends abstractbound
{
	public boundTotal(Parser yyp)
	{
		super(yyp);
	}
	public String Kind;
	@Override
	public Object accept(IVisitor v)
	{
		return v.Visit(this);
	}
	public boundTotal(Parser yyp, String kind)
	{
		super(yyp);
	Kind = kind;
	}


	@Override
	public String getYynameEndl()
	{
		return "boundTotal";
	}
	@Override
	public int getYynumEndl()
	{
		return 190;
	}
}