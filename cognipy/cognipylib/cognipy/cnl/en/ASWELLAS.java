package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ASWELLAS+52
public class ASWELLAS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ASWELLAS";
	}
	@Override
	public int getYynum()
	{
		return 52;
	}
	public ASWELLAS(Lexer yyl)
	{
		super(yyl);
	}
}