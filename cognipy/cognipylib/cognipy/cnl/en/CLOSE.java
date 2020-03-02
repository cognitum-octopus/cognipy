package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%CLOSE+94
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
		return 94;
	}
	public CLOSE(Lexer yyl)
	{
		super(yyl);
	}
}