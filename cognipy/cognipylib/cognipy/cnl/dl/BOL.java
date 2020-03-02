package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%+BOL+8
public class BOL extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "BOL";
	}
	@Override
	public int getYynum()
	{
		return 8;
	}
	public BOL(Lexer yyl)
	{
		super(yyl);
	}
}