package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%GE+37
public class GE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "GE";
	}
	@Override
	public int getYynum()
	{
		return 37;
	}
	public GE(Lexer yyl)
	{
		super(yyl);
	}
}