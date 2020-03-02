package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DAYS+85
public class DAYS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DAYS";
	}
	@Override
	public int getYynum()
	{
		return 85;
	}
	public DAYS(Lexer yyl)
	{
		super(yyl);
	}
}