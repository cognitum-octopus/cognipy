package cognipy.cnl.dl;

import tools.*;
import cognipy.*;
import cognipy.cnl.*;

//%EQVR+24
public class EQVR extends TOKEN
{
	@Override
	public String getYyname()
	{
		return "EQVR";
	}
	@Override
	public int getYynum()
	{
		return 24;
	}
	public EQVR(Lexer yyl)
	{
		super(yyl);
	}
}