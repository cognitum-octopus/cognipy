package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%LE+36
public class LE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "LE";
	}
	@Override
	public int getYynum()
	{
		return 36;
	}
	public LE(Lexer yyl)
	{
		super(yyl);
	}
}