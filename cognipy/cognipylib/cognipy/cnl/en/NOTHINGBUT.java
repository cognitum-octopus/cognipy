package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NOTHINGBUT+20
public class NOTHINGBUT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NOTHINGBUT";
	}
	@Override
	public int getYynum()
	{
		return 20;
	}
	public NOTHINGBUT(Lexer yyl)
	{
		super(yyl);
	}
}