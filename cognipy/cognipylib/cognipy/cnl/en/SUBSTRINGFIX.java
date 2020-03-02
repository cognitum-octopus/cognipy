package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUBSTRINGFIX+73
public class SUBSTRINGFIX extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUBSTRINGFIX";
	}
	@Override
	public int getYynum()
	{
		return 73;
	}
	public SUBSTRINGFIX(Lexer yyl)
	{
		super(yyl);
	}
}