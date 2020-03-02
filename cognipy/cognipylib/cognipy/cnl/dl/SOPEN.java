package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SOPEN+14
public class SOPEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SOPEN";
	}
	@Override
	public int getYynum()
	{
		return 14;
	}
	public SOPEN(Lexer yyl)
	{
		super(yyl);
	}
}