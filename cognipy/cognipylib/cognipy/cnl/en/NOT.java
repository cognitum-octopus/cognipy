package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NOT+48
public class NOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "NOT";
	}
	@Override
	public int getYynum()
	{
		return 48;
	}
	public NOT(Lexer yyl)
	{
		super(yyl);
	}
}