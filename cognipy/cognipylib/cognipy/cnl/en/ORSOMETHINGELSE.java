package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ORSOMETHINGELSE+26
public class ORSOMETHINGELSE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ORSOMETHINGELSE";
	}
	@Override
	public int getYynum()
	{
		return 26;
	}
	public ORSOMETHINGELSE(Lexer yyl)
	{
		super(yyl);
	}
}