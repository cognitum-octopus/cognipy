package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%THE+19
public class THE extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "THE";
	}
	@Override
	public int getYynum()
	{
		return 19;
	}
	public THE(Lexer yyl)
	{
		super(yyl);
	}
}