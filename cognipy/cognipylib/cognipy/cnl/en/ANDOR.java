package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ANDOR+51
public class ANDOR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ANDOR";
	}
	@Override
	public int getYynum()
	{
		return 51;
	}
	public ANDOR(Lexer yyl)
	{
		super(yyl);
	}
}