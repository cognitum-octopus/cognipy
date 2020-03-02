package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%IT+36
public class IT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "IT";
	}
	@Override
	public int getYynum()
	{
		return 36;
	}
	public IT(Lexer yyl)
	{
		super(yyl);
	}
}