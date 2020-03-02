package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class endl extends Parser
{
	public endl()
	{
		super(new yyendl(), new tokens());
	}
	public endl(YyParser syms)
	{
		super(syms, new tokens());
	}
	public endl(YyParser syms, ErrorHandler erh)
	{
		super(syms, new tokens(erh));
	}

}