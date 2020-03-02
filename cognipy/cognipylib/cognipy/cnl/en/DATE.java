package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DATE+64
public class DATE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DATE";
	}
	@Override
	public int getYynum()
	{
		return 64;
	}
	public DATE(Lexer yyl)
	{
		super(yyl);
	}
}