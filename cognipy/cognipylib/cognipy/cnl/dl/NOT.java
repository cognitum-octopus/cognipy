package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%NOT+31
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
		return 31;
	}
	public NOT(Lexer yyl)
	{
		super(yyl);
	}
}