package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%PLUS+79
public class PLUS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "PLUS";
	}
	@Override
	public int getYynum()
	{
		return 79;
	}
	public PLUS(Lexer yyl)
	{
		super(yyl);
	}
}