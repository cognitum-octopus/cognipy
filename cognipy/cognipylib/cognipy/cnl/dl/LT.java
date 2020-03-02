package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%LT+38
public class LT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "LT";
	}
	@Override
	public int getYynum()
	{
		return 38;
	}
	public LT(Lexer yyl)
	{
		super(yyl);
	}
}