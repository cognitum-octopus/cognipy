package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%MINUS+80
public class MINUS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "MINUS";
	}
	@Override
	public int getYynum()
	{
		return 80;
	}
	public MINUS(Lexer yyl)
	{
		super(yyl);
	}
}