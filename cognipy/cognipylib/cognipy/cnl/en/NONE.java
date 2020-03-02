package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NONE+42
public class NONE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NONE";
	}
	@Override
	public int getYynum()
	{
		return 42;
	}
	public NONE(Lexer yyl)
	{
		super(yyl);
	}
}