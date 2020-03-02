package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%MATCHES+16
public class MATCHES extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "MATCHES";
	}
	@Override
	public int getYynum()
	{
		return 16;
	}
	public MATCHES(Lexer yyl)
	{
		super(yyl);
	}
}