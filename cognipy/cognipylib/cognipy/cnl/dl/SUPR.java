package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUPR+23
public class SUPR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUPR";
	}
	@Override
	public int getYynum()
	{
		return 23;
	}
	public SUPR(Lexer yyl)
	{
		super(yyl);
	}
}