package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%CANNOT+47
public class CANNOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "CANNOT";
	}
	@Override
	public int getYynum()
	{
		return 47;
	}
	public CANNOT(Lexer yyl)
	{
		super(yyl);
	}
}