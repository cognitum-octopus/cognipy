package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%COMMA+13
public class COMMA extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "COMMA";
	}
	@Override
	public int getYynum()
	{
		return 13;
	}
	public COMMA(Lexer yyl)
	{
		super(yyl);
	}
}