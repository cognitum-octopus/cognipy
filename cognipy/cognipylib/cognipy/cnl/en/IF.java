package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%IF+34
public class IF extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "IF";
	}
	@Override
	public int getYynum()
	{
		return 34;
	}
	public IF(Lexer yyl)
	{
		super(yyl);
	}
}