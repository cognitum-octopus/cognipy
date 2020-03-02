package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%THINGS+44
public class THINGS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "THINGS";
	}
	@Override
	public int getYynum()
	{
		return 44;
	}
	public THINGS(Lexer yyl)
	{
		super(yyl);
	}
}