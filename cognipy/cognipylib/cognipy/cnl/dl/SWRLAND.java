package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SWRLAND+51
public class SWRLAND extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SWRLAND";
	}
	@Override
	public int getYynum()
	{
		return 51;
	}
	public SWRLAND(Lexer yyl)
	{
		super(yyl);
	}
}