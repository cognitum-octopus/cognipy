package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%MUST+42
public class MUST extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "MUST";
	}
	@Override
	public int getYynum()
	{
		return 42;
	}
	public MUST(Lexer yyl)
	{
		super(yyl);
	}
}