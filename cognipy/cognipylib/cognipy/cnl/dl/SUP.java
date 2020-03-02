package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%SUP+19
public class SUP extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "SUP";
	}
	@Override
	public int getYynum()
	{
		return 19;
	}
	public SUP(Lexer yyl)
	{
		super(yyl);
	}
}