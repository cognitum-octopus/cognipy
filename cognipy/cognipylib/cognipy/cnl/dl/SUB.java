package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUB+18
public class SUB extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUB";
	}
	@Override
	public int getYynum()
	{
		return 18;
	}
	public SUB(Lexer yyl)
	{
		super(yyl);
	}
}