package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+oobjectCardinal+148
public class oobjectCardinal extends oobjectRelated
{
	public oobjectCardinal(Parser yyp)
	{
		super(yyp);
	}
	public String Cmp;
	public String Cnt;


	@Override
	public String getYynameEndl()
	{
		return "oobjectCardinal";
	}
	@Override
	public int getYynumEndl()
	{
		return 148;
	}
}