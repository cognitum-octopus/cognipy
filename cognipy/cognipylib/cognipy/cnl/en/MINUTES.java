package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%MINUTES+87
public class MINUTES extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "MINUTES";
	}
	@Override
	public int getYynum()
	{
		return 87;
	}
	public MINUTES(Lexer yyl)
	{
		super(yyl);
	}
}