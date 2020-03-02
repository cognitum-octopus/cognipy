package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SOME+33
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
		return 33;
	}
	public SOME(Lexer yyl)
	{
		super(yyl);
	}
}