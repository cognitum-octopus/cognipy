package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%BIGNAME+98
public class BIGNAME extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "BIGNAME";
	}
	@Override
	public int getYynum()
	{
		return 98;
	}
	public BIGNAME(Lexer yyl)
	{
		super(yyl);
	}
}