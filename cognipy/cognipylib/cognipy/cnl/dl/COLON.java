package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%COLON+55
public class COLON extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "COLON";
	}
	@Override
	public int getYynum()
	{
		return 55;
	}
	public COLON(Lexer yyl)
	{
		super(yyl);
	}
}