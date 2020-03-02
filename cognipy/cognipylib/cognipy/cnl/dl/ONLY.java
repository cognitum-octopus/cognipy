package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%ONLY+32
public class ONLY extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "ONLY";
	}
	@Override
	public int getYynum()
	{
		return 32;
	}
	public ONLY(Lexer yyl)
	{
		super(yyl);
	}
}