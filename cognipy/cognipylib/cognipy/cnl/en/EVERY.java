package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EVERY+37
public class EVERY extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EVERY";
	}
	@Override
	public int getYynum()
	{
		return 37;
	}
	public EVERY(Lexer yyl)
	{
		super(yyl);
	}
}