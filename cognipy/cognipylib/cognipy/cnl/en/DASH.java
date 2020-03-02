package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DASH+81
public class DASH extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DASH";
	}
	@Override
	public int getYynum()
	{
		return 81;
	}
	public DASH(Lexer yyl)
	{
		super(yyl);
	}
}