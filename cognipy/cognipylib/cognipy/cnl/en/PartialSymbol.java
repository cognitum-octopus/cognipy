package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class PartialSymbol extends TOKEN
{
	public PartialSymbol(Parser yyp)
	{
		super(yyp);
	}

	@Override
	public String getYyname()
	{
		return (String)this.getClass().GetProperty("yyname_" + this.yyps.getClass().getSimpleName()).GetValue(this, new Object[] { });
	}
	@Override
	public int getYynum()
	{
		return (Integer)this.getClass().GetProperty("yynum_" + this.yyps.getClass().getSimpleName()).GetValue(this, new Object[] { });
	}

	public String getYynameEndl()
	{
		return null;
	}
	public int getYynumEndl()
	{
		return 0;
	}
}