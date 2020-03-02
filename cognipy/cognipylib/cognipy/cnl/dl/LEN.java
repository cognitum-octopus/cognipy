package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%LEN+41
public class LEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "LEN";
	}
	@Override
	public int getYynum()
	{
		return 41;
	}
	public LEN(Lexer yyl)
	{
		super(yyl);
	}
}