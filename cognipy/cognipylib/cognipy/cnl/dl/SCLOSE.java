package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SCLOSE+15
public class SCLOSE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SCLOSE";
	}
	@Override
	public int getYynum()
	{
		return 15;
	}
	public SCLOSE(Lexer yyl)
	{
		super(yyl);
	}
}