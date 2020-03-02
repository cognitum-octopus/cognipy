package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%COLON+82
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
		return 82;
	}
	public COLON(Lexer yyl)
	{
		super(yyl);
	}
}