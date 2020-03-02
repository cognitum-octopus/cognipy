package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NE+35
public class NE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NE";
	}
	@Override
	public int getYynum()
	{
		return 35;
	}
	public NE(Lexer yyl)
	{
		super(yyl);
	}
}