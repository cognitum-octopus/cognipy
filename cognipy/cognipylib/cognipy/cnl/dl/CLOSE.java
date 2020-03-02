package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%CLOSE+12
public class CLOSE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "CLOSE";
	}
	@Override
	public int getYynum()
	{
		return 12;
	}
	public CLOSE(Lexer yyl)
	{
		super(yyl);
	}
}