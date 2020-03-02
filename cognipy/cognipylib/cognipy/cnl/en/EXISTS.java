package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EXISTS+68
public class EXISTS extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EXISTS";
	}
	@Override
	public int getYynum()
	{
		return 68;
	}
	public EXISTS(Lexer yyl)
	{
		super(yyl);
	}
}