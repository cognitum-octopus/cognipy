package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SHOULD+43
public class SHOULD extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SHOULD";
	}
	@Override
	public int getYynum()
	{
		return 43;
	}
	public SHOULD(Lexer yyl)
	{
		super(yyl);
	}
}