package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%FOLLOWEDBY+84
public class FOLLOWEDBY extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "FOLLOWEDBY";
	}
	@Override
	public int getYynum()
	{
		return 84;
	}
	public FOLLOWEDBY(Lexer yyl)
	{
		super(yyl);
	}
}