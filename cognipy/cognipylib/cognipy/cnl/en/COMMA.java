package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%COMMA+100
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
		return 100;
	}
	public COMMA(Lexer yyl)
	{
		super(yyl);
	}
}