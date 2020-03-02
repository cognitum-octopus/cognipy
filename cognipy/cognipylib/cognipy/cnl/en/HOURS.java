package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%HOURS+86
public class HOURS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "HOURS";
	}
	@Override
	public int getYynum()
	{
		return 86;
	}
	public HOURS(Lexer yyl)
	{
		super(yyl);
	}
}