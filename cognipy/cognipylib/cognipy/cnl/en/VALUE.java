package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%VALUE+61
public class VALUE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "VALUE";
	}
	@Override
	public int getYynum()
	{
		return 61;
	}
	public VALUE(Lexer yyl)
	{
		super(yyl);
	}
}