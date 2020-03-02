package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%THEN+35
public class THEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "THEN";
	}
	@Override
	public int getYynum()
	{
		return 35;
	}
	public THEN(Lexer yyl)
	{
		super(yyl);
	}
}