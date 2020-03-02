package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SOME+60
public class SOME extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SOME";
	}
	@Override
	public int getYynum()
	{
		return 60;
	}
	public SOME(Lexer yyl)
	{
		super(yyl);
	}
}