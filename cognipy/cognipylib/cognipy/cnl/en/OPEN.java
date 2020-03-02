package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%OPEN+93
public class OPEN extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "OPEN";
	}
	@Override
	public int getYynum()
	{
		return 93;
	}
	public OPEN(Lexer yyl)
	{
		super(yyl);
	}
}