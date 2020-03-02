package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%VERYBIGNAME+99
public class VERYBIGNAME extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "VERYBIGNAME";
	}
	@Override
	public int getYynum()
	{
		return 99;
	}
	public VERYBIGNAME(Lexer yyl)
	{
		super(yyl);
	}
}