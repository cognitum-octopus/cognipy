package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

public class dl extends Parser
{
	public dl()
	{
		super(new yydl(), new tokens());
	}
	public dl(YyParser syms)
	{
		super(syms, new tokens());
	}
	public dl(YyParser syms, ErrorHandler erh)
	{
		super(syms, new tokens(erh));
	}

}