package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%TIMES+83
public class TIMES extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "TIMES";
	}
	@Override
	public int getYynum()
	{
		return 83;
	}
	public TIMES(Lexer yyl)
	{
		super(yyl);
	}
}