package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUPD+26
public class SUPD extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUPD";
	}
	@Override
	public int getYynum()
	{
		return 26;
	}
	public SUPD(Lexer yyl)
	{
		super(yyl);
	}
}