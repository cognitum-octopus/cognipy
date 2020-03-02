package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%DOT+54
public class DOT extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "DOT";
	}
	@Override
	public int getYynum()
	{
		return 54;
	}
	public DOT(Lexer yyl)
	{
		super(yyl);
	}
}