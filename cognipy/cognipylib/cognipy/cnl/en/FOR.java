package cognipy.cnl.en;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%FOR+67
public class FOR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "FOR";
	}
	@Override
	public int getYynum()
	{
		return 67;
	}
	public FOR(Lexer yyl)
	{
		super(yyl);
	}
}