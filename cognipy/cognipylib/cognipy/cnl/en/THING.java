package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%THING+43
public class THING extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "THING";
	}
	@Override
	public int getYynum()
	{
		return 43;
	}
	public THING(Lexer yyl)
	{
		super(yyl);
	}
}