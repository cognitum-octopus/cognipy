package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%WORDNUM+59
public class WORDNUM extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "WORDNUM";
	}
	@Override
	public int getYynum()
	{
		return 59;
	}
	public WORDNUM(Lexer yyl)
	{
		super(yyl);
	}
}