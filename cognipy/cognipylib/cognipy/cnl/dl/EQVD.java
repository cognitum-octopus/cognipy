package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EQVD+27
public class EQVD extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EQVD";
	}
	@Override
	public int getYynum()
	{
		return 27;
	}
	public EQVD(Lexer yyl)
	{
		super(yyl);
	}
}