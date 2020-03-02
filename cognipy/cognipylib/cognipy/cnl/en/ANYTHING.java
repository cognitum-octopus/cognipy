package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ANYTHING+25
public class ANYTHING extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ANYTHING";
	}
	@Override
	public int getYynum()
	{
		return 25;
	}
	public ANYTHING(Lexer yyl)
	{
		super(yyl);
	}
}