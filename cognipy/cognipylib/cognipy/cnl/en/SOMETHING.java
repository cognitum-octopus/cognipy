package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SOMETHING+41
public class SOMETHING extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SOMETHING";
	}
	@Override
	public int getYynum()
	{
		return 41;
	}
	public SOMETHING(Lexer yyl)
	{
		super(yyl);
	}
}